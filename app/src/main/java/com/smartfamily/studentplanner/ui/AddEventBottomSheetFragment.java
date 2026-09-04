package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.entity.EventType;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;
import com.smartfamily.studentplanner.reminder.EventReminderManager;
import com.smartfamily.studentplanner.utils.Utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class AddEventBottomSheetFragment extends BottomSheetDialogFragment {

    // Constants for fragment modes
    public static final String MODE_ADD = "add";
    public static final String MODE_UPDATE = "update";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private EventTaskViewModel eventTaskViewModel;
    private TextInputEditText descriptionInput;
    private MaterialButton pickDateButton;
    private Button saveTaskButton;
    private RadioGroup radioGroup;
    private EventType eventType;
    private long selectedDate;

    private long eventId;
    private String mode;

    private static final int YEARLY_REPETITIONS = 5;
    private static final int MONTHLY_REPETITIONS = 12;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_event_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();

        // Initialize the ViewModel for managing task-related data
        eventTaskViewModel = new ViewModelProvider(requireActivity()).get(EventTaskViewModel.class);

        // Find and initialize UI components
        descriptionInput = view.findViewById(R.id.taskDescriptionInput);
        radioGroup = view.findViewById(R.id.eventTypeRadioGroup);
        RadioButton eventOnce = view.findViewById(R.id.eventOnce);
        eventOnce.setChecked(true);
        pickDateButton = view.findViewById(R.id.pickDateButton);
        saveTaskButton = view.findViewById(R.id.saveTaskButton);

        // Set up button click listeners
        pickDateButton.setOnClickListener(v -> showDatePickerDialog());
        saveTaskButton.setOnClickListener(v -> saveTask());

        // Check the mode of the fragment (add/view/update)
        assert arguments != null;
        mode = arguments.getString("mode", MODE_ADD);

        if (MODE_ADD.equals(mode)) {
            // Configure the fragment for adding a new event
            setWindowMode(MODE_ADD);
        }

        if (MODE_UPDATE.equals(mode)) {
            // Configure the fragment for updating an existing event
            setWindowMode(MODE_UPDATE);
            eventId = requireArguments().getLong("eventId", -1);
            if (eventId != -1) {
                loadTaskData(eventId, MODE_UPDATE);
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
        // Observe task data by taskId from the ViewModel
        eventTaskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                // Populate the UI fields with task data
                descriptionInput.setText(task.getDescription());
                descriptionInput.setHint("");
                selectedDate = task.getSelectedDate();
                eventType = task.getEventType();
            }
            // If the mode is "update", configure the UI for read-only viewing
            if (task != null && mode.equals(MODE_UPDATE)) {
                String date = dateFormat.format(task.getSelectedDate());
                pickDateButton.setText(date);
            }

            if (eventType != null) {
                switch (eventType) {
                    case ONCE:
                        radioGroup.check(R.id.eventOnce);
                        break;
                    case MONTHLY:
                        radioGroup.check(R.id.eventMonthly);
                        break;
                    case YEARLY:
                        radioGroup.check(R.id.eventYearly);
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
        // Retrieve input values from the UI
        String description = descriptionInput.getText().toString().trim();

        // Validate input fields
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_description_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!description.matches(Utils.NO_MULTIPLE_SPACES_REGEX)) {
            Toast.makeText(requireContext(), R.string.too_much_spaces_description_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == 0) {
            Toast.makeText(requireContext(), R.string.empty_date_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        EventType eventType;

        if (selectedRadioButtonId == R.id.eventOnce) {
            eventType = EventType.ONCE;
        } else if (selectedRadioButtonId == R.id.eventMonthly) {
            eventType = EventType.MONTHLY;
        } else {
            eventType = EventType.YEARLY;
        }

        // Create a new TaskEntity with the collected input
        EventTaskEntity event = new EventTaskEntity(
                0, description, false, selectedDate, eventType
        );

        if (EventType.YEARLY.equals(eventType)) {
            event.setRepetitions(YEARLY_REPETITIONS);
        } else if (EventType.MONTHLY.equals(eventType)) {
            event.setRepetitions(MONTHLY_REPETITIONS);
        }

        Context applicationContext = requireContext().getApplicationContext();

        if (MODE_ADD.equals(mode)) {
            // If the mode is "add", insert a new event into the database
            eventTaskViewModel.insert(event, eventEntity -> {
                // Set a reminder for the new event
                EventReminderManager.setEventReminder(applicationContext, event);
                new Handler(Looper.getMainLooper()).post(() -> {
                    LocalDate lastEventDate = new Date(eventEntity.getSelectedDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    eventTaskViewModel.setLastTaskDate(lastEventDate);
                    showEventSuccessfullyAdded();
                    dismiss();
                });
            });
        } else {
            // If the mode is "update", update the existing event
            event.setId(eventId);  // Set the event ID to update the correct record
            EventReminderManager.cancelEventReminder(requireContext(), (int) eventId);  // Cancel any existing reminder
            eventTaskViewModel.update(event);  // Update the event in the database
            EventReminderManager.setEventReminder(requireContext(), event);  // Set a new reminder for the updated event
        }
        // Close the fragment after saving the event
        dismiss();
    }

    private static void showEventSuccessfullyAdded() {
        EventFragment.eventSuccessfullyAdded.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        EventFragment.eventSuccessfullyAdded.animate()
                                .alpha(0)
                                .setDuration(1500)
                );
    }

    private void setWindowMode(String mode) {

        if (mode.equals(MODE_UPDATE)) {

            //blue header
            EventFragment.makeBlur();

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