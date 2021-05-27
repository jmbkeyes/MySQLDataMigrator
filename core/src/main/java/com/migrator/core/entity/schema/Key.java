package com.migrator.core.entity.schema;

import com.migrator.core.entity.schema.annotations.DbProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Key {
    public static final String FLAG_PRIMARY = "PRIMARY";

    @DbProperty("CONSTRAINT_SCHEMA")
    private String schema;
    @DbProperty("CONSTRAINT_NAME")
    private String name;
    @DbProperty("TABLE_NAME")
    private String tableName;
    @DbProperty("COLUMN_NAME")
    private String columnName;
    @DbProperty("ORDINAL_POSITION")
    private Long ordinalPosition;
    @DbProperty("POSITION_IN_UNIQUE_CONSTRAINT")
    private Long positionInUniqueConstraint;
    @DbProperty("REFERENCED_TABLE_SCHEMA")
    private String referencedSchema;
    @DbProperty("REFERENCED_TABLE_NAME")
    private String referencedTable;
    @DbProperty("REFERENCED_COLUMN_NAME")
    private String referencedColumn;

    private Table table;
}
