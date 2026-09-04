package com.smartfamily.studentplanner.data.dao;

import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;

public interface OnRoutineTaskInsertedListener {
    void onRoutineTaskInserted(RoutineTaskEntity task);
}
