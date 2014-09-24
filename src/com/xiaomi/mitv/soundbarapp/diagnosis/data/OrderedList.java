package com.xiaomi.mitv.soundbarapp.diagnosis.data;

import java.util.ArrayList;

/**
 * Created by chenxuetong on 7/10/14.
 */
public class OrderedList<T extends Comparable<T>> extends ArrayList<T> {
    @Override
    public boolean add(T element){
        add(findPos(element), element);
        return true;
    }

    private int findPos(T element){
        int pos = size();
        for(int i=0; i<size(); i++){
            if(get(i).compareTo(element)>0){
                pos = i;
            }
        }
        return pos;
    }
}
