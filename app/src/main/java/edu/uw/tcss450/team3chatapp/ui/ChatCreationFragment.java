package edu.uw.tcss450.team3chatapp.ui;


import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.MyConnectionRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.model.ConnectionListViewModel;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

import static android.view.View.GONE;

/**
 * Fragment to set information and members in a chat room.
 * @author Kameron Dodd
 * @version 12/4/19
 */
public class ChatCreationFragment extends Fragment {

    /** The List of the current user's connections that can be added. */
    private ArrayList<Connection> mAvailableConnections = new ArrayList<>();
    /** The List of connections that will be added to the chat. */
    private ArrayList<Connection> mToAddConnections = new ArrayList<>();
    /** EditText for the name of the chat. */
    private EditText mNameInput;
    /** EditText for the description of the chat. */
    private EditText mDescInput;
    /** RecyclerView for contacts that can be added to the chat. */
    private RecyclerView mAvailableView;
    /** RecyclerView for contacts that will be added to the chat. */
    private RecyclerView mToAddView;
    /** Button to create a new chat. */
    private Button mCreate;
    /** Button to invite users to an existing chat. */
    private Button mInvite;
    /** The Chat being operated on. */
    private Chat mChat;
    /** The current user's MemberID. */
    private int mMemberID;
    /** The current user's JWT. */
    private String mJWT;
    /** The URI to use when hitting the WS. */
    private String mInviteURI;

    /** Required empty public constructor. */
    public ChatCreationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_creation, container, false);
        List<Connection> contacts =
                ConnectionListViewModel.getFactory().create(ConnectionListViewModel.class)
                        .getCurrentConnections().getValue();
        for(Connection connection: Objects.requireNonNull(contacts)) {
            if(connection.getRelation() == Connection.Relation.ACCEPTED)
                mAvailableConnections.add(connection);
        }

        ChatCreationFragmentArgs args =
                ChatCreationFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mMemberID = args.getMemberID();
        mJWT = args.getJWT();
        mChat = args.getChat();

        // views used for creating a new chat
        mNameInput = rootView.findViewById(R.id.et_chat_create_name);
        mDescInput = rootView.findViewById(R.id.et_chat_create_desc);
        mCreate = rootView.findViewById(R.id.btn_chat_create_make);

        // views used for inviting to an existing chat
        TextView mChatName = rootView.findViewById(R.id.tv_chat_invite_name);
        TextView mChatDescription = rootView.findViewById(R.id.tv_chat_invite_desc);
        mInvite = rootView.findViewById(R.id.btn_invite_invite);

        // Determine which views are needed based on creating new room or inviting to one
        if(mChat == null) {
            mChatName.setVisibility(GONE);
            mChatDescription.setVisibility(GONE);
            mInvite.setVisibility(GONE);
        } else {
            mNameInput.setVisibility(GONE);
            mDescInput.setVisibility(GONE);
            mCreate.setVisibility(GONE);
            mChatName.setText(mChat.getName());
            mChatDescription.setText(mChat.getDescription());
        }

        mAvailableView = rootView.findViewById(R.id.list_chat_create_contacts);
        mAvailableView.setAdapter(new MyConnectionRecyclerViewAdapter(mAvailableConnections, this::addToInvitees));

        mToAddView = rootView.findViewById(R.id.list_chat_create_toadd);
        mToAddView.setAdapter(new MyConnectionRecyclerViewAdapter(mToAddConnections, this::removeFromInvitees));

        mCreate.setOnClickListener(tView -> makeRoom());
        mInvite.setOnClickListener(v -> inviteUsers(mChat.getChatID()));

        mInviteURI = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_invite))
                .build().toString();

        return rootView;
    }

    /**
     * Adds a connection to the list of connections to invite to the new room.
     * @param tConnection the connection to add
     */
    private void addToInvitees(final Connection tConnection) {
        mAvailableConnections.remove(tConnection);
        mToAddConnections.add(tConnection);
        Objects.requireNonNull(mToAddView.getAdapter()).notifyDataSetChanged();
        Objects.requireNonNull(mAvailableView.getAdapter()).notifyDataSetChanged();
    }

    /**
     * Removes a connection from the list of connections to invite to the new room.
     * @param tConnection the connection to add
     */
    private void removeFromInvitees(final Connection tConnection) {
        mAvailableConnections.add(tConnection);
        mToAddConnections.remove(tConnection);
        Objects.requireNonNull(mToAddView.getAdapter()).notifyDataSetChanged();
        Objects.requireNonNull(mAvailableView.getAdapter()).notifyDataSetChanged();
    }

    /** Sends request to the web service to create a chat room with the given information. */
    private void makeRoom() {
        String chatName = mNameInput.getText().toString();
        String chatDesc = mDescInput.getText().toString();
        if (chatName.equals("")) {
            mNameInput.setError("Room must have a name.");
            return;
        }
        // Disable UI elements to prevent attempted double creation or change to list contents
        mCreate.setEnabled(false);
        ((MyConnectionRecyclerViewAdapter) Objects.requireNonNull(mAvailableView.getAdapter()))
                .setClickable(false);
        ((MyConnectionRecyclerViewAdapter) Objects.requireNonNull(mToAddView.getAdapter()))
                .setClickable(false);

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_create))
                .build();

        JSONObject body = new JSONObject();
        try {
            body.put(getString(R.string.keys_json_connections_memberid_int), mMemberID);
            body.put(getString(R.string.keys_json_chats_name), chatName);
            body.put(getString(R.string.keys_json_chats_description), chatDesc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), body)
                .onPostExecute(this::inviteFromCreate)
                .onCancelled(e -> {
                    Log.e("CHAT_CREATE", e);
                    mCreate.setEnabled(true);
                    mCreate.setError("Could not create room at this time.");
                })
                .addHeaderField("authorization", mJWT)
                .build().execute();
    }

    /**
     * Invites users that were queued to be invited following creation of the chat.
     * @param res the response from the WS
     */
    private void inviteFromCreate(final String res) {
        try {
            // Get chat id from creation response, then move to invite using it
            JSONObject root = new JSONObject(res);
            if(root.getBoolean("success")) {
                // If no other users to be added, immediately return
                if (!mToAddConnections.isEmpty())
                    inviteUsers(root.getInt(getString(R.string.keys_json_chats_id)));
                else {
                    Toast.makeText(getActivity(), "Chatroom created.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(Objects.requireNonNull(getView())).popBackStack();
                }
            } else {
                Log.e("ERROR", "Chat creation error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invites all users whose IDs are contained in mToAddConnections to the given chat.
     * @param tChatID the ID of the chat to invite to
     */
    private void inviteUsers(final int tChatID) {

        // Check if we are creating a room and have no other invitees or are just inviting
        if(mToAddConnections.isEmpty()) {
            mCreate.setError("Please select users to add.");
            mInvite.setError("Please select users to add.");
            return;
        }
        mCreate.setEnabled(false);
        mInvite.setEnabled(false);
        JSONObject body = new JSONObject();
        try {
            ArrayList<Integer> memberIDs = new ArrayList<>();
            for (Connection c : mToAddConnections) { // Get all memberIDs from list of invitees
                memberIDs.add(c.getMemberID());
            }
            body.put(getString(R.string.keys_json_connections_sender_int), mMemberID);
            body.put(getString(R.string.keys_json_chats_id), tChatID);
            body.put(getString(R.string.keys_json_chats_invitees), new JSONArray(memberIDs));

            new SendPostAsyncTask.Builder(mInviteURI, body)
                    .onPostExecute(this::onInvitePostExecute)
                    .onCancelled(e -> {
                        Log.e("CHAT_CREATE", e);
                        mCreate.setEnabled(true);
                        mCreate.setError("Could not invite users at this time.");
                    })
                    .addHeaderField("authorization", mJWT)
                    .build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Performs operations following WS response when inviting.
     * @param res the response from the WS
     */
    private void onInvitePostExecute(final String res) {
        try {
            JSONObject root = new JSONObject(res);
            if(root.getBoolean("success")) {
                if(mNameInput.getText().toString().equals(""))
                    Toast.makeText(getActivity(), "Users successfully invited.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Chatroom created and users successfully invited.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(Objects.requireNonNull(getView())).popBackStack();
                return;
            } else {
                Log.e("ERROR", "Chat creation error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
        // Reenable UI elements to allow retries
        mCreate.setEnabled(true);
        mInvite.setEnabled(true);
        ((MyConnectionRecyclerViewAdapter) Objects.requireNonNull(mAvailableView.getAdapter()))
                .setClickable(true);
        ((MyConnectionRecyclerViewAdapter) Objects.requireNonNull(mToAddView.getAdapter()))
                .setClickable(true);
        mCreate.setError("Could not invite at this time, please try again later.");
        mInvite.setError("Could not invite at this time, please try again later.");
    }

}
