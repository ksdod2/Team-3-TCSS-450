package edu.uw.tcss450.team3chatapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.ChatListViewModel;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.model.ConnectionListViewModel;
import edu.uw.tcss450.team3chatapp.ui.ChatFragmentDirections;
import edu.uw.tcss450.team3chatapp.ui.ConnectionHomeFragmentDirections;
import edu.uw.tcss450.team3chatapp.utils.PushReceiver;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.ThemeChanger;
import edu.uw.tcss450.team3chatapp.utils.Utils;
import me.pushy.sdk.Pushy;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView mNavigationView;
    private HomeActivityArgs mArgs;
    private ColorFilter mDefault;

    private HomePushMessageReceiver mPushMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply user-preferred theme from shared preferences
        mPrefs = getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(mPrefs.contains(getString(R.string.keys_prefs_theme))) {
            int themeId = mPrefs.getInt(getString(R.string.keys_prefs_theme), R.style.DarkMode);
            ThemeChanger.setThemeOnActivityCreation(this, themeId);
        } else {
            mPrefs.edit().putInt(getString(R.string.keys_prefs_theme), R.style.DarkMode).apply();
            ThemeChanger.setThemeOnActivityCreation(this, R.style.DarkMode);
        }
        setContentView(R.layout.activity_home);

        // Setup Navigation Elements
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each menu should be considered as top level destination.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_chats, R.id.nav_connectionhome, R.id.nav_weather)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.nav_graph_home, getIntent().getExtras());
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);

        mArgs = HomeActivityArgs.fromBundle(Objects.requireNonNull(getIntent().getExtras()));
        // Check for unread messages; Navigate immediately to chatroom of pushed message or room
        if(mArgs.getChatMessage() != null || mArgs.getChat() != null) {
            Uri chatUri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_chat))
                    .appendPath(getString(R.string.ep_chat_getcontents))
                    .build();

            JSONObject chatInfo = new JSONObject();
            try {
                if(mArgs.getChatMessage() != null)
                    chatInfo.put("chatid", mArgs.getChatMessage().getRoom());
                else
                    chatInfo.put("chatid", mArgs.getChat().getChatID());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                    .onPostExecute(this::handleDisplayChatOnPostExecute)
                    .onCancelled(error -> Log.e("CHAT_ROOM_NAV", error))
                    .addHeaderField("authorization", mArgs.getJwt())
                    .build().execute();

        // Check for new connections; Navigate immediately to view new connection
        } else if (mArgs.getConnection() != null) {
            NavController nc = Navigation.findNavController(this, R.id.nav_host_fragment);
            MobileNavigationDirections.ActionGlobalNavConnectionview connection =
                    MobileNavigationDirections.actionGlobalNavConnectionview(mArgs.getConnection(), mArgs.getUserId(), mArgs.getJwt());
            nc.navigate(connection);
        }
        // Get information to populate ViewModels
        Utils.updateWeatherIfNecessary(mPrefs);
        fetchConnections();
        fetchChats();

        mNavigationView.setNavigationItemSelectedListener(this::onNavigationSelected);
        mDefault = toolbar.getNavigationIcon().getColorFilter();

        // Set navigation drawer header fields with user information
        View header = mNavigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tv_nav_header)).setText(mArgs.getCredentials().getUsername());
        ((TextView) header.findViewById(R.id.tv_verification_message)).setText(mArgs.getCredentials().getEmail());
    }

    private void fetchWeatherPost(final String result) {}

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReceiver == null) {
            mPushMessageReceiver = new HomePushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(PushReceiver.RECEIVED_NEW_MESSAGE);
        iFilter.addAction(PushReceiver.RECEIVED_NEW_CONN);
        iFilter.addAction(PushReceiver.RECEIVED_NEW_CONVO);
        registerReceiver(mPushMessageReceiver, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReceiver != null){
            unregisterReceiver(mPushMessageReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private boolean onNavigationSelected(final MenuItem menuItem) {
        // Regardless of action, user is now aware of a notification that arrived somewhere
        ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                .setColorFilter(mDefault);

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                navController.navigate(R.id.nav_home, getIntent().getExtras());
                break;
            case R.id.nav_chats:
                MobileNavigationDirections.ActionGlobalNavChats chats =
                        ChatFragmentDirections.actionGlobalNavChats(mArgs.getJwt(), mArgs.getUserId());

                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(chats);
                menuItem.setTitle(R.string.menu_chats);
                break;

            case R.id.nav_connectionhome:
                MobileNavigationDirections.ActionGlobalNavConnectionhome directions
                        = ConnectionHomeFragmentDirections.actionGlobalNavConnectionhome(mArgs.getUserId(), mArgs.getJwt());

                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(directions);
                menuItem.setTitle(R.string.menu_connections);
                break;
            case R.id.nav_weather:
                navController.navigate(R.id.nav_weather);
                break;
        }
        //Close the drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        return true;
    }

//    private void updateWeatherIfNecessary() {
//        WeatherProfileViewModel model = WeatherProfileViewModel.getFactory().create(WeatherProfileViewModel.class);
//
//        if(model.getAllWeatherProfiles().getValue() == null || model.getTimeStamp() < Utils.getTopOfLastHour()) {
//            ArrayList<Location> savedLocations;
//
//            Location curLoc = Objects.requireNonNull(LocationViewModel.getFactory().create(LocationViewModel.class).getCurrentLocation().getValue());
//            String locKey = getString(R.string.keys_prefs_savedlocations);
//            Gson gson = new Gson();
//
//            if(mPrefs.contains(locKey)) {
//                Type typeOfLocList = new TypeToken<List<Location>>(){}.getType();
//
//                String listAsJSON = mPrefs.getString(locKey, "");
//                savedLocations = gson.fromJson(listAsJSON, typeOfLocList);
//
//                //Add current location to saved locations if current location is far enough away from any other saved locations.
//                boolean containsCurLoc = false;
//                for(Location savedLoc : savedLocations) {
//                    if(Utils.areCloseTogether(savedLoc, curLoc)) {
//                        containsCurLoc = true;
//                        break;
//                    }
//                }
//                if(!containsCurLoc) {
//                    savedLocations.add(curLoc);
//                    mPrefs.edit().putString(locKey, gson.toJson(savedLocations)).apply();
//                }
//            } else {
//                savedLocations = new ArrayList<>();
//                savedLocations.add(curLoc);
//                mPrefs.edit().putString(locKey, gson.toJson(savedLocations)).apply();
//            }
//
//            model.update(savedLocations);
//        }

//        if(savedLocations.size() > 0) {
//            Uri uri = new Uri.Builder()
//                    .scheme("https")
//                    .authority(getString(R.string.ep_base))
//                    .appendPath(getString(R.string.ep_weather))
//                    .appendQueryParameter("requests", buildWeatherQuery(savedLocations))
//                    .build();
//
//            Log.d("WEATHER_URI", uri.toString());
//
//            new GetAsyncTask.Builder(uri.toString())
//                    .onPostExecute(this::fetchWeatherPost)
//                    .onCancelled(error -> Log.e("", error))
//                    .build().execute();
//        } else {
//            Log.d("WEATHER_ERR", "Unable to get device location & no saved locations");
//        }
//    }

    private void fetchConnections() {
        Uri connectionUri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_connections_get))
                .build();

        JSONObject connInfo = new JSONObject();
        try {
            connInfo.put("memberid", mArgs.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(connectionUri.toString(), connInfo)
                .onPostExecute(this::handleConnectionsOnPostExecute)
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mArgs.getJwt())
                .build().execute();
    }

    private void fetchChats() {
        Uri chatUri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base))
                .appendPath(getString(R.string.ep_chat))
                .appendPath(getString(R.string.ep_chat_getchats))
                .build();

        JSONObject chatInfo = new JSONObject();
        try {
            chatInfo.put("memberid", mArgs.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                .onPostExecute(this::handleChatsOnPostExecute)
                .onCancelled(error -> Log.e("CONN_NAV", error))
                .addHeaderField("authorization", mArgs.getJwt())
                .build().execute();
    }

    private void handleConnectionsOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections_rows))) {
                ArrayList<Connection> connections = new ArrayList<>();
                JSONArray data = root.getJSONArray( getString(R.string.keys_json_connections_rows));
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonConn = data.getJSONObject(i);
                    connections.add(new Connection(jsonConn.getInt(getString(R.string.keys_json_connections_memberid_int)),
                            jsonConn.getString(getString(R.string.keys_json_connections_firstname_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_lastname_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_username_str)),
                            jsonConn.getString(getString(R.string.keys_json_connections_email_str)),
                            jsonConn.getInt(getString(R.string.keys_json_connections_verified_int)),
                            jsonConn.getInt(getString(R.string.keys_json_connections_sender_int)) == mArgs.getUserId()));
                }
                // Set the list of connections in the ViewModel to the full set of user connections
                ConnectionListViewModel.getFactory().create(ConnectionListViewModel.class).setConnections(connections);
            } else {
                Log.e("ERROR!", "Database Error");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void handleChatsOnPostExecute(final String result) {
        //parse JSON

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats))) {
                ArrayList<Chat> chats = new ArrayList<>();
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_chats));
                for(int i = 0; i < data.length(); i++) {
                    JSONObject room = data.getJSONObject(i);

                    chats.add(new Chat(room.getInt(getString(R.string.keys_json_chats_id)),
                            room.getString(getString(R.string.keys_json_chats_name)),
                            room.getString(getString(R.string.keys_json_chats_description))));
                }
                // Set the list of chats in the ViewModel to the full set of user chats
                ChatListViewModel.getFactory().create(ChatListViewModel.class).setChats(chats);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
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

                    messages[i] = new ChatMessage(message.getInt(getString(R.string.keys_json_connections_memberid_int)) == mArgs.getUserId(),
                            message.getString(getString(R.string.keys_json_chatmessage_sender)),
                            message.getString(getString(R.string.keys_json_chatmessage_timestamp)),
                            message.getString(getString(R.string.keys_json_chatmessage_message)));
                }
                MobileNavigationDirections.ActionGlobalNavChatroom chatroom =
                        MobileNavigationDirections.actionGlobalNavChatroom(messages, mArgs.getJwt(),
                                                mArgs.getUserId(), Objects.requireNonNull(mArgs.getChatMessage()).getRoom(), "Chatroom");
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(chatroom);
            } else {
                Log.e("ERROR!", "Couldn't get messages from chat");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.nav_settings);
                break;
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new DeleteTokenAsyncTask().execute();
    }

    /**
     * Performs asynchronous tasks associated with logging out of the application
     */
    @SuppressLint("StaticFieldLeak")
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mPrefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            mPrefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
            mPrefs.edit().remove(getString(R.string.keys_prefs_stay_logged_in)).apply();

            //unregister the device from the Pushy servers
            Pushy.unregister(HomeActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void theVoid) {
            super.onPostExecute(theVoid);

            // Alternatively, close current session and return to login fragments
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            //Ends this Activity and removes it from the Activity back stack.
            finish();
        }
    }

    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver.
     */
    private class HomePushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NavController nc =
                    Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment);
            NavDestination nd = nc.getCurrentDestination();
            if (Objects.requireNonNull(intent.getAction()).equals(PushReceiver.RECEIVED_NEW_CONN)
                    && intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {

                try {
                    JSONObject msgInfo = new JSONObject(Objects.requireNonNull(intent.getStringExtra("message")));
                    boolean sender = intent.getStringExtra("SENDER").equals(mArgs.getCredentials().getUsername());
                    Connection pushed =
                            new Connection(msgInfo.getInt(getString(R.string.keys_json_connections_memberid_int)),
                                    msgInfo.getString(getString(R.string.keys_json_connections_firstname_str)),
                                    msgInfo.getString(getString(R.string.keys_json_connections_lastname_str)),
                                    msgInfo.getString(getString(R.string.keys_json_connections_username_str)),
                                    msgInfo.getString(getString(R.string.keys_prefs_email)),
                                    msgInfo.getInt("relation"), sender);
                    // Update ViewModel of connections with change based on connection
                    if (msgInfo.getBoolean("new")) {
                        ConnectionListViewModel.getFactory().create(ConnectionListViewModel.class).addConnection(pushed);
                        if(pushed.getRelation() == Connection.Relation.UNACCEPTED && !pushed.amSender()
                            && Objects.requireNonNull(nd).getId() != R.id.nav_connectionhome) {

                            ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                                    .setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
                            mNavigationView.getMenu().findItem(R.id.nav_connectionhome).setChecked(true);
                        }
                    } else
                        ConnectionListViewModel.getFactory().create(ConnectionListViewModel.class).removeConnection(pushed);

                } catch (JSONException e) {
                    // Couldn't get the notification properly, just give up
                    Log.e("PUSH CONNECTION", Objects.requireNonNull(e.getMessage()));
                }

            } else if (Objects.requireNonNull(intent.getAction()).equals(PushReceiver.RECEIVED_NEW_CONVO)
                    && intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {

                try {
                    JSONObject chtInfo = new JSONObject(Objects.requireNonNull(intent.getStringExtra("message")));
                    Chat pushed = new Chat(chtInfo.getInt(getString(R.string.keys_json_chats_id)),
                            chtInfo.getString(getString(R.string.keys_json_chats_name)),
                            chtInfo.getString(getString(R.string.keys_json_chats_description)));
                    // Update ViewModel of connections with change based on chat
                    if (chtInfo.getBoolean("new")) {
                        ChatListViewModel.getFactory().create(ChatListViewModel.class).addChat(pushed);
                        ChatListViewModel.getFactory().create(ChatListViewModel.class).setUnread(pushed.getChatID());
                        // Color navigation menu to show new chat received
                        if (Objects.requireNonNull(nd).getId() != R.id.nav_chats) {
                            ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                                    .setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
                            SpannableString s = new SpannableString(getString(R.string.menu_chats));
                            s.setSpan(new ForegroundColorSpan(Color.CYAN), 0, s.length(), 0);
                            mNavigationView.getMenu().findItem(R.id.nav_chats).setTitle(s);
                        }
                    } else
                        ChatListViewModel.getFactory().create(ChatListViewModel.class).removeChat(pushed);

                } catch (JSONException e) {
                    // Couldn't get the notification properly, just give up
                    Log.e("PUSH CHAT", Objects.requireNonNull(e.getMessage()));
                }
            } else if (Objects.requireNonNull(intent.getAction()).equals(PushReceiver.RECEIVED_NEW_MESSAGE)
                    && intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {
                try {
                    JSONObject msgInfo = new JSONObject(Objects.requireNonNull(intent.getStringExtra("message")));
                    // Set chat where message came from to display as having unread messages
                    ChatListViewModel.getFactory().create(ChatListViewModel.class).setUnread(msgInfo.getInt("room"));
                    // Color navigation menu to show new message received
                    if (Objects.requireNonNull(nd).getId() != R.id.nav_chats) {
                        ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                                .setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
                        SpannableString s = new SpannableString(getString(R.string.menu_chats));
                        s.setSpan(new ForegroundColorSpan(Color.CYAN), 0, s.length(), 0);
                        mNavigationView.getMenu().findItem(R.id.nav_chats).setTitle(s);
                    }
                } catch (JSONException e) {
                    // Couldn't get the notification properly, just give up
                    Log.e("PUSH MESSAGE", Objects.requireNonNull(e.getMessage()));
                }
            }
        }
    }
}