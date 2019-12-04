package edu.uw.tcss450.team3chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.ThemeChanger;
import edu.uw.tcss450.team3chatapp.utils.Utils;
import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_LOCATIONS = 8414;

    private SharedPreferences mPrefs;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);

        //Apply user-preferred theme from shared preferences before setContentView
        mPrefs = getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(mPrefs.contains(getString(R.string.keys_prefs_theme))) {
            int themeId = mPrefs.getInt(getString(R.string.keys_prefs_theme), R.style.DarkMode);
            ThemeChanger.setThemeOnActivityCreation(this, themeId);
        } else {
            mPrefs.edit().putInt(getString(R.string.keys_prefs_theme), R.style.DarkMode).apply();
            ThemeChanger.setThemeOnActivityCreation(this, R.style.DarkMode);
        }

        setContentView(R.layout.activity_main);

        // Get device location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATIONS);
        } else {
            //The user has already allowed the use of Locations. Get the current location.
            requestLocation();
        }


        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .setGraph(R.navigation.nav_graph_login, getIntent().getExtras());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_LOCATIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the locations-related task you need to do.
                requestLocation();
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                Log.d("PERMISSION DENIED", "Nothing to see or do here.");

                // TODO Inform user that app needs location permissions
                finishAndRemoveTask();
            }
        } // Add other request permissions to listen to here as else (or convert to switch statement)
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("REQUEST LOCATION", "User did NOT allow permission to request location!");
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("LOCATION", location.toString());

                            LocationViewModel LocModel = LocationViewModel.getFactory().create(LocationViewModel.class);
                            LocModel.changeLocation(location);

                            // Get saved weather info view model from SharedPreferences and check for update:
                            WeatherProfileViewModel weatherModel = ViewModelProviders
                                    .of(this, new WeatherProfileViewModel.WeatherFactory(getApplication()))
                                    .get(WeatherProfileViewModel.class);
                            Utils.updateWeatherIfNecessary(weatherModel);
                        }
            });
        }
    }
}