package com.exigen.client;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientLogger {
    private static ClientLogger instance = new ClientLogger();
    private Logger logger;

    private ClientLogger() {
        this.logger = Logger.getLogger("com.exigen.client");
        try {
            FileHandler fileHandler = new FileHandler("Client.log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            //LEVEL - WARNING
            logger.setLevel(Level.WARNING);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error due client.log file creation");
        }
    }

    public static ClientLogger getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
