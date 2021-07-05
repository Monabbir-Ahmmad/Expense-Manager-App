package com.example.hishab.ui.statistics;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.hishab.R;

public class MonthYearPicker extends AppCompatDialogFragment {

    private TextView monthYearText;
    private boolean showYearOnly = false;
    private int yearMin = 1900, yearMax = 2100;
    private int selectedMonth = -1, selectedYear = -1;
    private OnPositiveButtonClickListener listener;

    //Constructor
    public MonthYearPicker() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this dialog fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View view = getActivity().getLayoutInflater().inflate(R.layout.month_year_picker, null);
        builder.setView(view)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    listener.onPositiveButtonClick(selectedMonth, selectedYear);
                    dismiss();
                });

        //Find views
        NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        monthYearText = view.findViewById(R.id.textView_monthYearText);

        selectedMonth = Math.max(selectedMonth, 0);
        selectedYear = Math.max(selectedYear, yearMin);

        String[] monthNames = getResources().getStringArray(R.array.monthsArray);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(monthNames);
        monthPicker.setValue(selectedMonth);

        yearPicker.setMinValue(yearMin);
        yearPicker.setMaxValue(yearMax);
        yearPicker.setValue(selectedYear);

        setMonthYearText(monthNames[selectedMonth], String.valueOf(selectedYear));

        if (showYearOnly) {
            monthPicker.setVisibility(View.GONE);
        }

        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedMonth = newVal;
            setMonthYearText(monthNames[selectedMonth], String.valueOf(selectedYear));

        });
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedYear = newVal;
            setMonthYearText(monthNames[selectedMonth], String.valueOf(selectedYear));

        });

        return builder.create();
    }

    // Set top textView
    private void setMonthYearText(String month, String year) {
        if (showYearOnly)
            monthYearText.setText(year);
        else
            monthYearText.setText(String.format("%s %s", month, year));

    }

    // Set the minimum year
    public MonthYearPicker setYearMin(int yearMin) {
        this.yearMin = yearMin;
        return this;
    }

    // Set the maximum year
    public MonthYearPicker setYearMax(int yearMax) {
        this.yearMax = yearMax;
        return this;
    }

    // Set if only year picker should show
    public MonthYearPicker setShowYearOnly(boolean showYearOnly) {
        this.showYearOnly = showYearOnly;
        return this;
    }

    // Set year on view show
    public MonthYearPicker setYear(int year) {
        selectedYear = year;
        return this;
    }

    // Set month on view show
    public MonthYearPicker setMonth(int month) {
        selectedMonth = month;
        return this;
    }

    //Set positive button click listener
    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }

    //Interface for OnPositiveButtonClickListener
    public interface OnPositiveButtonClickListener {

        void onPositiveButtonClick(int selectedMonth, int selectedYear);
    }
}
