package com.smartfamily.studentplanner.data.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartfamily.studentplanner.data.dao.OnRoutineTaskInsertedListener;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.repository.RoutineTaskRepository;

import java.time.LocalDate;
import java.util.List;

public class RoutineTaskViewModel extends AndroidViewModel {
    private final RoutineTaskRepository routineTaskRepository;
    private final MutableLiveData<LocalDate> lastTaskDate = new MutableLiveData<>();

    public RoutineTaskViewModel(@NonNull Application application) {
        super(application);
        // Initialize the repository
        routineTaskRepository = new RoutineTaskRepository(application);
    }

    public void setLastTaskDate(LocalDate date) {
        lastTaskDate.setValue(date);
    }

    public LiveData<LocalDate> getLastTaskDate() {
        return lastTaskDate;
    }

    public RoutineTaskEntity getRoutineById(int routineId) {
        return routineTaskRepository.getRoutineById(routineId);
    }

    public LiveData<List<RoutineTaskEntity>> getTasksForPage(int pageIndex) {
        return routineTaskRepository.getTasksForPage(pageIndex);
    }

    public void insert(RoutineTaskEntity task, OnRoutineTaskInsertedListener listener) {
        routineTaskRepository.insert(task, listener);
    }

    // Method to update an existing routine task
    public void update(RoutineTaskEntity task) {
        routineTaskRepository.update(task);
    }

    // Method to delete an existing routine task
    public void deleteTaskById(long taskId) {
        routineTaskRepository.deleteByTaskId(taskId);
    }

    public void deleteAllRoutineTasks(int pageIndex) {
        routineTaskRepository.deleteAllCompletedTasks(pageIndex);
    }

    // Method to get a routine task by its ID (LiveData)
    public LiveData<RoutineTaskEntity> getTaskById(long taskId) {
        return routineTaskRepository.getTaskById(taskId);
    }

    // Updates the completion status of a routine task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        routineTaskRepository.updateTaskCompletion(taskId, isCompleted);
    }

    public void deleteRoutineTask(RoutineTaskEntity routine) {
        routineTaskRepository.delete(routine);
    }

    public LiveData<List<RoutineTaskEntity>> getAllRoutines() {
        return routineTaskRepository.getAllRoutines();
    }
}
