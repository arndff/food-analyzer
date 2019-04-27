package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.entity.Report;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FoodServerCache {
    private static final String CACHES_FOLDER = "data/";

    private static final String FOOD_BY_NAME_CACHE_FILE = "data/foodByName.json";
    private static final String FOOD_BY_NDBNO_CACHE_FILE = "data/foodByNdbno.json";
    private static final String FOOD_BY_UPC_CACHE_FILE = "data/foodByUpc.json";

    private ConcurrentMap<String, List<Product>> foodByNameCache;
    private ConcurrentMap<String, Report> foodByNdbnoCache;
    private ConcurrentMap<String, Product> foodByUpcCache;

    private Logger logger = FoodServer.getFoodServerLogger();

    public ConcurrentMap<String, List<Product>> getFoodByNameCache() {
        return foodByNameCache;
    }

    public ConcurrentMap<String, Report> getFoodByNdbnoCache() {
        return foodByNdbnoCache;
    }

    public ConcurrentMap<String, Product> getFoodByUpcCache() {
        return foodByUpcCache;
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

    void loadAllCachesFromFiles() {
        File cachesFolder = new File(CACHES_FOLDER);
        cachesFolder.mkdir();

        foodByNameCache = loadCache(FOOD_BY_NAME_CACHE_FILE, new TypeToken<List<Product>>(){}.getType());
        foodByNdbnoCache = loadCache(FOOD_BY_NDBNO_CACHE_FILE, Report.class);
        foodByUpcCache = loadCache(FOOD_BY_UPC_CACHE_FILE, Product.class);
    }

    void saveAllCachesToFiles() {
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

    private <T> ConcurrentMap<String, T> loadCache(String path, Type myType) {
        File file = setupFilePath(path);
        ConcurrentMap<String, T> cache = new ConcurrentHashMap<>();

        if (file.length() == 0) {
            return cache;
        }

        final String error = "Error occurred in FoodServer::loadCache.";

        try (FileReader in = new FileReader(file)) {
            Gson gson = new Gson();
            // There's an issue with this approach caused by the generics, imho.
            // cache = gson.fromJson(in, new TypeToken<ConcurrentMap<String, T>>() {}.getType());

            // Solution to the aforementioned issue using getParameterized method which wasn't available in GSON 2.6.2!
            // Project's GSON version: 2.8.5
            cache = gson.fromJson(in, TypeToken.getParameterized(ConcurrentMap.class, String.class, myType).getType());
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
}
