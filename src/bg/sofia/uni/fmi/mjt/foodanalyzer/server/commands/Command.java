package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServer;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidBarcodeArgumentsException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public abstract class Command {
    protected final ConcurrentMap<String, List<Product>> foodByNameCache;
    protected final ConcurrentMap<String, Report> foodByNdbnoCache;
    protected final ConcurrentMap<String, Product> foodByUpcCache;

    protected static final String API_URL = "https://api.nal.usda.gov/ndb";
    protected static final String API_KEY = "yqVQElHgqao3jzD9KbtKeygI2UqpOf41XYbNpcd9";

    protected static final Logger foodServerLogger = FoodServer.getFoodServerLogger();

    Command(ConcurrentMap<String, List<Product>> foodByNameCache,
            ConcurrentMap<String, Report> foodByNdbnoCache,
            ConcurrentMap<String, Product> foodByUpcCache) {
        this.foodByNameCache = foodByNameCache;
        this.foodByNdbnoCache = foodByNdbnoCache;
        this.foodByUpcCache = foodByUpcCache;
    }

    // helper method which is used in all query methods
    protected JsonObject urlResponseToJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonParser parser = new JsonParser();

        return parser.parse(response).getAsJsonObject();
    }

    public abstract String execute(String argument) throws InvalidBarcodeArgumentsException, NoInformationFoundException;
}
