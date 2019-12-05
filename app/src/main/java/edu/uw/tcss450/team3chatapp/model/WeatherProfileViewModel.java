package edu.uw.tcss450.team3chatapp.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.Utils;

public class WeatherProfileViewModel extends AndroidViewModel {

    private static WeatherProfileViewModel mInstance;
    private MutableLiveData<WeatherProfile> mCurrentLocationWeatherProfile;
    private MutableLiveData<WeatherProfile> mSelectedLocationWeatherProfile;
    private MutableLiveData<List<WeatherProfile>> mSavedLocationsWeatherProfiles;

    private long mLastUpdated;
    private ArrayList<LatLng> mSavedLocations;

    private WeatherProfileViewModel(Application theApp) {
        super(theApp);
        mCurrentLocationWeatherProfile = new MutableLiveData<>();
        mSelectedLocationWeatherProfile = new MutableLiveData<>();
        mSavedLocationsWeatherProfiles = new MutableLiveData<>();
        mLastUpdated = System.currentTimeMillis() / 1000L;
    }

    // Public methods
    public void update(ArrayList<LatLng> theLocationsToUpdate) {
        if(theLocationsToUpdate.size() > 0) {
            mSavedLocations = theLocationsToUpdate;

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority("team3-chatapp-backend.herokuapp.com")
                    .appendPath("weather")
                    .appendPath("batch")
                    .appendQueryParameter("requests", Utils.buildWeatherProfileQuery(theLocationsToUpdate))
                    .build();

            Log.d("API_CALL_FULL", uri.toString());

            new GetAsyncTask.Builder(uri.toString())
                    .onPostExecute(this::fetchWeatherPost)
                    .onCancelled(error -> Log.e("", error))
                    .build().execute();
        } else {
            Log.d("WEATHER_ERR", "Unable to get device location & no saved locations");
        }
    }

    public void saveLocation(final WeatherProfile theWP) {
        List<WeatherProfile> savedLocations = mSavedLocationsWeatherProfiles.getValue();
        Objects.requireNonNull(savedLocations).add(theWP);
        mSavedLocationsWeatherProfiles.setValue(savedLocations);
        saveToSharedPrefs();
    }

    public void removeLocation(final int idx) {
        List<WeatherProfile> savedLocations = mSavedLocationsWeatherProfiles.getValue();
        Objects.requireNonNull(savedLocations).remove(idx);
        mSavedLocationsWeatherProfiles.setValue(savedLocations);
        saveToSharedPrefs();
    }

    //Getters
    public LiveData<WeatherProfile> getCurrentLocationWeatherProfile() {return mCurrentLocationWeatherProfile;}

    public LiveData<WeatherProfile> getSelectedLocationWeatherProfile() {return mSelectedLocationWeatherProfile;}

    public LiveData<List<WeatherProfile>> getSavedLocationWeatherProfiles() {return mSavedLocationsWeatherProfiles;}

    public long getTimeStamp() {
        return mLastUpdated;
    }

    // Public setter for location selected on map
    public void setSelectedLocationWeatherProfile(final WeatherProfile theWP) {mSelectedLocationWeatherProfile.setValue(theWP);}

    //Private setters
    private void setCurrentLocationWeatherProfile(final WeatherProfile theWP) {
        mCurrentLocationWeatherProfile.setValue(theWP);
        saveToSharedPrefs();
    }

    private void setSavedLocationWeatherProfile(final ArrayList<WeatherProfile> theWPs) {mSavedLocationsWeatherProfiles.setValue(theWPs);}

    private void setTimeStamp(final long theTime) {
        mLastUpdated = theTime;
        saveToSharedPrefs();
    }

    //Private helpers
    private void fetchWeatherPost(final String result) {
        try {
            JSONObject root = new JSONObject(result).getJSONObject("response");
            if(root.has("responses")) {
                JSONArray data = root.getJSONArray("responses");
                ArrayList<WeatherProfile> savedLocationWeatherProfileList = new ArrayList<>();

                for(int i = 0; i < data.length(); i+=3) {
                    int id = i / 3;
                    LatLng loc = mSavedLocations.get(id);
                    String obsJSONStr = data.getJSONObject(i).toString();
                    String dailyJSONStr = data.getJSONObject(i+1).toString();
                    String hourlyJSONStr = data.getJSONObject(i+2).toString();
                    String cityState = getCityState(obsJSONStr);

                    WeatherProfile wp = new WeatherProfile(loc, obsJSONStr, dailyJSONStr, hourlyJSONStr, cityState);

                    // First block of weather info is always current location
                    if(i == 0) {
                        mCurrentLocationWeatherProfile.setValue(wp);
                    } else {
                        savedLocationWeatherProfileList.add(wp);
                    }
                }
                mSavedLocationsWeatherProfiles.setValue(savedLocationWeatherProfileList);
                mLastUpdated = System.currentTimeMillis() / 1000L;

                // Save updated WeatherProfileVM info to sharedPrefs:
                saveToSharedPrefs();
            }
        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_UPDATE_ERR", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void saveToSharedPrefs() {
        SharedPreferences prefs = getApplication().getSharedPreferences(getApplication().getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        Gson gson = new Gson();

        prefs.edit().putString(getApplication().getString(R.string.keys_prefs_weathervm_current), gson.toJson(getCurrentLocationWeatherProfile().getValue())).apply();
        prefs.edit().putString(getApplication().getString(R.string.keys_prefs_weathervm_saved), gson.toJson(getSavedLocationWeatherProfiles().getValue())).apply();
        prefs.edit().putLong(getApplication().getString(R.string.keys_prefs_weathervm_lastupdated), mLastUpdated).apply();

    }

    private String getCityState(final String theJSONasStr) {

        String result = "";

        try {
            JSONObject theJSON = new JSONObject(theJSONasStr);
            if (theJSON.has(getApplication().getString(R.string.keys_json_weather_response))) {
                JSONObject response = theJSON.getJSONObject(getApplication().getString(R.string.keys_json_weather_response));
                if(response.has(getApplication().getString(R.string.keys_json_weather_place))) {

                    JSONObject place = response.getJSONObject(getApplication().getString(R.string.keys_json_weather_place));

                    result = Utils.formatCityState(place.getString(getApplication().getString(R.string.keys_json_weather_name)),
                            place.getString(getApplication().getString(R.string.keys_json_weather_state)).toUpperCase());

                } else {
                    Log.d("WEATHER_POST", "Either Place or Ob missing form Response: " + response.toString());
                }
            }
        } catch(JSONException e){e.printStackTrace();}

        return result;
    }

    // Factory class
    public static class WeatherFactory extends ViewModelProvider.NewInstanceFactory {
        private final Application mApplication;

        public WeatherFactory(Application theApplication) {
            mApplication = theApplication;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public WeatherProfileViewModel create(@NonNull Class modelClass) {
            if(mInstance == null) {
                mInstance = new WeatherProfileViewModel(mApplication);
                SharedPreferences prefs =  mApplication.getSharedPreferences(mApplication.getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
                if(prefs.contains(mApplication.getString(R.string.keys_prefs_weathervm_current))
                        &&prefs.contains(mApplication.getString(R.string.keys_prefs_weathervm_saved))
                        &&prefs.contains(mApplication.getString(R.string.keys_prefs_weathervm_lastupdated))) {

                    //setup Gson and types
                    Gson gson = new Gson();
                    Type typeCurrent = new TypeToken<WeatherProfile>(){}.getType();
                    Type typeSaved = new TypeToken<List<WeatherProfile>>(){}.getType();

                    // Get current WP, saved location WPs & last updated from SharedPrefs and convert using Gson
                    String currentWPasString = prefs.getString(mApplication.getString(R.string.keys_prefs_weathervm_current), "");
                    WeatherProfile currentWP = gson.fromJson(currentWPasString, typeCurrent);

                    String savedWPsAsString = prefs.getString(mApplication.getString(R.string.keys_prefs_weathervm_saved), "");
                    ArrayList<WeatherProfile> savedWPs = gson.fromJson(savedWPsAsString, typeSaved);

                    long lastUpdated = prefs.getLong(mApplication.getString(R.string.keys_prefs_weathervm_lastupdated), 0);

                    // Set new instance's fields to what we pulled from SharedPrefs
                    mInstance.setCurrentLocationWeatherProfile(currentWP);
                    mInstance.setSavedLocationWeatherProfile(savedWPs);
                    mInstance.setTimeStamp(lastUpdated);


                }
            }
            return mInstance;
        }
    }
}