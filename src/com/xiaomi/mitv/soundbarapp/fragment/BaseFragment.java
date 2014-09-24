package com.xiaomi.mitv.soundbarapp.fragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xiaomi.mitv.soundbarapp.TypefaceManager;
import com.xiaomi.mitv.utils.AsyncTaskRunner;

/**
 * Created by chenxuetong on 8/26/14.
 */
public class BaseFragment extends Fragment {
    protected boolean mIsDestoryed;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View root = getView();
        if( root instanceof ViewGroup){
            TypefaceManager.updateTextFace(getActivity(), (ViewGroup) root);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsDestoryed = true;
    }

    protected void goBack(){
        getActivity().onBackPressed();
    }

    public boolean onBackPressed(){
        return false;
    }

    protected void updateUi(Runnable r){
        Activity a = getActivity();
        if(a!=null && !mIsDestoryed) a.runOnUiThread(r);
    }

    protected void runTask(Runnable r){
        AsyncTaskRunner.run(r);
    }

    protected <T> T findViewbyId(int rid){
        return (T)getActivity().findViewById(rid);
    }
}
