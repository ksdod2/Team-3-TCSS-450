package edu.uw.tcss450.team3chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.HomeActivity;
import edu.uw.tcss450.team3chatapp.HomeActivityArgs;
import edu.uw.tcss450.team3chatapp.MobileNavigationDirections;
import edu.uw.tcss450.team3chatapp.MyChatRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.ChatListViewModel;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.model.Credentials;
import edu.uw.tcss450.team3chatapp.model.LocationViewModel;
import edu.uw.tcss450.team3chatapp.utils.GetAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;

public class HomeFragment extends Fragment {

    private TextView mWeatherTemp;
    private TextView mWeatherDescription;
    private ImageView mWeatherIcon;
    private TextView mCityState;
    private Location mLocation;
    private String mUnits;
    private ArrayList<Chat> mFavs;
    private Chat currentChat;
    private String mJWT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mFavs = new ArrayList<>();
        ChatListViewModel model = ChatListViewModel.getFactory().create(ChatListViewModel.class);
        model.getCurrentChats().observe(this, this::updateRecyclerView);
        RecyclerView favs = rootView.findViewById(R.id.list_home_favorites);
        favs.setAdapter(new MyChatRecyclerViewAdapter(mFavs, this::displayChat, null));

        return rootView;
    }

    /**
     * Updates the RecyclerView of favorite Chats upon notification of the Chat ViewModel being updated.
     * @param tChats the list of chats to update with
     */
    private void updateRecyclerView(final List<Chat> tChats) {
        mFavs.clear();
        for (Chat c : tChats) {
            if (c.isFavorited())
                mFavs.add(c);
        }
        RecyclerView roomList = getView().findViewById(R.id.list_home_favorites);
        roomList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Custom back button functionality to exit app from this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Objects.requireNonNull(getActivity()).finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //Get shared preferences for preferred temperature units
        SharedPreferences prefs = Objects.requireNonNull(getActivity())
                .getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.keys_prefs_tempunit))) {
            mUnits = prefs.getString(getString(R.string.keys_prefs_tempunit), "F");
        } else { //Otherwise set units to default (imperial)
            mUnits = "F";
            prefs.edit().putString(getString(R.string.keys_prefs_tempunit), "F").apply();
        }

        // Get credentials from HomeActivityArgs
        Credentials credentials = HomeActivityArgs.fromBundle(Objects.requireNonNull(getArguments())).getCredentials();

        // calendar for current time
        Calendar calendar = Calendar.getInstance();

        // Get last known device location
        LocationViewModel model = LocationViewModel.getFactory().create(LocationViewModel.class);
        mLocation = model.getCurrentLocation().getValue();

        // Get UI elements
        mWeatherDescription = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_status);
        mWeatherTemp = getView().findViewById(R.id.tv_home_temperature);
        mWeatherIcon = getView().findViewById(R.id.iv_home_weatherIcon);
        mCityState = getView().findViewById(R.id.tv_home_citystate);

        TextView units = getView().findViewById(R.id.tv_home_unit);
        TextView greeting = Objects.requireNonNull(getView()).findViewById(R.id.tv_home_greeting);
        TextView date = getView().findViewById((R.id.tv_home_date));
        TextView dayOfWeek = getView().findViewById((R.id.tv_home_dayOfWeek));

        // Set preferred unit of measurement
        if("F".equals(mUnits)) {
            units.setText("°F");
        } else {
            units.setText("°C");
        }

        // format the date and day of week
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");

        String dateString = dateFormat.format(calendar.getTime());
        String dayOfWeekString = dayOfWeekFormat.format((calendar.getTime()));

        // set greeting message with credentials
        String greetingText = "Welcome, " + credentials.getFirstName() + " " + credentials.getLastName() + "!";
        greeting.setText(greetingText);

        // set current date and day of week
        date.setText(dateString);
        dayOfWeek.setText(dayOfWeekString);

        //Make weather API call and display info
        populateWeatherData();
    }

    private void populateWeatherData() {
        if(mLocation != null) {
            String fields = "place.name,place.state,ob.tempC,ob.tempF,ob.weather,ob.icon";
            String action = mLocation.getLatitude() + "," + mLocation.getLongitude();

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_weather_weather))
                    .appendPath(getString(R.string.ep_weather_observations))
                    .appendPath(action)
                    .appendQueryParameter("fields", fields)
                    .build();

            new GetAsyncTask.Builder(uri.toString())
                    .onPreExecute(this::weatherOnPre)
                    .onCancelled(this::weatherOnCancel)
                    .onPostExecute(this::weatherOnPost)
                    .build().execute();
        } else {
            Log.d("WEATHER_URI", "location is null");
        }
    }

    private void weatherOnPre() {
        //TODO show progressbar layout to hide blank details
    }

    private void weatherOnCancel(final String theResult) {
        try {
            JSONObject jsonObj = new JSONObject(theResult);
            Log.d("WEATHER_CANCEL", jsonObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void weatherOnPost(final String theResult) {
        String logTag = "WEATHER_POST";
        //parse JSON
        try {
            JSONObject root = new JSONObject(theResult);
            if(root.has("response")) {
                JSONObject response = root.getJSONObject("response");
                if(response.has("place") && response.has("ob")) {
                    JSONObject place = response.getJSONObject("place");
                    JSONObject ob = response.getJSONObject("ob");


                    String cityState = formatCityState(place.getString("name"),
                                                        place.getString("state").toUpperCase());

                    String icFile = ob.getString("icon").substring(0, ob.getString("icon").length()-4);

                    int id = getResources().getIdentifier(icFile, "mipmap", getContext().getPackageName());

                    mWeatherDescription.setText(ob.getString("weather"));
                    mWeatherTemp.setText("F".equals(mUnits) ? ob.getString("tempF") : ob.getString("tempC"));
                    mCityState.setText(cityState);
                    mWeatherIcon.setImageResource(id);
                } else {
                    Log.d(logTag, "Either Place or Ob missing form Response: " + root.toString());
                }
            } else {
                Log.d(logTag, "Response Missing: " + root.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(logTag, Objects.requireNonNull(e.getMessage()));
        }
    }

    private String formatCityState(String name, String state) {

        StringBuilder city = new StringBuilder();

        String split[];
        if(name.contains(" ")) {
            split = name.split(" ");
            for(String s : split) {
                city.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
            }
        } else {
            city.append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append(" ");
        }
        city.trimToSize();

        return city.append(", ").append(state).toString();
    }

    /**
     * Moves to the appropriate fragment to display a chatroom.
     * @param tChat the chat to display content from
     */
    private void displayChat(final Chat tChat) {
        // Viewing the chat, remove alert that there are new messages in it
        tChat.setNew(false);

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
                .addHeaderField("authorization", ((HomeActivity) getActivity()).mArgs.getJwt())
                .build().execute();
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

                    messages[i] = new ChatMessage(message.getInt(getString(R.string.keys_json_connections_memberid_int)) ==
                                                    ((HomeActivity) getActivity()).mArgs.getUserId(),
                            message.getString(getString(R.string.keys_json_chatmessage_sender)),
                            message.getString(getString(R.string.keys_json_chatmessage_timestamp)),
                            message.getString(getString(R.string.keys_json_chatmessage_message)));
                }
                MobileNavigationDirections.ActionGlobalNavChatroom chatroom =
                        MobileNavigationDirections.actionGlobalNavChatroom(messages, ((HomeActivity) getActivity()).mArgs.getJwt(),
                                ((HomeActivity) getActivity()).mArgs.getUserId(), currentChat.getChatID(),
                                currentChat.getName(), currentChat.isFavorited());
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment)
                        .navigate(chatroom);
            } else {
                Log.e("ERROR!", "Couldn't get messages from chat");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
    }
}