package com.weatherreport;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.*;

final class Location {
    /**
     * @param place The city name you want to locate
     * @throws IOException The url is invalid.
     * @return LatLng the found position of the given place.
     */
    LatLng getGeoLocation(GeoApiContext context, String place) throws IOException, InterruptedException, ApiException {

        GeocodingResult[] results = GeocodingApi.geocode(context,
                place).await();
        return results[0].geometry.location;
    }
}
