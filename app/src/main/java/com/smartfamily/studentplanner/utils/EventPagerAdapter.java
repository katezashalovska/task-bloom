package com.smartfamily.studentplanner.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.smartfamily.studentplanner.ui.EventFragment;

import java.util.ArrayList;

public class EventPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Integer> years;

    public EventPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Integer> years) {
        super(fragmentActivity);
        this.years = years;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt("year", years.get(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

    public void setYears(ArrayList<Integer> years) {
        this.years = years;
        notifyDataSetChanged();
    }
}
