package com.xiaomi.mitv.soundbarapp.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbarapp.MainActivity2;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 9/12/14.
 */
public class A2dpDlg {
    private static final String TAG = "source";

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_DISCONNECT = 1;

    private MainActivity2 mMainActivity;
    private A2dpProfile mProfile;
    private boolean mSelectionDone = false;
    private AudioManager mAudioManager;

    public A2dpDlg(MainActivity2 context, A2dpProfile profile){
        mMainActivity = context;
        mProfile = profile;
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void show(Runnable runnable, boolean confirmed){
        BluetoothDevice bar = mProfile.getBarDevice(mMainActivity);
        if(bar == null) return;

        boolean imConnected = mProfile.isConnected(bar);

        if (!imConnected) {
            show(ACTION_CONNECT, runnable, confirmed);
        } else {
            show(ACTION_DISCONNECT, runnable, confirmed);
        }
    }

    public boolean isA2dpSelected(){
        return mAudioManager.isBluetoothA2dpOn();
    }

    private void show(final int action, final Runnable callback, boolean confirmed){
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        final View contain = View.inflate(mMainActivity, R.layout.source_selector, null);
        builder.setView(contain);
        final AlertDialog dlg = builder.create();

        View.OnClickListener listener = new View.OnClickListener() {
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
        };

        View item1 = contain.findViewById(R.id.source_1);
        item1.setOnClickListener(listener);

        TextView v = (TextView)contain.findViewById(R.id.source_content_1);
        if(action == ACTION_CONNECT) {
            v.setText("连接我的手机");
        }else{
            v.setText("断开我的手机");
        }

        dlg.show();
        caculatePos(dlg);
        if(confirmed) listener.onClick(item1);
    }

    private void caculatePos(AlertDialog dlg){
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp =w.getAttributes();

        DisplayMetrics display = mMainActivity.getResources().getDisplayMetrics();
        int dpi = display.densityDpi;
        lp.width = 200*dpi/160;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = -100;

        w.setAttributes(lp);
    }

    private void connectPhone2Bar(final AlertDialog dlg, final Runnable callback){
        final BluetoothDevice bar = mProfile.getBarDevice(mMainActivity);
        if(bar == null) return;
        boolean ok = mProfile.connect(bar, null);
        if(ok && callback != null) {
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
        }else{
            dlg.dismiss();
        }
    }

    private void disconnectPhoneFromBar(final AlertDialog dlg, final Runnable callback){
        final BluetoothDevice bar = mProfile.getBarDevice(mMainActivity);
        if(bar == null) return;
        boolean ok = mProfile.disconnect(bar, null);
        mMainActivity.showDefaultEntries(true);
        if(ok && callback != null) {
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
        }else{
            dlg.dismiss();
        }
    }

    private void runDelay(Runnable r, long delay){
        new Handler(Looper.getMainLooper()).postDelayed(r, delay);
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(mMainActivity)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.ok, null)
                .create()
                .show();
    }
}
