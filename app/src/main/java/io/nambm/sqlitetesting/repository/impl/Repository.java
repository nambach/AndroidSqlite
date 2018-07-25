package io.nambm.sqlitetesting.repository.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.nambm.sqlitetesting.repository.annotation.SqliteNotNull;
import io.nambm.sqlitetesting.repository.component.DataHelper;
import io.nambm.sqlitetesting.repository.IRepository;
import io.nambm.sqlitetesting.repository.component.Schema;
import io.nambm.sqlitetesting.repository.SqliteGenericObject;

public class Repository<T extends SqliteGenericObject> implements IRepository<T> {

    private SQLiteOpenHelper helper;
    private SQLiteDatabase database;
    private Schema schema;
    private Class<T> objectClazz;

    public Repository(Context context) {
        setup();
        helper = new DataHelper(context, schema);
        database = helper.getWritableDatabase();
    }

    private void setup() {
        objectClazz = ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]);
        schema = Schema.generateSchema(objectClazz);
    }

    public void closeRepo() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (helper != null) {
            helper.close();
        }
    }

    private ContentValues convertObject(T object, String... properties) throws NoSuchFieldException, IllegalAccessException {
        ContentValues values = new ContentValues();

        List<Field> fields;

        if (properties.length == 0) {
            fields = new ArrayList<>(Arrays.asList(object.getClass().getDeclaredFields()));
            List<Field> toRemove = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(SqliteNotNull.class)) {
                    toRemove.add(field);
                }
            }
            fields.removeAll(toRemove);
        } else {
            fields = new ArrayList<>();
            for (String property : properties) {
                fields.add(object.getClass().getDeclaredField(property));
            }
        }

        for (Field field : fields) {
            String property = field.getName();
            field.setAccessible(true);
            switch (field.getType().getSimpleName()) {
                case "String":
                    values.put(property, (String) field.get(object));
                    break;
                case "int":
                    values.put(property, field.getInt(object));
                    break;
                case "double":
                    values.put(property, field.getDouble(object));
                    break;
                case "float":
                    values.put(property, field.getFloat(object));
                    break;
                case "long":
                    values.put(property, field.getLong(object));
                    break;
                case "boolean":
                    values.put(property, field.getBoolean(object));
                    break;
            }
        }

        return values;
    }

    public boolean insert(T object) {
        if (queryById(object) != null) {
            return false;
        }

        ContentValues values;
        try {
            values = convertObject(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            values = new ContentValues();
        }

        return database.insert(schema.getTableName(), null, values) != -1;
    }

    public boolean update(T object, String... properties) {
        if (queryById(object) == null) {
            return false;
        }

        ContentValues values;
        try {
            values = convertObject(object, properties);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            values = new ContentValues();
        }
        values.put(schema.getColumnId(), object.getId());

        String whereClause = schema.getColumnId() + " = ?";
        String[] whereArgs = {object.getId()};

        int rs = database.update(schema.getTableName(),
                values,
                whereClause,
                whereArgs);

        return true;
    }

    public boolean delete(T object) {
        String whereClause = schema.getColumnId() + " = ?";
        String[] whereArgs = {object.getId()};

        database.delete(schema.getTableName(),
                whereClause,
                whereArgs);

        return true;
    }

    public T queryById(T object) {
        String whereClause = schema.getColumnId() + " = ?";
        String[] whereArgs = {object.getId()};

        Cursor cursor = database.query(schema.getTableName(),
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        T result = null;

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            result = convertFromCursor(cursor);
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return result;
    }

    public List<T> query(T object, String... properties) {
        StringBuilder whereClause = new StringBuilder();
        String[] whereArgs = new String[properties.length];

        for (int i = 0; i < properties.length; i++) {
            whereClause.append(properties[i]).append(" = ?,");
            try {
                Field field = object.getClass().getField(properties[i]);
                field.setAccessible(true);
                whereArgs[i] = field.get(object) + "";
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }

        int lastComma = whereClause.lastIndexOf(",");
        whereClause.replace(lastComma, lastComma + 1, "");

        Cursor cursor = database.query(schema.getTableName(),
                null,
                whereClause.toString(),
                whereArgs,
                null,
                null,
                schema.getColumnId() + " ASC");

        List<T> list = new ArrayList<>();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                list.add(convertFromCursor(cursor));
                if (cursor.isLast()) {
                    break;
                }

            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public List<T> queryAll() {
        Cursor cursor = database.query(schema.getTableName(),
                null,
                null,
                null,
                null,
                null,
                null);

        List<T> list = new ArrayList<>();
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                list.add(convertFromCursor(cursor));
                if (cursor.isLast()) {
                    break;
                }

            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    private T convertFromCursor(Cursor cursor) {
        T instance;
        try {
            instance = objectClazz.newInstance();

            for (Field field : objectClazz.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName();
                int columnIndex = cursor.getColumnIndex(columnName);
                if (columnIndex < 0) {
                    continue;
                }
                switch (field.getType().getSimpleName()) {
                    case "String":
                        field.set(instance, cursor.getString(columnIndex));
                        break;
                    case "int":
                        field.set(instance, cursor.getInt(columnIndex));
                        break;
                    case "double":
                        field.set(instance, cursor.getDouble(columnIndex));
                        break;
                    case "float":
                        field.set(instance, cursor.getFloat(columnIndex));
                        break;
                    case "long":
                        field.set(instance, cursor.getLong(columnIndex));
                        break;
                    case "boolean":
                        boolean value = cursor.getInt(columnIndex) == 1;
                        field.set(instance, value);
                        break;
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }
}
