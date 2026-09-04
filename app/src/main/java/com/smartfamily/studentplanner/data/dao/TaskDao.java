package com.smartfamily.studentplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.smartfamily.studentplanner.data.database.LocalDateConverter;
import com.smartfamily.studentplanner.data.entity.TaskEntity;

import java.time.LocalDate;
import java.util.List;

@Dao
@TypeConverters({LocalDateConverter.class})
public interface TaskDao {
    // CREATE: Insert a new task into the database
    @Insert
    long insert(TaskEntity task);

    // CREATE: Insert list of task into the database
    @Insert
    long[] insertAll(List<TaskEntity> tasks);

    // READ: Get all tasks from the database as LiveData
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<TaskEntity>> getAllTasks();

    // READ: Get a task by its ID
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<TaskEntity> getTaskById(long taskId);

    // UPDATE: Update an existing task
    @Update
    void update(TaskEntity task);

    // DELETE: Delete task by id
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteByTaskId(long taskId);

    // UPDATE: Updates the completion status of a task
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    void updateTaskCompletion(long taskId, boolean isCompleted);

    // UPDATE: Updates the task list
    @Update
    void updateAll(List<TaskEntity> tasks);

    // DELETE: Delete all tasks marked as completed
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    void deleteAllCompletedTasks();

    // READ: Get a task by routineTaskId
    @Query("SELECT * FROM tasks WHERE routineTaskId = :routineTaskId ORDER BY dueDate DESC LIMIT 1")
    LiveData<TaskEntity> getTaskByRoutineTaskId(long routineTaskId);

    // READ: Get a list of tasks by routineTaskId
    @Query("SELECT * FROM tasks WHERE routineTaskId = :routineTaskId")
    LiveData<List<TaskEntity>> getTasksByRoutineTaskId(long routineTaskId);

    // READ: Get all tasks from database for a specific day
    @Query("SELECT * FROM tasks WHERE DATE(dueDate / 1000, 'unixepoch') = DATE(:date) ORDER BY dueDate ASC")
     LiveData<List<TaskEntity>> getTasksForDate(LocalDate date);

    // READ: Get all eventTasks from the database
    @Query("SELECT * FROM tasks")
    List<TaskEntity> getAllTasksBlocking();

    // READ: Get a list of tasks associated with a particular routine
    @Query("SELECT * FROM tasks WHERE routineTaskId = :routineTaskId ORDER BY dueDate ASC")
    List<TaskEntity> getTasksByRoutineTaskIdBlocking(long routineTaskId);

    // DELETE: Delete multiple tasks by their IDs
    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    void deleteTasksByIds(List<Long> taskIds);

    // Updates the reminder sent status for a task.
    @Query("UPDATE tasks SET is_reminder_sent = :isReminderSent WHERE id = :taskId")
    void updateReminderSentStatus(long taskId, boolean isReminderSent);

    // Method to reset the status of all reminders (used on "first run")
    @Query("UPDATE tasks SET is_reminder_sent = 0")
    void resetAllTaskReminderSentStatus();

    // Method for performing a set of operations within a single database transaction
    @Transaction
    default void runInTransaction(Runnable runnable) {
        runnable.run();
    }
}
