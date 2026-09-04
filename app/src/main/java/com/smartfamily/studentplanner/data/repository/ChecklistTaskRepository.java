package com.smartfamily.studentplanner.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.dao.ChecklistTaskDao;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChecklistTaskRepository {
    private final ChecklistTaskDao checklistTaskDao;

    // ExecutorService for background operations
    private final ExecutorService executorService;

    public ChecklistTaskRepository(Application application) {
        // Get the database instance
        TaskDatabase database = TaskDatabase.getDatabase(application);

        // Initialize DAO
        checklistTaskDao = database.checklistTaskDao();

        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(2);
    }

    // Returns all checklist tasks as LiveData
    public LiveData<List<ChecklistTaskEntity>> getAllTasks(long checklistId) {
        return checklistTaskDao.getAllCheckListTasksByChecklistId(checklistId);
    }

    // Inserts a new checklist task into the database
    public void insert(ChecklistTaskEntity task) {
        executorService.execute(() -> checklistTaskDao.insert(task));
    }

    // Updates an existing checklist task in the database
    public void update(ChecklistTaskEntity task) {
        executorService.execute(() -> checklistTaskDao.update(task));
    }

    // Deletes an existing checklist task by id
    public void deleteByTaskId(long taskId) {
        executorService.execute(() -> checklistTaskDao.deleteByTaskId(taskId));
    }

    // Deletes all completed checklist tasks
    public void deleteAllCompletedTasks(long checklistId) {
        executorService.execute(() -> checklistTaskDao.deleteAllCompletedTasksByChecklistId(checklistId));
    }

    // Deletes all checklist tasks by current checklist id
    public void deleteAllTaskByChecklistId(long checklistId) {
        executorService.execute(() -> checklistTaskDao.deleteAllTasksByChecklistId(checklistId));
    }

    // Retrieves a checklist task by its ID
    public LiveData<ChecklistTaskEntity> getTaskById(long taskId) {
        return checklistTaskDao.getChecklistTaskById(taskId);
    }

    // Updates the completion status of a checklist task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        executorService.execute(() -> checklistTaskDao.updateTaskCompletion(taskId, isCompleted));
    }

    public void deleteAllChecklistTasks() {
        executorService.execute(checklistTaskDao::deleteAllChecklistTasks);
    }
}
