package com.example.mapinguari.workoutclass.exerciseObjects;

import android.util.Log;

import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Formatter;

/**
 * Created by mapinguari on 8/11/15.
 */
public class ErgoFormatter{

    public static String formatSeconds(Double seconds) {
        int hours = (int) Math.floor(seconds / 3600);
        double secondsT = seconds % 3600;
        int mins = (int) Math.floor(secondsT / 60);
        double secsRem = secondsT % 60;
        int secs = (int) Math.floor(secsRem);
        //TODO: NOT SURE WHAT THE ERGO DOES HERE.
        int centi = (int) Math.round((secsRem % 1) * 10);
        Formatter formatter = new Formatter();
        if(hours > 0){
            formatter.format("%d:%02d:%02d.%d", hours, mins, secs, centi);
        } else if(mins > 0) {
            formatter.format("%d:%02d.%d", mins, secs, centi);
        }
        else{
            formatter.format(":%02d.%d", secs, centi);
        }
        return formatter.out().toString();
    }

    //HH:MM:SS.C
    public static Integer[] segmentedParse(String string) throws NotHumanStringException {
        String[] hmr = string.split(":");
        Integer hours = 0,mins = 0;
        String rem  = "";
        try {
            switch (hmr.length) {
                case (1): //Nothing parsed
                    rem = hmr[0];
                    break;
                case (2): //Parsed seconds
                    if(!(hmr[0].isEmpty())) {
                        mins = Integer.parseInt(hmr[0]);
                        rem = hmr[1];
                        break;
                    } else {
                        rem = hmr[1];
                        break;
                    }
                case (3): //Parsed mins & seconds
                    hours = Integer.parseInt(hmr[0]);
                    mins = Integer.parseInt(hmr[1]);
                    rem = hmr[2];
                    break;
            }
        }catch(NumberFormatException e){
            throw new NotHumanStringException();
        }
        String[] sc = rem.split("\\.");
        Integer seconds = 0,centi = 0;
        try {
            switch(sc.length){
                case(1):
                    throw new NotHumanStringException();
                case(2): //seconds and rem
                    seconds = Integer.parseInt(sc[0]);
                    centi = Integer.parseInt(sc[1].subSequence(0,1).toString());
            }
        }
        catch(NumberFormatException e){
            throw new NotHumanStringException();
        }
        Integer[] result = {hours,mins,seconds,centi};
        return result;
    }

    public static Double parseSeconds(String string) throws NotHumanStringException {
        Integer[] in = segmentedParse(string);
        return in[0]*3600 + in[1]*60 + in[2] + (((double) in[3])/10);
    }

}
