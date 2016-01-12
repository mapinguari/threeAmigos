package com.example.mapinguari.workoutclass.ExerciseObjectParsers;

import com.example.mapinguari.workoutclass.ExerciseObjectBuilders.IntervalChecker;

import java.util.Vector;

/**
 * Created by mapinguari on 1/12/16.
 */
public class IntervalParser extends PerformanceMeasureParser {

    private Double restTimeDouble;

    public IntervalParser(String timeString, String distanceString, String column3String, String spmString, Double restTimeData) {
        super(timeString, distanceString, column3String, spmString);
        this.restTimeDouble = restTimeData;
    }

    public IntervalParser(Vector<String> stringRow, Double restTimeData) {
        super(stringRow);
        this.restTimeDouble = restTimeData;
    }

    public IntervalChecker getIntervalChecker(){
        return new IntervalChecker(timeDouble,distanceDouble,splitDouble,spmInteger,restTimeDouble);
    }

    public IntervalChecker runForIntervalChecker(){
        getAllValues();
        return getIntervalChecker();
    }
}
