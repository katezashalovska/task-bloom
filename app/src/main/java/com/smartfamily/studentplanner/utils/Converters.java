package com.smartfamily.studentplanner.utils;

import androidx.room.TypeConverter;

import com.smartfamily.studentplanner.data.entity.EventType;

import java.util.Date;

public class Converters {

    // SQLite does not have a dedicated DATE type,
    // so we need to convert it to milliseconds and back

    // Convert Date to long (milliseconds)
    @TypeConverter
    public static long fromDate(Date date) {
        return (date != null) ? date.getTime() : 0;
    }

    // Convert long (milliseconds) back to Date
    @TypeConverter
    public static Date toDate(long timestamp) {
        return (timestamp != 0) ? new Date(timestamp) : null;
    }

    @TypeConverter
    public static EventType fromEventType(String value) {
        if (value == null) {
            return null;
        }
        switch (value.toLowerCase()) {
            case "monthly":
                return EventType.MONTHLY;
            case "yearly":
                return EventType.YEARLY;
            case "once":
                return EventType.ONCE;
            default:
                throw new IllegalArgumentException("Unknown event type: " + value);
        }
    }

    @TypeConverter
    public static String toEventType(EventType eventType) {
        if (eventType == null) {
            return null;
        }
        switch (eventType) {
            case MONTHLY:
                return "monthly";
            case YEARLY:
                return "yearly";
            case ONCE:
                return "once";
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}