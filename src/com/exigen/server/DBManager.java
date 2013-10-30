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
    protected synchronized ArrayList<Patient> getPatientsList() throws SQLException {
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
     * @return Doctor list
     */
    protected synchronized ArrayList<Doctor> getDoctorsList() throws SQLException {
        ArrayList<Doctor> doctorsList = new ArrayList<Doctor>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM " + doctorsTableName +
                    " ORDER BY specialization");
            while (rs.next()) {
                Doctor doctor = new Doctor(rs.getString(2), rs.getString(3),
                        Integer.parseInt(rs.getString(4)), rs.getString(5));
                doctor.setId(rs.getInt(1));
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
    protected synchronized ArrayList<Record> getRecordsList() throws SQLException {
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
    protected synchronized boolean delete(Patient patient) throws SQLException {
        //todo
        //fix: deletes even if related records exist
        Statement stmt = conn.createStatement();
        //gets current date in sql format for comparing purposes
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date sqlCurrentDate = new Date(utilDate.getTime());
        String query = "SELECT COUNT(patient_id) FROM " + recordsTableName + " WHERE patient_id=" +
                patient.getId() + " AND rec_date>='" + sqlCurrentDate + "'";
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        try {
            String update =
                    "DELETE FROM " + patientsTableName + " WHERE patient_id=" + patient.getId();
            stmt.executeUpdate(update);
        } finally {
            rs.close();
            stmt.close();
        }
        return true;
    }

    /**
     * deletes doctor with a specified id from doctors table if there is no one record related
     * with this doctor exists on current and further dates
     *
     * @param doctor doctor specified in client
     * @return true if success
     * @throws SQLException
     */
    protected synchronized boolean delete(Doctor doctor) throws SQLException {
        //todo
        //fix: deletes even if related records exist
        Statement stmt = conn.createStatement();
        //gets current date in sql format for comparing purposes
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date sqlCurrentDate = new Date(utilDate.getTime());
        String query = "SELECT COUNT(doctor_id) FROM " + recordsTableName + " WHERE doctor_id=" +
                doctor.getId() + " AND rec_date>='" + sqlCurrentDate + "'";
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        try {
            String update =
                    "DELETE FROM " + doctorsTableName + " WHERE doctor_id=" + doctor.getId();
            stmt.executeUpdate(update);
        } finally {
            rs.close();
            stmt.close();
        }
        return true;
    }

    /**
     * deletes specified record from DB
     *
     * @param record record to delete
     * @return true if delete operation was successful
     * @throws SQLException
     */
    protected synchronized boolean delete(Record record) throws SQLException {
        Statement stmt = conn.createStatement();
        String update = "DELETE FROM " + recordsTableName + " WHERE record_id=" + record.getId();
        try {
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * Search for patients in DB according specified search params
     *
     * @param searchParam Patient instance which represents search param: whether all fields together
     *                    (name, surname, district and diagnosis) or only one of them
     * @return ArrayList of Patient obj which meets specified search params
     * @throws SQLException
     */
    protected synchronized ArrayList<Patient> search(Patient searchParam) throws SQLException {
        if (searchParam == null)
            return getPatientsList();
        ArrayList<Patient> result = new ArrayList<Patient>();
        Patient p;
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        String name = searchParam.getName();
        String surname = searchParam.getSurname();
        String district = searchParam.getDistrict();
        String diagnosis = searchParam.getDiagnosis();
        int insuranceId = searchParam.getInsuranceId();
        try {
            if (insuranceId != 0) {
                String query = "SELECT * FROM " + patientsTableName + " WHERE insurance_id=" +
                        insuranceId;
                rs = stmt.executeQuery(query);
                p = new Patient(
                        rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getInt(6)
                );
                p.setId(rs.getInt(1));
                result.add(p);
            } else {
                String queryBase = "SELECT * FROM " + patientsTableName;
                String appendix = "";
                //if all fields (except insuranceId) is specified
                if (name != null && surname != null && district != null && diagnosis != null &&
                        !name.equals("") && !surname.equals("") && !district.equals("") &&
                        !diagnosis.equals("")) {
                    appendix = " WHERE patient_name='" + name + "' AND patient_surname='" +
                            surname + "' AND district='" + district + "' AND diagnosis='" +
                            diagnosis + "'";
                } else {
                    //else searches by only one specified param
                    if (name != null && !name.equals(""))
                        appendix = " WHERE patient_name='" + searchParam.getName() + "'";
                    else if (surname != null && !surname.equals(""))
                        appendix = " WHERE patient_surname='" + searchParam.getSurname() + "'";
                    else if (district != null && !district.equals(""))
                        appendix = " WHERE district='" + searchParam.getDistrict() + "'";
                    else if (diagnosis != null && !diagnosis.equals(""))
                        appendix = " WHERE diagnosis='" + searchParam.getDiagnosis() + "'";
                }
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
        return result;
    }

    /**
     * Search for doctors in DB according specified search params
     *
     * @param searchParam Doctor instance which represents search params: whether all fields
     *                    defined or just only one
     * @return ArrayList of Doctor obj which meets specified search params
     * @throws SQLException
     */
    protected synchronized ArrayList<Doctor> search(Doctor searchParam) throws SQLException {
        if (searchParam == null)
            return getDoctorsList();
        ArrayList<Doctor> result = new ArrayList<Doctor>();
        Doctor d;
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        String name = searchParam.getName();
        String surname = searchParam.getSurname();
        int room = searchParam.getRoom();
        String specialization = searchParam.getSpecialization();
        String query = null;
        try {
            if (name != null && surname != null && specialization != null &&
                    !name.equals("") && !surname.equals("") &&
                    room != 0 && !specialization.equals("")) {
                query = "SELECT * FROM " + doctorsTableName + " WHERE doctor_name='" + name +
                        "' AND doctor_surname='" + surname + "' AND room=" + room +
                        " AND specialization='" + specialization + "'";
            } else {
                if (name != null && !name.equals(""))
                    query = "SELECT * FROM " + doctorsTableName +
                            " WHERE doctor_name='" + name + "'";
                else if (surname != null && !surname.equals(""))
                    query = "SELECT * FROM " + doctorsTableName +
                            " WHERE doctor_surname='" + surname + "'";
                else if (room != 0)
                    query = "SELECT * FROM " + doctorsTableName + " WHERE room=" + room;
                else if (specialization != null && !specialization.equals(""))
                    query = "SELECT * FROM " + doctorsTableName +
                            " WHERE specialization='" + specialization + "'";
            }
            if (query != null) {
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    d = new Doctor(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
                    result.add(d);
                }
            }
        } finally {
            if (rs != null)
                rs.close();
            stmt.close();
        }
        return result;
    }

    /**
     * adds new patient to patients DB
     *
     * @param patient Patient instance
     * @return true if success
     */
    protected synchronized boolean add(Patient patient) throws SQLException {
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
                            patient.getInsuranceId() + ")";
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
    protected synchronized boolean add(Doctor doctor) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "INSERT INTO " + doctorsTableName + " (" +
                            "doctor_name, doctor_surname, room, specialization) " +
                            "VALUES ('" +
                            doctor.getName() + "', '" +
                            doctor.getSurname() + "', " +
                            doctor.getRoom() + ", '" +
                            doctor.getSpecialization() + "')";
            stmt.executeUpdate(update);
        } finally {
            stmt.close();
        }
        return true;
    }

    /**
     * Adds new record to DB
     *
     * @param record record to add
     * @return true if operation was successful, otherwise returns false
     * @throws SQLException
     */
    protected synchronized boolean add(Record record) throws SQLException {
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
        //checks if specified doctor has less than 5 records on specified date already
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
        return true;
    }

    /**
     * @return ArrayList of Strings which are distinct specializations retrieved from
     *         doctors table in DB
     * @throws SQLException
     */
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
