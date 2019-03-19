package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServer;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GetFoodCommand extends Command {
    public GetFoodCommand(ConcurrentMap<String, List<Product>> foodByNameCache, ConcurrentMap<String, Product> foodByUpcCache) {
        super(foodByNameCache, null, foodByUpcCache);
    }

    @Override
    public String execute(String argument) throws NoInformationFoundException {
        Gson gson = new Gson();

        if (foodByNameCache.containsKey(argument)) {
            List<Product> products = foodByNameCache.get(argument);

            return products.stream()
                           .map(Product::toString)
                           .collect(Collectors.joining(";"));
        }

        String url = API_URL + "/search/?q=" + argument + "&api_key=" + API_KEY;

        try {
            JsonObject responseToJson = urlResponseToJson(url);
            JsonElement listProperty = responseToJson.get("list");

            if(listProperty == null) {
                throw new NoInformationFoundException("No information found for " + argument + ".");
            }

            JsonArray items = listProperty.getAsJsonObject()
                                          .get("item").getAsJsonArray();

            List<Product> products = gson.fromJson(items, new TypeToken<List<Product>>() {}.getType());
            products.forEach(Product::setNameAndUpc);

            // Updating foodByNameCache
            foodByNameCache.put(argument, products);

            // Updating foodByUpcCache
            products.stream()
                    .filter(product -> product.getUpc() != null)
                    .collect(Collectors.toList())
                    .forEach(product -> foodByUpcCache.put(product.getUpc(), product));

            return products.stream()
                           .map(Product::toString)
                           .collect(Collectors.joining(";"));
        } catch (IOException | InterruptedException e) {
            Logger foodServerLogger = FoodServer.getFoodServerLogger();
            foodServerLogger.log(Level.WARNING, "Exception caught in GetFoodCommand::execute.", e);
        }

        return null;
    }
}
