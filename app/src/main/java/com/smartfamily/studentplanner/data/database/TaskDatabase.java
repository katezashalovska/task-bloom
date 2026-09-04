package com.smartfamily.studentplanner.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.smartfamily.studentplanner.data.dao.ChecklistDao;
import com.smartfamily.studentplanner.data.dao.ChecklistTaskDao;
import com.smartfamily.studentplanner.data.dao.EventTaskDao;
import com.smartfamily.studentplanner.data.dao.RoutineTaskDao;
import com.smartfamily.studentplanner.data.dao.TaskDao;
import com.smartfamily.studentplanner.data.entity.ChecklistEntity;
import com.smartfamily.studentplanner.data.entity.ChecklistTaskEntity;
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {TaskEntity.class, RoutineTaskEntity.class, ChecklistTaskEntity.class, ChecklistEntity.class, EventTaskEntity.class}, version = 15, exportSchema = false)
@TypeConverters(Converters.class)  // Connecting our TypeConverter
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract ChecklistTaskDao checklistTaskDao();
    public abstract ChecklistDao checklistDao();
    public abstract EventTaskDao eventTaskDao();
    public abstract RoutineTaskDao routineTaskDao();

    // Singleton instance of the TaskDatabase
    // Using Singleton pattern to ensure only one instance of the database is created
    private static volatile TaskDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // create a new table checklist_tasks
            database.execSQL("CREATE TABLE IF NOT EXISTS checklist_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "description TEXT, " +
                    "isCompleted INTEGER NOT NULL)");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // create a new table checklist
            database.execSQL("CREATE TABLE IF NOT EXISTS checklists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT)");

            // Add column checklistId в checklist_tasks
            database.execSQL("ALTER TABLE checklist_tasks ADD COLUMN checklistId INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // create a new table event_tasks
            database.execSQL("CREATE TABLE IF NOT EXISTS event_tasks (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "description TEXT, " +
                    "isCompleted INTEGER NOT NULL)");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE event_tasks ADD COLUMN pageIndex INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE event_tasks ADD COLUMN selectedDate INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE event_tasks ADD COLUMN eventType TEXT");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE event_tasks ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN taskPriority TEXT");
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE checklists ADD COLUMN isExpanded INTEGER NOT NULL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS routine_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "isCompleted INTEGER NOT NULL, " +
                    "pageIndex INTEGER NOT NULL, " +
                    "selectedDate INTEGER NOT NULL, " +
                    "routineType TEXT, " +
                    "repetitions INTEGER NOT NULL)");
        }
    };

    // Adding the routineTaskId column to the tasks table
    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN routineTaskId INTEGER");
        }
    };

    // Adding foreign key routineTaskId to tasks table
    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE new_tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "title TEXT," +
                    "description TEXT," +
                    "dueDate INTEGER," +
                    "latitude REAL NOT NULL," +
                    "longitude REAL NOT NULL," +
                    "isCompleted INTEGER NOT NULL," +
                    "taskPriority TEXT," +
                    "routineTaskId INTEGER," +
                    "FOREIGN KEY(routineTaskId) REFERENCES routine_tasks(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)");

            // Copy data from the old table 'tasks' to the new 'new_tasks'
            database.execSQL("INSERT INTO new_tasks (id, title, description, dueDate, latitude, longitude, isCompleted, taskPriority, routineTaskId) " +
                    "SELECT id, title, description, dueDate, latitude, longitude, isCompleted, taskPriority, routineTaskId FROM tasks");

            // Delete the old table 'tasks'
            database.execSQL("DROP TABLE tasks");

            // Rename the new table 'new_tasks' to 'tasks'
            database.execSQL("ALTER TABLE new_tasks RENAME TO tasks");

            // Create an index on a foreign key for performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_routineTaskId ON tasks (routineTaskId)");
        }
    };

    // Adding the is_reminder_sent column to the tasks table
    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN is_reminder_sent INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Adding the is_user_modified column to the tasks table
    static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN is_user_modified INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static TaskDatabase getDatabase(final Context context) {
        // Check if the database instance is already created
        if (INSTANCE == null) {
            // Synchronizing to avoid creating multiple instances in a multi-threaded environment
            synchronized (TaskDatabase.class) {
                // Double-checking to avoid creating the database if it was created by another thread
                if (INSTANCE == null) {
                    // Creating the database instance with Room's database builder
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TaskDatabase.class, "task_database")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
                            .addMigrations(MIGRATION_10_11)
                            .addMigrations(MIGRATION_11_12)
                            .addMigrations(MIGRATION_12_13)
                            .addMigrations(MIGRATION_13_14)
                            .addMigrations(MIGRATION_14_15)
                            // You can add more configurations like migrations here
                            .build();
                }
            }
        }
        // Return the singleton instance
        return INSTANCE;
    }
}

