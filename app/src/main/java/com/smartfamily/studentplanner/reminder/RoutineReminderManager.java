package com.smartfamily.studentplanner.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class RoutineReminderManager {
    private static final String TAG = "RoutineReminderManager";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";

    private RoutineReminderManager() {}

    private static String formatDateTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new java.util.Date(timeInMillis));
    }

    public static boolean arePushNotificationsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
    }

    // method for setting of routine reminder
    public static void setRoutineReminder(Context context, RoutineTaskEntity routine) {
        if (!arePushNotificationsEnabled(context)) {
            Log.d(TAG, "Push notifications are disabled. Not setting reminder for routine ID: " + routine.getId());
            return;
        }

        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create Intent for RoutineReminderReceiver
        Intent intent = new Intent(context, RoutineReminderReceiver.class);
        intent.putExtra("routine_description", routine.getDescription()); // put routine's description to Intent
        intent.putExtra("routine_id", (int) routine.getId());

        // Create PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) routine.getId(),  // Use ID routine as requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate time for notification (one day before dueDate(our deadline))
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(routine.getSelectedDate());

        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        if (RoutineType.MONTHLY.equals(routine.getRoutineType())) {
            int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (calendar.get(Calendar.DAY_OF_MONTH) > maxDayOfMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, maxDayOfMonth);
            }

            Log.d(TAG, "Routine date is: " + formatDateTime(calendar.getTimeInMillis()));

            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long firstReminderTime = calendar.getTimeInMillis();

            PendingIntent firstPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) routine.getId() * 1000 + 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstReminderTime,
                    firstPendingIntent
            );

            Log.d(TAG, "Reminder time (Date): " + formatDateTime(calendar.getTimeInMillis()));
        } else if (RoutineType.ANNUAL.equals(routine.getRoutineType())) {
            gregorianCalendar.setTimeInMillis(calendar.getTimeInMillis());
            if (gregorianCalendar.get(Calendar.MONTH) == Calendar.FEBRUARY && gregorianCalendar.get(Calendar.DAY_OF_MONTH) == 29) {
                if (!gregorianCalendar.isLeapYear(gregorianCalendar.get(Calendar.YEAR))) {
                    gregorianCalendar.set(Calendar.DAY_OF_MONTH, 28);
                }
            }

            Log.d(TAG, "Routine date is: " + formatDateTime(gregorianCalendar.getTimeInMillis()));

            gregorianCalendar.add(Calendar.DAY_OF_YEAR, -1);
            long firstReminderTime = gregorianCalendar.getTimeInMillis();

            PendingIntent firstPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) routine.getId() * 1000 + 3,
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
            Log.d(TAG, "Routine date is: " + formatDateTime(calendar.getTimeInMillis()));
            calendar.roll(Calendar.DAY_OF_YEAR, -1);
            Log.d(TAG, "Reminder time (Date): " + formatDateTime(calendar.getTimeInMillis()));

            long reminderTime = calendar.getTimeInMillis();

            // Set Alarm
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );
            Log.d(TAG, "Once alarm set for routine ID: " + routine.getId());
        }
    }

    // method for cancel of routine reminder
    public static void cancelRoutineReminder(Context context, int routineId) {
        Log.d(TAG, "cancelRoutineReminder() called for routine ID: " + routineId);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, RoutineReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                routineId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "Alarm cancelled for routine ID: " + routineId);
    }
}
