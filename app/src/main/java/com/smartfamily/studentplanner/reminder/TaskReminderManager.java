package com.smartfamily.studentplanner.reminder;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskReminderManager {
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_FIRST_RUN_LOGIC_DONE = "first_run_logic_done";

    private TaskReminderManager() {}

    public static boolean arePushNotificationsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
    }

    // method for setting of task reminder
    public static void setTaskReminder(Context context, TaskEntity task) {
        if (!arePushNotificationsEnabled(context)) {
            Log.d(TAG, "Push notifications are disabled. Not setting reminder for task ID: " + task.getId());
            return;
        }

        // Create Intent for TaskReminderReceiver
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("task_title", task.getTitle()); // put task's title to Intent
        intent.putExtra("task_description", task.getDescription()); // put task's description to Intent
        intent.putExtra("task_id", (int) task.getId());

        // Create PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) task.getId(),  // Use ID task as requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate time for notification (one day before dueDate(our deadline))
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDueDate());
        calendar.add(Calendar.DAY_OF_YEAR, -1);  // minus 1 day

        long reminderTimeMillis = calendar.getTimeInMillis();
        scheduleReminderAlarm(context, reminderTimeMillis, pendingIntent, task.getId(), task.getTitle(), task.getDueDate());
    }

    // method for cancel of task reminder
    public static void cancelTaskReminder(Context context, int taskId) {
        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create Intent и PendingIntent
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel Alarm
        alarmManager.cancel(pendingIntent);
    }

    private static void scheduleReminderAlarm(Context context, long reminderTimeMillis, PendingIntent pendingIntent, long entityId, String entityTitle, Date taskDueDate) {
        long currentTimeMillis = System.currentTimeMillis();

        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTimeInMillis(reminderTimeMillis);

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentTimeMillis);

        Calendar dueDateCal = Calendar.getInstance();
        dueDateCal.setTime(taskDueDate);

        long finalReminderTime;
        String logMessage;

        // If the reminder time is in the future
        if (reminderTimeMillis > currentTimeMillis) {
            finalReminderTime = reminderTimeMillis;
            logMessage = "Reminder for task ID: " + entityId + " (" + entityTitle + ") installed on: " + new Date(finalReminderTime);
        }
        // If the reminder time is in the past, but the reminder day is today
        else if ((reminderCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                reminderCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) ||
                (dueDateCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                        dueDateCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR))) {
            // Set it to "now" (with a small delay of 5 seconds)
            finalReminderTime = currentTimeMillis + 5 * 1000;
            logMessage = "Reminder for task ID: " + entityId + " (" + entityTitle + ") should have gone off, it's going off now: " + new Date(finalReminderTime);
        }
        // Do not set an alarm if the reminder time and the task due date are both in the past
        else {
            Log.d(TAG, "Reminder for task ID: " + entityId + " (" + entityTitle + ") already in the past (" + new Date(reminderTimeMillis) + "), is no plans to set.");
            return;
        }

        // Setting an alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finalReminderTime,
                    pendingIntent
            );
            Log.d(TAG, logMessage);
        }
    }

    public static void cancelAllTaskReminders(Context context, TaskViewModel viewModel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<TaskEntity> allTasks = viewModel.getAllTasksBlocking();

            if (allTasks != null) {
                for (TaskEntity task : allTasks) {
                    cancelTaskReminder(context, (int) task.getId());
                }
            }
        });
        executor.shutdown();
    }


    public static void rescheduleAllTaskReminders(Context context, TaskViewModel viewModel) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Cancel all previous individual task alarms
            List<TaskEntity> allTasksAtStart = viewModel.getAllTasksBlocking();
            if (allTasksAtStart != null) {
                for (TaskEntity task : allTasksAtStart) {
                    cancelTaskReminder(context, (int) task.getId());
                }
            }

            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean isFirstRunLogicNeeded = preferences.getBoolean(KEY_FIRST_RUN_LOGIC_DONE, true);

            if (!arePushNotificationsEnabled(context)) {
                Log.d(TAG, "Push notifications are disabled. No reminders will be set.");
                return;
            }

            if (isFirstRunLogicNeeded) {
                Log.d(TAG, "First reminder activation or reset detected.");

                List<TaskEntity> allTasks = viewModel.getAllTasksBlocking();
                if (allTasks != null) {
                    viewModel.resetAllTaskReminderSentStatus();
                    Log.d(TAG, "Reminder sending status for all tasks reset to false.");
                }
                preferences.edit().putBoolean(KEY_FIRST_RUN_LOGIC_DONE, false).apply();
            }

            // Set only current and nearest individual alarms
            List<TaskEntity> currentTasks = viewModel.getAllTasksBlocking();
            if (currentTasks != null) {
                Calendar now = Calendar.getInstance();
                Calendar oneWeekLater = (Calendar) now.clone();
                oneWeekLater.add(Calendar.WEEK_OF_YEAR, 1);

                for (TaskEntity task : currentTasks) {
                    if (task.isReminderSent()) {
                        Log.d(TAG, "Reminder for task ID:" + task.getId() + "(" + task.getTitle() + ") has already been sent, cannot be rescheduled.");
                        continue;
                    }

                    Calendar dueDateCal = Calendar.getInstance();
                    dueDateCal.setTime(task.getDueDate());

                    if (dueDateCal.getTimeInMillis() > now.getTimeInMillis() &&
                            dueDateCal.getTimeInMillis() <= oneWeekLater.getTimeInMillis()) {
                        setTaskReminder(context, task);
                    }
                    else if (dueDateCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            dueDateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                        setTaskReminder(context, task);
                    }
                }
            }
        });
        executor.shutdown();
    }
}
