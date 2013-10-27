package com.exigen.entity;

public class Doctor extends Person{

    private int room;
    private String specialization;
    private int recordsCount;

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

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }
}
