package com.example.hishab.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.example.hishab.DateTimeUtil;
import com.example.hishab.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LineChartMarker extends MarkerView {

    private final TextView textView;
    private final long startTimestamp;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private final TypedValue colorPrimary, surfaceColor;
    private final String currency;


    //Constructor
    public LineChartMarker(Context context, long startTimestamp) {
        super(context, R.layout.marker_view);
        this.startTimestamp = startTimestamp;

        //Find views
        textView = findViewById(R.id.textView_marker);

        colorPrimary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        surfaceColor = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.surfaceColor, surfaceColor, true);
        currency = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("currency", "$");
    }

    //Callbacks every time the MarkerView is redrawn, can be used to update the views
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String date = dateFormat.format(startTimestamp + (long) e.getX() * DateTimeUtil.DAY_IN_MS);
        String amount = decimalFormat.format(e.getY());
        textView.setText(String.format("%s : %s%s", date, currency, amount));
        super.refreshContent(e, highlight);
    }

    //Canvas draw position
    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(surfaceColor.data);
        canvas.drawCircle(posX, posY, 12f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setColor(colorPrimary.data);
        canvas.drawCircle(posX, posY, 12f, paint);

        if (posY >= getChartView().getHeight() / 2) { //When value in above chart center
            posY = posY - getHeight();
        } else { //When value in below chart center
            posY = posY + getHeight();
        }
        super.draw(canvas, posX, posY);
        getOffsetForDrawingAtPoint(posX, posY);
    }

    //Marker position center of highlighted point
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -(getHeight() / 2));
    }
}
