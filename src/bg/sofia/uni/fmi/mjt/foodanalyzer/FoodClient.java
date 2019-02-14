package bg.sofia.uni.fmi.mjt.foodanalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class FoodClient {
    private static final int SERVER_PORT = 4444;

    public static void main(String[] args) {
        try(Socket s = new Socket("localhost", SERVER_PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            Scanner sc = new Scanner(System.in)) {

            while(true) {
                // 1) user should be required to enter their API key first,
                //    so ClientRequestHandler to be initialized with personal API key
                // 2) enter N queries

                String message = sc.nextLine(); // read a line from the console

                if(message.equals("quit")) {
                    System.out.println("Closing your session...");
                    break;
                }

                System.out.println("Sending request <" + message + "> to the server...");
                pw.println(message);

                System.out.println("The server replied:");
                String reply = br.readLine();
                String[] objects = reply.split(";");
                Arrays.stream(objects).forEach(System.out::println);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
