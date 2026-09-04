package com.smartfamily.studentplanner.data.dao;

import com.smartfamily.studentplanner.data.entity.EventTaskEntity;

public interface OnEventTaskInsertedListener {
    void onEventTaskInserted(EventTaskEntity task);
}
