package com.migrator.core.dao;

import com.migrator.core.entity.DatabaseInfo;
import com.migrator.core.entity.schema.Column;
import com.migrator.core.entity.schema.Key;
import com.migrator.core.entity.schema.Table;
import com.migrator.core.utils.DbUtil;
import com.migrator.core.utils.FileUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SchemaDAO {
    public List<Table> getTables(Connection connection, String database) throws SQLException {
        return DbUtil.getBeanList(connection,
                FileUtil.getStringByClasspath("sql/table.sql"),
                Table.class, database);
    }

    public List<Column> getColumns(Connection connection, String database) throws SQLException {
        return DbUtil.getBeanList(connection,
                FileUtil.getStringByClasspath("sql/column.sql"),
                Column.class, database);
    }

    public List<Key> getKeys(Connection connection, String database) throws SQLException {
        return DbUtil.getBeanList(connection,
                FileUtil.getStringByClasspath("sql/key.sql"),
                Key.class, database);
    }
}
