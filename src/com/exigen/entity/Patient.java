package com.exigen.entity;

public class Patient extends Person {

    private String district;
    private String diagnosis;
    private int insuranceId;

    public Patient(String name, String surname, String district, String diagnosis, int insuranceId) {
        super(name, surname);
        this.district = district;
        this.diagnosis = diagnosis;
        this.insuranceId = insuranceId;
    }

    public String getDistrict() {
        return district;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public int getInsuranceId() {
        return insuranceId;
    }

    @Override
    public boolean equals(Object obj) {
        Patient p = (Patient) obj;
        if (this.insuranceId == p.getInsuranceId())
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return insuranceId;
    }
}
