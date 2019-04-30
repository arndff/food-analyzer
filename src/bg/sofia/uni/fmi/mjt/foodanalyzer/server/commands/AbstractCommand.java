package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServer;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidBarcodeArgumentsException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;

import java.net.http.HttpClient;
import java.util.logging.Logger;

public abstract class AbstractCommand {
    static final String API_URL = "https://api.nal.usda.gov/ndb";
    static final String API_KEY = "yqVQElHgqao3jzD9KbtKeygI2UqpOf41XYbNpcd9";

    static final Logger foodServerLogger = FoodServer.getFoodServerLogger();

    HttpClient client;

    AbstractCommand(HttpClient client) {
        this.client = client;
    }

    public abstract String execute(String argument) throws InvalidBarcodeArgumentsException, NoInformationFoundException;
}
