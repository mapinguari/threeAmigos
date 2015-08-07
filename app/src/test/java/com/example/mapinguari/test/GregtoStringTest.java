package com.example.mapinguari.test;

import com.example.mapinguari.workoutclass.exerciseObjects.GregtoString;

import junit.framework.TestCase;

import java.util.GregorianCalendar;

/**
 * Created by mapinguari on 8/1/15.
 */
public class GregtoStringTest extends TestCase {

    GregorianCalendar gc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gc = new GregorianCalendar();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetDateTime() throws Exception {
        String str1 = GregtoString.getDateTime(gc);
        String str2 = GregtoString.getDateTime(GregtoString.getGregCal(str1));
        assertEquals(str1, str2);
    }

    public void testSomething() throws Exception{
        assertEquals(1,1);
    }

}