package edu.uw.tcss450.team3chatapp.ui;


import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatBottomSheetFragment extends BottomSheetDialogFragment {
    private Chat mChat;
    private int mMemberID;
    private String mJWT;


    public ChatBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mChat = (Chat) getArguments().getSerializable("chat");
        mMemberID = getArguments().getInt("memberid");
        mJWT = getArguments().getString("jwt");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_bottom_sheet, container, false);

        Button leave = rootView.findViewById(R.id.btn_sheet_leave);
        leave.setOnClickListener(view -> {
            Log.i("CLICKED", mChat.getName());
        });

        return rootView;
    }

    /**
     * Leaves the chat associated with the menu.
     * @param tView the view containing the chat
     */
    private void leaveChat(final View tView) {
        Uri chatUri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_confirm))
                .build();

        JSONObject chatInfo = new JSONObject();
        try {
            chatInfo.put("memberid", mMemberID);
            chatInfo.put(getString(R.string.keys_json_chats_id), mChat.getChatID());
            chatInfo.put(getString(R.string.keys_json_connections_verified_int), 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                .onPostExecute(s -> dismiss())
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();
    }

    /**
     * Navigates to invite users from contacts to the chat associated with the menu.
     * @param tView the view containing the chat
     */
    private void invite(final View tView) {

    }

}
