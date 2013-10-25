package com.exigen.entity;

import java.io.Serializable;

public class Person implements Comparable, Serializable {

    private String name;
    private String surname;

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
