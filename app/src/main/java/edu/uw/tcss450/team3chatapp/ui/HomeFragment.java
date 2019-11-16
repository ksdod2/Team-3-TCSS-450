package edu.uw.tcss450.team3chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.HomeActivityArgs;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Credentials;
import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;

public class HomeFragment extends Fragment {

    private TextView mWeatherTemp;
    private TextView mWeatherDescription;
    private ImageView mWeatherIcon;
    private TextView mCityCountry;
    private Location mLocation;
    private String mUnits; //for header param "units" (metric | imperial)

    //MIGHT need once custom locations are enabled
    private int mCityID; //for header param "id"
    private String mCityName; //for header param "q"
    private String mAPIkey; //for header param "appid"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Custom back button functionality to exit app from this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Objects.requireNonNull(getActivity()).finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //Get shared preferences for preferred temperature units
        SharedPreferences prefs = Objects.requireNonNull(getActivity())
                .getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.keys_prefs_tempunit))) {
            mUnits = prefs.getString(getString(R.string.keys_prefs_tempunit), "imperial");
        } else { //Otherwise set units to default (imperial)
            mUnits = "imperial";
            prefs.edit().putString(getString(R.string.keys_prefs_tempunit), "imperial").apply();
        }

        // Get credentials from HomeActivityArgs
        Credentials credentials = HomeActivityArgs.fromBundle(Objects.requireNonNull(getArguments())).getCredentials();

        // calendar for current time
        Calendar calendar = Calendar.getInstance();

        // Get last known device location
        LocationViewModel model = LocationViewModel.getFactory().create(LocationViewModel.class);
        mLocation = model.getCurrentLocation().getValue();

        // Get UI elements
        mWeatherDescription = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_status);
        mWeatherTemp = getView().findViewById(R.id.tv_home_temperature);
        mWeatherIcon = getView().findViewById(R.id.iv_home_weatherIcon);
        mCityCountry = getView().findViewById(R.id.tv_home_citycountry);

        TextView units = getView().findViewById(R.id.tv_home_unit);
        TextView greeting = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_greeting);
        TextView date = getView().findViewById((R.id.tv_home_date));
        TextView dayOfWeek = getView().findViewById((R.id.tv_home_dayOfWeek));

        // Set preferred unit of measurement
        if("imperial".equals(mUnits)) {
            units.setText("F");
        } else {
            units.setText("C");
        }

        // format the date and day of week
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");

        String dateString = dateFormat.format(calendar.getTime());
        String dayOfWeekString = dayOfWeekFormat.format((calendar.getTime()));

        // set greeting message with credentials
        String greetingText = "Welcome, " + credentials.getFirstName() + " " + credentials.getLastName() + "!";
        greeting.setText(greetingText);

        // set current date and day of week
        date.setText(dateString);
        dayOfWeek.setText(dayOfWeekString);

        //Make weather API call and display info
        populateWeatherData();
    }

    private void populateWeatherData() {

        if(mLocation != null) {
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority(getString(R.string.ep_weather_base))
                    .appendPath(getString(R.string.ep_weather_data))
                    .appendPath(getString(R.string.ep_weather_ver))
                    .appendPath(getString(R.string.ep_weather_weather))
                    //Location can be passed in 3 ways:
                    //1. City Name (query param "q"):
                    //.appendQueryParameter("q", mCityName)
                    //2. City ID (query param "id"):
                    //.appendQueryParameter("id", mCityID)
                    //3. Lat & Lon (query params "lat" & "lon"):
                    .appendQueryParameter("lat", Double.toString(mLocation.getLatitude()))
                    .appendQueryParameter("lon", Double.toString(mLocation.getLongitude()))
                    .appendQueryParameter("appid", getString(R.string.api_key_openweathermap))
                    .appendQueryParameter("units", mUnits)
                    .build();

            new GetAsyncTask.Builder(uri.toString())
                    .onPreExecute(this::weatherOnPre)
                    .onCancelled(this::weatherOnCancel)
                    .onPostExecute(this::weatherOnPost)
                    .build().execute();
        } else {
            Log.d("WEATHER_URI", "location is null");
        }
    }

    private void weatherOnPre() {
        //TODO show progressbar layout to hide blank details
    }

    private void weatherOnCancel(final String theResult) {
        try {
            JSONObject jsonObj = new JSONObject(theResult);
            Log.d("WEATHER_CANCEL", jsonObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void weatherOnPost(final String theResult) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(theResult);
            if(root.has("main") && root.has("weather") && root.has("sys") && root.has("name")) {
                JSONObject sys = root.getJSONObject("sys");
                JSONObject main = root.getJSONObject("main");
                JSONObject weather = root.getJSONArray("weather").getJSONObject(0);

                int temp = (int) Double.parseDouble(main.getString("temp"));
                String weatherDesc = weather.getString("description").substring(0, 1).toUpperCase() +
                                     weather.getString("description").substring(1);
                String cityPlusCountry = root.getString("name") + ", " + sys.getString("country");

                mWeatherTemp.setText(String.valueOf(temp));
                mWeatherDescription.setText(weatherDesc);
                mWeatherIcon.setImageResource(R.mipmap.weather_icon_08);
                mCityCountry.setText(cityPlusCountry);
            } else {
                Log.d("WEATHER_POST", "main or weather missing in response: " + root.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_POST", Objects.requireNonNull(e.getMessage()));
        }
    }
}