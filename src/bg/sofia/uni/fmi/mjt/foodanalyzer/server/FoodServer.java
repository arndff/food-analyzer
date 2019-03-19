package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FoodServer {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 20;

    private static final String CACHES_FOLDER = "data/";
    private static final String FOOD_BY_NAME_CACHE_FILE = "data/foodByName.json";
    private static final String FOOD_BY_NDBNO_CACHE_FILE = "data/foodByNdbno.json";
    private static final String FOOD_BY_UPC_CACHE_FILE = "data/foodByUpc.json";

    private ConcurrentMap<String, List<Product>> foodByNameCache;
    private ConcurrentMap<String, Report> foodByNdbnoCache;
    private ConcurrentMap<String, Product> foodByUpcCache;

    protected static final Logger logger = Logger.getLogger(FoodServer.class.getName());

    private static final FoodServer foodServer = new FoodServer();

    private FoodServer() {}

    public static FoodServer getFoodServer() {
        return foodServer;
    }

    public static Logger getFoodServerLogger() {
        return logger;
    }

    private File setupFilePath(String path) {
        File file = new File(path);

        // if the file already exists, it won't be created once again
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.log(Level.WARNING,
                        "Error occurred when createNewFile() method has been called in FoodServer::setupFilePath.", e);
            }
        }

        return file;
    }

    private <T> ConcurrentMap<String, T> loadCache(String path) {
        File file = setupFilePath(path);
        ConcurrentMap<String, T> cache = new ConcurrentHashMap<>();

        if (file.length() == 0) {
            return cache;
        }

        final String error = "Error occurred in FoodServer::loadCache.";

        try (FileReader in = new FileReader(file)) {
            Gson gson = new Gson();
            cache = gson.fromJson(in, new TypeToken<ConcurrentMap<String, T>>() {}.getType());
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, error + "(" + path + " not found)", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, error, e);
        }

        return cache;
    }

    private <T> void saveCache(String path, ConcurrentMap<String, T> cache) {
        File file = new File(path);

        try (FileWriter out = new FileWriter(file, false)) {
            Gson gson = new Gson();
            out.write(gson.toJson(cache));
        } catch (IOException e) {
            logger.log(Level.WARNING,
                    String.format("Error occurred in FoodServer::saveCache. Cache argument: %s", path.split("/")[1]), e);
        }
    }

    private void loadAllCachesFromFiles() {
        foodByNameCache = loadCache(FOOD_BY_NAME_CACHE_FILE);
        foodByNdbnoCache = loadCache(FOOD_BY_NDBNO_CACHE_FILE);
        foodByUpcCache = loadCache(FOOD_BY_UPC_CACHE_FILE);
    }

    private void saveAllCachesToFiles() {
        if (foodByNameCache.size() != 0) {
            saveCache(FOOD_BY_NAME_CACHE_FILE, foodByNameCache);
        }
        if (foodByNdbnoCache.size() != 0) {
            saveCache(FOOD_BY_NDBNO_CACHE_FILE, foodByNdbnoCache);
        }
        if (foodByUpcCache.size() != 0) {
            saveCache(FOOD_BY_UPC_CACHE_FILE, foodByUpcCache);
        }

        logger.log(Level.INFO, "Data from all three caches have been saved successfully.");
    }

    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

        // FoodServer::saveAllCachesToFiles is going to be executed after the server is terminated
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAllCachesToFiles));

        File cachesFolder = new File(CACHES_FOLDER);
        cachesFolder.mkdir();

        loadAllCachesFromFiles();
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
                            new ClientRequestHandler(clientSocket, foodByNameCache, foodByNdbnoCache, foodByUpcCache);

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
        getFoodServer().start();
    }
}
