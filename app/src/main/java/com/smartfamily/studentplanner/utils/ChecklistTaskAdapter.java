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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;
import com.smartfamily.studentplanner.data.viewModel.ChecklistTaskViewModel;
import com.smartfamily.studentplanner.ui.EditChecklistTaskBottomSheetFragment;

import java.util.List;

public class ChecklistTaskAdapter extends RecyclerView.Adapter<ChecklistTaskAdapter.ChecklistTaskViewHolder> {

    private final List<ChecklistTaskEntity> checkListTaskList;
    private final Context context;
    private final ChecklistTaskViewModel checklistTaskViewModel;

    public ChecklistTaskAdapter(List<ChecklistTaskEntity> checkListTaskList, Context context, ChecklistTaskViewModel checklistTaskViewModel) {
        this.checkListTaskList = checkListTaskList;
        this.context = context;
        this.checklistTaskViewModel = checklistTaskViewModel;
    }

    // Add a method to update the checklist task list
    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<ChecklistTaskEntity> newTaskList) {
        checkListTaskList.clear();
        if (newTaskList != null) {
            checkListTaskList.addAll(newTaskList);
        }
        notifyDataSetChanged(); // Notify RecyclerView about data update
    }

    @NonNull
    @Override
    public ChecklistTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checklist_item_layout, parent, false);
        return new ChecklistTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistTaskViewHolder holder, int position) {
        // Get the current checklist task from the list
        ChecklistTaskEntity checkListTaskEntity = checkListTaskList.get(position);

        // Set the description of the checklist task
        holder.description.setText(checkListTaskEntity.getDescription());

        // Setting the state of a checkbox without a listener trigger
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(checkListTaskEntity.isCompleted());

        holder.editItemButton.setOnClickListener(v -> {
            showPopupWindowForChecklistItem(v, checkListTaskEntity.getId());
        });

        // Handle checkbox state changes
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the local checklist task state
            checkListTaskEntity.setCompleted(isChecked);
            // Updating the state in the database
            checklistTaskViewModel.updateTaskCompletion(checkListTaskEntity.getId(), isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return checkListTaskList.size();
    }

    // ChecklistTaskViewHolder class for the RecyclerView
    // Represents a single item view in the RecyclerView
    static class ChecklistTaskViewHolder extends RecyclerView.ViewHolder {
        // UI elements for displaying checklist task details
        TextView description;
        CheckBox checkBox;
        ImageView editItemButton;

        // Constructor for initializing the UI elements
        public ChecklistTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.checklistItemTitle);
            checkBox = itemView.findViewById(R.id.checkBoxChecklistItem);
            editItemButton = itemView.findViewById(R.id.iconEditChecklistItem);
        }
    }

    @SuppressLint("MissingInflatedId")
    private void showPopupWindowForChecklistItem(View anchorView, long checklistItemId) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_checklist_item, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout edit = popupView.findViewById(R.id.editItem);
        LinearLayout delete = popupView.findViewById(R.id.deleteItem);

        edit.setOnClickListener(v -> {
            EditChecklistTaskBottomSheetFragment fragment = new EditChecklistTaskBottomSheetFragment();

            Bundle bundle = new Bundle();
            bundle.putLong("checklistTaskId", checklistItemId);
            fragment.setArguments(bundle);

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                fragment.show(activity.getSupportFragmentManager(), fragment.getTag());
            }

            popupWindow.dismiss();
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
            messageText.setText(R.string.checklist_item_delete_message);

            btnYes.setOnClickListener(v1 -> {
                checklistTaskViewModel.deleteTaskById(checklistItemId);
                dialog.dismiss();
            });

            btnNo.setOnClickListener(v1 -> dialog.dismiss());

            popupWindow.dismiss();
        });

        popupWindow.setClippingEnabled(false);
        anchorView.post(() -> popupWindow.showAsDropDown(anchorView, -460, -70));
    }
}
