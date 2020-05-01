package edu.uw.tcss450.team3chatapp.utils;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;

/** Collection of static utility methods used in multiple places throughout the app. */
public class Utils {

    /** Sorting params for weather API requests. (basically "limit response to these fields".) */
    public static final String OBS_FIELDS = "place.name,place.state,ob.tempC,ob.tempF,ob.humidity,ob.windSpeedKPH,ob.windSpeedMPH,ob.weather,ob.weatherShort,ob.cloudsCoded,ob.icon";
    private static final String DAILY_FIELDS = "periods.timestamp,periods.weatherPrimary,periods.minTempC,periods.minTempF,periods.maxTempC,periods.maxTempF,periods.pop,periods.sunrise,periods.sunset,periods.icon";
    private static final String HOURLY_FIELDS = "periods.timestamp,periods.avgTempC,periods.avgTempF,periods.icon";

    /** Shouldn't be able to instantiate. */
    private Utils() {}

    /**
     * Checks if weather information in WeatherProfileViewModel needs to be updated so that requests to
     * the API aren't made every time a fragment with weather info is loaded. Weather info is considered outdated if:
     *   - There is no weather info (duh).
     *   - the timestamp is before the top of the last hour (e.g. current time is 5:10 but timestamp is before 5:00)
     * @param theWeatherVM most recent weather information.
     */
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
            Location curLoc = LocationViewModel
                    .getFactory()
                    .create(LocationViewModel.class)
                    .getCurrentLocation()
                    .getValue();
            if(curLoc != null) {locationsToUpdate.add(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()));}

            // Add saved locations from VM to list of locations to update
            if(theWeatherVM.getSavedLocationWeatherProfiles().getValue() != null) {
                for(WeatherProfile wp : theWeatherVM.getSavedLocationWeatherProfiles().getValue()) {
                    locationsToUpdate.add(wp.getLocation());
                }
            }
        }
    }

    /**
     * Helper method to build the monster batch request param string for API GET request.
     * @param tLocations    locations to get weather for, current and or saved.
     * @return              query string formatted to get current, 10 day & 24 hour weather information for each location.
     */
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

    /**
     * Formats the city and state strings into one in the form "{City}, {State}"
     * @param tCity  the city
     * @param tState the state
     * @return      the formatted, concatenated string.
     */
    public static String formatCityState(String tCity, String tState) {

        StringBuilder formattedCity = new StringBuilder();

        String[] split;
        if(tCity.contains(" ")) {
            split = tCity.split(" ");
            for(String s : split) {
                formattedCity.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
            }
        } else {
            formattedCity.append(tCity.substring(0, 1).toUpperCase()).append(tCity.substring(1)).append(" ");
        }
        formattedCity.trimToSize();

        String formattedState = tState;
        if(tState.length() > 2) {
            formattedState = getStateAbbr(tState);
        }

        return formattedCity.append(", ").append(formattedState).toString();
    }

    /**
     * Provides custom weather info based on cloud code returned by API.
     * @param theCode   cloud code.
     * @return          custom weather message.
     */
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

    /** @return the current time, rounded down to the last hour. */
    private static long getTopOfLastHour() {
        return (System.currentTimeMillis() / 1000L) - ((System.currentTimeMillis() / 1000L) % 3600);
    }

    /**
     * Manually hides soft keyboard.
     * @param activity context.
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDisplayTemp(Double tNumber, String tUnit) {
        DecimalFormat df = new DecimalFormat("###");
        double result = tNumber - 273.15;
        if("f".equals(tUnit.toLowerCase())) {
            result = result * 9 / 5 + 32;
        }
        return df.format(result);
    }

    public static String getStateAbbr(String fullName) {
        Map<String, String> states = new HashMap<String, String>();
        states.put("Alabama","AL");
        states.put("Alaska","AK");
        states.put("Alberta","AB");
        states.put("American Samoa","AS");
        states.put("Arizona","AZ");
        states.put("Arkansas","AR");
        states.put("Armed Forces (AE)","AE");
        states.put("Armed Forces Americas","AA");
        states.put("Armed Forces Pacific","AP");
        states.put("British Columbia","BC");
        states.put("California","CA");
        states.put("Colorado","CO");
        states.put("Connecticut","CT");
        states.put("Delaware","DE");
        states.put("District Of Columbia","DC");
        states.put("Florida","FL");
        states.put("Georgia","GA");
        states.put("Guam","GU");
        states.put("Hawaii","HI");
        states.put("Idaho","ID");
        states.put("Illinois","IL");
        states.put("Indiana","IN");
        states.put("Iowa","IA");
        states.put("Kansas","KS");
        states.put("Kentucky","KY");
        states.put("Louisiana","LA");
        states.put("Maine","ME");
        states.put("Manitoba","MB");
        states.put("Maryland","MD");
        states.put("Massachusetts","MA");
        states.put("Michigan","MI");
        states.put("Minnesota","MN");
        states.put("Mississippi","MS");
        states.put("Missouri","MO");
        states.put("Montana","MT");
        states.put("Nebraska","NE");
        states.put("Nevada","NV");
        states.put("New Brunswick","NB");
        states.put("New Hampshire","NH");
        states.put("New Jersey","NJ");
        states.put("New Mexico","NM");
        states.put("New York","NY");
        states.put("Newfoundland","NF");
        states.put("North Carolina","NC");
        states.put("North Dakota","ND");
        states.put("Northwest Territories","NT");
        states.put("Nova Scotia","NS");
        states.put("Nunavut","NU");
        states.put("Ohio","OH");
        states.put("Oklahoma","OK");
        states.put("Ontario","ON");
        states.put("Oregon","OR");
        states.put("Pennsylvania","PA");
        states.put("Prince Edward Island","PE");
        states.put("Puerto Rico","PR");
        states.put("Quebec","QC");
        states.put("Rhode Island","RI");
        states.put("Saskatchewan","SK");
        states.put("South Carolina","SC");
        states.put("South Dakota","SD");
        states.put("Tennessee","TN");
        states.put("Texas","TX");
        states.put("Utah","UT");
        states.put("Vermont","VT");
        states.put("Virgin Islands","VI");
        states.put("Virginia","VA");
        states.put("Washington","WA");
        states.put("West Virginia","WV");
        states.put("Wisconsin","WI");
        states.put("Wyoming","WY");
        states.put("Yukon Territory","YT");

        return states.get(fullName);
    }
}