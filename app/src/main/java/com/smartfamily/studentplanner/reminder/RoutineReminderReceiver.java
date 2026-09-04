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
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineType;
import com.smartfamily.studentplanner.data.viewModel.RoutineTaskViewModel;
import com.smartfamily.studentplanner.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoutineReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "RoutineReminderReceiver";
    private static final int MONTHLY_REPETITIONS = 12;
    private static final int YEARLY_REPETITIONS = 5;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ROUTINE_ID_EXTRA = "routine_id";

    private String formatDateTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract data from Intent
        String description = intent.getStringExtra("routine_description");
        int routineId = intent.getIntExtra("routine_id", -1);
        // Create notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // create channel id
        String channelId = "routine_reminder_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId, // set channel id
                "Routine Reminders", // set channel name
                NotificationManager.IMPORTANCE_HIGH // set importance
        );
        // create notification channel
        notificationManager.createNotificationChannel(channel);

        // create Intent for MainActivity open
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("notification_type", "routine");
        resultIntent.putExtra(ROUTINE_ID_EXTRA, routineId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // create PendingIntent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, routineId, resultIntent,
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
            RoutineTaskViewModel viewModel = new ViewModelProvider.AndroidViewModelFactory(application).create(RoutineTaskViewModel.class);

            RoutineTaskEntity routine = viewModel.getRoutineById(routineId);

            if (routine != null && (routine.getRepetitions() - 1) > 0 && (RoutineType.MONTHLY.equals(routine.getRoutineType()) || RoutineType.ANNUAL.equals(routine.getRoutineType()))) {
                routine.setRepetitions(routine.getRepetitions() - 1);
                viewModel.update(routine);

                Calendar nextReminderTime = Calendar.getInstance();
                nextReminderTime.setTimeInMillis(routine.getSelectedDate());

                int reminderNumber;
                if (RoutineType.MONTHLY.equals(routine.getRoutineType())) {
                    reminderNumber = MONTHLY_REPETITIONS - routine.getRepetitions();
                    nextReminderTime.add(Calendar.MONTH, reminderNumber);
                    nextReminderTime.add(Calendar.DAY_OF_YEAR, -1);

                    Log.d(TAG, "Next reminder is: " + formatDateTime(nextReminderTime.getTimeInMillis()));
                } else if (RoutineType.ANNUAL.equals(routine.getRoutineType())) {
                    reminderNumber = YEARLY_REPETITIONS - routine.getRepetitions();
                    nextReminderTime.add(Calendar.YEAR, reminderNumber);
                    nextReminderTime.add(Calendar.DAY_OF_YEAR, -1);
                }

                long nextReminderTimeMillis = nextReminderTime.getTimeInMillis();
                Log.d(TAG, "Next reminder will be set on: " + formatDateTime(nextReminderTimeMillis));

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent reminderIntent = new Intent(context, RoutineReminderReceiver.class);
                reminderIntent.putExtra("routine_description", routine.getDescription());
                reminderIntent.putExtra("routine_id", (int) routine.getId());
                PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (int) routine.getId() * 1000 + 2,
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextReminderTimeMillis,
                        nextPendingIntent
                );
            } else if (routine != null && routine.getRepetitions() == 0 && (RoutineType.MONTHLY.equals(routine.getRoutineType()) || RoutineType.ANNUAL.equals(routine.getRoutineType()))) {
                Log.d(TAG, "Reminders finished for routine ID: " + routineId);
            } else if (routine != null && !RoutineType.MONTHLY.equals(routine.getRoutineType()) && !RoutineType.ANNUAL.equals(routine.getRoutineType())) {
                Log.d(TAG, "Reminder is not repeating for routine ID: " + routineId);
            } else {
                Log.d(TAG, "Routine with ID: " + routineId + " not found.");
            }
        });
    }
}
