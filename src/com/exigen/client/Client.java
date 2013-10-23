package com.exigen.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread{
    public static void main(String[] args) {
        try {
            //dispatching
            Socket s = new Socket("localhost", 3128);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            System.out.print(">");
            String text = consoleReader.readLine();
            out.println(text);
            //receiving
            String data = in.readLine();
            //printing to console
            System.out.println(data);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
