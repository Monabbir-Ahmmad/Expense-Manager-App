package com.example.hishab.ui.statistics.charts;

import android.content.Context;
import android.util.TypedValue;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.example.hishab.mutilities.DateTimeUtil;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StatLineChart {
    private final Context context;
    private final LineChart lineChart;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final TypedValue colorBlackWhite;
    private final TypedValue colorPrimary;
    private final DatabaseHelper databaseHelper;


    public StatLineChart(Context context, LineChart lineChart) {
        this.context = context;
        this.lineChart = lineChart;

        databaseHelper = new DatabaseHelper(context);

        //This gets a color according to theme
        colorBlackWhite = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBlackWhite, colorBlackWhite, true);
        colorPrimary = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

    }

    //This sets the data into the line chart
    public void initLineChart(int selectedMonth, int selectedYear) {
        //Clear chart before updating data
        lineChart.clear();

        ArrayList<Entry> lineEntryArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);

        long lineStartPosX = calendar.getTimeInMillis();

        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);

            long dayStart = calendar.getTimeInMillis();
            long dayEnd = dayStart + DateTimeUtil.DAY_IN_MS - 1L;

            float dailyExpenseSum = databaseHelper.getFilteredSum(DataItem.EXPENSE, "All", dayStart, dayEnd);

            if (dailyExpenseSum > 0)
                lineEntryArray.add(new Entry(i - 1, dailyExpenseSum));
        }


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

    //This creates the line chart
    private void renderLineChart(LineDataSet lineDataSet, long lineStartPosX) {
        //Marker view
        LineChartMarker lineChartMarker = new LineChartMarker(context, lineStartPosX);
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
}
