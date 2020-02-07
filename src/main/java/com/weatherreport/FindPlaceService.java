package com.weatherreport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

final class FindPlaceService {

    /**
     * @param place The city name you want to locate
     * @throws IOException The url is invalid.
     */
    String getGeoLocation(String place) throws InterruptedException, IOException {
        HttpClient client = HttpClientBuilder.create().build();
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?key=%s&input=%s&inputtype=textquery&fields=geometry/location",
                System.getenv("GEO_API_KEY"),
                place
        );

        System.out.println("attempting to send request to " + url);
        HttpGet request = new HttpGet(url);
        JSONObject location = new JSONObject();

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(jsonString);
            int status = response.getStatusLine().getStatusCode();
            System.out.println("Response" + status);

            if (status == 200) {
                JSONArray candidates = jsonObject.getJSONArray("candidates");
                location = candidates.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");

                return String.format(
                        "%s found at lat: %8.5f lng: %8.5f",
                        place,
                        location.getDouble("lat"),
                        location.getDouble("lng")
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "I'm sorry, I was unable to locate " + place;
    }
}
