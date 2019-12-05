package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

public class WeatherProfile implements Serializable {

    private double mLatitude;
    private double mLongitude;
    private String mCityState;
    private String mCurrentWeatherJSONStr;
    private String m10DayForecastJSONStr;
    private String m24hrForecastJSONStr;

    public WeatherProfile( final LatLng tLoc,
                    final String tCurWeather,
                    final String t10day,
                    final String t24hr,
                    final String tCityState) {

        mLatitude = tLoc.latitude;
        mLongitude = tLoc.longitude;
        mCurrentWeatherJSONStr = tCurWeather;
        m10DayForecastJSONStr = t10day;
        m24hrForecastJSONStr = t24hr;
        mCityState = tCityState;
    }

    public LatLng getLocation() {return new LatLng(mLatitude, mLongitude);}
    public String getCurrentWeather() {return mCurrentWeatherJSONStr;}
    public String get10DayForecast() {return m10DayForecastJSONStr;}
    public String get24hrForecast() {return m24hrForecastJSONStr;}
    public String getCityState() {return mCityState;}

    @Override
    public boolean equals(@Nullable Object theOther) {
        WeatherProfile other = (WeatherProfile) theOther;
        return mCityState.equals(Objects.requireNonNull(other).getCityState());
    }
}
