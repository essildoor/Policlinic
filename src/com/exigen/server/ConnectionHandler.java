package com.exigen.server;

import com.exigen.entity.Patient;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    Socket client;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            ObjectInputStream objInp = new ObjectInputStream(client.getInputStream());
            Patient patient = (Patient) objInp.readObject();
            System.out.print("surname: " + patient.getSurname());
            System.out.print("  name: " + patient.getName());
            System.out.print("  district: " + patient.getDistrict());
            System.out.print("  diagnosis: " + patient.getDiagnosis());
            String request = "";
            while (!request.equals("stop")) {
                request = in.readLine();
                System.out.println("Request: " + request);
            }
            client.close();
        } catch (IOException e) {
            System.out.println("I/O error " + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
