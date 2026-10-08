package com.example.hw1.model;

import com.example.hw1.orm.Column;
import com.example.hw1.orm.Id;
import com.example.hw1.orm.Table;

@Table("college")
public class College {
    @Id
    @Column("id")
    private String id;
    @Column("name")
    private String name;
    @Column("code")
    private String code;

    public College() {
    }

    public College(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String toString() {
        return "College{id='%s', name='%s', code='%s'}".formatted(id, name, code);
    }
}
