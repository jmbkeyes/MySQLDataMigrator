package com.migrator.core.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.migrator.core.entity.DatabaseInfo;
import com.migrator.core.utils.FileUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class MigratorDataSource {
    public static class DataSourceHolder{
        private static DruidDataSource source;
        private static DruidDataSource target;

        public static DruidDataSource initDataSource(DatabaseInfo targetDb){
            DruidDataSource target = new DruidDataSource();
            target.setDriverClassName("com.mysql.jdbc.Driver");
            target.setUrl(targetDb.getUrl());
            target.setUsername(targetDb.getUsername());
            target.setPassword(targetDb.getPassword());
            target.setMinIdle(2);
            target.setMaxActive(10);
            target.setInitialSize(2);
            return target;
        }

        static{
            try {
                Properties properties = FileUtil.loadProperties("db.properties");
                DatabaseInfo sourceDb = new DatabaseInfo(properties, "source");
                DatabaseInfo targetDb = new DatabaseInfo(properties, "target");
                source = initDataSource(sourceDb);
                target = initDataSource(targetDb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static DataSource getSourceInstance(){
            return DataSourceHolder.source;
        }

        public static DataSource getTargetInstance(){
            return DataSourceHolder.target;
        }
    }
}
