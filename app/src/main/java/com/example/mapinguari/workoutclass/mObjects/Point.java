package com.example.mapinguari.workoutclass.mObjects;

/**
 * Created by mapinguari on 2/28/16.
 */
public class Point<N extends Number> {

    public N x;
    public N y;

    public Point(N x, N y) {
        this.x = x;
        this.y = y;
    }

    public double dist(Point q){
        return Math.sqrt(Math.pow(x.doubleValue() - q.x.doubleValue(),2) + Math.pow(y.doubleValue()-q.y.doubleValue(),2));
    }

    public Point<Integer> snapToGrid(){
        return new Point((int) Math.floor(x.doubleValue()),(int) Math.floor(y.doubleValue()));
    }


}
