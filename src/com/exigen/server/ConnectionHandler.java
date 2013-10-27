package com.exigen.server;

import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;

@SuppressWarnings("Unchecked")
public class ConnectionHandler implements Runnable {

    private Socket client;
    private DBManager dbManager;
    private ObjectOutputStream objOut;
    private ObjectInputStream objInp;
    private Logger logger;

    public ConnectionHandler(Socket client) throws IOException {
        this.client = client;
        dbManager = DBManager.getInstance();
        logger = ServerLogger.getInstance().getLogger();
        objOut = new ObjectOutputStream(client.getOutputStream());
        objInp = new ObjectInputStream(client.getInputStream());
    }

    @Override
    public void run() {
        try {
            System.out.println("Client handler started");
            while (true) {
                int request = objInp.readInt();
                System.out.println("Request: " + request);
                sendResponse(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends response to client according its request
     *
     * @param request request from client
     * @throws IOException
     */
    private void sendResponse(int request) throws IOException, ClassNotFoundException, SQLException {
        switch (request) {
            case STOP:
                stop();
                break;
            case REQUEST_ALL_LISTS: {
                objOut.writeInt(OK);
                objOut.flush();
                sendAllLists();
                break;
            }
            case REQUEST_PATIENTS_LIST: {
                objOut.writeInt(OK);
                objOut.flush();
                sendPatientsList();
                break;
            }
            case REQUEST_DOCTORS_LIST: {
                objOut.writeInt(OK);
                objOut.flush();
                sendDoctorsList();
                break;
            }
            case REQUEST_RECORDS_LIST: {
                objOut.writeInt(OK);
                objOut.flush();
                sendRecordsList();
                break;
            }
            case REQUEST_ADD_PATIENT: {
                objOut.writeInt(OK);
                objOut.flush();
                sendResponseOnAddPatient((Patient) objInp.readObject());
                break;
            }
            case REQUEST_ADD_DOCTOR: {
                objOut.writeInt(OK);
                objOut.flush();
                sendResponseOnAddDoctor((Doctor) objInp.readObject());
                break;
            }
            case REQUEST_ADD_RECORD: {
                break;
            }
            case REQUEST_EDIT_PATIENT: {
                break;
            }
            case REQUEST_EDIT_DOCTOR: {
                break;
            }
            case REQUEST_DELETE_PATIENT: {
                objOut.writeInt(OK);
                objOut.flush();
                sendResponseOnDeletePatient((Patient) objInp.readObject());
                break;
            }
            case REQUEST_DELETE_DOCTOR: {
                objOut.writeInt(OK);
                objOut.flush();
                sendResponseOnDeleteDoctor((Doctor) objInp.readObject());
                break;
            }
            case REQUEST_DELETE_RECORD: {
                break;
            }
            case REQUEST_SEARCH_PATIENT: {
                break;
            }
            case REQUEST_SEARCH_DOCTOR: {
                break;
            }
            case REQUEST_SEARCH_RECORD: {
                break;
            }
            default:
                System.out.println("unknown request param in sendResponse():" + request);
        }
    }

    /**
     * closes resources before exit
     *
     * @throws IOException
     */
    private void stop() throws IOException {
        System.out.println("Client has been disconnected\nHandler terminated");
        logger.log(Level.FINE, "Client has been disconnected\n" +
                "Handler terminated");
        objInp.close();
        objOut.close();
        client.close();
    }

    private void sendResponseOnAddPatient(Patient patient) throws IOException, SQLException {
        if (dbManager.add(patient)) {
            objOut.writeInt(OK);
            objOut.flush();
        } else {
            objOut.writeInt(ERROR);
            objOut.flush();
        }
    }

    private void sendResponseOnAddDoctor(Doctor doctor) throws IOException, SQLException {
        if (dbManager.add(doctor)) {
            objOut.writeInt(OK);
            objOut.flush();
        } else {
            objOut.writeInt(ERROR);
            objOut.flush();
        }
    }

    private void sendResponseOnDeletePatient(Patient patient) throws IOException, SQLException {
        if (dbManager.delete(patient)) {
            objOut.writeInt(OK);
            objOut.flush();
        } else {
            objOut.writeInt(ERROR);
            objOut.flush();
        }
    }

    private void sendResponseOnDeleteDoctor(Doctor doctor) throws IOException, SQLException {
        if (dbManager.delete(doctor)) {
            objOut.writeInt(OK);
            objOut.flush();
        } else {
            objOut.writeInt(ERROR);
            objOut.flush();
        }
    }

    private void sendAllLists() throws IOException, SQLException {
        ArrayList result = new ArrayList();
        result.add(dbManager.getPatientsList());
        result.add(dbManager.getDoctorsList());
        result.add(dbManager.getRecordsList());
        objOut.writeObject(result);
    }

    private void sendPatientsList() throws IOException, SQLException {
        objOut.writeObject(dbManager.getPatientsList());
    }

    private void sendDoctorsList() throws IOException, SQLException {
        objOut.writeObject(dbManager.getDoctorsList());
    }

    private void sendRecordsList() throws IOException, SQLException {
        objOut.writeObject(dbManager.getRecordsList());
    }
}
