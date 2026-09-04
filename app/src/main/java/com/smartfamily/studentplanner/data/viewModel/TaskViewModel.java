package com.smartfamily.studentplanner.data.viewModel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.smartfamily.studentplanner.data.dao.OnTaskInsertedListener;
import com.smartfamily.studentplanner.data.dao.OnTasksInsertedListener;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskPriority;
import com.smartfamily.studentplanner.data.repository.TaskRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> allTasks;
    private final MutableLiveData<LocalDate> lastTaskDate = new MutableLiveData<>();
    private final MutableLiveData<Long> highlightedTaskId = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> deletedTaskDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> dateToNavigateTo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> routineTasksRefreshTrigger = new MutableLiveData<>();
    // Name for SharedPreferences that stores login state
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    public LiveData<LocalDate> getLastTaskDate() {
        return lastTaskDate;
    }

    public void setLastTaskDate(LocalDate date) {
        if (date != null) {
            lastTaskDate.postValue(date);
        }
    }

    public LiveData<LocalDate> getDeletedTaskDate() {
        return deletedTaskDate;
    }

    public void setDeletedTaskDate(LocalDate date) {
        deletedTaskDate.postValue(date);
    }

    // ViewModel constructor accepts Application to access the context
    public TaskViewModel(@NonNull Application application) {
        super(application);
        // Initialize the repository
        repository = new TaskRepository(application); // Pass the context to the repository

        // Get all tasks as LiveData
        allTasks = repository.getAllTasks();
    }

    // Method to get all tasks (LiveData)
    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    // Method to insert a new task
    public void insert(TaskEntity task, OnTaskInsertedListener listener) {
        repository.insert(task, listener);
    }

    // Inserts all existing task in the database
    public void insertAll(List<TaskEntity> tasks, OnTasksInsertedListener listener) {
        repository.insertAll(tasks, listener);
    }

    // Updates all existing task in the database
    public void updateAllTasks(List<TaskEntity> tasks) {
        repository.updateAllTasks(tasks);
    }

    // Method to update an existing task
    public void update(TaskEntity task) {
        repository.update(task);
    }

    // Method to delete  an existing task
    public void deleteTaskById(long taskId) {
        repository.deleteByTaskId(taskId);
    }

    // Method to delete all completed tasks
    public void deleteAllCompletedTasks() {
        repository.deleteAllCompletedTasks();
    }

    // Method to get a task by its ID (LiveData)
    public LiveData<TaskEntity> getTaskById(long taskId) {
        return repository.getTaskById(taskId);
    }

    // Updates the completion status of a task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        repository.updateTaskCompletion(taskId, isCompleted);
    }

    // Method to get tasks for a specific date (LiveData)
    public LiveData<List<TaskEntity>> getTasksForDate(Instant date) {
        // Convert Instant to LocalDate for querying (if your repository uses LocalDate)
        LocalDate localDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return repository.getTasksForDate(localDate);
    }

    // Retrieves ID of the task that should be highlighted in UI.
    public Long getHighlightedTaskId() {
        return highlightedTaskId.getValue();
    }

    // Sets ID of the task that should be highlighted in UI.
    public void setHighlightedTaskId(Long taskId) {
        highlightedTaskId.postValue(taskId);
    }

    // Sets the specific date to which UI should navigate or focus.
    public void setDateToNavigateTo(LocalDate date) {
        dateToNavigateTo.postValue(date);
    }

    // Get task by routineTaskId
    public LiveData<TaskEntity> getTaskByRoutineTaskId(long routineTaskId) {
        return repository.getTaskByRoutineTaskId(routineTaskId);
    }

    // Get list of tasks by routineTaskId
    public LiveData<List<TaskEntity>> getTasksByRoutineTaskId(long routineTaskId) {
        return repository.getTasksByRoutineTaskId(routineTaskId);
    }

    // READ: Get all eventTasks from the database
    public List<TaskEntity> getAllTasksBlocking() {
        return repository.getAllTasksBlocking();
    }

    // Updates the reminder sent status for a task.
    public void updateTaskReminderSentStatus(long taskId, boolean isReminderSent) {
        repository.updateTaskReminderSentStatus(taskId, isReminderSent);
    }

    // Resets the reminder sent status for all tasks to 'false'.
    public void resetAllTaskReminderSentStatus() {
        repository.resetAllTaskReminderSentStatus();
    }

    // Method to activate UI refresh trigger
    public void triggerRoutineTasksRefresh() {
        routineTasksRefreshTrigger.postValue(true);
    }

    // Method for initiating the process of reconcile tasks related to routine
    public void reconcileRoutineTasks(RoutineTaskEntity routine) {
        repository.reconcileRoutineTasks(routine);
        triggerRoutineTasksRefresh();
    }

    // Method to log out of the system
    public void logout() {
        // Logout of Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Clearing login state in SharedPreferences
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.apply();
    }

    // Checking login status
    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ||
                getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false);
    }

    // Setting login status
    public void setLoggedIn(boolean loggedIn) {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, loggedIn);
        editor.apply();
    }

    // Method for updating routine and future tasks
    public void updateRoutineAndFutureTasks(
            Long routineTaskId,
            Long currentInstanceTaskId,
            LocalDate newDueDate,
            String newTitle,
            String newDescription,
            TaskPriority newPriority,
            double newLatitude,
            double newLongitude
    ) {
        // Run operation in a background thread
        repository.updateRoutineAndFutureTasks(
                routineTaskId,
                currentInstanceTaskId,
                newDueDate,
                newTitle,
                newDescription
        );

        triggerRoutineTasksRefresh();
        setDateToNavigateTo(newDueDate);
    }
}