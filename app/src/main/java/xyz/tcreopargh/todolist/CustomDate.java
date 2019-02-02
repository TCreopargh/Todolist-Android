package xyz.tcreopargh.todolist;

import android.content.Context;
import android.text.format.DateFormat;
import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author TCreopargh
 */
public class CustomDate {

    private int year;
    private int month;
    private int day;

    public CustomDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
    public static String getDateString(Calendar time, Context context) {
        boolean is24Hrs = DateFormat.is24HourFormat(context);
        SimpleDateFormat dateFormat;
        if (is24Hrs) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E",
                context.getResources().getConfiguration().locale);
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a E",
                context.getResources().getConfiguration().locale);
        }
        return dateFormat.format(time.getTime());
    }

    public static String getIntervalString(Calendar notificationTime, Context context) {
        Calendar time = Calendar.getInstance();
        String intervalText;
        long interval = notificationTime.getTimeInMillis() - time.getTimeInMillis();
        if (interval < 60 * 1000) {
            intervalText = (int) interval / 1000 + "秒";
        } else if (interval < 60 * 60 * 1000) {
            intervalText = (int) interval / (60 * 1000) + "分钟";
        } else if (interval < 24 * 60 * 60 * 1000) {
            intervalText =
                (int) interval / (60 * 60 * 1000) + "小时" + (int) interval % (60 * 60 * 1000) / (60 * 1000) + "分钟";
        } else {
            intervalText =
                (int) interval / (24 * 60 * 60 * 1000) + "天" + (int) interval % (24 * 60 * 60 * 1000) / (60 * 60 * 1000)
                    + "小时";
        }
        return intervalText;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @NonNull
    @Override
    public String toString() {
        return year + "-" + month + "-" + day;
    }
}
