package edu.uw.tcss450.team3chatapp.utils;

import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;

public class Utils {

    private static final int MIN_DIST_BETWEEN_LOCATIONS = 2000;

    private Utils() {}

    public static long getTopOfLastHour() {
        return (System.currentTimeMillis() / 1000L) - ((System.currentTimeMillis() / 1000L) % 3600);
    }

    public static boolean areCloseTogether(final Location loc1, final Location loc2) {
        return loc1.distanceTo(loc2) < MIN_DIST_BETWEEN_LOCATIONS;
    }

    public static void updateWeatherIfNecessary(SharedPreferences prefs) {
        WeatherProfileViewModel model = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);
        List<WeatherProfile> test = model.getAllWeatherProfiles().getValue();
        if(model.getAllWeatherProfiles().getValue() == null || model.getTimeStamp() < Utils.getTopOfLastHour()) {
            ArrayList<Location> savedLocations;

            Location curLoc = Objects.requireNonNull(LocationViewModel.getFactory().create(LocationViewModel.class).getCurrentLocation().getValue());
            String locKey = "savedLocations";
            Type typeOfLocList = new TypeToken<List<Location>>() {}.getType();
            Gson gson = new Gson();

            if (prefs.contains(locKey)) {

                String listAsJSON = prefs.getString(locKey, "");
                savedLocations = gson.fromJson(listAsJSON, typeOfLocList);

                //Add current location to saved locations if current location is far enough away from any other saved locations.
                boolean containsCurLoc = false;
                for (Location savedLoc : savedLocations) {
                    if (Utils.areCloseTogether(savedLoc, curLoc)) {
                        containsCurLoc = true;
                        break;
                    }
                }
                if (!containsCurLoc) {
                    savedLocations.add(curLoc);
                    prefs.edit().putString(locKey, gson.toJson(savedLocations, typeOfLocList)).apply();
                }
            } else {
                savedLocations = new ArrayList<>();
                savedLocations.add(curLoc);
                prefs.edit().putString(locKey, gson.toJson(savedLocations, typeOfLocList)).apply();
            }
            model.update(savedLocations);
        }
    }
}

