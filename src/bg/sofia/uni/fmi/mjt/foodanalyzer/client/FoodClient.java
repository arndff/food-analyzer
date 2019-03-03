package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class FoodClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int SERVER_PORT = 4444;
    private static final String QUIT_MESSAGE = "quit";

    public static void start() {
        try (Socket socket = new Socket(DEFAULT_HOST, SERVER_PORT);
             PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if (QUIT_MESSAGE.equals(message)) {
                    System.out.println("Closing your session...");
                    break;
                }

                System.out.println("Sending request <" + message + "> to the server...");
                printWriter.println(message);

                System.out.println("The server replied:");
                String reply = bufferedReader.readLine();
                String[] objects = reply.split(";");
                Arrays.stream(objects).forEach(System.out::println);
            }
        } catch (IOException e) {
            System.out.println("A problem occurred. Closing your session...");
        }
    }

    public static void main(String[] args) {
        FoodClient.start();
    }
}
