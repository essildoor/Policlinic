package com.exigen.entity;

public class Doctor extends Person{

    private int room;
    private DoctorSpecialization specialization;

    public Doctor(String name, String surname, int room, DoctorSpecialization specialization) {
        super(name, surname);
        this.room = room;
        this.specialization = specialization;
    }

    public int getRoom() {
        return room;
    }

    public DoctorSpecialization getSpecialization() {
        return specialization;
    }
}
