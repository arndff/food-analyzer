package bg.sofia.uni.fmi.mjt.foodanalyzer;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Report;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.UPCAReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ClientRequestHandler implements Runnable {
    private Socket socket;
    private String apiKey = "yqVQElHgqao3jzD9KbtKeygI2UqpOf41XYbNpcd9";

    private final ConcurrentMap<String, List<Product>> foodByNameCache;
    private final ConcurrentMap<String, Report> foodByNdboCache;
    private final ConcurrentMap<String, Product> foodByUpcCache;

    public ClientRequestHandler(Socket socket,
                                ConcurrentMap<String, List<Product>> foodByNameCache,
                                ConcurrentMap<String, Report> foodByNdbnoCache,
                                ConcurrentMap<String, Product> foodByUpcCache) {
        this.socket = socket;
        // this.apiKey = apiKey;

        this.foodByNameCache = foodByNameCache;
        this.foodByNdboCache = foodByNdbnoCache;
        this.foodByUpcCache = foodByUpcCache;
    }

    @Override
    public void run() {
        try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String inputLine;

            while((inputLine = in.readLine()) != null) { // read the message from the client
                String[] queryTokens = inputLine.split(" ");
                String inputValidationErr = validateUserInput(queryTokens);

                if(inputValidationErr == null) {
                    String result = executeQueryByItsType(queryTokens);
                    out.println(result); // send result to the client
                } else {
                    out.println(inputValidationErr);
                }
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String validateUserInput(String[] userInput) {
        if(userInput.length != 2) {
            return "A query must have a type and an argument.";
        }

        String queryType = userInput[0];

        switch(queryType) {
            case "get-food": case "get-food-report": case "get-food-by-barcode":
                return null;
            default:
                return "Incorrect query type.";
        }
    }

    private boolean validateQueryByBarcodeArg(String arg) {
        return (arg.startsWith("--upc=<") || arg.startsWith("--img=<")) && arg.endsWith(">");
    }

    private String extractQueryByBarcodeArg(String arg) {
        int openingBracketIdx = arg.indexOf('<');
        int closingBracketIdx = arg.indexOf('>');

        return arg.substring(openingBracketIdx + 1, closingBracketIdx);
    }

    private String executeQueryByItsType(String[] userInput) {
        String queryType = userInput[0];
        String queryArg = userInput[1];

        switch(queryType) {
            case "get-food":
                return getFoodByName(queryArg);
            case "get-food-report":
                return getFoodByNdbno(queryArg);
            case "get-food-by-barcode":
                String[] splittedArgs = queryArg.split("\\|");

                if(validateQueryByBarcodeArg(splittedArgs[0])) {
                    String arg = extractQueryByBarcodeArg(splittedArgs[0]);
                    boolean isPathToImg = splittedArgs[0].startsWith("--img");

                    return getFoodByBarcode(arg, isPathToImg);
                } else {
                    return "Get food by barcode was called with invalid argument.";
                }

            default:
                return null; // should never reach this line
        }
    }

    // helper method which is used in all query methods
    private JsonObject urlResponseToJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonParser parser = new JsonParser();

        return parser.parse(response).getAsJsonObject();
    }

    private String getFoodByName(String name) {
        Gson gson = new Gson();

        if (foodByNameCache.containsKey(name)) {
            List<Product> products = foodByNameCache.get(name);
            StringBuilder allProducts = new StringBuilder();

            for (Product product : products) {
                allProducts.append(product);
                allProducts.append(";");
            }

            return allProducts.toString();
        }

        String url = "https://api.nal.usda.gov/ndb/search/?q=" + name + "&api_key=" + apiKey;

        try {
            JsonObject responseToJson = urlResponseToJson(url);
            JsonArray items = responseToJson.get("list").getAsJsonObject()
                                            .get("item").getAsJsonArray();

            List<Product> products = gson.fromJson(items, new TypeToken<List<Product>>() {}.getType());
            StringBuilder allProducts = new StringBuilder();

            for(Product product : products) {
                product.setNameAndUpc();
                allProducts.append(product);
                allProducts.append(";");
            }

            foodByNameCache.put(name, products);

            for(Product product : products) {
                if(product.getUpc() != null) {
                    System.out.println(product);
                    foodByUpcCache.put(product.getUpc(), product);
                }
            }

            return allProducts.toString();
        } catch(NullPointerException e) {
            return "No information found for " + name + ".";
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    // helper method which is used in getFoodByNdbno to set some of Report's object data members
    private double extractSpecificNutrient(JsonArray nutrients, int index) {
        return nutrients.get(index).getAsJsonObject()
                        .get("value").getAsDouble();
    }

    private String getFoodByNdbno(String ndbno) {
        if(foodByNdboCache.containsKey(ndbno)) {
            System.out.println("Printing from if..........");

            return foodByNdboCache.get(ndbno).toString();
        }

        String url = "https://api.nal.usda.gov/ndb/V2/reports?ndbno=" + ndbno + "&format=json&api_key=" + apiKey;

        try {
            JsonObject response = urlResponseToJson(url);

            JsonObject food = response.getAsJsonArray("foods")
                                      .get(0).getAsJsonObject()
                                      .get("food").getAsJsonObject();

            String name = food.get("desc").getAsJsonObject()
                              .get("name").getAsString().split(", U")[0];

            String ingredients = food.get("ing").getAsJsonObject()
                                     .get("desc").getAsString();

            JsonArray nutrients = food.get("nutrients").getAsJsonArray();
            double kcal = extractSpecificNutrient(nutrients, 0);
            double protein = extractSpecificNutrient(nutrients, 1);
            double fat = extractSpecificNutrient(nutrients, 2);
            double carbohydrate = extractSpecificNutrient(nutrients, 3);
            double fiber = extractSpecificNutrient(nutrients, 4);

            Report report = new Report(name, ingredients, kcal, protein, fat, carbohydrate, fiber);

            foodByNdboCache.put(ndbno, report);

            return report.toString();
        } catch(NullPointerException e) {
            return "No information found for ndbno " + ndbno + ".";
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String decodeBarcode(String fileName) {
        File file = new File(fileName);
        BufferedImage image = null;
        BinaryBitmap bitmap = null;
        Result result = null;

        try {
            image = ImageIO.read(file);
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
            bitmap = new BinaryBitmap(new HybridBinarizer(source));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null)
            return null;

        UPCAReader reader = new UPCAReader();
        try {
            result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException | FormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getFoodByBarcode(String arg, boolean isPathToImg) {
        String barcode;

        System.out.println("arg " + arg);

        barcode = (isPathToImg) ? decodeBarcode(arg) : arg;

        System.out.println("barcode " + barcode);

        if(barcode != null) {
            if (foodByUpcCache.containsKey(barcode)) {
                return foodByUpcCache.get(barcode).toString();
            }
        } else {
            return "Invalid upc or image path.";
        }

        return "No product with this barcode=" + barcode + " has been found.";
    }
}
