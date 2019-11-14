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

    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {

        String typeOfMessage = intent.getStringExtra("type");
        // Sender's username is always sent
        String sender = intent.getStringExtra("sender");

        if (typeOfMessage.equals("msg")) {
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
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    // Build the notification and display it
                    notificationManager.notify(1, builder.build());
                }
            } catch (JSONException e) {
                Log.e("PUSH ERROR", e.getMessage());
            }
        } else if (typeOfMessage.equals("conn")) {
            try {
                JSONObject message = new JSONObject(intent.getStringExtra("message"));

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.getMyMemoryState(appProcessInfo);

                if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                    //app is in the foreground so send the message to the active Activities
                    Log.d("PUSHY", "Message received in foreground: " + message.toString());

                    //create an Intent to broadcast a message to other parts of the app.
                    Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                    i.putExtra("SENDER", sender);
                    i.putExtra("MESSAGE", message.toString());
                    i.putExtras(intent.getExtras());

                    context.sendBroadcast(i);

                } else {
                    //app is in the background so create and post a notification
                    Log.d("PUSHY", "Message received in background: " + message.toString());

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
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    // Build the notification and display it
                    notificationManager.notify(1, builder.build());
                }
            } catch (JSONException e) {
                Log.e("PUSH ERROR", e.getMessage());
            }
        } else if (typeOfMessage.equals("room")) {
            // STUFF HERE FOR ROOM INVITATIONS
        }
    }
}
