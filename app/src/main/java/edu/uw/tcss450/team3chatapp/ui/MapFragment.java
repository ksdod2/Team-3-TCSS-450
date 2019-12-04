package edu.uw.tcss450.team3chatapp.ui;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.model.MapResultViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private Geocoder mCoder;
    private Marker mMarker;  // Only use one marker at a time for clarity

    public MapFragment() { /* Required empty public constructor */}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mCoder = new Geocoder(getActivity().getApplicationContext());
        view.findViewById(R.id.btn_map_use).setOnClickListener(this::returnLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Use current location as the starting point for the map
        LocationViewModel model =  LocationViewModel.getFactory().create(LocationViewModel.class);
        Location l = model.getCurrentLocation().getValue();

        // Add a marker in the current device location and move the camera
        LatLng current = new LatLng(l.getLatitude(), l.getLongitude());
        mMarker = mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0f));
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMarker.remove();
        try { // Attempt to use the location's address as a label via Google Maps' reverse Geocoding
            List<Address> place = mCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(place.get(0).getAddressLine(0)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the currently selected location as the one to be used in WeatherFragment.
     * @param tView the button triggering the return
     */
    private void returnLocation(final View tView) {
        MapResultViewModel.getFactory().create(MapResultViewModel.class).setResult(mMarker.getPosition());
        Navigation.findNavController(getView()).popBackStack();
    }
}
