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
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.Utils;

public class HomeFragment extends Fragment {

    private TextView mWeatherTemp;
    private TextView mWeatherDescription;
    private ImageView mWeatherIcon;
    private TextView mCityState;
    private Location mLocation;
    private String mUnits;

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
            mUnits = prefs.getString(getString(R.string.keys_prefs_tempunit), "F");
        } else { //Otherwise set units to default (imperial)
            mUnits = "F";
            prefs.edit().putString(getString(R.string.keys_prefs_tempunit), "F").apply();
        }

        // Get credentials from HomeActivityArgs
        Credentials credentials = HomeActivityArgs.fromBundle(Objects.requireNonNull(getArguments())).getCredentials();

        // calendar for current time
        Calendar calendar = Calendar.getInstance();

        // Get last known device location
        LocationViewModel locVM = LocationViewModel.getFactory().create(LocationViewModel.class);
        mLocation = locVM.getCurrentLocation().getValue();

        // Update weather if necessary
        Utils.updateWeatherIfNecessary(prefs);

        // Get UI elements
        mWeatherDescription = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_status);
        mWeatherTemp = getView().findViewById(R.id.tv_home_temperature);
        mWeatherIcon = getView().findViewById(R.id.iv_home_weatherIcon);
        mCityState = getView().findViewById(R.id.tv_home_citystate);

        TextView units = getView().findViewById(R.id.tv_home_unit);
        TextView greeting = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_greeting);
        TextView date = getView().findViewById((R.id.tv_home_date));
        TextView dayOfWeek = getView().findViewById((R.id.tv_home_dayOfWeek));

        // Set preferred unit of measurement
        if("F".equals(mUnits)) {
            units.setText("°F");
        } else {
            units.setText("°C");
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

        // Use data in WeatherProfileViewModel to display weather
        populateWeatherData();
    }

    private void populateWeatherData() {
        WeatherProfileViewModel model = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);
        WeatherProfile curLocWP = model.getCurrentLocationWeatherProfile().getValue();

        /* On app boot w/ stay signed in checked, there's no way for the onPostExecute
           that updates the weather for current location to run BEFORE reaching this
           point, so weather data for current location needs to be loaded manually.*/
        if(curLocWP == null) {
            String locEP = mLocation.getLatitude() + "," + mLocation.getLongitude();
            String obsFields = "place.name,place.state,ob.tempC,ob.tempF,ob.weather,ob.icon";

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_weather))
                    .appendPath(getString(R.string.ep_weather_obs))
                    .appendPath(locEP)
                    .appendQueryParameter("fields", obsFields)
                    .build();

            new GetAsyncTask.Builder(uri.toString())
                    .onPreExecute(this::weatherOnPre)
                    .onCancelled(this::weatherOnCancel)
                    .onPostExecute(this::weatherOnPost)
                    .onCancelled(error -> Log.e("", error))
                    .build().execute();

        } else {
            weatherOnPost(curLocWP.getCurrentWeather().toString());
        }
    }

    private void weatherOnPre() {
        getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
    }

    private void weatherOnCancel(final String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
    }

    private void weatherOnPost(final String result) {
        try {
            JSONObject currentWeatherRoot = new JSONObject(result);
            if (currentWeatherRoot.has(getString(R.string.keys_json_weather_response))) {
                JSONObject response = currentWeatherRoot.getJSONObject(getString(R.string.keys_json_weather_response));
                if(response.has(getString(R.string.keys_json_weather_place))
                        && response.has(getString(R.string.keys_json_weather_ob))) {

                    JSONObject place = response.getJSONObject("place");
                    JSONObject ob = response.getJSONObject("ob");

                    String cityState = formatCityState(place.getString("name"),
                            place.getString("state").toUpperCase());

                    String icFile = ob.getString("icon").substring(0, ob.getString("icon").length()-4);

                    int id = getResources().getIdentifier(icFile, "mipmap", getContext().getPackageName());

                    mWeatherDescription.setText(ob.getString("weather"));
                    mWeatherTemp.setText("F".equals(mUnits) ? ob.getString("tempF") : ob.getString("tempC"));
                    mCityState.setText(cityState);
                    mWeatherIcon.setImageResource(id);
                } else {
                    Log.d("WEATHER_POST", "Either Place or Ob missing form Response: " + response.toString());
                }
            }
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
        } catch(JSONException e) {
            //TODO Print useful error message
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
        }
    }

    private String formatCityState(String name, String state) {

        StringBuilder city = new StringBuilder();

        String[] split;
        if(name.contains(" ")) {
            split = name.split(" ");
            for(String s : split) {
                city.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
            }
        } else {
            city.append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append(" ");
        }
        city.trimToSize();

        return city.append(", ").append(state).toString();
    }
}