package com.smartfamily.studentplanner.data.dao;

import com.smartfamily.studentplanner.data.entity.TaskEntity;

public interface OnTaskInsertedListener {
    void onTaskInserted(TaskEntity task);
}
