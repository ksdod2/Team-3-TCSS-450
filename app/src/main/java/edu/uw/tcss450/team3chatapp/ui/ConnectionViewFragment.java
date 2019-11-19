package edu.uw.tcss450.team3chatapp.ui;


import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * Fragment for displaying all a user's connections, with ability to move to searching for users
 * and viewing information about a given contact.
 * @author Kameron Dodd
 * @version 11/18/19
 */
public class ConnectionViewFragment extends Fragment {
    private int mMemberID;
    private String mJWT;
    private TextView mStatus;
    private Button mAccept;
    private Button mReject;
    private Button mSend;
    private Button mRemove;
    private Connection mConn;

    public ConnectionViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection_view, container, false);

        mConn = ConnectionViewFragmentArgs.fromBundle(getArguments()).getConnectionDetails();
        mMemberID = ConnectionViewFragmentArgs.fromBundle(getArguments()).getMemberID();
        mJWT = ConnectionViewFragmentArgs.fromBundle(getArguments()).getJWT();

        // All buttons are GONE by default and made visible as needed
        mAccept = view.findViewById(R.id.btn_connection_view_accept);
        mAccept.setVisibility(View.GONE);
        mReject = view.findViewById(R.id.btn_connection_view_reject);
        mReject.setVisibility(View.GONE);
        mSend = view.findViewById(R.id.btn_connection_view_send);
        mSend.setVisibility(View.GONE);
        mRemove = view.findViewById(R.id.btn_connection_view_remove);
        mRemove.setVisibility(View.GONE);
        mStatus = view.findViewById(R.id.tv_connection_view_status);

        ((TextView) view.findViewById(R.id.tv_connection_view_name)).setText(mConn.getFirstName() + " " + mConn.getLastName());
        ((TextView) view.findViewById(R.id.tv_connection_view_username)).setText(mConn.getUsername());
        ((TextView) view.findViewById(R.id.tv_connection_view_email)).setText(mConn.getEmail());

        // Make needed buttons visible based on connection details
        if(mConn.getRelation() == Connection.Relation.ACCEPTED) { // Existing
            mRemove.setVisibility(View.VISIBLE);
            mRemove.setOnClickListener(this::removeConnection);
            mStatus.setText(R.string.tv_status_accepted);
        } else if (mConn.getRelation() == Connection.Relation.UNACCEPTED && mConn.amSender()) { // Sent
            mRemove.setVisibility(View.VISIBLE);
            mRemove.setOnClickListener(this::removeConnection);
            mStatus.setText(R.string.tv_status_sent);
        } else if(mConn.getRelation() == Connection.Relation.UNACCEPTED && !mConn.amSender()) { // Received
            mAccept.setVisibility(View.VISIBLE);
            mReject.setVisibility(View.VISIBLE);
            mAccept.setOnClickListener(this::acceptConnection);
            mReject.setOnClickListener(this::removeConnection);
            mStatus.setText(R.string.tv_status_received);
        } else { // No relation, used when searching
            mSend.setVisibility(View.VISIBLE);
            mSend.setOnClickListener(this::sendConnection);
            mStatus.setText(R.string.tv_status_sendnew);
        }

        return view;
    }

    /**
     * Method used to accept a connection request.
     * @param view The button used to initiate the request
     */
    private void acceptConnection(final View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_connections_confirm))
                .build();

        JSONObject body = new JSONObject();
        try {
            if(mConn.amSender()) {
                body.put(getString(R.string.keys_json_connections_sender_int), mMemberID);
                body.put(getString(R.string.keys_json_connections_receiver_int), mConn.getMemberID());
            } else {
                body.put(getString(R.string.keys_json_connections_sender_int), mConn.getMemberID());
                body.put(getString(R.string.keys_json_connections_receiver_int), mMemberID);
            }
            body.put(getString(R.string.keys_json_connections_accepted_str), getString(R.string.keys_json_connections_accepted_yes_str));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }

        new SendPostAsyncTask.Builder(uri.toString(), body)
                .onPostExecute(this::returnFromChange)
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();

        mAccept.setEnabled(false);
        mReject.setEnabled(false);
    }

    /**
     * Method used to either remove an existing connection or reject a connection request.
     * @param view The button used to initiate the request
     */
    private void removeConnection(final View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_connections_confirm))
                .build();

        JSONObject body = new JSONObject();
        try {
            if(mConn.amSender()) {
                body.put(getString(R.string.keys_json_connections_sender_int), mMemberID);
                body.put(getString(R.string.keys_json_connections_receiver_int), mConn.getMemberID());
            } else {
                body.put(getString(R.string.keys_json_connections_sender_int), mConn.getMemberID());
                body.put(getString(R.string.keys_json_connections_receiver_int), mMemberID);
            }
            body.put(getString(R.string.keys_json_connections_accepted_str), getString(R.string.keys_json_connections_accepted_no_str));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }

        new SendPostAsyncTask.Builder(uri.toString(), body)
                .onPostExecute(this::returnFromChange)
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();

        view.setEnabled(false);
    }


    /**
     * Method used to either remove an existing connection or reject a connection request.
     * @param view The button used to initiate the request
     */
    private void sendConnection(final View view) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_connections_send))
                .build();

        JSONObject body = new JSONObject();
        try {
            body.put(getString(R.string.keys_json_connections_sender_int), mMemberID);
            body.put(getString(R.string.keys_json_connections_receiver_int), mConn.getMemberID());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }

        new SendPostAsyncTask.Builder(uri.toString(), body)
                .onPostExecute(this::returnFromChange)
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();

        view.setEnabled(false);
    }

    /**
     * Method to either inform user of server error or return to connections home upon making a
     * change to the given connection or invitation.
     * @param result The JSON returned by the server
     */
    private void returnFromChange(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if(root.getBoolean("success")) {
                Navigation.findNavController(getView()).popBackStack();
            } else {
                Log.e("ERROR", "Connection change error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
        mRemove.setEnabled(true);
        mRemove.setError("Could not remove at this time, please try again later.");
        mReject.setEnabled(true);
        mReject.setError("Could not remove at this time, please try again later.");
        mAccept.setEnabled(true);
        mSend.setEnabled(true);
        mSend.setError("Could not send invitation at this time, please try again later.");
    }
}
