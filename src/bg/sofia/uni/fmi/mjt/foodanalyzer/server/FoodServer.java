package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FoodServer {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 20;

    private static final FoodServerCache cache = new FoodServerCache();
    private static final Logger logger = Logger.getLogger(FoodServer.class.getName());

    public static Logger getFoodServerLogger() {
        return logger;
    }

    private void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

        // FoodServer::saveAllCachesToFiles is going to be executed after the server is terminated
        Runtime.getRuntime().addShutdownHook(new Thread(cache::saveAllCachesToFiles));

        cache.loadAllCachesFromFiles();
        logger.log(Level.INFO, "Data from all three caches have been loaded successfully.");

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            logger.log(Level.INFO, "FoodServer has been started and listening for incoming requests.");
            Socket clientSocket;

            while (true) {
                // Calling accept() blocks and waits for connection request by a client
                // When a request comes, accept() returns a socket to communicate with this client
                try {
                    clientSocket = serverSocket.accept();
                    logger.log(Level.INFO, "Accepted connection request from client: " + clientSocket.getInetAddress());

                    // Want each client to be processed in a separate thread
                    // to keep the current thread free to accept() requests from new clients
                    ClientRequestHandler clientHandler =
                            new ClientRequestHandler(clientSocket, cache);

                    executorService.execute(clientHandler);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "ServerSocket::accept failed.", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create an instance of ServerSocket.", e);
        }

        logger.log(Level.INFO, "Server is terminating...");
    }

    public static void main(String[] args) {
        FoodServer foodServer = new FoodServer();
        foodServer.start();
    }
}
