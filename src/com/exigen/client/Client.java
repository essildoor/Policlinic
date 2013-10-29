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
        String host = "";
        int port = 0;
        try {
            //establishes connection to server with a specified params
            logger = ClientLogger.getInstance().getLogger();
            cfg = ClientConfig.getInstance();
            port = cfg.getPort();
            host = cfg.getHost();
            s = new Socket(host, port);
            objInp = new ObjectInputStream(s.getInputStream());
            objOut = new ObjectOutputStream(s.getOutputStream());
            //launches main form instance related to current client instance
            MainForm.setupAndShowGUI(this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't connect to socket " + host + ":" + port);
            //e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        Client client = new Client();
        /*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int request;
        while (true) {
            System.out.print(">");
            request = Integer.parseInt(br.readLine());
            Object o = client.sendRequest(request, null);
            System.out.println("object = " + o);
        }*/
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
            System.out.println("sendRequest: request=" + request + ", param=" + param);
            objOut.writeInt(request);
            objOut.flush();
            System.out.println("sendRequest: request dispatched");
            int response = objInp.readInt();
            System.out.println("sendRequest: response=" + response);
            if (response == OK)
                switch (request) {
                    case REQUEST_ALL_LISTS:
                        objOut.writeObject(param);
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
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
                    }
                    case REQUEST_SEARCH_DOCTOR: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
                    }
                    case REQUEST_SEARCH_RECORD: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
                    }
                    case REQUEST_DOCTOR_SPECIALIZATION_LIST: {
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
