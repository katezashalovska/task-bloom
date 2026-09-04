package com.smartfamily.studentplanner.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.smartfamily.studentplanner.R;

import org.checkerframework.checker.nullness.qual.NonNull;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG = "PushNotificationService";


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            sendNotification(title, body);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        String channelName = getString(R.string.default_notification_channel_name);
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server if needed.
        Log.d(TAG, "Sending registration token to server: " + token);
    }
}
