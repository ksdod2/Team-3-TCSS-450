/*General TODOs:
TODO make layout "scroll" up as user clicks fields lower on screen so that they don't have to exit keyboard every time
TODO Add javadox on existing code so we don't have to document entire app at once
 */

package edu.uw.tcss450.team3chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import edu.uw.tcss450.team3chatapp.model.Credentials;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

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
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password)) &&
                prefs.contains(getString(R.string.keys_prefs_stay_logged_in))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");
            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.et_login_email);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.et_login_password);
            passwordEdit.setText(password);

            // Button not needed to actually log in
            loginAttempt(null);
        }
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
        EditText etEmail = Objects.requireNonNull(getView()).findViewById(R.id.et_login_email);
        EditText etPass = getView().findViewById(R.id.et_login_password);

        boolean validated = validateLogin(etEmail, etPass);

        if (validated) {
            Credentials credentials = new Credentials
                    .Builder(etEmail.getText().toString().toLowerCase().trim(), etPass.getText().toString())
                    .build();

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_login))
                    .build();

            JSONObject msg = credentials.asJSONObject();

            new SendPostAsyncTask
                    .Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRegisterPre)
                    .onPostExecute(this::handleRegisterPost)
                    .onCancelled(this::handleRegisterCancelled)
                    .build()
                    .execute();
        }
    }

    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_stay_logged_in), "true").apply();
    }

    private void handleRegisterCancelled(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private void handleRegisterPre() {
        getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.btn_login_login).setEnabled(false);
        getActivity().findViewById(R.id.btn_login_register).setEnabled(false);
    }

    private void handleRegisterPost(String result) {
        try {
            JSONObject root = new JSONObject(result);
            if(root.has(getString(R.string.keys_json_login_success_bool))) {
                boolean success = root.getBoolean(getString(R.string.keys_json_register_success_bool));
                if(success) {
                    Credentials userCreds = new Credentials.Builder(
                            ((EditText) getActivity().findViewById(R.id.et_login_email)).getText().toString(),
                            ((EditText) getActivity().findViewById(R.id.et_login_password)).getText().toString())
                            .addUsername(root.getString("username"))
                            .build();

                    SharedPreferences prefs = getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
                    if(((Switch) getActivity().findViewById(R.id.swt_login_stayloggedin)).isChecked()) {
                        saveCredentials(userCreds);
                    }

                    LoginFragmentDirections.NavActionLoginToHome homeActivity =
                            LoginFragmentDirections.navActionLoginToHome(userCreds);
                    homeActivity.setJwt(root.getString("token"));
                    homeActivity.setUserId((Integer) root.get("memberid"));
                    Navigation
                            .findNavController(Objects.requireNonNull(getView()))
                            .navigate(homeActivity);

                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    //TODO better error here too?
                    Log.e("ERROR_LOGIN", "Unsuccessful");
                    ((EditText) Objects
                            .requireNonNull(getView())
                            .findViewById(R.id.et_login_email))
                            .setError("Unable to login.\nPlease check credentials and retry.");
                }
                getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
                getActivity().findViewById(R.id.btn_login_login).setEnabled(true);
                getActivity().findViewById(R.id.btn_login_register).setEnabled(true);
            } else {
                Log.e("ERROR", "Unsuccessful");
            }
        } catch(JSONException error) {
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + error.getMessage());

            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
            getActivity().findViewById(R.id.btn_login_login).setEnabled(true);
            getActivity().findViewById(R.id.btn_login_register).setEnabled(true);

            ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.et_login_email)).setError("Unable to login.\nPlease try again later");
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
