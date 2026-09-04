package com.smartfamily.studentplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "checklist_tasks")
public class ChecklistTaskEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;  // ChecklistTask ID
    private long checklistId;  // Связь с ChecklistEntity
    private String description;
    private boolean isCompleted;

    public ChecklistTaskEntity() {
    }

    public ChecklistTaskEntity(long id, long checklistId, @NotNull String description, boolean isCompleted) {
        this.id = id;
        this.checklistId = checklistId;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(long checklistId) {
        this.checklistId = checklistId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
