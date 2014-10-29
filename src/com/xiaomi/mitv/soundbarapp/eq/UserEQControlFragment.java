package com.xiaomi.mitv.soundbarapp.eq;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.utils.Log;
import com.xiaomi.mitv.widget.GainView;

/**
 * Created by chenxuetong on 7/28/14.
 */
public class UserEQControlFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {
    private static final int GAIN_OFFSET = 12;
    private static final String EQ_SETTING = "bar_eq_style_band";
    private ViewGroup mMainView;
    private SeekBar mBar1;
    private SeekBar mBar2;
    private SeekBar mBar3;
    private SeekBar mBar4;
    private SeekBar mBar5;
    private GainView mGainView;

    public static UserEQControlFragment newInstance() {
        UserEQControlFragment f = new UserEQControlFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (ViewGroup)inflater.inflate(R.layout.settings_user_eq, container, false);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TextView)findViewbyId(R.id.action_bar_text)).setText(R.string.custom_eq_setup);

        mBar1 = findViewbyId(R.id.eq_band1);
        mBar1.setOnSeekBarChangeListener(this);
        mBar2 = findViewbyId(R.id.eq_band2);
        mBar2.setOnSeekBarChangeListener(this);
        mBar3 = findViewbyId(R.id.eq_band3);
        mBar3.setOnSeekBarChangeListener(this);
        mBar4 = findViewbyId(R.id.eq_band4);
        mBar4.setOnSeekBarChangeListener(this);
        mBar5 = findViewbyId(R.id.eq_band5);
        mBar5.setOnSeekBarChangeListener(this);

        mGainView = findViewbyId(R.id.eq_gain_wave);

        ((View)findViewbyId(R.id.actionbar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        loadUserEQ();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.logD("seeker: onProgressChanged from user: " + fromUser);
        if(!fromUser) return;

        int value = progress- GAIN_OFFSET;
        switch (seekBar.getId()){
            case R.id.eq_band1: showValue(R.id.eq_value1, value); mGainView.updateGain(1, value);break;
            case R.id.eq_band2: showValue(R.id.eq_value2, value); mGainView.updateGain(2, value);break;
            case R.id.eq_band3: showValue(R.id.eq_value3, value); mGainView.updateGain(3, value);break;
            case R.id.eq_band4: showValue(R.id.eq_value4, value); mGainView.updateGain(4, value);break;
            case R.id.eq_band5: showValue(R.id.eq_value5, value); mGainView.updateGain(5, value);break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.logD("seeker: onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress();
        switch (seekBar.getId()){
            case R.id.eq_band1: setEq(1, value); break;
            case R.id.eq_band2: setEq(2, value); break;
            case R.id.eq_band3: setEq(3, value); break;
            case R.id.eq_band4: setEq(4, value); break;
            case R.id.eq_band5: setEq(5, value); break;
        }
        Log.logD("seeker: onStopTrackingTouch");
    }

    @Override
    public boolean onBackPressed() {
        saveEqGain();
        return super.onBackPressed();
    }

    private void saveEqGain(){
        int[] val = new int[]{mBar1.getProgress(), mBar2.getProgress(), mBar3.getProgress(), mBar4.getProgress(), mBar5.getProgress()};

        StringBuffer buffer = new StringBuffer();
        String separator = "";
        for(int v : val) {
            int gain = (v - GAIN_OFFSET) * 60;
            buffer.append(separator).append(gain);
            separator = ",";
        }
        SoundBarORM.addSetting(getActivity(), EQ_SETTING, buffer.toString());
    }

    private int[] loadSavedEqGain(){
        String strV = SoundBarORM.getSettingValue(getActivity(), EQ_SETTING);
        if(strV == null || strV.split(",").length<5){
            return new int[]{0,0,0,0,0};
        }

        String[] vals = strV.split(",");
        int[] ret = new int[5];
        for (int i=0; i<vals.length; i++){
            ret[i] = Integer.valueOf(vals[i]);
        }
        return ret;
    }

    private void setEq(final int bandIndex, final int value){
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                int gain = (value- GAIN_OFFSET)*60;
                Log.logD("set " + bandIndex +", gain=" + gain);
                UserEQ0x21A eq = new UserEQ0x21A(bandIndex, gain);
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    return mibar.setUserEQControl(eq);
                }catch (GaiaException e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if(ok) {
                    //Toast.makeText(getActivity(), "操作完成", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "设置失败，请在播放音乐时调整！", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void showValue(int id, int val){
        ((TextView)mMainView.findViewById(id)).setText(String.valueOf(val)+"db");
    }

    private void updateEQUI(final UserEQ0x21A eq){
        Log.logD("Get sound eq: " + eq.toString());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (eq.mBand) {
                    case 1: {
                        mBar1.setProgress(eq.mValue / 60 + GAIN_OFFSET);
                        showValue(R.id.eq_value1, eq.mValue / 60);
                        break;
                    }
                    case 2: {
                        mBar2.setProgress(eq.mValue / 60 + GAIN_OFFSET);
                        showValue(R.id.eq_value2, eq.mValue / 60);
                        break;
                    }
                    case 3: {
                        mBar3.setProgress(eq.mValue / 60 + GAIN_OFFSET);
                        showValue(R.id.eq_value3, eq.mValue / 60);
                        break;
                    }
                    case 4: {
                        mBar4.setProgress(eq.mValue / 60 + GAIN_OFFSET);
                        showValue(R.id.eq_value4, eq.mValue / 60);
                        break;
                    }
                    case 5: {
                        mBar5.setProgress(eq.mValue / 60 + GAIN_OFFSET);
                        showValue(R.id.eq_value5, eq.mValue / 60);
                        break;
                    }
                }
            }
        });
    }

    private void loadUserEQ(){
        mMainView.findViewById(R.id.eq_settings).setVisibility(View.GONE);
        mMainView.findViewById(R.id.eq_progress).setVisibility(View.VISIBLE);
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    int[] gains = loadSavedEqGain();
                    if(gains[0]+gains[1]+gains[2]+gains[3]+gains[4]!=0){
                        int style = mibar.getEQControl();
                        if(style != EQManager.EQ_COSTUM){
                            mibar.setEQControl(EQManager.EQ_COSTUM);
                        }
                        for (int i = 0; i < 5; i++) {
                            UserEQ0x21A eq = new UserEQ0x21A(i+1, gains[i]);
                            mibar.setUserEQControl(eq);
                        }
                        mGainView.setGains(gains);
                    }

                    for (int i = 0; i < 5; i++) {
                        UserEQ0x21A eq = new UserEQ0x21A(i+1, gains[i]);
                        updateEQUI(eq);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if(ok) {
                    mMainView.findViewById(R.id.eq_settings).setVisibility(View.VISIBLE);
                }else{
                    getFragmentManager().popBackStack();
                    Toast.makeText(getActivity(), "错误，请在播放时调整EQ！", Toast.LENGTH_LONG).show();
                }
                mMainView.findViewById(R.id.eq_progress).setVisibility(View.GONE);
            }
        }.execute();
    }
}
