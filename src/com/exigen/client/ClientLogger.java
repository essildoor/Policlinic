package com.exigen.client;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientLogger {
    private static ClientLogger instance = new ClientLogger();
    private Logger clientLogger;

    private ClientLogger() {
        this.clientLogger = Logger.getLogger("com.exigen.client");
        try {
            FileHandler fileHandler = new FileHandler("Client.log");
            fileHandler.setFormatter(new SimpleFormatter());
            clientLogger.addHandler(fileHandler);
            //LEVEL - WARNING
            clientLogger.setLevel(Level.WARNING);
        } catch (IOException e) {
            clientLogger.log(Level.SEVERE, "IO error due log file creation");
        }
    }

    public static ClientLogger getInstance() {
        return instance;
    }

    public Logger getClientLogger() {
        return this.clientLogger;
    }
}
