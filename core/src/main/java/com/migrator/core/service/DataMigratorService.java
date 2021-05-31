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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class DataMigratorService {
    private List<Table> tables;
    private List<Column> columns;
    private List<Column> targetColumns;
    private List<Table> targetTables;
    private List<Key> keys;
    /**33727 4t
     * 32066 8t
     * 21707 12t
     * 23555 16t
     */
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MigratorConfig.getMigratedThreadCount());
    private final Semaphore semaphore = new Semaphore(1000);

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

        Connection targetConnection = MigratorDataSource.getInstance().getTargetDataSource().getConnection();
        String targetDatabase = MigratorConfig.getTargetDbName();
        targetTables = schemaDAO.getTables(connection, targetDatabase);
        targetColumns = schemaDAO.getColumns(connection, targetDatabase);

        if(!connection.isClosed()){
            connection.close();
        }
        if(!targetConnection.isClosed()){
            targetConnection.close();
        }

        long start = System.currentTimeMillis();
        List<String> tables = MigratorConfig.getMigrateTables();
        List<String> verifyTableDiffResult = verifyTableDiffs(tables);
        if(verifyTableDiffResult.size()>0){
            throw new RuntimeException(String.format("table(%s)'s definitions are not same.", StringUtil.join(verifyTableDiffResult, Consts.COMMA_DELIMITER)));
        }

        for(String table: tables) {
            migrateTable(table);
        }
        threadPool.shutdown();
        while(!threadPool.isTerminated()){
        }
        System.out.println(String.format("耗时%sms", System.currentTimeMillis() - start));
    }

    private List<String> verifyTableDiffs(List<String> migratedTables) {
        List<String> diffs = new ArrayList<>();
        for(String table :migratedTables){
            Optional<Table> source = tables.stream().filter(t->t.getName().equals(table)).findFirst();
            Optional<Table> target = targetTables.stream().filter(t->t.getName().equals(table)).findFirst();
            if(!source.isPresent() || !target.isPresent()){
                diffs.add(table);
            }else{
                List<Column> sourceCols = columns.stream().filter(t->t.getTableName().equals(table)).collect(Collectors.toList());
                List<Column> targetCols = targetColumns.stream().filter(t->t.getTableName().equals(table)).collect(Collectors.toList());
                if(sourceCols.size() != targetCols.size()){
                    diffs.add(table);
                }else{
                    sourceCols.sort(Comparator.comparing(Column::getName));
                    targetCols.sort(Comparator.comparing(Column::getName));
                    for(int i = 0, len = sourceCols.size(); i < len; i++){
                        if(!sourceCols.get(i).equals(targetCols.get(i))){
                            diffs.add(table);
                            break;
                        }
                    }
                }
            }
        }

        return diffs;
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

        for(MigrationSubTask subTask: subTasks){
            try {
                semaphore.acquire();
                threadPool.submit(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        System.out.println(subTask.getMinId() + "," + subTask.getMaxId());
                        try (Connection sourceConnection = MigratorDataSource.getInstance().getSourceDataSource().getConnection();
                             Connection targetConnection = MigratorDataSource.getInstance().getTargetDataSource().getConnection();) {
                             List<Object[]> objects = DbUtil.getSourceTableData(sourceConnection, selectSQL, new Object[]{subTask.getMinId(), subTask.getMaxId()});
                             DbUtil.batchInsert(targetConnection, insertSQL, objects);
                        } finally {
                            semaphore.release();
                        }
                    }
                });
            }catch (Exception ex){
                semaphore.release();
                System.out.println(ex);
            }
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
