package com.exigen.server;

import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.RegistrationRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {

    private Connection conn;
    private String driver;
    private String dbName;
    private Logger logger;
    private String patientsTableName;
    private String doctorsTableName;
    private String recordsTableName;

    public DBManager() {
        logger = ServerLogger.getInstance().getLogger();
        driver = "org.apache.derby.jdbc.EmbeddedDriver";
        dbName = "PolyclinicDataBase";
        patientsTableName = "PATIENTS_TABLE";
        doctorsTableName = "DOCTORS_TABLE";
        recordsTableName = "RECORDS_TABLE";
    }

    public void connect() {
        String connectionURL = "jdbc:derby:" + dbName;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(connectionURL);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "JDBC driver class not found!");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void getPatientsList() {
        ArrayList<Patient> patientsList = new ArrayList<Patient>();
        try {
            Statement stmt2 = conn.createStatement();
            ResultSet rs = stmt2.executeQuery("SELECT * FROM " + patientsTableName);
            System.out.println("Patients table\n\n");
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " " + rs.getString(2) + " " + rs.getString(3)
                        + " " + rs.getString(4) + " " + rs.getString(5));
            }
            rs.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void add(Patient patient) {
        try {
            Statement stmt = conn.createStatement();
            String update =
                    "INSERT INTO " + patientsTableName + " (COL2, COL3, COL4, COL5) " + "VALUES ('" +
                            patient.getSurname() + "', '" +
                            patient.getName() + "', '" +
                            patient.getDistrict() + "', '" +
                            patient.getDiagnosis() +
                            "')";
            stmt.executeUpdate(update);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void add(Doctor doctor) {

    }

    public void add(RegistrationRecord record) {

    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DBManager dbManager = new DBManager();
        dbManager.connect();
        dbManager.getPatientsList();
        dbManager.conn.close();
    }
}
