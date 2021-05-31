package com.migrator.core.config;

import com.migrator.core.Consts;
import com.migrator.core.utils.FileUtil;
import com.migrator.core.utils.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MigratorConfig {
    private static Properties properties = null;
    static{
        String configPath = System.getProperty("config.path");
                    if(ObjectUtils.isEmpty(configPath)){
            configPath = "config.properties";
        }
        try {
            properties = FileUtil.loadProperties(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties(){
        return properties;
    }

    public static List<String> getMigrateTables(){
        String[] tables = properties.getProperty("tables", "").split(Consts.COMMA_DELIMITER);
        return new ArrayList<String>(Arrays.asList(tables));
    }

    public static int getMigratedThreadCount(){
        return Integer.parseInt(properties.getProperty("threads.count", "4"));
    }

    public static List<String> getExcludePKTables(){
        String[] tables = properties.getProperty("pk.excludes", "").split(Consts.COMMA_DELIMITER);
        return new ArrayList<String>(Arrays.asList(tables));
    }

    public static String getSourceDbName(){
        return properties.getProperty("source.dbname");
    }

    public static String getTargetDbName(){
        return properties.getProperty("target.dbname");
    }
}
