package com.migrator.core.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.migrator.core.Consts;
import com.migrator.core.config.MigratorConfig;
import com.migrator.core.entity.DatabaseInfo;
import com.migrator.core.utils.FileUtil;
import com.migrator.core.utils.ObjectUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class MigratorDataSource {
    private DruidDataSource source;
    private DruidDataSource target;

    private DruidDataSource initDataSource(DatabaseInfo targetDb){
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

    public DataSource getSourceDataSource(){
        return source;
    }

    public DataSource getTargetDataSource(){
        return target;
    }

    private MigratorDataSource(){
        Properties properties = MigratorConfig.getProperties();
        DatabaseInfo sourceDb = new DatabaseInfo(properties, "source");
        DatabaseInfo targetDb = new DatabaseInfo(properties, "target");
        source = initDataSource(sourceDb);
        target = initDataSource(targetDb);
    }

    public static class DataSourceHolder{
        private static final  MigratorDataSource INSTANCE = new MigratorDataSource();
    }

    public static final MigratorDataSource getInstance(){
        return DataSourceHolder.INSTANCE;
    }
}
