package com.exigen.server;

public class InteractionProtocol {

    private final int OK = 0;
    private final int ERROR = -1;
    private final int REQUEST_PATIENTS_TABLE = 1;
    private final int REQUEST_DOCTORS_TABLE = 2;
    private final int REQUEST_RECORDS_TABLE = 3;
    private final int REQUEST_ALL_TABLES = 10;

    public void getRequest(int request) {
        switch (request) {
            case 1:

        }
    }


}
