package edu.uw.tcss450.team3chatapp.model;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherProfile {

    private int mID;
    private Location mLocation;
    private JSONObject mCurrentWeatherJSON;
    private JSONObject m10DayForecastJSON;
    private JSONObject m24hrForecastJSON;

    public WeatherProfile(final int tID,
                          final Location tLoc,
                          final JSONObject tCurWeather,
                          final JSONObject t10day,
                          final JSONObject t24hr) {

        mID = tID;
        mLocation = tLoc;
        mCurrentWeatherJSON = tCurWeather;
        m10DayForecastJSON = t10day;
        m24hrForecastJSON = t24hr;
    }

    public int getID() {return mID;}
    public Location getLocation() {return mLocation;}
    public JSONObject getCurrentWeather() {return mCurrentWeatherJSON;}
    public JSONObject get10DayForecast() {return m10DayForecastJSON;}
    public JSONObject get24hrForecast() {return m24hrForecastJSON;}
}
