package com.xiaomi.mitv.soundbarapp.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbar.protocol.Source;
import com.xiaomi.mitv.soundbar.protocol.TraceInfo0x816;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 9/12/14.
 */
public class A2dpDlg {
    private static final String TAG = "source";

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_DISCONNECT = 1;

    private Context mConext;
    private A2dpProfile mProfile;
    private boolean mSelectionDone = false;

    public A2dpDlg(Context context, A2dpProfile profile){
        mConext = context;
        mProfile = profile;
    }

    public void show(View base, Runnable runnable){
        BluetoothDevice bar = getBarDevice();
        if(bar == null) return;

        boolean imConnected = mProfile.isConnected(bar);

        if (!imConnected) {
            show(base, ACTION_CONNECT, runnable);
        } else {
            show(base, ACTION_DISCONNECT, runnable);
        }
    }

    private void show(View base, final int action, final Runnable callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(mConext);
        final View contain = View.inflate(mConext, R.layout.source_selector, null);
        builder.setView(contain);
        final AlertDialog dlg = builder.create();
        {
            View item1 = contain.findViewById(R.id.source_1);
            item1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectionDone) return;
                    mSelectionDone = true;
                    contain.findViewById(R.id.source_progress).setVisibility(View.VISIBLE);
                    if(action == ACTION_CONNECT){
                        connectPhone2Bar(dlg, callback);
                    }else{
                        disconnectPhoneFromBar(dlg, callback);
                    }
                }
            });

            TextView v = (TextView)contain.findViewById(R.id.source_content_1);
            if(action == ACTION_CONNECT) {
                v.setText("连接我的手机");
            }else{
                v.setText("断开我的手机");
            }
        }
        dlg.show();
        caculatePos(base, dlg);
    }

    private void caculatePos(View base, AlertDialog dlg){
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp =w.getAttributes();

        DisplayMetrics display = mConext.getResources().getDisplayMetrics();
        int dpi = display.densityDpi;
        lp.width = 200*dpi/160;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = -100;

        w.setAttributes(lp);
    }

    private BluetoothDevice getBarDevice(){
        String addr = SoundBarORM.getSettingValue(mConext, SoundBarORM.addressName);
        if (addr == null) return null;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bar = adapter.getRemoteDevice(addr);
        return bar;
    }

    private void connectPhone2Bar(final AlertDialog dlg, final Runnable callback){
        final BluetoothDevice bar = getBarDevice();
        if(bar == null) return;
        mProfile.connect(bar, new Runnable() {
            @Override
            public void run() {
                showAlert("提示", "手机已经连接到音响，播放音乐试试效果吧!");
            }
        });
        if(callback != null) {
            runDelay(new Runnable() {
                @Override
                public void run() {
                    callback.run();
                    runDelay(new Runnable() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                        }
                    }, 1000);
                }
            }, 5000);
        }
    }

    private void disconnectPhoneFromBar(final AlertDialog dlg, final Runnable callback){
        final BluetoothDevice bar = getBarDevice();
        if(bar == null) return;
        mProfile.disconnect(bar, null);
        if(callback != null) {
            runDelay(new Runnable() {
                @Override
                public void run() {
                    callback.run();
                    runDelay(new Runnable() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                        }
                    }, 1000);
                }
            }, 5000);
        }
    }

    private void runDelay(Runnable r, long delay){
        new Handler(Looper.getMainLooper()).postDelayed(r, delay);
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(mConext)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.ok, null)
                .create()
                .show();
    }
}
