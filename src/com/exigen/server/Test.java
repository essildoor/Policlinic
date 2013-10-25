package com.exigen.server;

import com.exigen.entity.Patient;

import java.io.*;
import java.net.Socket;

public class Test {
    public static void main(String[] args) {
        try {
            Patient patient = new Patient("andrei", "ivanov", "vyborgsky", "");

            Socket s = new Socket("localhost", 4545);
            ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());
            objOut.writeObject(patient);
            objOut.flush();
            objOut.close();
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String text = "";
            while (!text.equals("stop")) {
                System.out.print(">");
                text = consoleReader.readLine();
                out.println(text);
            }
        } catch (IOException e) {
            System.out.println("Couldnt connect on local host:4545");
            e.printStackTrace();
        }

    }
}
