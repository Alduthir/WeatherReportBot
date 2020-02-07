package com.weatherreport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class GetWeatherReportService {
    /**
     * Default method using Kelvin for temperature units
     *
     * @param place the placename to search for weatherinfo
     * @return Response message for the bot
     */
    String getWeatherReport(String place) {
        HttpClient client = HttpClientBuilder.create().build();
        String url = String.format(
                "http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s",
                place,
                System.getenv("WEATHER_API_KEY")
        );

        return handleWeatherRequest(place, client, url) + "K";
    }

    /**
     *
     * @param place the placename to search for weatherinfo
     * @param units The format for temperature units. Accepts metric or imperial
     * @return Response message for the bot
     */
    String getWeatherReport(String place, String units) {
        if (units.equals("metric") || units.equals("imperial")) {
            HttpClient client = HttpClientBuilder.create().build();
            String url = String.format(
                    "http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s&units=%s",
                    place,
                    System.getenv("WEATHER_API_KEY"),
                    units
            );

            String message = handleWeatherRequest(place, client, url);
            if (units.equals("imperial")) {
                return message + "F";
            }
            return message + "C";
        }
        return String.format("Unable to process request, %s is not a valid unit measurement.", units);
    }

    /**
     *
     * @param place place
     * @param client the http client
     * @param url the requested url
     * @return The response message
     */
    String handleWeatherRequest(String place, HttpClient client, String url) {
        System.out.println("attempting to send request to " + url);
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(jsonString);

            int status = response.getStatusLine().getStatusCode();
            System.out.println("Response" + status);
            if (status == 200) {
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                Double temperature = jsonObject.getJSONObject("main").getDouble("temp");

                return String.format(
                        "The weather in %s is currently %s with a temperature of %.2f",
                        place,
                        weatherArray.getJSONObject(0).getString("main"),
                        temperature
                );

            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
