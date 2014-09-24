package com.xiaomi.mitv.soundbarapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by chenxuetong on 9/10/14.
 */
public class TypefaceManager {
    private static Typeface mType;

    public static void updateTextFace(Context context, ViewGroup root){
        if(mType==null) init(context);
        setFontStyle(root, mType);
    }

    private static void init(Context context){
//        mType = Typeface.createFromAsset(context.getAssets(),"fonts/lanting.TTF");
    }

    private static void setFontStyle(ViewGroup root, Typeface type){
//        if(root == null) return;
//        for(int i=0; i<root.getChildCount(); i++){
//            View v = root.getChildAt(i);
//            if(v instanceof TextView){
//                ((TextView)v).setTypeface(type);
//            }else if(v instanceof ViewGroup){
//                setFontStyle((ViewGroup)v, type);
//            }
//        }
    }
}
