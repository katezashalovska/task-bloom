package com.smartfamily.studentplanner.data.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartfamily.studentplanner.data.dao.OnEventTaskInsertedListener;
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.repository.EventTaskRepository;

import java.time.LocalDate;
import java.util.List;

public class EventTaskViewModel extends AndroidViewModel {
    private final EventTaskRepository eventTaskRepository;
    private final MutableLiveData<LocalDate> lastTaskDate = new MutableLiveData<>();

    public EventTaskViewModel(@NonNull Application application) {
        super(application);
        // Initialize the repository
        eventTaskRepository = new EventTaskRepository(application);
    }

    public void setLastTaskDate(LocalDate date) {
        lastTaskDate.setValue(date);
    }

    public LiveData<LocalDate> getLastTaskDate() {
        return lastTaskDate;
    }

    public EventTaskEntity getEventById(int eventId) {
        return eventTaskRepository.getEventById(eventId);
    }

    public LiveData<List<EventTaskEntity>> getTasksForPage(int pageIndex) {
        return eventTaskRepository.getTasksForPage(pageIndex);
    }

    public void insert(EventTaskEntity task, OnEventTaskInsertedListener listener) {
        eventTaskRepository.insert(task, listener);
    }

    // Method to update an existing event task
    public void update(EventTaskEntity task) {
        eventTaskRepository.update(task);
    }

    // Method to delete an existing event task
    public void deleteTaskById(long taskId) {
        eventTaskRepository.deleteByTaskId(taskId);
    }

    public void deleteAllEventTasks(int pageIndex) {
        eventTaskRepository.deleteAllCompletedTasks(pageIndex);
    }

    // Method to get a event task by its ID (LiveData)
    public LiveData<EventTaskEntity> getTaskById(long taskId) {
        return eventTaskRepository.getTaskById(taskId);
    }

    // Updates the completion status of a event task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        eventTaskRepository.updateTaskCompletion(taskId, isCompleted);
    }

    // DELETE: Delete eventTasks from database
    public void deleteEventTask(EventTaskEntity event) {
        eventTaskRepository.delete(event);
    }

    public LiveData<List<EventTaskEntity>> getAllEvents() {
        return eventTaskRepository.getAllEvents();
    }

    // READ: Get all eventTasks from the database
    public List<EventTaskEntity> getAllEventTasksBlocking() {
        return eventTaskRepository.getAllEventTasksSync();
    }
}
