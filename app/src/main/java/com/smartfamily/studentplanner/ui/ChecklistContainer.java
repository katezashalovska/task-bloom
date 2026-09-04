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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.ChecklistTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.ChecklistViewModel;
import com.smartfamily.studentplanner.utils.ChecklistRecycleViewAdapter;
import com.smartfamily.studentplanner.utils.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class ChecklistContainer extends Fragment {
    private ChecklistViewModel checklistViewModel;
    private ChecklistTaskViewModel checklistTaskViewModel;
    private ImageButton settingsButton;
    private ImageButton accountButton;
    private TextView dateTextView;
    private TextView dayOfWeek;
    private RecyclerView checklistRecycleView;
    private ChecklistRecycleViewAdapter checklistAdapter;
    public static LinearLayout checklistPlug;
    public static MaterialButton checklistSuccessfullyAdded;
    public static MaterialButton checklistSuccessfullyDeleted;
    private static RelativeLayout mainChecklistLayout;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.checklist_layout, container, false);

        dateTextView = view.findViewById(R.id.dateTextView);
        dayOfWeek = view.findViewById(R.id.dayOfWeek);
        checklistPlug = view.findViewById(R.id.emptyChecklistLayout);

        checklistSuccessfullyAdded = view.findViewById(R.id.checklistSuccessfullyAdded);
        checklistSuccessfullyDeleted = view.findViewById(R.id.checklistSuccessfullyDeleted);
        mainChecklistLayout = view.findViewById(R.id.mainChecklistLayout);

        checklistViewModel = new ViewModelProvider(this).get(ChecklistViewModel.class);
        checklistTaskViewModel = new ViewModelProvider(this).get(ChecklistTaskViewModel.class);

        checklistRecycleView = view.findViewById(R.id.checkListRecyclerView);

        checklistAdapter = new ChecklistRecycleViewAdapter(
                new ArrayList<>(), getContext(), checklistViewModel,
                checklistTaskViewModel, getViewLifecycleOwner()
        );
        checklistRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        checklistRecycleView.setAdapter(checklistAdapter);

        accountButton = view.findViewById(R.id.account);
        accountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        settingsButton = view.findViewById(R.id.icSettings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingActivity.class);
            startActivity(intent);
        });

        updateDateTextView(LocalDate.now());

        checklistViewModel.getAllChecklists().observe(getViewLifecycleOwner(), checklists -> {
            if (checklists == null || checklists.isEmpty()) {
                checklistPlug.setVisibility(View.VISIBLE);
                checklistRecycleView.setVisibility(View.GONE);
            } else {
                checklistPlug.setVisibility(View.GONE);
                checklistRecycleView.setVisibility(View.VISIBLE);
                checklistAdapter.setChecklists(checklists);
            }
        });

        return view;
    }

    public static void makeBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mainChecklistLayout.setRenderEffect(RenderEffect.createBlurEffect(20, 20, Shader.TileMode.MIRROR));
        }
    }

    public static void clearBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mainChecklistLayout != null) {
                mainChecklistLayout.setRenderEffect(null);
            }
        }
    }

    private void updateDateTextView(LocalDate date) {
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
}
