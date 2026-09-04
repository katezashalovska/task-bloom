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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.entity.EventTaskEntity;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;
import com.smartfamily.studentplanner.utils.EventPagerAdapter;
import com.smartfamily.studentplanner.utils.EventTaskAdapter;
import com.smartfamily.studentplanner.utils.Utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventFragment extends Fragment {
    private EventTaskAdapter adapter;
    private EventTaskViewModel eventTaskViewModel;
    public RecyclerView recyclerView;
    private LinearLayout greetingLinearLayout;
    private int currentYear;
    public ViewPager2 viewPager;
    private EventPagerAdapter pagerAdapter;
    private ImageButton previousDateButton;
    private ImageButton nextDateButton;
    private TextView yearTextView;
    public static MaterialButton eventSuccessfullyAdded;
    public static MaterialButton eventSuccessfullyDeleted;
    private static RelativeLayout mainEventLayout;
    private ArrayList<Integer> years = new ArrayList<>();
    private ImageButton settingsButton;
    private ImageButton menuButton;
    public static final String EVENT_ID_EXTRA = "event_id";
    private List<EventTaskEntity> allEventTasksCache = new ArrayList<>();

    private static final int YEARS_BEFORE_CURRENT = 20;
    private static final int YEARS_AFTER_CURRENT = 30;


    public EventFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        eventTaskViewModel = new ViewModelProvider(this).get(EventTaskViewModel.class);

        greetingLinearLayout = view.findViewById(R.id.greetingLinearLayout);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create the adapter
        adapter = new EventTaskAdapter(new ArrayList<>(), getContext(), eventTaskViewModel);
        recyclerView.setAdapter(adapter);

        viewPager = view.findViewById(R.id.eventViewPager);

        int currentYear1 = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = -YEARS_BEFORE_CURRENT; i < YEARS_AFTER_CURRENT; i++) {
            years.add(currentYear1 + i);
        }

        pagerAdapter = new EventPagerAdapter(requireActivity(), years);
        viewPager.setAdapter(pagerAdapter);

        previousDateButton = view.findViewById(R.id.previousDateButtonContainer);
        yearTextView = view.findViewById(R.id.yearTextView);
        nextDateButton = view.findViewById(R.id.nextDateButtonContainer);

        eventSuccessfullyAdded = view.findViewById(R.id.eventSuccessfullyAdded);
        eventSuccessfullyDeleted = view.findViewById(R.id.eventSuccessfullyDeleted);
        mainEventLayout = view.findViewById(R.id.mainEventLayout);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearTextView.setText(String.valueOf(currentYear));

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

                int itemCount = pagerAdapter != null ? pagerAdapter.getItemCount() : 0;
                previousDateButton.setEnabled(position > 0);
                nextDateButton.setEnabled(position < itemCount - 1);

                viewPager.setUserInputEnabled(true);

                if (!years.isEmpty() && position >= 0 && position < years.size()) {
                    int currentDisplayedYear = years.get(position);
                    yearTextView.setText(String.valueOf(currentDisplayedYear));
                }

                displayEventsForCurrentYear();
            }
        });

        LocalDate localDate = LocalDate.now();
        currentYear = localDate.getYear();

        greetingLinearLayout.setVisibility(View.GONE);

        eventTaskViewModel.getAllEvents().observe(getViewLifecycleOwner(), allEvents -> {
            if (allEvents != null) {
                this.allEventTasksCache = allEvents;
                displayEventsForCurrentYear();
            }
        });

        adapter.setOnItemDeleteListener(eventId -> {
            adapter.resetHighlight();
        });

        ImageView addCalendarButton = view.findViewById(R.id.activateCalendar);
        addCalendarButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new CalendarFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        previousDateButton.setOnClickListener(v -> {
            currentYear--;
            pagerAdapter.setYears(years);
            viewPager.setCurrentItem(years.indexOf(currentYear));
        });

        nextDateButton.setOnClickListener(v -> {
            currentYear++;
            pagerAdapter.setYears(years);
            viewPager.setCurrentItem(years.indexOf(currentYear));
        });

        return view;
    }

    private void displayEventsForCurrentYear() {
        List<EventTaskEntity> validEvents = new ArrayList<>();
        Date currentDate = new Date();

        for (EventTaskEntity event : allEventTasksCache) {
            long timestamp = event.getSelectedDate();
            Date date = new Date(timestamp);
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(date);

            if (eventCalendar.get(Calendar.YEAR) == currentYear) {
                if (isSameDayOrAfter(date, currentDate)) {
                    validEvents.add(event);
                } else {
                    new Thread(() -> eventTaskViewModel.deleteEventTask(event)).start();
                }
            }
        }

        validEvents.sort(Utils.getDateAndIdComparator());

        adapter.setTasks(validEvents);

        Bundle extras = requireActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey(EVENT_ID_EXTRA)) {
            int receivedEventId = extras.getInt(EVENT_ID_EXTRA);
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (adapter.getTasks().get(i).getId() == receivedEventId) {
                    recyclerView.scrollToPosition(i);
                    adapter.highlightItem(i);
                    break;
                }
            }

            Intent currentActivityIntent = requireActivity().getIntent();
            if (currentActivityIntent != null && currentActivityIntent.hasExtra(EVENT_ID_EXTRA)) {
                currentActivityIntent.removeExtra(EVENT_ID_EXTRA);
                requireActivity().setIntent(currentActivityIntent);
            }
        }

        if (validEvents.isEmpty()) {
            greetingLinearLayout.setVisibility(View.VISIBLE);
        } else {
            greetingLinearLayout.setVisibility(View.GONE);
        }

        updateNavigationButtonsState();
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

    public static void makeBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mainEventLayout.setRenderEffect(RenderEffect.createBlurEffect(20, 20, Shader.TileMode.MIRROR));
        }
    }

    public static void clearBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mainEventLayout != null) {
                mainEventLayout.setRenderEffect(null);
            }
        }
    }

    private void updateNavigationButtonsState() {
        boolean hasFutureTasks = false;
        boolean hasPastTasks = false;

        // Check for tasks in the future and past relative to currentYear
        for (EventTaskEntity event : allEventTasksCache) {
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(new Date(event.getSelectedDate()));
            int eventYear = eventCalendar.get(Calendar.YEAR);

            if (eventYear > currentYear) {
                hasFutureTasks = true;
            } else if (eventYear < currentYear) {
                hasPastTasks = true;
            }

            // If tasks are found in both directions, the loop can be broken
            if (hasFutureTasks && hasPastTasks) {
                break;
            }
        }

        // Setting the availability of buttons
        previousDateButton.setEnabled(hasPastTasks);
        nextDateButton.setEnabled(hasFutureTasks);
    }
}
