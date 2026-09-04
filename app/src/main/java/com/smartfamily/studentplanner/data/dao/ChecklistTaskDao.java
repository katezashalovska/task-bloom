package com.smartfamily.studentplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;

import java.util.List;

@Dao
public interface ChecklistTaskDao {
    // CREATE: Insert a new checklistTasks into the database
    @Insert
    void insert(ChecklistTaskEntity task);

    // READ: Get all checklistTasks from the database as LiveData by certain checklistId
    @Query("SELECT * FROM checklist_tasks WHERE checklistId = :checklistId ORDER BY id DESC")
    LiveData<List<ChecklistTaskEntity>> getAllCheckListTasksByChecklistId(long checklistId);

    // READ: Get a checklistTask by its ID
    @Query("SELECT * FROM checklist_tasks WHERE id = :checklistTaskId")
    LiveData<ChecklistTaskEntity> getChecklistTaskById(long checklistTaskId);

    // UPDATE: Update an existing checklistTask
    @Update
    void update(ChecklistTaskEntity task);

    // DELETE: Delete checklistTask by id
    @Query("DELETE FROM checklist_tasks WHERE id = :checklistTaskId")
    void deleteByTaskId(long checklistTaskId);

    // UPDATE: Updates the completion status of a checklistTask
    @Query("UPDATE checklist_tasks SET isCompleted = :isCompleted WHERE id = :checklistTaskId")
    void updateTaskCompletion(long checklistTaskId, boolean isCompleted);

    // DELETE: Delete all checklistTasks marked as completed by checklistId
    @Query("DELETE FROM checklist_tasks WHERE isCompleted = 1 AND checklistId = :checklistId")
    void deleteAllCompletedTasksByChecklistId(long checklistId);

    // DELETE: Delete all checklistTasks by checklistId
    @Query("DELETE FROM checklist_tasks WHERE checklistId = :checklistId")
    void deleteAllTasksByChecklistId(long checklistId);

    // DELETE: Delete all checklistTasks by checklistId
    @Query("DELETE FROM checklist_tasks")
    void deleteAllChecklistTasks();
}
