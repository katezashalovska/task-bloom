package com.smartfamily.studentplanner.ui;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.utils.TaskAdapter;

import java.time.LocalDate;
import java.time.ZoneId;


public class TasksForDateFragment extends Fragment {
    private static final String ARG_DATE = "date";
    public TaskAdapter adapter;
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks_for_date, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTasksForDate);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(null, requireContext(), new ViewModelProvider(requireActivity()).get(TaskViewModel.class));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocalDate date = (LocalDate) getArguments().getSerializable(ARG_DATE);
        if (date != null) {
            taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
            taskViewModel.getTasksForDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).observe(getViewLifecycleOwner(), tasks -> {
                adapter.setTasks(tasks);

                Long highlightedId = taskViewModel.getHighlightedTaskId();
                if (highlightedId != null && tasks != null) {
                    for (int i = 0; i < tasks.size(); i++) {
                        if (tasks.get(i).getId() == highlightedId) {
                            adapter.highlightItem(i);
                            taskViewModel.setHighlightedTaskId(null);
                            break;
                        }
                    }
                }
            });
        }
    }
}
