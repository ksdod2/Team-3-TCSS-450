package edu.uw.tcss450.team3chatapp;


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
import android.widget.EditText;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Custom back button functionality to exit app from this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Objects.requireNonNull(getActivity()).finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //Set onClickListener for Login Button:
        Button btnLogin = view.findViewById(R.id.btn_login_login);
        btnLogin.setOnClickListener(this::loginAttempt);

        Button btnRegister = view.findViewById(R.id.btn_login_register);
        btnRegister.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.nav_action_loginToRegister));
    }

    private void loginAttempt(final View tButton) {
        EditText etEmail = getView().findViewById(R.id.et_login_email);
        EditText etPass = getView().findViewById(R.id.et_login_password);

        boolean validated = validateLogin(etEmail, etPass);

        if (validated) {
            //TODO Login good on app side
        }
    }

    private boolean validateLogin(EditText etEmail, EditText etPass) {
        boolean result = true;
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ("".equals(email) || !email.matches(emailPattern)) {
            result = false;
            etEmail.setError("Invalid Email Address");
        } else if (pass.length() < 6) {
            result = false;
            etPass.setError("Invalid Password");
        }
        return result;
    }
}
