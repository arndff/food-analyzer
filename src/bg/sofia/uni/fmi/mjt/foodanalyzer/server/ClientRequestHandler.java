package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands.AbstractCommand;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidBarcodeArgumentsException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidQueryTypeException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.UnsuccessfulQueryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.http.HttpClient;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientRequestHandler implements Runnable {
    private Socket socket;
    private final FoodServerCache cache;

    private static final Logger foodServerLogger = FoodServer.getFoodServerLogger();

    ClientRequestHandler(Socket socket, FoodServerCache cache) {
        this.socket = socket;
        this.cache = cache;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String inputLine;

            while ((inputLine = in.readLine()) != null) { // read the message from the client
                String[] userInput = inputLine.split("\\s+");
                int userInputLength = userInput.length;

                if (userInputLength == 2) {
                    String result;

                    try {
                        result = executeQueryByItsType(userInput);
                    } catch (Exception e) {
                        foodServerLogger.log(Level.INFO, e.getMessage());
                        result = e.getMessage();
                    }

                    out.println(result); // send result to the client
                } else {
                    out.println("You should pass exactly two arguments: a query and its argument.");
                }
            }
        } catch (IOException e) {
            foodServerLogger.log(Level.SEVERE, "A problem occurred in ClientRequestHandler::run.", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                foodServerLogger.log(Level.SEVERE,
                        "A problem occurred while trying to close the socket in ClientRequestHandler::run", e);
            }
        }
    }

    private String executeQueryByItsType(String[] userInput)
            throws InvalidQueryTypeException, UnsuccessfulQueryException,
                   InvalidBarcodeArgumentsException, NoInformationFoundException {
        String queryType = userInput[0];
        String queryArg = userInput[1];

        HttpClient client = HttpClient.newHttpClient();

        CommandFactory commandFactory = CommandFactory.getInstance();
        AbstractCommand cmd = commandFactory.getCommand(client, queryType, cache);

        if (cmd != null) {
            try {
                String result = cmd.execute(queryArg);

                if (result != null) {
                    return result;
                } else {
                    throw new UnsuccessfulQueryException();
                }
            } catch (InvalidBarcodeArgumentsException | NoInformationFoundException e) {
                throw e;
            }
        } else {
            throw new InvalidQueryTypeException();
        }
    }
}
