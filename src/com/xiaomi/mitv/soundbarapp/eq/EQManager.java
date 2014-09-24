package com.xiaomi.mitv.soundbarapp.eq;

import android.content.Context;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A;
import com.xiaomi.mitv.soundbarapp.R;

import java.util.*;

/**
 * Created by chenxuetong on 8/28/14.
 */
public class EQManager {
    public static final int EQ_COSTUM = IMiSoundDevice.EQ_COSTUM;
    public static final int EQ_STANDARD = IMiSoundDevice.EQ_STANDARD;
    public static final int EQ_ROCK = IMiSoundDevice.EQ_ROCK;
    public static final int EQ_CLEAR = IMiSoundDevice.EQ_CLEAR;
    public static final int EQ_SOFT = IMiSoundDevice.EQ_LIGHT;

    public EQStyle findEqById(int id) {
        if(mStyles.containsKey(id)) {
            return mStyles.get(id);
        }
        return null;
    }

    public int idOfStyle(EQStyle style){
        if (style==null) return -1;
        Set<Integer> ids = mStyles.keySet();
        for(Integer id : ids){
            EQStyle item = mStyles.get(id);
            if(item.equals(style)) return id;
        }
        return R.id.eq_style_custom;
    }

    public EQStyle readSoundBarStyle(Context context){
        IMiSoundDevice mibar = new DefaultMisoundDevice(context);
        try {
            int current_eq = mibar.getEQControl();
            if(current_eq == EQ_COSTUM) {
                EQStyle style = new EQStyle();
                for (int band : EQStyle.BANDS) {
                    UserEQ0x21A eq = new UserEQ0x21A(band, -1);
                    eq = mibar.getUserEQControl(eq);
                    if (eq != null) {
                        style.setGain(band, eq.mValue);
                    }
                }
                return style;
            }else{
                if(current_eq!=EQ_STANDARD) mibar.setEQControl(EQ_STANDARD);
                return mStyles.get(R.id.eq_style_standard);
            }
        }catch (GaiaException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean setUserEQ(Context context, int id){
        final EQStyle style = findEqById(id);
        if(style==null) return false;

        IMiSoundDevice mibar = new DefaultMisoundDevice(context);
        try {
            int current_Eq = mibar.getEQControl();

            boolean ok = true;
            if(id==R.id.eq_style_standard && current_Eq!=EQ_STANDARD){
                ok = mibar.setEQControl(EQ_STANDARD);
            }else {
                if(current_Eq!=EQ_COSTUM) ok=mibar.setEQControl(EQ_COSTUM);
                for (int band : EQStyle.BANDS) {
                    UserEQ0x21A eq = new UserEQ0x21A(band, style.getGain(band));
                    ok = ok && mibar.setUserEQControl(eq);
                }
            }
            return ok;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public EQStyleResource getResourceById(int id){
        if(!mResources.containsKey(id)) return null;
        return mResources.get(id);
    }

    private HashMap<Integer, EQStyleResource> mResources = new HashMap<Integer, EQStyleResource>();
    {
        mResources.put(R.id.eq_style_standard, new EQStyleResource(R.string.eq_standar,R.drawable.home_page_bg_xiaomisound_standard, R.drawable.home_page_button_xiaomisound_standard));
        mResources.put(R.id.eq_style_class, new EQStyleResource(R.string.eq_class,R.drawable.home_page_bg_xiaomisound_class, R.drawable.home_page_button_xiaomisound_class));
        mResources.put(R.id.eq_style_custom, new EQStyleResource(R.string.eq_custom,R.drawable.home_page_bg_xiaomisound_custom, R.drawable.home_page_button_xiaomisound_custom));
        mResources.put(R.id.eq_style_jazz, new EQStyleResource(R.string.eq_jazz,R.drawable.home_page_bg_xiaomisound_jazz, R.drawable.home_page_button_xiaomisound_jazz));
        mResources.put(R.id.eq_style_movie, new EQStyleResource(R.string.eq_movie,R.drawable.home_page_bg_xiaomisound_movie, R.drawable.home_page_button_xiaomisound_movie));
        mResources.put(R.id.eq_style_pop, new EQStyleResource(R.string.eq_pop,R.drawable.home_page_bg_xiaomisound_pop, R.drawable.home_page_button_xiaomisound_pop));
        mResources.put(R.id.eq_style_rock, new EQStyleResource(R.string.eq_rock,R.drawable.home_page_bg_xiaomisound_rock, R.drawable.home_page_button_xiaomisound_rock));
        mResources.put(R.id.eq_style_tv, new EQStyleResource(R.string.eq_tv,R.drawable.home_page_bg_xiaomisound_tv, R.drawable.home_page_button_xiaomisound_tv));
    }

    private HashMap<Integer, EQStyle> mStyles = new HashMap<Integer, EQStyle>();
    {
        mStyles.put(R.id.eq_style_standard, new EQStyle(0,0,0,0,0));
        mStyles.put(R.id.eq_style_movie, new EQStyle(8,0,5,4,2));
        mStyles.put(R.id.eq_style_tv, new EQStyle(-5,-5,3,2,3));
        mStyles.put(R.id.eq_style_pop, new EQStyle(2,2,2,2,2));
        mStyles.put(R.id.eq_style_rock, new EQStyle(6,3,2,1,2));
        mStyles.put(R.id.eq_style_class, new EQStyle(-3,-2,0,-1,-4));
        mStyles.put(R.id.eq_style_jazz, new EQStyle(-2,3,3,4,-2));
    }

}
