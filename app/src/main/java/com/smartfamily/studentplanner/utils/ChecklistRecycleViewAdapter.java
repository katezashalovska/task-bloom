package com.smartfamily.studentplanner.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.ChecklistEntity;
import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;
import com.smartfamily.studentplanner.data.viewModel.ChecklistTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.ChecklistViewModel;
import com.smartfamily.studentplanner.ui.AddChecklistBottomSheetFragment;
import com.smartfamily.studentplanner.ui.ChecklistContainer;

import java.util.ArrayList;
import java.util.List;

public class ChecklistRecycleViewAdapter extends
        RecyclerView.Adapter<ChecklistRecycleViewAdapter.ChecklistViewHolder> {
    private static final int CHECKLIST_ITEM_LENGTH = 30;
    private final List<ChecklistEntity> checklistEntities;
    private final Context context;
    private final ChecklistViewModel checklistViewModel;
    private final ChecklistTaskViewModel checklistTaskViewModel;
    private final LifecycleOwner lifecycleOwner;
    private int totalChecklistItems;
    private int totalCheckedChecklistItems;

    public ChecklistRecycleViewAdapter(List<ChecklistEntity> checklistEntities,
                                       Context context,
                                       ChecklistViewModel checklistViewModel,
                                       ChecklistTaskViewModel checklistTaskViewModel,
                                       LifecycleOwner lifecycleOwner) {
        this.checklistEntities = checklistEntities;
        this.context = context;
        this.checklistViewModel = checklistViewModel;
        this.checklistTaskViewModel = checklistTaskViewModel;
        this.lifecycleOwner = lifecycleOwner;

    }

    // Add a method to update the task list
    @SuppressLint("NotifyDataSetChanged")
    public void setChecklists(List<ChecklistEntity> newChecklistList) {
        checklistEntities.clear();
        if (newChecklistList != null) {
            checklistEntities.addAll(newChecklistList);
        }
        notifyDataSetChanged(); // Notify RecyclerView about data update
    }


    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checklist_block_layout_new, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        ChecklistEntity checklist = checklistEntities.get(position);

        holder.checklistTitle.setText(checklist.getName());

        ChecklistTaskAdapter adapter = new ChecklistTaskAdapter(new ArrayList<>(), context, checklistTaskViewModel);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setAdapter(adapter);

        holder.checklistContentLayout.setVisibility(checklist.isExpanded() ? View.VISIBLE : View.GONE);

        holder.checklistHeaderLayout.setOnClickListener(v -> {
            if (adapter.getItemCount() == 0) return;

            boolean currentlyVisible = holder.checklistContentLayout.getVisibility() == View.VISIBLE;

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                holder.checklistContentLayout.setVisibility(currentlyVisible ? View.GONE : View.VISIBLE);
            }, 300);
            checklist.setExpanded(!currentlyVisible);
            checklistViewModel.update(checklist);
        });

        holder.editChecklistTitle.setOnClickListener(v -> {
            showPopupWindow(v, checklist.getId());
        });

        holder.addItem.setOnClickListener(v-> addChecklistItem(holder, checklist));

        holder.recyclerView.setTag(checklist.getId());

        checklistTaskViewModel.getAllTasks(checklist.getId()).observe(lifecycleOwner, checkListTaskEntities -> {
            Object tag = holder.recyclerView.getTag();
            if (tag instanceof Long && (Long) tag != checklist.getId()) {
                return;
            }

            if (checkListTaskEntities == null || checkListTaskEntities.isEmpty()) {
                holder.progressLayout.setVisibility(View.GONE);
                adapter.setTasks(checkListTaskEntities);

            } else {

                holder.progressLayout.setVisibility(View.VISIBLE);
                totalChecklistItems = checkListTaskEntities.size();
                totalCheckedChecklistItems = (int) checkListTaskEntities.stream()
                        .filter(ChecklistTaskEntity::isCompleted)
                        .count();

                holder.progressBar.setMax(totalChecklistItems);
                holder.progressBar.setProgress(totalCheckedChecklistItems);
                holder.progressText.setText(context.getString(R.string.checklist_progress,
                        totalCheckedChecklistItems,
                        totalChecklistItems));
                // Update the adapter when tasks are updated
                adapter.setTasks(checkListTaskEntities);
            }
        });

    }

    private void addChecklistItem(@NonNull ChecklistViewHolder holder, ChecklistEntity checklist) {
        String description = holder.enterItem.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.empty_title_alert), Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.length() > CHECKLIST_ITEM_LENGTH) {
            Toast.makeText(context, context.getString(R.string.checklist_item_size_alert, CHECKLIST_ITEM_LENGTH), Toast.LENGTH_SHORT).show();
            return;
        }

        ChecklistTaskEntity checkListTaskEntity = new ChecklistTaskEntity(0, checklist.getId(), description, false);
        checklistTaskViewModel.insert(checkListTaskEntity);
        holder.enterItem.setText("");
    }

    @Override
    public int getItemCount() {
        return checklistEntities.size();
    }

    @SuppressLint("MissingInflatedId")
    private void showPopupWindow(View anchorView, long checklistId) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_checklist, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout edit = popupView.findViewById(R.id.editChecklist);
        LinearLayout delete = popupView.findViewById(R.id.deleteChecklist);

        edit.setOnClickListener(v -> {

            AddChecklistBottomSheetFragment fragment = new AddChecklistBottomSheetFragment();

            // Pass the mode and task ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString("mode", "update");
            bundle.putLong("checklistId", checklistId);
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
            messageText.setText(R.string.checklist_delete_message);

            btnYes.setOnClickListener(v1 -> {
                checklistViewModel.deleteChecklistById(checklistId);
                checklistTaskViewModel.deleteAllChecklistTasksByChecklistId(checklistId);
                showChecklistSuccessfullyDeleted();

                dialog.dismiss();
            });

            btnNo.setOnClickListener(v1 -> dialog.dismiss());

            popupWindow.dismiss(); // close PopupWindow
        });

        popupWindow.setClippingEnabled(false); // Allow to go beyond the screen border

        anchorView.post(() -> popupWindow.showAsDropDown(anchorView, -528, -70));
    }

    private static void showChecklistSuccessfullyDeleted() {
        ChecklistContainer.checklistSuccessfullyDeleted.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        ChecklistContainer.checklistSuccessfullyDeleted.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        private TextView checklistTitle;
        private ImageView editChecklistTitle;
        private LinearLayout progressLayout;
        private ProgressBar progressBar;
        private TextView progressText;
        private EditText enterItem;
        private ImageButton addItem;
        private RecyclerView recyclerView;
        private LinearLayout checklistHeaderLayout;
        private LinearLayout checklistContentLayout;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            checklistTitle = itemView.findViewById(R.id.checkListName);
            editChecklistTitle = itemView.findViewById(R.id.iconEditChecklistBlock);
            progressLayout = itemView.findViewById(R.id.progressBarLayout);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            enterItem = itemView.findViewById(R.id.checklistItemEditText);
            addItem = itemView.findViewById(R.id.addChecklistItemButton);
            recyclerView = itemView.findViewById(R.id.checkListItemRecyclerView);
            checklistHeaderLayout = itemView.findViewById(R.id.checklistHeaderLayout);
            checklistContentLayout = itemView.findViewById(R.id.checklistContentLayout);
        }
    }
}
