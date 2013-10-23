package com.exigen.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    Socket s;
    int num;

    public static void main(String[] args) {
        try {
            int i = 0;
            ServerSocket serverSocket = new ServerSocket(3128, 0, InetAddress.getByName("localhost"));
            System.out.println("server started");

            while (true) {
                new Server(i, serverSocket.accept());
                System.out.println("opening connection #" + ++i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Server(int num, Socket s) {
        this.num = num;
        this.s = s;

        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            String data = in.readLine();
            data = "" + num + ": " + data;
            out.println(data);
            s.close();
        } catch (IOException e) {
            System.out.println("init error: " + e);
        }
    }

}
