package edu.uw.tcss450.team3chatapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.uw.tcss450.team3chatapp.model.MyChatMessageRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.ChatListViewModel;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.utils.PushReceiver;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A fragment representing a list of ChatMessages.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatMessageFragment extends Fragment {

    /** The RecyclerView that contains the ChatMessages. */
    private RecyclerView mChatWindow;
    /** The List of ChatMessages. */
    private ArrayList<ChatMessage> mMessages;
    /** The current user's MemberID. */
    private int mMemberID;
    /** The current user's JWT. */
    private String mJWT;
    /** The current chat's ID. */
    private int mChatID;
    /** The Url to use when sending messages. */
    private String mSendUrl;
    /** Whether the current chat is favorited. */
    private boolean amFav;
    /** EditText for the message to send. */
    private EditText mSendField;
    /** The button to send messages with */
    private Button mSendButton;
    /** A receiver for push notifications from PushReceiver. */
    private PushMessageReceiver mPushMessageReceiver;

    /** Required empty public constructor. */
    public ChatMessageFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReceiver == null) {
            mPushMessageReceiver = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        Objects.requireNonNull(getActivity()).registerReceiver(mPushMessageReceiver, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReceiver != null){
            Objects.requireNonNull(getActivity()).unregisterReceiver(mPushMessageReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatmessage_list, container, false);

        ChatMessageFragmentArgs args = ChatMessageFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mMemberID = args.getMemberID();
        mChatID = args.getChatID();
        mJWT = args.getJWT();
        amFav = args.getFavorite();
        mMessages = new ArrayList<>(Arrays.asList(args.getMessages()));
        Collections.reverse(mMessages);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).setTitle(args.getChatname());

        mChatWindow = view.findViewById(R.id.list_chatroom);
        // Clicking on an individual message does nothing
        mChatWindow.setAdapter(new MyChatMessageRecyclerViewAdapter(mMessages, null));
        // Automatically scroll to most recent messages
        mChatWindow.scrollToPosition(mMessages.size() - 1);

        mSendField = view.findViewById(R.id.et_chatroom_send);
        mSendButton = view.findViewById(R.id.btn_chatroom_send);
        mSendButton.setOnClickListener(v -> sendMessage());

        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_send))
                .build()
                .toString();
        // Prepare to add option to existing menu by flagging need to alter it
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        String label = amFav? "Unfavorite" : "Favorite";
        menu.add(label);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Ensure the item selected was the one added by the fragment and not an activity's item
        if(item.getTitle() != null && (item.getTitle().equals("Favorite") || item.getTitle().equals("Unfavorite"))) {
            Set<String> favs = new HashSet<>();
            SharedPreferences prefs = Objects.requireNonNull(getActivity())
                    .getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
            if (!prefs.contains(getString(R.string.keys_prefs_favorites))) {
                favs.add("" + mChatID);
                prefs.edit().putStringSet(getString(R.string.keys_prefs_favorites), favs).apply();
                item.setTitle("Unfavorite");
                ChatListViewModel.getFactory().create(ChatListViewModel.class)
                        .setFavorite(mChatID, true);
            } else {
                favs = prefs.getStringSet(getString(R.string.keys_prefs_favorites), null);
                // Either set favorite or remove it if already exists
                if(!Objects.requireNonNull(favs).contains("" + mChatID)) {
                    favs.add("" + mChatID);
                    item.setTitle("Unfavorite");
                    ChatListViewModel.getFactory().create(ChatListViewModel.class)
                            .setFavorite(mChatID, true);
                }
                else {
                    favs.remove("" + mChatID);
                    item.setTitle("Favorite");
                    ChatListViewModel.getFactory().create(ChatListViewModel.class)
                            .setFavorite(mChatID, false);
                }
                prefs.edit().putStringSet(getString(R.string.keys_prefs_favorites), favs).apply();
            }
            Log.i("STORED CHATS", favs.toString());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Hits the WS to send the entered message. */
    private void sendMessage() {
        String msg = mSendField.getText().toString();
        if(msg.equals("")) { // Don't let users send blank messages and clog the room
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put(getString(R.string.keys_json_connections_memberid_int), mMemberID);
            body.put(getString(R.string.keys_json_chatmessage_message), msg);
            body.put(getString(R.string.keys_json_chats_id), mChatID);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }

        new SendPostAsyncTask.Builder(mSendUrl, body)
                .onPostExecute(this::handleSendOnPostExecute)
                .onCancelled(error -> Log.e("CHAT_MESSAGE_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();

        mSendButton.setEnabled(false);
    }

    /**
     * Performs operations following WS response when sending a message.
     * @param result the response from the WS
     */
    private void handleSendOnPostExecute(final String result) {
        mSendButton.setEnabled(true);
        try {
            JSONObject root = new JSONObject(result);
            if(root.has(getString(R.string.keys_json_login_success_bool)) && root.getBoolean(getString(R.string.keys_json_login_success_bool))) {
                // Sending was successful, blank chat field
                mSendField.setText("");
            } else {
                Log.e("CHAT_ERR", result);
                mSendField.setError("Could not send message, please try again");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
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
        void onListFragmentInteraction(ChatMessage item);
    }

    /** A PushMessageReceiver for handling chat messages in the foreground. */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {
                try {
                    // Immediately build out new chat message to add to RecyclerView
                    JSONObject body = new JSONObject(Objects.requireNonNull(intent.getStringExtra("MESSAGE")));
                    ChatMessage newMessage = new ChatMessage(body.getInt("memberid")  == mMemberID,
                            intent.getStringExtra("SENDER"),
                            body.getString("stamp"),
                            body.getString("text"));
                    mMessages.add(newMessage);
                    // Update chat and scroll to newest message
                    Objects.requireNonNull(mChatWindow.getAdapter()).notifyDataSetChanged();
                    mChatWindow.scrollToPosition(mMessages.size() - 1);
                    // Make sure this chat does not mark as having unread messages while in it
                    ChatListViewModel.getFactory().create(ChatListViewModel.class)
                            .setUnread(mChatID, false);
                } catch (JSONException e) {
                    Log.e("CHAT_PUSH", Objects.requireNonNull(e.getMessage()));
                }
            }
        }
    }
}
