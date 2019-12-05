package edu.uw.tcss450.team3chatapp.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("SimpleDateFormat")
public class WeatherFragment extends Fragment {

    private static final int SAVED_LOCATIONS_LIMIT = 10;

    private WeatherProfile mWPtoLoad;
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

        // Check if weather should be updated first and set preferred units
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        WeatherProfileViewModel weatherVm = ViewModelProviders
                .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                .get(WeatherProfileViewModel.class);
        Utils.updateWeatherIfNecessary(weatherVm);

        if(prefs.contains(getString(R.string.keys_prefs_tempunit))) {
            mUnits = prefs.getString(getString(R.string.keys_prefs_tempunit), "F");
        } else { //Otherwise set units to default (imperial)
            mUnits = "F";
            prefs.edit().putString(getString(R.string.keys_prefs_tempunit), "F").apply();
        }

        // Check for passed in location to load from map fragment, instead of just the current location
        WeatherFragmentArgs args = WeatherFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mWPtoLoad = args.getWeatherProfile();

        //default to device location
        if(mWPtoLoad == null) {
            mWPtoLoad = weatherVm.getCurrentLocationWeatherProfile().getValue();}

        //display weather info to user
        populateWeatherData(mWPtoLoad);

        // Set navigation to Map View & Saved Locations Fragment
        view.findViewById(R.id.tv_weather_map).setOnClickListener(v ->
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(WeatherFragmentDirections.actionNavWeatherToNavMap()));
        view.findViewById(R.id.tv_weather_viewSavedLocations).setOnClickListener(v ->
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(WeatherFragmentDirections.actionNavWeatherToNavWeatherprofiles()));
        view.findViewById(R.id.tv_weather_saveCurrentLocation).setOnClickListener(v -> saveLocationAttempt(weatherVm));
    }

    private void saveLocationAttempt(WeatherProfileViewModel tWPVM) {

        // Check if location they're trying to save is near already saved location
        boolean noMatch = true;
        List<WeatherProfile> savedLocationWPs = tWPVM.getSavedLocationWeatherProfiles().getValue();
        if(Objects.requireNonNull(savedLocationWPs).size() >= SAVED_LOCATIONS_LIMIT) {
            Toast.makeText(getContext(), "Maximum number of saved locations already reached.", Toast.LENGTH_LONG).show();
        } else {
            for(WeatherProfile wp : savedLocationWPs) {
                if(mWPtoLoad.getCityState().equals(wp.getCityState())) {
                    noMatch = false;
                    break;
                }
            }
            //if no match then save location, otherwise let the user know it's already saved.
            if(noMatch) {
                tWPVM.saveLocation(mWPtoLoad);
                Toast.makeText(getContext(), "Saved " + mWPtoLoad.getCityState() + " to your saved locations!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "This location is already saved!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void populateWeatherData(final WeatherProfile theWP) {
        if(theWP != null) {
            try {
                JSONObject currentWeatherJSON = new JSONObject(theWP.getCurrentWeather());
                JSONObject forecast24HrJSON = new JSONObject(theWP.get24hrForecast());
                JSONObject forecast10DayJSON = new JSONObject(theWP.get10DayForecast());

                setupCurrent(currentWeatherJSON, getFirst(forecast10DayJSON));
                setup24Hour(forecast24HrJSON);
                setup10Day(forecast10DayJSON);
            } catch (JSONException e) {e.printStackTrace();}
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
        String tempUnitDisplay = mUnits;
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

                    String curTempDisplay = "F".equals(mUnits)
                                            ? ob.getString(getString(R.string.keys_json_weather_tempf))
                                            : ob.getString(getString(R.string.keys_json_weather_tempc));
                    curTempDisplay += getString(R.string.misc_temp_unit_symbol);
                    tvCurrentTemp.setText(curTempDisplay);

                    tvDescription.setText(Utils.cloudDecode(ob.getString(getString(R.string.keys_json_weather_cloudcode))));

                    String humidityDisplay = ob.getString(getString(R.string.keys_json_weather_humidity)) + "%";
                    tvHumidity.setText(humidityDisplay);

                    tvWindSpeed.setText("F".equals(mUnits)
                                            ? ob.getString(getString(R.string.keys_json_weather_windspeed_imperial))
                                            : ob.getString(getString(R.string.keys_json_weather_windspeed_metric)));

                    ivIcon.setImageResource(getResources().getIdentifier(icFile, "mipmap", Objects.requireNonNull(getContext()).getPackageName()));

                    String lowTempDisplay = "F".equals(mUnits)
                            ? theDaysForecastJSON.getString(getString(R.string.keys_json_weather_mintempf))
                            : theDaysForecastJSON.getString(getString(R.string.keys_json_weather_mintempc));
                    lowTempDisplay += getString(R.string.misc_temp_unit_symbol);
                    tvMinTemp.setText(lowTempDisplay);

                    String highTempDisplay = "F".equals(mUnits)
                            ? theDaysForecastJSON.getString(getString(R.string.keys_json_weather_maxtempf))
                            : theDaysForecastJSON.getString(getString(R.string.keys_json_weather_maxtempc));
                    highTempDisplay += getString(R.string.misc_temp_unit_symbol);
                    tvMaxTemp.setText(highTempDisplay);

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
        LinearLayout container = Objects.requireNonNull(getView()).findViewById(R.id.layout_weather_24hourForecast);
        ArrayList<ArrayList<View>> lists = build24HourLists(container);

        ArrayList<View> hours = lists.get(0);
        ArrayList<View> icons = lists.get(1);
        ArrayList<View> temps = lists.get(2);

        // Parse JSON
        try {
            if (the24HourForecastJSON.has(getString(R.string.keys_json_weather_response))) {
                JSONArray responseArr = the24HourForecastJSON.getJSONArray(getString(R.string.keys_json_weather_response));
                if(responseArr.getJSONObject(0).has(getString(R.string.keys_json_weather_periods_array))) {
                    JSONArray periods = responseArr.getJSONObject(0).getJSONArray(getString(R.string.keys_json_weather_periods_array));

                    for(int i = 0; i < periods.length(); i++) {
                        JSONObject curHourData = periods.getJSONObject(i);

                        TextView tvCurHour = (TextView) hours.get(i);
                        ImageView ivCurIcon = (ImageView) icons.get(i);
                        TextView tvCurTemp = (TextView) temps.get(i);

                        String hourDisplay =new SimpleDateFormat("HH:mm").format(new java.util.Date(Long.parseLong(curHourData.getString(getString(R.string.keys_json_weather_hourly_timestamp)))*1000L));
                        tvCurHour.setText(hourDisplay);

                        String icFile = curHourData.getString(getString(R.string.keys_json_weather_icon))
                                .substring(0, curHourData.getString(getString(R.string.keys_json_weather_icon)).length()-4);
                        ivCurIcon.setImageResource(getResources().getIdentifier(icFile, "mipmap", Objects.requireNonNull(getContext()).getPackageName()));

                        String tempDisplay = "F".equals(mUnits)
                                ? curHourData.getString(getString(R.string.keys_json_weather_avgtempf))
                                : curHourData.getString(getString(R.string.keys_json_weather_avgtempc));
                         tempDisplay += getString(R.string.misc_temp_unit_symbol);
                        tvCurTemp.setText(tempDisplay);
                    }
                }
            }
        } catch(JSONException e) {
            //TODO Print useful error message
        }
    }

    private void setup10Day(final JSONObject the10DayJSON) {
        LinearLayout container = Objects.requireNonNull(getView()).findViewById(R.id.layout_weather_10dayForecast);
        ArrayList<ArrayList<View>> lists = build10DayLists(container);

        ArrayList<View> dates = lists.get(0);
        ArrayList<View> icons = lists.get(1);
        ArrayList<View> highs = lists.get(2);
        ArrayList<View> lows = lists.get(3);

        // parse JSON
        try{
            if (the10DayJSON.has(getString(R.string.keys_json_weather_response))) {
                JSONArray responseArr = the10DayJSON.getJSONArray(getString(R.string.keys_json_weather_response));
                if (responseArr.getJSONObject(0).has(getString(R.string.keys_json_weather_periods_array))) {
                    JSONArray periods = responseArr.getJSONObject(0).getJSONArray(getString(R.string.keys_json_weather_periods_array));

                    for(int i = 0; i < periods.length(); i++) {
                        JSONObject curDayData = periods.getJSONObject(i);

                        // Get views to display info in
                        TextView tvCurDate = (TextView) dates.get(i);
                        ImageView ivCurIcon = (ImageView) icons.get(i);
                        TextView tvCurHigh = (TextView) highs.get(i);
                        TextView tvCurLow = (TextView) lows.get(i);

                        // Display Info
                        String formattedDate = new SimpleDateFormat("EEE, MMM dd")
                                .format(new java.util.Date(Long.parseLong(curDayData.getString(getString(R.string.keys_json_weather_hourly_timestamp)))*1000L));
                        tvCurDate.setText(formattedDate);

                        String icFile = curDayData.getString(getString(R.string.keys_json_weather_icon))
                                .substring(0, curDayData.getString(getString(R.string.keys_json_weather_icon)).length()-4);
                        ivCurIcon.setImageResource(getResources().getIdentifier(icFile, "mipmap", Objects.requireNonNull(getContext()).getPackageName()));

                        String tempDisplay = "F".equals(mUnits)
                                ? curDayData.getString(getString(R.string.keys_json_weather_maxtempf))
                                : curDayData.getString(getString(R.string.keys_json_weather_maxtempc));
                        tempDisplay += getString(R.string.misc_temp_unit_symbol);
                        tvCurHigh.setText(tempDisplay);

                        tempDisplay = "F".equals(mUnits)
                                ? curDayData.getString(getString(R.string.keys_json_weather_mintempf))
                                : curDayData.getString(getString(R.string.keys_json_weather_mintempc));
                        tempDisplay += getString(R.string.misc_temp_unit_symbol);
                        tvCurLow.setText(tempDisplay);
                    }
                }
            }
        } catch (JSONException e) {
            //TODO Print useful error message
        }
    }

    /**
     * Helper method gets first day of 10 day forecast for
     * populating current conditions (i.e. today's forecast).
     *
     * @param theListJSON   JSON object containing list of all 10 forecasts
     * @return              JSON object containing just the first day's forecast (today)
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

    private ArrayList<ArrayList<View>> build24HourLists(final LinearLayout theParent) {
        ArrayList<ArrayList<View>> lists = new ArrayList<>();
        ArrayList<View> hours = new ArrayList<>();
        ArrayList<View> icons = new ArrayList<>();
        ArrayList<View> temps = new ArrayList<>();

        for(int i = 0; i < theParent.getChildCount(); i++) {
            LinearLayout hourContainer = (LinearLayout) theParent.getChildAt(i);
            for(int j = 0; j < hourContainer.getChildCount(); j++) {
                switch(j) {
                    case 0:
                        hours.add(hourContainer.getChildAt(j));
                        break;
                    case 1:
                        icons.add(hourContainer.getChildAt(j));
                        break;
                    case 2:
                        temps.add(hourContainer.getChildAt(j));
                        break;
                    default:
                        throw new IllegalStateException("Lenard I hate you...");
                }
            }
        }

        lists.add(hours);
        lists.add(icons);
        lists.add(temps);

        return lists;
    }

    private ArrayList<ArrayList<View>> build10DayLists(final LinearLayout theParent) {
        ArrayList<ArrayList<View>> lists = new ArrayList<>();
        ArrayList<View> dates = new ArrayList<>();
        ArrayList<View> icons = new ArrayList<>();
        ArrayList<View> highs = new ArrayList<>();
        ArrayList<View> lows = new ArrayList<>();

        for(int i = 0; i < theParent.getChildCount(); i++) {
            LinearLayout dayContainer = (LinearLayout) theParent.getChildAt(i);
            for(int j = 0; j < dayContainer.getChildCount(); j++) {
                switch(j) {
                    case 0:
                        dates.add(dayContainer.getChildAt(j));
                        break;
                    case 1:
                        icons.add(dayContainer.getChildAt(j));
                        break;
                    case 2:
                        LinearLayout highLowContainer = (LinearLayout) dayContainer.getChildAt(j);
                        for(int k = 0; k < highLowContainer.getChildCount(); k++) {
                            switch(k) {
                                case 0:
                                    highs.add(highLowContainer.getChildAt(k));
                                    break;
                                case 3:
                                    lows.add(highLowContainer.getChildAt(k));
                                    break;
                                default: break;
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("You're killing me Lenard...");
                }
            }
        }

        lists.add(dates);
        lists.add(icons);
        lists.add(highs);
        lists.add(lows);

        return lists;
    }
}