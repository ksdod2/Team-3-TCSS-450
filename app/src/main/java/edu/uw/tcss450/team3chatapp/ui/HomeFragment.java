package edu.uw.tcss450.team3chatapp.ui;

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
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;

public class HomeFragment extends Fragment {

    private TextView mWeatherTemp;
    private TextView mWeatherDescription;
    private ImageView mWeatherIcon;

    //TODO remove hard coding for Tacoma when ready
    private double mLat = 47.25288; //for header param "lat"
    private double mLon = -122.44429; //for header param "lon"
    private int mCityID = 5812944; //for header param "id"
    private String mCityName = "Tacoma"; //for header param "q"
    private String mUnits = "imperial"; //for header param "units" (metric | imperial)
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

        // Get credentials from HomeActivityArgs
        Credentials credentials = HomeActivityArgs.fromBundle(Objects.requireNonNull(getArguments())).getCredentials();

        // calendar for current time
        Calendar calendar = Calendar.getInstance();

        mWeatherDescription = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_status);
        mWeatherTemp = getView().findViewById(R.id.tv_home_temperature);
        mWeatherIcon = getView().findViewById(R.id.iv_home_weatherIcon);

        TextView greeting = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_greeting);
        TextView date = getView().findViewById((R.id.tv_home_date));
        TextView dayOfWeek = getView().findViewById((R.id.tv_home_dayOfWeek));

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
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_data))
                .appendPath(getString(R.string.ep_weather_ver))
                .appendPath(getString(R.string.ep_weather_weather))
                .appendQueryParameter("q", mCityName)
                .appendQueryParameter("appid", getString(R.string.api_key_openweathermap))
                .appendQueryParameter("units", mUnits)
                .build();

        new GetAsyncTask.Builder(uri.toString())
                .onPreExecute(this::weatherOnPre)
                .onCancelled(this::weatherOnCancel)
                .onPostExecute(this::weatherOnPost)
                .build().execute();
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
            if(root.has("main") && root.has("weather")) {
                JSONObject main = root.getJSONObject("main");
                JSONObject weather = root.getJSONArray("weather").getJSONObject(0);

                int temp = (int) Double.parseDouble(main.getString("temp"));
                String weatherDesc = weather.getString("description").substring(0, 1).toUpperCase() +
                                     weather.getString("description").substring(1);

                mWeatherTemp.setText(String.valueOf(temp));
                mWeatherDescription.setText(weatherDesc);
                mWeatherIcon.setImageResource(R.mipmap.weather_icon_08);
            } else {
                Log.d("WEATHER_POST", "main or weather missing in response: " + root.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_POST", Objects.requireNonNull(e.getMessage()));
        }
    }
}