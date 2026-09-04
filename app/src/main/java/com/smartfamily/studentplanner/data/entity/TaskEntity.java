package com.smartfamily.studentplanner.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "tasks",
        foreignKeys = @ForeignKey(entity = RoutineTaskEntity.class,
                parentColumns = "id",
                childColumns = "routineTaskId",
                onDelete = ForeignKey.CASCADE),
                indices = {@Index(value = {"routineTaskId"})})
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;  // Task ID

    private String title; // Task title
    private String description; // Task description
    private Date dueDate;  // Task due date

    //variables for location
    //There are two variable that LatLng needs
    private double latitude;
    private double longitude;
    private boolean isCompleted;  // Task completion status
    private Long routineTaskId; // Routine Task ID
    private TaskPriority taskPriority; // Task priority type
    @ColumnInfo(name = "is_reminder_sent")
    private boolean isReminderSent;
    @ColumnInfo(name = "is_user_modified")
    private boolean isUserModified;

    // Room uses an empty constructor to create an entity object
    // and then assign values to fields via setters
    // or directly if available.
    public TaskEntity() {
    }

    public TaskEntity(long id, @NonNull String title, @NonNull String description, @NonNull Date dueDate, double latitude, double longitude, boolean isCompleted, TaskPriority taskPriority, Long routineTaskId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCompleted = isCompleted;
        this.taskPriority = taskPriority;
        this.routineTaskId = routineTaskId;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public TaskPriority getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(TaskPriority taskPriority) {
        this.taskPriority = taskPriority;
    }

    public Long getRoutineTaskId() {
        return routineTaskId;
    }

    public void setRoutineTaskId(Long routineTaskId) {
        this.routineTaskId = routineTaskId;
    }

    public boolean isReminderSent() {
        return isReminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        isReminderSent = reminderSent;
    }

    public boolean isUserModified() {
        return isUserModified;
    }

    public void setUserModified(boolean userModified) {
        isUserModified = userModified;
    }
}
