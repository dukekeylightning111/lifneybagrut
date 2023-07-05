package com.example.mysportfriends_school_project;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

public class TimeInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source.subSequence(start, end).toString());

        if (!isValidTime(builder.toString())) {
            return "";
        }

        return null;
    }

    private boolean isValidTime(String time) {
        return time.matches("^([01]\\d|2[0-3]):([0-5]\\d)$");
    }
}

