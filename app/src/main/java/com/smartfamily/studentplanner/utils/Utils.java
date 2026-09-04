package com.smartfamily.studentplanner.utils;

import androidx.appcompat.app.AppCompatDelegate;

import com.smartfamily.studentplanner.data.entity.SortableByDateAndId;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static final String NO_MULTIPLE_SPACES_REGEX = "^(?!.* {2,}).*$";
    public static final String KEY_MODE = "mode";
    public static final String OOB_CODE = "oobCode";
    public static final int PASSWORD_LENGTH = 6;

    private Utils() {}

    public static Locale getCurrentLocale() {
        Locale currentLocale = AppCompatDelegate.getApplicationLocales().get(0);
        if (currentLocale == null) {
            currentLocale = Locale.ENGLISH;
        }

        return currentLocale;
    }

    // Universal comparator based on generic sorting by date and id
    public static <T extends SortableByDateAndId> Comparator<T> getDateAndIdComparator() {
        return (item1, item2) -> {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(new Date(item1.getSelectedDate()));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(new Date(item2.getSelectedDate()));

            int yearComparison = Integer.compare(cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
            if (yearComparison != 0) {
                return yearComparison;
            }

            int dayOfYearComparison = Integer.compare(cal1.get(Calendar.DAY_OF_YEAR), cal2.get(Calendar.DAY_OF_YEAR));
            if (dayOfYearComparison != 0) {
                return dayOfYearComparison;
            }

            return Long.compare(item2.getId(), item1.getId());
        };
    }
}
