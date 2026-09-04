package com.smartfamily.studentplanner.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "checklists")
public class ChecklistEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;  // Checklist ID
    private String name;
    private boolean isExpanded = true;

    public ChecklistEntity() {
    }

    public ChecklistEntity(long id, @NonNull String name, boolean isExpanded) {
        this.id = id;
        this.name = name;
        this.isExpanded = isExpanded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
