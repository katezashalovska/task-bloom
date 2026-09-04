package com.smartfamily.studentplanner.reminder;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.ui.HomeFragment;
import com.smartfamily.studentplanner.ui.MainActivity;

public class TaskReminderReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract data from Intent
        String title = intent.getStringExtra("task_title");
        String description = intent.getStringExtra("task_description");
        int taskId = intent.getIntExtra("task_id", -1);

        if (taskId != -1) {
            TaskViewModel taskViewModel = new TaskViewModel((Application) context.getApplicationContext());
            taskViewModel.updateTaskReminderSentStatus(taskId, true);
            Log.d("TaskReminderReceiver", "Reminder for task ID:: " + taskId + " marked as sent.");
        }

        // Create notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // create channel id
        String channelId = "task_reminder_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId, // set channel id
                "Task Reminders", // set channel name
                NotificationManager.IMPORTANCE_HIGH // set importance
        );
        // create notification channel
        notificationManager.createNotificationChannel(channel);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(HomeFragment.TASK_ID_EXTRA, taskId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // create PendingIntent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, taskId, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // build notification structure
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications) // set icon
                .setContentTitle(context.getString(R.string.notification_content_title) + " " + title) // set title
                .setContentText(description) // set description
                .setPriority(NotificationCompat.PRIORITY_HIGH) // set priority
                .setAutoCancel(true) // set auto cancel function
                .setContentIntent(resultPendingIntent);

        // Send notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
