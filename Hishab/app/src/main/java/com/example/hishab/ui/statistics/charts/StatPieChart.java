package com.example.hishab.ui.statistics.charts;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;

import androidx.preference.PreferenceManager;

import com.example.hishab.R;
import com.example.hishab.data.DataItem;
import com.example.hishab.database.DatabaseHelper;
import com.example.hishab.mutilities.DateTimeUtil;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatPieChart {
    private final Context context;
    private final PieChart pieChart;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final TypedValue colorBlackWhite;
    private final DatabaseHelper databaseHelper;


    public StatPieChart(Context context, PieChart pieChart) {
        this.context = context;
        this.pieChart = pieChart;

        databaseHelper = new DatabaseHelper(context);

        //This gets a color according to theme
        colorBlackWhite = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBlackWhite, colorBlackWhite, true);

    }

    //This sets the data into the pie chart
    public void initPieChart(int transactionTypeId, int choiceId) {
        //Clear chart before updating data
        pieChart.clear();

        ArrayList<PieEntry> pieEntryArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        long startTime = 0, endTime = 0;
        int endTimeRange = 0;
        float categorySum, totalExpense = 0;
        String transactionType = null;
        String[] categoryArray = null;

        if (transactionTypeId == R.id.btn_pie_ex) {
            categoryArray = context.getResources().getStringArray(R.array.expenseCategoryArray);
            transactionType = DataItem.EXPENSE;
        } else if (transactionTypeId == R.id.btn_pie_in) {
            categoryArray = context.getResources().getStringArray(R.array.incomeCategoryArray);
            transactionType = DataItem.INCOME;
        }

        switch (choiceId) {
            case R.id.btn_pie_week:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                endTimeRange = calendar.getActualMaximum(Calendar.DAY_OF_WEEK);
                break;
            case R.id.btn_pie_month:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                endTimeRange = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
            case R.id.btn_pie_year:
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                endTimeRange = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                break;
            default:
                calendar.clear();
                endTimeRange = 0;
                break;
        }

        startTime = calendar.getTimeInMillis();
        endTime = startTime + (endTimeRange * DateTimeUtil.DAY_IN_MS) - 1L;

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

    //This is the pie chart design
    private void renderPieChart(PieDataSet pieDataSet, float totalExpense) {
        String currency = PreferenceManager.getDefaultSharedPreferences(context).getString("currency", "$");

        //This gets a color array
        int[] colorArray = context.getResources().getIntArray(R.array.colorArray);
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
                centerText.setSpan(new ForegroundColorSpan(colorArray[(int) h.getX()]), amount.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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


}
