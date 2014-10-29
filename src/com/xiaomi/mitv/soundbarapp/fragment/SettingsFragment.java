package com.xiaomi.mitv.soundbarapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xiaomi.mitv.idata.util.DeviceHelper;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.util.ConfirmActivityDlg;
import com.xiaomi.mitv.utils.Log;
import com.xiaomi.mitv.widget.MiSwitch;

/**
 * Created by ThinkPad User on 14-7-5.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public static final int WOOFER_VOL_MAX = 31;
    private static final String TAG = "Settings";

    private View mLoading;
    private SeekBar mWooferVolume;
    private TextView mWooferVolumeText;
    private MiSwitch mToneMuteView;
    private MiSwitch mSafeModeView;
    private WooferVolumeSet mWooferVolController;
    private boolean mHaveSource = true;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWooferVolController = new WooferVolumeSet();
        buildView();
    }

    @Override
    public void onResume() {
        super.onResume();
        runTask(new Runnable() {
            @Override
            public void run() {
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.VISIBLE);
                    }
                });
                initWoofer();
                initMutetone();
                initSafetyMode();
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void setHaveSource(boolean haveSource){
        mHaveSource = haveSource;
    }

    void buildView(){
        ((View)findViewbyId(R.id.actionbar)).setOnClickListener(this);
        ((TextView)findViewbyId(R.id.action_bar_text)).setText(R.string.main_entry_settings);
        mLoading = findViewbyId(R.id.loading);
        mWooferVolumeText = findViewbyId(R.id.woofer_seek_text);
        mWooferVolume = findViewbyId(R.id.woofer_seek);
        mWooferVolume.setMax(WOOFER_VOL_MAX);
        mWooferVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mWooferVolumeText.setText(100*progress/31 +"%");
                if(fromUser) mWooferVolController.applyChange();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mToneMuteView = findViewbyId(R.id.woofer_connect_tone);
        mToneMuteView.setOnCheckChangedListener(new MiSwitch.OnCheckChangedListener() {
            @Override
            public void onChanged(boolean checked) {
                muteWooferTone(!checked);
            }
        });
        ((View)findViewbyId(R.id.master_rest)).setOnClickListener(this);
        mSafeModeView = findViewbyId(R.id.safe_mode);
        mSafeModeView.setOnCheckChangedListener(new MiSwitch.OnCheckChangedListener() {
            @Override
            public void onChanged(boolean checked) {
                changeSafeMode(checked);
            }
        });

        String ver = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuCurrentVersion);
        String addr = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.addressName);
        String name = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.Mibar_identify_name);
        ((TextView)findViewbyId(R.id.device_name)).setText(name);
        ((TextView)findViewbyId(R.id.setting_device_version)).setText("固件v"+ver +"  硬件地址"+addr);

        String pre = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuPreVersion);
        if(pre == null){
            ((TextView)findViewbyId(R.id.upgrade_log)).setText("未检查到升级信息");
        }else{
            ((TextView)findViewbyId(R.id.upgrade_log)).setText("从"+pre+"升级到"+ver);
        }

        TextView key = (TextView)getActivity().findViewById(R.id.app_key);
        try {
            key.setText(DeviceHelper.getDeviceID(getActivity()));
        }catch (Exception ne){}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.master_rest:
                showConfirm();
                break;
            case R.id.actionbar:
                goBack();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.woofer_connect_tone:
                changeWooferConnectTone(isChecked);
                break;

            case R.id.safe_mode:
                changeSafeMode(isChecked);
                break;
        }
    }

    private void changeWooferConnectTone(final boolean checked) {
        runTask(new Runnable() {
            @Override
            public void run() {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    mibar.setMuteToneVolume(!checked);
                }catch (GaiaException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeSafeMode(final boolean checked) {
        runTask(new Runnable() {
            @Override
            public void run() {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    mibar.setSafetyMod(checked);
                }catch (GaiaException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void muteWooferTone(final boolean mute) {
        runTask(new Runnable() {
            @Override
            public void run() {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    mibar.setMuteToneVolume(mute);
                }catch (GaiaException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void initWoofer(){
        IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
        try {
            if (mHaveSource && mibar.isSubWooferConnected(false)) {
                final int wooferVol = mibar.getWooferVolume();
                mWooferVolController.setBaseVol(wooferVol);
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        mWooferVolume.setProgress(wooferVol);
                        mWooferVolumeText.setText(100*wooferVol/31 +"%");
                    }
                });
            }else{
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        mWooferVolume.setEnabled(false);
                        mWooferVolume.setProgressDrawable(getResources().getDrawable(R.drawable.seeker_woofer_vol_normal));
                        mWooferVolume.setThumb(getResources().getDrawable(R.drawable.setting_botton_subwoofer_volume_selected));
                        TextView info = findViewbyId(R.id.setting_woofer_vol_info);
                        info.setVisibility(View.VISIBLE);
                        info.setText("在播放电视或者蓝牙设备连接时，才可以调节音量");
                    }
                });
            }
        }catch (GaiaException e){
            e.printStackTrace();
        }
    }

    private void initMutetone(){
        IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
        try {
            final boolean mute = mibar.getMuteToneVolume();
            updateUi(new Runnable() {
                @Override
                public void run() {
                    mToneMuteView.setChecked(!mute);
                }
            });
        }catch (GaiaException e){
            e.printStackTrace();
        }
    }

    private void initSafetyMode(){
        IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
        try {
            final boolean safetyMode = mibar.getSafetyMode();
            updateUi(new Runnable() {
                @Override
                public void run() {
                    mSafeModeView.setChecked(safetyMode);
                }
            });
        }catch (GaiaException e){
            e.printStackTrace();
        }
    }

    private void showConfirm() {
        ConfirmActivityDlg.show(getActivity(), R.string.setting_reset_alert, new ConfirmActivityDlg.onAction() {
            @Override
            public void onConfirmed(boolean ret) {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    mibar.masterReset();
                } catch (GaiaException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class WooferVolumeSet implements Runnable {
        private int mCurrentVol;
        private Handler mHandler;
        private boolean mRunning = false;

        public WooferVolumeSet(){
            mCurrentVol = 0;
            if(mCurrentVol>WOOFER_VOL_MAX) mCurrentVol = WOOFER_VOL_MAX;
            mHandler = new Handler(getActivity().getMainLooper());
        }

        public void setBaseVol(int base){
            mCurrentVol = base;
        }

        public void applyChange(){
            if(!mRunning) {
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 200);
            }
        }

        @Override
        public void run() {
            mRunning = true;
            runTask(new Runnable() {
                @Override
                public void run() {
                    IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                    try {
                        while (mCurrentVol <= WOOFER_VOL_MAX) {
                            int vol = mWooferVolume.getProgress();
                            Log.logD(TAG, "change woofer vol from: " + mCurrentVol + " to " + vol);
                            if (vol < mCurrentVol) {
                                if (mibar.setWooferVolume(IMiSoundDevice.WOOFER_VOL_DECRESE)) {
                                    --mCurrentVol;
                                }
                            } else if (vol > mCurrentVol) {
                                if (mibar.setWooferVolume(IMiSoundDevice.WOOFER_VOL_INCRESE)) {
                                    ++mCurrentVol;
                                }
                            } else break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mRunning = false;
                    }
                }
            });
        }
    }
}
