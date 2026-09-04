package com.smartfamily.studentplanner.ui;

import static com.smartfamily.studentplanner.ui.HomeFragment.TASK_ID_EXTRA;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.utils.TaskAdapter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DayTasksFragment extends Fragment {
    private RecyclerView tasksRecyclerView;
    private LocalDate date;
    private TaskAdapter adapter;
    private TaskViewModel taskViewModel;
    private static final String ARG_DATE = "date";
    private Integer taskIdToHighlightOnCreate = null;

    public static DayTasksFragment newInstance(LocalDate date, TaskViewModel taskViewModel, Integer taskId) {
        DayTasksFragment fragment = new DayTasksFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        if (taskId != null) {
            args.putInt(TASK_ID_EXTRA, taskId);
        }
        fragment.setArguments(args);
        fragment.taskViewModel = taskViewModel;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = (LocalDate) getArguments().getSerializable("date");
            if (getArguments().containsKey(TASK_ID_EXTRA)) {
                taskIdToHighlightOnCreate = getArguments().getInt(TASK_ID_EXTRA);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_tasks, container, false);
        tasksRecyclerView = view.findViewById(R.id.dayTasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        adapter = new TaskAdapter(new ArrayList<>(), requireContext(), taskViewModel);
        tasksRecyclerView.setAdapter(adapter);

        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                List<TaskEntity> tasksForDay = tasks.stream()
                        .filter(task -> task.getDueDate() != null && task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(date))
                        .sorted(Comparator.comparing(TaskEntity::isCompleted)
                                .thenComparing(TaskEntity::getTaskPriority))
                        .collect(Collectors.toList());

                adapter.setTasks(tasksForDay);
                if (taskIdToHighlightOnCreate != null) {
                    highlightTask(taskIdToHighlightOnCreate);
                    taskIdToHighlightOnCreate = null;
                }
            } else {
                Log.d("DayTasksFragment", "Tasks is null");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void highlightTask(int taskId) {
        if (tasksRecyclerView != null && adapter != null && adapter.getItemCount() > 0) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (adapter.getTasks().get(i).getId() == taskId) {
                    tasksRecyclerView.scrollToPosition(i);
                    adapter.highlightItem(i);
                    break;
                }
            }
        }
    }
}
