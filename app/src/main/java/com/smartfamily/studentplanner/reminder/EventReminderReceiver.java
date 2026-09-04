package com.smartfamily.studentplanner.reminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.entity.EventType;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;
import com.smartfamily.studentplanner.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "EventReminderReceiver";
    private static final int MONTHLY_REPETITIONS = 12;
    private static final int YEARLY_REPETITIONS = 5;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String EVENT_ID_EXTRA = "event_id";

    private String formatDateTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract data from Intent
        String description = intent.getStringExtra("event_description");
        int eventId = intent.getIntExtra("event_id", -1);
        // Create notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // create channel id
        String channelId = "event_reminder_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId, // set channel id
                "Event Reminders", // set channel name
                NotificationManager.IMPORTANCE_HIGH // set importance
        );
        // create notification channel
        notificationManager.createNotificationChannel(channel);

        // create Intent for MainActivity open
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("notification_type", "event");
        resultIntent.putExtra(EVENT_ID_EXTRA, eventId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // create PendingIntent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, eventId, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // build notification structure
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications) // set icon
                .setContentText(description) // set description
                .setPriority(NotificationCompat.PRIORITY_HIGH) // set priority
                .setAutoCancel(true) // set auto cancel function
                .setContentIntent(resultPendingIntent); // set PendingIntent for transition on click

        // Send notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Application application = (Application) context.getApplicationContext();
            EventTaskViewModel viewModel = new ViewModelProvider.AndroidViewModelFactory(application).create(EventTaskViewModel.class);

            EventTaskEntity event = viewModel.getEventById(eventId);

            if (event != null && (event.getRepetitions() - 1) > 0 && (EventType.MONTHLY.equals(event.getEventType()) || EventType.YEARLY.equals(event.getEventType()))) {
                event.setRepetitions(event.getRepetitions() - 1);
                viewModel.update(event);

                Calendar nextReminderTime = Calendar.getInstance();
                nextReminderTime.setTimeInMillis(event.getSelectedDate());

                int reminderNumber;
                if (EventType.MONTHLY.equals(event.getEventType())) {
                    reminderNumber = MONTHLY_REPETITIONS - event.getRepetitions();
                    nextReminderTime.add(Calendar.MONTH, reminderNumber);
                    nextReminderTime.add(Calendar.DAY_OF_YEAR, -1);

                    Log.d(TAG, "Next reminder is: " + formatDateTime(nextReminderTime.getTimeInMillis()));
                } else if (EventType.YEARLY.equals(event.getEventType())) {
                    reminderNumber = YEARLY_REPETITIONS - event.getRepetitions();
                    nextReminderTime.add(Calendar.YEAR, reminderNumber);
                    nextReminderTime.add(Calendar.DAY_OF_YEAR, -1);
                }

                long nextReminderTimeMillis = nextReminderTime.getTimeInMillis();
                Log.d(TAG, "Next reminder will be set on: " + formatDateTime(nextReminderTimeMillis));

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent reminderIntent = new Intent(context, EventReminderReceiver.class);
                reminderIntent.putExtra("event_description", event.getDescription());
                reminderIntent.putExtra("event_id", (int) event.getId());
                PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (int) event.getId() * 1000 + 2,
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextReminderTimeMillis,
                        nextPendingIntent
                );
            } else if (event != null && event.getRepetitions() == 0 && (EventType.MONTHLY.equals(event.getEventType()) || EventType.YEARLY.equals(event.getEventType()))) {
                Log.d(TAG, "Reminders finished for event ID: " + eventId);
            } else if (event != null && !EventType.MONTHLY.equals(event.getEventType()) && !EventType.YEARLY.equals(event.getEventType())) {
                Log.d(TAG, "Reminder is not repeating for event ID: " + eventId);
            } else {
                Log.d(TAG, "Event with ID: " + eventId + " not found.");
            }
        });
    }
}
