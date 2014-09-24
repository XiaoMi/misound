package com.xiaomi.mitv.soundbarapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;
import com.csr.gaia.android.library.gaia.GaiaCommand;
import com.xiaomi.mitv.idata.util.iDataCenterORM;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.GaiaControl;
import com.xiaomi.mitv.soundbar.IMiSoundDevice;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbar.callback.SoundBarStateTracker2;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.gaia.GaiaHelper;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.utils.Log;
import com.xiaomi.mitv.widget.RoundAnimationView;

import java.util.Date;

/**
 * Created by chenxuetong on 8/18/14.
 */
public class ConnectingActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "ConnectingActivity";
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private ConnectTask mConnectTask;
    private RoundAnimationView mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connecting_layout);

        findViewById(R.id.connect_try).setOnClickListener(this);
        mAnimation = (RoundAnimationView)findViewById(R.id.discovery_animation);
        if(bluetoothIsReady()){
            doConnect();
        }
        new A2dpProfile(getApplicationContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode != Activity.RESULT_OK) {
                    showBluetoothAlert();
                } else {
                    doConnect();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
        A2dpProfile.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mConnectTask != null) mConnectTask.cancel(false);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.connect_try){
            mAnimation.setOnClickListener(null);
            doConnect();
        }
    }

    private void doConnect(){
        findViewById(R.id.connecting_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.connecting_failure_panel).setVisibility(View.GONE);
        mConnectTask = new ConnectTask();
        mConnectTask.execute();
    }

    private void onConnected(boolean ok) {
        if(ok) {
            gotoMainActivity();
        }else {
            mAnimation.setImage(R.drawable.search_icon_xiaomisound_no_box);
            findViewById(R.id.connecting_panel).setVisibility(View.GONE);
            findViewById(R.id.connecting_failure_panel).setVisibility(View.VISIBLE);
            if(mLastScanCode == GaiaControl.SCAN_BT_BOUND_FAILED){
                Toast.makeText(this, "蓝牙配对失败！请在系统设置->蓝牙中点击‘小米家庭音响’，完成配对后重试！", Toast.LENGTH_LONG).show();
            }
            mAnimation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WrapperActivity.go(ConnectingActivity.this, WrapperActivity.FRAGMENT_DIAGNOSIS, false);
                }
            });
        }
    }

    public void onGotVersion(String version) {
        Log.logD("onGotVersion()=" + version);
        if(version==null){
            reportErr2Cloud("soundbar_device_get_version_fail");
        }else{
            int verCode = GaiaHelper.verCode(version);
            //save version code
            SoundBarORM.addSetting(this, SoundBarORM.dfuCurrentVersionCode, String.valueOf(verCode));
            SoundBarORM.addSetting(this, SoundBarORM.dfuCurrentVersion, version);
        }
    }

    private void onBtFound(boolean got) {
        Log.logD(TAG, "onBtfound() with " + got);
    }

    private boolean bluetoothIsReady(){
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if(ba == null || !ba.isEnabled()) {
            Intent enableBtIntent;

            enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            overridePendingTransition(0,0);
            return false;
        }
        return true;
    }

    public void gotoMainActivity() {
//        Toast.makeText(this, "connect OK", Toast.LENGTH_LONG).show();
        MainActivity2.go(this);
        finish();
    }

    public void showBluetoothAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("小米音响");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setMessage("亲，打开蓝牙才能连接小米音响哦");
        builder.show();
    }

    private void reportErr2Cloud(String key){
        String time = DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString();
        iDataCenterORM.getInstance(this).sendDataBack(key, time);
    }

    int mLastScanCode = GaiaControl.SCAN_BT_OK;
    private SoundBarStateTracker2 mCallback = new SoundBarStateTracker2() {
        @Override
        public void connected() {}

        @Override
        public void disConnected() {}

        @Override
        public void deviceFound(boolean got, int code) {
            mLastScanCode = code;
            String address = SoundBarORM.getSettingValue(ConnectingActivity.this, SoundBarORM.addressName);
            if(!TextUtils.isEmpty(address)){
                Log.logD("PortalFragment.deviceFounded() found bt device: " + address);
            }
            onBtFound(got);
        }

        @Override
        public void onCommand(int command_id, GaiaCommand result) {}
    };

    private class ConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            mAnimation.setImage(R.drawable.search_icon_xiaomisound_box);
            mAnimation.startAnimation();
            IMiSoundDevice mibar = new DefaultMisoundDevice(ConnectingActivity.this);
            try {
                mibar.register(mCallback);
                return mibar.requestModuleVersion();
            }catch (GaiaException e){
                e.printStackTrace();
                return null;
            }finally {
                mibar.unregister(mCallback);
            }
        }

        @Override
        protected void onPostExecute(String ver) {
            mAnimation.stopAnimation();
            if(isCancelled()) return;
            onGotVersion(ver);
            onConnected(!TextUtils.isEmpty(ver));
        }
    }
}
