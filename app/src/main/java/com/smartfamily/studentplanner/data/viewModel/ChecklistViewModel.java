package com.smartfamily.studentplanner.data.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.smartfamily.studentplanner.data.entity.ChecklistEntity;
import com.smartfamily.studentplanner.data.repository.ChecklistRepository;

import java.util.List;

public class ChecklistViewModel extends AndroidViewModel {
    private final ChecklistRepository checklistRepository;

    public ChecklistViewModel(@NonNull Application application) {
        super(application);
        checklistRepository = new ChecklistRepository(application);
    }

    // Method to get all checklist (LiveData)
    public LiveData<List<ChecklistEntity>> getAllChecklists() {
        return checklistRepository.getAllChecklists();
    }

    // Method to insert a new checklist
    public long insert(ChecklistEntity task) {
        return checklistRepository.insert(task);
    }

    // Method to get current checklist
    public LiveData<ChecklistEntity> getChecklistById(long id) {
        return checklistRepository.getChecklistById(id);
    }

    // Method to delete an existing checklist
    public void deleteChecklistById(long checklistId) {
        checklistRepository.deleteChecklistById(checklistId);
    }

    public void deleteAllChecklists() {
        checklistRepository.deleteAllChecklists();
    }

    // Method to update the name of checklist
    public void updateChecklistName(long checklistId, String name) {
        checklistRepository.updateChecklistName(checklistId, name);
    }

    // Method to update an existing checklist
    public void update(ChecklistEntity checklist) {
        checklistRepository.update(checklist);
    }

}
