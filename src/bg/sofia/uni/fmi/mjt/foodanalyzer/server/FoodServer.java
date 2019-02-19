package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Report;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
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

public class FoodServer {
    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 10;

    private static final String FOOD_BY_NAME_CACHE_FILE = "data/foodByName.json";
    private static final String FOOD_BY_NDBNO_CACHE_FILE = "data/foodByNdbno.json";
    private static final String FOOD_BY_UPC_CACHE_FILE = "data/foodByUpc.json";

    private static ConcurrentMap<String, List<Product>> foodByNameCache;
    private static ConcurrentMap<String, Report> foodByNdbnoCache;
    private static ConcurrentMap<String, Product> foodByUpcCache;

    private static File setupFilePath(String path) {
        // if the folder and the file already exist, they won't be created once again
        File folder = new File(path.substring(0, path.indexOf('/')));
        folder.mkdir();
        File file = new File(path);

        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return file;
    }

    private static ConcurrentMap<String, List<Product>> loadFoodByNameCache() {
        File file = setupFilePath(FOOD_BY_NAME_CACHE_FILE);
        ConcurrentMap<String, List<Product>> cache = new ConcurrentHashMap<>();

        if (file.length() == 0) {
            return cache;
        }

        try (FileReader in = new FileReader(file)) {
             Gson gson = new Gson();
             cache = gson.fromJson(in, new TypeToken<ConcurrentMap<String, List<Product>>>() {}.getType());
        } catch (IOException e) {
             System.err.println(e.getMessage());
        }

        return cache;
    }

    private static <T> ConcurrentMap<String, T> loadSimpleCache(String path) {
        File file = setupFilePath(path);
        ConcurrentHashMap<String, T> cache = new ConcurrentHashMap<>();

        if (file.length() == 0) {
            return cache;
        }

        try (FileReader in = new FileReader(file)) {
             Gson gson = new Gson();
             cache = gson.fromJson(in, new TypeToken<ConcurrentMap<String, T>>() {}.getType());
        } catch (IOException e) {
             System.err.println(e.getMessage());
        }

        return cache;
    }

    private static <T> void saveCache(String path, T cache) {
        File file = new File(path);

        try (FileWriter out = new FileWriter(file, false)) {
             Gson gson = new Gson();
             out.write(gson.toJson(cache));
        } catch(IOException e) {
             System.err.println(e.getMessage());
        }
    }

    private static void loadAllCachesFromFiles() {
        foodByNameCache = loadFoodByNameCache();
        foodByNdbnoCache = loadSimpleCache(FOOD_BY_NDBNO_CACHE_FILE);
        foodByUpcCache = loadSimpleCache(FOOD_BY_UPC_CACHE_FILE);
    }

    private static void saveAllCachesToFiles() {
        if (foodByNameCache.size() != 0) {
            saveCache(FOOD_BY_NAME_CACHE_FILE, foodByNameCache);
        }
        if (foodByNdbnoCache.size() != 0) {
            saveCache(FOOD_BY_NDBNO_CACHE_FILE, foodByNdbnoCache);
        }
        if (foodByUpcCache.size() != 0) {
            saveCache(FOOD_BY_UPC_CACHE_FILE, foodByUpcCache);
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

        // the method inside run() is going to be executed after the server is closed
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                saveAllCachesToFiles();
            }
        });

        loadAllCachesFromFiles();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
             System.out.println("FoodServer has been started and listening for incoming requests.");
             Socket clientSocket;

             while (true) {
                // Calling accept() blocks and waits for connection request by a client
                // When a request comes, accept() returns a socket to communicate with this client
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection request from client: " + clientSocket.getInetAddress());

                // Want each client to be processed in a separate thread
                // to keep the current thread free to accept() requests from new clients
                ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket,
                                                                              foodByNameCache,
                                                                              foodByNdbnoCache,
                                                                              foodByUpcCache);

                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("Server is terminating...");
    }
}
