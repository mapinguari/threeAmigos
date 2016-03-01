package com.example.mapinguari.workoutclass.ergoGrids;


import android.graphics.Rect;
import android.util.Log;
//WRITE OWN RECTANGLE CLASS TO DECOUPLE FROM ANDROID

import com.example.mapinguari.workoutclass.exerciseObjects.Interval;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mapinguari on 2/22/16.
 */
public abstract class PM3plus {

    double screenWidth;
    double screenHeight;

    private double x = 7.9;

    final double fontHeight = 0.3/7.9;
    final double charWidth = 0.2/7.9;
    final double vGapSize = 0.25/x; //CHECK THIS VALUE

    private Rect boundingRect;

    final double ovLeft = 0.25/7.9;
    final double ovTop = 0.95/7.9;
    final double ovRight = 2.5/7.9;
    final double ovBottom = ovTop + fontHeight;
    private Rect overView;

    final double tdLeft = ovLeft;
    final double tdTop = 1.95/x;
    final double tdRight = 3.5/x;
    final double tdBottom = tdTop + fontHeight;
    private Rect timeDate;

    private ArrayList<Rect> headerRects;
    final double hTop = 2/x;
    final double hBottom = hTop + fontHeight;
    final double[] hLeftRight = {1.3/x,2.3/x,2.6/x,3.8/x,4.1/x,5.4/x,5.6/x,6.2/x,6.5/x,7/x}; //LEFT EXTENT FIRST RIGHT AFTER

    private ArrayList<Rect> summaryRects;
    final double sTop = 2.6/x;
    final double sBottom = sTop + fontHeight;
    final double[] sLeftRight = {0.2/x,2.3/x,2.6/x,3.8/x,4/x,5.4/x,5.6/x,6.2/x,6.4/x,7/x};

    private ArrayList<ArrayList<Rect>> intervalRects;
    final double iTop = 3.25/x;
    final double[] iLeftRight = sLeftRight;

    private Rect makeRect(double left, double top, double right, double bottom){
        return new Rect((int) Math.floor(screenWidth*(left-(charWidth/2))),(int) Math.floor(screenHeight*(top - (fontHeight /2))),
                (int) Math.ceil(screenWidth*(right+(charWidth / 2))),(int) Math.ceil(screenHeight*(bottom+(fontHeight / 2))));
    }

    private ArrayList<Rect> makeArrayRect(double top, double bottom, double[] leftRight){
        ArrayList<Rect> result = new ArrayList<>();
        for(int i = 0; i < (leftRight.length / 2);i++){
            result.add(makeRect(leftRight[i * 2], top, leftRight[i * 2 + 1], bottom));
        }
        return result;
    }


    public PM3plus(double w, double h){
        screenWidth = w;
        screenHeight = h;
        boundingRect = new Rect(0, 0,(int) w,(int) h);
        overView = makeRect(ovLeft,ovTop,ovRight,ovBottom);
        //timeDate = makeRect(tdLeft, tdTop, tdRight, tdBottom);
        headerRects = makeArrayRect(hTop, hBottom, hLeftRight);
        summaryRects = makeArrayRect(sTop, sBottom, sLeftRight);
        intervalRects = new ArrayList<>(8);
        for(int i = 0; i < 8; i++){
            intervalRects.add(makeArrayRect(iTop + ((fontHeight+ vGapSize)*i),iTop + (fontHeight*(i+1) + (vGapSize *i)),iLeftRight));
            Log.w("Doing your bidding", Integer.toString(intervalRects.size()));

        }

    }

    public ArrayList<Rect> notHR(ArrayList<Rect> rectArrayList){
        return (ArrayList<Rect>) rectArrayList.subList(0,3);
    }

    public Rect getBoundingRect() {
        return boundingRect;
    }

    public ArrayList<Rect> getHeaderRects() {
        return headerRects;
    }

    public ArrayList<Rect> getSummaryRects() {
        return summaryRects;
    }

    public Rect getOverView() {
        return overView;
    }

    public Rect getTimeDate() {
        return timeDate;
    }

    public ArrayList<ArrayList<Rect>> getIntervalRects() {
        return intervalRects;
    }

    public Rect timeHeader(){
        return headerRects.get(0);
    }

    public Rect distanceHeader(){
        return headerRects.get(1);
    }

    public Rect col3Header(){
        return headerRects.get(2);
    }

    public Rect spmHeader(){
        return headerRects.get(3);
    }

    public Rect hrHeader(){
        return headerRects.get(4);
    }

    public Rect timeSummary(){
        return summaryRects.get(0);
    }

    public Rect distanceSummary(){
        return summaryRects.get(1);
    }

    public Rect col3Summary(){
        return summaryRects.get(2);
    }

    public Rect spmSummary(){
        return summaryRects.get(3);
    }

    public Rect hrSummary(){
        return summaryRects.get(4);
    }

    public ArrayList<Rect> colRects(int i) {
        ArrayList<Rect> result = new ArrayList<>();

        result.add(summaryRects.get(i));
        for (ArrayList<Rect> ar : intervalRects) {
            result.add(ar.get(i));
        }
        return result;
    }

    public ArrayList<Rect> timeRects(){
        return colRects(0);
    }

    public ArrayList<Rect> distanceRects(){
        return colRects(1);
    }

    public ArrayList<Rect> col3Rects(){
        return colRects(2);
    }

    public ArrayList<Rect> spmRects(){
        return colRects(3);
    }

    public ArrayList<Rect> hrRects(){
        return colRects(4);
    }

}
