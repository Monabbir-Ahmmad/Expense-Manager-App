package com.example.hishab.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment implements View.OnClickListener {

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private Button btnLineSort, btnBarSort;
    private TextView tvDailyAvgEx, tvMonthlyTotalEx, tvMonthAvgEx, tvMonthAvgIn, tvYearlyTotalEx, tvYearlyTotalIn;
    private MaterialButtonToggleGroup grpBtnPieSort, grpBtnPieTransactionType;
    private DatabaseHelper databaseHelper;
    private TypedValue colorBlackWhite, colorPrimary;
    private String currency;

    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");

        //Find views
        pieChart = view.findViewById(R.id.pieChart);
        lineChart = view.findViewById(R.id.lineChart);
        barChart = view.findViewById(R.id.barChart);
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

        //This gets a color according to theme
        colorBlackWhite = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorBlackWhite, colorBlackWhite, true);
        colorPrimary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        btnLineSort.setOnClickListener(this);
        btnBarSort.setOnClickListener(this);
        grpBtnPieTransactionType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked)
                initPieChart(checkedId, grpBtnPieSort.getCheckedButtonId());
        });
        grpBtnPieSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked)
                initPieChart(grpBtnPieTransactionType.getCheckedButtonId(), checkedId);
        });

        Calendar calendar = Calendar.getInstance();

        initPieChart(grpBtnPieTransactionType.getCheckedButtonId(), grpBtnPieSort.getCheckedButtonId());

        btnLineSort.setText(monthYearFormat.format(calendar.getTime()));
        initLineChart(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        btnBarSort.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        initBarChart(calendar.get(Calendar.YEAR));

        return view;
    }


    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();

        if (v.getId() == R.id.button_lineChart_sort) { //Open Month and Year picker
            MonthYearPicker monthYearPicker = new MonthYearPicker()
                    .setYearMin(1970)
                    .setYearMax(3000)
                    .setYear(calendar.get(Calendar.YEAR))
                    .setMonth(calendar.get(Calendar.MONTH));

            monthYearPicker.setOnPositiveButtonClickListener((selectedMonth, selectedYear) -> {
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                btnLineSort.setText(monthYearFormat.format(calendar.getTime()));
                initLineChart(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            });

            monthYearPicker.show(getActivity().getSupportFragmentManager(), "MonthYearPicker");

        } else if (v.getId() == R.id.button_barChart_sort) { //Open Year picker
            MonthYearPicker yearPicker = new MonthYearPicker()
                    .setYearMin(1970)
                    .setYearMax(3000)
                    .setYear(calendar.get(Calendar.YEAR))
                    .setShowYearOnly(true);

            yearPicker.setOnPositiveButtonClickListener((selectedMonth, selectedYear) -> {
                btnBarSort.setText(String.valueOf(selectedYear));
                initBarChart(selectedYear);
            });

            yearPicker.show(getActivity().getSupportFragmentManager(), "MonthYearPicker");
        }

    }


    //This sets the data into the pie chart
    private void initPieChart(int transactionTypeId, int choiceId) {
        //Clear chart before updating data
        pieChart.clear();

        ArrayList<PieEntry> pieEntryArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        long startTime = 0, endTime = 0;
        float categorySum, totalExpense = 0;
        String transactionType = null;
        String[] categoryArray = null;

        if (transactionTypeId == R.id.btn_pie_ex) {
            categoryArray = getResources().getStringArray(R.array.expenseCategoryArray);
            transactionType = DataItem.EXPENSE;
        } else if (transactionTypeId == R.id.btn_pie_in) {
            categoryArray = getResources().getStringArray(R.array.incomeCategoryArray);
            transactionType = DataItem.INCOME;
        }

        if (choiceId == R.id.btn_pie_week) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            startTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            endTime = calendar.getTimeInMillis() + DateTimeUtil.DAY_IN_MS - 1000L;

        } else if (choiceId == R.id.btn_pie_month) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            endTime = calendar.getTimeInMillis() + DateTimeUtil.DAY_IN_MS - 1000L;

        } else if (choiceId == R.id.btn_pie_year) {
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            startTime = calendar.getTimeInMillis();
            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            endTime = calendar.getTimeInMillis() + DateTimeUtil.DAY_IN_MS - 1000L;
        }

        //This adds the values of each category into the PieEntry
        for (String category : categoryArray) {
            categorySum = databaseHelper.getFilteredSum(transactionType, category, startTime, endTime);
            totalExpense += categorySum;
            if (categorySum > 0)
                pieEntryArray.add(new PieEntry(categorySum, category));
        }

        if (pieEntryArray.size() > 0) { //Insert PieEntries into the PieDataSet and create PieData from PieDataSet
            PieDataSet pieDataSet = new PieDataSet(pieEntryArray, null);
            PieData pieData = new PieData(pieDataSet);

            pieChart.setData(pieData);
            renderPieChart(pieDataSet, totalExpense);
        }

        //No data text
        pieChart.setNoDataText("No Data Found");
        pieChart.setNoDataTextColor(colorBlackWhite.data);
        pieChart.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(20f));

    }

    //This sets the data into the line chart
    private void initLineChart(int selectedMonth, int selectedYear) {
        //Clear chart before updating data
        lineChart.clear();

        ArrayList<Entry> lineEntryArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        int numOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        long dayStart, dayEnd;
        float dailyExpenseSum, dailyAverageExpense, monthlyTotalExpense = 0;
        long lineStartPosX = calendar.getTimeInMillis();

        for (int i = 1; i <= numOfDays; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);

            dayStart = calendar.getTimeInMillis();
            dayEnd = dayStart + DateTimeUtil.DAY_IN_MS - 1000L;

            dailyExpenseSum = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", dayStart, dayEnd);
            monthlyTotalExpense += dailyExpenseSum;

            if (dailyExpenseSum > 0)
                lineEntryArray.add(new Entry(i - 1, dailyExpenseSum));
        }

        dailyAverageExpense = monthlyTotalExpense / numOfDays;

        tvDailyAvgEx.setText(String.format("%s%s", currency, decimalFormat.format(dailyAverageExpense)));
        tvMonthlyTotalEx.setText(String.format("%s%s", currency, decimalFormat.format(monthlyTotalExpense)));

        if (lineEntryArray.size() > 0) { //Insert LineEntries into the LineDataSet and create LineData from LineDataSet
            LineDataSet lineDataSet = new LineDataSet(lineEntryArray, null);
            LineData lineData = new LineData(lineDataSet);

            lineChart.setData(lineData);
            renderLineChart(lineDataSet, lineStartPosX);
        }

        //No data text
        lineChart.setNoDataText("No Data Found");
        lineChart.setNoDataTextColor(colorBlackWhite.data);
        lineChart.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(20f));

    }


    //This sets the data into the bar chart
    private void initBarChart(int selectedYear) {
        //Clear chart before updating data
        barChart.clear();

        ArrayList<BarEntry> barEntryExpenseArray = new ArrayList<>();
        ArrayList<BarEntry> barEntryIncomeArray = new ArrayList<>();


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        long monthStart, monthEnd;
        float monthlyExpenseSum, monthlyAverageExpense, yearlyTotalExpense = 0;
        float monthlyIncomeSum, monthlyAverageIncome, yearlyTotalIncome = 0;
        boolean dataFound = false;

        for (int i = 0; i < 12; i++) {
            calendar.set(Calendar.MONTH, i);

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            monthStart = calendar.getTimeInMillis();

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            monthEnd = calendar.getTimeInMillis() + DateTimeUtil.DAY_IN_MS - 1000L;

            //Expense
            monthlyExpenseSum = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", monthStart, monthEnd);
            yearlyTotalExpense += monthlyExpenseSum;
            barEntryExpenseArray.add(new BarEntry(i, monthlyExpenseSum));

            //Income
            monthlyIncomeSum = databaseHelper.getFilteredSum(DataItem.INCOME, "All", monthStart, monthEnd);
            yearlyTotalIncome += monthlyIncomeSum;
            barEntryIncomeArray.add(new BarEntry(i, monthlyIncomeSum));

            if ((monthlyExpenseSum > 0 || monthlyIncomeSum > 0) && !dataFound)
                dataFound = true;

        }

        monthlyAverageExpense = yearlyTotalExpense / 12;
        monthlyAverageIncome = yearlyTotalIncome / 12;

        tvMonthAvgIn.setText(String.format("Income: %s%s", currency, decimalFormat.format(monthlyAverageIncome)));
        tvMonthAvgEx.setText(String.format("Expense: %s%s", currency, decimalFormat.format(monthlyAverageExpense)));

        tvYearlyTotalIn.setText(String.format("Income: %s%s", currency, decimalFormat.format(yearlyTotalIncome)));
        tvYearlyTotalEx.setText(String.format("Expense: %s%s", currency, decimalFormat.format(yearlyTotalExpense)));

        if (dataFound) { //Insert BarEntries into BarDataSet and create BarData from BarDataSet
            BarDataSet barExpenseDataSet = new BarDataSet(barEntryExpenseArray, DataItem.EXPENSE);
            BarDataSet barIncomeDataSet = new BarDataSet(barEntryIncomeArray, DataItem.INCOME);
            BarData barData = new BarData(barIncomeDataSet, barExpenseDataSet);

            barChart.setData(barData);
            renderBarChart(barExpenseDataSet, barIncomeDataSet, barData);
        }

        //No data text
        barChart.setNoDataText("No Data Found");
        barChart.setNoDataTextColor(colorBlackWhite.data);
        barChart.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(20f));

    }


    //This is the pie chart design
    private void renderPieChart(PieDataSet pieDataSet, float totalExpense) {
        //This gets a color array
        int[] colorArray = getContext().getResources().getIntArray(R.array.colorArray);
        List<Integer> colorList = new ArrayList<>(colorArray.length);
        for (int i : colorArray) {
            colorList.add(i);
        }


        //Pie chart click event
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Change the center text to selected entry value and label with SpannableString styling
                PieEntry entry = (PieEntry) e;
                String amount = currency + decimalFormat.format(e.getY());
                String label = entry.getLabel();
                SpannableString centerText = new SpannableString(amount + "\n" + label);
                centerText.setSpan(new RelativeSizeSpan(.65f), amount.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                pieChart.setCenterText(centerText);
            }

            @Override
            public void onNothingSelected() { //Reset center text
                String amount = currency + decimalFormat.format(totalExpense);
                String label = "Total";
                SpannableString centerText = new SpannableString(amount + "\n" + label);
                centerText.setSpan(new RelativeSizeSpan(.65f), amount.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                pieChart.setCenterText(centerText);
            }
        });

        //Legends of the chart
        Legend legend = pieChart.getLegend();
        legend.setTextColor(colorBlackWhite.data);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10f);
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setYEntrySpace(7f);
        legend.setDrawInside(true);

        //Description of the chart
        Description description = pieChart.getDescription();
        description.setEnabled(false);

        //Pie slice attribute
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setIconsOffset(new MPPointF(0, 40));
        pieDataSet.setColors(colorArray);

        //Outside values with line
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLinePart1OffsetPercentage(100f);
        pieDataSet.setValueLinePart1Length(0.25f);
        pieDataSet.setValueLinePart2Length(0.15f);
        pieDataSet.setValueLineWidth(2f);
        pieDataSet.setUsingSliceColorAsValueLineColor(true);

        //Pie value attr
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setValueTextColors(colorList);
        pieDataSet.setValueFormatter(new PercentFormatter(pieChart));

        //Entry label
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);

        //Transparent circle
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);

        //Center hole
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(90f);
        pieChart.setCenterTextSize(60f);
        pieChart.setCenterTextRadiusPercent(75f);
        pieChart.setHoleColor(Color.TRANSPARENT);

        //Center Text
        String amount = currency + decimalFormat.format(totalExpense);
        String label = "Total";
        SpannableString centerText = new SpannableString(amount + "\n" + label);
        centerText.setSpan(new RelativeSizeSpan(.65f), amount.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextColor(colorBlackWhite.data);
        pieChart.setCenterTextSize(24f);

        //Animation
        pieChart.animateXY(1000, 1000);
        pieChart.setDragDecelerationFrictionCoef(0.97f);

        //Off set
        pieChart.setExtraOffsets(0f, 15f, 0f, 55f);

        //Refresh chart
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();

    }

    //This creates the line chart
    private void renderLineChart(LineDataSet lineDataSet, long lineStartPosX) {
        //Marker view
        LineChartMarker lineChartMarker = new LineChartMarker(getActivity(), lineStartPosX);
        lineChartMarker.setChartView(lineChart);
        lineChart.setMarker(lineChartMarker);

        //Touch attribute
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawBorders(false);

        //Line attribute
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(colorPrimary.data);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleRadius(1.5f);
        lineDataSet.setCircleColor(colorPrimary.data);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);

        //Highlight
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setHighLightColor(colorPrimary.data);
        lineDataSet.setHighlightLineWidth(1f);

        //Description of the chart
        Description description = lineChart.getDescription();
        description.setEnabled(false);

        //Legends of the chart
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        //Y axis left
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setEnabled(true);
        yAxisLeft.setDrawAxisLine(false);
        if (lineDataSet.getYMax() >= 2000)
            yAxisLeft.setGranularity(1000);
        else if (lineDataSet.getYMax() >= 1000)
            yAxisLeft.setGranularity(100);
        else
            yAxisLeft.setGranularity(10);

        yAxisLeft.setLabelCount(5);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setTextColor(colorBlackWhite.data);
        yAxisLeft.enableGridDashedLine(10f, 10f, 0f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value >= 1000000) {
                    return Math.round(value / 1000000) + "M";
                } else if (value >= 1000) {
                    return Math.round(value / 1000) + "K";
                }
                return decimalFormat.format(value);
            }
        });

        //X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1);
        xAxis.setLabelCount(5);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setTextColor(colorBlackWhite.data);
        xAxis.enableGridDashedLine(30f, 10000f, 10f);
        xAxis.setGridLineWidth(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return dateFormat.format(lineStartPosX + (long) value * DateTimeUtil.DAY_IN_MS);
            }
        });

        //Y axis left
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        //Animation
        lineChart.animateY(1000);

        //View port offset
        lineChart.setExtraOffsets(0, 0, 0, 10f);

        //Refresh chart
        lineChart.notifyDataSetChanged();
        lineChart.fitScreen();
        lineChart.invalidate();
    }

    //This creates the bar chart
    private void renderBarChart(BarDataSet barExpenseDataSet, BarDataSet barIncomeDataSet, BarData barData) {
        //Marker view
        BarChartMarker barChartMarker = new BarChartMarker(getActivity());
        barChartMarker.setChartView(barChart);
        barChart.setMarker(barChartMarker);

        //Bar attributes
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setFitBars(true);

        //Bar width
        barData.setBarWidth(0.25f);
        barData.groupBars(0, 0.4f, 0.05f);

        //Bar color
        barData.setDrawValues(false);
        barExpenseDataSet.setColor(getActivity().getColor(R.color.light_red));
        barIncomeDataSet.setColor(getActivity().getColor(R.color.light_green));
        barExpenseDataSet.setHighLightAlpha(50);
        barIncomeDataSet.setHighLightAlpha(50);


        //Description of the chart
        Description description = barChart.getDescription();
        description.setEnabled(false);

        //Legends of the chart
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        //Y axis left
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setEnabled(true);
        yAxisLeft.setDrawAxisLine(false);
        if (barExpenseDataSet.getYMax() >= 2000)
            yAxisLeft.setGranularity(1000);
        else if (barExpenseDataSet.getYMax() >= 1000)
            yAxisLeft.setGranularity(100);
        else
            yAxisLeft.setGranularity(10);

        yAxisLeft.setLabelCount(5);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setTextColor(colorBlackWhite.data);
        yAxisLeft.enableGridDashedLine(10f, 10f, 0f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value >= 1000000) {
                    return Math.round(value / 1000000) + "M";
                } else if (value >= 1000) {
                    return Math.round(value / 1000) + "K";
                }
                return decimalFormat.format(value);
            }
        });

        //X axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1);
        xAxis.setLabelCount(12);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(12);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(colorBlackWhite.data);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        String[] xAxisLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value >= 0)
                    return xAxisLabels[(int) value % xAxisLabels.length];
                return "";
            }
        });

        //Y axis left
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        //Animation
        barChart.animateY(1000);

        //View port offset
        barChart.setExtraOffsets(0, 0, 0, 10f);

        //Refresh chart
        barChart.notifyDataSetChanged();
        barChart.fitScreen();
        barChart.invalidate();

    }

}

