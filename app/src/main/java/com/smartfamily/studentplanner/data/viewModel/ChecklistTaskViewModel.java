package com.smartfamily.studentplanner.data.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;
import com.smartfamily.studentplanner.data.repository.ChecklistTaskRepository;

import java.util.List;

public class ChecklistTaskViewModel extends AndroidViewModel {
    private final ChecklistTaskRepository checklistTaskRepository;

    public ChecklistTaskViewModel(@NonNull Application application) {
        super(application);
        // Initialize the repository
        checklistTaskRepository = new ChecklistTaskRepository(application);
    }

    // Method to get all checklist tasks (LiveData)
    public LiveData<List<ChecklistTaskEntity>> getAllTasks(long checklistId) {
        return checklistTaskRepository.getAllTasks(checklistId);
    }

    // Method to insert a new checklist task
    public void insert(ChecklistTaskEntity task) {
        checklistTaskRepository.insert(task);
    }

    // Method to update an existing checklist task
    public void update(ChecklistTaskEntity task) {
        checklistTaskRepository.update(task);
    }

    // Method to delete  an existing checklist task
    public void deleteTaskById(long taskId) {
        checklistTaskRepository.deleteByTaskId(taskId);
    }

    // Method to delete all completed checklist task
    public void deleteAllCompletedChecklistTasks(long checklistId) {
        checklistTaskRepository.deleteAllCompletedTasks(checklistId);
    }

    // Method to delete all checklist tasks by current checklist id
    public void deleteAllChecklistTasksByChecklistId(long checklistId) {
        checklistTaskRepository.deleteAllTaskByChecklistId(checklistId);
    }

    // Method to get a checklist task by its ID (LiveData)
    public LiveData<ChecklistTaskEntity> getTaskById(long taskId) {
        return checklistTaskRepository.getTaskById(taskId);
    }

    // Updates the completion status of a checklist task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        checklistTaskRepository.updateTaskCompletion(taskId, isCompleted);
    }

    public void deleteAllChecklistTasks() {
        checklistTaskRepository.deleteAllChecklistTasks();
    }
}
