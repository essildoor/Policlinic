package com.exigen.entity;

import java.io.Serializable;

public class Person implements Comparable, Serializable {

    private String name;
    private String surname;
    private int id;

    public Person(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Object o) {
        Person p = (Person) o;
        if (this.surname.compareTo(p.getSurname()) < 0)
            return -1;
        if (this.surname.compareTo(p.getSurname()) == 0)
            return 0;
        return 1;
    }
}
