package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.RoutineTaskEntity;
import com.smartfamily.studentplanner.data.entity.RoutineType;
import com.smartfamily.studentplanner.data.viewModel.RoutineTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.utils.RoutineTaskAdapter;
import com.smartfamily.studentplanner.utils.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutineFragment extends Fragment {
    private RoutineTaskAdapter adapter;
    private RoutineTaskViewModel routineTaskViewModel;
    private TaskViewModel taskViewModel;
    public RecyclerView recyclerView;
    private LinearLayout greetingLinearLayout;
    private ConstraintLayout toDoListFilterMenu;
    private int currentYear;
    public ViewPager2 viewPager;
    private TextView yearTextView;
    private TextView dayOfWeek;
    private TextView dateTextView;
    public static MaterialButton routineSuccessfullyAdded;
    public static MaterialButton routineSuccessfullyDeleted;
    private static RelativeLayout mainRoutineLayout;
    private ArrayList<Integer> years = new ArrayList<>();
    private ImageButton settingsButton;
    private ImageButton menuButton;
    public static final String ROUTINE_ID_EXTRA = "routine_id";
    private TextView allTaskPlug;
    private TextView dailyPlug;
    private TextView weeklyPlug;
    private TextView monthlyPlug;
    private TextView annualPlug;
    private List<TextView> filterButtons;
    private RoutineType currentRoutineFilterType = null;
    private LocalDate currentSelectedDate;
    private static final int YEARS_BEFORE_CURRENT = 2;
    private static final int YEARS_AFTER_CURRENT = 3;


    public RoutineFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routine, container, false);

        routineTaskViewModel = new ViewModelProvider(this).get(RoutineTaskViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        greetingLinearLayout = view.findViewById(R.id.greetingLinearLayout);
        toDoListFilterMenu = view.findViewById(R.id.toDoListFilterMenu);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.routineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create the adapter
        adapter = new RoutineTaskAdapter(new ArrayList<>(), getContext(), routineTaskViewModel, taskViewModel);
        recyclerView.setAdapter(adapter);

        viewPager = view.findViewById(R.id.routineViewPager);

        int currentYear1 = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = -YEARS_BEFORE_CURRENT; i < YEARS_AFTER_CURRENT; i++) {
            years.add(currentYear1 + i);
        }

        RoutinePagerAdapter pagerAdapter = new RoutinePagerAdapter(requireActivity(), years);
        viewPager.setAdapter(pagerAdapter);

        yearTextView = view.findViewById(R.id.yearTextView);

        routineSuccessfullyAdded = view.findViewById(R.id.routineSuccessfullyAdded);
        routineSuccessfullyDeleted = view.findViewById(R.id.routineSuccessfullyDeleted);
        mainRoutineLayout = view.findViewById(R.id.mainRoutineLayout);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        dayOfWeek = view.findViewById(R.id.dayOfWeek);
        dateTextView = view.findViewById(R.id.dateTextView);

        currentSelectedDate = LocalDate.now();
        Locale currentLocale = Utils.getCurrentLocale();

        updateDateTextView(currentSelectedDate);

        DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", currentLocale);
        String dayOfWeekString = currentSelectedDate.format(dayOfWeekFormatter);

        StringBuilder dayOfWeekBuilder = new StringBuilder(dayOfWeekString);
        if (dayOfWeekBuilder.length() > 0) {
            dayOfWeekBuilder.setCharAt(0, Character.toUpperCase(dayOfWeekBuilder.charAt(0)));
        }
        dayOfWeek.setText(dayOfWeekBuilder.toString());

        DateTimeFormatter monthAndDayFormatter = DateTimeFormatter.ofPattern("MMMM d", currentLocale);
        String monthAndDayString = currentSelectedDate.format(monthAndDayFormatter);

        StringBuilder monthAndDayBuilder = new StringBuilder(monthAndDayString);
        if (monthAndDayBuilder.length() > 0) {
            monthAndDayBuilder.setCharAt(0, Character.toUpperCase(monthAndDayBuilder.charAt(0)));
        }
        dateTextView.setText(monthAndDayBuilder.toString());
        settingsButton = view.findViewById(R.id.icSettings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingActivity.class);
            startActivity(intent);
        });

        menuButton = view.findViewById(R.id.icMenu);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentYear = years.get(position);
                yearTextView.setText(String.valueOf(currentYear));
                applyFilter(currentRoutineFilterType);
            }
        });

        LocalDate localDate = LocalDate.now();
        currentYear = localDate.getYear();

        allTaskPlug = view.findViewById(R.id.allTaskPlug);
        dailyPlug = view.findViewById(R.id.dailyPlug);
        weeklyPlug = view.findViewById(R.id.weeklyPlug);
        monthlyPlug = view.findViewById(R.id.monthlyPlug);
        annualPlug = view.findViewById(R.id.annualPlug);

        filterButtons = new ArrayList<>();
        filterButtons.add(allTaskPlug);
        filterButtons.add(dailyPlug);
        filterButtons.add(weeklyPlug);
        filterButtons.add(monthlyPlug);
        filterButtons.add(annualPlug);

        setSelectedFilterButton(allTaskPlug);
        currentRoutineFilterType = null;

        setupFilterButtons();

        greetingLinearLayout.setVisibility(View.GONE);
        toDoListFilterMenu.setVisibility(View.GONE);

        routineTaskViewModel.getAllRoutines().observe(getViewLifecycleOwner(), routineTaskEntities -> {
            if (routineTaskEntities != null) {
                List<RoutineTaskEntity> allRoutines = new ArrayList<>(routineTaskEntities);

                filterAndDisplayRoutines(allRoutines);

                List<RoutineTaskEntity> validRoutines = new ArrayList<>();
                Date currentDate = new Date();

                for (RoutineTaskEntity routine : routineTaskEntities) {
                    Date date = new Date(routine.getSelectedDate());

                    Calendar routineCalendar = Calendar.getInstance();
                    routineCalendar.setTime(date);

                    if (routineCalendar.get(Calendar.YEAR) == currentYear) {
                        if (isSameDayOrAfter(date, currentDate)) {
                            validRoutines.add(routine);
                        } else {
                            new Thread(() -> routineTaskViewModel.deleteRoutineTask(routine)).start();
                        }
                    }
                }

                validRoutines.sort(Utils.getDateAndIdComparator());

                adapter.setTasks(validRoutines);

                Bundle extras = requireActivity().getIntent().getExtras();
                if (extras != null && extras.containsKey(ROUTINE_ID_EXTRA)) {
                    int receivedRoutineId = extras.getInt(ROUTINE_ID_EXTRA);
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (adapter.getTasks().get(i).getId() == receivedRoutineId) {
                            recyclerView.scrollToPosition(i);
                            adapter.highlightItem(i);
                            break;
                        }
                    }
                    requireActivity().setIntent(new Intent());
                }

                adapter.setOnItemDeleteListener(routineId -> adapter.resetHighlight());

                if (validRoutines.isEmpty()) {
                    greetingLinearLayout.setVisibility(View.VISIBLE);
                    toDoListFilterMenu.setVisibility(View.GONE);
                } else {
                    greetingLinearLayout.setVisibility(View.GONE);
                    toDoListFilterMenu.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    private void setupFilterButtons() {
        allTaskPlug.setOnClickListener(v -> {
            setSelectedFilterButton(allTaskPlug);
            applyFilter(null);
        });

        dailyPlug.setOnClickListener(v -> {
            setSelectedFilterButton(dailyPlug);
            applyFilter(RoutineType.DAILY);
        });

        weeklyPlug.setOnClickListener(v -> {
            setSelectedFilterButton(weeklyPlug);
            applyFilter(RoutineType.WEEKLY);
        });

        monthlyPlug.setOnClickListener(v -> {
            setSelectedFilterButton(monthlyPlug);
            applyFilter(RoutineType.MONTHLY);
        });

        annualPlug.setOnClickListener(v -> {
            setSelectedFilterButton(annualPlug);
            applyFilter(RoutineType.ANNUAL);
        });
    }

    private void setSelectedFilterButton(TextView selectedButton) {
        for (TextView button : filterButtons) {
            button.setSelected(false);
        }
        selectedButton.setSelected(true);
    }

    private void filterAndDisplayRoutines(List<RoutineTaskEntity> allRoutines) {
        List<RoutineTaskEntity> routinesAfterTypeFilter = new ArrayList<>();

        for (RoutineTaskEntity routine : allRoutines) {
            if (currentRoutineFilterType == null) {
                routinesAfterTypeFilter.add(routine);
            } else {
                if (routine.getRoutineType() == currentRoutineFilterType) {
                    routinesAfterTypeFilter.add(routine);
                }
            }
        }

        List<RoutineTaskEntity> finalFilteredRoutines = new ArrayList<>();
        Date currentDate = new Date();

        for (RoutineTaskEntity routine : routinesAfterTypeFilter) {
            Calendar routineCalendar = Calendar.getInstance();
            routineCalendar.setTime(new Date(routine.getSelectedDate()));


            if (isSameDayOrAfter(new Date(routine.getSelectedDate()), currentDate)) {
                finalFilteredRoutines.add(routine);
            } else {
                new Thread(() -> routineTaskViewModel.deleteRoutineTask(routine)).start();
            }
        }

        finalFilteredRoutines.sort(Utils.getDateAndIdComparator());

        adapter.setTasks(finalFilteredRoutines);
        adapter.notifyDataSetChanged();
    }

    private void applyFilter(@Nullable RoutineType filterType) {
        currentRoutineFilterType = filterType;
        routineTaskViewModel.getAllRoutines().observe(getViewLifecycleOwner(), this::filterAndDisplayRoutines);
    }

    private boolean isSameDayOrAfter(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) {
            return true;
        } else if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            return cal1.get(Calendar.DAY_OF_YEAR) >= cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }
    }

    private void updateDateTextView(LocalDate date) {
        Locale currentLocale = Utils.getCurrentLocale();

        if (dateTextView != null) {
            DateTimeFormatter monthAndDayFormat = DateTimeFormatter.ofPattern("MMMM d", currentLocale);
            String formattedDate = date.format(monthAndDayFormat);

            if (!formattedDate.isEmpty()) {
                formattedDate = Character.toUpperCase(formattedDate.charAt(0)) + formattedDate.substring(1);
            }
            dateTextView.setText(formattedDate);
        }

        if (dayOfWeek != null) {
            DateTimeFormatter dayOfWeekFormat = DateTimeFormatter.ofPattern("EEEE", currentLocale);
            String formattedDayOfWeek = date.format(dayOfWeekFormat);

            if (!formattedDayOfWeek.isEmpty()) {
                formattedDayOfWeek = Character.toUpperCase(formattedDayOfWeek.charAt(0)) + formattedDayOfWeek.substring(1);
            }
            dayOfWeek.setText(formattedDayOfWeek);
        }
    }

    public static void makeBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mainRoutineLayout.setRenderEffect(RenderEffect.createBlurEffect(20, 20, Shader.TileMode.MIRROR));
        }
    }

    public static void clearBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mainRoutineLayout != null) {
                mainRoutineLayout.setRenderEffect(null);
            }
        }
    }

}
