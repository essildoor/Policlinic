package com.exigen.client;

import com.exigen.client.gui.MainForm;
import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;

@SuppressWarnings("Unchecked")
public class Client {

    private ObjectInputStream objInp;
    private ObjectOutputStream objOut;
    private Socket s;
    private ClientConfig cfg;
    private Logger logger;

    public Client() {
        try {
            //establishes connection to server with a specified params
            logger = ClientLogger.getInstance().getLogger();
            cfg = ClientConfig.getInstance();
            int port = cfg.getPort();
            String host = cfg.getHost();
            s = new Socket(host, port);
            objInp = new ObjectInputStream(s.getInputStream());
            objOut = new ObjectOutputStream(s.getOutputStream());

            //launches main form instance related to current client instance
            MainForm.setupAndShowGUI(this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in Client init " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
    }

    /**
     * sends any request to server, returns any possible response =)
     *
     * @param request request
     * @param param   request parameter
     * @return Object
     */
    public synchronized Object sendRequest(int request, Object param) {
        try {
        objOut.writeInt(request);
        objOut.flush();
        int response = objInp.readInt();
        if (response == OK)
            switch (request) {
                case REQUEST_ALL_LISTS:
                    return receiveAllLists();
                case REQUEST_PATIENTS_LIST:
                    return receivePatientsList();
                case REQUEST_DOCTORS_LIST:
                    return receiveDoctorsList();
                case REQUEST_RECORDS_LIST:
                    return receiveRecordsList();
                case REQUEST_ADD_PATIENT: {
                    objOut.writeObject(param);
                    objOut.flush();
                    return objInp.readInt();
                }
                case REQUEST_ADD_DOCTOR:  {
                    objOut.writeObject(param);
                    objOut.flush();
                    return objInp.readInt();
                }
                case REQUEST_ADD_RECORD: {
                    objOut.writeObject(param);
                    objOut.flush();
                    return objInp.readInt();
                }
                case REQUEST_DELETE_PATIENT: {
                    objOut.writeObject(param);
                    objOut.flush();
                    return objInp.readInt();
                }
                case REQUEST_DELETE_DOCTOR: {
                    objOut.writeObject(param);
                    objOut.flush();
                    return objInp.readInt();
                }
            }
        System.out.println("server response is not OK");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * receives all tables data from server
     *
     * @return ArrayList with 3 Lists: patients, doctors and records
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList receiveAllLists() throws IOException, ClassNotFoundException {
        return (ArrayList) objInp.readObject();
    }

    /**
     * receives patients list from server
     *
     * @return ArrayList of Patient instances
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList<Patient> receivePatientsList() throws IOException, ClassNotFoundException {
        return (ArrayList<Patient>) objInp.readObject();
    }

    /**
     * receives doctors list from server
     *
     * @return ArrayList of Doctor instances
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList<Doctor> receiveDoctorsList() throws IOException, ClassNotFoundException {
        return (ArrayList<Doctor>) objInp.readObject();
    }

    /**
     * receives records list from server
     *
     * @return ArrayList of Record instances
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList<Record> receiveRecordsList() throws IOException, ClassNotFoundException {
        return (ArrayList<Record>) objInp.readObject();
    }


}
