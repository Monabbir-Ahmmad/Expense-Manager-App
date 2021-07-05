package com.example.hishab.ui.overview;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.util.Pair;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class FilterDialog extends AppCompatDialogFragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnPositiveButtonClickListener listener;
    private AutoCompleteTextView filterCategory, filterSortBy;
    private Button filterDateRange;
    private long startTimestamp = 1L;
    private long endTimestamp = 4200000000000L;

    public FilterDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this dialog fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View view = getActivity().getLayoutInflater().inflate(R.layout.filter_dialog, null);
        builder.setView(view)
                .setTitle("Filter")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("APPLY", (dialog, which) -> {
                    listener.OnPositiveButtonClick(filterCategory.getText().toString(), filterSortBy.getText().toString(), startTimestamp, endTimestamp);
                    dismiss();
                });

        //Find views
        filterDateRange = view.findViewById(R.id.filter_dateRange);
        filterCategory = view.findViewById(R.id.filter_category);
        filterSortBy = view.findViewById(R.id.filter_sortBy);

        long timeOffSet = TimeZone.getDefault().getRawOffset();

        //This is the date range picker
        filterDateRange.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select date range")
                    .build();

            dateRangePicker.show(getActivity().getSupportFragmentManager(), "Date picker");

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                startTimestamp = selection.first - timeOffSet;
                endTimestamp = selection.second - timeOffSet + DateTimeUtil.DAY_IN_MS - 1000L;

                filterDateRange.setText(String.format("%s - %s", dateFormat.format(startTimestamp), dateFormat.format(endTimestamp)));
            });
        });


        //This is the category dropdown
        ArrayList<String> category = new ArrayList<>();
        category.add("All");
        category.addAll(Arrays.asList(getResources().getStringArray(R.array.categoryArray)));
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_dropdown_filter, category);
        filterCategory.setText(categoryAdapter.getItem(0), false);
        filterCategory.setAdapter(categoryAdapter);

        //This is the sort by dropdown
        String[] sortBy = getResources().getStringArray(R.array.sortByArray);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_dropdown_filter, sortBy);
        filterSortBy.setText(sortByAdapter.getItem(0), false);
        filterSortBy.setAdapter(sortByAdapter);

        return builder.create();
    }

    //Set filter apply listener
    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }

    //Interface for OnPositiveButtonClickListener
    public interface OnPositiveButtonClickListener {
        //This applies the filter
        void OnPositiveButtonClick(String category, String sortBy, long startTimestamp, long endTimestamp);
    }

}
