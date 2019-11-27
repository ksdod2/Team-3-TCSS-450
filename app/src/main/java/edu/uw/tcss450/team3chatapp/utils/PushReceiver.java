package edu.uw.tcss450.team3chatapp.utils;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team3chatapp.MainActivity;
import edu.uw.tcss450.team3chatapp.R;
import me.pushy.sdk.Pushy;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class PushReceiver extends BroadcastReceiver {

    public static final String RECEIVED_NEW_MESSAGE = "new message from pushy";
    public static final String RECEIVED_NEW_CONN = "new connection from pushy";
    public static final String RECEIVED_NEW_CONVO = "new chat from pushy";

    public static final String CHAT_MESSAGE = "msg";
    public static final String CHAT_ROOM = "convo";
    public static final String CONNECTION = "conn";

    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {

        String typeOfMessage = intent.getStringExtra("type");
        // Sender's username is always sent
        String sender = intent.getStringExtra("sender");

        switch (typeOfMessage) {
            case CHAT_MESSAGE:
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));
                    String messageText = message.getString("text");

                    ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                    ActivityManager.getMyMemoryState(appProcessInfo);

                    if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                        //app is in the foreground so send the message to the active Activities
                        Log.d("PUSHY", "Message received in foreground: " + messageText);

                        //create an Intent to broadcast a message to other parts of the app.
                        Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                        i.putExtra("SENDER", sender);
                        i.putExtra("MESSAGE", message.toString());
                        i.putExtras(intent.getExtras());

                        context.sendBroadcast(i);

                    } else {
                        //app is in the background so create and post a notification
                        Log.d("PUSHY", "Message received in background: " + messageText);

                        Intent i = new Intent(context, MainActivity.class);
                        i.putExtras(intent.getExtras());

                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                i, PendingIntent.FLAG_UPDATE_CURRENT);

                        //research more on notifications the how to display them
                        //https://developer.android.com/guide/topics/ui/notifiers/notifications
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_menu_chats)
                                .setContentTitle("Message from: " + sender)
                                .setContentText(messageText)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent);

                        // Automatically configure a ChatMessageNotification Channel for devices running Android O+
                        Pushy.setNotificationChannel(builder, context);

                        // Get an instance of the NotificationManager service
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        // Build the notification and display it
                        notificationManager.notify(1, builder.build());
                    }
                } catch (JSONException e) {
                    Log.e("PUSH ERROR", e.getMessage());
                }
                break;
            case CONNECTION:
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));

                    ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                    ActivityManager.getMyMemoryState(appProcessInfo);

                    if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                        //app is in the foreground so send the message to the active Activities
                        Log.d("PUSHY", "Connection received in foreground: " + message.toString());

                        //create an Intent to broadcast a message to other parts of the app.
                        Intent i = new Intent(RECEIVED_NEW_CONN);
                        i.putExtra("SENDER", sender);
                        i.putExtra("MESSAGE", message.toString());
                        i.putExtras(intent.getExtras());

                        context.sendBroadcast(i);

                    } else {
                        //app is in the background so create and post a notification
                        Log.d("PUSHY", "Message received in background: " + message.toString());
                        // Updates to all sorts of connections will be received, only notify for invitations
                        if (message.getBoolean("new") && message.getInt("relation") == 0) {

                            Intent i = new Intent(context, MainActivity.class);
                            i.putExtras(intent.getExtras());

                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                    i, PendingIntent.FLAG_UPDATE_CURRENT);

                            //research more on notifications the how to display them
                            //https://developer.android.com/guide/topics/ui/notifiers/notifications
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.ic_menu_chats)
                                    .setContentTitle("Connection request from: " + sender)
                                    .setContentText("You have received a new connection request.")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent);

                            // Automatically configure a ChatMessageNotification Channel for devices running Android O+
                            Pushy.setNotificationChannel(builder, context);

                            // Get an instance of the NotificationManager service
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            // Build the notification and display it
                            notificationManager.notify(1, builder.build());
                        }
                    }
                } catch (JSONException e) {
                    Log.e("PUSH ERROR", e.getMessage());
                }
                break;
            case CHAT_ROOM:
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));

                    ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                    ActivityManager.getMyMemoryState(appProcessInfo);

                    if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                        //app is in the foreground so send the message to the active Activities
                        Log.d("PUSHY", "Chat received in foreground: " + message.toString());

                        //create an Intent to broadcast a message to other parts of the app.
                        Intent i = new Intent(RECEIVED_NEW_CONVO);
                        i.putExtra("SENDER", sender);
                        i.putExtra("MESSAGE", message.toString());
                        i.putExtras(intent.getExtras());

                        context.sendBroadcast(i);
                    } else {
                        //app is in the background so create and post a notification
                        Log.d("PUSHY", "Chat received in background:  " + message.toString());

                        Intent i = new Intent(context, MainActivity.class);
                        i.putExtras(intent.getExtras());

                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                i, PendingIntent.FLAG_UPDATE_CURRENT);

                        //research more on notifications the how to display them
                        //https://developer.android.com/guide/topics/ui/notifiers/notifications
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_menu_chats)
                                .setContentTitle(sender + " has added you to a new chat:")
                                .setContentText(message.getString("name"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent);

                        // Automatically configure a ChatMessageNotification Channel for devices running Android O+
                        Pushy.setNotificationChannel(builder, context);

                        // Get an instance of the NotificationManager service
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        // Build the notification and display it
                        notificationManager.notify(1, builder.build());
                    }
                } catch (JSONException e) {
                    Log.e("PUSH ERROR", e.getMessage());
                }
                break;
        }
    }
}
