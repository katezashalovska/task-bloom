package com.smartfamily.studentplanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.smartfamily.studentplanner.data.dao.TaskDao;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.TaskEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TaskDatabaseTest {

    private TaskDatabase database; // The in-memory Room database instance used for testing
    private TaskDao taskDao;       // The DAO object to interact with the database

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    // Ensures LiveData updates are executed immediately in tests

    @Before
    public void setUp() {
        // Initialize the in-memory database and DAO before each test
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase.class)
                .allowMainThreadQueries() // Allows queries on the main thread for testing purposes
                .build();
        taskDao = database.taskDao(); // Obtain the DAO
    }

    @After
    public void tearDown() {
        // Close the database after each test to release resources
        database.close();
    }

    @Test
    public void testInsertAndGetTask() throws InterruptedException {
        // Test for inserting a task into the database and retrieving it

        // Arrange: Create a new TaskEntity and set its properties
        TaskEntity task = new TaskEntity();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setDueDate(new Date());
        task.setLatitude(10.0);
        task.setLongitude(20.0);
        task.setCompleted(false);

        // Act: Insert the task into the database
        taskDao.insert(task);

        // Retrieve the list of tasks from the database
        List<TaskEntity> tasks = getValue(taskDao.getAllTasks());

        // Assert: Verify the task was inserted correctly
        assertEquals(1, tasks.size()); // Check that one task exists in the database
        assertEquals("Test Task", tasks.get(0).getTitle()); // Check the title of the inserted task
    }

    @Test
    public void testUpdateTask() throws InterruptedException {
        // Test for updating a task in the database

        // Arrange: Insert a task and retrieve it
        TaskEntity task = new TaskEntity();
        task.setTitle("Original Title");
        task.setDescription("Original Description");
        task.setDueDate(new Date());
        task.setLatitude(10.0);
        task.setLongitude(20.0);
        task.setCompleted(false);

        taskDao.insert(task); // Insert the task into the database
        TaskEntity insertedTask = getValue(taskDao.getAllTasks()).get(0);

        // Update the title of the task
        insertedTask.setTitle("Updated Title");
        taskDao.update(insertedTask); // Update the task in the database

        // Retrieve the updated task by ID
        TaskEntity updatedTask = getValue(taskDao.getTaskById(insertedTask.getId()));

        // Assert: Verify the task was updated correctly
        assertEquals("Updated Title", updatedTask.getTitle());
    }

    @Test
    public void testDeleteTask() throws InterruptedException {
        // Test for deleting a task from the database

        //  Insert a task into the database
        TaskEntity task = new TaskEntity();
        task.setTitle("Task to Delete"); // Set task title
        task.setDescription("Delete Description"); // Set task description
        task.setDueDate(new Date()); // Set due date to current date
        task.setLatitude(10.0); // Set latitude
        task.setLongitude(20.0); // Set longitude
        task.setCompleted(false); // Set task as not completed

        taskDao.insert(task); // Insert the task into the database
        TaskEntity insertedTask = getValue(taskDao.getAllTasks()).get(0); // Retrieve the task (get the first task from the database)

        // Act: Delete the task by ID
        taskDao.deleteByTaskId(insertedTask.getId()); // Delete the task by its ID

        // Retrieve the list of tasks to confirm deletion
        List<TaskEntity> tasks = getValue(taskDao.getAllTasks()); // Get all tasks from the database after deletion

        // Verify the database is empty after deletion
        assertTrue(tasks.isEmpty()); // Assert that the task list is empty (the task was deleted)
    }

    // Helper method to extract LiveData value synchronously
    private <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1); // Create a CountDownLatch to wait for the LiveData to change
        final Holder<T> holder = new Holder<>(); // Holder to store the value from LiveData

        // Create an observer for the LiveData
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                holder.value = t; // Store the LiveData value in the holder
                latch.countDown(); // Decrement the latch count (signal that the LiveData has been updated)
                liveData.removeObserver(this); // Remove observer after receiving the data
            }
        };

        // Observe LiveData forever (until we get the value and remove the observer)
        liveData.observeForever(observer);

        // Wait for the LiveData to be updated (maximum 2 seconds)
        latch.await(2, TimeUnit.SECONDS);

        return holder.value; // Return the value from the holder (LiveData's value)
    }

    // Generic holder class to store the LiveData value
    private static class Holder<T> {
        T value; // Holds the value retrieved from LiveData
    }
}