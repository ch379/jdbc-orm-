package com.example.hw1.model;

import com.example.hw1.orm.Column;
import com.example.hw1.orm.Id;
import com.example.hw1.orm.Table;

import java.time.LocalDate;

@Table("student")
public class Student {
    @Id
    @Column("id")
    private String id;
    @Column("name")
    private String name;
    @Column("major")
    private String major;
    @Column("age")
    private int age;
    @Column("enrollment_date")
    private LocalDate enrollmentDate;
    @Column("graduated")
    private boolean graduated;
    @Column("tuition")
    private double tuition;

    public Student() {
    }

    public Student(String id, String name, String major, int age, LocalDate enrollmentDate,
                   boolean graduated, double tuition) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollmentDate = enrollmentDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMajor() { return major; }
    public int getAge() { return age; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public boolean isGraduated() { return graduated; }
    public double getTuition() { return tuition; }

    public void setName(String name) { this.name = name; }
    public void setMajor(String major) { this.major = major; }
    public void setAge(int age) { this.age = age; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public void setGraduated(boolean graduated) { this.graduated = graduated; }
    public void setTuition(double tuition) { this.tuition = tuition; }

    @Override
    public String toString() {
        return "Student{id='%s', name='%s', major='%s', age=%d, enrollmentDate=%s, graduated=%s, tuition=%.2f}"
                .formatted(id, name, major, age, enrollmentDate, graduated, tuition);
    }
}
