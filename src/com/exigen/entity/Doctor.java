package com.exigen.entity;

public class Doctor extends Person{

    private int room;
    private String specialization;

    public Doctor(String name, String surname, int room, String specialization) {
        super(name, surname);
        this.room = room;
        this.specialization = specialization;
    }

    public int getRoom() {
        return room;
    }

    public String getSpecialization() {
        return specialization;
    }
}
