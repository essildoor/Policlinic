package com.exigen.server;

import java.util.logging.Logger;

public class ServerConfig {

    private Logger logger;
    private static ServerConfig instance = new ServerConfig();
    //Properties variables
    private int serverThreadPoolSize = 5;
    private int serverPort = 4545;


}
