package com.example.mapinguari.workoutclass;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by mapinguari on 7/25/15.
 */
public class GregtoString {

    public static String getDateTime(GregorianCalendar gCal) {
        int year = gCal.get(Calendar.YEAR);
        int month = gCal.get(Calendar.MONTH);
        int day = gCal.get(Calendar.DAY_OF_MONTH);
        int hour = gCal.get(Calendar.HOUR_OF_DAY);
        int minute = gCal.get(Calendar.MINUTE);
        int seconds = gCal.get(Calendar.SECOND);
        return (toString(year) + "-" + toString(month) + "-" + toString(day) + "T"
                + toString(hour) + ":" + toString(minute) + ":" + toString(seconds));
    }

    private static String toString(int i){
        return (Integer.toString(i));
    }

    private static int toInt(String s){
        return (Integer.parseInt(s));
    }



    public static GregorianCalendar getGregCal(String dateTime){
        String[] date = dateTime.split("T")[0].split("-");
        String[] time = dateTime.split("T")[1].split(":");
        int year = toInt(date[0]);
        int month = toInt(date[1]);
        int day = toInt(date[2]);
        int hour = toInt(time[0]);
        int min = toInt(time[1]);
        int sec = toInt(time[2]);
        GregorianCalendar cal = new GregorianCalendar(year,month,day,hour,min,sec);
        return cal;
    }
}
