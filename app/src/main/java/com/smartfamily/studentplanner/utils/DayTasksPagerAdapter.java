package com.smartfamily.studentplanner.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.ui.DayTasksFragment;

import java.time.LocalDate;
import java.util.List;

public class DayTasksPagerAdapter extends FragmentStateAdapter {
    public List<LocalDate> dates;
    public TaskViewModel taskViewModel;
    private final ViewPager2 viewPager;
    private final FragmentActivity fragmentActivity;
    private Integer taskIdToHighlight;

    public DayTasksPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<LocalDate> dates, TaskViewModel taskViewModel, ViewPager2 viewPager, Integer initialTaskId) {
        super(fragmentActivity);
        this.dates = dates;
        this.taskViewModel = taskViewModel;
        this.viewPager = viewPager;
        this.fragmentActivity = fragmentActivity;
        this.taskIdToHighlight = initialTaskId;
    }

    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LocalDate date = dates.get(position);
        Integer taskIdToPass = null;
        if ((date.equals(LocalDate.now()) || date.equals(LocalDate.now().plusDays(1))) && taskIdToHighlight != null) {
            taskIdToPass = taskIdToHighlight;
        }
        return DayTasksFragment.newInstance(date, taskViewModel, taskIdToPass);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
}
