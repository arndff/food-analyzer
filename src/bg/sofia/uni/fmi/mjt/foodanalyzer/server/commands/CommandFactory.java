package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServerCache;

import java.net.http.HttpClient;

public class CommandFactory {
    private static final String GET_FOOD = "get-food";
    private static final String GET_FOOD_REPORT = "get-food-report";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";

    private static final CommandFactory commandFactory = new CommandFactory();

    private CommandFactory() {}

    public static CommandFactory getInstance() {
        return commandFactory;
    }

    public AbstractCommand getCommand(HttpClient client, String commandType, FoodServerCache cache) {
        switch(commandType) {
            case GET_FOOD:
                return new FoodCommand(client, cache.getFoodByNameCache(), cache.getFoodByUpcCache());
            case GET_FOOD_REPORT:
                return new FoodReportCommand(client, cache.getFoodByNdbnoCache());
            case GET_FOOD_BY_BARCODE:
                return new FoodByBarcodeCommand(client, cache.getFoodByUpcCache());
            default:
                return null;
        }
    }
}
