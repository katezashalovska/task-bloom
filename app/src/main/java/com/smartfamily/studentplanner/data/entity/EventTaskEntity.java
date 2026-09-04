package com.smartfamily.studentplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "event_tasks")
public class EventTaskEntity implements SortableByDateAndId {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String description;
    private boolean isCompleted;
    private int pageIndex;
    private long selectedDate;
    private EventType eventType;
    private int repetitions;


    public EventTaskEntity() {
    }

    public EventTaskEntity(long id, @NotNull String description, boolean isCompleted, long selectedDate, EventType eventType) {
        this.id = id;
        this.description = description;
        this.isCompleted = isCompleted;
        this.selectedDate = selectedDate;
        this.eventType = eventType;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }
}
