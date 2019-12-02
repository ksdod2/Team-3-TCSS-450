package edu.uw.tcss450.team3chatapp.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

    private WeatherProfileViewModel mWeatherModel;
    private SharedPreferences mPrefs;
    private String mUnits;

    public WeatherFragment() {/*Required empty public constructor*/}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        Utils.updateWeatherIfNecessary(mPrefs);
        mWeatherModel = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);

        if(mPrefs.contains(getString(R.string.keys_prefs_tempunit))) {
            mUnits = mPrefs.getString(getString(R.string.keys_prefs_tempunit), "F");
        } else { //Otherwise set units to default (imperial)
            mUnits = "F";
            mPrefs.edit().putString(getString(R.string.keys_prefs_tempunit), "F").apply();
        }

        populateWeatherData(mWeatherModel.getCurrentLocationWeatherProfile().getValue());
    }

    private void populateWeatherData(final WeatherProfile theWP) {
        if(theWP != null) {
            setupCurrent(theWP.getCurrentWeather(), getFirst(theWP.get10DayForecast()));
            setup24Hour(theWP.get24hrForecast());
            setup10Day(theWP.get10DayForecast());
        } else {
            Log.e("WEATHER_FRAG_ERR", "No current weather profile");
        }
    }

    private void setupCurrent(final JSONObject theCurrentConditionsJSON, final JSONObject theDaysForecastJSON) {
        // Get TextViews to populate from layout
        TextView tvCurrentTempUnits = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_current_tempunits);
        TextView tvWindSpeedUnits = Objects.requireNonNull(getView().findViewById(R.id.tv_weather_current_windspeed_units));

        TextView tvCityState = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentCityState);
        TextView tvCurrentTemp = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentTemperatureDefault);
        TextView tvDescription = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentDescriptionDefault);
        TextView tvHumidity = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentHumidityDefault);
        TextView tvWindSpeed = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentWindDefault);
        ImageView ivIcon = Objects.requireNonNull(getView()).findViewById(R.id.iv_weather_currentIcon);

        TextView tvMinTemp = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentMinimumDefault);
        TextView tvMaxTemp = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentMaximumDefault);
        TextView tvRainChance = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentPrecipitationDefault);
        TextView tvSunrise = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentSunriseDefault);
        TextView tvSunset = Objects.requireNonNull(getView()).findViewById(R.id.tv_weather_currentSunsetDefault);

        //Set units
        String tempUnitDisplay = getString(R.string.tv_weather_tempunitsym) + mUnits;
        tvCurrentTempUnits.setText(tempUnitDisplay);
        tvWindSpeedUnits.setText("F".equals(mUnits) ? getString(R.string.tv_weather_windunits_imperial)
                                                    : getString(R.string.tv_weather_windunits_metric));

        // parse JSON
        try {
            if (theCurrentConditionsJSON.has(getString(R.string.keys_json_weather_response))) {
                JSONObject response = theCurrentConditionsJSON.getJSONObject(getString(R.string.keys_json_weather_response));
                if(response.has(getString(R.string.keys_json_weather_place)) && response.has(getString(R.string.keys_json_weather_ob))) {
                    JSONObject place = response.getJSONObject("place");
                    JSONObject ob = response.getJSONObject("ob");

                    // Get icon file resource name (minus ".png" extension)
                    String icFile = ob.getString(getString(R.string.keys_json_weather_icon))
                            .substring(0, ob.getString(getString(R.string.keys_json_weather_icon)).length()-4);

                    // Display info
                    tvCityState.setText(Utils.formatCityState(place.getString("name"), place.getString("state").toUpperCase()));

                    tvCurrentTemp.setText("F".equals(mUnits)
                                            ? ob.getString(getString(R.string.keys_json_weather_tempf))
                                            : ob.getString(getString(R.string.keys_json_weather_tempc)));

                    tvDescription.setText(ob.getString(getString(R.string.keys_json_weather_desc)));

                    String humidityDisplay = ob.getString(getString(R.string.keys_json_weather_humidity)) + "%";
                    tvHumidity.setText(humidityDisplay);

                    tvWindSpeed.setText("F".equals(mUnits)
                                            ? ob.getString(getString(R.string.keys_json_weather_windspeed_imperial))
                                            : ob.getString(getString(R.string.keys_json_weather_windspeed_metric)));

                    ivIcon.setImageResource(getResources().getIdentifier(icFile, "mipmap", getContext().getPackageName()));

                    tvMinTemp.setText("F".equals(mUnits)
                            ? theDaysForecastJSON.getString(getString(R.string.keys_json_weather_mintempf))
                            : theDaysForecastJSON.getString(getString(R.string.keys_json_weather_mintempc)));

                    tvMaxTemp.setText("F".equals(mUnits)
                            ? theDaysForecastJSON.getString(getString(R.string.keys_json_weather_maxtempf))
                            : theDaysForecastJSON.getString(getString(R.string.keys_json_weather_maxtempc)));

                    String rainChanceDisplay = theDaysForecastJSON.getString(getString(R.string.keys_json_weather_precipitation)) + "%";
                    tvRainChance.setText(rainChanceDisplay);

                    tvSunrise.setText(new SimpleDateFormat("HH:mm").format(new java.util.Date(Long.parseLong(theDaysForecastJSON.getString(getString(R.string.keys_json_weather_sunrise)))*1000L)));

                    tvSunset.setText(new SimpleDateFormat("HH:mm").format(new java.util.Date(Long.parseLong(theDaysForecastJSON.getString(getString(R.string.keys_json_weather_sunset)))*1000L)));
                }
            }
        } catch(JSONException e) {
            //TODO Print useful error message
        }
    }

    private void setup24Hour(final JSONObject the24HourForecastJSON) {

    }

    private void setup10Day(final JSONObject the10DayJSON) {

    }

    /**
     * Helper method gets first day of 10 day forecast for populating current conditions
     *
     * @param theListJSON
     * @return
     */
    private JSONObject getFirst(final JSONObject theListJSON) {
        JSONObject first = null;

        try {
            first = theListJSON
                    .getJSONArray(getString(R.string.keys_json_weather_response))
                    .getJSONObject(0)
                    .getJSONArray(getString(R.string.keys_json_weather_periods_array))
                    .getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return first;
    }
}
