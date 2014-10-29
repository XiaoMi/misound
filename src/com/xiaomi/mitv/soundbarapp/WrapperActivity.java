package com.xiaomi.mitv.soundbarapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mitv.idata.client.app.AppData;
import com.xiaomi.mitv.idata.util.Counter;
import com.xiaomi.mitv.soundbarapp.diagnosis.DiagnosisFragment;
import com.xiaomi.mitv.soundbarapp.eq.UserEQControlFragment;
import com.xiaomi.mitv.soundbarapp.faq.FaqFragment;
import com.xiaomi.mitv.soundbarapp.eq.EQSettingsFragment;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.soundbarapp.fragment.FeedbackFragment;
import com.xiaomi.mitv.soundbarapp.fragment.SettingsFragment;
import com.xiaomi.mitv.soundbarapp.player.PlayListFragment;

/**
 * Created by chenxuetong on 8/21/14.
 */
public class WrapperActivity extends FragmentActivity {
    public static final String FRAGMENT_UPGRADE = "upgrade";
    public static final String FRAGMENT_SETTINGS = "settings";
    public static final String FRAGMENT_EQ = "eq";
    public static final String FRAGMENT_FAQ = "faq";
    public static final String FRAGMENT_FEEDBACK = "feedback";
    public static final String FRAGMENT_DIAGNOSIS = "diagnosis";
    public static final String FRAGMENT_USER_EQ = "user_eq";
    public static final String FRAGMENT_PLAYLIST = "play_list";

    private BaseFragment mFragement;

    public static void go(Activity from, String fragment, boolean sourceReady){
        Intent i = new Intent(from, WrapperActivity.class);
        i.putExtra("fragment", fragment);
        i.putExtra("have_source", sourceReady);
        i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        from.startActivity(i);
    }

    public static void go(Activity from, String fragment){
        go(from,fragment,true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            String fragment = i.getStringExtra("fragment");
            if(FRAGMENT_SETTINGS.equals(fragment)){
                boolean sourceReaday = i.getBooleanExtra("have_source", true);
                onSettings(sourceReaday);
            }
            if(FRAGMENT_FAQ.equals(fragment)){
                onFaq();
            }
            if(FRAGMENT_FEEDBACK.equals(fragment)){
                onFeedback();
            }
            if(FRAGMENT_DIAGNOSIS.equals(fragment)){
                onExamine();
            }
            if(FRAGMENT_EQ.equals(fragment)){
                onEQ();
            }
            if(FRAGMENT_USER_EQ.equals(fragment)){
                onUserEQ();
            }
            if(FRAGMENT_PLAYLIST.equals(fragment)){
                onPlayList();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onResume(this);
    }

    private void onPlayList() {
        mFragement = PlayListFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_PLAYLIST)
                .commitAllowingStateLoss();
    }

    private void onUserEQ() {
        mFragement = UserEQControlFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_UPGRADE)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if(mFragement != null && mFragement.onBackPressed()){
            return;
        }
        finish();
    }

    private void onEQ() {
        mFragement = EQSettingsFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_EQ)
                .commitAllowingStateLoss();
        reportFeatureAccess(FRAGMENT_EQ);
    }

    public void onSettings(boolean sourceReday) {
        SettingsFragment sf = SettingsFragment.newInstance();
        sf.setHaveSource(sourceReday);
        mFragement = sf;
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_SETTINGS)
                .commitAllowingStateLoss();
        reportFeatureAccess(FRAGMENT_SETTINGS);
    }


    public void onFaq() {
        mFragement = FaqFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_FAQ)
                .commitAllowingStateLoss();
        reportFeatureAccess(FRAGMENT_FAQ);
    }

    public void onExamine() {
        DiagnosisFragment.OnListener listener = new DiagnosisFragment.OnListener() {
            @Override
            public void goBack() {
                finish();
            }

            @Override
            public void goFeedback() {
                getFragmentManager().popBackStack();//quit myself
                onFeedback();
            }

            @Override
            public void goFaq() {
                onFaq();
            }
        };

        mFragement = DiagnosisFragment.newInstance(listener);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_DIAGNOSIS)
                .commitAllowingStateLoss();
        reportFeatureAccess(FRAGMENT_DIAGNOSIS);
    }

    public void onFeedback() {
        mFragement = FeedbackFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragement, FRAGMENT_FEEDBACK)
                .commitAllowingStateLoss();
        reportFeatureAccess(FRAGMENT_FEEDBACK);
    }

    private void showAlert(String title, String msg, final boolean exit) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void reportFeatureAccess(String feature) {
        if (TextUtils.isEmpty(feature)) return;
        Counter.count(this, AppData.appID, AppData.appKey, feature, 1);
    }
}
