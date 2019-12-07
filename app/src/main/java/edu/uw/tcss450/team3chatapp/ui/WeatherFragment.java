package edu.uw.tcss450.team3chatapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.Utils;

/** Handes logic for WeatherHub landing screen. */
@SuppressLint("SimpleDateFormat")
public class WeatherFragment extends Fragment {

    /**  */
    private static final int SAVED_LOCATIONS_LIMIT = 10;

    /** ViewModel for weather profiles */
    private WeatherProfileViewModel mWeatherVM;
    /** weather profile information top be shown when fragment loads */
    private WeatherProfile mWPtoLoad;
    /** user-preferred unit choice (imperial/metric) */
    private String mUnits;
    /** LatLng object created from the zip code entered by user. */
    private LatLng mFromZip;

    /** Required empty public constructor */
    public WeatherFragment() {/*Required empty public constructor*/}

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    /**
     * {@inheritDoc}
     * Sets up view and sets onClickListener.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if weather should be updated first and set preferred units
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        mWeatherVM = ViewModelProviders
                .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                .get(WeatherProfileViewModel.class);
        Utils.updateWeatherIfNecessary(mWeatherVM);

        if(prefs.contains(getString(R.string.keys_prefs_tempunit))) {
            mUnits = prefs.getString(getString(R.string.keys_prefs_tempunit), "F");
        } else { //Otherwise set units to default (imperial)
            mUnits = "F";
            prefs.edit().putString(getString(R.string.keys_prefs_tempunit), "F").apply();
        }

        // Check for selected location besides device location
        mWPtoLoad = mWeatherVM.getSelectedLocationWeatherProfile().getValue();

        //default to device location
        if(mWPtoLoad == null) {
            mWPtoLoad = mWeatherVM.getCurrentLocationWeatherProfile().getValue();}

        //display weather info to user
        populateWeatherData(mWPtoLoad);

        // Set navigation to Map View & Saved Locations Fragment
        view.findViewById(R.id.tv_weather_map).setOnClickListener(v ->
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(WeatherFragmentDirections.actionNavWeatherToNavMap()));
        view.findViewById(R.id.tv_weather_viewSavedLocations).setOnClickListener(v ->
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(WeatherFragmentDirections.actionNavWeatherToNavWeatherprofiles()));
        view.findViewById(R.id.tv_weather_saveCurrentLocation).setOnClickListener(v -> saveLocationAttempt(mWeatherVM));

        Button btnSearch = view.findViewById(R.id.btn_weather_search);
        EditText etZipSearch = view.findViewById(R.id.et_weather_searchByZip);

        btnSearch.setOnClickListener(v -> searchZip(etZipSearch.getText().toString()));
    }

    /** Searches for location information by user-entered zip code, then hits API to get weather information. */
    private void searchZip(String tZip) {
        if(tZip.length() != 5) {
            Toast.makeText(getContext(), "Zip-code invalid: Must be 5 digits", Toast.LENGTH_LONG).show();
        } else {
            Utils.hideKeyboard(Objects.requireNonNull(getActivity()));
            Geocoder geo = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> list = geo.getFromLocationName(tZip, 1);
                if(list != null && !list.isEmpty()) {
                    Address add = list.get(0);
                    String cityState = Utils.formatCityState(add.getLocality(), add.getAdminArea());

                    //compare to current and saved locations
                    if(!Objects.requireNonNull(mWeatherVM.getCurrentLocationWeatherProfile().getValue()).getCityState().equals(cityState)) {
                        boolean noMatch = true;
                        for(WeatherProfile wp : Objects.requireNonNull(mWeatherVM.getSavedLocationWeatherProfiles().getValue())) {
                            if(cityState.equals(wp.getCityState())) {
                                noMatch = false;
                                break;
                            }
                        }

                        if(noMatch) {
                            ArrayList<LatLng> wrapper = new ArrayList<>();
                            mFromZip = new LatLng(add.getLatitude(),add.getLongitude());
                            wrapper.add(mFromZip);

                            Uri uri = new Uri.Builder()
                                    .scheme("https")
                                    .authority("team3-chatapp-backend.herokuapp.com")
                                    .appendPath("weather")
                                    .appendPath("batch")
                                    .appendQueryParameter("requests", Utils.buildWeatherProfileQuery(wrapper))
                                    .build();

                            Log.d("API_CALL_MAP", uri.toString());

                            new GetAsyncTask.Builder(uri.toString())
                                    .onPreExecute(this::searchZipPre)
                                    .onCancelled(this::searchZipCancel)
                                    .onPostExecute(this::searchZipPost)
                                    .onCancelled(error -> Log.e("", error))
                                    .build().execute();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "No valid location found", Toast.LENGTH_LONG).show();
                }
            } catch(Exception e) {e.printStackTrace();}
        }

    }

    /** onPre */
    private void searchZipPre() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.btn_weather_search).setEnabled(false);
        Objects.requireNonNull(getActivity()).findViewById(R.id.layout_wait).setVisibility(View.VISIBLE);
    }

    /** onCancel */
    private void searchZipCancel(final String result) {
        populateWeatherData(mWeatherVM.getCurrentLocationWeatherProfile().getValue());

        Objects.requireNonNull(getActivity()).findViewById(R.id.btn_weather_search).setEnabled(true);
        Objects.requireNonNull(getActivity()).findViewById(R.id.layout_wait).setVisibility(View.GONE);

        Log.d("ZIP", result);
    }

    /** onPost */
    private void searchZipPost(final String result) {
        WeatherProfile wpToLoad = null;
        try {
            JSONObject root = new JSONObject(result).getJSONObject("response");
            if(root.has("responses")) {
                JSONArray data = root.getJSONArray("responses");

                String obsJSONStr = data.getJSONObject(0).toString();
                String dailyJSONStr = data.getJSONObject(1).toString();
                String hourlyJSONStr = data.getJSONObject(2).toString();
                String cityState = getCityState(obsJSONStr);

                wpToLoad = new WeatherProfile(mFromZip, obsJSONStr, dailyJSONStr, hourlyJSONStr, cityState);

                // Set current location to one chosen on map so it's loaded again when they go back to map
                WeatherProfileViewModel weatherVm = ViewModelProviders
                        .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                        .get(WeatherProfileViewModel.class);
                weatherVm.setSelectedLocationWeatherProfile(wpToLoad);
            }
        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_UPDATE_ERR", Objects.requireNonNull(e.getMessage()));

            Objects.requireNonNull(getActivity()).findViewById(R.id.btn_weather_search).setEnabled(true);
            Objects.requireNonNull(getActivity()).findViewById(R.id.layout_wait).setVisibility(View.GONE);
        }

        if(wpToLoad == null) {
            Toast.makeText(getContext(), "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
            wpToLoad = mWeatherVM.getCurrentLocationWeatherProfile().getValue();
        }

        mWeatherVM.setSelectedLocationWeatherProfile(wpToLoad);
        mWPtoLoad = wpToLoad;
        populateWeatherData(wpToLoad);

        Objects.requireNonNull(getActivity()).findViewById(R.id.btn_weather_search).setEnabled(true);
        Objects.requireNonNull(getActivity()).findViewById(R.id.layout_wait).setVisibility(View.GONE);
    }

    /** Attempts to save location to ViewModel */
    private void saveLocationAttempt(WeatherProfileViewModel tWPVM) {

        // Check if location they're trying to save is near already saved location
        boolean noMatch = true;
        List<WeatherProfile> savedLocationWPs = tWPVM.getSavedLocationWeatherProfiles().getValue();
        if (Objects.requireNonNull(savedLocationWPs).size() >= SAVED_LOCATIONS_LIMIT) {
            Toast.makeText(getContext(), "Maximum number of saved locations already reached.", Toast.LENGTH_LONG).show();
        } else if(Objects.requireNonNull(tWPVM.getCurrentLocationWeatherProfile().getValue()).equals(mWPtoLoad)) {
            Toast.makeText(getContext(), "Saved " + mWPtoLoad.getCityState() + " to your saved locations!", Toast.LENGTH_LONG).show();
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

    /** Controller for setting up views with weather info. */
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

    /** Sets up current conditions section in fragment. */
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
                    String cityState = Utils.formatCityState(place.getString("name"), place.getString("state").toUpperCase()) + "\u00A0";
                    tvCityState.setText(cityState);

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

    /** Sets up 24 hour forecast section in fragment. */
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

    /** Sets up 10 day forecast section in fragment. */
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

    /** Helper method gets lists of views to populate for 24 hour forecast section. */
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

    /** Helper method gets lists of views to populate for 24 hour forecast section. */
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

    /**
     * Parses JSON from for the city and state information of location.
     * @param theJSONasStr JSON object representing API's observation endpoint response.
     * @return the city and state information, formatted as "{City}, {State}".
     */
    private String getCityState(final String theJSONasStr) {

        String result = "";

        try {
            JSONObject theJSON = new JSONObject(theJSONasStr);
            if (theJSON.has(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_response))) {
                JSONObject response = theJSON.getJSONObject(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_response));
                if(response.has(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_place))) {

                    JSONObject place = response.getJSONObject(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_place));

                    result = Utils.formatCityState(place.getString(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_name)),
                            place.getString(Objects.requireNonNull(getActivity()).getString(R.string.keys_json_weather_state)).toUpperCase());

                } else {
                    Log.d("WEATHER_POST", "Either Place or Ob missing form Response: " + response.toString());
                }
            }
        } catch(JSONException e){e.printStackTrace();}

        return result;
    }
}