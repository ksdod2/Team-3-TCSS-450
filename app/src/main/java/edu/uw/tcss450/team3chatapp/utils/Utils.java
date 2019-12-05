package edu.uw.tcss450.team3chatapp.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;

public class Utils {

    public static final String OBS_FIELDS = "place.name,place.state,ob.tempC,ob.tempF,ob.humidity,ob.windSpeedKPH,ob.windSpeedMPH,ob.weather,ob.weatherShort,ob.cloudsCoded,ob.icon";
    private static final String DAILY_FIELDS = "periods.timestamp,periods.minTempC,periods.minTempF,periods.maxTempC,periods.maxTempF,periods.pop,periods.sunrise,periods.sunset,periods.icon";
    private static final String HOURLY_FIELDS = "periods.timestamp,periods.avgTempC,periods.avgTempF,periods.icon";

    private Utils() {}

    public static void updateWeatherIfNecessary(WeatherProfileViewModel theWeatherVM) {
        ArrayList<LatLng> locationsToUpdate = new ArrayList<>();
        // Redundant code in branches because method will be called a lot without hitting them.
        if(theWeatherVM.getCurrentLocationWeatherProfile().getValue() == null) {
            // Add current location to list of locations to update
            Location curLoc = Objects.requireNonNull(LocationViewModel
                    .getFactory()
                    .create(LocationViewModel.class)
                    .getCurrentLocation()
                    .getValue());
            locationsToUpdate.add(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()));

            theWeatherVM.update(locationsToUpdate);

        } else if(theWeatherVM.getTimeStamp() < Utils.getTopOfLastHour()) {
            // Add current location to list of locations to update first:
            Location curLoc = Objects.requireNonNull(LocationViewModel
                    .getFactory()
                    .create(LocationViewModel.class)
                    .getCurrentLocation()
                    .getValue());
            locationsToUpdate.add(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()));

            // Add saved locations from VM to list of locations to update
            if(theWeatherVM.getSavedLocationWeatherProfiles().getValue() != null) {
                for(WeatherProfile wp : theWeatherVM.getSavedLocationWeatherProfiles().getValue()) {
                    locationsToUpdate.add(wp.getLocation());
                }
            }
        }
    }

    public static String buildWeatherProfileQuery(ArrayList<LatLng> tLocations) {
        StringBuilder result = new StringBuilder();

        String qm = "%3F";
        String amp = "%26";

        for (LatLng loc : tLocations) {
            String locEP = loc.latitude + "," + loc.longitude;

            //append query string for this location to entire request.
            String req = "/observations/" + locEP + qm +
                    "fields=" + Utils.OBS_FIELDS +
                    "," +
                    //append 10-day forecast request: '/forecasts/{locEP}?limit=10&fields={dailyFields},'
                    "/forecasts/" + locEP + qm +
                    "limit=10" + amp +
                    "fields=" + Utils.DAILY_FIELDS +
                    "," +
                    //append 24hr forecast request: '/forecasts/{locEP}?filter=1hr&limit=24&fields={hourlyFields},'
                    "/forecasts/" + locEP + qm +
                    "filter=1hr" + amp +
                    "limit=24" + amp +
                    "fields=" + Utils.HOURLY_FIELDS +
                    ",";
            result.append(req);
        }
        //remove trailing comma:
        result.deleteCharAt(result.length() - 1);

        return result.toString();
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

    public static String cloudDecode(final String theCode) {
        String response;
        switch(theCode) {
            case "CL":
                response = "Clear Outside"; break;
            case "FW":
                response = "Mostly Clear"; break;
            case "SC":
                response = "Partly Cloudy"; break;
            case "BK":
                response = "Mostly Cloudy"; break;
            case "OV":
                response = "Cloudy Outside"; break;
            default:
                response = "Unsure. Try again later."; break;
        }
        return response;
    }

    private static long getTopOfLastHour() {
        return (System.currentTimeMillis() / 1000L) - ((System.currentTimeMillis() / 1000L) % 3600);
    }
}

