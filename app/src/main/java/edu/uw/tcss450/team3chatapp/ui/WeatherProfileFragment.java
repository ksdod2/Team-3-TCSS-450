package edu.uw.tcss450.team3chatapp.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.team3chatapp.MyWeatherProfileRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.model.WeatherProfileViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WeatherProfileFragment extends Fragment {
    private ArrayList<WeatherProfile> mProfiles = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherprofile_list, container, false);

        // Begin observing the ViewModel
        WeatherProfileViewModel model = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);
        model.getAllWeatherProfiles().observe(this, this::updateRecyclerView);

        mProfiles.addAll(model.getAllWeatherProfiles().getValue());

        RecyclerView profiles = view.findViewById(R.id.list_weatherprofile);
        profiles.setAdapter(new MyWeatherProfileRecyclerViewAdapter(mProfiles, this::displayMenu));

        return view;
    }

    /**
     * Updates the RecyclerView of WeatherProfiles upon changes to the ViewModel.
     * @param tProfiles the list of profiles to update with
     */
    private void updateRecyclerView(List<WeatherProfile> tProfiles) {
        mProfiles.clear();
        mProfiles.addAll(tProfiles);

        RecyclerView profileList = getView().findViewById(R.id.list_weatherprofile);
        profileList.getAdapter().notifyDataSetChanged();
    }

    /**
     * Displays the menu for options involving a WeatherProfile when selected
     * @param tProfile the profile to give options for
     */
    private void displayMenu(final WeatherProfile tProfile) {
        WeatherProfileFragmentDirections.ActionNavWeatherprofilesToNavWeatherprofileBottomsheet menu =
                WeatherProfileFragmentDirections.actionNavWeatherprofilesToNavWeatherprofileBottomsheet(tProfile);
        Navigation.findNavController(getView()).navigate(menu);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(WeatherProfile item);
    }
}
