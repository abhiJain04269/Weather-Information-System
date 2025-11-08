package MyPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/weather")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String myApiKey = "use Api key";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String city = request.getParameter("city");

        try {
            // Build API URL
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + myApiKey + "&units=metric";
            System.out.println("Calling API: " + apiUrl); // Log API URL

            // Call API and get JSON response
            JsonObject jsonObject = getJsonResponse(apiUrl);

            // Check if city is valid
            if (jsonObject.has("cod") && jsonObject.get("cod").getAsInt() != 200) {
                request.setAttribute("error", "City not found! Please enter a valid city.");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            // Extract weather data
            double tempC = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
            int cloudCover = jsonObject.getAsJsonObject("clouds").get("all").getAsInt();
            int visibility = jsonObject.has("visibility") ? jsonObject.get("visibility").getAsInt() / 1000 : 0;
            String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
            String weatherDescription = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();

            // Date & Time
            long dt = jsonObject.get("dt").getAsLong() * 1000;
            SimpleDateFormat sdfDate = new SimpleDateFormat("EEE MMM dd yyyy");
            String date = sdfDate.format(new Date(dt));
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            String time = sdfTime.format(new Date(dt));

            // Log the API response for verification
            System.out.println("API Response: " + jsonObject.toString());

            // Set attributes for JSP
            request.setAttribute("city", city);
            request.setAttribute("temperature", String.format("%.2f", tempC));
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("cloudCover", cloudCover);
            request.setAttribute("visibility", visibility);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("weatherDescription", weatherDescription);
            request.setAttribute("date", date);
            request.setAttribute("currentTime", time);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Unable to fetch weather data. Please try again.");
        }

        // Forward to JSP
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    // Utility method to make GET request and return JSON
    private JsonObject getJsonResponse(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inp = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inp);
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(reader);
        while (scanner.hasNext()) {
            sb.append(scanner.nextLine());
        }
        scanner.close();
        connection.disconnect();

        Gson gson = new Gson();
        return gson.fromJson(sb.toString(), JsonObject.class);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
