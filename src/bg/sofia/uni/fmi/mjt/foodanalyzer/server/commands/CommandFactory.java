package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class CommandFactory {

    private static final String GET_FOOD = "get-food";
    private static final String GET_FOOD_REPORT = "get-food-report";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";

    private static final CommandFactory commandFactory = new CommandFactory();

    private CommandFactory() {}

    public static CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public Command getCommand(String commandType,
                              ConcurrentMap<String, List<Product>> foodByNameCache,
                              ConcurrentMap<String, Report> foodByNdbnoCache,
                              ConcurrentMap<String, Product> foodByUpcCache) {
        switch(commandType) {
            case GET_FOOD:
                return new GetFood(foodByNameCache, foodByUpcCache);
            case GET_FOOD_REPORT:
                return new GetFoodReport(foodByNdbnoCache);
            case GET_FOOD_BY_BARCODE:
                return new GetFoodByBarcode(foodByUpcCache);
            default:
                return null;
        }
    }
}
