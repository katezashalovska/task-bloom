package com.smartfamily.studentplanner.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "routine_tasks")
public class RoutineTaskEntity implements SortableByDateAndId {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private boolean isCompleted;
    private int pageIndex;
    private long selectedDate;
    private RoutineType routineType;
    private int repetitions;


    public RoutineTaskEntity() {
    }

    public RoutineTaskEntity(long id, @NonNull String title, @NotNull String description, boolean isCompleted, long selectedDate, RoutineType routineType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.selectedDate = selectedDate;
        this.routineType = routineType;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(long selectedDate) {
        this.selectedDate = selectedDate;
    }

    public RoutineType getRoutineType() {
        return routineType;
    }

    public void setRoutineType(RoutineType routineType) {
        this.routineType = routineType;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }
}
