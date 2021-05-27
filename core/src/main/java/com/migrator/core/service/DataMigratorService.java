package com.migrator.core.service;

import com.migrator.core.Consts;
import com.migrator.core.dao.MigratorDataSource;
import com.migrator.core.dao.SchemaDAO;
import com.migrator.core.entity.DatabaseInfo;
import com.migrator.core.entity.PKRange;
import com.migrator.core.entity.schema.Column;
import com.migrator.core.entity.schema.Key;
import com.migrator.core.entity.schema.Table;
import com.migrator.core.entity.sqllite.MigrationSubTask;
import com.migrator.core.utils.DbUtil;
import com.migrator.core.utils.FileUtil;
import com.migrator.core.utils.StringUtil;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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

    public void initialize() throws IOException, SQLException {
        /*
        Properties properties = FileUtil.loadProperties("db.properties");
        DatabaseInfo sourceDb = new DatabaseInfo(properties.getProperty("source.host"), Integer.parseInt(properties.getProperty("source.port")), properties.getProperty("source.username"), properties.getProperty("source.password"), properties.getProperty("source.dbname"));
        Connection connection = DbUtil.getConnection(sourceDb);
        tables = schemaDAO.getTables(connection, sourceDb);
        columns = schemaDAO.getColumns(connection, sourceDb);
        keys = schemaDAO.getKeys(connection, sourceDb);

        Connection targetConnection = DbUtil.getConnection(new DatabaseInfo(properties, "target"));
        migrateTable(connection, targetConnection, "account");

        if(!connection.isClosed()){
            connection.close();
        }
         */
        Connection connection = MigratorDataSource.DataSourceHolder.getSourceInstance().getConnection();
        String database = "bocai";
        tables = schemaDAO.getTables(connection, database);
        columns = schemaDAO.getColumns(connection, database);
        keys = schemaDAO.getKeys(connection, database);
        if(!connection.isClosed()){
            connection.close();
        }

        long start = System.currentTimeMillis();
        migrateTable("account");
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
        try(Connection sourceConnection = MigratorDataSource.DataSourceHolder.getSourceInstance().getConnection()) {
            String rangeSQL = String.format("SELECT MIN(id) AS min, MAX(id) AS max FROM %s", tableName);
            PKRange range = DbUtil.getPKRange(sourceConnection, rangeSQL);
            subTasks = getSubTasks(range.getMin(), range.getMax());
        }
        if(subTasks == null || subTasks.isEmpty()){
            return;
        }

        List<String> colNames = cols.stream().map(Column::getName).collect(Collectors.toList());
        String selectSQL = String.format("SELECT %s FROM %s where %s >=? and %s<?", StringUtil.join(colNames, Consts.COMMA_DELIMITER), tableName, key.get().getColumnName(), key.get().getColumnName());
        String insertSQL = String.format("INSERT INTO %s (%s) VALUES(%s)", tableName, StringUtil.join(colNames, Consts.COMMA_DELIMITER), StringUtil.repeatStr("?", colNames.size(), Consts.COMMA_DELIMITER));

        try {
            for(MigrationSubTask subTask: subTasks){
                threadPool.execute(new Runnable(){
                    @SneakyThrows
                    @Override
                    public void run() {
                        System.out.println(subTask.getMinId() + "," + subTask.getMaxId());
                        try(Connection sourceConnection = MigratorDataSource.DataSourceHolder.getSourceInstance().getConnection();
                            Connection targetConnection = MigratorDataSource.DataSourceHolder.getTargetInstance().getConnection();) {
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
