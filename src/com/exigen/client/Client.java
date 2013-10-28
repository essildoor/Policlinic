package com.exigen.client;

import com.exigen.client.gui.MainForm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
                        objOut.writeObject(null);
                        objOut.flush();
                        return objInp.readObject();
                    case REQUEST_PATIENTS_LIST:
                        return objInp.readObject();
                    case REQUEST_DOCTORS_LIST:
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
                    case REQUEST_RECORDS_LIST:
                        return objInp.readObject();
                    case REQUEST_ADD_PATIENT: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readInt();
                    }
                    case REQUEST_ADD_DOCTOR: {
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
                    case REQUEST_DELETE_RECORD: {

                    }
                    case REQUEST_EDIT_PATIENT: {

                    }
                    case REQUEST_EDIT_DOCTOR: {

                    }
                    case REQUEST_SEARCH_PATIENT: {

                    }
                    case REQUEST_SEARCH_DOCTOR: {

                    }
                    case REQUEST_SEARCH_RECORD: {

                    }
                    case REQUEST_DOCTOR_SPECIALIZATION_LIST: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
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
}
