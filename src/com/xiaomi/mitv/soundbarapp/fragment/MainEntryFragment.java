package com.xiaomi.mitv.soundbarapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.WrapperActivity;
import com.xiaomi.mitv.soundbarapp.player.PlayerFragment;
import com.xiaomi.mitv.soundbarapp.upgrade.UpgradeFragment;

/**
 * Created by chenxuetong on 9/28/14.
 */
public class MainEntryFragment extends BaseFragment implements View.OnClickListener {
    public static final String UPGRADE_TAG = "upgarde";
    public static final String PLAYER_TAG = "player";

    private View mEQEntry;
    private View mSettings;
    private View mDiagnosis;
    protected boolean mBarSourceReady;
    private boolean mSettingEnabled = true;
    private boolean mEqEnabled = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_main_entries_with_player, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSettings = findViewbyId(R.id.main_entry_settings);
        mSettings.setOnClickListener(this);
        mEQEntry = findViewbyId(R.id.main_entry_eq);
        mEQEntry.setOnClickListener(this);
        mDiagnosis = findViewbyId(R.id.main_entry_diagnose);
        mDiagnosis.setOnClickListener(this);
        mEQEntry.setEnabled(mEqEnabled);
        mSettings.setEnabled(mSettingEnabled);

        Fragment f = getFragmentManager().findFragmentByTag(UPGRADE_TAG);
        if(f==null) {
            Fragment upgrade = new UpgradeFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_entry_upgrade, upgrade, UPGRADE_TAG)
                    .commit();
        }

        f = getFragmentManager().findFragmentByTag(PLAYER_TAG);
        if(f==null){
            PlayerFragment pf = new PlayerFragment();
            FragmentActivity activity = getActivity();
            if(activity instanceof PlayerFragment.OnPlayerStateListener){
                pf.setStateListener((PlayerFragment.OnPlayerStateListener)activity);
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.main_player_container, pf, PLAYER_TAG)
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_entry_upgrade:
                break;
            case R.id.main_entry_settings:
                showSettings();
                break;
            case R.id.main_entry_eq:
                showEq();
                break;
            case R.id.main_entry_diagnose:
                WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_DIAGNOSIS);
                break;
        }
    }

    public void setSourceReady(boolean ready){
        mBarSourceReady = ready;
    }

    public void showSettings(){
        if(mSettings.isEnabled()) {
            WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_SETTINGS, mBarSourceReady);
        }
    }

    public void showEq(){
        if(mEQEntry.isEnabled()) {
            if (mBarSourceReady) {
                WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_EQ);
            } else {
                Toast.makeText(getActivity(), "请在播放时调节音效！", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void enableSettings(boolean enabled) {
        mSettingEnabled = enabled;
        if(mSettings != null) {
            mSettings.setEnabled(enabled);
        }
    }

    public void enableEq(boolean enabled) {
        mEqEnabled = enabled;
        if(mEQEntry != null) {
            mEQEntry.setEnabled(enabled);
        }
    }
}
