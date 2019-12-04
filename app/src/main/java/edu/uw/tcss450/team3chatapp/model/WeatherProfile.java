package edu.uw.tcss450.team3chatapp.model;

import android.location.Location;

import java.io.Serializable;

public class WeatherProfile implements Serializable {

    private double mLatitude;
    private double mLongitude;
    private String mCityState;
    private String mCurrentWeatherJSONStr;
    private String m10DayForecastJSONStr;
    private String m24hrForecastJSONStr;

    WeatherProfile(final Location tLoc,
                          final String tCurWeather,
                          final String t10day,
                          final String t24hr,
                          final String tCityState) {

        mLatitude = tLoc.getLatitude();
        mLongitude = tLoc.getLongitude();
        mCurrentWeatherJSONStr = tCurWeather;
        m10DayForecastJSONStr = t10day;
        m24hrForecastJSONStr = t24hr;
        mCityState = tCityState;
    }

    public Location getLocation() {
        Location wrapper = new Location("");
        wrapper.setLatitude(mLatitude);
        wrapper.setLongitude(mLongitude);
        return wrapper;
    }
    public String getCurrentWeather() {return mCurrentWeatherJSONStr;}
    public String get10DayForecast() {return m10DayForecastJSONStr;}
    public String get24hrForecast() {return m24hrForecastJSONStr;}
    public String getCityState() {return mCityState;}
}
