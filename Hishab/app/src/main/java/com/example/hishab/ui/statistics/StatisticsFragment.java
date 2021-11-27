package com.example.hishab.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.example.hishab.mutilities.DateTimeUtil;
import com.example.hishab.mutilities.MonthYearPicker;
import com.example.hishab.ui.statistics.charts.StatBarChart;
import com.example.hishab.ui.statistics.charts.StatLineChart;
import com.example.hishab.ui.statistics.charts.StatPieChart;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticsFragment extends Fragment implements View.OnClickListener {

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private Button btnLineSort, btnBarSort;
    private TextView tvDailyAvgEx, tvMonthlyTotalEx, tvMonthAvgEx, tvMonthAvgIn, tvYearlyTotalEx, tvYearlyTotalIn;
    private MaterialButtonToggleGroup grpBtnPieSort, grpBtnPieTransactionType;
    private DatabaseHelper databaseHelper;
    private String currency;
    private StatPieChart statPieChart;
    private StatLineChart statLineChart;
    private StatBarChart statBarChart;


    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");

        //Find views
        btnLineSort = view.findViewById(R.id.button_lineChart_sort);
        grpBtnPieSort = view.findViewById(R.id.grpBtn_pieChart_sort);
        grpBtnPieTransactionType = view.findViewById(R.id.grpBtn_pie_transactionType);
        btnBarSort = view.findViewById(R.id.button_barChart_sort);
        tvDailyAvgEx = view.findViewById(R.id.textView_dailyAvg);
        tvMonthlyTotalEx = view.findViewById(R.id.textView_monthlyTotal);
        tvMonthAvgEx = view.findViewById(R.id.textView_monthlyAvgEx);
        tvMonthAvgIn = view.findViewById(R.id.textView_monthlyAvgIn);
        tvYearlyTotalEx = view.findViewById(R.id.textView_yearlyTotalEx);
        tvYearlyTotalIn = view.findViewById(R.id.textView_yearlyTotalIn);


        databaseHelper = new DatabaseHelper(getActivity());
        currency = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("currency", "$");

        statPieChart = new StatPieChart(getActivity(), view.findViewById(R.id.pieChart));
        statLineChart = new StatLineChart(getActivity(), view.findViewById(R.id.lineChart));
        statBarChart = new StatBarChart(getActivity(), view.findViewById(R.id.barChart));

        btnLineSort.setOnClickListener(this);
        btnBarSort.setOnClickListener(this);

        grpBtnPieTransactionType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked)
                statPieChart.initPieChart(checkedId, grpBtnPieSort.getCheckedButtonId());
        });
        grpBtnPieSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked)
                statPieChart.initPieChart(grpBtnPieTransactionType.getCheckedButtonId(), checkedId);
        });

        Calendar calendar = Calendar.getInstance();

        statPieChart.initPieChart(grpBtnPieTransactionType.getCheckedButtonId(), grpBtnPieSort.getCheckedButtonId());

        btnLineSort.setText(monthYearFormat.format(calendar.getTime()));
        statLineChart.initLineChart(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        setLineChartSummary(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        btnBarSort.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        statBarChart.initBarChart(calendar.get(Calendar.YEAR));
        setBarChartSummary(calendar.get(Calendar.YEAR));

        return view;
    }


    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();

        //Open Month and Year picker
        if (v.getId() == R.id.button_lineChart_sort) {
            MonthYearPicker monthYearPicker = new MonthYearPicker()
                    .setYearMin(1970)
                    .setYearMax(3000)
                    .setYear(calendar.get(Calendar.YEAR))
                    .setMonth(calendar.get(Calendar.MONTH));

            monthYearPicker.setOnPositiveButtonClickListener((selectedMonth, selectedYear) -> {
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                btnLineSort.setText(monthYearFormat.format(calendar.getTime()));
                statLineChart.initLineChart(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                setLineChartSummary(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            });

            monthYearPicker.show(getActivity().getSupportFragmentManager(), "MonthYearPicker");

        }
        //Open Year picker
        else if (v.getId() == R.id.button_barChart_sort) {
            MonthYearPicker yearPicker = new MonthYearPicker()
                    .setYearMin(1970)
                    .setYearMax(3000)
                    .setYear(calendar.get(Calendar.YEAR))
                    .setShowYearOnly(true);

            yearPicker.setOnPositiveButtonClickListener((selectedMonth, selectedYear) -> {
                btnBarSort.setText(String.valueOf(selectedYear));
                statBarChart.initBarChart(selectedYear);
                setBarChartSummary(selectedYear);
            });

            yearPicker.show(getActivity().getSupportFragmentManager(), "MonthYearPicker");
        }

    }

    //This sets the summary views for the line chart
    private void setLineChartSummary(int selectedMonth, int selectedYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);

        int numOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        long monthStart = calendar.getTimeInMillis();
        long monthEnd = monthStart + (numOfDays * DateTimeUtil.DAY_IN_MS) - 1l;

        float monthlyTotalExpense = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", monthStart, monthEnd);

        tvDailyAvgEx.setText(String.format("%s%s", currency, decimalFormat.format(monthlyTotalExpense / numOfDays)));
        tvMonthlyTotalEx.setText(String.format("%s%s", currency, decimalFormat.format(monthlyTotalExpense)));

    }

    //This sets the summary views for the bar chart
    private void setBarChartSummary(int selectedYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, selectedYear);

        int numOfDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        long yearStart = calendar.getTimeInMillis();
        long yearEnd = yearStart + (numOfDays * DateTimeUtil.DAY_IN_MS) - 1l;

        float yearlyTotalExpense = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", yearStart, yearEnd);
        float yearlyTotalIncome = databaseHelper.getFilteredSum(DataItem.INCOME, "All", yearStart, yearEnd);

        tvMonthAvgIn.setText(String.format("Income: %s%s", currency, decimalFormat.format(yearlyTotalIncome / 12)));
        tvMonthAvgEx.setText(String.format("Expense: %s%s", currency, decimalFormat.format(yearlyTotalExpense / 12)));

        tvYearlyTotalIn.setText(String.format("Income: %s%s", currency, decimalFormat.format(yearlyTotalIncome)));
        tvYearlyTotalEx.setText(String.format("Expense: %s%s", currency, decimalFormat.format(yearlyTotalExpense)));

    }

}

