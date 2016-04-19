package com.example.mapinguari.workoutclass.cameraClasses;

/**
 * Created by mapinguari on 4/13/16.
 */
public interface ScreenInterfaceFinder {

    /* This function should return the int which corresponds to the lcd screen, case interface

     */
    int findInterfacePoint(byte[] colOrRow);

    boolean lengthOk(int length);
}
