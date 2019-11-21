package edu.uw.tcss450.team3chatapp.model;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.Utils;

public class WeatherProfileViewModel extends ViewModel {

    private static WeatherProfileViewModel mInstance;

    private ArrayList<WeatherProfile> mWeatherProfileList;
    private MutableLiveData<List<WeatherProfile>> mLiveWeatherProfiles;
    private ArrayList<Location> mSavedLocations;
    private long mLastUpdated;

    private WeatherProfileViewModel() {
        mLiveWeatherProfiles = new MutableLiveData<>();
        mLastUpdated = System.currentTimeMillis() / 1000L;
    }

    public long getTimeStamp() {return mLastUpdated;}

    public void update(ArrayList<Location> savedLocations) {
        if(savedLocations.size() > 0) {
            mSavedLocations = savedLocations;

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority("team3-chatapp-backend.herokuapp.com")
                    .appendPath("weather")
                    .appendQueryParameter("requests", buildWeatherQuery(savedLocations))
                    .build();

            //TODO remove
            Log.d("WEATHER_URI", uri.toString());

            new GetAsyncTask.Builder(uri.toString())
                    .onPostExecute(this::fetchWeatherPost)
                    .onCancelled(error -> Log.e("", error))
                    .build().execute();
        } else {
            Log.d("WEATHER_ERR", "Unable to get device location & no saved locations");
        }
    }

    public WeatherProfile getWeatherProfileForLocation(final Location newLoc) {
        WeatherProfile closest = null;
        float shortestDist = Float.MAX_VALUE;

        for(WeatherProfile w : mWeatherProfileList) {
            Location thisLoc = w.getLocation();
            float distBetweenLocations = thisLoc.distanceTo(newLoc);

            if(Utils.areCloseTogether(newLoc, thisLoc)
                    && distBetweenLocations < shortestDist) {
                shortestDist = distBetweenLocations;
                closest = w;
            }
        }
        return closest;
    }

    public LiveData<List<WeatherProfile>> getAllWeatherProfiles() {
        return mLiveWeatherProfiles;
    }

//    public void addWeatherProfile(final WeatherProfile newWP) {
//        boolean alreadyHaveLocation = false;
//        Location newLoc = newWP.getLocation();
//        WeatherProfile closeEnough = null;
//        for(WeatherProfile wp : mWeatherProfileList) {
//            Location thisLoc = wp.getLocation();
//            if(areCloseTogether(thisLoc, newLoc)) {
//                alreadyHaveLocation = true;
//                closeEnough = wp;
//                break;
//            }
//        }
//
//        if(alreadyHaveLocation) {
//            mWeatherProfileList.remove(closeEnough);
//            mWeatherProfileList.add(newWP);
//        } else {
//            mWeatherProfileList.add(newWP);
//        }
//
//        mLiveWeatherProfiles.setValue(mWeatherProfileList);
//    }

//    public void removeWeatherProfile(final int tID) {
//        mWeatherProfileList.remove(tID);
//        mLiveWeatherProfiles.setValue(mWeatherProfileList);
//    }

    private void fetchWeatherPost(final String result) {
        try {
            JSONObject root = new JSONObject(result).getJSONObject("response");
            if(root.has("responses")) {
                JSONArray data = root.getJSONArray("responses");
                mWeatherProfileList = new ArrayList<>();

                for(int i = 0; i < data.length(); i+=3) {
                    int id = i / 3;
                    Location loc = mSavedLocations.get(id);
                    JSONObject obsJSON = data.getJSONObject(i);
                    JSONObject dailyJSON = data.getJSONObject(i+1);
                    JSONObject hourlyJSON = data.getJSONObject(i+2);

                    WeatherProfile wp = new WeatherProfile(id, loc, obsJSON, dailyJSON, hourlyJSON);
                    mWeatherProfileList.add(wp);
                }

                mSavedLocations = null;
                mLiveWeatherProfiles.setValue(mWeatherProfileList);
            }

        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_UPDATE_ERR", Objects.requireNonNull(e.getMessage()));
        }
    }

    private String buildWeatherQuery(ArrayList<Location> tSavedLocations) {
        StringBuilder result = new StringBuilder();

        String obsFields = "place.name,place.state,ob.tempC,ob.tempF,ob.weather,ob.icon";
        String dailyFields = "periods.weather,periods.minTempC,periods.minTempF,periods.maxTempC,periods.maxTempF,periods.icon";
        String hourlyFields = "periods.weather,periods.avgTempC,periods.avgTempF,periods.icon";
        String qm = "%3F";
        String amp = "%26";

        for(Location loc : tSavedLocations) {
            StringBuilder req = new StringBuilder();
            String locEP = loc.getLatitude() + "," + loc.getLongitude();

            //append current weather request: '/observations/{locEP}?fields={obsFields},'
            req.append("/observations/").append(locEP).append(qm)
                    .append("fields=").append(obsFields)
                    .append(",");
            //append 10-day forecast request: '/forecasts/{locEP}?limit=10&fields={dailyFields},'
            req.append("/forecasts/").append(locEP).append(qm)
                    .append("limit=10").append(amp)
                    .append("fields=").append(dailyFields)
                    .append(",");
            //append 24hr forecast request: '/forecasts/{locEP}?filter=1hr&limit=24&fields={hourlyFields},'
            req.append("/forecasts/").append(locEP).append(qm)
                    .append("filter=1hr").append(amp)
                    .append("limit=24").append(amp)
                    .append("fields=").append(hourlyFields)
                    .append(",");

            //append query string for this location to entire request.
            result.append(req);
        }
        //remove trailing comma:
        result.deleteCharAt(result.length()-1);

        return result.toString();
    }

    public static ViewModelProvider.Factory getFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public WeatherProfileViewModel create(@NonNull Class modelClass) {
                if(mInstance == null) { mInstance = new WeatherProfileViewModel(); }
                return mInstance;
            }
        };
    }
}
