package com.example.hw1.orm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JDBCTool {
    private JDBCTool() {
    }

    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        var fields = mappedFields(clazz);
        while (rs.next()) {
            T object = newInstance(clazz);
            for (Field field : fields) {
                String columnName = columnName(field);
                Object value = rs.getObject(columnName);
                try {
                    field.setAccessible(true);
                    field.set(object, convertValue(value, field.getType()));
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException("Cannot write field: " + field.getName(), exception);
                }
            }
            result.add(object);
        }
        return result;
    }

    public static <T> int save(T obj, Connection connection) throws SQLException {
        Class<?> clazz = obj.getClass();
        List<Field> fields = mappedFields(clazz);
        String columns = fields.stream().map(JDBCTool::columnName).collect(Collectors.joining(", "));
        String placeholders = String.join(", ", java.util.Collections.nCopies(fields.size(), "?"));
        String sql = "INSERT INTO " + tableName(clazz) + " (" + columns + ") VALUES (" + placeholders + ")";
        return executeUpdate(sql, fields.stream()
                .map(field -> toJdbcValue(fieldValue(obj, field)))
                .toList(), connection);
    }

    public static <T> int update(T obj, Connection connection) throws SQLException {
        Class<?> clazz = obj.getClass();
        Field idField = idField(clazz);
        List<Field> fields = mappedFields(clazz).stream().filter(field -> field != idField).toList();
        String assignments = fields.stream()
                .map(field -> columnName(field) + " = ?")
                .collect(Collectors.joining(", "));
        String sql = "UPDATE " + tableName(clazz) + " SET " + assignments
                + " WHERE " + columnName(idField) + " = ?";
        List<Object> values = new ArrayList<>(fields.stream()
                .map(field -> toJdbcValue(fieldValue(obj, field)))
                .toList());
        values.add(toJdbcValue(fieldValue(obj, idField)));
        return executeUpdate(sql, values, connection);
    }

    public static <T> int delete(T obj, Connection connection) throws SQLException {
        Class<?> clazz = obj.getClass();
        Field idField = idField(clazz);
        String sql = "DELETE FROM " + tableName(clazz) + " WHERE " + columnName(idField) + " = ?";
        return executeUpdate(sql, List.of(toJdbcValue(fieldValue(obj, idField))), connection);
    }

    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws SQLException {
        Field idField = idField(clazz);
        String sql = "SELECT " + mappedFields(clazz).stream().map(JDBCTool::columnName).collect(Collectors.joining(", "))
                + " FROM " + tableName(clazz)
                + " WHERE " + columnName(idField) + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                List<T> results = resultSetToList(rs, clazz);
                return results.isEmpty() ? null : results.get(0);
            }
        }
    }

    private static int executeUpdate(String sql, List<Object> values, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i + 1, values.get(i));
            }
            return statement.executeUpdate();
        }
    }

    private static List<Field> mappedFields(Class<?> clazz) {
        return List.of(clazz.getDeclaredFields()).stream()
                .filter(field -> field.isAnnotationPresent(Column.class))
                .toList();
    }

    private static String tableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Class must have @Table: " + clazz.getName());
        }
        return table.value();
    }

    private static String columnName(Field field) {
        return field.getAnnotation(Column.class).value();
    }

    private static Field idField(Class<?> clazz) {
        return mappedFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class must have an @Id field: " + clazz.getName()));
    }

    private static Object fieldValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read field: " + field.getName(), exception);
        }
    }

    private static Object toJdbcValue(Object value) {
        if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return value.toString();
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }
        return value;
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Class needs a no-argument constructor: " + clazz.getName(), exception);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value instanceof Number number ? number.intValue() != 0 : Boolean.parseBoolean(value.toString());
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value.toString());
        }
        if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value.toString());
        }
        if (targetType == Timestamp.class) {
            return Timestamp.valueOf(value.toString());
        }
        throw new IllegalArgumentException("Unsupported conversion: " + value.getClass() + " -> " + targetType);
    }
}
