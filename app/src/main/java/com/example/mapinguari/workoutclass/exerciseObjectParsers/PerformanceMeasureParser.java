package com.example.mapinguari.workoutclass.exerciseObjectParsers;

import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mapinguari on 1/12/16.
 */
public abstract class PerformanceMeasureParser {

    private static final Double THIRD_COLUMN_ERROR_MARGIN = 0.25;
    int NUMBER_OF_EMPTY_CELLS_ALLOWED_PER_ROW = 2;

    private String anyDigRegex = "[0-9]";
    private String anyOCRRegex = "[0-9\\.:]";
    private String minDecRegex = "[1-5]";
    private String secDecRegex = "[0-5]";


    private Pattern prettyDesperateRegex = Pattern.compile("([0-9\\.:]{1,2}:)?([0-9\\.:]?[0-9\\.:])?:[0-9\\.:]{2}\\.[0-9\\.:]");
    private Pattern basicTimeRegex = Pattern.compile("(\\d{1,2}:)?(\\d{2})?:\\d{2}\\.\\d");
    private Pattern betterTimeRegex = Pattern.compile("([0-9]{1,2}:)?([0-5]?[0-9])?:[0-5][0-9]\\.[0-9]");
    private Pattern desperateTimeRegex = Pattern.compile(anyOCRRegex+"{9,10}|"+ anyOCRRegex + "{5,7}");

    private Pattern distanceRegex = Pattern.compile("[0-9]{1,6}");

    private Pattern SPMRegex = Pattern.compile("[0-9]{1,2}");

    private Pattern CalRegex = Pattern.compile("[0-9]{1,4}");
    private Pattern PowerRegex = Pattern.compile("[0-9]{1,4}");


    private String timeString = null;
    private String distanceString = null;
    private String column3String = null;
    private String spmString = null;

    protected Double timeDouble = 0.0;
    protected Double distanceDouble = 0.0;
    protected Double column3Double = 0.0;
    protected Double splitDouble = 0.0;
    protected Integer spmInteger = 0;

    public PerformanceMeasureParser(String timeString, String distanceString, String column3String, String spmString) {
        this.timeString = timeString;
        this.distanceString = distanceString;
        this.column3String = column3String;
        this.spmString = spmString;
    }

    public PerformanceMeasureParser(Vector<String> stringRow){

        int emptyNum = 0;
        for(String s: stringRow){
            if(s.isEmpty())
                emptyNum++;
        }

//        if(stringRow.size() > 4){
//            //maybe try and figure out what columns should be merged
////            stringRow = reduceNumberofCols();
//        }
//
//        if(stringRow.size() < 4){
//            //try and figure out which columns to split.
////            stringRow = increaseNumberofCols();
//        }

        if(stringRow.size() == 4 && emptyNum == 0){
            this.timeString = stringRow.get(0);
            this.distanceString = stringRow.get(1);
            this.column3String = stringRow.get(2);
            this.spmString = stringRow.get(3);
        } else {
            this.timeString = "";
            this.distanceString = "";
            this.column3String = "";
            this.spmString = "";
        }
    }


    //This function takes the strings and parses them as well as possible.
    public boolean getAllValues(){
        boolean gotTime = getTime();
        boolean gotDistance = getDistance();
        boolean gotThirdColumn = getThirdColumn();
        boolean gotSplit = decipherThirdColumn();
        boolean gotSpm = getSPM();
        return gotTime&&gotDistance&&gotThirdColumn&&gotSplit&&gotSpm;
    }


    //These functions are just simple checks that the data is not essentially empty in each field
    public boolean gotTimeData(){
        return timeDouble != 0.0;
    }

    public boolean gotDistanceData(){
        return distanceDouble != 0.0;
    }

    public boolean gotcolumn3Data(){
        return column3Double != 0.0;
    }

    public boolean gotSplitData(){
        return splitDouble != 0.0;
    }

    public boolean gotSpmData(){
        return spmInteger != 0;
    }

    //Non null field checks end here


    //Managed to get all column Strings
    public boolean gotStringValues(){
        return timeString != null && distanceString != null && column3String != null && spmString != null;
    }


    //We have parsed the Strings into data, nothing has been done about the 3rd column
    public boolean gotDataValues() {
        return gotTimeData() && gotDistanceData() && gotcolumn3Data() && gotSpmData();
    }


    //We have all the data required for a performance monitor... Of sorts.
    public boolean gotCompleteData(){
        return gotTimeData() && gotDistanceData() && gotSplitData() && gotSpmData();
    }


    //Using the information at our disposal, will try to determine what the data in this column is
    // namely it will be split, watts, or calories.
    //It is possible that we will re write this function to only attempt to get the correct unit once
    // so as to prevent the algorithm checking each individual row.
    public boolean decipherThirdColumn(){
        boolean result = false;
        if(gotTimeData() && gotDistanceData()){
            Double split = PowerUnit.split(timeDouble,distanceDouble);
            Double watts = PowerUnit.watts(timeDouble,distanceDouble);
            Double cal = PowerUnit.calories(timeDouble,distanceDouble);

            Double dISplit = Math.abs(split - column3Double);
            Double dIWatts = Math.abs(watts - column3Double);
            Double dICal = Math.abs(cal - column3Double);
            Double[] diffAr = {dISplit,dIWatts,dICal};


            int posOfMin = 0;
            Double minDiff = Double.MAX_VALUE;


            for(int i = 0;i <3; i++){
                if(diffAr[i]<minDiff){
                    posOfMin = i;
                }
            }

            if(minDiff > diffAr[posOfMin]*THIRD_COLUMN_ERROR_MARGIN){
                posOfMin = 100;
            }


            switch(posOfMin){
                case(0): splitDouble = column3Double;
                    result = true;
                    break;
                case(1): splitDouble = PowerUnit.splitFromWatts(column3Double);
                    result = true;
                    break;
                case(2): splitDouble = PowerUnit.splitFromCal(column3Double);
                    break;
                default: splitDouble = 0.0;
                    result = false;
            }
        }
        return result;
    }


    //Parsing functions other than time start here

    public Integer getIntProto(String sS,int n,int m){

        sS = colonPeriodConvert(sS);
        //catch digits
        String regex;
        if(m < n){
            regex = "[\\d:\\.]"+"{"+n+",}";
        } else {
            regex = "[\\d:\\.]"+"{"+n+","+m+"}";

        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sS);

        Integer result = null;
        if(matcher.find()) {
            sS = matcher.group();
            result = Integer.valueOf(sS);
        }
        return result;
    }

    public boolean getSPM(){
        Integer result =getIntProto(spmString, 1, 2);
        if(result != null)
            spmInteger = result;
        return spmInteger != 0;
    }

    public boolean getDistance(){
        Integer result = getIntProto(distanceString,1,0);
        if(result != null)
            distanceDouble = result.doubleValue();
        return distanceDouble != 0.0;
    }

    public boolean getThirdColumn(){
        Double value = getTime(column3String);
        boolean result = false;
        if(value != null){
            column3Double = value;
            result = true;
        } else {
            Integer temp = getCaloriesAndWatts(column3String);
            if(temp != null){
                column3Double = temp.doubleValue();
                result = true;
            }
        }
        return result;
    }

    /*These are identical since they are both values between 1 and 4 digits long. Can think about
    how to be more precise about this later (possible values for both seperately)
     */
    public Integer getCaloriesAndWatts(String calS){
        return getIntProto(calS,1,4);
    }


    private String colonPeriodConvert(String s){
        s = s.replaceAll(":","1");
        s = s.replaceAll("\\.","0");
        return s;
    }

    //Non time parsing functions end here


    //Time Parsing functions start Here

    public boolean getTime(){
        Double result = getTime(timeString);
        if(result != null)
            timeDouble = result;
        return timeDouble != 0.0;
    }

    public Double getTime(String sS){
        Double result = null;
        String timeString = getTimeString(sS);
        try {
            result = ErgoFormatter.parseSeconds(timeString);
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }


    public String getTimeString(String sS){
        String result = null;
        Matcher matcher = betterTimeRegex.matcher(sS);
        if(matcher.find()){
            sS = matcher.group();
            result = sS;
        } else {
            matcher = prettyDesperateRegex.matcher(sS);
            if (matcher.find()) {
                sS = matcher.group();
                switch (sS.length()){
                    case 5 : result = length5(sS);
                        break;
                    case 6 : result = length6(sS);
                        break;
                    case 7 : result = length7(sS);
                        break;
                    case 9 : result = length9(sS);
                        break;
                    case 10 : result = length10(sS);
                        break;
                }
            }
        }
        return result;
    }

    private String length5(String string){
        string = colonPeriodConvert(string);
        String result = ":" + string.substring(1,2) + "." + string.charAt(4);
        return result;
    }

    private String length6(String string){
        String result;
        char min1 = string.charAt(0);
        if(min1 ==  ':' || min1 == '.')
            min1 = fixChar(min1);
        result = min1 + length5(string.substring(1));
        return result;
    }

    private String length7(String string){
        String result;
        char min2 = string.charAt(0);
        if(min2 == ':' || min2 == '.' )
            min2 = fixChar(min2);
        result = min2 + length6(string.substring(1));
        return result;
    }

    private String length9(String string){
        String result;
        char h1 = string.charAt(0);
        if(h1== ':' || h1 == '.' )
            h1 = fixChar(h1);
        result = h1 + ':' + length7(string.substring(2));
        return result;
    }

    private String length10(String string){
        String result;
        char h2 = string.charAt(0);
        if(h2== ':' || h2 == '.' )
            h2 = fixChar(h2);
        result = h2 + length9(string.substring(2));
        return result;
    }

    /**
     * @param c
     * @return
     */
    private char fixChar(char c){
        switch(c){
            case ':' : c = 1;
                break;
            case '.' : c = 1;
                break;
            case '6' : c = 5;
                break;
            case '7' : c = 2;
                break;
            case '8' : c = 3;
                break;
            case '9' : c = 5;
                break;
            case '0' : c = 3;
                break;
        }
        return c;
    }

    //Time parsing functions end here




}

