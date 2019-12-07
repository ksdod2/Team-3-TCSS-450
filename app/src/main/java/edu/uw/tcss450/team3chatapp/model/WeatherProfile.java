package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object to store weather information for a given location.
 *
 * @author Alex Bledsoe
 */
public class WeatherProfile implements Serializable {

    /** latitude of location */
    private double mLatitude;
    /** longitude of location */
    private double mLongitude;
    /** "{CityName}, {StateName}" of location */
    private String mCityState;
    /** Current conditions JSON from weather API */
    private String mCurrentWeatherJSONStr;
    /** 10 Day forecast info JSON from weather API */
    private String m10DayForecastJSONStr;
    /** 24 Hour forecast info JSON from weather API */
    private String m24hrForecastJSONStr;

    /**
     * Constructor
     *
     * @param tLoc          latitiude & longitude that weather relates to.
     * @param tCurWeather   current conditions JSON string.
     * @param t10day        10 day forecast JSON string.
     * @param t24hr         24 hour forecast JSON string.
     * @param tCityState    formatted city and state of location
     */
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

    /** @return latitude and longitude of location as LatLng object. */
    public LatLng getLocation() {return new LatLng(mLatitude, mLongitude);}
    /** @return JSON string for current weather conditions. */
    public String getCurrentWeather() {return mCurrentWeatherJSONStr;}
    /** @return JSON string for 10 day forecast information. */
    public String get10DayForecast() {return m10DayForecastJSONStr;}
    /** @return JSON string for 24 hour forecast information. */
    public String get24hrForecast() {return m24hrForecastJSONStr;}
    /** @return "{CityName}, {StateName}" */
    public String getCityState() {return mCityState;}

    /** @return true if the weather profiles have the same mCityState field; false otherwise. */
    @Override
    public boolean equals(@Nullable Object theOther) {
        if(!(theOther instanceof WeatherProfile)){return false;}
        WeatherProfile other = (WeatherProfile) theOther;
        return mCityState.equals(Objects.requireNonNull(other).getCityState());
    }
}
