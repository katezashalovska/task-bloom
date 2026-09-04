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
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;
import com.smartfamily.studentplanner.reminder.EventReminderManager;
import com.smartfamily.studentplanner.ui.AddEventBottomSheetFragment;
import com.smartfamily.studentplanner.ui.EventFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventTaskAdapter extends RecyclerView.Adapter<EventTaskAdapter.EventTaskViewHolder> {

    private final List<EventTaskEntity> eventTaskList;
    private final Context context;
    private final EventTaskViewModel eventTaskViewModel;
    private int highlightedPosition = -1;

    public EventTaskAdapter(List<EventTaskEntity> eventTaskList, Context context, EventTaskViewModel eventTaskViewModel) {
        this.eventTaskList = eventTaskList;
        this.context = context;
        this.eventTaskViewModel = eventTaskViewModel;

    }

    // Add a method to update the event task list
    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<EventTaskEntity> newTaskList) {
        eventTaskList.clear();
        if (newTaskList != null) {
            eventTaskList.addAll(newTaskList);
        }
        notifyDataSetChanged(); // Notify RecyclerView about data update
    }

    public List<EventTaskEntity> getTasks() {
        return eventTaskList;
    }

    public void highlightItem(int position) {
        int oldHighlightedPosition = highlightedPosition;
        highlightedPosition = position;

        if (oldHighlightedPosition != -1 && oldHighlightedPosition < eventTaskList.size()) {
            notifyItemChanged(oldHighlightedPosition);
        }
        if (highlightedPosition != -1 && highlightedPosition < eventTaskList.size()) {
            notifyItemChanged(highlightedPosition);
        }
    }

    @NonNull
    @Override
    public EventTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_event_list_block_layout, parent, false);
        return new EventTaskViewHolder(view, eventTaskViewModel, deleteListener, this);
    }

    public void resetHighlight() {
        highlightedPosition = -1;
        notifyDataSetChanged();
    }

    public interface OnItemDeleteListener {
        void onItemDeleted(int eventId);
    }

    private OnItemDeleteListener deleteListener;

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull EventTaskViewHolder holder, int position) {
        // Get the current event task from the list
        EventTaskEntity eventTaskEntity = eventTaskList.get(position);

        // Set the description of the event task
        holder.description.setText(eventTaskEntity.getDescription());

        String eventTypeString = "";
        if (eventTaskEntity.getEventType() != null) {
            switch (eventTaskEntity.getEventType()) {
                case ONCE:
                    eventTypeString = "Once";
                    break;
                case MONTHLY:
                    eventTypeString = "Monthly";
                    break;
                case YEARLY:
                    eventTypeString = "Yearly";
                    break;
            }
        }

        holder.repetition.setText(eventTypeString);
        holder.date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(eventTaskEntity.getSelectedDate())));

        // EditEventTask click handler
        holder.editEventTask.setOnClickListener(v -> {
            showPopupWindow(v, eventTaskList.get(position));
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
        return eventTaskList.size();
    }

    @SuppressLint("MissingInflatedId")
    private void showPopupWindow(View anchorView, EventTaskEntity eventTask) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_event_list, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout edit = popupView.findViewById(R.id.editTaskItem);
        LinearLayout delete = popupView.findViewById(R.id.deleteTaskItem);

        edit.setOnClickListener(v -> {
            //Toast.makeText(context, "Edit event", Toast.LENGTH_SHORT).show();
            AddEventBottomSheetFragment fragment = new AddEventBottomSheetFragment();

            // Pass the mode and event ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString("mode", "update");
            bundle.putLong("eventId", eventTask.getId());
            fragment.setArguments(bundle);

            // Open the BottomSheetFragment to view the event details
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
                EventReminderManager.cancelEventReminder(context.getApplicationContext(), (int) eventTask.getId());

                eventTaskViewModel.deleteTaskById(eventTask.getId());

                showEventSuccessfullyDeleted();
                dialog.dismiss();
                resetHighlight();
            });

            btnNo.setOnClickListener(v1 -> dialog.dismiss());

            popupWindow.dismiss(); // close PopupWindow
        });

        popupWindow.setClippingEnabled(false); // Allow to go beyond the screen border

        anchorView.post(() -> popupWindow.showAsDropDown(anchorView, -428, -70));
    }

    private static void showEventSuccessfullyDeleted() {
        EventFragment.eventSuccessfullyDeleted.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        EventFragment.eventSuccessfullyDeleted.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    // EventTaskViewHolder class for the RecyclerView
    // Represents a single item view in the RecyclerView
    static class EventTaskViewHolder extends RecyclerView.ViewHolder {
        // UI elements for displaying event task details
        TextView description;
        TextView date;
        ImageView editEventTask;
        TextView repetition;
        EventTaskViewModel viewModel;
        OnItemDeleteListener deleteListener;
        EventTaskAdapter adapter;

        // Constructor for initializing the UI elements
        public EventTaskViewHolder(@NonNull View itemView, EventTaskViewModel viewModel, OnItemDeleteListener deleteListener, EventTaskAdapter adapter) {
            super(itemView);
            description = itemView.findViewById(R.id.eventTaskTitle);
            date = itemView.findViewById(R.id.eventTaskDate);
            editEventTask = itemView.findViewById(R.id.iconEditEventListBlock);
            repetition = itemView.findViewById(R.id.eventRepetition);
            this.viewModel = viewModel;
            this.deleteListener = deleteListener;
            this.adapter = adapter;
        }
    }
}
