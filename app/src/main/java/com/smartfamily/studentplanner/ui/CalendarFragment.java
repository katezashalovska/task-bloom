package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.utils.CalendarAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initWidgets(view);
        selectedDate = LocalDate.now();
        setMonthView();

        ImageButton prevButton = view.findViewById(R.id.previousMonthAction);
        prevButton.setOnClickListener(v -> previousMonthAction());

        ImageButton nextButton = view.findViewById(R.id.nextMonthAction);
        nextButton.setOnClickListener(v -> nextMonthAction());

        return view;
    }

    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        Objects.requireNonNull(calendarRecyclerView.getAdapter()).notifyDataSetChanged();
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++) {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            }
            else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals("")) {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
