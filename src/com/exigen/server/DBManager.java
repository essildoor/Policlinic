package com.exigen.server;

import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {
    private static DBManager instance;
    private Connection conn;
    private String driver;
    private String dbName;
    private Logger logger;
    private String patientsTableName;
    private String doctorsTableName;
    private String recordsTableName;

    public static synchronized DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    private DBManager() {
        logger = ServerLogger.getInstance().getLogger();
        driver = "org.apache.derby.jdbc.EmbeddedDriver";
        dbName = "PolyclinicDataBase";
        patientsTableName = "PATIENTS_TABLE";
        doctorsTableName = "DOCTORS_TABLE";
        recordsTableName = "RECORDS_TABLE";
        connect();
    }

    /**
     * Establish connection to DB with a specified name (dbName)
     */
    private void connect() {
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

    /**
     * selects from DB whole patients list
     *
     * @return Patient list
     */
    public synchronized ArrayList<Patient> getPatientsList() throws SQLException {
        ArrayList<Patient> patientsList = new ArrayList<Patient>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM " + patientsTableName);
            while (rs.next()) {
                Patient patient = new Patient(rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5));
                patient.setId(rs.getInt(1));
                patientsList.add(patient);
            }
            rs.close();
        } finally {
            if (rs != null)
                rs.close();
            stmt.close();
        }
        return patientsList;
    }

    /**
     * selects from DB whole doctors list
     *
     * @return Doctor list
     */
    public synchronized ArrayList<Doctor> getDoctorsList() throws SQLException {
        ArrayList<Doctor> doctorsList = new ArrayList<Doctor>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM " + doctorsTableName);
            while (rs.next()) {
                Doctor doctor = new Doctor(rs.getString(2), rs.getString(3),
                        Integer.parseInt(rs.getString(4)), rs.getString(5));
                doctor.setId(rs.getInt(1));
                doctor.setRecordsCount(rs.getInt(6));
                doctorsList.add(doctor);
            }

        } finally {
            if (rs != null)
                rs.close();
            stmt.close();
        }
        return doctorsList;
    }

    /**
     * selects from DB whole records list
     *
     * @return Record list
     */
    public synchronized ArrayList<Record> getRecordsList() throws SQLException {
        ArrayList<Record> recordsList = new ArrayList<Record>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("");
        } finally {
            stmt.close();
        }
        return recordsList;
    }



    /**
     * deletes patient with a specified id from patients table
     *
     * @param patient patient specified in client
     * @return true if success
     * @throws SQLException
     */
    public synchronized boolean delete(Patient patient) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "DELETE FROM " + patientsTableName + " WHERE patient_id=" + patient.getId();
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * deletes doctor with a specified id from doctors table
     *
     * @param doctor doctor specified in client
     * @return true if success
     * @throws SQLException
     */
    public synchronized boolean delete(Doctor doctor) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "DELETE FROM " + doctorsTableName + " WHERE doctor_id=" + doctor.getId();
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * adds new patient to patients DB
     *
     * @param patient Patient instance
     * @return true if success
     */
    public synchronized boolean add(Patient patient) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "INSERT INTO " + patientsTableName + " (" +
                            "patient_surname, patient_name, district, diagnosis) " + "VALUES ('" +
                            patient.getSurname() + "', '" +
                            patient.getName() + "', '" +
                            patient.getDistrict() + "', '" +
                            patient.getDiagnosis() +
                            "')";
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * adds new doctor to doctors table in DB
     *
     * @param doctor Doctor instance
     * @return true if success
     */
    public synchronized boolean add(Doctor doctor) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "INSERT INTO " + doctorsTableName + " (" +
                            "doctor_surname, doctor_name, room, specialization, records_count) " +
                            "VALUES ('" +
                            doctor.getSurname() + "', '" +
                            doctor.getName() + "', " +
                            doctor.getRoom() + ", '" +
                            doctor.getSpecialization() +
                            doctor.getRecordsCount() +
                            "')";
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    public synchronized boolean add(Record record) throws SQLException {
        int patient_id = record.getPatient().getId();
        int doctor_id = record.getDoctor().getId();
        java.util.Date date = record.getDate();
        Statement stmt = conn.createStatement();
        String query = "SELECT records_count FROM " + doctorsTableName +
                "WHERE doctor_id=" + doctor_id;
        ResultSet rs = stmt.executeQuery(query);
        if (rs.getInt(1) < 5) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String updt = "INSERT INTO " + recordsTableName + " (doctor_id, patient_id, date) " +
                    "VALUES (" + doctor_id + ", " + patient_id + ", " + format.format(date) + ")";
            stmt.executeUpdate(updt);
        } else {
            return false;
        }
        return true;
    }

    /**
     * test
     *
     * @param args
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    /*public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DBManager dbManager = new DBManager();
        dbManager.connect();
        dbManager.conn.close();
    }*/

    /**
     * executes statement which creates schema and tables with specified names (see constructor)
     *
     * @throws SQLException
     */
    private void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        String patientTable = "CREATE TABLE " + patientsTableName + " (" +
                "patient_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "patient_surname VARCHAR(20) NOT NULL," +
                "patient_name VARCHAR(20) NOT NULL," +
                "district VARCHAR(50) NOT NULL," +
                "diagnosis VARCHAR(255) NOT NULL," +
                "PRIMARY KEY (patient_id))";
        String doctorTable = "CREATE TABLE " + doctorsTableName + " (" +
                "doctor_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_surname VARCHAR(20) NOT NULL," +
                "doctor_name VARCHAR(20) NOT NULL," +
                "room INT NOT NULL," +
                "specialization VARCHAR(50) NOT NULL," +
                "records_count INT NOT NULL," +
                "PRIMARY KEY (doctor_id))";

        String recordTable = "CREATE TABLE " + recordsTableName + " (" +
                "record_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_id INT NOT NULL," +
                "patient_id INT NOT NULL," +
                "date DATE," +
                "PRIMARY KEY (record_id))";
        stmt.executeUpdate(patientTable);
        stmt.executeUpdate(doctorTable);
        stmt.executeUpdate(recordTable);
    }

    private void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE " + patientsTableName);
        stmt.executeUpdate("DROP TABLE " + doctorsTableName);
        stmt.executeUpdate("DROP TABLE " + recordsTableName);
        System.out.println("tables dropped");
    }

    /*public static void main(String[] args) throws SQLException {
        DBManager d = DBManager.getInstance();
        d.connect();
        d.dropTables();
        d.createTables();
    }*/
}
