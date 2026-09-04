package com.smartfamily.studentplanner.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.dao.ChecklistDao;
import com.smartfamily.studentplanner.data.database.TaskDatabase;
import com.smartfamily.studentplanner.data.entity.ChecklistEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChecklistRepository {
    private final ChecklistDao checklistDao;

    //private final LiveData<List<ChecklistEntity>> allChecklists;

    // ExecutorService for background operations
    private final ExecutorService executorService;

    public ChecklistRepository(Application application) {

        // Get the database instance
        TaskDatabase database = TaskDatabase.getDatabase(application);
        // Initialize DAO
        checklistDao = database.checklistDao();

        // Fetch all checklists from the DAO
        //allChecklists = checklistDao.getAllCheckLists();

        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<ChecklistEntity>> getAllChecklists() {
        return checklistDao.getAllCheckLists();
    }

    // Inserts a new checklist into the database
    public long insert(ChecklistEntity task) {
        executorService.execute(() -> checklistDao.insert(task));
        return task.getId();
    }

    // Updates the name of a checklist
    public void updateChecklistName(long checklistId, String name) {
        executorService.execute(() -> checklistDao.updateChecklistName(checklistId, name));
    }

    // Updates checklist
    public void update(ChecklistEntity checklist) {
        executorService.execute(() -> checklistDao.update(checklist));
    }

    // Deletes checklist by id
    public void deleteChecklistById(long checklistId) {
        executorService.execute(() -> checklistDao.deleteChecklistById(checklistId));
    }

    // Gets checklist by id
    public LiveData<ChecklistEntity> getChecklistById(long id) {
        return checklistDao.getChecklistById(id);
    }

    // Deletes all checklists
    public void deleteAllChecklists() {
        executorService.execute(checklistDao::deleteAllChecklists);
    }
}
