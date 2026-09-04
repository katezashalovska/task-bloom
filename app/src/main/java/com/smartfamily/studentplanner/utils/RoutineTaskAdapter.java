package com.smartfamily.studentplanner.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.viewModel.RoutineTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.reminder.TaskReminderManager;
import com.smartfamily.studentplanner.ui.AddRoutineBottomSheetFragment;
import com.smartfamily.studentplanner.ui.RoutineFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutineTaskAdapter extends RecyclerView.Adapter<RoutineTaskAdapter.RoutineTaskViewHolder> {

    private final List<RoutineTaskEntity> routineTaskList;
    private final Context context;
    private final RoutineTaskViewModel routineTaskViewModel;
    private final TaskViewModel taskViewModel;
    private int highlightedPosition = -1;

    public RoutineTaskAdapter(List<RoutineTaskEntity> routineTaskList, Context context, RoutineTaskViewModel routineTaskViewModel, TaskViewModel taskViewModel) {
        this.routineTaskList = routineTaskList;
        this.context = context;
        this.routineTaskViewModel = routineTaskViewModel;
        this.taskViewModel = taskViewModel;
    }

    // Add a method to update the routine task list
    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<RoutineTaskEntity> newTaskList) {
        routineTaskList.clear();
        if (newTaskList != null) {
            routineTaskList.addAll(newTaskList);
        }
        notifyDataSetChanged(); // Notify RecyclerView about data update
    }

    public List<RoutineTaskEntity> getTasks() {
        return routineTaskList;
    }

    public void highlightItem(int position) {
        highlightedPosition = position;
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public RoutineTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_routine_list_block_layout, parent, false);
        return new RoutineTaskViewHolder(view, routineTaskViewModel, deleteListener, this);
    }

    public void resetHighlight() {
        highlightedPosition = -1;
        notifyDataSetChanged();
    }

    public interface OnItemDeleteListener {
        void onItemDeleted(int routineId);
    }

    private OnItemDeleteListener deleteListener;

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineTaskViewHolder holder, int position) {
        // Get the current routine task from the list
        RoutineTaskEntity routineTaskEntity = routineTaskList.get(position);

        // Set the title and description of the task
        holder.title.setText(routineTaskEntity.getTitle());
        holder.description.setText(routineTaskEntity.getDescription());

        String routineTypeString = "";
        if (routineTaskEntity.getRoutineType() != null) {
            switch (routineTaskEntity.getRoutineType()) {
                case DAILY:
                    routineTypeString = "Daily";
                    break;
                case WEEKLY:
                    routineTypeString = "Weekly";
                    break;
                case MONTHLY:
                    routineTypeString = "Monthly";
                    break;
                case ANNUAL:
                    routineTypeString = "Annual";
                    break;
            }
        }

        holder.repetition.setText(routineTypeString);
        holder.date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(routineTaskEntity.getSelectedDate())));

        // EditRoutineTask click handler
        holder.editRoutineTask.setOnClickListener(v -> {
            showPopupWindow(v, routineTaskList.get(position));
        });

        holder.itemView.setActivated(position == highlightedPosition);

        CardView cardView = holder.itemView.findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setBackgroundResource(R.drawable.event_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.event_background);
        }
    }

    @Override
    public int getItemCount() {
        return routineTaskList.size();
    }

    @SuppressLint("MissingInflatedId")
    private void showPopupWindow(View anchorView, RoutineTaskEntity routineTask) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_routine_list, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout edit = popupView.findViewById(R.id.editTaskItem);
        LinearLayout delete = popupView.findViewById(R.id.deleteTaskItem);

        edit.setOnClickListener(v -> {
            //Toast.makeText(context, "Edit routine", Toast.LENGTH_SHORT).show();
            AddRoutineBottomSheetFragment fragment = new AddRoutineBottomSheetFragment();

            // Pass the mode and routine ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString("mode", "update");
            bundle.putLong("routineId", routineTask.getId());
            fragment.setArguments(bundle);

            // Open the BottomSheetFragment to view the routine details
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
            messageText.setText(R.string.delete_event_alert);

            btnYes.setOnClickListener(v1 -> {
                // Cancel task reminder for current task
                TaskReminderManager.cancelTaskReminder(context, (int) routineTask.getId());
                // If the user clicked "Yes", delete the task
                routineTaskViewModel.deleteTaskById(routineTask.getId());
                taskViewModel.getTaskByRoutineTaskId(routineTask.getId());
                showRoutineSuccessfullyDeleted();
                dialog.dismiss();
            });

            btnNo.setOnClickListener(v1 -> dialog.dismiss());

            popupWindow.dismiss(); // close PopupWindow
        });

        popupWindow.setClippingEnabled(false); // Allow to go beyond the screen border

        anchorView.post(() -> popupWindow.showAsDropDown(anchorView, -428, -70));
    }

    private static void showRoutineSuccessfullyDeleted() {
        RoutineFragment.routineSuccessfullyDeleted.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        RoutineFragment.routineSuccessfullyDeleted.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    // RoutineTaskViewHolder class for the RecyclerView
    // Represents a single item view in the RecyclerView
    static class RoutineTaskViewHolder extends RecyclerView.ViewHolder {
        // UI elements for displaying routine task details
        TextView title;
        TextView description;
        TextView date;
        ImageView editRoutineTask;
        TextView repetition;
        RoutineTaskViewModel viewModel;
        OnItemDeleteListener deleteListener;
        RoutineTaskAdapter adapter;

        // Constructor for initializing the UI elements
        public RoutineTaskViewHolder(@NonNull View itemView, RoutineTaskViewModel viewModel, OnItemDeleteListener deleteListener, RoutineTaskAdapter adapter) {
            super(itemView);
            title = itemView.findViewById(R.id.routineTaskTitle);
            description = itemView.findViewById(R.id.subtitle);
            date = itemView.findViewById(R.id.routineTaskDate);
            editRoutineTask = itemView.findViewById(R.id.iconEditRoutineListBlock);
            repetition = itemView.findViewById(R.id.routineRepetition);
            this.viewModel = viewModel;
            this.deleteListener = deleteListener;
            this.adapter = adapter;

            editRoutineTask.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    RoutineTaskEntity deletedRoutine = adapter.routineTaskList.get(position);
                    viewModel.deleteRoutineTask(deletedRoutine);
                    if (deleteListener != null && deletedRoutine.getId() == adapter.highlightedPosition) {
                        deleteListener.onItemDeleted((int) deletedRoutine.getId());
                        adapter.resetHighlight();
                    }
                }
            });
        }
    }
}
