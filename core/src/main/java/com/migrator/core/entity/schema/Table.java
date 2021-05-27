package com.migrator.core.entity.schema;

import com.migrator.core.entity.schema.annotations.DbProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class Table {
    @DbProperty("TABLE_SCHEMA")
    private String schema;
    @DbProperty("TABLE_NAME")
    private String name;
    @DbProperty("TABLE_TYPE")
    private String type;
    @DbProperty("ENGINE")
    private String engine;
    @DbProperty("CREATE_TIME")
    private Timestamp createTime;
    @DbProperty("UPDATE_TIME")
    private Timestamp updateTime;
    @DbProperty("CHARACTER_SET_NAME")
    private String charset;
    @DbProperty("TABLE_COLLATION")
    private String collate;
    @DbProperty("TABLE_COMMENT")
    private String comment;

    private List<Column> columns;
    private List<Key> keys;
}
