package com.smartfamily.studentplanner.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.entity.EventType;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventReminderManager {
    private static final String TAG = "EventReminderManager";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";

    private EventReminderManager() {}

    private static String formatDateTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new java.util.Date(timeInMillis));
    }

    public static boolean arePushNotificationsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
    }

    // method for setting of event reminder
    public static void setEventReminder(Context context, EventTaskEntity event) {
        if (!arePushNotificationsEnabled(context)) {
            Log.d(TAG, "Push notifications are disabled. Not setting reminder for event ID: " + event.getId());
            return;
        }

        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create Intent for EventReminderReceiver
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("event_description", event.getDescription()); // put event's description to Intent
        intent.putExtra("event_id", (int) event.getId());

        // Create PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) event.getId(),  // Use ID event as requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate time for notification (one day before dueDate(our deadline))
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getSelectedDate());

        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        if (EventType.MONTHLY.equals(event.getEventType())) {
            int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (calendar.get(Calendar.DAY_OF_MONTH) > maxDayOfMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, maxDayOfMonth);
            }

            Log.d(TAG, "Event date is: " + formatDateTime(calendar.getTimeInMillis()));

            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long firstReminderTime = calendar.getTimeInMillis();

            PendingIntent firstPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) event.getId() * 1000 + 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstReminderTime,
                    firstPendingIntent
            );

            Log.d(TAG, "Reminder time (Date): " + formatDateTime(calendar.getTimeInMillis()));
        } else if (EventType.YEARLY.equals(event.getEventType())) {
            gregorianCalendar.setTimeInMillis(calendar.getTimeInMillis());
            if (gregorianCalendar.get(Calendar.MONTH) == Calendar.FEBRUARY && gregorianCalendar.get(Calendar.DAY_OF_MONTH) == 29) {
                if (!gregorianCalendar.isLeapYear(gregorianCalendar.get(Calendar.YEAR))) {
                    gregorianCalendar.set(Calendar.DAY_OF_MONTH, 28);
                }
            }

            Log.d(TAG, "Event date is: " + formatDateTime(gregorianCalendar.getTimeInMillis()));

            gregorianCalendar.add(Calendar.DAY_OF_YEAR, -1);
            long firstReminderTime = gregorianCalendar.getTimeInMillis();

            PendingIntent firstPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) event.getId() * 1000 + 3,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstReminderTime,
                    firstPendingIntent
            );

            Log.d(TAG, "Reminder time (Date): " + formatDateTime(gregorianCalendar.getTimeInMillis()));
        } else {
            Log.d(TAG, "Event date is: " + formatDateTime(calendar.getTimeInMillis()));
            calendar.roll(Calendar.DAY_OF_YEAR, -1);
            Log.d(TAG, "Reminder time (Date): " + formatDateTime(calendar.getTimeInMillis()));

            long reminderTime = calendar.getTimeInMillis();

            // Set Alarm
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );
            Log.d(TAG, "Once alarm set for event ID: " + event.getId());
        }
    }

    // method for cancel of event reminder
    public static void cancelEventReminder(Context context, int eventId) {
        Log.d(TAG, "cancelEventReminder() called for event ID: " + eventId);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, EventReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "Alarm cancelled for event ID: " + eventId);
    }

    public static void cancelAllEventReminders(Context context, EventTaskViewModel viewModel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<EventTaskEntity> allEvents = viewModel.getAllEventTasksBlocking();

            if (allEvents != null) {
                for (EventTaskEntity event : allEvents) {
                    cancelEventReminder(context, (int) event.getId());
                }
            }
        });
        executor.shutdown();
    }

    public static void rescheduleAllEventReminders(Context context, EventTaskViewModel viewModel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<EventTaskEntity> allEvents = viewModel.getAllEventTasksBlocking();

            if (allEvents != null) {
                for (EventTaskEntity event : allEvents) {
                    cancelEventReminder(context, (int) event.getId());
                    setEventReminder(context, event);
                }
            }
        });
        executor.shutdown();
    }
}
