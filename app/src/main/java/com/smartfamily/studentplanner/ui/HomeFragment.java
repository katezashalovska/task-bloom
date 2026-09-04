package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.TaskEntity;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.utils.DayTasksPagerAdapter;
import com.smartfamily.studentplanner.utils.TaskAdapter;
import com.smartfamily.studentplanner.utils.Utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    public TaskAdapter adapter;
    public TaskViewModel taskViewModel;
    public TextView dateTextView;
    private TextView dayOfWeek;
    public ViewPager2 viewPager;
    public DayTasksPagerAdapter pagerAdapter;
    public List<LocalDate> dates;
    public List<LocalDate> allDates = new ArrayList<>();
    public ViewPager2.OnPageChangeCallback pageChangeCallback;
    private ImageButton settingsButton;
    private ImageButton accountButton;
    private ConstraintLayout filterMenu;
    private LinearLayout plugLayout;
    public static MaterialButton taskSuccessfullyAdded;
    public static MaterialButton taskSuccessfullyDeleted;
    private static RelativeLayout mainToDoListLayout;
    public static final String TASK_ID_EXTRA = "highlighted_task_id";
    public Integer taskIdFromActivity;
    private LocalDate targetDateOnDataLoad;
    private Integer targetTaskIdOnDataLoad;
    private LocalDate pendingDeletedTaskDate = null;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the ViewModel
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        dateTextView = rootView.findViewById(R.id.dateTextView);
        dayOfWeek = rootView.findViewById(R.id.dayOfWeek);

        taskSuccessfullyAdded = rootView.findViewById(R.id.taskSuccessfullyAdded);
        taskSuccessfullyDeleted = rootView.findViewById(R.id.taskSuccessfullyDeleted);
        mainToDoListLayout = rootView.findViewById(R.id.mainToDoListLayout);
        ImageButton previousDateButton = rootView.findViewById(R.id.previousDateButton);
        ImageButton nextDateButton = rootView.findViewById(R.id.nextDateButton);

        filterMenu = rootView.findViewById(R.id.toDoListFilterMenu);
        plugLayout = rootView.findViewById(R.id.toDoListPlugLayout);
        settingsButton = rootView.findViewById(R.id.icSettings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingActivity.class);
            startActivity(intent);
        });
        accountButton = rootView.findViewById(R.id.account);

        accountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Initialize with the last selected date or today's date
        LocalDate initialDate = taskViewModel.getLastTaskDate().getValue() != null ? 
                                taskViewModel.getLastTaskDate().getValue() : 
                                LocalDate.now();
        updateDateTextView(initialDate);

        viewPager = rootView.findViewById(R.id.viewPager);
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (allDates != null && position >= 0 && position < allDates.size()) {
                    LocalDate selectedDate = allDates.get(position);
                    updateDateTextView(selectedDate);
                    // explicitly save the date to ensure it's preserved
                    if (taskViewModel != null) {
                        taskViewModel.setLastTaskDate(selectedDate);
                    }
                } else {
                    Log.w("HomeFragment", "Invalid position or dates list is null: position=" + position + ", dates=" + dates);
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        Bundle extras = requireActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey(TASK_ID_EXTRA)) {
            taskIdFromActivity = extras.getInt(TASK_ID_EXTRA);
        } else {
            targetDateOnDataLoad = LocalDate.now();
        }

        taskViewModel.getDeletedTaskDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                pendingDeletedTaskDate = date;
                taskViewModel.setDeletedTaskDate(null);
            }
        });

        pagerAdapter = new DayTasksPagerAdapter(requireActivity(), allDates, taskViewModel, viewPager, taskIdFromActivity);

        // Observe LiveData for task updates
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                allDates.clear();
                dates = tasks.stream()
                        .map(task -> task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .distinct()
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList());

                // Create a list of dates, including previous and next days
                if (!dates.isEmpty()) {
                    LocalDate firstDate = dates.get(0);
                    LocalDate lastDate = dates.get(dates.size() - 1);
                    long dayDiff = Math.abs(ChronoUnit.DAYS.between(firstDate, LocalDate.now()));
                    LocalDate dateIterator = firstDate.minusDays(dayDiff);

                    while (!dateIterator.isAfter(lastDate)) {
                        allDates.add(dateIterator);
                        dateIterator = dateIterator.plusDays(1);
                    }
                } else {
                    allDates.add(LocalDate.now());
                }

                if (viewPager.getAdapter() == null) {
                    viewPager.setAdapter(pagerAdapter);
                }

                LocalDate dateToSetInPager;

                if (pendingDeletedTaskDate != null) {
                    dateToSetInPager = pendingDeletedTaskDate;
                    pendingDeletedTaskDate = null;
                } else if (taskIdFromActivity != null) {
                    TaskEntity taskToHighlight = tasks.stream()
                            .filter(t -> t.getId() == taskIdFromActivity)
                            .findFirst()
                            .orElse(null);
                    if (taskToHighlight != null) {
                        dateToSetInPager = taskToHighlight.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        taskIdFromActivity = null;
                        requireActivity().setIntent(new Intent());
                    } else {
                        dateToSetInPager = LocalDate.now();
                    }
                } else if (targetDateOnDataLoad != null) {
                    dateToSetInPager = targetDateOnDataLoad;
                    targetDateOnDataLoad = null;
                } else if (taskViewModel.getLastTaskDate().getValue() != null) {
                    dateToSetInPager = taskViewModel.getLastTaskDate().getValue();
                } else {
                    dateToSetInPager = LocalDate.now();
                }

                int position = allDates.indexOf(dateToSetInPager);
                if (position != -1) {
                    viewPager.setCurrentItem(position, false);
                } else {
                    Log.w("HomeFragment", "Target date " + dateToSetInPager + " not found in allDates. Attempting to find nearest or move to current date.");

                    LocalDate nearestDateWithTasks = findNearestDateWithTasks(dateToSetInPager, allDates);
                    if (nearestDateWithTasks != null) {
                        position = allDates.indexOf(nearestDateWithTasks);
                        if (position != -1) {
                            viewPager.setCurrentItem(position, false);
                        } else {
                            viewPager.setCurrentItem(allDates.indexOf(LocalDate.now()), false);
                        }
                    } else {
                        int currentDayPosition = allDates.indexOf(LocalDate.now());
                        if (currentDayPosition != -1) {
                            viewPager.setCurrentItem(currentDayPosition, false);
                        } else if (!allDates.isEmpty()) {
                            viewPager.setCurrentItem(0, false);
                        }
                    }
                }
            }

            //show filter menu and plug image
            if (tasks == null || tasks.isEmpty()) {
                //filterMenu.setVisibility(View.GONE); // uncomment for full version
                viewPager.setVisibility(View.GONE);
                plugLayout.setVisibility(View.VISIBLE);
                taskViewModel.setLastTaskDate(LocalDate.now());

                // Setting a transparent background
                if (previousDateButton != null) {
                    previousDateButton.setBackgroundResource(android.R.color.transparent);
                }
                if (nextDateButton != null) {
                    nextDateButton.setBackgroundResource(android.R.color.transparent);
                }
            } else {
                //hide them
                //filterMenu.setVisibility(View.VISIBLE); // uncomment for full version
                plugLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                // If there is at least one task: return the click effect
                TypedValue outValue = new TypedValue();
                if (getContext() != null) {
                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                    int rippleDrawableResId = outValue.resourceId;

                    // Applying the ripple effect
                    if (previousDateButton != null) {
                        previousDateButton.setBackgroundResource(rippleDrawableResId);
                    }
                    if (nextDateButton != null) {
                        nextDateButton.setBackgroundResource(rippleDrawableResId);
                    }
                }
            }
        });

        if (previousDateButton != null && nextDateButton != null && allDates != null) {
            previousDateButton.setOnClickListener(v -> {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem > 0) {
                    viewPager.setCurrentItem(currentItem - 1);
                }
            });

            nextDateButton.setOnClickListener(v -> {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem < allDates.size() - 1) {
                    viewPager.setCurrentItem(currentItem + 1);
                }
            });
        } else {
            Log.w("HomeFragment", "One of the buttons or allDates is null");
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                AddTaskBottomSheetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    LocalDate dateFromBottomSheet = (LocalDate) bundle.getSerializable(AddTaskBottomSheetFragment.RESULT_TASK_DATE);
                    int taskIdFromBottomSheet = bundle.getInt(AddTaskBottomSheetFragment.RESULT_TASK_ID, -1);
                    if (dateFromBottomSheet != null && taskIdFromBottomSheet != -1) {
                        targetDateOnDataLoad = dateFromBottomSheet;
                        targetTaskIdOnDataLoad = taskIdFromBottomSheet;

                        // Ensure UI operations are on the main thread
                        requireView().post(() -> { //
                            if (allDates != null && !allDates.isEmpty()) {
                                int position = allDates.indexOf(targetDateOnDataLoad);
                                if (position != -1) {
                                    viewPager.setCurrentItem(position, true);
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        if (pagerAdapter != null && position < pagerAdapter.getItemCount()) {
                                            pagerAdapter.notifyItemChanged(position, targetTaskIdOnDataLoad);
                                        }
                                        targetTaskIdOnDataLoad = null;
                                    }, 300);
                                }
                            }
                        });
                    }
                });
    }

    private LocalDate findNearestDateWithTasks(LocalDate referenceDate, List<LocalDate> availableDates) {
        if (availableDates.isEmpty()) {
            return null;
        }

        // Try to find a date in the future
        for (LocalDate date : availableDates) {
            if (!date.isBefore(referenceDate)) {
                return date;
            }
        }

        // If no future date, try to find a date in the past
        for (int i = availableDates.size() - 1; i >= 0; i--) {
            LocalDate date = availableDates.get(i);
            if (!date.isAfter(referenceDate)) {
                return date;
            }
        }
        return null; // Should not happen if availableDates is not empty
    }

    public static void makeBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mainToDoListLayout.setRenderEffect(RenderEffect.createBlurEffect(20, 20, Shader.TileMode.MIRROR));
        }
    }

    public static void clearBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mainToDoListLayout != null) {
                mainToDoListLayout.setRenderEffect(null);
            }
        }
    }

    private void updateDateTextView(LocalDate date) {
        // Always save the selected date to the ViewModel first
        if (taskViewModel != null && date != null) {
            taskViewModel.setLastTaskDate(date);
        }
        
        Locale currentLocale = Utils.getCurrentLocale();

        if (dateTextView != null) {
            DateTimeFormatter monthAndDayFormat = DateTimeFormatter.ofPattern("MMMM d", currentLocale);

            StringBuilder builder = new StringBuilder(date.format(monthAndDayFormat));
            builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));

            dateTextView.setText(builder.toString());
        }
        if (dayOfWeek != null) {
            DateTimeFormatter dayOfWeekFormat = DateTimeFormatter.ofPattern("EEEE", currentLocale);

            StringBuilder builder = new StringBuilder(date.format(dayOfWeekFormat));
            builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));

            dayOfWeek.setText(builder.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (getActivity() != null && getView() != null) {
            getView().setBackgroundResource(R.color.page_background_color);
        }

        // Check if we need to restore the last selected date
        if (taskViewModel != null && viewPager != null && allDates != null && !allDates.isEmpty()) {
            LocalDate lastDate = taskViewModel.getLastTaskDate().getValue();
            if (lastDate != null) {
                int position = allDates.indexOf(lastDate);
                if (position != -1 && position != viewPager.getCurrentItem()) {
                    viewPager.setCurrentItem(position, false);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Save the current date when leaving the fragment
        if (allDates != null && !allDates.isEmpty() && viewPager != null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem >= 0 && currentItem < allDates.size()) {
                LocalDate currentDate = allDates.get(currentItem);
                if (taskViewModel != null && currentDate != null) {
                    taskViewModel.setLastTaskDate(currentDate);
                }
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Make sure current selected date is saved before destroying view
        if (allDates != null && !allDates.isEmpty() && viewPager != null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem >= 0 && currentItem < allDates.size()) {
                taskViewModel.setLastTaskDate(allDates.get(currentItem));
            }
        }
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
    }
}