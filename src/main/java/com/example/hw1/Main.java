package com.example.hw1;

import com.example.hw1.model.College;
import com.example.hw1.model.Student;
import com.example.hw1.orm.JDBCTool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("data"));
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:data/homework.db")) {
            createTables(connection);
            clearDemoData(connection);

            College college = new College("C001", "计算机学院", "CS");
            System.out.println("学院新增: " + JDBCTool.save(college, connection) + " 条");
            college.setName("计算机与人工智能学院");
            System.out.println("学院更新: " + JDBCTool.update(college, connection) + " 条");
            System.out.println("学院查询: " + JDBCTool.getOneById("C001", College.class, connection));

            Student student = new Student("S001", "张三", "软件工程", 20,
                    LocalDate.of(2025, 9, 1), false, 5800.00);
            System.out.println("学生新增: " + JDBCTool.save(student, connection) + " 条");
            student.setAge(21);
            student.setGraduated(true);
            System.out.println("学生更新: " + JDBCTool.update(student, connection) + " 条");
            System.out.println("学生查询: " + JDBCTool.getOneById("S001", Student.class, connection));

            try (Statement statement = connection.createStatement();
                 var rs = statement.executeQuery("SELECT id, name, major, age, enrollment_date, graduated, tuition FROM student")) {
                List<Student> students = JDBCTool.resultSetToList(rs, Student.class);
                System.out.println("ResultSet 转对象列表: " + students);
            }

            System.out.println("学生删除: " + JDBCTool.delete(student, connection) + " 条");
            System.out.println("学院删除: " + JDBCTool.delete(college, connection) + " 条");
        }
    }

    private static void createTables(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS student (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        major TEXT NOT NULL,
                        age INTEGER NOT NULL,
                        enrollment_date TEXT NOT NULL,
                        graduated INTEGER NOT NULL,
                        tuition REAL NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS college (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        code TEXT NOT NULL UNIQUE
                    )
                    """);
        }
    }

    private static void clearDemoData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM student");
            statement.executeUpdate("DELETE FROM college");
        }
    }
}
