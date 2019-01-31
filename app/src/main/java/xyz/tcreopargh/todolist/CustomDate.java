package xyz.tcreopargh.todolist;

import android.content.Context;
import androidx.annotation.NonNull;
import java.util.Calendar;

public class CustomDate {

    private int year;
    private int month;
    private int day;

    public CustomDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
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
