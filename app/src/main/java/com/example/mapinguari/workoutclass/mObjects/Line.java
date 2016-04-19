package com.example.mapinguari.workoutclass.mObjects;

/**
 * Created by mapinguari on 4/18/16.
 */
public class Line {

    double gradient;
    double constant;

    public Line(double gradient, double constant) {
        this.gradient = gradient;
        this.constant = constant;
    }

    public Point intersection(Line l2){
        Point<Integer> result;
        if(gradient - l2.gradient != 0) {

            double x = (constant - l2.constant) / (gradient - l2.gradient);
            double y = gradient * x + constant;
            result = new Point(x,y);
        } else {
            result = null;
        }
        return result;

    }
}
