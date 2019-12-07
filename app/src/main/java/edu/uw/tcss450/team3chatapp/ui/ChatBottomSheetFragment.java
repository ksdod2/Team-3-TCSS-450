package edu.uw.tcss450.team3chatapp.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass for options when interacting with a Chat.
 */
public class ChatBottomSheetFragment extends BottomSheetDialogFragment {

    /** The Chat being operated on */
    private Chat mChat;
    /** The MemberID of the current user */
    private int mMemberID;
    /** The JWT of the current user */
    private String mJWT;

    /** Required empty public constructor. */
    public ChatBottomSheetFragment() {}

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ChatBottomSheetFragmentArgs args =
                ChatBottomSheetFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mChat = args.getChat();
        mMemberID = args.getMemberID();
        mJWT = args.getJWT();
        // Inflate the layout for this fragment
        View rootView =
                inflater.inflate(R.layout.fragment_chat_bottom_sheet, container, false);

        Button leave = rootView.findViewById(R.id.btn_sheet_leave);
        leave.setOnClickListener(v -> leaveChat());

        Button invite = rootView.findViewById(R.id.btn_sheet_invite);
        invite.setOnClickListener(v -> invite());

        return rootView;
    }

    /** Leaves the chat associated with the menu. */
    private void leaveChat() {
        // Hit WS to leave chat
        Uri chatUri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_leave))
                .build();

        JSONObject chatInfo = new JSONObject();
        try {
            chatInfo.put("memberid", mMemberID);
            chatInfo.put(getString(R.string.keys_json_chats_id), mChat.getChatID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                .onPostExecute(s -> dismiss())
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();
    }

    /** Navigates to invite users from contacts to the chat associated with the menu. */
    private void invite() {
        ChatBottomSheetFragmentDirections.ActionNavChatBottomsheetToNavChatCreate invite =
                ChatBottomSheetFragmentDirections.actionNavChatBottomsheetToNavChatCreate(mMemberID, mJWT, mChat);
        NavHostFragment.findNavController(this).navigate(invite);
    }

}
