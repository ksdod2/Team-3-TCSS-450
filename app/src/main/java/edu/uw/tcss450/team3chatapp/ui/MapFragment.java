package edu.uw.tcss450.team3chatapp.ui;


import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.Utils;

/** Handles logic for map view. */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    /** The Google Maps object to use for the fragment's interactions. */
    private GoogleMap mMap;
    /** A Google Maps API Geocoder object to use with locations. */
    private Geocoder mCoder;
    /** The marker to display the selected location. */
    private Marker mMarker;
    /** The WeatherProfileViewModel to update with information from the map. */
    private WeatherProfileViewModel mWeatherVM;

    /** Required empty public constructor. */
    public MapFragment() {}

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * {@inheritDoc}
     * Setup instance fields and set onClickListeners.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCoder = new Geocoder(Objects.requireNonNull(getActivity()).getApplicationContext());
        mWeatherVM = ViewModelProviders
                .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                .get(WeatherProfileViewModel.class);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        view.findViewById(R.id.btn_map_use).setOnClickListener(v -> returnLocation());

    }

    /**
     * {@inheritDoc}
     * Add marker to user selected location if set, otherwise the device location.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in the selected location from before if available; otherwise current device location and move the camera
        WeatherProfile previouslySelected = mWeatherVM.getSelectedLocationWeatherProfile().getValue();
        LatLng current = previouslySelected == null
                ? Objects.requireNonNull(mWeatherVM.getCurrentLocationWeatherProfile().getValue()).getLocation()
                : previouslySelected.getLocation();
        mMarker = mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(this);
    }

    /**
     * {@inheritDoc}
     * Move move marker and update map.
     */
    @Override
    public void onMapClick(LatLng latLng) {
        mMarker.remove();
        try { // Attempt to use the location's address as a label via Google Maps' reverse Geocoding
            List<Address> place = mCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(place.get(0).getAddressLine(0)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        } catch (IOException e) {
            e.printStackTrace();
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        }
    }

    /** Returns the currently selected location as the one to be used in WeatherFragment. */
    private void returnLocation() {
        LatLng mapLocation = mMarker.getPosition();
        WeatherProfile wpToLoad = null;
        WeatherProfileViewModel weatherVM = ViewModelProviders
                .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                .get(WeatherProfileViewModel.class);

        Log.i("MAP", mapLocation.toString());

        // Get location info from LatLng passed back
        String cityState = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            Address address = geocoder.getFromLocation(mapLocation.latitude, mapLocation.longitude, 1).get(0);
            String area = address.getLocality();
            String state = address.getAdminArea();
            cityState = area == null || state == null
                    ? "" : Utils.formatCityState(address.getLocality(), address.getAdminArea());
        } catch (IOException e) {e.printStackTrace();}

        // Compare to saved locations
        if(!"".equals(cityState)) {
            for(WeatherProfile wp : Objects.requireNonNull(weatherVM.getSavedLocationWeatherProfiles().getValue())) {
                if(cityState.equals(wp.getCityState())) {
                    wpToLoad = wp;
                    break;
                }
            }
        }

        // Hit
        if(wpToLoad == null) {
            ArrayList<LatLng> wrapper = new ArrayList<>();
            wrapper.add(mapLocation);

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority("team3-chatapp-backend.herokuapp.com")
                    .appendPath("weather")
                    .appendPath("batch")
                    .appendQueryParameter("requests", Utils.buildWeatherProfileQuery(wrapper))
                    .build();

            Log.d("API_CALL_MAP", uri.toString());

            new GetAsyncTask.Builder(uri.toString())
                    .onPostExecute(this::fetchWeatherPost)
                    .onCancelled(error -> Log.e("", error))
                    .build().execute();
        }
    }

    /**
     * Parses JSON for information to load in weather fragment.
     * @param result the JSON response from weather API
     */
    private void fetchWeatherPost(final String result) {
        WeatherProfile wpToLoad = null;
        try {
            JSONObject root = new JSONObject(result).getJSONObject("response");
            if(root.has("responses")) {
                JSONArray data = root.getJSONArray("responses");

                String obsJSONStr = data.getJSONObject(0).toString();
                String dailyJSONStr = data.getJSONObject(1).toString();
                String hourlyJSONStr = data.getJSONObject(2).toString();
                String cityState = getCityState(obsJSONStr);

                wpToLoad = new WeatherProfile(mMarker.getPosition(), obsJSONStr, dailyJSONStr, hourlyJSONStr, cityState);

                // Set current location to one chosen on map so it's loaded again when they go back to map
                WeatherProfileViewModel weatherVm = ViewModelProviders
                        .of(this, new WeatherProfileViewModel.WeatherFactory(Objects.requireNonNull(getActivity()).getApplication()))
                        .get(WeatherProfileViewModel.class);
                weatherVm.setSelectedLocationWeatherProfile(wpToLoad);
            }
        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER_UPDATE_ERR", Objects.requireNonNull(e.getMessage()));
        }

        if(wpToLoad == null) {Toast.makeText(getContext(), "Oops, something went wrong. Please try again.", Toast.LENGTH_LONG).show();}

        Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_global_nav_weather);
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
