package com.migrator.core.utils;

import com.migrator.core.entity.DatabaseInfo;
import com.migrator.core.entity.PKRange;
import com.migrator.core.exception.DbConnectionException;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbUtil {
    public static <T> List<T> getBeanList(Connection connection, String sql, Class<T> clazz) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new BeanListHandler<>(clazz, new BasicRowProcessor(new BeanProcessor(MigrateBeanTools.customColumn(clazz)))));
    }

    public static <T> List<T> getBeanList(Connection connection, String sql, Class<T> clazz, Object... params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new BeanListHandler<>(clazz, new BasicRowProcessor(new BeanProcessor(MigrateBeanTools.customColumn(clazz)))), params);
    }

    public static <T> T getBean(Connection connection, String sql, Class<T> clazz) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new BeanHandler<>(clazz, new BasicRowProcessor(new BeanProcessor(MigrateBeanTools.customColumn(clazz)))));
    }

    public static <T> T getBean(Connection connection, String sql, Class<T> clazz, Object... params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new BeanHandler<>(clazz, new BasicRowProcessor(new BeanProcessor(MigrateBeanTools.customColumn(clazz)))), params);
    }

    public static Connection getConnection(String url, String username, String password) {
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new DbConnectionException("数据库连接失败,url:" + url + ",user:" + username);
        }
        return connection;
    }
    public static Connection getConnection(DatabaseInfo database) {
        return getConnection(database.getUrl(), database.getUsername(), database.getPassword());
    }


    public static void execute(Connection connection, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        runner.update(connection, sql);
    }

    public static List<Object[]> getSourceTableData(Connection connection, String sql, Object[] params) throws SQLException {
        List<ArrayList<Object>> data = new ArrayList();
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new ArrayListHandler(), params);
    }

    public static PKRange getPKRange(Connection connection, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, sql, new BeanHandler<PKRange>(PKRange.class));
    }

    public static void batchInsert(Connection connection, String insertSQL, List<Object[]> objects) throws SQLException {
        if(objects != null && objects.size() > 0){
            int rows = objects.size(), cols = objects.get(0).length, currRowIndex = 0;
            Object[][] params = new Object[rows][cols];
            for(Object[] objs: objects){
                for(int i = 0; i < cols; i++){
                    params[currRowIndex][i] = objs[i];
                }
                currRowIndex ++ ;
            }
            try {
                QueryRunner runner = new QueryRunner();
                runner.batch(connection, insertSQL, params);
                //int[] t = runner.batch(connection, insertSQL, params);
                //System.out.println(t.length);
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }
}