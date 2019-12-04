package edu.uw.tcss450.team3chatapp.ui;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherProfileBottomSheetFragment extends BottomSheetDialogFragment {
    private WeatherProfile mProfile;


    public WeatherProfileBottomSheetFragment() { /* Required empty public constructor */ }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_weather_profile_bottom_sheet, container, false);

        WeatherProfileBottomSheetFragmentArgs args = WeatherProfileBottomSheetFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mProfile = args.getWeatherProfile();

        root.findViewById(R.id.btn_profilemenu_remove).setOnClickListener(this::removeProfile);
        root.findViewById(R.id.btn_profilemenu_use).setOnClickListener(this::useProfile);

        return root;
    }

    /**
     * Removes the selected location from the list of stored locations.
     * @param tView the view triggering the event
     */
    private void removeProfile(final View tView) {
        /* TODO: Whatever steps need to be taken for removing the location */

        // Close bottom menu
        dismiss();
    }

    /**
     * Confirms using the selected location as the one for other purposes.
     * @param tView the view triggering the event
     */
    private void useProfile(final View tView) {
        /* TODO: Store the selected profile for use. To maintain backstack, should probably use ViewModel. */

        // Show user confirmation of selection, then close bottom menu
        Toast.makeText(getContext(), "Loaded " + mProfile.getLocation().toString(), Toast.LENGTH_SHORT).show();
        dismiss();
    }

}
