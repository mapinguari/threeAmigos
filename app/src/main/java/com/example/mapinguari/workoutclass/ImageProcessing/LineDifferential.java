package com.example.mapinguari.workoutclass.ImageProcessing;




import com.example.mapinguari.workoutclass.mObjects.LineSeg;
import com.example.mapinguari.workoutclass.mObjects.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mapinguari on 2/23/16.
 */
public class LineDifferential {

    int[] image;
    int imageWidth;
    int imageHeight;

    Map<Point<Double>,Integer> pointChangeMap;

    public LineDifferential(int[] image, int imageWidth, int imageHeight) {
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }


    public Map<Point<Double>,Integer> makePointChangeMap(ArrayList<Point<Double>> pointList){

        Map<Point<Double>,Integer> result = new HashMap<>();

        Integer priV,posV;
        Integer valueChange;
        for(int i = 1; i < pointList.size() - 1;i++){
            priV = image[pointToIndex(mPointBeforeGrid(pointList,i))];
            posV = image[pointToIndex(mPointAfterGrid(pointList, i))];
            valueChange = posV - priV;
            result.put(pointList.get(i),valueChange);
        }
        this.pointChangeMap = result;
        return result;
    }


    public int pointToIndex(Point<Integer> p){
        return (p.y*imageWidth + p.x);
    }


    public Point<Integer> mPointBeforeGrid(ArrayList<Point<Double>> pointList, int pointIndex){
        Point<Integer> result;
        if(pointIndex == 0){
            result = null;
        } else {
            Point<Double> prior = pointList.get(pointIndex - 1);
            LineSeg lineSeg = new LineSeg(prior,pointList.get(pointIndex));
            result = lineSeg.midpoint().snapToGrid();
        }
        return result;
    }

    public Point<Integer> mPointAfterGrid(ArrayList<Point<Double>> pointList, int pointIndex){
        Point<Integer> result;
        if(pointIndex == pointList.size()-1){
            result = null;
        } else {
            Point<Double> posterior = pointList.get(pointIndex + 1);
            LineSeg lineSeg = new LineSeg(pointList.get(pointIndex),posterior);
            result = lineSeg.midpoint().snapToGrid();
        }
        return result;
    }


    public ArrayList<Point<Double>> gridPointsInOrder(Point<Integer> p, Point<Integer> q){
        ArrayList<Point<Double>> result = new ArrayList();
        LineSeg lineSeg = new LineSeg(p,q);

        ArrayList<Point> xPoints = new ArrayList<>();
        int[] xints = lofInt(p.x,q.x);
        ArrayList<Point> yPoints = new ArrayList<>();
        int[] yints = lofInt(p.y,q.y);

        int current;
        for(int i = 1;i<xints.length;i++){
            current = xints[i];
            xPoints.add(new Point(Double.valueOf(current), lineSeg.getYFromX(current)));
        }

        for(int i = 1;i<yints.length;i++){
            current = yints[i];
            yPoints.add(new Point(Double.valueOf(current), lineSeg.getXFromY(current)));
        }

        int xp=0,yp=0;
        double xd,yd;

        result.add(new Point(p.x.doubleValue(),p.y.doubleValue()));

        while(xp< xPoints.size() - 1 && yp< yPoints.size() - 1){
            xd = p.dist(xPoints.get(xp));
            yd = p.dist(yPoints.get(yp));
            if(xd < yd){
                result.add(xPoints.get(xp));
                xp++;
            } else if(xd > yd){
                result.add(yPoints.get(yp));
                yp++;
            } else {
                result.add(xPoints.get(xp));
                xp++;
                yp++;
            }

        }

        int remp;
        ArrayList<Point> remPoints;

        if(xp == xPoints.size() - 1 || xp == 0){
            remp = yp;
            remPoints = yPoints;
        } else {
            remp = xp;
            remPoints = xPoints;
        }

        while(remp < remPoints.size()){
            result.add(remPoints.get(remp));
            remp++;
        }

        return result;
    }

    private int[] lofInt(int a, int b){
        int[] result;
        if(a==b){
            result = new int[1];
            result[0] = a;
        }else {
            result = new int[Math.abs(b - a) + 1];
            if(a < b){
                for(int i = 0; i <= (b-a);i++){
                    result[i] = a + i;
                }
            } else {
                for(int i = 0; i <= (b-a);i--){
                    result[i] = a + i;
                }
            }
        }
        return result;
    }

}