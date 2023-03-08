
/*
Stanley Hsieh
2/7/2022
Assignment 1
client class for bus route length calculation.
*/
import java.util.*;
import java.lang.*;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean repeat = true;
        while (repeat) {
            System.out.print("Please enter a letter that your destination starts with: ");
            char letter = scanner.next().charAt(0);
            RouteFinder x = new RouteFinder();
            Map<String, Map<String, String>> LocationMap = new HashMap(
                    x.getBusRoutesUrls(Character.toUpperCase(letter)));
            System.out.print("Please enter your destination: ");
            String destination = scanner.next();
            String finalDestination = destination.substring(0, 1).toUpperCase() + destination.substring(1);
            System.out.println();
            System.out.println("\uD83D\uDE0D" + " Bus Trips Lengths in Minutes are:");
            System.out.println(
                    x.getBusRouteTripsLengthsInMinutesToAndFromDestination(LocationMap.get(finalDestination)) + "\n");
            System.out.println(
                    "Do you want to check different destination? Please type Y to continue or press any other key to exit: ");
            String answer = scanner.next();
            if (!answer.equals("Y")) {
                repeat = false;
            }
        }

    }
}
