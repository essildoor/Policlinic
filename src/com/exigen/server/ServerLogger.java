package com.exigen.server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerLogger {
    private static ServerLogger instance = new ServerLogger();
    private Logger logger;

    private ServerLogger() {
        this.logger = Logger.getLogger("com.exigen.server");
        try {
            FileHandler fileHandler = new FileHandler("Server.log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            //LEVEL - WARNING
            logger.setLevel(Level.WARNING);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error due server.log file creation");
        }
    }

    public static ServerLogger getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
