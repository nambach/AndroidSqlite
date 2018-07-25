package io.nambm.sqlitetesting.repository.component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import io.nambm.sqlitetesting.repository.SqliteGenericObject;
import io.nambm.sqlitetesting.repository.annotation.SqliteNotNull;
import io.nambm.sqlitetesting.repository.annotation.SqlitePrimaryKey;
import io.nambm.sqlitetesting.repository.annotation.SqliteTableName;
import io.nambm.sqlitetesting.repository.annotation.SqliteUnique;

public class Schema {

    private String tableName;
    private Map<String, String> columns;
    private String columnId;

    private Schema() {
        columns = new TreeMap<>();
    }

    private static Schema create() {
        return new Schema();
    }

    private Schema tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    private Schema column(String columnName, String columnType, boolean notNull, boolean primaryKey, boolean autoIncrement, boolean unique) {
        if (notNull) {
            columnType += " NOT NULL";
        } else {
            return this;
        }
        if (primaryKey) {
            columnType += " PRIMARY KEY";
            this.columnId = columnName;
        }
        if (primaryKey && autoIncrement) {
            columnType += " AUTOINCREMENT";
        }
        if (unique) {
            columnType += " UNIQUE";
        }

        this.columns.put(columnName, columnType);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnId() {
        return columnId;
    }

    public String getCreateTableCommand() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(tableName).append("(");
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            builder.append(entry.getKey())
                    .append(" ")
                    .append(entry.getValue())
                    .append(",");
        }

        int lastComma = builder.lastIndexOf(",");
        builder.replace(lastComma, lastComma + 1, ");");

        return builder.toString();
    }

    public String getDropTableCommand() {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public static <T extends SqliteGenericObject> Schema generateSchema(Class<T> clazz) {
        String tableName = clazz.getAnnotation(SqliteTableName.class).value();

        Schema schema = Schema.create().tableName(tableName);

        for (Field field : clazz.getDeclaredFields()) {
            boolean primaryKey = false, notNull = false, autoIncrement = false, unique = false;
            String columnName, columnType = DataType.TEXT;
            columnName = field.getName();

            switch (field.getType().getSimpleName()) {
                case "String":
                    columnType = DataType.TEXT;
                    break;
                case "int":
                    columnType = DataType.INTEGER;
                    break;
                case "double":
                    columnType = DataType.REAL;
                    break;
                case "float":
                    columnType = DataType.REAL;
                    break;
                case "long":
                    columnType = DataType.INTEGER;
                    break;
                case "boolean":
                    columnType = DataType.NUMERIC;
                    break;
            }

            if (field.isAnnotationPresent(SqliteNotNull.class)) {
                notNull = true;
            }

            if (field.isAnnotationPresent(SqlitePrimaryKey.class)) {
                primaryKey = true;
                autoIncrement = field.getAnnotation(SqlitePrimaryKey.class).autoIncrement();
            }

            if (field.isAnnotationPresent(SqliteUnique.class)) {
                unique = true;
            }

            schema.column(columnName, columnType, notNull, primaryKey, autoIncrement, unique);
        }

        return schema;
    }
}
