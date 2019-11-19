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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import edu.uw.tcss450.team3chatapp.MyChatRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatFragment extends Fragment {

    private int mMemberID;
    private String mJWT;
    private int currentChat;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(getArguments());
        mMemberID = args.getMemberID();
        mJWT = args.getJWT();

        RecyclerView recyclerView = view.findViewById(R.id.list_chatroom);
        ArrayList<Chat> rooms = new ArrayList(Arrays.asList(args.getRooms()));
        recyclerView.setAdapter(new MyChatRecyclerViewAdapter(rooms, this::displayChat, this::displayMenu));

        return view;
    }

    /**
     * Moves to the appropriate fragment to display a chatroom.
     * @param tChat the chat to display content from
     */
    private void displayChat(final Chat tChat) {
        // Get all the prior messages of the chat asynchronously
        Uri chatUri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_getcontents))
                .build();

        JSONObject chatInfo = new JSONObject();
        try {
            chatInfo.put("chatid", tChat.getChatID());
            currentChat = tChat.getChatID();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                .onPostExecute(this::handleDisplayChatOnPostExecute)
                .onCancelled(error -> Log.e("CHAT_ROOM_NAV", error))
                .addHeaderField("authorization", mJWT)
                .build().execute();
    }

    /**
     * Displays the bottom modal menu to use with chat options.
     * @param tChat the chat for which options should apply to
     */
    private void displayMenu(final Chat tChat) {
        ChatBottomSheetFragment dialog = new ChatBottomSheetFragment();
        Bundle chatArg = new Bundle();
        chatArg.putSerializable("chat", tChat);
        chatArg.putInt("memberid", mMemberID);
        chatArg.putString("jwt", mJWT);
        dialog.setArguments(chatArg);
        dialog.show(getFragmentManager(), null);
    }

    private void handleDisplayChatOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats_messages))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_chats_messages));
                ChatMessage[] messages = new ChatMessage[data.length()];
                for(int i = 0; i < data.length(); i++) {
                    JSONObject message = data.getJSONObject(i);

                    messages[i] = new ChatMessage(message.getString(getString(R.string.keys_json_chatmessage_sender)),
                            message.getString(getString(R.string.keys_json_chatmessage_timestamp)),
                            message.getString(getString(R.string.keys_json_chatmessage_message)));
                }

                ChatFragmentDirections.ActionChatFragmentToChatMessageFragment chatroom =
                        ChatFragmentDirections.actionChatFragmentToChatMessageFragment(messages, mJWT, mMemberID, currentChat);

                NavController nc = Navigation.findNavController(getView());
                if (nc.getCurrentDestination().getId() != R.id.nav_chats) // Ensure back button doesn't break nav
                    nc.navigateUp();
                nc.navigate(chatroom);

            } else {
                Log.e("ERROR!", "Couldn't get messages from chat");
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
        void onListFragmentInteraction(Chat item);
    }
}
