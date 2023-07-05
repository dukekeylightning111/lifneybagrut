package com.example.mysportfriends_school_project;
import android.text.InputFilter;
import android.text.Spanned;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateInputFilter implements InputFilter {
    private SimpleDateFormat sdf;

    public DateInputFilter() {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.sdf.setLenient(false);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source.subSequence(start, end).toString());

        if (!isValidDate(builder.toString())) {
            return "";
        }

        return null;
    }

    private boolean isValidDate(String date) {
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}