package com.smartfamily.studentplanner.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskPriority;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.reminder.TaskReminderManager;
import com.smartfamily.studentplanner.ui.AddTaskBottomSheetFragment;
import com.smartfamily.studentplanner.ui.HomeFragment;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskEntity> taskList;
    private final Context context;
    private final TaskViewModel taskViewModel;
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private int highlightedPosition = RecyclerView.NO_POSITION;

    public TaskAdapter(List<TaskEntity> taskList, Context context, TaskViewModel taskViewModel) {
        this.taskList = (taskList != null) ? taskList : new ArrayList<>();
        this.context = context;
        this.taskViewModel = taskViewModel;
    }

    public List<TaskEntity> getTasks() {
        return taskList;
    }

    // Add a method to update the task list
    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<TaskEntity> newTaskList) {
        taskList.clear();
        if (newTaskList != null) {
            taskList.addAll(newTaskList);
        }
        notifyDataSetChanged(); // Notify RecyclerView about data update
    }

    public void highlightItem(int position) {
        if (highlightedPosition != position) {
            int oldHighlightedPosition = highlightedPosition;
            highlightedPosition = position;
            notifyItemChanged(oldHighlightedPosition);
            notifyItemChanged(highlightedPosition);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_to_do_list_block_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Get the current task from the list
        TaskEntity task = taskList.get(position);
        // Set the title and description of the task
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());

        holder.date.setText(dateFormat.format(task.getDueDate()));

        // Show the location icon only if the coordinates are set (not 0.0, 0.0)
        if (task.getLatitude() != 0.0 && task.getLongitude() != 0.0) {
            holder.locationIcon.setVisibility(View.VISIBLE);
        } else {
            holder.locationIcon.setVisibility(View.INVISIBLE);
        }

        // Setting the state of a checkbox without a listener trigger
        holder.checkBox.setOnCheckedChangeListener(null); // Removing the listener before updating
        holder.checkBox.setChecked(task.isCompleted());

        // Handle checkbox state changes
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked); // Update the local task state
            taskViewModel.updateTaskCompletion(task.getId(), isChecked); // Updating the state in the database
            Date changedTaskDate = task.getDueDate();
            LocalDate changedTaskLocalDate = changedTaskDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            taskViewModel.setLastTaskDate(changedTaskLocalDate);
        });

        TaskPriority taskPriority = task.getTaskPriority();
        if (taskPriority != null) {

            switch (taskPriority) {
                case LOW:
                    holder.taskPriority.setImageResource(R.drawable.ic_low_priority);
                    break;
                case MEDIUM:
                    holder.taskPriority.setImageResource(R.drawable.ic_middle_priority);
                    break;
                case HIGH:
                    holder.taskPriority.setImageResource(R.drawable.ic_hight_priority);
                    break;
            }
        }

        // EditToDoListTask click handler
        holder.editToDoListTask.setOnClickListener(v -> {
            showPopupWindow(v, task);
        });

        holder.itemView.setActivated(position == highlightedPosition);

        CardView cardView = holder.itemView.findViewById(R.id.toDoListCardView);
        if (cardView != null) {
            cardView.setBackgroundResource(R.drawable.to_do_list_task_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.to_do_list_task_background);
        }
    }

    // Returns the total number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @SuppressLint("MissingInflatedId")
    private void showPopupWindow(View anchorView, TaskEntity task) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_to_do_list, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

         LinearLayout edit = popupView.findViewById(R.id.editTaskItem);
         LinearLayout delete = popupView.findViewById(R.id.deleteTaskItem);

         edit.setOnClickListener(v -> {
             //Toast.makeText(context, "Edit task", Toast.LENGTH_SHORT).show();
             AddTaskBottomSheetFragment fragment = new AddTaskBottomSheetFragment();

             // Pass the mode and task ID to the fragment
             Bundle bundle = new Bundle();
             bundle.putString("mode", "update");
             bundle.putLong("taskId", task.getId());
             fragment.setArguments(bundle);

             // Open the BottomSheetFragment to view the task details
             if (context instanceof AppCompatActivity) {
                 AppCompatActivity activity = (AppCompatActivity) context;
                 fragment.show(activity.getSupportFragmentManager(), fragment.getTag());
             }

             popupWindow.dismiss(); // close PopupWindow
         });

        delete.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null);
            dialog.setContentView(dialogView);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0f;
                window.setAttributes(params);
            }

            dialog.show();

            Button btnYes = dialogView.findViewById(R.id.btnYes);
            Button btnNo = dialogView.findViewById(R.id.btnNo);
            TextView messageText = dialogView.findViewById(R.id.messageText);
            messageText.setText(R.string.delete_message_alert);

            btnYes.setOnClickListener(v1 -> {
                TaskReminderManager.cancelTaskReminder(context, (int) task.getId());
                taskViewModel.deleteTaskById(task.getId());
                LocalDate deletedTaskDate = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                taskViewModel.deleteTaskById(task.getId());
                taskViewModel.setDeletedTaskDate(deletedTaskDate);
                showTaskSuccessfullyDeleted();
                dialog.dismiss();
            });

            btnNo.setOnClickListener(v1 -> dialog.dismiss());

            popupWindow.dismiss(); // close PopupWindow
        });

        popupWindow.setClippingEnabled(false); // Allow to go beyond the screen border

        anchorView.post(() -> popupWindow.showAsDropDown(anchorView, -428, -70));
    }

    private static void showTaskSuccessfullyDeleted() {
        HomeFragment.taskSuccessfullyDeleted.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        HomeFragment.taskSuccessfullyDeleted.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    // ViewHolder class for the RecyclerView
    // Represents a single item view in the RecyclerView
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        // UI elements for displaying task details
        TextView title;
        TextView description;
        TextView date;
        CheckBox checkBox;
        ImageView taskPriority;
        ImageView editToDoListTask;
        ImageView locationIcon;

        // Constructor for initializing the UI elements
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.subtitle);
            date = itemView.findViewById(R.id.date);
            checkBox = itemView.findViewById(R.id.checkBox);
            taskPriority = itemView.findViewById(R.id.iconPriority);
            editToDoListTask = itemView.findViewById(R.id.iconEditToDoListBlock);
            locationIcon = itemView.findViewById(R.id.iconLocation);
        }
    }
}
