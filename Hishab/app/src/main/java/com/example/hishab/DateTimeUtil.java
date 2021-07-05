package com.example.hishab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {

    public static final long DAY_IN_MS = 86400000L;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());

    //Constructor
    public DateTimeUtil() {
    }


    //Generates Timestamp in sec form Date and time
    public long getTimestamp(String date, String time) {
        long timestamp = 0L;
        try {
            Date datetime = dateTimeFormat.parse(date + " " + time);
            timestamp = datetime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }


    //Get get date from timestamp
    public String getDate(long timestamp) {
        return dateFormat.format(timestamp);
    }


    //Get get time from timestamp
    public String getTime(long timestamp) {
        return timeFormat.format(timestamp);
    }


    //Get time ago from timestamp
    public String getTimeAgo(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        long today = calendar.getTimeInMillis();

        if (timestamp >= today && timestamp < today + DAY_IN_MS)
            return "Today " + timeFormat.format(timestamp);

        else if (timestamp >= today + DAY_IN_MS && timestamp < today + (DAY_IN_MS * 2))
            return "Tomorrow " + timeFormat.format(timestamp);

        else if (timestamp >= today - DAY_IN_MS && timestamp < today)
            return "Yesterday " + timeFormat.format(timestamp);

        else
            return dateFormat.format(timestamp);
    }


}
