package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FoodClient {
    private static final int SERVER_PORT = 4444;
    private static final String DEFAULT_HOST = "localhost";
    private static final String QUIT_MESSAGE = "quit";

    private static final Logger logger = Logger.getLogger(FoodClient.class.getName());

    private void start() {
        try (Socket socket = new Socket(DEFAULT_HOST, SERVER_PORT);
             PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if (QUIT_MESSAGE.equals(message)) {
                    logger.log(Level.INFO, "Closing your session...");
                    break;
                }

                logger.log(Level.INFO, "Sending request <{0}> to the server...", message);
                printWriter.println(message);

                logger.log(Level.INFO, "The server replied:");
                String reply = bufferedReader.readLine();
                String[] objects = reply.split(";");
                Arrays.stream(objects)
                      .forEach(object -> logger.log(Level.INFO, object));
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "A problem occurred. Closing your session...");
        }
    }

    public static void main(String[] args) {
        FoodClient foodClient = new FoodClient();
        foodClient.start();
    }
}
