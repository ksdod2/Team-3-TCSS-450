package edu.uw.tcss450.team3chatapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import edu.uw.tcss450.team3chatapp.MyChatMessageRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.utils.PushReceiver;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatMessageFragment extends Fragment {

    private RecyclerView mChatWindow;
    private ArrayList<ChatMessage> mMessages;
    private int mMemberID;
    private int mChatID;
    private String mJWT;
    private String mSendUrl;
    private EditText mSendField;
    private Button mSendButton;

    private PushMessageReceiver mPushMessageReceiver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatMessageFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReceiver == null) {
            mPushMessageReceiver = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReceiver, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReceiver != null){
            getActivity().unregisterReceiver(mPushMessageReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatmessage_list, container, false);

        ChatMessageFragmentArgs args = ChatMessageFragmentArgs.fromBundle(getArguments());
        mMemberID = args.getMemberID();
        mChatID = args.getChatID();
        mJWT = args.getJWT();
        mMessages = new ArrayList(Arrays.asList(args.getMessages()));
        Collections.reverse(mMessages);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(args.getChatname());

        mChatWindow = view.findViewById(R.id.list_chatroom);
        // Clicking on an individual message does nothing
        mChatWindow.setAdapter(new MyChatMessageRecyclerViewAdapter(mMessages, null));
        // Automatically scroll to most recent messages
        mChatWindow.scrollToPosition(mMessages.size() - 1);

        mSendField = view.findViewById(R.id.et_chatroom_send);
        mSendButton = view.findViewById(R.id.btn_chatroom_send);
        mSendButton.setOnClickListener(this::sendMessage);

        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_send))
                .build()
                .toString();

        return view;
    }

    private void sendMessage(final View tView) {
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
            Log.e("ERROR!", e.getMessage());
        }

        new SendPostAsyncTask.Builder(mSendUrl, body)
                .onPostExecute(this::handleSendOnPostExecute)
                .onCancelled(error -> Log.e("CHAT_MESSAGE_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();

        mSendButton.setEnabled(false);
    }

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
            Log.e("ERROR!", e.getMessage());
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

    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {
                try {
                    // Immediately build out new chat message to add to RecyclerView
                    JSONObject body = new JSONObject(intent.getStringExtra("MESSAGE"));
                    ChatMessage newMessage = new ChatMessage(body.getInt("memberid")  == mMemberID,
                            intent.getStringExtra("SENDER"),
                            body.getString("stamp"),
                            body.getString("text"));
                    mMessages.add(newMessage);
                    // Update chat and scroll to newest message
                    mChatWindow.getAdapter().notifyDataSetChanged();
                    mChatWindow.scrollToPosition(mMessages.size() - 1);
                } catch (JSONException e) {
                    Log.e("CHAT_PUSH", e.getMessage());
                }
            }
        }
    }
}
