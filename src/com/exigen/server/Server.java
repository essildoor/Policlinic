package com.exigen.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final int THREAD_POOL_SIZE = 2;
    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void start(int port) throws IOException {
        final ServerSocket ss = new ServerSocket(port);
        System.out.println("Server started");
        while (!executor.isShutdown()) {
            executor.submit(new ConnectionHandler(ss.accept()));
        }
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        new Server().start(4545);
    }
}
