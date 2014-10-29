package com.xiaomi.mitv.soundbarapp.fragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mitv.soundbarapp.TypefaceManager;
import com.xiaomi.mitv.utils.AsyncTaskRunner;
import org.w3c.dom.Text;

/**
 * Created by chenxuetong on 8/26/14.
 */
public abstract class BaseFragment extends Fragment {
    protected View mRootView;
    protected boolean mIsDestroyed = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View root = getView();
        if( root instanceof ViewGroup){
            TypefaceManager.updateTextFace(getActivity(), (ViewGroup) root);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String tag = getTag();
        if(TextUtils.isEmpty(tag)) tag = BaseFragment.class.getSimpleName();
        MobclickAgent.onPageStart(tag);
    }

    @Override
    public void onPause() {
        super.onPause();
        String tag = getTag();
        if(TextUtils.isEmpty(tag)) tag = BaseFragment.class.getSimpleName();
        MobclickAgent.onPageStart(tag);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsDestroyed = true;
    }

    protected void goBack(){
        getActivity().onBackPressed();
    }

    public boolean onBackPressed(){
        return false;
    }

    protected void updateUi(Runnable r){
        Activity a = getActivity();
        if(a!=null && !mIsDestroyed) a.runOnUiThread(r);
    }

    protected void runTask(Runnable r){
        AsyncTaskRunner.run(r);
    }

    protected <T> T findViewbyId(int rid){
        return (T)mRootView.findViewById(rid);
    }
}
