# JDBC ORM 作业

本项目使用 Java 21、Maven、JDBC 和 SQLite，演示了一个简化的 ORM 工具类。

## 运行

```bash
mvn clean compile exec:java
```

运行后会自动创建 `data/homework.db`，并依次演示学院表和学生表的新增、更新、查询、ResultSet 转对象列表和删除。

## 核心方法

`com.example.hw1.orm.JDBCTool` 包含：

- `resultSetToList(ResultSet rs, Class<T> clazz)`
- `save(T obj, Connection connection)`
- `update(T obj, Connection connection)`
- `delete(T obj, Connection connection)`
- `getOneById(String id, Class<T> clazz, Connection connection)`

实体类通过 `@Table`、`@Column` 和 `@Id` 注解完成表名、字段名和主键映射，工具类使用 Java 反射创建对象并赋值。
