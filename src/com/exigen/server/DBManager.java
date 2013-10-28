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
            rs = stmt.executeQuery("SELECT * FROM " + patientsTableName + " ORDER BY surname");
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
     * @param param doctor specialization or null
     * @return Doctor list
     */
    public synchronized ArrayList<Doctor> getDoctorsList(Object param) throws SQLException {
        //appends to query data to select only doctors with specified specialization
        String appendix = "";
        if (param != null && param instanceof String)
            appendix = " WHERE specialization=" + param;
        ArrayList<Doctor> doctorsList = new ArrayList<Doctor>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM " + doctorsTableName + appendix +
                    " ORDER BY specialization");
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
        ArrayList<Patient> patients = new ArrayList<Patient>();
        ArrayList<Doctor> doctors = new ArrayList<Doctor>();
        ArrayList<Date> dates = new ArrayList<Date>();
        ResultSet rsPatients = null;
        ResultSet rsDoctors = null;
        ResultSet rsDates = null;
        ResultSet rsRecordsIds;
        try {
            Record record;
            Patient patient;
            Doctor doctor;
            rsRecordsIds = stmt.executeQuery("SELECT record_id FROM " + recordsTableName +
                    " ORDER BY doctor_id");
            rsPatients =
                    stmt.executeQuery("SELECT * FROM " + patientsTableName + " WHERE patient_id=" +
                            "(SELECT patient_id FROM " + recordsTableName + ")");
            rsDoctors = stmt.executeQuery("SELECT * FROM " + doctorsTableName + " WHERE doctor_id=" +
                    "(SELECT doctor_id FROM " + recordsTableName + ")");
            rsDates = stmt.executeQuery("SELECT date FROM " + recordsTableName);
            while (rsPatients.next()) {
                patient = new Patient(rsPatients.getString(2), rsPatients.getString(3),
                        rsPatients.getString(4), rsPatients.getString(5));
                patient.setId(rsPatients.getInt(1));
                patients.add(patient);
            }
            while (rsDoctors.next()) {
                doctor = new Doctor(rsDoctors.getString(2), rsDoctors.getString(3),
                        rsDoctors.getInt(4), rsDoctors.getString(5));
                doctor.setRecordsCount(rsDoctors.getInt(6));
                doctors.add(doctor);
            }
            while (rsDates.next()) {
                dates.add(rsDates.getDate(1));
            }
            int i = 0;
            while (rsRecordsIds.next()) {
                record = new Record(doctors.get(i), patients.get(i), dates.get(i));
                record.setId(rsRecordsIds.getInt(1));
                recordsList.add(record);
            }
        } finally {
            if (rsPatients != null && rsDoctors != null && rsDates != null) {
                rsDates.close();
                rsDoctors.close();
                rsPatients.close();
            }
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
        //checks if specified doctor already has 5 records on specified date
        String query = "SELECT COUNT(doctor_id) AS rec_count_on_date FROM " + recordsTableName +
                "WHERE doctor_id=" + doctor_id + " AND date=" + date;
        ResultSet rs = stmt.executeQuery(query);
        int rCountOnDate = rs.getInt(1);
        if (rCountOnDate == 5)
            return false;
        query = "SELECT records_count FROM " + doctorsTableName +
                "WHERE doctor_id=" + doctor_id;
        int rCount = rs.getInt(1);
        rs = stmt.executeQuery(query);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //creates record entry
        String updt = "INSERT INTO " + recordsTableName + " (doctor_id, patient_id, date) " +
                "VALUES (" + doctor_id + ", " + patient_id + ", " + format.format(date) + ")";
        //updates recordsCount in doctors entry
        String updateRCount = "UPDATE " + doctorsTableName + " SET records_count=" + ++rCount;
        stmt.executeUpdate(updt);
        stmt.executeUpdate(updateRCount);
        rs.close();
        stmt.close();
        return true;
    }

    protected synchronized ArrayList<String> getDoctorsSpecializationList() throws SQLException {
        ArrayList<String> result = new ArrayList<String>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        String query = "SELECT DISTINCT specialization FROM " + doctorsTableName;
        try {
            rs = stmt.executeQuery(query);
            while (rs.next())
                result.add(rs.getString(1));
        } finally {
            if (rs != null)
                rs.close();
            stmt.close();
        }
        return result;
    }

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
        stmt.close();
    }

    private void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE " + patientsTableName);
        stmt.executeUpdate("DROP TABLE " + doctorsTableName);
        stmt.executeUpdate("DROP TABLE " + recordsTableName);
        stmt.close();
        System.out.println("tables dropped");
    }

    /*public static void main(String[] args) throws SQLException {
        DBManager d = DBManager.getInstance();
        d.connect();
        d.dropTables();
        d.createTables();
    }*/
}
