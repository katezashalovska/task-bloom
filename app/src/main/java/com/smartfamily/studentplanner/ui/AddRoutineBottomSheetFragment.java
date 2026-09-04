package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.dao.OnRoutineTaskInsertedListener;
import com.smartfamily.studentplanner.data.dao.OnTasksInsertedListener;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineType;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskPriority;
import com.smartfamily.studentplanner.data.viewModel.RoutineTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.reminder.RoutineReminderManager;
import com.smartfamily.studentplanner.utils.Utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddRoutineBottomSheetFragment extends BottomSheetDialogFragment {

    // Constants for fragment modes
    public static final String MODE_ADD = "add";
    public static final String MODE_UPDATE = "update";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final int ROUTINE_TITLE_LENGTH = 20;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private RoutineTaskViewModel routineTaskViewModel;
    private TaskViewModel taskViewModel;
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private MaterialButton pickDateButton;
    private Button saveTaskButton;
    private RoutineType selectedRoutineType;
    private long selectedDate;

    private long routineId;
    private String mode;

    private static final int YEARLY_REPETITIONS = 5;
    private static final int MONTHLY_REPETITIONS = 12;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_routine_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();

        // Initialize the ViewModel for managing task-related data
        routineTaskViewModel = new ViewModelProvider(requireActivity()).get(RoutineTaskViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Find and initialize UI components
        titleInput = view.findViewById(R.id.taskTitleInput);
        descriptionInput = view.findViewById(R.id.taskDescriptionInput);

        pickDateButton = view.findViewById(R.id.pickDateButton);
        saveTaskButton = view.findViewById(R.id.saveTaskButton);

        RadioButton routineDaily = view.findViewById(R.id.routineDaily);
        RadioButton routineWeekly = view.findViewById(R.id.routineWeekly);
        RadioButton routineMonthly = view.findViewById(R.id.routineMonthly);
        RadioButton routineAnnual = view.findViewById(R.id.routineAnnual);

        List<RadioButton> allRoutineTypeButtons = new ArrayList<>();
        allRoutineTypeButtons.add(routineDaily);
        allRoutineTypeButtons.add(routineWeekly);
        allRoutineTypeButtons.add(routineMonthly);
        allRoutineTypeButtons.add(routineAnnual);

        for (RadioButton button : allRoutineTypeButtons) {
            button.setOnClickListener(v -> {
                RoutineType clickedType = getRoutineTypeForString(((RadioButton) v).getText().toString());
                setSelectedRadioButton(clickedType);
            });
        }

        setSelectedRadioButton(RoutineType.DAILY);

        // Set up button click listeners
        pickDateButton.setOnClickListener(v -> showDatePickerDialog());
        saveTaskButton.setOnClickListener(v -> saveTask());

        // Check the mode of the fragment (add/view/update)
        assert arguments != null;
        mode = arguments.getString("mode", MODE_ADD);

        if (MODE_ADD.equals(mode)) {
            // Configure the fragment for adding a new routine
            setWindowMode(MODE_ADD);
        }

        if (MODE_UPDATE.equals(mode)) {
            // Configure the fragment for updating an existing routine
            setWindowMode(MODE_UPDATE);
            routineId = requireArguments().getLong("routineId", -1);
            if (routineId != -1) {
                loadTaskData(routineId, MODE_UPDATE);
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

    private void loadTaskData(long taskId, String mode) {
        routineTaskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                titleInput.setHint("");
                titleInput.setText(task.getTitle());
                descriptionInput.setText(task.getDescription());
                descriptionInput.setHint("");
                selectedDate = task.getSelectedDate();
                setSelectedRadioButton(task.getRoutineType());
            }

            if (task != null && mode.equals(MODE_UPDATE)) {
                String date = dateFormat.format(task.getSelectedDate());
                pickDateButton.setText(date);
            }

            View fragmentView = getView();

            if (fragmentView != null && this.selectedRoutineType != null) {
                switch (this.selectedRoutineType) {
                    case DAILY:
                        ((RadioButton) fragmentView.findViewById(R.id.routineDaily)).setChecked(true);
                        break;
                    case WEEKLY:
                        ((RadioButton) fragmentView.findViewById(R.id.routineWeekly)).setChecked(true);
                        break;
                    case MONTHLY:
                        ((RadioButton) fragmentView.findViewById(R.id.routineMonthly)).setChecked(true);
                        break;
                    case ANNUAL:
                        ((RadioButton) fragmentView.findViewById(R.id.routineAnnual)).setChecked(true);
                        break;
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDatePickerDialog() {
        // Create a Calendar instance to get the current date
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != 0) {
            calendar.setTime(new Date(selectedDate));
        }

        LocalDate today = LocalDate.now();

        // Initialize a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Callback triggered when the user selects a date
                    LocalDate selectedLocalDate = LocalDate.of(year, month + 1, dayOfMonth);
                    if (selectedLocalDate.isBefore(today)) {
                        Toast.makeText(requireContext(), R.string.date_before_today_alert, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime().getTime();
                    pickDateButton.setText(dateFormat.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Display the DatePickerDialog to the user
        datePickerDialog.show();

    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_title_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.length() > ROUTINE_TITLE_LENGTH) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.title_size_alert, ROUTINE_TITLE_LENGTH), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!title.matches(Utils.NO_MULTIPLE_SPACES_REGEX)) {
            Toast.makeText(requireContext(), R.string.too_much_spaces_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_description_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == 0) {
            Toast.makeText(requireContext(), R.string.empty_date_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (this.selectedRoutineType == null) {
            Toast.makeText(requireContext(), "Please select the type of routine.", Toast.LENGTH_SHORT).show();
            return;
        }

        RoutineTaskEntity routine = new RoutineTaskEntity(
                0, title, description, false, selectedDate, this.selectedRoutineType
        );

        if (RoutineType.ANNUAL.equals(this.selectedRoutineType)) {
            routine.setRepetitions(YEARLY_REPETITIONS);
        } else if (RoutineType.MONTHLY.equals(this.selectedRoutineType)) {
            routine.setRepetitions(MONTHLY_REPETITIONS);
        }

        if (MODE_ADD.equals(mode)) {
            // If the mode is "add", insert a new routine into the database
            routineTaskViewModel.insert(routine, new OnRoutineTaskInsertedListener() {
                @Override
                public void onRoutineTaskInserted(RoutineTaskEntity insertedRoutine) {
                    long newRoutineId = insertedRoutine.getId();
                    List<TaskEntity> generatedTasks = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(insertedRoutine.getSelectedDate());

                    // Set the TaskEntity generation period
                    int numberOfIterations = 0;
                    switch (insertedRoutine.getRoutineType()) {
                        case DAILY:
                            numberOfIterations = 365;
                            break;
                        case WEEKLY:
                            numberOfIterations = 52;
                            break;
                        case MONTHLY:
                            numberOfIterations = 12;
                            break;
                        case ANNUAL:
                            numberOfIterations = 1;
                            break;
                    }

                    long generatedDateMillis = -1;
                    for (int i = 0; i < numberOfIterations; i++) {
                        Calendar newRoutineCalendar = (Calendar) calendar.clone();

                        switch (insertedRoutine.getRoutineType()) {
                            case DAILY:
                                newRoutineCalendar.add(Calendar.DAY_OF_YEAR, i);
                                generatedDateMillis = newRoutineCalendar.getTimeInMillis();
                                break;
                            case WEEKLY:
                                newRoutineCalendar.add(Calendar.WEEK_OF_YEAR, i);
                                generatedDateMillis = newRoutineCalendar.getTimeInMillis();
                                break;
                            case MONTHLY:
                                newRoutineCalendar.add(Calendar.MONTH, i);
                                // Handle cases where the original day of the month may not exist in the new month
                                int originalDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                                if (newRoutineCalendar.get(Calendar.DAY_OF_MONTH) != originalDayOfMonth) {
                                    newRoutineCalendar.set(Calendar.DAY_OF_MONTH, newRoutineCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                }
                                generatedDateMillis = newRoutineCalendar.getTimeInMillis();
                                break;
                            case ANNUAL:
                                if (i == 0) {
                                    newRoutineCalendar.add(Calendar.YEAR, 0);
                                    generatedDateMillis = newRoutineCalendar.getTimeInMillis();
                                }
                                break;
                        }

                        if (generatedDateMillis != -1) {
                            TaskEntity generatedTask = new TaskEntity(
                                    0,
                                    insertedRoutine.getTitle(),
                                    insertedRoutine.getDescription(),
                                    new Date(generatedDateMillis),
                                    0.0, 0.0,
                                    false,
                                    TaskPriority.LOW,
                                    newRoutineId
                            );
                            generatedTasks.add(generatedTask);
                        }
                    }

                    if (!generatedTasks.isEmpty()) {
                        taskViewModel.insertAll(generatedTasks, new OnTasksInsertedListener() {
                            @Override
                            public void onTasksInserted() {
                                RoutineReminderManager.setRoutineReminder(requireContext(), insertedRoutine);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    taskViewModel.setLastTaskDate(LocalDate.now());
                                    showRoutineSuccessfullyAdded();
                                    dismiss();
                                });
                            }
                        });
                    }
                }
            });
        } else {
            // If the mode is "update", update the existing routine
            routine.setId(routineId);  // Set the routine ID to update the correct record
            RoutineReminderManager.cancelRoutineReminder(requireContext(), (int) routineId);  // Cancel any existing reminder
            routineTaskViewModel.update(routine);  // Update the routine in the database
            RoutineReminderManager.setRoutineReminder(requireContext(), routine);  // Set a new reminder for the updated routine
            // Update the corresponding TaskEntity
            taskViewModel.reconcileRoutineTasks(routine);
            dismiss();
        }
    }

    private static void showRoutineSuccessfullyAdded() {
        RoutineFragment.routineSuccessfullyAdded.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        RoutineFragment.routineSuccessfullyAdded.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    private void setWindowMode(String mode) {

        if (mode.equals(MODE_UPDATE)) {

            //blue header
            RoutineFragment.makeBlur();

            // Configure the UI for "update" mode
            descriptionInput.setHint(null);
            descriptionInput.setPadding(32, 32, 0, 32);
            descriptionInput.setFocusable(true);
            descriptionInput.setFocusableInTouchMode(true);
            descriptionInput.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            descriptionInput.setOnLongClickListener(v -> true);

            pickDateButton.setVisibility(View.VISIBLE);
            saveTaskButton.setVisibility(View.VISIBLE);
        }
    }

    private void setSelectedRadioButton(RoutineType typeToSelect) {
        View fragmentView = getView();
        if (fragmentView == null) {
            return;
        }

        RadioButton routineDaily = fragmentView.findViewById(R.id.routineDaily);
        RadioButton routineWeekly = fragmentView.findViewById(R.id.routineWeekly);
        RadioButton routineMonthly = fragmentView.findViewById(R.id.routineMonthly);
        RadioButton routineAnnual = fragmentView.findViewById(R.id.routineAnnual);

        routineDaily.setChecked(false);
        routineWeekly.setChecked(false);
        routineMonthly.setChecked(false);
        routineAnnual.setChecked(false);

        if (typeToSelect != null) {
            switch (typeToSelect) {
                case DAILY:
                    routineDaily.setChecked(true);
                    break;
                case WEEKLY:
                    routineWeekly.setChecked(true);
                    break;
                case MONTHLY:
                    routineMonthly.setChecked(true);
                    break;
                case ANNUAL:
                    routineAnnual.setChecked(true);
                    break;
            }
        }
        this.selectedRoutineType = typeToSelect;
    }

    private RoutineType getRoutineTypeForString(String typeString) {
        if (typeString == null) return null;
        if (typeString.equalsIgnoreCase(getString(R.string.daily))) {
            return RoutineType.DAILY;
        } else if (typeString.equalsIgnoreCase(getString(R.string.weekly))) {
            return RoutineType.WEEKLY;
        } else if (typeString.equalsIgnoreCase(getString(R.string.monthly))) {
            return RoutineType.MONTHLY;
        } else if (typeString.equalsIgnoreCase(getString(R.string.annual))) {
            return RoutineType.ANNUAL;
        }
        return null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        //this.dialog = dialog;
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