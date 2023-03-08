
/*
Stanley Hsieh
2/7/2022
Assignment 1
pulls data from commity transit webpage using regex and calculates length of specified bus route.
*/
import java.util.*;
import java.lang.Exception;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoUnit.MINUTES;

public class RouteFinder implements IRouteFinder {

    // loops through the html file looking for destinations that start with
    // destInitial, and returns a hashmap that contains destination, bus number, and
    // url.
    public Map<String, Map<String, String>> getBusRoutesUrls(final char destInitial) {
        Map<String, Map<String, String>> LocationMap = new HashMap<>();
        try {
            URLConnection transit = new URL("https://www.communitytransit.org/busservice/schedules/").openConnection();
            transit.setRequestProperty("user-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            BufferedReader in = new BufferedReader(new InputStreamReader(transit.getInputStream()));
            String inputLine = "";
            String text = "";
            while ((inputLine = in.readLine()) != null) {
                text += inputLine + "\n";
            }
            in.close();

            Pattern pattern = Pattern.compile("<h3>(" + destInitial + ".*)</h3>"); // regex looking for destinations
            Pattern pattern2 = Pattern.compile("(<strong><a.*?\"(.*)\"(.*)?>(.*)</a></strong>)|(<hr id.*>)"); // regex
                                                                                                              // looking
                                                                                                              // for
                                                                                                              // both
                                                                                                              // url and
                                                                                                              // bus
                                                                                                              // number.
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) { // loops through html text for destinations starting with destInitial.
                Map<String, String> RouteAndUrlMap = new HashMap<>();
                System.out.println("Destination: " + matcher.group(1));
                Matcher matcher2 = pattern2.matcher(text.substring(matcher.end()));
                while (matcher2.find()) { // loops through html text looking for bus number and corresponding url
                    if (matcher2.group().charAt(1) == 'h') {
                        break;
                    }
                    String url = "https://www.communitytransit.org/busservice" + matcher2.group(2);
                    RouteAndUrlMap.put(matcher2.group(4), url);
                    System.out.println("Bus Number: " + matcher2.group(4));
                }
                LocationMap.put(matcher.group(1), RouteAndUrlMap);
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        } catch (Exception e) {
            System.out.println("Invalid URL");
        }

        return LocationMap;
    }

    // loops through hashmap collecting bus times for each route per destination to
    // calculate the length of each trip, returning a hashmap with the route number
    // destination and time.
    public Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(
            final Map<String, String> destinationBusesMap) {
        Map<String, List<Long>> BusRouteLengths = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>(destinationBusesMap.keySet());
        try {
            for (int i = 0; i < keys.size(); i++) {
                URLConnection transit = new URL(destinationBusesMap.get(keys.get(i))).openConnection();
                transit.setRequestProperty("user-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                BufferedReader in = new BufferedReader(new InputStreamReader(transit.getInputStream()));
                String inputLine = "";
                String text = "";
                while ((inputLine = in.readLine()) != null) {
                    text += inputLine + "\n";
                }
                in.close();

                String startTime = "";
                String time = "";
                Boolean firstNumber = true;
                Pattern pattern = Pattern.compile("<small>(.*)</small>(\\s)*</h2>(\\s)*</td>"); // regex looking for
                                                                                                // destination.
                Pattern pattern2 = Pattern
                        .compile("(</tbody>)|(<td .*>(\\s)*(.*(AM|PM)))|</td>\\s.*(\\s.*|.*)</tr>"); // regex looking
                                                                                                     // for bus times.
                Matcher matcher = pattern.matcher(text);
                int count2 = 0;
                int listNumber = 0;
                while (matcher.find()) { // loops through each destination
                    if (count2 == 2) {
                        break;
                    }
                    Matcher matcher2 = pattern2.matcher(text.substring(matcher.end()));
                    List<Long> TripLength = new ArrayList();
                    while (matcher2.find()) { // loops through the table of bus times calculating length and putting it
                                              // into the hashmap.
                        if (matcher2.group().equals("</tbody>")) {
                            startTime = "";
                            time = "";
                            firstNumber = true;
                            BusRouteLengths.put(keys.get(i) + " - " + matcher.group(1).substring(3), TripLength);
                            break;
                        }

                        if (matcher2.group(4) == null) {
                            TripLength.add(timeCalculation(startTime, time));
                            firstNumber = true;
                            continue;
                        }
                        if (firstNumber) {
                            startTime = matcher2.group(4);
                            firstNumber = false;
                        } else {
                            time = matcher2.group(4);
                        }

                    }
                    listNumber++;
                    count2++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error has occured");
        }
        return BusRouteLengths;
    }

    // calculates the difference between 2 times and returns diffrence in minutes.
    public static Long timeCalculation(String startTime, String endTime) {
        // if statements used to format the times, to fit localtime format.
        if (startTime.length() != 8) {
            startTime = "0" + startTime;
        }
        if (endTime.length() != 8) {
            endTime = "0" + endTime;
        }
        if (startTime.substring(6, 8).equals("PM")) {
            int x = Integer.parseInt(startTime.substring(0, 2));
            if (x != 12) {
                x = x + 12;
            }
            startTime = String.valueOf(x) + startTime.substring(2, 5);

        } else {
            startTime = startTime.substring(0, 5);
        }

        if (endTime.substring(6, 8).equals("PM")) {
            int x = Integer.parseInt(endTime.substring(0, 2));
            if (x != 12) {
                x = x + 12;
            }
            endTime = String.valueOf(x) + endTime.substring(2, 5);

        } else {
            endTime = endTime.substring(0, 5);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime sTime = LocalTime.parse(startTime, dtf);
        LocalTime eTime = LocalTime.parse(endTime, dtf);
        Long difference = sTime.until(eTime, MINUTES);
        return difference;
    }

}
