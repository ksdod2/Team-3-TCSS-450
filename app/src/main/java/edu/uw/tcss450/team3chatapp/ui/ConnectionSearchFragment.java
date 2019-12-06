package edu.uw.tcss450.team3chatapp.ui;


import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.MyConnectionRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass for searching for new connections.
 */
public class ConnectionSearchFragment extends Fragment {

    /** The MemberID of the current user */
    private int mMemberID;
    /** The JWT of the current user */
    private String mJWT;

    /** EditText for entering a user's first name. */
    private EditText mFirstNameField;
    /** EditText for entering a user's last name. */
    private EditText mLastNameField;
    /** EditText for entering a user's username. */
    private EditText mUsernameField;
    /** EditText for entering a user's email. */
    private EditText mEmailField;
    /** The button to initiate searches with. */
    private Button mSearchButton;
    /** The RecyclerView to display connections resulting from the search. */
    private RecyclerView searchResults;
    /** The list of resulting Connections from a search. */
    final private ArrayList<Connection> results = new ArrayList<>();
    /** The background to use with the loading screen layout. */
    private View mWaitScreen;
    /** The loading bar for the loading screen layout. */
    private View mWaitBar;

    /** Required empty public constructor. */
    public ConnectionSearchFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connection_search, container, false);

        ConnectionSearchFragmentArgs args = ConnectionSearchFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mMemberID = args.getMemberID();
        mJWT = args.getJWT();

        mFirstNameField = rootView.findViewById(R.id.et_connection_search_firstname);
        mLastNameField = rootView.findViewById(R.id.et_connection_search_lastname);
        mUsernameField = rootView.findViewById(R.id.et_connection_search_username);
        mEmailField = rootView.findViewById(R.id.et_connection_search_email);
        mSearchButton = rootView.findViewById(R.id.btn_connection_search_search);
        mSearchButton.setOnClickListener(v -> doSearch());
        mWaitScreen = rootView.findViewById(R.id.layout_connection_search_wait);
        mWaitBar = rootView.findViewById(R.id.pb_search);

        searchResults = rootView.findViewById(R.id.list_connection_searchresults);
        // Initialize RecyclerView with empty dataset to make sure it is properly created
        searchResults.setAdapter(new MyConnectionRecyclerViewAdapter(results, this::displayConnection));

        return rootView;
    }

    /**
     * Makes a search request to the backend API using entered information. Users must input at least
     * one valid set of search information, but more can be combined for refinement of results.
     */
    private void doSearch() {
        // Make sure at least one search criteria has been entered
        boolean nameSearch = (!mFirstNameField.getText().toString().equals("")
                && !mLastNameField.getText().toString().equals(""));
        boolean usernameSearch = !mUsernameField.getText().toString().equals("");
        boolean emailSearch = !mEmailField.getText().toString().equals("");
        if (nameSearch || usernameSearch || emailSearch) {

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_connections))
                    .appendPath(getString(R.string.ep_connections_search))
                    .build();

            JSONObject body = new JSONObject();
            try {
                body.put(getString(R.string.keys_json_connections_memberid_int), mMemberID);
                if (nameSearch) {
                    body.put(getString(R.string.keys_json_connections_firstname_str), mFirstNameField.getText().toString());
                    body.put(getString(R.string.keys_json_connections_lastname_str), mLastNameField.getText().toString());
                }
                if (usernameSearch)
                    body.put(getString(R.string.keys_json_connections_username_str), mUsernameField.getText().toString());
                if (emailSearch)
                    body.put(getString(R.string.keys_json_connections_email_str), mEmailField.getText().toString());

            } catch (JSONException e) {
                Log.e("SEARCH_ERR", Objects.requireNonNull(e.getMessage()));
            }

            new SendPostAsyncTask.Builder(uri.toString(), body)
                    .onPostExecute(this::doSearchOnPostExecute)
                    .onCancelled(error -> Log.e("SEARCH_ERR", error))
                    .addHeaderField("authorization", mJWT)
                    .build().execute();

            // Disable buttons and display loading screen assets
            mSearchButton.setEnabled(false);
            mWaitScreen.setVisibility(View.VISIBLE);
            mWaitBar.setVisibility(View.VISIBLE);

        } else {
            mFirstNameField.setError(getString(R.string.et_connectionsearch_general_error));
        }
    }

    /**
     * Performs operations following WS response when getting search results.
     * @param result the response from the WS
     */
    private void doSearchOnPostExecute (final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections_search_rows))) {
                JSONArray data = root.getJSONArray( getString(R.string.keys_json_connections_search_rows));
                // Discard old results from view
                results.clear();
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonConn = data.getJSONObject(i);

                    results.add(new Connection(jsonConn.getInt(getString(R.string.keys_json_connections_memberid_int)),
                            jsonConn.getString(getString(R.string.keys_json_connections_firstname_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_lastname_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_username_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_email_str)),
                            jsonConn.optInt(getString(R.string.keys_json_connections_verified_int), -1),
                            false));
                }
                Objects.requireNonNull(searchResults.getAdapter()).notifyDataSetChanged();

            } else {
                Log.e("ERROR!", "Database Error");
                mSearchButton.setError("Unable to search at this time, please try again later.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }

        // Return screen to fully visible and usable
        mSearchButton.setEnabled(true);
        mWaitScreen.setVisibility(View.GONE);
        mWaitBar.setVisibility(View.GONE);
    }

    /**
     * Moves to display a given connection.
     * @param tConn the Connection to display
     */
    private void displayConnection(final Connection tConn) {
        NavController nc = Navigation.findNavController(Objects.requireNonNull(getView()));
        if (Objects.requireNonNull(nc.getCurrentDestination())
                .getId() != R.id.nav_connectionsearch) { // Ensure back button doesn't break nav
            nc.navigateUp();
        }
        // Pass connection as arg, allow fragment to determine layout based on that
        ConnectionSearchFragmentDirections.ActionConnectionSearchFragmentToNavConnectionview connectionView =
                ConnectionSearchFragmentDirections.actionConnectionSearchFragmentToNavConnectionview(tConn, mMemberID, mJWT);
        nc.navigate(connectionView);
    }

}
