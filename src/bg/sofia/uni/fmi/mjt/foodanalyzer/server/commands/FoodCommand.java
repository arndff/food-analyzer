package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.ProductResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FoodCommand extends AbstractCommand {
    private ConcurrentMap<String, List<Product>> foodByNameCache;
    private ConcurrentMap<String, Product> foodByUpcCache;

    FoodCommand(HttpClient client, ConcurrentMap<String, List<Product>> foodByNameCache, ConcurrentMap<String, Product> foodByUpcCache) {
        super(client);
        this.foodByNameCache = foodByNameCache;
        this.foodByUpcCache = foodByUpcCache;
    }

    @Override
    public String execute(String argument) throws NoInformationFoundException {
        if (foodByNameCache.containsKey(argument)) {
            List<Product> products = foodByNameCache.get(argument);

            return products.stream()
                           .map(Product::toString)
                           .collect(Collectors.joining(";"));
        }

        String url = API_URL + "/search/?q=" + argument + "&api_key=" + API_KEY;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            Gson gson = new Gson();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ProductResponse productResponse = gson.fromJson(response, ProductResponse.class);

            if (productResponse.getList() == null) {
                throw new NoInformationFoundException("No information found for food=" + argument + ".");
            }

            List<Product> products = productResponse.getList().getItem();
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
            foodServerLogger.log(Level.WARNING, "Exception caught in FoodCommand::execute.", e);
        }

        return null;
    }
}
