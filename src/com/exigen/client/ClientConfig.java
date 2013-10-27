package com.exigen.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConfig {

    private static ClientConfig instance = new ClientConfig();
    //Properties variables
    private int mainFrameDefaultWidth = 800;
    private int mainFrameDefaultHeight = 600;
    private String host = "localhost";
    private int port = 4545;
    private int mainFrameTablesSyncRateMs = 30000;

    public static ClientConfig getInstance() {
        return instance;
    }

    private ClientConfig() {
        //logger init
        Logger logger = ClientLogger.getInstance().getLogger();
        //load properties
        Properties config = new Properties();
        try {
            config.loadFromXML(new FileInputStream("ClientConfig.xml"));
            this.mainFrameDefaultWidth =
                    Integer.parseInt(config.getProperty("mainFrameDefaultWidth"));
            this.mainFrameDefaultHeight =
                    Integer.parseInt(config.getProperty("mainFrameDefaultHeight"));
            this.host = config.getProperty("hostName");
            this.port = Integer.parseInt(config.getProperty("portNumber"));
            this.mainFrameTablesSyncRateMs =
                    Integer.parseInt(config.getProperty("mainFrameTablesSyncRateMs"));
            logger.log(Level.INFO, "ClientConfig initialized successfully");
        } catch (IOException e) {
            logger.log(Level.WARNING, "IO error due reading ClientConfig.xml\n" +
                    "default values are applied");
        }
    }

    public int getMainFrameTablesSyncRateMs() {
        return mainFrameTablesSyncRateMs;
    }

    public int getMainFrameDefaultWidth() {
        return mainFrameDefaultWidth;
    }

    public int getMainFrameDefaultHeight() {
        return mainFrameDefaultHeight;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
