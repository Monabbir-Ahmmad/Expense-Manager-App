package com.example.hishab.ui.overview;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.util.Pair;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FilterDialog extends AppCompatDialogFragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final List<String> categoryArray = new ArrayList<>();
    private OnPositiveButtonClickListener listener;
    private AutoCompleteTextView filterSortBy;
    private ChipGroup chipGroupIn, chipGroupEx;
    private Button filterDateRange, clearSelectChipGroupIn, clearSelectChipGroupEx;
    private long startTimestamp = 1L;
    private long endTimestamp = 4200000000000L;
    private boolean isChipGroupInClear = false, isChipGroupExClear = false;

    //Constructor
    public FilterDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this dialog fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View view = getActivity().getLayoutInflater().inflate(R.layout.filter_dialog, null);
        builder.setView(view)
                .setTitle("Filter")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("APPLY", (dialog, which) -> {
                    //Add income categories
                    for (Integer id : chipGroupIn.getCheckedChipIds()) {
                        Chip chip = chipGroupIn.findViewById(id);
                        categoryArray.add(chip.getText().toString());
                    }
                    //Add expense categories
                    for (Integer id : chipGroupEx.getCheckedChipIds()) {
                        Chip chip = chipGroupEx.findViewById(id);
                        categoryArray.add(chip.getText().toString());
                    }
                    listener.OnPositiveButtonClick(categoryArray, filterSortBy.getText().toString(), startTimestamp, endTimestamp);
                });

        //Find views
        filterDateRange = view.findViewById(R.id.filter_dateRange);
        filterSortBy = view.findViewById(R.id.filter_sortBy);
        chipGroupIn = view.findViewById(R.id.filter_chipGroup_in);
        chipGroupEx = view.findViewById(R.id.filter_chipGroup_ex);
        clearSelectChipGroupIn = view.findViewById(R.id.btn_clear_chipIn);
        clearSelectChipGroupEx = view.findViewById(R.id.btn_clear_chipEx);


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


        clearSelectChipGroupIn.setOnClickListener(v -> {
            if (isChipGroupInClear) {
                selectAllChips(chipGroupIn);
            } else {
                chipGroupIn.clearCheck();
            }

            isChipGroupInClear = !isChipGroupInClear;
            clearSelectChipGroupIn.setText(isChipGroupInClear ? "Select all" : "Clear all");
        });


        clearSelectChipGroupEx.setOnClickListener(v -> {
            if (isChipGroupExClear) {
                selectAllChips(chipGroupEx);
            } else {
                chipGroupEx.clearCheck();
            }

            isChipGroupExClear = !isChipGroupExClear;
            clearSelectChipGroupEx.setText(isChipGroupExClear ? "Select all" : "Clear all");
        });

        addChipsToGroup(chipGroupIn, getResources().getStringArray(R.array.incomeCategoryArray));
        addChipsToGroup(chipGroupEx, getResources().getStringArray(R.array.expenseCategoryArray));

        //This is the sort by dropdown
        String[] sortBy = getResources().getStringArray(R.array.sortByArray);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_dropdown_filter, sortBy);
        filterSortBy.setText(sortByAdapter.getItem(0), false);
        filterSortBy.setAdapter(sortByAdapter);

        return builder.create();
    }

    //Add chips to chip group
    private void addChipsToGroup(ChipGroup chipGroup, String[] categoryArray) {
        for (String text : categoryArray) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_filter_chip, chipGroup, false);
            chip.setId(View.generateViewId());
            chip.setText(text);
            chip.setChecked(true);
            chipGroup.addView(chip);
        }
    }

    //Select all chips in chip group
    private void selectAllChips(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(true);
        }
    }

    //Set filter apply listener
    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }

    //Interface for OnPositiveButtonClickListener
    public interface OnPositiveButtonClickListener {
        //This applies the filter
        void OnPositiveButtonClick(List<String> categoryArray, String sortBy, long startTimestamp, long endTimestamp);
    }

}
