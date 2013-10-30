package com.exigen.server;

import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
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

    protected DBManager() {
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
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
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
    protected ArrayList<Patient> getPatientsList() throws SQLException {
        ArrayList<Patient> patientsList = new ArrayList<Patient>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM patients ORDER BY patient_surname");
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
    protected ArrayList<Doctor> getDoctorsList() throws SQLException {
        ArrayList<Doctor> doctorsList = new ArrayList<Doctor>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM doctors ORDER BY specialization");
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
    protected ArrayList<Record> getRecordsList() throws SQLException {
        ArrayList<Record> recordsList = new ArrayList<Record>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try {
            Record record;
            Patient patient;
            Doctor doctor;
            java.util.Date date;
            String query = "SELECT doctor_name, doctor_surname, room, specialization, " +
                    "patient_name, patient_surname, district, diagnosis, insurance_id , rec_date " +
                    "FROM records, doctors, patients " +
                    "WHERE records.doctor_id=doctors.doctor_id " +
                    "AND records.patient_id=patients.patient_id";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                doctor = new Doctor(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4));
                patient = new Patient(rs.getString(5), rs.getString(6), rs.getString(7),
                        rs.getString(8), rs.getInt(9));
                date = rs.getDate(10);
                record = new Record(doctor, patient, date);
                recordsList.add(record);
            }
        } finally {
            if (rs != null) {
                rs.close();
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
    protected boolean delete(Patient patient) throws SQLException {
        //todo
        //fix: deletes even if related records exist
        Statement stmt = conn.createStatement();
        //gets current date in sql format for comparing purposes
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date sqlCurrentDate = new Date(utilDate.getTime());
        String query = "SELECT COUNT(patient_id) FROM records WHERE patient_id=" +
                patient.getId() + " AND rec_date>='" + sqlCurrentDate + "'";
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        try {
            String update =
                    "DELETE FROM patients WHERE patient_id=" + patient.getId();
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
    protected boolean delete(Doctor doctor) throws SQLException {
        //todo
        //fix: deletes even if related records exist
        Statement stmt = conn.createStatement();
        //gets current date in sql format for comparing purposes
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        Date sqlCurrentDate = new Date(utilDate.getTime());
        String query = "SELECT COUNT(doctor_id) FROM records WHERE doctor_id=" +
                doctor.getId() + " AND rec_date>='" + sqlCurrentDate + "'";
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        try {
            String update =
                    "DELETE FROM doctors WHERE doctor_id=" + doctor.getId();
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
    protected boolean delete(Record record) throws SQLException {
        Statement stmt = conn.createStatement();
        String update = "DELETE FROM records WHERE record_id=" + record.getId();
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
    protected ArrayList<Patient> search(Patient searchParam) throws SQLException {
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
                String query = "SELECT * FROM patients WHERE insurance_id=" +
                        insuranceId;
                rs = stmt.executeQuery(query);
                if (rs.next()) {
                    p = new Patient(
                            rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                            rs.getInt(6)
                    );
                    p.setId(rs.getInt(1));
                    result.add(p);
                }
            } else {
                String queryBase = "SELECT * FROM patients";
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
    protected ArrayList<Doctor> search(Doctor searchParam) throws SQLException {
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
                query = "SELECT * FROM doctors WHERE doctor_name='" + name +
                        "' AND doctor_surname='" + surname + "' AND room=" + room +
                        " AND specialization='" + specialization + "'";
            } else {
                if (name != null && !name.equals(""))
                    query = "SELECT * FROM doctors" +
                            " WHERE doctor_name='" + name + "'";
                else if (surname != null && !surname.equals(""))
                    query = "SELECT * FROM doctors" +
                            " WHERE doctor_surname='" + surname + "'";
                else if (room != 0)
                    query = "SELECT * FROM doctors WHERE room=" + room;
                else if (specialization != null && !specialization.equals(""))
                    query = "SELECT * FROM doctors" +
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
    protected boolean add(Patient patient) throws SQLException {
        Statement stmt = conn.createStatement();
        int insuranceId = patient.getInsuranceId();
        try {
            String query = "SELECT insurance_id FROM patients";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                if (rs.getInt(1) == insuranceId)
                    return false;
            }
            String update =
                    "INSERT INTO patients (" +
                            "patient_name, patient_surname, district, diagnosis, insurance_id) " +
                            "VALUES ('" +
                            patient.getName() + "', '" +
                            patient.getSurname() + "', '" +
                            patient.getDistrict() + "', '" +
                            patient.getDiagnosis() + "', " +
                            patient.getInsuranceId() + ")";
            stmt.executeUpdate(update);
            System.out.println("patient added");
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
    protected boolean add(Doctor doctor) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String update =
                    "INSERT INTO doctors (" +
                            "doctor_name, doctor_surname, room, specialization) " +
                            "VALUES ('" +
                            doctor.getName() + "', '" +
                            doctor.getSurname() + "', " +
                            doctor.getRoom() + ", '" +
                            doctor.getSpecialization() + "')";
            stmt.executeUpdate(update);
            System.out.println("doctor added");
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
    protected boolean add(Record record) throws SQLException {
        int patient_id = record.getPatient().getId();
        int doctor_id = record.getDoctor().getId();
        java.util.Date date = record.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Statement stmt = conn.createStatement();
        //checks if specified doctor still exist
        String query = "SELECT COUNT(doctor_id) FROM doctors";
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        //checks if specified doctor has less than 5 records on specified date already
        query = "SELECT COUNT(record_id) FROM records" +
                " WHERE doctor_id=" + doctor_id + " AND rec_date='" + format.format(date) + "'";
        rs = stmt.executeQuery(query);
        if (rs.next() && rs.getInt(1) >= 5)
            return false;
        //checks if specified patient exist
        query = "SELECT COUNT(patient_id) FROM patients";
        rs = stmt.executeQuery(query);
        if (!rs.next() || rs.getInt(1) == 0)
            return false;
        String update = "INSERT INTO records (" +
                "doctor_id, patient_id, rec_date) VALUES (" +
                doctor_id + ", " + patient_id + ", '" + format.format(date) + "')";
        System.out.println("add records update:\n" + update);
        stmt.executeUpdate(update);
        rs.close();
        stmt.close();
        System.out.println("record added");
        return true;
    }

    /**
     * @return ArrayList of Strings which are distinct specializations retrieved from
     *         doctors table in DB
     * @throws SQLException
     */
    protected ArrayList<String> getDoctorsSpecializationList() throws SQLException {
        ArrayList<String> result = new ArrayList<String>();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        String query = "SELECT DISTINCT specialization FROM doctors";
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
        String patientTable = "CREATE TABLE patients (" +
                "patient_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "patient_name VARCHAR(20) NOT NULL," +
                "patient_surname VARCHAR(20) NOT NULL," +
                "district VARCHAR(50) NOT NULL," +
                "diagnosis VARCHAR(255) NOT NULL," +
                "insurance_id INT NOT NULL UNIQUE," +
                "PRIMARY KEY (patient_id))";
        String doctorTable = "CREATE TABLE doctors (" +
                "doctor_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_name VARCHAR(20) NOT NULL," +
                "doctor_surname VARCHAR(20) NOT NULL," +
                "room INT NOT NULL," +
                "specialization VARCHAR(50) NOT NULL," +
                "PRIMARY KEY (doctor_id))";

        String recordTable = "CREATE TABLE records (" +
                "record_id INT NOT NULL GENERATED ALWAYS AS IDENTITY," +
                "doctor_id INT NOT NULL," +
                "patient_id INT NOT NULL," +
                "rec_date DATE," +
                "PRIMARY KEY (record_id))";
        stmt.executeUpdate(patientTable);
        stmt.executeUpdate(doctorTable);
        stmt.executeUpdate(recordTable);
        System.out.println("tables created");
        stmt.close();
    }

    private void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE patients");
        stmt.executeUpdate("DROP TABLE doctors");
        stmt.executeUpdate("DROP TABLE records");
        stmt.close();
        System.out.println("tables dropped");
    }

    /*public static void main(String[] args) throws SQLException {
        DBManager d = new DBManager();
        d.dropTables();
        d.createTables();
    }*/
}
