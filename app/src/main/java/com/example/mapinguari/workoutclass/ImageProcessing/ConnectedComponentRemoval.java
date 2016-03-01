package com.example.mapinguari.workoutclass.ImageProcessing;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mapinguari on 2/18/16.
 * This is primarily so as to remove very small, not very higly connected regions which are very
 * likely to be noise
 */
public class ConnectedComponentRemoval {

    //JUST NEED TO WRITE CONDITIONS FOR WHICH TO REMOVE COMPONENTS!
    int[] image;
    int imageWidth;
    int imageHeight;
    int black;
    int white;
    int sizeFail;
    int connFail;
    ArrayList<Component> allComponents;


    public ConnectedComponentRemoval(int[] image, int imageWidth, int imageHeight, int black, int white) {
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.black = black;
        this.white = white;
    }

    public ConnectedComponentRemoval(int[] image, int imageWidth, int imageHeight, int black, int white, int sizeFail, int connFail) {
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.black = black;
        this.white = white;
        this.sizeFail = sizeFail;
        this.connFail = connFail;
    }

    //Do some clever stuff here to set failure conditions
    private void cleverFailSetting(){

    }


    public void doEverything(){
        allComponents();
        cleanAllFailingComponents();
    }


    private int xcoord(int i) {
        return i % imageWidth;
    }

    private int ycoord(int i) {
        return i / imageWidth;
    }

    private int imageInd(int x, int y) {
        return x + y * imageWidth;
    }

    private boolean inImage(int x, int y) {
        return 0 <= x && x < imageWidth && 0 <= y && y < imageHeight;
    }

    private int[] fourWAdj(int i) {
        int x = xcoord(i);
        int y = ycoord(i);
        int nInd = inImage(x, y - 1) && image[imageInd(x,y-1)] == black? imageInd(x, y - 1) : -1;
        int eInd = inImage(x + 1, y) && image[imageInd(x+1,y)] == black? imageInd(x + 1, y) : -1;
        int wInd = inImage(x - 1, y) && image[imageInd(x-1,y)] == black? imageInd(x - 1, y) : -1;
        int sInd = inImage(x, y + 1) && image[imageInd(x,y+1)] == black? imageInd(x, y + 1) : -1;
        int[] protoAdj = {nInd, eInd, sInd, wInd};
        int size = 0;
        for (int j = 0; j < protoAdj.length; j++) {
            if (protoAdj[j] >= 0)
                size++;
        }
        int[] result = new int[size];
        int current = 0;
        for (int j = 0; j < protoAdj.length; j++) {
            if (protoAdj[j] >= 0) {
                result[current] = protoAdj[j];
                current++;
            }

        }
        return result;

    }

    private Collection<Integer> convert(int[] array){
        ArrayList<Integer> result = new ArrayList<>(array.length);
        for(int i = 0; i< array.length;i++){
            result.add(array[i]);
        }
        return result;
    }

    private int quickAdjCheck(int[] image,int[] toCheck){
        int count = 0;
        for(int i = 0;i < toCheck.length;i++){
            if(image[toCheck[i]] == black)
                count++;
        }
        return count;
    }

    private void cleanComponent(Component component){

        for(Integer i : component){
            image[i] = white;
        }

    }

    private void cleanAllFailingComponents(){
        for(Component c : allComponents){
            if(c.size() < sizeFail || c.maxAdj < connFail){
                cleanComponent(c);
            }
        }
    }

    private ArrayList<Component> allComponents(){

        HashMap<Integer,Boolean> visited = new HashMap<>(image.length);
        for(int i = 0;i < image.length;i++)
            visited.put(i,false);
        Iterator<Integer> imageIter = visited.keySet().iterator();
        ArrayList<Component> result = new ArrayList<>();

        int cGroupNum = 0;
        int cInd;
        Component cComponent;

        while(imageIter.hasNext()){
            cInd = imageIter.next();
            Log.w("what is going on?", Integer.toString(cInd));
            if(!visited.get(cInd)) {
                if (image[cInd] == black) {
                    cComponent = connectedComponent(visited, cInd, cGroupNum);
                    result.add(cComponent);
                    cGroupNum++;
                } else {
                    visited.put(cInd, true);
                }
            }
        }
        allComponents = result;
        return result;

    }


    private Component connectedComponent(HashMap<Integer,Boolean> visited, int seed, int grNum){
        Set<Integer> toVisit = new HashSet<>();
        Component component = new Component(grNum);
        Iterator<Integer> cIterator;
        Integer cIndex;
        int[] cAdjs;

        toVisit.addAll(convert(fourWAdj(seed)));
        visited.put(seed,true);
        while(!toVisit.isEmpty()){
            cIterator = toVisit.iterator();
            cIndex = cIterator.next();
            Log.w("innner Index", Integer.toString(cIndex));
            cIterator.remove();
            cAdjs = fourWAdj(cIndex);

            component.add(cIndex);
            Log.w("Component size", Integer.toString(component.size()));
            component.updateAdj(quickAdjCheck(image, cAdjs));

            visited.put(cIndex, true);
            for(int j = 0;j < cAdjs.length;j++){
                if(!visited.get(cAdjs[j])){
                    toVisit.add(cAdjs[j]);
                }
            }
        }
        return component;

    }

    private class Component implements Collection<Integer>{

        ArrayList<Integer> indicies;
        int maxAdj;
        int compNumber;

        public Component(int compNumber){
            indicies = new ArrayList<>();
            maxAdj = 0;
            this.compNumber = compNumber;
        }

        public void updateAdj(int newAdj){
            if(newAdj > maxAdj)
                maxAdj = newAdj;
        }

        @Override
        public boolean remove(Object object) {
            return indicies.remove(object);
        }

        @Override
        public boolean isEmpty() {
            return indicies.isEmpty();
        }

        @Override
        public boolean add(Integer object) {
            return indicies.add(object);
        }

        @Override
        public int size() {
            return indicies.size();
        }

        @Override
        public boolean addAll(Collection<? extends Integer> collection) {
            return indicies.addAll(collection);
        }

        @Override
        public void clear() {
            indicies.clear();
        }

        @Override
        public boolean contains(Object object) {
            return indicies.contains(object);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return indicies.containsAll(collection);
        }

        @NonNull
        @Override
        public Iterator<Integer> iterator() {
            return indicies.iterator();
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return indicies.removeAll(collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return indicies.retainAll(collection);
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return indicies.toArray();
        }

        @NonNull
        @Override
        public <T> T[] toArray(T[] array) {
            return indicies.toArray(array);
        }
    }
}
