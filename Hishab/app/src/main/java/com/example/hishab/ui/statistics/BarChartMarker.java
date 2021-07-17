package com.example.hishab.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.example.hishab.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;

public class BarChartMarker extends MarkerView {

    private final TextView textView;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final String currency;
    //Constructor
    public BarChartMarker(Context context) {
        super(context, R.layout.marker_view);

        //Find views
        textView = findViewById(R.id.textView_marker);

        currency = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("currency", "$");
    }

    //Callbacks every time the MarkerView is redrawn, can be used to update the views
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        textView.setText(String.format("%s%s", currency, decimalFormat.format(e.getY())));
        super.refreshContent(e, highlight);
    }

    //Canvas draw position


    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        posY = posY - getHeight() / 4;
        super.draw(canvas, posX, posY);
        getOffsetForDrawingAtPoint(posX, posY);
    }

    //Marker position center of highlighted point
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
