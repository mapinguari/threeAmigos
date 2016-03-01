package com.example.mapinguari.workoutclass.mObjects;

/**
 * Created by mapinguari on 2/28/16.
 */
public class Line {

    private Point start;
    private Point finish;
    private double gradient;

    public Line(Point start, Point finish) {
        this.start = start;
        this.finish = finish;
        this.gradient = (finish.y.doubleValue() - start.y.doubleValue()) / (finish.x.doubleValue() - start.x.doubleValue());
    }

    public double getYFromX(double x){
        return start.y.doubleValue() + gradient*(x - start.x.doubleValue());
    }

    public double getXFromY(double y){
        return start.x.doubleValue() + (1/gradient)*(y-start.y.doubleValue());
    }

    public Point midpoint(){
        return new Point((start.x.doubleValue() + finish.x.doubleValue()) / 2,(start.y.doubleValue() + finish.y.doubleValue()) /2);
    }
}
