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
            rs = stmt.executeQuery("SELECT * FROM " + patientsTableName + " ORDER BY patient_surname");
            while (rs.next()) {
                Patient patient = new Patient(rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getInt(6));
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
    public synchronized ArrayList<Doctor> getDoctorsList(String param) throws SQLException {
        //appends to query data to select only doctors with specified specialization
        System.out.println("getDoctorsList: entering. param=" + param);
        String appendix = "";
        if (param != null)
            appendix = " WHERE specialization='" + param + "'";
        System.out.println("getDoctorsList: appendix=" + appendix);
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
            rsPatients =
                    stmt.executeQuery("SELECT * FROM " + patientsTableName + " WHERE patient_id=" +
                            "(SELECT patient_id FROM " + recordsTableName + ")");
            while (rsPatients.next()) {
                patient = new Patient(rsPatients.getString(2), rsPatients.getString(3),
                        rsPatients.getString(4), rsPatients.getString(5), rsPatients.getInt(6));
                patient.setId(rsPatients.getInt(1));
                patients.add(patient);
            }
            rsDoctors = stmt.executeQuery("SELECT * FROM " + doctorsTableName + " WHERE doctor_id=" +
                    "(SELECT doctor_id FROM " + recordsTableName + ")");
            while (rsDoctors.next()) {
                doctor = new Doctor(rsDoctors.getString(2), rsDoctors.getString(3),
                        rsDoctors.getInt(4), rsDoctors.getString(5));
                doctor.setRecordsCount(rsDoctors.getInt(6));
                doctors.add(doctor);
            }
            rsDates = stmt.executeQuery("SELECT rec_date FROM " + recordsTableName);
            while (rsDates.next()) {
                dates.add(rsDates.getDate(1));
            }
            int i = 0;
            rsRecordsIds = stmt.executeQuery("SELECT record_id FROM " + recordsTableName +
                    " ORDER BY doctor_id");
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

    public synchronized ArrayList<Patient> search(Patient patient) throws SQLException {
        if (patient == null)
            return null;
        System.out.println("search patient: entering. parameter=" + patient);
        ArrayList<Patient> result = new ArrayList<Patient>();
        Patient p;
        Statement stmt = conn.createStatement();
        ResultSet rs = null;

        try {
            if (patient.getInsuranceId() != 0) {
                System.out.println("search patient: patient != null && insuranceId != 0");
                String query = "SELECT * FROM " + patientsTableName + " WHERE insurance_id=" +
                        patient.getInsuranceId();
                rs = stmt.executeQuery(query);
                p = new Patient(
                        rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6)
                );
                p.setId(rs.getInt(1));
                result.add(p);
            } else {
                System.out.println("search patient: search by name, or surname or..");
                String queryBase = "SELECT * FROM " + patientsTableName;
                String appendix = "";
                if (patient.getName() != null)
                    appendix = " WHERE patient_name='" + patient.getName() + "'";
                if (patient.getSurname() != null)
                    appendix = " WHERE patient_surname='" + patient.getSurname() + "'";
                if (patient.getDistrict() != null)
                    appendix = " WHERE district='" + patient.getDistrict() + "'";
                if (patient.getDiagnosis() != null)
                    appendix = " WHERE diagnosis='" + patient.getDiagnosis() + "'";
                System.out.println("search patient: query=" + queryBase + appendix);
                rs = stmt.executeQuery(queryBase + appendix);
                while (rs.next()) {
                    p = new Patient(rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getInt(6));
                    p.setId(rs.getInt(1));
                    result.add(p);
                }
            }
        } finally {
            if (rs != null)
                rs.close();
            stmt.close();
        }
        System.out.println("search patient: leaving. result size=" + result.size());
        return result;
    }

    /**
     * adds new patient to patients DB
     *
     * @param patient Patient instance
     * @return true if success
     */
    public synchronized boolean add(Patient patient) throws SQLException {
        Statement stmt = conn.createStatement();
        int insuranceId = patient.getInsuranceId();
        try {
            String query = "SELECT insurance_id FROM " + patientsTableName;
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                if (rs.getInt(1) == insuranceId)
                    return false;
            }
            String update =
                    "INSERT INTO " + patientsTableName + " (" +
                            "patient_name, patient_surname, district, diagnosis, insurance_id) " +
                            "VALUES ('" +
                            patient.getName() + "', '" +
                            patient.getSurname() + "', '" +
                            patient.getDistrict() + "', '" +
                            patient.getDiagnosis() + "', " +
                            patient.getInsuranceId() +
                            ")";
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
                            "doctor_name, doctor_surname, room, specialization, records_count) " +
                            "VALUES ('" +
                            doctor.getName() + "', '" +
                            doctor.getSurname() + "', " +
                            doctor.getRoom() + ", '" +
                            doctor.getSpecialization() + "', " +
                            doctor.getRecordsCount() +
                            ")";
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Statement stmt = conn.createStatement();
        //checks if specified doctor still exist
        String query = "SELECT COUNT(doctor_id) FROM " + doctorsTableName;
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        query = "SELECT COUNT(record_id) FROM " + recordsTableName +
                " WHERE doctor_id=" + doctor_id + " AND rec_date='" + format.format(date) + "'";
        rs = stmt.executeQuery(query);
        if (rs.next() && rs.getInt(1) >= 5)
            return false;
        //checks if specified patient exist
        query = "SELECT COUNT(patient_id) FROM " + patientsTableName;
        rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        String update = "INSERT INTO " + recordsTableName + " (" +
                "doctor_id, patient_id, rec_date) VALUES (" +
                doctor_id + ", " + patient_id + ", '" + format.format(date) + "')";
        stmt.executeUpdate(update);
        rs.close();
        stmt.close();
        /*//checks if specified doctor already has 5 records on specified date
        String query = "SELECT COUNT(doctor_id) AS rec_count_on_date FROM " + recordsTableName +
                " WHERE doctor_id=" + doctor_id + " AND rec_date='" + format.format(date) + "'";
        ResultSet rs = stmt.executeQuery(query);
        int rowCount = 0;
        if (rs.last()) {
            rowCount = rs.getRow();
        }
        if (rowCount != 0) {
            rs.beforeFirst();
            int rCountOnDate = rs.getInt(1);
            if (rCountOnDate == 5)
                return false;
        }
        query = "SELECT records_count FROM " + doctorsTableName +
                "WHERE doctor_id=" + doctor_id;
        int rCount = rs.getInt(1);
        rs = stmt.executeQuery(query);
        //creates record entry
        String updt = "INSERT INTO " + recordsTableName + " (doctor_id, patient_id, rec_date) " +
                "VALUES (" + doctor_id + ", " + patient_id + ", '" + format.format(date) + "')";
        //updates recordsCount in doctors entry
        String updateRCount = "UPDATE " + doctorsTableName + " SET records_count=" + ++rCount;
        stmt.executeUpdate(updt);
        stmt.executeUpdate(updateRCount);
        rs.close();
        stmt.close();*/
        return true;
    }

    public synchronized ArrayList<String> getDoctorsSpecializationList() throws SQLException {
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
                "patient_name VARCHAR(20) NOT NULL," +
                "patient_surname VARCHAR(20) NOT NULL," +
                "district VARCHAR(50) NOT NULL," +
                "diagnosis VARCHAR(255) NOT NULL," +
                "insurance_id INT NOT NULL UNIQUE," +
                "PRIMARY KEY (patient_id))";
        String doctorTable = "CREATE TABLE " + doctorsTableName + " (" +
                "doctor_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_name VARCHAR(20) NOT NULL," +
                "doctor_surname VARCHAR(20) NOT NULL," +
                "room INT NOT NULL," +
                "specialization VARCHAR(50) NOT NULL," +
                "records_count INT NOT NULL," +
                "PRIMARY KEY (doctor_id))";

        String recordTable = "CREATE TABLE " + recordsTableName + " (" +
                "record_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_id INT NOT NULL," +
                "patient_id INT NOT NULL," +
                "rec_date DATE," +
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
