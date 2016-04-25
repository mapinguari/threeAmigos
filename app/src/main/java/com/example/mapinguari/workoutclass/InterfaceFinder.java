package com.example.mapinguari.workoutclass;

import com.example.mapinguari.workoutclass.cameraClasses.ScreenInterfaceFinder;
import java.util.Arrays;

public class InterfaceFinder implements ScreenInterfaceFinder {
    private static final float[] diff={-0.5f,0f,0.5f};
    private float[] op;
    private int filterLength;

    InterfaceFinder() {
        filterLength=5;
        op=get1DGaussian(filterLength);
    }


    public int findInterfacePoint(byte[] colOrRow){
        float[] fdata=new float[colOrRow.length];

        for(int i=0;i<colOrRow.length;i++)
                fdata[i]=(float)colOrRow[i];

        float[] gData=applyFilter(fdata,op);

        float[] diffData=applyFilter(gData,diff);

        int edge=findMax(Arrays.copyOfRange(diffData, filterLength / 2 + 1,
                diffData.length - (filterLength / 2 + 1)));

        return edge+filterLength/2+1;
    }

    public boolean lengthOk(int length){
        return length>(filterLength+2);
    }

    private static float[] get1DGaussian(int length){

        float[] op = new float[length];
        float sigma=1.4f;

        float div1=-1/(2*sigma*sigma);
        float k=((float)length-1f)/2f;
        float total=0;

        for(int j=0;j<length;j++){
            op[j]=(float)Math.exp(div1*((j-k)*(j-k)));
            total+=op[j];
        }

        for(int j=0;j<length;j++){
            op[j]/=total;
        }
        return op;
    }

    private static float[] applyFilter(float[] array,float[] op){
        float[] ret=new float[array.length];

        for(int i=0;i<array.length-(op.length);i++){
            float total=0;
            for (int j=0;j<op.length;j++){
                total+=array[i+j]*op[i];
            }
            ret[i]=Math.abs(total);
        }
        return ret;
    }


    private static int findMax(float[] array){
        int max=0;
        for(int i=1;i<array.length;i++)
            max=(array[i]>array[max])?i:max;

        return max;
    }


}
