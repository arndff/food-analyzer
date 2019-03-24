package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class GetFoodReportCommand extends Command {

    public GetFoodReportCommand(ConcurrentMap<String, Report> foodByNdbnoCache) {
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
    public String execute(String argument) throws NoInformationFoundException {
        if (foodByNdbnoCache.containsKey(argument)) {
            return foodByNdbnoCache.get(argument).toString();
        }

        String url = API_URL + "/V2/reports?ndbno=" + argument + "&format=json&api_key=" + API_KEY;

        try {
            JsonObject response = urlResponseToJson(url);

            JsonElement food = response.getAsJsonArray("foods")
                                       .get(0).getAsJsonObject()
                                       .get("food");

            if (food == null) {
                throw new NoInformationFoundException("No information found for ndbno " + argument + ".");
            }

            Report report = createReportObject(food.getAsJsonObject());

            // Updating foodByNdbnoCache
            foodByNdbnoCache.put(argument, report);

            return report.toString();
        } catch (IOException | InterruptedException e) {
            foodServerLogger.log(Level.WARNING, "Exception caught in GetFoodReportCommand::execute.", e);
        }

        return null;
    }
}
