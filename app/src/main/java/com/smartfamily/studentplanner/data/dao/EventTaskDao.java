package com.smartfamily.studentplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartfamily.studentplanner.data.entity.EventTaskEntity;

import java.util.List;

@Dao
public interface EventTaskDao {
    // CREATE: Insert a new eventTasks into the database
    @Insert
    long insert(EventTaskEntity task);

    // READ: Get all eventTasks from the database as LiveData
    @Query("SELECT * FROM event_tasks")
    LiveData<List<EventTaskEntity>> getAllEvents();

    @Query("SELECT * FROM event_tasks WHERE selectedDate = :selectedDate ORDER BY id DESC")
    LiveData<List<EventTaskEntity>> getEventsForDate(long selectedDate);

    @Query("SELECT * FROM event_tasks WHERE pageIndex = :pageIndex ORDER BY id DESC")
    LiveData<List<EventTaskEntity>> getEventsForPage(int pageIndex);

    // READ: Get a eventTask by its ID
    @Query("SELECT * FROM event_tasks WHERE id = :eventTaskId")
    LiveData<EventTaskEntity> getEventTaskById(long eventTaskId);

    @Query("SELECT * FROM event_tasks WHERE id = :eventTaskId")
    EventTaskEntity getEventById(long eventTaskId);

    // UPDATE: Update an existing eventTask
    @Update
    void update(EventTaskEntity task);

    // DELETE: Delete eventTask by id
    @Query("DELETE FROM event_tasks WHERE id = :eventTaskId")
    void deleteByTaskId(long eventTaskId);

    // UPDATE: Updates the completion status of a eventTask
    @Query("UPDATE event_tasks SET isCompleted = :isCompleted WHERE id = :eventTaskId")
    void updateTaskCompletion(long eventTaskId, boolean isCompleted);

    // DELETE: Delete all eventTasks marked as completed
    @Query("DELETE FROM event_tasks WHERE isCompleted = 1 AND pageIndex = :pageIndex")
    void deleteAllCompletedTasks(int pageIndex);

    // DELETE: Delete eventTasks from database
    @Delete
    void delete(EventTaskEntity eventTask);

    // READ: Get all eventTasks from the database
    @Query("SELECT * FROM event_tasks ORDER BY selectedDate ASC")
    List<EventTaskEntity> getAllEventTasksSync();
}
