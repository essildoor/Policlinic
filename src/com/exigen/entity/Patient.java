package com.exigen.entity;

public class Patient extends Person {

    private String district;
    private String diagnosis;

    public Patient(String name, String surname, String district, String diagnosis) {
        super(name, surname);
        this.district = district;
        this.diagnosis = diagnosis;
    }

    public String getDistrict() {
        return district;
    }

    public String getDiagnosis() {
        return diagnosis;
    }
}
