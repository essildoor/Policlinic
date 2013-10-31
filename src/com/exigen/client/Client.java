package com.exigen.client;

import com.exigen.client.gui.MainForm;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;

@SuppressWarnings("Unchecked")
public class Client {

    private ObjectInputStream objInp;
    private ObjectOutputStream objOut;
    private Socket clientSocket;
    private Logger logger;

    public Client(String host, int port) throws IOException {
        //establishes connection to server with a specified params
        logger = ClientLogger.getInstance().getLogger();
        clientSocket = new Socket(host, port);
        objInp = new ObjectInputStream(clientSocket.getInputStream());
        objOut = new ObjectOutputStream(clientSocket.getOutputStream());

    }

    //client console mode
    public static void main(String[] args) throws IOException {
        ClientConfig cfg = ClientConfig.getInstance();
        Client client = new Client(cfg.getHost(), cfg.getPort());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int request = 0;
        //type 100 for exit
        while (request != STOP) {
            System.out.print("type request code>");
            request = Integer.parseInt(br.readLine());
            Object o = client.sendRequest(request, null);
            System.out.println("object = " + o);
        }
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
            logger.log(Level.FINEST, "sendRequest: request=" + request + ", param=" + param);
            objOut.writeInt(request);
            objOut.flush();
            logger.log(Level.FINEST, "sendRequest: request dispatched");
            int response = objInp.readInt();
            logger.log(Level.FINEST, "sendRequest: response=" + response);
            if (response == OK)
                switch (request) {
                    case REQUEST_ALL_LISTS:
                        return objInp.readObject();
                    case REQUEST_PATIENTS_LIST:
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readObject();
                    case REQUEST_DOCTORS_LIST:
                        objOut.writeObject(param);
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
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readInt();
                    }
                    case REQUEST_EDIT_PATIENT: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readInt();
                    }
                    case REQUEST_EDIT_DOCTOR: {
                        objOut.writeObject(param);
                        objOut.flush();
                        return objInp.readInt();
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
            logger.log(Level.WARNING, "server response is not OK");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SERVER_IS_NOT_RESPONDING;
    }
}
