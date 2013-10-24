package com.exigen.entity;

import java.util.Date;

public class RegistrationRecord {

    private Doctor doctor;
    private Patient patient;
    private Date date;

    public RegistrationRecord(Doctor doctor, Patient patient, Date date) {
        this.doctor = doctor;
        this.patient = patient;
        this.date = date;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public Date getDate() {
        return date;
    }
}
