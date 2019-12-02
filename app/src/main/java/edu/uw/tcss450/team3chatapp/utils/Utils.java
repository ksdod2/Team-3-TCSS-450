package edu.uw.tcss450.team3chatapp.utils;

import android.content.SharedPreferences;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;

public class Utils {

    private Utils() {}

    public static void updateWeatherIfNecessary(SharedPreferences prefs) {
        WeatherProfileViewModel weatherModel = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);

        if(weatherModel.getCurrentLocationWeatherProfile().getValue() == null || weatherModel.getTimeStamp() < Utils.getTopOfLastHour()) {
            ArrayList<Location> savedLocations;
            Location curLoc = Objects.requireNonNull(LocationViewModel
                                                    .getFactory()
                                                    .create(LocationViewModel.class)
                                                    .getCurrentLocation()
                                                    .getValue());

            String locKey = "savedLocations";
            Type typeOfLocList = new TypeToken<List<Location>>() {}.getType();
            Gson gson = new Gson();

            if (prefs.contains(locKey)) {

                String listAsJSON = prefs.getString(locKey, "");
                savedLocations = gson.fromJson(listAsJSON, typeOfLocList);

                savedLocations.add(0, curLoc);
            } else {
                savedLocations = new ArrayList<>();
                savedLocations.add(curLoc);
            }
            weatherModel.update(savedLocations);
        }
    }

    public static String formatCityState(String name, String state) {

        StringBuilder city = new StringBuilder();

        String[] split;
        if(name.contains(" ")) {
            split = name.split(" ");
            for(String s : split) {
                city.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
            }
        } else {
            city.append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append(" ");
        }
        city.trimToSize();

        return city.append(", ").append(state).toString();
    }

    private static long getTopOfLastHour() {
        return (System.currentTimeMillis() / 1000L) - ((System.currentTimeMillis() / 1000L) % 3600);
    }
}

