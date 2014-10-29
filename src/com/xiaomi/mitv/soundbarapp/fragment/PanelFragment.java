package com.xiaomi.mitv.soundbarapp.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbar.bluetooth.Util;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.protocol.ByteUtil;
import com.xiaomi.mitv.soundbar.protocol.TraceInfo0x816;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.MainActivity2;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.eq.EQManager;
import com.xiaomi.mitv.soundbarapp.eq.EQStyle;
import com.xiaomi.mitv.soundbarapp.eq.EQStyleResource;
import com.xiaomi.mitv.soundbarapp.util.Worker;
import com.xiaomi.mitv.utils.Log;
import com.xiaomi.mitv.widget.RoundSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenxuetong on 8/20/14.
 */
public class PanelFragment extends BaseFragment implements View.OnClickListener{
    private static final java.lang.String TAG = "PanelFragment";
    private static final int MAX_VOL =31;;

    private View mMain;
    private TextView mVolumeProgressTextView;
    private RoundSeekBar mSoundbarVol;
    private View mVolumeImageView;
    private ImageView mStaticVolumeImageView;
    private Button mSourceButton;
    private View mSafetyMode;
    private TextView mEqStyle;
    private RatingBar mWooferVol;
    private TextView mWooferInfo;
    private SoundBarVolumeSet mSoundBarVolController;
    private TraceInfo0x816 mBaseInfo = null;
    private PanelListener mListener;
    private A2dpProfile mProfile;

    private Worker mWorker;
    private Handler mDeviceInfoHandler;

    public interface PanelListener {
        public void onPanelRefreshed(TraceInfo0x816 info);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorker = new Worker("panel_worker");
        mDeviceInfoHandler = new Handler(mWorker.getLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMain = inflater.inflate(R.layout.fragment_panel, container, false);
        return mMain;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProfile = new A2dpProfile(getActivity());
        buildUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBarPanel(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProfile = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWorker!=null) mWorker.quit();
    }

    @Override
    protected void runTask(Runnable r) {
        if(mDeviceInfoHandler!=null) mDeviceInfoHandler.post(r);
        else super.runTask(r);
    }

    public void setInitListener(PanelListener l){
        mListener = l;
    }

    public boolean isSourceReady() {
        return mBaseInfo==null?false:mBaseInfo.mAutoRouting.audio_source != TraceInfo0x816.AudioRouting.Audio_source.none;
    }

    public void enableVolControl(boolean enable){
        if(mSoundbarVol != null) mSoundbarVol.setEnabled(enable);
    }

    public boolean supportNewUi(){
        return ((MainActivity2)getActivity()).supportNewUi();
    }

    public boolean supportSource(){
        return ((MainActivity2)getActivity()).supportSource();
    }

    public void refreshBarPanel(long delay){
        if(supportNewUi()) {
            mDeviceInfoHandler.removeCallbacks(mInitTask);
            if(delay<=0){
                mInitTask.run();
            }else {
                mDeviceInfoHandler.postDelayed(mInitTask, delay);
            }
        }else {
            onInitFinished();
        }
    }

    private void buildUi(){
        mVolumeImageView = findViewById(R.id.main_panel_vol_wave);
        mStaticVolumeImageView = findViewById(R.id.main_panel_vol_wave_static);
        mVolumeProgressTextView = findViewById(R.id.main_panel_vol_val);
        mSourceButton = findViewById(R.id.main_panel_input_source);
        mSourceButton.setOnClickListener(this);
        mSafetyMode = findViewById(R.id.main_panel_safety_status);
        mEqStyle = findViewById(R.id.main_panel_eq_style);
        mEqStyle.setText(getString(R.string.main_panel_eq_style_f, getString(R.string.eq_standar)));
        mEqStyle.setOnClickListener(this);
        mWooferVol = findViewById(R.id.main_panel_woofer_vol);
        mWooferVol.setIsIndicator(true);
        ((View)findViewbyId(R.id.main_panel_woofer_container)).setOnClickListener(this);
        mWooferInfo = findViewById(R.id.main_panel_woofer_vol_info);

        mSoundbarVol = findViewById(R.id.main_panel_vol_seekbar);
        mSoundbarVol.setMax(MAX_VOL);
        mSoundbarVol.setOnSeekBarChangeListener(mVolumeListener);
        showBarName();
    }

    private void showBarName(){
        runTask(new Runnable() {
            @Override
            public void run() {
                String addr = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.addressName);
                final String barName = Util.getName(addr);
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.main_panel_title_id)).setText(barName);
                    }
                });
            }
        });
    }

    private Runnable mInitTask = new Runnable() {
        @Override
        public void run() {
            loadBaseInfo();
            if (isSourceReady()) {
                loadEqStyle();
            }
            onInitFinished();
        }
    };

    private void onInitFinished(){
        updateUi(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) mListener.onPanelRefreshed(mBaseInfo);
            }
        });
    }

    private <T> T findViewById(int rid){
        return (T)getActivity().findViewById(rid);
    }

    private RoundSeekBar.OnSeekBarChangeListener mVolumeListener = new RoundSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onSeekBegin() {
            mVolumeProgressTextView.setVisibility(View.VISIBLE);
            ((View)findViewById(R.id.main_panel_vol_val_title)).setVisibility(View.VISIBLE);
            mVolumeImageView.setVisibility(View.GONE);
            mStaticVolumeImageView.setVisibility(View.GONE);
        }

        @Override
        public void onSeekChanged(RoundSeekBar bar, int vol, boolean fromUser) {
            showVol(vol);
            if(vol==0){
                mVolumeProgressTextView.setVisibility(View.GONE);
                ((View)findViewById(R.id.main_panel_vol_val_title)).setVisibility(View.GONE);
                showVolWaveImg(true, 0);
            }else{
                mVolumeProgressTextView.setVisibility(View.VISIBLE);
                ((View)findViewById(R.id.main_panel_vol_val_title)).setVisibility(View.VISIBLE);
                mVolumeImageView.setVisibility(View.GONE);
                mStaticVolumeImageView.setVisibility(View.GONE);
            }
            if(mSoundBarVolController !=null) mSoundBarVolController.applyChange();
        }

        @Override
        public void onSeekEnd() {
            final int vol = mSoundbarVol.getProgress();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVolumeProgressTextView.setVisibility(View.GONE);
                    showVolWaveImg(true, vol);
                    ((View)findViewById(R.id.main_panel_vol_val_title)).setVisibility(View.GONE);
                }
            }, 1500);
        }
        private void showVol(int vol){
            mVolumeProgressTextView.setText(100*vol/MAX_VOL + "%");
        }
    };

    private void handleInitException() {
        Log.logD(TAG, "failed to exec command!");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_panel_woofer_vol_info:
                connectWoofer();
                break;
            case R.id.main_panel_input_source:
                selectInputSource();
                break;
            case R.id.main_panel_eq_style:
                ((MainActivity2)getActivity()).showEq();
                break;
            case R.id.main_panel_woofer_container:
                ((MainActivity2)getActivity()).showSettings();
                break;
            default:break;
        }
    }

    private void selectInputSource() {
        if(!supportSource() || mBaseInfo==null || mBaseInfo.mAutoRouting==null) return;
        A2dpDlg dlg = new A2dpDlg((MainActivity2)getActivity(), mProfile);
        dlg.show(new Runnable(){
            @Override
            public void run() {
                refreshBarPanel(1000);
            }
        }, false);
    }

    private void connectWoofer() {
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected void onPreExecute() {
                ((View)findViewById(R.id.main_panel_no_woofer_hint)).setVisibility(View.GONE);
//                mWooferInfo.setTextColor(getResources().getColor(R.color.white));
                ((View)findViewById(R.id.main_panel_connect_woofer_progress)).setVisibility(View.VISIBLE);
                mWooferInfo.setText(R.string.main_panel_woofer_connecting);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                try {
                    return mibar.connectWoofer();
                } catch (GaiaException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if(!ok){
                    ((View)findViewById(R.id.main_panel_no_woofer_hint)).setVisibility(View.VISIBLE);
                    ((View)findViewById(R.id.main_panel_connect_woofer_progress)).setVisibility(View.GONE);
                    mWooferInfo.setText(R.string.main_panel_no_woofer);
//                    mWooferInfo.setTextColor(Color.RED);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runTask(new Runnable() {
                            @Override
                            public void run() {
                                loadBaseInfo();
                            }
                        });
                    }
                }, 15000);
            }
        }.execute();
    }

    private void loadEqStyle() {
        EQManager manager = new EQManager();
        EQStyle style = manager.readSoundBarStyle(getActivity());
        int styleId = manager.idOfStyle(style);
        if(-1 == styleId){
            Log.logD(TAG, "load EQ failed! set to default.");
            return;
        }
        final EQStyleResource res = manager.getResourceById(styleId);
        updateUi(new Runnable() {
            @Override
            public void run() {
                mEqStyle.setText(getString(R.string.main_panel_eq_style_f, getString(res.nameId)));
                View view = findViewById(R.id.main_panel);
                view.setBackgroundResource(res.bgId);
                mSoundbarVol.setThumbDrawableResource(res.thumbId);
            }
        });
    }

    private void loadBaseInfo() {
        IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
        String tmpStr = null;
        try {
            String data = mibar.querySystemTraceInfo();
            SoundBarORM.addSetting(getActivity(), "traceInfo", data);
            Log.logD("0x816: " + data);
            JSONObject o = new JSONObject(data);
            tmpStr = o.getString("raw");
        } catch (GaiaException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mBaseInfo = null;
        if(tmpStr != null) {
            byte[] raw = ByteUtil.HexString2Bytes(tmpStr);
            mBaseInfo = new TraceInfo0x816();
            if (!mBaseInfo.parse(raw)) {
                mBaseInfo = null;
            }
        }
        if(mBaseInfo == null){
            updateUi(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "抱歉，同步音响状态失败,请确保是最新固件，或者重启应用刷新状态!", Toast.LENGTH_LONG).show();
                }
            });
        }

        updateUi(new Runnable() {
            @Override
            public void run() {
                ((View)findViewById(R.id.main_panel_connect_woofer_progress)).setVisibility(View.GONE);
                if(mBaseInfo==null){handleInitException(); return;}
                mSourceButton.setText(mBaseInfo.mAutoRouting.str_source());
                if (mBaseInfo.mAutoRouting.audio_source != TraceInfo0x816.AudioRouting.Audio_source.none) {
                    int soundBarVol = mBaseInfo.mSoundBar.volumeOfSource(mBaseInfo.mAutoRouting.audio_source);
                    mSoundbarVol.setProgress(soundBarVol);
                    mSoundbarVol.setEnabled(true);
                    mSoundBarVolController = new SoundBarVolumeSet(soundBarVol);
                    showVolWaveImg(true, soundBarVol);
                } else {
                    mSoundbarVol.setProgress(0);
                    mSoundbarVol.setEnabled(false);
                    mSoundbarVol.setThumbDrawableResource(R.drawable.home_page_button_xiaomisound_standard_unavailable);
                    showVolWaveImg(false, 0);
                }
                if (mBaseInfo.mAutoRouting.woofer_ready) {
                    mWooferInfo.setOnClickListener(null);
                    ((View)findViewById(R.id.main_panel_no_woofer_hint)).setVisibility(View.GONE);
                    mWooferInfo.setText(R.string.main_panel_woofer_vol);
//                    mWooferInfo.setTextColor(getResources().getColor(R.color.white));
                    int wooferVol = mBaseInfo.mSubwoofer.volumeOfSource(mBaseInfo.mAutoRouting.audio_source);
                    mWooferVol.setVisibility(View.VISIBLE);
                    mWooferVol.setRating(wooferVol * 10 / MAX_VOL);
                } else {
                    ((View)findViewById(R.id.main_panel_no_woofer_hint)).setVisibility(View.VISIBLE);
                    mWooferInfo.setText(R.string.main_panel_no_woofer);
//                    mWooferInfo.setTextColor(Color.RED);
                    mWooferInfo.setOnClickListener(PanelFragment.this);
                    mWooferVol.setVisibility(View.GONE);
                }
                mSafetyMode.setVisibility(mBaseInfo.mSoundBar.safety_mode?View.VISIBLE:View.GONE);
            }
        });
    }

    private void showVolWaveImg(boolean haveInput, int vol){
        if(haveInput){
            if(vol==0){ //slient
                mStaticVolumeImageView.setImageResource(R.drawable.home_icon_slient);
                mStaticVolumeImageView.setVisibility(View.VISIBLE);
                mVolumeImageView.setVisibility(View.GONE);
            }else{
                mStaticVolumeImageView.setVisibility(View.GONE);
                mVolumeImageView.setVisibility(View.VISIBLE);
            }
        }else{
            mStaticVolumeImageView.setImageResource(R.drawable.home_page_pic_xiaomisound_audio);
            mStaticVolumeImageView.setVisibility(View.VISIBLE);
            mVolumeImageView.setVisibility(View.GONE);
        }
    }

    private class SoundBarVolumeSet implements Runnable {
        private int mCurrentVol;
        private Handler mHandler;
        private boolean mRunning = false;

        public SoundBarVolumeSet(int base){
            mCurrentVol = base;
            mHandler = new Handler(getActivity().getMainLooper());
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
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
                    try {
                        while(mCurrentVol<=MAX_VOL) {
                            int vol = mSoundbarVol.getProgress();
                            Log.logD(TAG, "change bar vol from: " + mCurrentVol + " to "+ vol);
                            if(vol < mCurrentVol) {
                                if(mibar.changeVolume(IMiSoundDevice.SOUNDBAR_VOL_DECRESE)){
                                    --mCurrentVol;
                                }
                            }else if(vol > mCurrentVol){
                                if(mibar.changeVolume(IMiSoundDevice.SOUNDBAR_VOL_INCRESE)){
                                    ++mCurrentVol;
                                }
                            }else break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mRunning = false;
                    }
                    return null;
                }
            }.execute();
        }
    }
}
