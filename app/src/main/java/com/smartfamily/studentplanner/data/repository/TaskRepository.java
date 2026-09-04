package com.smartfamily.studentplanner.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.dao.OnTaskInsertedListener;
import com.smartfamily.studentplanner.data.dao.OnTasksInsertedListener;
import com.smartfamily.studentplanner.data.dao.RoutineTaskDao;
import com.smartfamily.studentplanner.data.dao.TaskDao;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineType;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskPriority;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final RoutineTaskDao routineTaskDao;
    private final LiveData<List<TaskEntity>> allTasks;

    private static final int DAILY_REPETITIONS = 365;
    private static final int WEEKLY_REPETITIONS = 48;
    private static final int MONTHLY_REPETITIONS = 12;
    private static final int ANNUAL_REPETITIONS = 1;
    private static final int DEFAULT_REPETITIONS = 0;

    /*
    The ExecutorService is used to run tasks in the background (off the main thread).
    For example, in your application, it can be used to run database operations
    or other time-consuming tasks without freezing the user interface.
     */

    // ExecutorService for background operations
    private final ExecutorService executorService;

    public TaskRepository(Application application) {
        // Get the database instance
        TaskDatabase database = TaskDatabase.getDatabase(application);

        // Initialize DAO
        taskDao = database.taskDao();
        routineTaskDao = database.routineTaskDao();

        // Fetch all tasks from the DAO
        allTasks = taskDao.getAllTasks();

        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(2);
    }

    // Returns all tasks as LiveData
    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    // Inserts a new task into the database
    public void insert(TaskEntity task, OnTaskInsertedListener listener) {
        executorService.execute(() -> {
            long id = taskDao.insert(task);
            task.setId(id);
            if (listener != null) {
                listener.onTaskInserted(task);
            }
        });
    }

    // Inserts all existing task in the database
    public void insertAll(List<TaskEntity> tasks, OnTasksInsertedListener listener) {
        executorService.execute(() -> {
            taskDao.insertAll(tasks);
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(listener::onTasksInserted);
            }
        });
    }

    // Updates all existing task in the database
    public void updateAllTasks(List<TaskEntity> tasks) {
        executorService.execute(() -> taskDao.updateAll(tasks));
    }

    // Updates an existing task in the database
    public void update(TaskEntity task) {
        executorService.execute(() -> taskDao.update(task));
    }

    // Deletes an existing task by id
    public void deleteByTaskId(long taskId) {
        executorService.execute(() -> taskDao.deleteByTaskId(taskId));
    }

    // Deletes all completed tasks
    public void deleteAllCompletedTasks() {
        executorService.execute(taskDao::deleteAllCompletedTasks);
    }

    // Retrieves a task by its ID
    public LiveData<TaskEntity> getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }

    // Updates the completion status of a task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        executorService.execute(() -> taskDao.updateTaskCompletion(taskId, isCompleted));
    }

    // Method to get tasks for a specific date (LiveData)
    public LiveData<List<TaskEntity>> getTasksForDate(LocalDate date) {
        return taskDao.getTasksForDate(date);
    }

    // Get task by routineTaskId
    public LiveData<TaskEntity> getTaskByRoutineTaskId(long routineTaskId) {
        return taskDao.getTaskByRoutineTaskId(routineTaskId);
    }

    // Get list of tasks by routineTaskId
    public LiveData<List<TaskEntity>> getTasksByRoutineTaskId(long routineTaskId) {
        return taskDao.getTasksByRoutineTaskId(routineTaskId);
    }

    // Gets all eventTasks from the database
    public List<TaskEntity> getAllTasksBlocking() {
        return taskDao.getAllTasksBlocking();
    }

    // Gets tasks by routine ID
    public List<TaskEntity> getTasksByRoutineTaskIdBlocking(long routineTaskId) {
        return taskDao.getTasksByRoutineTaskIdBlocking(routineTaskId);
    }

    // Updates the reminder sent status for a task.
    public void updateTaskReminderSentStatus(long taskId, boolean isReminderSent) {
        TaskDatabase.databaseWriteExecutor.execute(() -> taskDao.updateReminderSentStatus(taskId, isReminderSent));
    }

    // Resets the reminder sent status for all tasks to 'false'.
    public void resetAllTaskReminderSentStatus() {
        TaskDatabase.databaseWriteExecutor.execute(taskDao::resetAllTaskReminderSentStatus);
    }

    // Method for determining the number of tasks associated with a routine.
    // It updates existing tasks, inserts new ones, and removes redundant.
    public void reconcileRoutineTasks(RoutineTaskEntity routine) {
        executorService.execute(() -> {
            List<TaskEntity> existingTasks = getTasksByRoutineTaskIdBlocking(routine.getId());

            int targetRepetitionsCount = getRepetitionsCountForRoutineType(routine.getRoutineType());

            List<TaskEntity> tasksToUpdate = new ArrayList<>();
            List<TaskEntity> tasksToInsert = new ArrayList<>();
            List<Long> taskIdsToDelete = new ArrayList<>();

            Calendar baseCalendar = Calendar.getInstance();
            baseCalendar.setTime(new Date(routine.getSelectedDate()));

            // Task generation/update logic
            for (int i = 0; i < targetRepetitionsCount; i++) {
                TaskEntity task;
                if (i < existingTasks.size()) {
                    task = existingTasks.get(i); // Use an existing task
                } else {
                    // Create a new task (if the target quantity is greater than the existing ones)
                    task = createTaskEntity(routine);
                }

                // Check isUserModified before updating routine fields
                if (!task.isUserModified()) {
                    task.setTitle(routine.getTitle());
                    task.setDescription(routine.getDescription());
                    Calendar newRoutineCalendar = calculateNewDueDate(routine, baseCalendar, i);
                    task.setDueDate(newRoutineCalendar.getTime());
                } else {
                    Log.d("TaskRepository", "Skipping routine update for user-modified task: " + task.getTitle() + " (ID: " + task.getId() + ")");
                }

                // Calculate a new due date for all tasks
                Calendar newRoutineCalendar = calculateNewDueDate(routine, baseCalendar, i);
                task.setDueDate(newRoutineCalendar.getTime());

                // Logic for determining whether to update or insert a new task
                if (task.getId() == 0) {
                    tasksToInsert.add(task);
                } else {
                    tasksToUpdate.add(task);
                }
            }

            // Logic for marking redundant tasks for deletion
            if (existingTasks.size() > targetRepetitionsCount) {
                // Delete only those tasks that were not manually modified
                taskIdsToDelete.addAll(existingTasks.stream()
                        .skip(targetRepetitionsCount)
                        .filter(task -> !task.isUserModified()) // Filter tasks modified by user
                        .map(TaskEntity::getId)
                        .collect(Collectors.toList()));

                // We log any user-modified tasks that would otherwise be deleted
                existingTasks.stream()
                        .skip(targetRepetitionsCount)
                        .filter(TaskEntity::isUserModified)
                        .forEach(task -> Log.d("TaskRepository", "Preserving user-modified task from deletion: " + task.getTitle() + " (ID: " + task.getId() + ")"));
            }

            // Perform all database operations in one transaction
            generateNewTasks(taskIdsToDelete, tasksToUpdate, tasksToInsert);
        });
    }

    // Method for performing all database operations in one transaction
    private void generateNewTasks(List<Long> taskIdsToDelete, List<TaskEntity> tasksToUpdate, List<TaskEntity> tasksToInsert) {
        taskDao.runInTransaction(() -> {
            if (!taskIdsToDelete.isEmpty()) {
                taskDao.deleteTasksByIds(taskIdsToDelete);
            }
            if (!tasksToUpdate.isEmpty()) {
                taskDao.updateAll(tasksToUpdate);
            }
            if (!tasksToInsert.isEmpty()) {
                taskDao.insertAll(tasksToInsert);
            }
        });
    }

    // Method for calculating a new due date
    private static @NonNull Calendar calculateNewDueDate(RoutineTaskEntity routine, Calendar baseCalendar, int i) {
        Calendar newRoutineCalendar = (Calendar) baseCalendar.clone();
        switch (routine.getRoutineType()) {
            case DAILY:
                newRoutineCalendar.add(Calendar.DAY_OF_YEAR, i);
                break;
            case WEEKLY:
                newRoutineCalendar.add(Calendar.WEEK_OF_YEAR, i);
                break;
            case MONTHLY:
                newRoutineCalendar.add(Calendar.MONTH, i);
                int originalDayOfMonth = baseCalendar.get(Calendar.DAY_OF_MONTH);
                if (newRoutineCalendar.get(Calendar.DAY_OF_MONTH) != originalDayOfMonth) {
                    newRoutineCalendar.set(Calendar.DAY_OF_MONTH, newRoutineCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                }
                break;
            case ANNUAL:
                newRoutineCalendar.add(Calendar.YEAR, i);
                break;
        }
        return newRoutineCalendar;
    }

    // Method for creating a new task
    private static @NonNull TaskEntity createTaskEntity(RoutineTaskEntity routine) {
        TaskEntity task;
        task = new TaskEntity();
        task.setRoutineTaskId(routine.getId());
        task.setCompleted(false);
        task.setTaskPriority(TaskPriority.LOW);
        task.setTitle(routine.getTitle());
        task.setDescription(routine.getDescription());
        task.setLatitude(0.0);
        task.setLongitude(0.0);
        task.setUserModified(false);
        return task;
    }

    // Method for determining the number of repetitions based on RoutineType
    private int getRepetitionsCountForRoutineType(RoutineType routineType) {
        switch (routineType) {
            case DAILY:
                return DAILY_REPETITIONS;
            case WEEKLY:
                return WEEKLY_REPETITIONS;
            case MONTHLY:
                return MONTHLY_REPETITIONS;
            case ANNUAL:
                return ANNUAL_REPETITIONS;
            default:
                return DEFAULT_REPETITIONS;
        }
    }

    // Method for updating routine and future tasks
    public void updateRoutineAndFutureTasks(
            Long routineTaskId,
            Long currentInstanceTaskId,
            LocalDate newDueDate,
            String newTitle,
            String newDescription
    ) {
        executorService.execute(() -> {
            RoutineTaskEntity routine = routineTaskDao.getRoutineTaskByIdBlocking(routineTaskId);

            if (routine == null) {
                return;
            }

            // Update the fields of the RoutineTaskEntity template based on the new values passed
            routine.setTitle(newTitle);
            routine.setDescription(newDescription);
            routine.setSelectedDate(Date.from(newDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());

            routineTaskDao.update(routine);

            // Call reconcileRoutineTasks with the updated routine.
            reconcileRoutineTasks(routine);
        });
    }
}