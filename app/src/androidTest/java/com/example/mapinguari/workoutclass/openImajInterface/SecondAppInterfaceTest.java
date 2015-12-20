package com.example.mapinguari.workoutclass.openImajInterface;

import junit.framework.TestCase;

/**
 * Created by mapinguari on 12/20/15.
 */
public class SecondAppInterfaceTest extends TestCase {

    public void testErgoScreenOutName() throws Exception {
        SecondAppInterface a = new SecondAppInterface(null);
        for(int i = 0; i <10; i++)
            System.out.print(a.ergoScreenOutName());
    }
}