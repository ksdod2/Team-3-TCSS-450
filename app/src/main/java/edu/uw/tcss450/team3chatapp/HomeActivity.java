package edu.uw.tcss450.team3chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
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

import edu.uw.tcss450.team3chatapp.model.Chat;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.ui.ConnectionHomeFragmentDirections;
import edu.uw.tcss450.team3chatapp.utils.SendPostAsyncTask;
import edu.uw.tcss450.team3chatapp.utils.ThemeChanger;
import me.pushy.sdk.Pushy;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;
    private AppBarConfiguration mAppBarConfiguration;
    private HomeActivityArgs mArgs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply user-preferred theme from shared preferences
        mPrefs = getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(mPrefs.contains(getString(R.string.keys_prefs_theme))) {
            int themeId = mPrefs.getInt(getString(R.string.keys_prefs_theme), R.style.AppTheme);
            ThemeChanger.setThemeOnActivityCreation(this, themeId);
        } else {
            mPrefs.edit().putInt(getString(R.string.keys_prefs_theme), R.style.AppTheme).apply();
            ThemeChanger.setThemeOnActivityCreation(this, R.style.AppTheme);
        }

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mArgs = HomeActivityArgs.fromBundle(getIntent().getExtras());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_chats, R.id.nav_connectionhome, R.id.nav_weather)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.nav_graph_home, getIntent().getExtras());
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        if(mArgs.getChatMessage() != null) { // Navigate immediately to chatroom of pushed message
            Uri chatUri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base))
                    .appendPath(getString(R.string.ep_chat))
                    .appendPath(getString(R.string.ep_chat_getcontents))
                    .build();

            JSONObject chatInfo = new JSONObject();
            try {
                chatInfo.put("chatid", mArgs.getChatMessage().getRoom());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendPostAsyncTask.Builder(chatUri.toString(), chatInfo)
                    .onPostExecute(this::handleDisplayChatOnPostExecute)
                    .onCancelled(error -> Log.e("CHAT_ROOM_NAV", error))
                    .addHeaderField("authorization", mArgs.getJwt())
                    .build().execute();
      } else if (mArgs.getConnection() != null) { // Navigate immediately to view new connection
            NavController nc = Navigation.findNavController(this, R.id.nav_host_fragment);
            MobileNavigationDirections.ActionGlobalNavConnectionview connection =
                    MobileNavigationDirections.actionGlobalNavConnectionview(mArgs.getConnection(), mArgs.getUserId(), mArgs.getJwt());
            nc.navigate(connection);
        }
        navigationView.setNavigationItemSelectedListener(this::onNavigationSelected);

        // Set navigation drawer header fields with user information
        View header = navigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tv_nav_header)).setText(mArgs.getCredentials().getUsername());
        ((TextView) header.findViewById(R.id.tv_verification_message)).setText(mArgs.getCredentials().getEmail());
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
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private boolean onNavigationSelected(final MenuItem menuItem) {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                navController.navigate(R.id.nav_home, getIntent().getExtras());
                break;
            case R.id.nav_chats:
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
                break;

            case R.id.nav_connectionhome:
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

                break;
            case R.id.nav_weather:
                navController.navigate(R.id.nav_weather);
                break;
        }
        //Close the drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        return true;
    }

    private void handleConnectionsOnPostExecute(final String result) {
        //parse JSON

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections_rows))) {
                JSONArray data = root.getJSONArray( getString(R.string.keys_json_connections_rows));

                ArrayList<Connection> currentConns = new ArrayList<>();
                ArrayList<Connection> incomingConns = new ArrayList<>();
                ArrayList<Connection> pendingConns = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonConn = data.getJSONObject(i);

                        if(jsonConn.getInt(getString(R.string.keys_json_connections_verified_int)) == 1) {
                            // Verified connection, fill out information and add to that list
                            currentConns.add(new Connection(jsonConn.getInt(getString(R.string.keys_json_connections_memberid_int)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_firstname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_lastname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_username_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_email_str)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_verified_int)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_sender_int)) == mArgs.getUserId()));

                        } else if(jsonConn.getInt(getString(R.string.keys_json_connections_sender_int)) == mArgs.getUserId()) {
                            // Connection request that was sent by user but has not been accepted
                            pendingConns.add(new Connection(jsonConn.getInt(getString(R.string.keys_json_connections_memberid_int)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_firstname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_lastname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_username_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_email_str)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_verified_int)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_sender_int)) == mArgs.getUserId()));
                        } else {
                            // Connection requests to this user
                            incomingConns.add(new Connection(jsonConn.getInt(getString(R.string.keys_json_connections_memberid_int)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_firstname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_lastname_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_username_str)),
                                    jsonConn.getString(getString(R.string.keys_json_connections_email_str)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_verified_int)),
                                    jsonConn.getInt(getString(R.string.keys_json_connections_sender_int)) == mArgs.getUserId()));
                        }
                    }

                    MobileNavigationDirections.ActionGlobalNavConnectionhome directions
                            = ConnectionHomeFragmentDirections.actionGlobalNavConnectionhome(
                                currentConns.toArray(new Connection[0]),
                                incomingConns.toArray(new Connection[0]),
                                pendingConns.toArray(new Connection[0]),
                            mArgs.getUserId(), mArgs.getJwt());

                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(directions);
            } else {
                Log.e("ERROR!", "Database Error");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void handleChatsOnPostExecute(final String result) {
        //parse JSON

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_chats))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_chats));
                Chat[] rooms = new Chat[data.length()];
                for(int i = 0; i < data.length(); i++) {
                    JSONObject room = data.getJSONObject(i);

                    rooms[i] = new Chat(room.getInt(getString(R.string.keys_json_chats_id)),
                            room.getString(getString(R.string.keys_json_chats_name)),
                            room.getString(getString(R.string.keys_json_chats_description)));
                }

                MobileNavigationDirections.ActionGlobalNavChats chats =
                        MobileNavigationDirections.actionGlobalNavChats(rooms, mArgs.getJwt(),
                                mArgs.getUserId());
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(chats);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
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

                    messages[i] = new ChatMessage(message.getString(getString(R.string.keys_json_chatmessage_sender)),
                            message.getString(getString(R.string.keys_json_chatmessage_timestamp)),
                            message.getString(getString(R.string.keys_json_chatmessage_message)));
                }
                MobileNavigationDirections.ActionGlobalNavChatroom chatroom =
                        MobileNavigationDirections.actionGlobalNavChatroom(messages, mArgs.getJwt(),
                                                mArgs.getUserId(), mArgs.getChatMessage().getRoom());
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(chatroom);
            } else {
                Log.e("ERROR!", "Couldn't get messages from chat");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // TODO: Implement theme changes and other potential options under settings option
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
}