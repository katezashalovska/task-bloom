package com.smartfamily.studentplanner.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class RoutinePagerAdapter extends FragmentStateAdapter {
    private final ArrayList<Integer> years;

    public RoutinePagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Integer> years) {
        super(fragmentActivity);
        this.years = years;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        RoutineFragment fragment = new RoutineFragment();
        Bundle args = new Bundle();
        args.putInt("year", years.get(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

}
