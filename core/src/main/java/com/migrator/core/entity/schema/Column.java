package com.migrator.core.entity.schema;

import com.migrator.core.Consts;
import com.migrator.core.entity.schema.annotations.DbProperty;
import com.migrator.core.utils.ObjectUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Column {
    public static final String FLAG_NOT_NULL = "NO";
    public static final String FLAG_DEFAULT_NULL = "YES";
    public static final String FLAG_AUTO_INCREMENT = "auto_increment";

    @DbProperty("TABLE_SCHEMA")
    private String schema;
    @DbProperty("TABLE_NAME")
    private String tableName;
    @DbProperty("COLUMN_NAME")
    private String name;
    @DbProperty("COLUMN_DEFAULT")
    private String defaultValue;
    @DbProperty("IS_NULLABLE")
    private String nullable;
    @DbProperty("DATA_TYPE")
    private String type;
    @DbProperty("CHARACTER_MAXIMUM_LENGTH")
    private Long maxLength;
    @DbProperty("NUMERIC_PRECISION")
    private Long numericPrecision;
    @DbProperty("NUMERIC_SCALE")
    private Long numericScale;
    @DbProperty("DATETIME_PRECISION")
    private Long datetimePrecision;
    @DbProperty("CHARACTER_SET_NAME")
    private String character;
    @DbProperty("COLLATION_NAME")
    private String collation;
    @DbProperty("COLUMN_TYPE")
    private String columnType;
    @DbProperty("COLUMN_KEY")
    private String columnKey;
    @DbProperty("EXTRA")
    private String extra;
    @DbProperty("COLUMN_COMMENT")
    private String comment;
    @DbProperty("GENERATION_EXPRESSION")
    private String generationExpression;

    private Table table;

    public String getName(){
        if(Consts.MY_SQL_KEYWORDS.contains(name)){
            return String.format("`%s`", name);
        }else{
            return name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Column)) {
            return false;
        }
        Column column = (Column) o;

        return Objects.equals(getTableName(), column.getTableName()) &&
                Objects.equals(getName(), column.getName()) &&
                ((ObjectUtils.isEmpty(getDefaultValue()) && ObjectUtils.isEmpty(column.getDefaultValue()))
                        || Objects.equals(getDefaultValue(), column.getDefaultValue())) &&
                Objects.equals(getNullable(), column.getNullable()) &&
                Objects.equals(getType(), column.getType()) &&
                Objects.equals(getMaxLength(), column.getMaxLength()) &&
                Objects.equals(getNumericPrecision(), column.getNumericPrecision()) &&
                Objects.equals(getNumericScale(), column.getNumericScale()) &&
                Objects.equals(getDatetimePrecision(), column.getDatetimePrecision()) &&
                Objects.equals(getExtra(), column.getExtra()) &&
                Objects.equals(getComment(), column.getComment()) &&
                Objects.equals(getGenerationExpression(), column.getGenerationExpression());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTableName(), getName(), getDefaultValue(), getNullable(), getType(), getMaxLength(), getNumericPrecision(), getNumericScale(), getDatetimePrecision(), getCharacter(), getCollation(), getColumnType(), getExtra(), getComment(), getGenerationExpression());
    }
}
