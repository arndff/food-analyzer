package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands.Command;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import static bg.sofia.uni.fmi.mjt.foodanalyzer.server.FoodServer.logger;

public class ClientRequestHandler implements Runnable {

    private Socket socket;

    private final ConcurrentMap<String, List<Product>> foodByNameCache;
    private final ConcurrentMap<String, Report> foodByNdbnoCache;
    private final ConcurrentMap<String, Product> foodByUpcCache;

    public ClientRequestHandler(Socket socket,
                                ConcurrentMap<String, List<Product>> foodByNameCache,
                                ConcurrentMap<String, Report> foodByNdbnoCache,
                                ConcurrentMap<String, Product> foodByUpcCache) {
        this.socket = socket;

        this.foodByNameCache = foodByNameCache;
        this.foodByNdbnoCache = foodByNdbnoCache;
        this.foodByUpcCache = foodByUpcCache;
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
                    String result = executeQueryByItsType(userInput);
                    out.println(result); // send result to the client
                } else {
                    out.println("You should pass exactly two arguments: a query and its argument.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "A problem occurred in ClientRequestHandler::run.", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "A problem occurred while trying to close the socket in ClientRequestHandler::run", e);
            }
        }
    }

    private String executeQueryByItsType(String[] userInput) {
        String queryType = userInput[0];
        String queryArg = userInput[1];

        CommandFactory commandFactory = CommandFactory.getCommandFactory();
        Command cmd = commandFactory.getCommand(queryType, foodByNameCache, foodByNdbnoCache, foodByUpcCache);

        if (cmd != null) {
            String result = cmd.execute(queryArg);

            if (result != null) {
                return result;
            } else {
                return "Your query wasn't executed successfully.";
            }
        } else {
            return "Invalid query type.";
        }
    }
}
