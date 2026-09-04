package com.smartfamily.studentplanner.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.dao.EventTaskDao;
import com.smartfamily.studentplanner.data.dao.OnEventTaskInsertedListener;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventTaskRepository {
    private final EventTaskDao eventTaskDao;

    // ExecutorService for background operations
    private final ExecutorService executorService;

    public EventTaskRepository(Application application) {
        // Get the database instance
        TaskDatabase database = TaskDatabase.getDatabase(application);

        // Initialize DAO
        eventTaskDao = database.eventTaskDao();

        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(2);
    }

    public EventTaskEntity getEventById(int eventId) {
        return eventTaskDao.getEventById(eventId);
    }

    // Returns all event tasks as LiveData related to a specific page
    public LiveData<List<EventTaskEntity>> getTasksForPage(int pageIndex) {
        return eventTaskDao.getEventsForPage(pageIndex);
    }

    public LiveData<List<EventTaskEntity>> getEventsForDate(long selectedDate) {
        return eventTaskDao.getEventsForDate(selectedDate);
    }

    // Inserts a new event task into the database
    public void insert(EventTaskEntity task, OnEventTaskInsertedListener listener) {
        executorService.execute(() -> {
            long id = eventTaskDao.insert(task);
            task.setId(id);
            if (listener != null) {
                listener.onEventTaskInserted(task);
            }
        });
    }

    // Updates an existing event task in the database
    public void update(EventTaskEntity task) {
        executorService.execute(() -> eventTaskDao.update(task));
    }

    // Deletes an existing event task by id
    public void deleteByTaskId(long taskId) {
        executorService.execute(() -> eventTaskDao.deleteByTaskId(taskId));
    }

    // Deletes all completed event tasks related to a specific page
    public void deleteAllCompletedTasks(int pageIndex) {
        executorService.execute(() -> eventTaskDao.deleteAllCompletedTasks(pageIndex));
    }

    // Retrieves a event task by its ID
    public LiveData<EventTaskEntity> getTaskById(long taskId) {
        return eventTaskDao.getEventTaskById(taskId);
    }

    // Updates the completion status of a event task
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        executorService.execute(() -> eventTaskDao.updateTaskCompletion(taskId, isCompleted));
    }

    // DELETE: Delete eventTasks from database
    public void delete(EventTaskEntity event) {
        executorService.execute(() -> eventTaskDao.delete(event));
    }

    public LiveData<List<EventTaskEntity>> getAllEvents() {
        return eventTaskDao.getAllEvents();
    }

    // READ: Get all eventTasks from the database
    public List<EventTaskEntity> getAllEventTasksSync() {
        return eventTaskDao.getAllEventTasksSync();
    }
}
