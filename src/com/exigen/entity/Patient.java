package com.exigen.entity;

public class Patient extends Person {

    private int district;
    private String diagnosis;

    public Patient(String name, String surname, int district, String diagnosis) {
        super(name, surname);
        this.district = district;
        this.diagnosis = diagnosis;
    }

    public int getDistrict() {
        return district;
    }

    public String getDiagnosis() {
        return diagnosis;
    }
}
