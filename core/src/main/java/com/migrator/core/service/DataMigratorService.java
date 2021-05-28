package com.migrator.core.service;

import com.migrator.core.Consts;
import com.migrator.core.config.MigratorConfig;
import com.migrator.core.dao.MigratorDataSource;
import com.migrator.core.dao.SchemaDAO;
import com.migrator.core.entity.PKRange;
import com.migrator.core.entity.schema.Column;
import com.migrator.core.entity.schema.Key;
import com.migrator.core.entity.schema.Table;
import com.migrator.core.entity.sqllite.MigrationSubTask;
import com.migrator.core.utils.DbUtil;
import com.migrator.core.utils.ObjectUtils;
import com.migrator.core.utils.StringUtil;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DataMigratorService {
    private List<Table> tables;
    private List<Column> columns;
    private List<Key> keys;
    /**33727 4t
     * 32066 8t
     * 21707 12t
     * 23555 16t
     */
    private static ExecutorService threadPool = Executors.newFixedThreadPool(12);

    private SchemaDAO schemaDAO;
    public DataMigratorService(SchemaDAO schemaDAO){
        this.schemaDAO = schemaDAO;
    }

    public void migrate() throws IOException, SQLException {
        Connection connection = MigratorDataSource.getInstance().getSourceDataSource().getConnection();
        String database = MigratorConfig.getSourceDbName();
        tables = schemaDAO.getTables(connection, database);
        columns = schemaDAO.getColumns(connection, database);
        keys = schemaDAO.getKeys(connection, database);
        if(!connection.isClosed()){
            connection.close();
        }

        long start = System.currentTimeMillis();
        List<String> tables = MigratorConfig.getMigrateTables();
        for(String table: tables) {
            migrateTable(table);
        }
        threadPool.shutdown();
        while(!threadPool.isTerminated()){
        }
        System.out.println(String.format("耗时%sms", System.currentTimeMillis() - start));
    }

    private void migrateTable(String tableName) throws SQLException {
        List<Column> cols = columns.stream().filter(t->t.getTableName().equals(tableName)).collect(Collectors.toList());
        Optional<Key> key =  keys.stream().filter(t->t.getTableName().equals(tableName) && t.getName().equals(Key.FLAG_PRIMARY)).findFirst();
        if(!key.isPresent()){
            System.out.println("无主键");
            return;
        }

        List<MigrationSubTask> subTasks = null;
        String whereCondition = Consts.EMPTY_STR, tableWhereKey = tableName + ".where";
        if(MigratorConfig.getProperties().containsKey(tableWhereKey)){
            whereCondition = MigratorConfig.getProperties().getProperty(tableWhereKey);
            if(ObjectUtils.isEmpty(whereCondition)){
                whereCondition = Consts.EMPTY_STR;
            }
        }

        try(Connection sourceConnection = MigratorDataSource.getInstance().getSourceDataSource().getConnection()) {
            String rangeSQL = String.format("SELECT MIN(id) AS min, MAX(id) AS max FROM %s %s", tableName, ObjectUtils.isEmpty(whereCondition) ? Consts.EMPTY_STR : (" WHERE " + whereCondition));
            PKRange range = DbUtil.getPKRange(sourceConnection, rangeSQL);
            subTasks = getSubTasks(range.getMin(), range.getMax());
        }
        if(subTasks == null || subTasks.isEmpty()){
            return;
        }

        List<String> colNames = cols.stream().map(Column::getName).collect(Collectors.toList());
        if(MigratorConfig.getExcludePKTables().contains(tableName)){
            colNames.remove(key.get().getColumnName());
        }
        String selectSQL = String.format("SELECT %s FROM %s where %s >=? and %s<? %s", StringUtil.join(colNames, Consts.COMMA_DELIMITER), tableName, key.get().getColumnName(), key.get().getColumnName(), ObjectUtils.isEmpty(whereCondition) ? Consts.EMPTY_STR: (" AND " + whereCondition));
        String insertSQL = String.format("INSERT INTO %s (%s) VALUES(%s)", tableName, StringUtil.join(colNames, Consts.COMMA_DELIMITER), StringUtil.repeatStr("?", colNames.size(), Consts.COMMA_DELIMITER));

        try {
            for(MigrationSubTask subTask: subTasks){
                threadPool.execute(new Runnable(){
                    @SneakyThrows
                    @Override
                    public void run() {
                        System.out.println(subTask.getMinId() + "," + subTask.getMaxId());
                        try(Connection sourceConnection = MigratorDataSource.getInstance().getSourceDataSource().getConnection();
                            Connection targetConnection = MigratorDataSource.getInstance().getTargetDataSource().getConnection();) {
                            List<Object[]> objects = DbUtil.getSourceTableData(sourceConnection, selectSQL, new Object[]{subTask.getMinId(), subTask.getMaxId()});
                            DbUtil.batchInsert(targetConnection, insertSQL, objects);
                        }
                    }
                });
            }
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    private List<MigrationSubTask> getSubTasks(long min, long max){
        List<MigrationSubTask> subTasks = new ArrayList<>();
        if(max == min){
            MigrationSubTask subTask = new MigrationSubTask();
            subTask.setMinId(min);
            subTask.setMaxId(min + 1);
            subTasks.add(subTask);
            return subTasks;
        }
        int step = 1000;
        if(max > 0){
            for(long start = min; start < max; start+=step){
                MigrationSubTask subTask = new MigrationSubTask();
                subTask.setMinId(start);
                subTask.setMaxId(start + step);
                subTasks.add(subTask);
            }
        }

        return subTasks;
    }
}
