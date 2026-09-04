package com.smartfamily.studentplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartfamily.studentplanner.data.entity.ChecklistEntity;

import java.util.List;

@Dao
public interface ChecklistDao {
    // CREATE: Insert a new checklist into the database
    @Insert
    long insert(ChecklistEntity checklist);

    // READ: Get all checklists from the database as LiveData
    @Query("SELECT * FROM checklists")
    LiveData<List<ChecklistEntity>> getAllCheckLists();

    // READ: Get a checklist by its ID
    @Query("SELECT * FROM checklists WHERE id = :checklistId")
    LiveData<ChecklistEntity> getChecklistById(long checklistId);

    // UPDATE: Update an existing checklist
    @Update
    void update(ChecklistEntity task);

    // DELETE: Delete checklist by id
    @Query("DELETE FROM checklists WHERE id = :checklistId")
    void deleteChecklistById(long checklistId);

    // UPDATE: Updates the name of a checklist
    @Query("UPDATE checklists SET name = :name WHERE id = :checklistId")
    void updateChecklistName(long checklistId, String name);

    // DELETE: Delete all checklists
    @Query("DELETE FROM checklists")
    void deleteAllChecklists();
}
