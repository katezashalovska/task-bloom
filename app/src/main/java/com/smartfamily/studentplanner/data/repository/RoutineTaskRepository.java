package com.smartfamily.studentplanner.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.dao.OnRoutineTaskInsertedListener;
import com.smartfamily.studentplanner.data.dao.RoutineTaskDao;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoutineTaskRepository {
    private final RoutineTaskDao routineTaskDao;

    // ExecutorService for background operations
    private final ExecutorService executorService;

    public RoutineTaskRepository(Application application) {
        // Get the database instance
        TaskDatabase database = TaskDatabase.getDatabase(application);

        // Initialize DAO
        routineTaskDao = database.routineTaskDao();

        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(2);
    }

    public RoutineTaskEntity getRoutineById(int routineId) {
        return routineTaskDao.getRoutineById(routineId);
    }

    // Returns all routine tasks as LiveData related to a specific page
    public LiveData<List<RoutineTaskEntity>> getTasksForPage(int pageIndex) {
        return routineTaskDao.getRoutinesForPage(pageIndex);
    }

    public LiveData<List<RoutineTaskEntity>> getRoutinesForDate(long selectedDate) {
        return routineTaskDao.getRoutinesForDate(selectedDate);
    }

    // Inserts a new routine task into the database
    public void insert(RoutineTaskEntity task, OnRoutineTaskInsertedListener listener) {
        executorService.execute(() -> {
            long id = routineTaskDao.insert(task);
            task.setId(id);
            if (listener != null) {
                listener.onRoutineTaskInserted(task);
            }
        });
    }

    // Updates an existing routine task in the database
    public void update(RoutineTaskEntity task) {
        executorService.execute(() -> routineTaskDao.update(task));
    }

    // Deletes an existing routine task by id
    public void deleteByTaskId(long taskId) {
        executorService.execute(() -> routineTaskDao.deleteByTaskId(taskId));
    }

    // Deletes all completed routine tasks related to a specific page
    public void deleteAllCompletedTasks(int pageIndex) {
        executorService.execute(() -> routineTaskDao.deleteAllCompletedTasks(pageIndex));
    }

    // Retrieves a routine task by its ID
    public LiveData<RoutineTaskEntity> getTaskById(long taskId) {
        return routineTaskDao.getRoutineTaskById(taskId);
    }

    // Updates the completion status of a routine task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        executorService.execute(() -> routineTaskDao.updateTaskCompletion(taskId, isCompleted));
    }

    public void delete(RoutineTaskEntity routine) {
        executorService.execute(() -> routineTaskDao.delete(routine));
    }

    public LiveData<List<RoutineTaskEntity>> getAllRoutines() {
        return routineTaskDao.getAllRoutines();
    }
}
