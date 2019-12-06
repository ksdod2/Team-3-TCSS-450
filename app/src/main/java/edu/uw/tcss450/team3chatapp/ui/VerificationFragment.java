package edu.uw.tcss450.team3chatapp.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;

/**
 * A simple {@link Fragment} subclass to display instructions for the user after registering.
 */
public class VerificationFragment extends Fragment {

    /** Required empty public constructor. */
    public VerificationFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Custom back button functionality to exit app from this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                navigateToLogin(getView());
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //Set OK button to go back to login screen
        Button btnOk = Objects.requireNonNull(getView()).findViewById(R.id.btn_verification_ok);
        btnOk.setOnClickListener(this::navigateToLogin);
    }

    /**
     * Returns to the login screen.
     * @param view the button triggering the navigation
     */
    private void navigateToLogin(final View view) {
        Navigation.findNavController(view).navigate(R.id.nav_action_verificationToLogin);
    }
}
