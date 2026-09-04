package com.smartfamily.studentplanner.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.ChecklistEntity;
import com.smartfamily.studentplanner.data.viewModel.ChecklistViewModel;
import com.smartfamily.studentplanner.utils.Utils;

public class AddChecklistBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String MODE_ADD = "add";
    private static final String MODE_UPDATE = "update";
    private static final int CHECKLIST_TITLE_LENGTH = 16;

    private TextInputEditText checklistTitle;
    private Button createChecklistButton;
    private ChecklistViewModel checklistViewModel;
    private String mode;
    private long checklistId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_checklist_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checklistTitle = view.findViewById(R.id.checklistTitleInput);
        createChecklistButton = view.findViewById(R.id.createChecklistTaskButton);
        createChecklistButton.setOnClickListener(v -> createCheckList());
        checklistViewModel = new ViewModelProvider(this).get(ChecklistViewModel.class);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();

        // Check the mode of the fragment (add/view/update)
        assert arguments != null;
        mode = arguments.getString("mode", MODE_ADD);

        if (MODE_ADD.equals(mode)) {
            // Configure the fragment for adding a new checklist
            setWindowMode(MODE_ADD);
        }

        if (MODE_UPDATE.equals(mode)) {
            setWindowMode(MODE_UPDATE);
            checklistId = requireArguments().getLong("checklistId");
            if (checklistId != -1) {
                loadTaskData(checklistId);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Retrieve the BottomSheetDialog instance
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            // Find the BottomSheet view inside the dialog
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // Configure the BottomSheetBehavior for the BottomSheet view
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                // Set the BottomSheet to always be fully expanded
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // Prevent the BottomSheet from collapsing to the "collapsed" state
                behavior.setSkipCollapsed(true);
            }
        }
    }

    private void setWindowMode(String mode) {
        if (mode.equals(MODE_UPDATE)) {
            //blue header
            ChecklistContainer.makeBlur();

            // Configure the UI for "update" mode
            checklistTitle.setHint(null);
            checklistTitle.setPadding(32, 32, 0, 32);
            checklistTitle.setFocusable(true);
            checklistTitle.setFocusableInTouchMode(true);
            checklistTitle.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            checklistTitle.setOnLongClickListener(v -> true);
        }
    }

    private void loadTaskData(long checklistId) {
        // Observe task data by checklistId from the ViewModel
        checklistViewModel.getChecklistById(checklistId).observe(getViewLifecycleOwner(), checklist -> {
            if (checklist != null) {
                // Populate the UI fields with checklist data
                checklistTitle.setText(checklist.getName());
                checklistTitle.setHint("");
            }
        });
    }

    private void createCheckList() {

        String checklistName = checklistTitle.getText().toString().trim();

        if (checklistName.isEmpty() || checklistName.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.empty_title_alert), Toast.LENGTH_SHORT).show();
            return;
        }

        if (checklistName.length() > CHECKLIST_TITLE_LENGTH) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.title_size_alert, CHECKLIST_TITLE_LENGTH), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checklistName.matches(Utils.NO_MULTIPLE_SPACES_REGEX)) {
            Toast.makeText(requireContext(), R.string.too_much_spaces_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (MODE_ADD.equals(mode)) {
            ChecklistEntity checklist = new ChecklistEntity(0, checklistName, true);
            checklistViewModel.insert(checklist);
            showChecklistSuccessfullyAdded();
            dismiss();

        } else {
            checklistViewModel.updateChecklistName(checklistId, checklistName);
            dismiss();
        }
    }

    private static void showChecklistSuccessfullyAdded() {
        ChecklistContainer.checklistSuccessfullyAdded.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        ChecklistContainer.checklistSuccessfullyAdded.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getActivity() instanceof OnBottomSheetDismissListener) {
            ((OnBottomSheetDismissListener) getActivity()).onBottomSheetDismissed();
        }
        // Get the BottomAppBar from the parent activity
        BottomAppBar bottomAppBar = requireActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performShow();
        }
    }
}
