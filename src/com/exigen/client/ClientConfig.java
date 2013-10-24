package com.exigen.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConfig {

    private Logger logger;
    private static ClientConfig instance = new ClientConfig();

    private Properties config;
    private int mainFrameDefaultWidth = 800;
    private int mainFrameDefaultHeight = 600;
    private String hostName = "localhost";
    private int portNumber = 3177;

    public static ClientConfig getInstance() {
        return instance;
    }

    private ClientConfig() {
        //logger init
        logger = ClientLogger.getInstance().getLogger();
        //load properties
        this.config = new Properties();
        try {
            config.loadFromXML(new FileInputStream("ClientConfig.xml"));
            this.mainFrameDefaultWidth =
                    Integer.parseInt(config.getProperty("mainFrameDefaultWidth"));
            this.mainFrameDefaultHeight =
                    Integer.parseInt(config.getProperty("mainFrameDefaultHeight"));
            this.hostName = config.getProperty("hostName");
            this.portNumber = Integer.parseInt(config.getProperty("portNumber"));
            logger.log(Level.INFO, "ClientConfig initialized successfully");
        } catch (IOException e) {
            logger.log(Level.WARNING, "IO error due reading ClientConfig.xml\n" +
                    "default values are applied");
        }
    }

    public int getMainFrameDefaultWidth() {
        return mainFrameDefaultWidth;
    }

    public int getMainFrameDefaultHeight() {
        return mainFrameDefaultHeight;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }
}
