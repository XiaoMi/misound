package com.xiaomi.mitv.soundbarapp.diagnosis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import com.xiaomi.mitv.idata.util.iDataCenterORM;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.protocol.ByteUtil;
import com.xiaomi.mitv.soundbar.protocol.TraceInfo0x816;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.WrapperActivity;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.utils.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenxuetong on 7/10/14.
 */
public class DiagnosisFragment extends BaseFragment implements View.OnClickListener {
    private ViewWrapper mUIContainer;
    private Engine mEngine;
    private OnListener mListener;
    private View mMainView;
    private SoundBarCheckTask mCheckTask;

    public interface OnListener{
        void goBack();
        void goFeedback();
        void goFaq();
    }

    public static DiagnosisFragment newInstance(OnListener listener){
        return new DiagnosisFragment(listener);
    }

    public DiagnosisFragment(){}
    private DiagnosisFragment(OnListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUIContainer = new ViewWrapper();
        mMainView = mUIContainer.create(inflater, container);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEngine = new Engine(getActivity());
        mUIContainer.onActivityReady(getActivity(), this);
        mUIContainer.showLoading(true);
        mConfigLoader.execute();

        mMainView.findViewById(R.id.actionbar).setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mConfigLoader.cancel(true);
        if(mCheckTask != null){
            mCheckTask.cancel(false);
            mCheckTask  = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.actionbar:
                if(mListener!=null) mListener.goBack();
                break;
            case R.id.category_bbs:
                goBBs();
                break;
            case R.id.category_faq:
                goFaq();
                break;
        }
    }

    private void goFaq(){
        WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_FAQ);
    }

    private void goBBs(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://bbs.xiaomi.cn/forum.php?mod=forumdisplay&fid=681&filter=typeid&typeid=4654"));
        startActivity(i);
    }

    private void showLoading(boolean loading){
        mMainView.findViewById(R.id.examine_loading).setVisibility(loading?View.VISIBLE:View.GONE);
//        mMainView.findViewById(R.id.examine_content).setVisibility(loading?View.GONE:View.VISIBLE);
    }

    private void doExamine() {
        String ver = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuCurrentVersion);
        if(ver.compareTo("4.0.4")<0){
            showAlert("提示", "该功能需要音响软件4.0.4版本，请升级后再体验这个功能！");
            return;
        }
        showLoading(true);
        mCheckTask = new SoundBarCheckTask();
        mCheckTask.execute();
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(getActivity())
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

    @Override
    public boolean onBackPressed() {
        return mUIContainer.onActivityBackPressed();
    }

    public void go2Feedback(){
        if(mListener!=null) mListener.goFeedback();
    }

    public void goBack() {
        if(mListener!=null) mListener.goBack();
    }

    private AsyncTask<Void, Void, Boolean> mConfigLoader = new AsyncTask<Void, Void, Boolean>() {
        @Override
        protected Boolean doInBackground(Void... params) {
            return mEngine.init();
        }

        @Override
        protected void onPostExecute(Boolean loadded) {
            if(isCancelled()) return;
            if(loadded) {
                mUIContainer.showLoading(false);
                mEngine.bindView(mUIContainer);
            }else{
                Toast.makeText(getActivity(), "抱歉，加载配置数据错误！", Toast.LENGTH_LONG).show();
            }
        }
    };

    private class SoundBarCheckTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            IMiSoundDevice mibar = new DefaultMisoundDevice(getActivity());
            try {
                String info = mibar.querySystemTraceInfo();
                reportTrace2Cloud(info);
                Log.logD("0x816: " + info);
                    JSONObject o = new JSONObject(info);
                    return o.getString("raw");
            } catch (GaiaException e ){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isCancelled()) return;
            showLoading(false);
            if(s==null){
                Toast.makeText(getActivity(), "错误，请重试！", Toast.LENGTH_LONG).show();
                return;
            }

            Log.logD("info: " + s);
            byte[] raw = ByteUtil.HexString2Bytes(s);
            TraceInfo0x816 info = new TraceInfo0x816();
            if(!info.parse(raw)){
                Toast.makeText(getActivity(), "抱歉，版本兼容性错误!", Toast.LENGTH_LONG).show();
                return;
            }

            StringBuffer buffer = new StringBuffer();
            String source = info.mAutoRouting.str_source();
            boolean wooferConnected = info.mAutoRouting.woofer_ready;
            buffer.append("当前输入源：").append(source.equals("none")?"没有":source).append("\n");
            buffer.append("低音炮状态：").append(info.mAutoRouting.woofer_ready ? "已连接" : "未连接").append("\n");
            int soundBarVol = info.mSoundBar.volumeOfSource(info.mAutoRouting.audio_source);
            if(soundBarVol>=0) {
                buffer.append("MiBar音量：").append(precent(soundBarVol, 31)).append("\n");
            }
            int wooferVol = info.mSubwoofer.volumeOfSource(info.mAutoRouting.audio_source);
            if(wooferVol>=0){
                buffer.append("低音炮音量：").append(precent(wooferVol, 31)).append("\n");
            }
            buffer.append("蓝牙安全模式：").append(info.mSoundBar.safety_mode?"打开":"关闭").append("\n");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("音响状态")
                    .setMessage(buffer.toString());
            if(!wooferConnected){
                builder.setNegativeButton("连接低音炮", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect2Woofer();
                    }
                });
            }
            builder.setPositiveButton("确定", null)
                    .create()
                    .show();
        }

        private void reportTrace2Cloud(String info){
            iDataCenterORM.getInstance(getActivity()).sendDataBack("hw_state", info);
        }

        private String precent(int v, int max){
            return String.valueOf(v*100/max)+"%";
        }
        private void connect2Woofer(){
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try{new DefaultMisoundDevice(getActivity()).connectWoofer();}catch (GaiaException e){e.printStackTrace();}
                    return null;
                }
            }.execute();
        }
    };
}
