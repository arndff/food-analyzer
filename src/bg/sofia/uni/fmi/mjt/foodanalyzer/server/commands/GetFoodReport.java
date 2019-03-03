package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServer;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetFoodReport extends Command {

    public GetFoodReport(ConcurrentMap<String, Report> foodByNdbnoCache) {
        super(null, foodByNdbnoCache, null);
    }

    // helper method which is used in createReportObject to set some of Report's object data members
    private double extractSpecificNutrient(JsonArray nutrients, int index) {
        return nutrients.get(index).getAsJsonObject()
                        .get("value").getAsDouble();
    }

    private Report createReportObject(JsonObject food) {
        String name = food.get("desc").getAsJsonObject()
                          .get("name").getAsString()
                          .split(", U")[0];

        String ingredients = food.get("ing").getAsJsonObject()
                                 .get("desc").getAsString();

        JsonArray nutrients = food.get("nutrients").getAsJsonArray();
        double kcal = extractSpecificNutrient(nutrients, 0);
        double protein = extractSpecificNutrient(nutrients, 1);
        double fat = extractSpecificNutrient(nutrients, 2);
        double carbohydrate = extractSpecificNutrient(nutrients, 3);
        double fiber = extractSpecificNutrient(nutrients, 4);

        return new Report(name, ingredients, kcal, protein, fat, carbohydrate, fiber);
    }

    @Override
    public String execute(String argument) {
        if (foodByNdbnoCache.containsKey(argument)) {
            return foodByNdbnoCache.get(argument).toString();
        }

        String url = API_URL + "/V2/reports?ndbno=" + argument + "&format=json&api_key=" + API_KEY;

        try {
            JsonObject response = urlResponseToJson(url);

            JsonObject food = response.getAsJsonArray("foods")
                                      .get(0).getAsJsonObject()
                                      .get("food").getAsJsonObject();

            Report report = createReportObject(food);

            // Updating foodByNdbnoCache
            foodByNdbnoCache.put(argument, report);

            return report.toString();
        } catch (NullPointerException e) {
            return "No information found for ndbno " + argument + ".";
        } catch (IOException | InterruptedException e) {
            Logger foodServerLogger = FoodServer.getFoodServerLogger();
            foodServerLogger.log(Level.WARNING, "Exception caught in GetFoodReport::execute.", e);
        }

        return null;
    }
}
