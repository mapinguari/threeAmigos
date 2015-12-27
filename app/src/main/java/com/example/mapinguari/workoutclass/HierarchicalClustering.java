package com.example.mapinguari.workoutclass;

import android.hardware.Camera;

import java.util.Set;

/**
 * Created by mapinguari on 12/27/15.
 */
public class HierarchicalClustering {

    private Set<Set<Point>> setSet;

    public HierarchicalClustering(Set<Point> s){

    }

    public void iterate(){

    }

    private double minDistance(Set<Point> s, Point p){
        double curMin = Double.MAX_VALUE;
        double curDis;
        for(Point q : s){
            try{
                curDis = euclideanDist(q,p);
                if(curDis < curMin){
                    curMin = curDis;
                }
            }catch(DimException e){
                e.getMessage();
            }
        }
        return curMin;
    }

    private double minDistance(Set<Point> s1, Set<Point> s2){
        double curMin = Double.MAX_VALUE;
        double curDis;
        for(Point p:s2){
            curDis = minDistance(s1,p);
            if(curDis < curMin){
                curMin = curDis;
            }
        }
        return curMin;
    }

    private double euclideanDist(Point p, Point q) throws DimException{
        double sum = 0;
        if(p.dim() == q.dim()) {
            for (int i = 0; i < p.dim(); i++) {
                sum += Math.pow(p.coord(i) - q.coord(i), 2);
            }
            return Math.sqrt(sum);
        }
        throw (new DimException());
    }

    private class Point {

        private double[] coordinates;

        private Point(double[] coords){
            this.coordinates = coords;
        }

        public double coord(int n){
            return coordinates[n];
        }

        public int dim(){
            return coordinates.length;
        }
    }

    private class DimException extends Exception {
    }
}
