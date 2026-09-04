package com.smartfamily.studentplanner.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.ChecklistTaskViewModel;

public class EditChecklistTaskBottomSheetFragment extends BottomSheetDialogFragment {
    private static final int CHECKLIST_ITEM_LENGTH = 30;
    private long checklistItemId;
    private TextInputEditText checklistTitleInput;
    private ChecklistTaskViewModel checklistTaskViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_checklist_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        checklistTitleInput = view.findViewById(R.id.checklistItemNameInput);
        Button saveButton = view.findViewById(R.id.saveItemNameButton);
        checklistTaskViewModel = new ViewModelProvider(this).get(ChecklistTaskViewModel.class);

        checklistItemId = requireArguments().getLong("checklistTaskId");

        checklistTaskViewModel.getTaskById(checklistItemId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                checklistTitleInput.setText(task.getDescription());
            }
        });

        saveButton.setOnClickListener(v -> {
            String newText = checklistTitleInput.getText().toString().trim();
            if (newText.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.empty_title_alert), Toast.LENGTH_SHORT).show();
                return;
            }

            if (newText.length() > CHECKLIST_ITEM_LENGTH) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.checklist_item_size_alert, CHECKLIST_ITEM_LENGTH), Toast.LENGTH_SHORT).show();
                return;
            }

            checklistTaskViewModel.getTaskById(checklistItemId).observe(getViewLifecycleOwner(), existingTask -> {
                if (existingTask != null) {
                    existingTask.setDescription(newText);
                    checklistTaskViewModel.update(existingTask);
                    dismiss();
                }
            });
        });
    }
}