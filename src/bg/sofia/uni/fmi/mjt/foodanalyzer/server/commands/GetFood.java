package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class GetFood extends Command {
    public GetFood(ConcurrentMap<String, List<Product>> foodByNameCache, ConcurrentMap<String, Product> foodByUpcCache) {
        super(foodByNameCache, null, foodByUpcCache);
    }

    @Override
    public String execute(String argument) {
        Gson gson = new Gson();

        if (foodByNameCache.containsKey(argument)) {
            List<Product> products = foodByNameCache.get(argument);

            return products.stream()
                           .map(Product::toString)
                           .collect(Collectors.joining("\n"));
        }

        String url = API_URL + "/search/?q=" + argument + "&api_key=" + API_KEY;

        try {
            JsonObject responseToJson = urlResponseToJson(url);
            JsonArray items = responseToJson.get("list").getAsJsonObject()
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
        } catch (NullPointerException e) {
            return "No information found for " + argument + ".";
        } catch (IOException | InterruptedException e) {
            // logger?


            e.printStackTrace();
        }

        return null;
    }
}
