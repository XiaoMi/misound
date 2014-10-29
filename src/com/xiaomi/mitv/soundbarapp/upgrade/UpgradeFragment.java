package com.xiaomi.mitv.soundbarapp.upgrade;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaomi.mitv.content.FirmwareVersion;
import com.xiaomi.mitv.idata.util.iDataCenterORM;
import com.xiaomi.mitv.soundbar.UpdateService;
import com.xiaomi.mitv.soundbar.api.IDFUUpdate;
import com.xiaomi.mitv.soundbar.api.ISoundBarStateTracker;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.ConnectingActivity;
import com.xiaomi.mitv.soundbarapp.MainActivity2;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.utils.Log;
import com.xiaomi.mitv.widget.RoundProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chenxuetong on 9/3/14.
 */
public class UpgradeFragment extends BaseFragment {
    private ProgressBar mVersionChecking;
    private TextView mProgressText;
    private ImageView mUpgradeReminder;
    private ImageView mIcon;
    private TextView mUpgradeStatus;
    private View mMainView;

    private IDFUUpdate dfuUpdate;
    private FirmwareUpgradeTask mUpgradeTask;
    private boolean mIsUploaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.upgrade_widget_layout, container, false);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVersionChecking = findViewbyId(R.id.upgrade_checking_progress);
        mProgressText = findViewbyId(R.id.upgrade_progres_text);
        mUpgradeReminder = findViewbyId(R.id.upgrade_reminder);
        mUpgradeStatus = findViewbyId(R.id.upgarde_status_text);
        mIcon = findViewbyId(R.id.upgrade_icon);

        View button = findViewbyId(R.id.upgrade_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpgrade();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return doCustomUpgrade();
            }
        });
        checkNewUpgrade();
        startService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mUpgradeTask != null){
            mUpgradeTask.cancel(false);
            mUpgradeTask = null;
        }
        clearService();
    }

    private void doUpgrade() {
        if(mUpgradeTask == null) {
            mUpgradeTask = new FirmwareUpgradeTask();
            mUpgradeTask.execute();
        }
    }

    private boolean doCustomUpgrade() {
        if (mUpgradeTask == null) {
            showVersionSelection();
            return true;
        }
        return false;
    }

    private void checkNewUpgrade(){
        runTask(new Runnable() {
            @Override
            public void run() {
                String localVersionName = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuCurrentVersion);
                boolean needUpdate = (new FirmwareManager(getActivity()).getNewFirmware()!=null) ||
                                        getUpgradeVersionFromAsset().compareTo(localVersionName)>0;
                if(!needUpdate) return;
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        mUpgradeReminder.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void showVersionSelection() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment pre = getFragmentManager().findFragmentByTag("versions");
        if(pre != null){
            ft.remove(pre);
        }
        ft.addToBackStack(null);
        VersionSelectFragment target = new VersionSelectFragment();
        target.setUpgradeHandler(this);
        target.show(ft, "versions");
    }

    private void onProgressMessage(int code) {
        int progress = code;
        Log.logD("progress: " + progress);
        if(progress>=0 && progress<=100) {
            mProgressText.setText(progress + "%");
        }

        if (100 == progress) {
        }
        if(-1 == progress){
            onUpgradeFailure();
        }

        if(200 == progress){
            mIsUploaded = true;
            onUploadDone();
        }
    }

    private void onUploadDone() {
        showUpgrading();
        new Counter();
    }

    private void onUpgradeDone() {
        String currentVersion = SoundBarORM.getSettingValue(this.getActivity(), SoundBarORM.dfuCurrentVersion);
        String toVersion = SoundBarORM.getSettingValue(this.getActivity(), "to be version");
        iDataCenterORM.getInstance(this.getActivity()).sendDataBack("soundbar_upgrade_suc", "from: "+currentVersion+ " to:"+toVersion);
        startActivity(new Intent(getActivity(), OverlayerOK.class));
        getActivity().finish();
        if(mUpgradeTask != null) {
            mUpgradeTask.cancel(false);
        }
        mUpgradeTask = null;
        mIsUploaded = false;
    }

    private void onUpgradeFailure() {
        showDefault(true);

        String currentVersion = SoundBarORM.getSettingValue(this.getActivity(), SoundBarORM.dfuCurrentVersion);
        String toVersion = SoundBarORM.getSettingValue(this.getActivity(), "to be version");

        iDataCenterORM.getInstance(this.getActivity()).sendDataBack("soundbar_upgrade_fail", "from: " + currentVersion + " to:" + toVersion);
        startActivity(new Intent(getActivity(), OverlayerFailure.class));
        if(mUpgradeTask != null) {
            mUpgradeTask.cancel(false);
        }
        mUpgradeTask = null;
        mIsUploaded = false;
        ((MainActivity2)getActivity()).onFirmwareUpgrading(true);
    }

    private void showDefault(boolean haveUpdate){
        mIcon.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.GONE);
        mProgressText.setText("0%");
        mUpgradeStatus.setText(R.string.main_entry_upgrade);
        mUpgradeReminder.setVisibility(haveUpdate?View.VISIBLE:View.GONE);
    }

    private void showVerChecking(){
        mIcon.setVisibility(View.GONE);
        mUpgradeReminder.setVisibility(View.GONE);
        mUpgradeStatus.setVisibility(View.VISIBLE);
        mUpgradeStatus.setText(R.string.main_upgrade_checking);
        mVersionChecking.setVisibility(View.VISIBLE);
    }

    private void showDownloading(){
        mIcon.setVisibility(View.GONE);
        mUpgradeReminder.setVisibility(View.GONE);
        mUpgradeStatus.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText("0%");
        mUpgradeStatus.setText(R.string.main_upgrade_downlad);
        mVersionChecking.setVisibility(View.GONE);
    }

    private void showUploading(){
        mIcon.setVisibility(View.INVISIBLE);
        mUpgradeReminder.setVisibility(View.GONE);
        mUpgradeStatus.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText("0%");
        mUpgradeStatus.setText(R.string.main_upgrade_upload);
        mVersionChecking.setVisibility(View.GONE);
    }

    private void showUpgrading(){
        mIcon.setVisibility(View.GONE);
        mUpgradeReminder.setVisibility(View.GONE);
        mUpgradeStatus.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText("0%");
        mUpgradeStatus.setText(R.string.main_upgrade_ongoing);
        mVersionChecking.setVisibility(View.GONE);
    }

    private String getUpgradeVersionFromAsset(){
        String versionName = "";
        try {
            //query local version
            AssetManager am = getActivity().getAssets();
            String[] files = am.list("dfu");
            for (String fileName : files) {
                if (fileName.endsWith(".ver")) {
                    versionName = fileName.substring(0, fileName.lastIndexOf(".ver"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return versionName;
    }

    private void startService() {
        Intent intent = new Intent(UpdateService.ACTION_UPDATE);
        boolean ret = getActivity().bindService(intent, sc, Context.BIND_AUTO_CREATE);
        Log.logD("startService return with " + ret);
    }

    private void clearService() {
        if(dfuUpdate != null) {
            try {
                dfuUpdate.unRegisterStateTracker();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            getActivity().unbindService(sc);
        }
    }

    ServiceConnection sc = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dfuUpdate = IDFUUpdate.Stub.asInterface(service);
            Log.logD("Service Connection 已连接");

            try {
                dfuUpdate.registerStateTracker(stateTracker);

                String address = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.addressName);
                boolean always_scan = true;
                if (always_scan || null == address
                        || 0 == address.length()) {
                    // searchSoundBar(null);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.logE("Service Connection 已断开连接");
        }
    };

    ISoundBarStateTracker stateTracker = new ISoundBarStateTracker.Stub() {
        @Override
        public void onState(int state, String msg) throws RemoteException {
            log("state="+state + ", msg="+msg);
        }

        @Override
        public void onProgress(int steps) throws RemoteException {
            uiHandler.obtainMessage(10, steps).sendToTarget();
        }

        @Override
        public void log(String msg) throws RemoteException {
            uiHandler.obtainMessage(1, msg).sendToTarget();
        }

        @Override
        public void onCommand(int command_id, String result, boolean suc)
                throws RemoteException {
        }

        @Override
        public void connected() throws RemoteException {
            log("小米家庭音响, connected ");
        }

        @Override
        public void disConnected() throws RemoteException {
            log("小米家庭音响, DISconnected");
            if(mIsUploaded) return; // while writing firmware, the connection is going to disconnecting.

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDefault(false);
                    if(mUpgradeTask != null) {
                        onUpgradeFailure();
                    }
                }
            });
        }

        @Override
        public void deviceFounded(boolean got) throws RemoteException {
            if(!got && mUpgradeTask!=null) onUpgradeFailure();
        }
    };

    Handler uiHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch(msg.what){
                case 10:{
                    // update_progress.setProgress((Integer)(msg.obj));
                    onProgressMessage((Integer) msg.obj);
                    break;
                }
                case 1:{
                    Log.logD("1: " + (String)(msg.obj));
                    break;
                }
                case 100:{
                    Log.logD("100: " + (String)(msg.obj));
                    break;
                }
            }
            super.dispatchMessage(msg);
        }
    };

    public void forceUpgrade(FirmwareVersion version) {
        if(mUpgradeTask != null) return;
        mUpgradeTask = new FirmwareUpgradeTask(version);
        mUpgradeTask.execute();
    }



    private class FirmwareUpgradeTask extends AsyncTask<Void, Integer, Boolean>{
        private static final int PROGRESS_BEGIN_DOWNLOAD = 0;
        private static final int PROGRESS_END_DOWNLOAD = 1;
        private static final int PROGRESS_BEGIN_UPGRADE = 2;
        private static final int PROGRESS_END_UPGRADE = 3;

        private FirmwareVersion mTargetVersion;

        FirmwareUpgradeTask(){}
        FirmwareUpgradeTask(FirmwareVersion version){
            mTargetVersion = version;
        }

        @Override
        protected void onPreExecute() {
            showVerChecking();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            FirmwareManager manager = new FirmwareManager(getActivity());
            FirmwareVersion latest  = mTargetVersion;
            if(latest == null) latest=manager.getNewFirmware();
            if(latest == null){
                String localVersionName = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuCurrentVersion);
                if(getUpgradeVersionFromAsset().compareTo(localVersionName)>0){
                    installDefaultFirmware();
                    publishProgress(PROGRESS_BEGIN_UPGRADE);
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "音响固件已经是最新版", Toast.LENGTH_LONG).show();
                            showDefault(false);
                        }
                    });
                    mUpgradeTask = null;
                }
                return true;
            }

            FirmwareManager.ProgressListener listener = new FirmwareManager.ProgressListener() {
                @Override
                public void onProgress(final int total, final int finished) {
                    if(total!=0){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int progress = (finished*100)/total;
                                onProgressMessage(progress);
                            }
                        });
                    }
                }
            };

            publishProgress(PROGRESS_BEGIN_DOWNLOAD, latest.size);
            String dfuFile = manager.download(latest, listener) ;
            if(dfuFile != null) {
                publishProgress(PROGRESS_END_DOWNLOAD, 0);
                try {
                    SoundBarORM.addSetting(getActivity(), SoundBarORM.force_update, "1");
                    publishProgress(PROGRESS_BEGIN_UPGRADE);
                    dfuUpdate.requestDFUUpdate(dfuFile, latest.codeName, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return false;
                }
            }else{
                publishProgress(PROGRESS_END_DOWNLOAD, -1);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            if(isCancelled()) return;
            if(!successful){
                onUpgradeFailure();
            }
            mVersionChecking.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(isCancelled()) return;
            if(values.length==0) return;
            int status = values[0];
            switch (status){
                case PROGRESS_BEGIN_DOWNLOAD:
                    showDownloading();
                    break;
                case PROGRESS_END_DOWNLOAD:
                    if(values[1] != 0) {
                        Toast.makeText(getActivity(), "抱歉下载升级包失败， 请重试！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case PROGRESS_BEGIN_UPGRADE:
                    showUploading();
                    ((MainActivity2)getActivity()).onFirmwareUpgrading(false);
                    break;
                case PROGRESS_END_UPGRADE:
                    break;
            }
        }
    }

    public static class OverlayerFailure extends Activity{
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.upgrade_result_layout);
            View root = findViewById(R.id.main_upgrade_result);
            ImageView flag = (ImageView)findViewById(R.id.upgrade_result_flag);
            TextView text = (TextView)findViewById(R.id.upgrade_result_text);
            TextView info = (TextView)findViewById(R.id.upgrade_result_info);
            flag.setImageResource(R.drawable.upgrade_page_icon_xiaomisound_update_failure);
            text.setText(R.string.main_upgrade_result_failed);
            info.setText(R.string.main_upgrade_result_failure_info);
            info.setTextColor(Color.parseColor("#f8b8a4"));
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }
    }

    public static class OverlayerOK extends Activity{
        private TextView mInfo;
        private Handler mHandler;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.upgrade_result_layout);
            ImageView flag = (ImageView)findViewById(R.id.upgrade_result_flag);
            TextView text = (TextView)findViewById(R.id.upgrade_result_text);
            mInfo = (TextView)findViewById(R.id.upgrade_result_info);
            flag.setImageResource(R.drawable.upgrade_page_icon_xiaomisound_update_successful);
            text.setText(R.string.main_upgrade_result_ok);
            mInfo.setText(getString(R.string.main_upgrade_result_ok_info, 3));
            mInfo.setTextColor(Color.parseColor("#9eccff"));

            View root = findViewById(R.id.main_upgrade_result);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reconnect();
                }
            });

            mHandler = new Handler();
            mHandler.postDelayed(mStop, 3000);
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            reconnect();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        private Runnable mStop = new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        };

        private void reconnect(){
            mHandler.removeCallbacks(mStop);
            finish();
            startActivity(new Intent(getApplicationContext(), ConnectingActivity.class));
        }
    }

    private class Counter {
        private int mCountDown = 30;
        final private Handler mHandler;
        public Counter(){
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(mCountDownUpdate, 1000);
        }
        private Runnable mCountDownUpdate = new Runnable() {
            @Override
            public void run() {
                mCountDown--;
                if(mCountDown>0){
                    onProgressMessage((30-mCountDown)*100/30);
                    mHandler.postDelayed(mCountDownUpdate, 1000);
                }else{
                    onUpgradeDone();
                }
            }
        };
    }



    private boolean installDefaultFirmware(){
        boolean result = true;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open("dfu/update.dfu");

            InputStream isversion = am.open("dfu/" + getResources().getString(R.string.dfu_version));

            copyDFUToLocalPath(is);
            is.close();

            copyVerToLocalPath(isversion);
            isversion.close();

            try {
                SoundBarORM.addSetting(getActivity(), SoundBarORM.force_update, "1");
                String fileDir = getActivity().getFilesDir().getAbsolutePath();
                dfuUpdate.requestDFUUpdate(fileDir + "/update.dfu", "", true);
            } catch (RemoteException e) {
                e.printStackTrace();
                result = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private void copyDFUToLocalPath(InputStream fIn){
        String dir = getActivity().getFilesDir().getAbsolutePath()+ "/update.dfu";

        File iconFile = new File(dir);
        FileOutputStream fOut = null;

        try {
            iconFile.createNewFile();
            fOut = new FileOutputStream(iconFile);

            byte []buffer = new byte[1024*10];
            int len = -1;
            while((len = fIn.read(buffer)) > 0){
                fOut.write(buffer, 0, len);
            }

            fOut.close();
            fIn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void copyVerToLocalPath(InputStream fIn){
        String dir = getActivity().getFilesDir().getAbsolutePath()+ "/" + getResources().getString(R.string.dfu_version);

        File iconFile = new File(dir);
        FileOutputStream fOut = null;

        try {
            iconFile.createNewFile();
            fOut = new FileOutputStream(iconFile);

            byte []buffer = new byte[1024*10];
            int len = -1;
            while((len = fIn.read(buffer)) > 0){
                fOut.write(buffer, 0, len);
            }

            fOut.close();
            fIn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
