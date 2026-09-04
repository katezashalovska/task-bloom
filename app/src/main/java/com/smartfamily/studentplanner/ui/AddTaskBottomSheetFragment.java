package com.smartfamily.studentplanner.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskPriority;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.reminder.TaskReminderManager;
import com.smartfamily.studentplanner.utils.SpeechRecognitionListener;
import com.smartfamily.studentplanner.utils.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTaskBottomSheetFragment extends BottomSheetDialogFragment {

    // Constants for fragment modes
    public static final String MODE_ADD = "add";
    public static final String MODE_UPDATE = "update";
    private static final String NO_LOCATION = "No Location";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final int TASK_TITLE_LENGTH = 20;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private TaskViewModel taskViewModel;
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private MaterialButton pickDateButton;
    private Button saveTaskButton;
    private MaterialButton locationButton;
    private RadioGroup radioGroup;
    private RadioButton highPriorityButton;
    private RadioButton mediumPriorityButton;
    private RadioButton lowPriorityButton;
    private TaskPriority taskPriority;
    private double latitude;
    private double longitude;
    private Date selectedDate;
    private long taskId;
    private Long routineTaskId;
    private String mode;
    private Button microphoneButton;
    private SpeechRecognizer speechRecognizer;
    private ActivityResultLauncher<Intent> mapsActivityLauncher;
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    public static final String REQUEST_KEY = "task_saved_request_key";
    public static final String RESULT_TASK_DATE = "saved_task_date";
    public static final String RESULT_TASK_ID = "saved_task_id";
    private Date originalDueDate;
    private boolean originalIsUserModified;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve arguments passed to this fragment
        Bundle arguments = getArguments();

        // Initialize the ViewModel for managing task-related data
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Register a launcher for MapsActivity to get location data
        mapsActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        latitude = data.getDoubleExtra("latitude", 0.0);
                        longitude = data.getDoubleExtra("longitude", 0.0);
                        locationButton.setText(getLocation(latitude, longitude));
                        Toast.makeText(requireContext(), R.string.toast_location_set_success, Toast.LENGTH_SHORT).show();
                    }
                });

        // Register a launcher for speech recognition
        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> speechResults = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (speechResults != null && !speechResults.isEmpty()) {
                            String spokenText = " " + speechResults.get(0).trim() + " ";

                            EditText activeInput = titleInput.hasFocus() ? titleInput : (descriptionInput.hasFocus() ? descriptionInput : null);
                            if (activeInput != null) {
                                int start = Math.max(activeInput.getSelectionStart(), 0);
                                int end = Math.max(activeInput.getSelectionEnd(), 0);
                                activeInput.getText().replace(Math.min(start, end), Math.max(start, end), spokenText, 0, spokenText.length());
                            }
                        }
                    }
                }
        );

        // Find and initialize UI components
        titleInput = view.findViewById(R.id.taskTitleInput);
        descriptionInput = view.findViewById(R.id.taskDescriptionInput);
        microphoneButton = view.findViewById(R.id.microphoneButton);
        radioGroup = view.findViewById(R.id.taskPriorityGroup);
        highPriorityButton = view.findViewById(R.id.highPriority);
        mediumPriorityButton = view.findViewById(R.id.mediumPriority);
        lowPriorityButton = view.findViewById(R.id.lowPriority);
        pickDateButton = view.findViewById(R.id.pickDateButton);
        saveTaskButton = view.findViewById(R.id.saveTaskButton);
        locationButton = view.findViewById(R.id.locationButton);

        // Initialize SpeechRecognizer and set a custom recognition listener
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener((AppCompatActivity) requireContext(), titleInput, descriptionInput));

        // Set up button click listeners
        pickDateButton.setOnClickListener(v -> showDatePickerDialog());
        saveTaskButton.setOnClickListener(v -> saveTask());
        microphoneButton.setOnClickListener(v -> startSpeechToText());
        locationButton.setOnClickListener(v -> openMapsActivity());

        // Check the mode of the fragment (add/view/update)
        assert arguments != null;
        mode = arguments.getString("mode", MODE_ADD);

        if (MODE_ADD.equals(mode)) {
            // Configure the fragment for adding a new task
            setWindowMode(MODE_ADD);
        }

        if (MODE_UPDATE.equals(mode)) {
            // Configure the fragment for updating an existing task
            setWindowMode(MODE_UPDATE);
            taskId = requireArguments().getLong("taskId", -1);
            if (taskId != -1) {
                loadTaskData(taskId, MODE_UPDATE);
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
        taskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                // Populate the UI fields with task data
                titleInput.setHint("");
                titleInput.setText(task.getTitle());
                descriptionInput.setText(task.getDescription());
                descriptionInput.setHint("");
                selectedDate = task.getDueDate();
                latitude = task.getLatitude();
                longitude = task.getLongitude();
                taskPriority = task.getTaskPriority();
                routineTaskId = task.getRoutineTaskId();
                this.originalDueDate = task.getDueDate();
                this.originalIsUserModified = task.isUserModified();

            }
            // If the mode is "update", configure the UI for read-only viewing
            if (task != null && mode.equals(MODE_UPDATE)) {
                String date = dateFormat.format(task.getDueDate());
                pickDateButton.setText(date);

                String location = getLocation(task.getLatitude(), task.getLongitude());
                locationButton.setText(location);
            }

            if (taskPriority != null) {
                switch (taskPriority) {
                    case HIGH:
                        highPriorityButton.setChecked(true);
                        break;
                    case MEDIUM:
                        mediumPriorityButton.setChecked(true);
                        break;
                    case LOW:
                        lowPriorityButton.setChecked(true);
                        break;
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDatePickerDialog() {
        // Create a Calendar instance to get the current date
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }
        // Initialize a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Callback triggered when the user selects a date
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
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
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // Validate input fields
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.empty_title_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.length() > TASK_TITLE_LENGTH) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.title_size_alert, TASK_TITLE_LENGTH), Toast.LENGTH_SHORT).show();
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

        if (selectedDate == null) {
            Toast.makeText(requireContext(), R.string.empty_date_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.before(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
            Toast.makeText(requireContext(), R.string.date_before_today_alert, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        TaskPriority taskPriority;

        if (selectedRadioButtonId == R.id.highPriority) {
            taskPriority = TaskPriority.HIGH;
        } else if (selectedRadioButtonId == R.id.mediumPriority) {
            taskPriority = TaskPriority.MEDIUM;
        } else {
            taskPriority = TaskPriority.LOW;
        }

        // Create a new TaskEntity with the collected input
        TaskEntity task = new TaskEntity(
                taskId, title, description, selectedDate, latitude, longitude, false, taskPriority, routineTaskId);

        task.setUserModified(originalIsUserModified);

        if (MODE_ADD.equals(mode)) {
            // If the mode is "add", insert a new task into the database
            taskViewModel.insert(task, taskEntity -> {
                LocalDate lastTaskDate = taskEntity.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                taskViewModel.setLastTaskDate(lastTaskDate);
                // Set a reminder for the new task using the taskEntity with the generated ID
                TaskReminderManager.setTaskReminder(requireContext(), taskEntity);
                showTaskSuccessfullyAdded();
                // Set the result for HomeFragment so it can jump to the date of the new task
                Bundle result = new Bundle();
                result.putSerializable(RESULT_TASK_DATE, lastTaskDate);
                result.putInt(RESULT_TASK_ID, (int) taskEntity.getId());
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                dismiss();
            });
        } else { // MODE_UPDATE
            // Checking if a task is recurring
            boolean isRecurringTask = task.getRoutineTaskId() != null;
            boolean dateHasChanged = !selectedDate.equals(originalDueDate);

            if (isRecurringTask && dateHasChanged && !originalIsUserModified) {
                // Show a modal window with a question
                showRecurringTaskUpdateDialog(task);
            } else {
                TaskReminderManager.cancelTaskReminder(requireContext(), (int) task.getId());
                taskViewModel.update(task);
                TaskReminderManager.setTaskReminder(requireContext(), task);

                // We send the result to HomeFragment and close the fragment
                LocalDate updatedTaskDate = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                Bundle result = new Bundle();
                result.putSerializable(RESULT_TASK_DATE, updatedTaskDate);
                result.putInt(RESULT_TASK_ID, (int) task.getId());
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                dismiss();
            }
        }
    }

    private static void showTaskSuccessfullyAdded() {
        HomeFragment.taskSuccessfullyAdded.animate()
                .alpha(1)
                .setDuration(1500)
                .withEndAction(() ->
                        HomeFragment.taskSuccessfullyAdded.animate()
                                .alpha(0)
                                .setDuration(1500));
    }

    private void setWindowMode(String mode) {
        if (mode.equals(MODE_UPDATE)) {
            //blue header
            HomeFragment.makeBlur();

            // Configure the UI for "update" mode
            titleInput.setHint(null);
            titleInput.setPadding(32, 32, 0, 32);

            descriptionInput.setHint(null);
            descriptionInput.setPadding(32, 32, 0, 32);
            descriptionInput.setFocusable(true);
            descriptionInput.setFocusableInTouchMode(true);
            descriptionInput.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            descriptionInput.setOnLongClickListener(v -> true);

            microphoneButton.setVisibility(View.VISIBLE);
            pickDateButton.setVisibility(View.VISIBLE);
            saveTaskButton.setVisibility(View.VISIBLE);
            locationButton.setVisibility(View.VISIBLE);
        }
    }

    // Function to start SpeechToText
    private void startSpeechToText() {
        // Create an Intent for starting speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify that we want to use free-form language model for speech recognition
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Set the language for speech recognition to the default locale of the device
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            // Start the activity for speech recognition and pass a request code for identifying the result
            speechRecognitionLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            // In case speech recognition is not supported on the device, show a toast message
            Toast.makeText(requireContext(), R.string.speech_recognition_alert, Toast.LENGTH_SHORT).show();
        }
    }

    private void openMapsActivity() {
        if (!isInternetAvailable()) {
            Toast.makeText(requireContext(), getString(R.string.net_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an intent to launch the MapsActivity
        Intent intent = new Intent(requireContext(), MapsActivity.class);
        // Check if the mode is "update" and if taskId is valid
        if (taskId != -1 && mode.equals(MODE_UPDATE)) {
            // Pass additional data to MapsActivity: indicating view-only mode and providing coordinates
            intent.putExtra("latitude", latitude); // Transfer the coordinates
            intent.putExtra("longitude", longitude);
            intent.putExtra("location", getLocation(latitude, longitude));
        }
        // Launch the MapsActivity using the registered activity launcher
        mapsActivityLauncher.launch(intent);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    // Method getting location by coordinates
    private String getLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String location = addresses.get(0).getAddressLine(0);
                return location != null && !location.isBlank() ? location : NO_LOCATION; // get location
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NO_LOCATION; // return plug, if we didn't get address
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

    private void showRecurringTaskUpdateDialog(TaskEntity updatedTask) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.recurring_task_update_title)
                .setMessage(R.string.recurring_task_update_message)
                .setPositiveButton(R.string.update_only_this_task, (dialog, which) -> {
                    // User selected "Update this task only"
                    updatedTask.setUserModified(true);
                    TaskReminderManager.cancelTaskReminder(requireContext(), (int) updatedTask.getId());
                    taskViewModel.update(updatedTask);
                    TaskReminderManager.setTaskReminder(requireContext(), updatedTask);

                    // Send the result and close the fragment
                    LocalDate updatedTaskDate = updatedTask.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Bundle result = new Bundle();
                    result.putSerializable(RESULT_TASK_DATE, updatedTaskDate);
                    result.putInt(RESULT_TASK_ID, (int) updatedTask.getId());
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                    dismiss();
                })
                .setNegativeButton(R.string.update_all_future_tasks, (dialog, which) -> {
                    // User selected "Update all future tasks"
                    TaskReminderManager.cancelTaskReminder(requireContext(), (int) updatedTask.getId());

                    taskViewModel.updateRoutineAndFutureTasks(
                            updatedTask.getRoutineTaskId(),
                            updatedTask.getId(),
                            updatedTask.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            updatedTask.getTitle(),
                            updatedTask.getDescription(),
                            updatedTask.getTaskPriority(),
                            updatedTask.getLatitude(),
                            updatedTask.getLongitude()
                    );
                    LocalDate updatedTaskDate = updatedTask.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Bundle result = new Bundle();
                    result.putSerializable(RESULT_TASK_DATE, updatedTaskDate);
                    result.putInt(RESULT_TASK_ID, (int) updatedTask.getId());
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                    dismiss();
                })
                .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}