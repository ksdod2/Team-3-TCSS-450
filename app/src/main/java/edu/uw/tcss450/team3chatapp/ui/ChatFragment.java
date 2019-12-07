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
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.MyChatRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.ChatListViewModel;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

/**
 * A fragment representing a list of Chats.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatFragment extends Fragment {

    /** The current user's MemberID. */
    private int mMemberID;
    /** The current user's JWT. */
    private String mJWT;
    /** The chat which has currently been selected. */
    private Chat currentChat;
    /** The list of Chats. */
    private ArrayList<Chat> mRooms = new ArrayList<>();

    /** Required empty public constructor. */
    public ChatFragment() {}

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));
        mMemberID = args.getMemberID();
        mJWT = args.getJWT();

        // Add this fragment as an observer to the chat ViewModel
        ChatListViewModel model = ChatListViewModel.getFactory().create(ChatListViewModel.class);
        model.getCurrentChats().observe(this, this::updateRecyclerView);
        mRooms.addAll(Objects.requireNonNull(model.getCurrentChats().getValue()));

        RecyclerView roomList = view.findViewById(R.id.list_chatroom);
        roomList.setAdapter(new MyChatRecyclerViewAdapter(mRooms, this::displayChat, this::displayMenu));

        view.findViewById(R.id.btn_chat_create).setOnClickListener(v -> {
            ChatFragmentDirections.ActionNavChatsToNavChatCreate create =
                    ChatFragmentDirections.actionNavChatsToNavChatCreate(mMemberID, mJWT, null);
            Navigation.findNavController(v).navigate(create);
        });

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
            currentChat = tChat;
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
     * Updates the RecyclerView of Chats upon notification of the Chat ViewModel being updated.
     * @param tChats the list of chats to update with
     */
    private void updateRecyclerView(final List<Chat> tChats) {
        mRooms.clear();
        mRooms.addAll(tChats);

        RecyclerView roomList = Objects.requireNonNull(getView()).findViewById(R.id.list_chatroom);
        Objects.requireNonNull(roomList.getAdapter()).notifyDataSetChanged();
    }

    /**
     * Displays the bottom modal menu to use with chat options.
     * @param tChat the chat for which options should apply to
     */
    private void displayMenu(final Chat tChat) {
        ChatFragmentDirections.ActionNavChatsToNavChatBottomsheet bottomSheet =
                ChatFragmentDirections.actionNavChatsToNavChatBottomsheet(tChat, mMemberID, mJWT);
        Navigation.findNavController(Objects.requireNonNull(getView())).navigate(bottomSheet);
    }

    /**
     * Performs operations following WS response when getting chat contents.
     * @param result the response from the WS
     */
    private void handleDisplayChatOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats_messages))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_chats_messages));
                ChatMessage[] messages = new ChatMessage[data.length()];
                for(int i = 0; i < data.length(); i++) {
                    JSONObject message = data.getJSONObject(i);

                    messages[i] = new ChatMessage(message.getInt(getString(R.string.keys_json_connections_memberid_int)) == mMemberID,
                            message.getString(getString(R.string.keys_json_chatmessage_sender)),
                            message.getString(getString(R.string.keys_json_chatmessage_timestamp)),
                            message.getString(getString(R.string.keys_json_chatmessage_message)));
                }
                // Viewing the chat, remove alert that there are new messages in it
                ChatListViewModel.getFactory().create(ChatListViewModel.class)
                        .setUnread(currentChat.getChatID(), false);

                ChatFragmentDirections.ActionChatFragmentToChatMessageFragment chatroom =
                        ChatFragmentDirections.actionChatFragmentToChatMessageFragment(messages,
                                                                        mJWT, mMemberID,
                                                                        currentChat.getChatID(),
                                                                        currentChat.getName(),
                                                                        currentChat.isFavorited());

                NavController nc = Navigation.findNavController(Objects.requireNonNull(getView()));
                if (Objects.requireNonNull(nc.getCurrentDestination())
                                .getId() != R.id.nav_chats) { // Ensure back button doesn't break nav
                    nc.navigateUp();
                }
                nc.navigate(chatroom);

            } else {
                Log.e("ERROR!", "Couldn't get messages from chat");
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
        void onListFragmentInteraction(Chat item);
    }
}