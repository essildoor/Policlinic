package com.exigen.entity;

import java.io.Serializable;
import java.util.Date;

public class Record implements Serializable{

    private Doctor doctor;
    private Patient patient;
    private Date date;
    private int id;

    public Record(Doctor doctor, Patient patient, Date date) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
