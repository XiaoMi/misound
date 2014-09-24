package com.xiaomi.mitv.soundbarapp.eq;

/**
 * Created by chenxuetong on 8/28/14.
 */
public class EQStyle{
    public static final int[] BANDS = new int[]{1,2,3,4,5};
    private int[] gains = new int[5];

    public EQStyle(int g60, int g200, int g800, int g3k, int g10k){
        gains[0] = g60 * 60;
        gains[1] = g200 * 60;
        gains[2] = g800 * 60;
        gains[3] = g3k * 60;
        gains[4] = g10k * 60;
    }

    public EQStyle(){}

    public void setGain(int band, int gain){
        if(band<1 || band>5) return;
        gains[band-1] = gain;
    }

    public int getGain(int band){
        if(band<1 || band>5) return 0;
        return gains[band-1];
    }

    @Override
    public boolean equals(Object o) {
        if(o!=null && o instanceof EQStyle){
            EQStyle style = (EQStyle)o;
            return gains[0]==style.gains[0] &&
                    gains[1]==style.gains[1] &&
                    gains[2]==style.gains[2] &&
                    gains[3]==style.gains[3] &&
                    gains[4]==style.gains[4];
        }
        return false;
    }
}
