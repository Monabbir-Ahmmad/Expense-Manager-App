package com.example.hishab.ui.statistics.charts;

import android.content.Context;
import android.util.TypedValue;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.example.hishab.mutilities.DateTimeUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class StatBarChart {
    private final Context context;
    private final BarChart barChart;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final TypedValue colorBlackWhite;
    private final TypedValue colorPrimary;
    private final DatabaseHelper databaseHelper;


    public StatBarChart(Context context, BarChart barChart) {
        this.context = context;
        this.barChart = barChart;

        databaseHelper = new DatabaseHelper(context);

        //This gets a color according to theme
        colorBlackWhite = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBlackWhite, colorBlackWhite, true);
        colorPrimary = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

    }

    //This sets the data into the bar chart
    public void initBarChart(int selectedYear) {
        //Clear chart before updating data
        barChart.clear();

        ArrayList<BarEntry> barEntryExpenseArray = new ArrayList<>();
        ArrayList<BarEntry> barEntryIncomeArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, selectedYear);

        boolean dataFound = false;

        for (int i = 0; i < 12; i++) {
            calendar.set(Calendar.MONTH, i);

            long monthStart = calendar.getTimeInMillis();
            long monthEnd = monthStart + (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * DateTimeUtil.DAY_IN_MS) - 1L;

            //Expense
            float monthlyExpenseSum = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", monthStart, monthEnd);
            barEntryExpenseArray.add(new BarEntry(i, monthlyExpenseSum));

            //Income
            float monthlyIncomeSum = databaseHelper.getFilteredSum(DataItem.INCOME, "All", monthStart, monthEnd);
            barEntryIncomeArray.add(new BarEntry(i, monthlyIncomeSum));

            if ((monthlyExpenseSum > 0 || monthlyIncomeSum > 0) && !dataFound)
                dataFound = true;

        }

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

    //This creates the bar chart
    private void renderBarChart(BarDataSet barExpenseDataSet, BarDataSet barIncomeDataSet, BarData barData) {
        //Marker view
        BarChartMarker barChartMarker = new BarChartMarker(context);
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
        barExpenseDataSet.setColor(context.getColor(R.color.light_red));
        barIncomeDataSet.setColor(context.getColor(R.color.light_green));
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
