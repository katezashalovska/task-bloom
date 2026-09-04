package com.smartfamily.studentplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;

import java.util.List;

@Dao
public interface RoutineTaskDao {
    // CREATE: Insert a new routineTasks into the database
    @Insert
    long insert(RoutineTaskEntity task);

    // READ: Get all routineTasks from the database as LiveData
    @Query("SELECT * FROM routine_tasks")
    LiveData<List<RoutineTaskEntity>> getAllRoutines();

    @Query("SELECT * FROM routine_tasks WHERE selectedDate = :selectedDate ORDER BY id DESC")
    LiveData<List<RoutineTaskEntity>> getRoutinesForDate(long selectedDate);

    @Query("SELECT * FROM routine_tasks WHERE pageIndex = :pageIndex ORDER BY id DESC")
    LiveData<List<RoutineTaskEntity>> getRoutinesForPage(int pageIndex);

    @Query("SELECT * FROM routine_tasks WHERE id = :routineTaskId")
    RoutineTaskEntity getRoutineTaskByIdBlocking(long routineTaskId);

    // READ: Get a routineTask by its ID
    @Query("SELECT * FROM routine_tasks WHERE id = :routineTaskId")
    LiveData<RoutineTaskEntity> getRoutineTaskById(long routineTaskId);

    @Query("SELECT * FROM routine_tasks WHERE id = :routineTaskId")
    RoutineTaskEntity getRoutineById(long routineTaskId);

    // UPDATE: Update an existing routineTask
    @Update
    void update(RoutineTaskEntity task);

    // DELETE: Delete routineTask by id
    @Query("DELETE FROM routine_tasks WHERE id = :routineTaskId")
    void deleteByTaskId(long routineTaskId);

    // UPDATE: Updates the completion status of a routineTask
    @Query("UPDATE routine_tasks SET isCompleted = :isCompleted WHERE id = :routineTaskId")
    void updateTaskCompletion(long routineTaskId, boolean isCompleted);

    // DELETE: Delete all routineTasks marked as completed
    @Query("DELETE FROM routine_tasks WHERE isCompleted = 1 AND pageIndex = :pageIndex")

    void deleteAllCompletedTasks(int pageIndex);

    @Delete
    void delete(RoutineTaskEntity routineTask);
}
