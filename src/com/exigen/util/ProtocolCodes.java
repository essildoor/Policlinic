package com.exigen.util;

public class ProtocolCodes {
    public static final int OK = 0;
    public static final int ERROR = -1;
    public static final int STOP = 100;
    public static final int SERVER_IS_NOT_RESPONDING = -2;
    public static final int CLIENT_IS_NOT_RESPONDING = -3;

    public static final int REQUEST_ALL_LISTS = 10;
    public static final int REQUEST_PATIENTS_LIST = 1;
    public static final int REQUEST_DOCTORS_LIST = 2;
    public static final int REQUEST_RECORDS_LIST = 3;

    public static final int REQUEST_ADD_PATIENT = 11;
    public static final int REQUEST_ADD_DOCTOR = 21;
    public static final int REQUEST_ADD_RECORD = 31;

    public static final int REQUEST_EDIT_PATIENT = 12;
    public static final int REQUEST_EDIT_DOCTOR = 22;

    public static final int REQUEST_DELETE_PATIENT = 13;
    public static final int REQUEST_DELETE_DOCTOR = 23;
    public static final int REQUEST_DELETE_RECORD = 33;

    public static final int REQUEST_SEARCH_PATIENT = 14;
    public static final int REQUEST_SEARCH_DOCTOR = 24;
    public static final int REQUEST_SEARCH_RECORD = 34;

    public static final int REQUEST_DOCTOR_SPECIALIZATION_LIST = 25;

}
