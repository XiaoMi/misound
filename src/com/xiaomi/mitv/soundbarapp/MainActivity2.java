package com.xiaomi.mitv.soundbarapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbarapp.fragment.PanelFragment;
import com.xiaomi.mitv.soundbarapp.upgrade.UpgradeFragment;

/**
 * Created by chenxuetong on 8/20/14.
 */
public class MainActivity2 extends FragmentActivity implements View.OnClickListener, PanelFragment.InitListener {
    private static final String PANEL_TAG = "panel";
    private static final String UPGRADE_TAG = "upgarde";

    private View mEQEntry;
    private View mSettings;
    private View mUpgrade;
    private View mDiagnosis;

    private PanelFragment mPanel;
    private boolean mNoSourceNotified = false;
    public static void go(Context context){
        Intent i = new Intent(context, MainActivity2.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_main);

        mNoSourceNotified = false;
        Fragment f = getSupportFragmentManager().findFragmentByTag(PANEL_TAG);
        if(f==null){
            mPanel = new PanelFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_panel, mPanel, PANEL_TAG)
                    .commit();
        }else{
            mPanel = (PanelFragment)f;
        }
        mPanel.setInitListener(this);

        Fragment upgrade = getSupportFragmentManager().findFragmentByTag(UPGRADE_TAG);
        if(upgrade==null){
            upgrade = new UpgradeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_entry_upgrade, upgrade, UPGRADE_TAG)
                    .commit();
        }
        buildUi();

        XiaomiUpdateAgent.update(this);
    }

    private void buildUi(){
        mSettings = findViewById(R.id.main_entry_settings);
        mSettings.setOnClickListener(this);
        mEQEntry = findViewById(R.id.main_entry_eq);
        mEQEntry.setOnClickListener(this);
        mDiagnosis = findViewById(R.id.main_entry_diagnose);
        mDiagnosis.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_entry_upgrade:
                break;
            case R.id.main_entry_settings:
                onSettings();
                break;
            case R.id.main_entry_eq:
                onEqEntry();
                break;
            case R.id.main_entry_diagnose:
                WrapperActivity.go(this, WrapperActivity.FRAGMENT_DIAGNOSIS);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new DefaultMisoundDevice(this).release();
        A2dpProfile.close();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void showAlert(String title, String msg, final boolean exit) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (exit) finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onPanelInitFinished() {
        if(mPanel!=null && !mNoSourceNotified){
            if(!mPanel.supportNewUi()) {
                mNoSourceNotified = true;
                String msg = "音响音效和设置功能需要固件4.0.4以上版本！";
                mSettings.setEnabled(false);
                showAlert("提示", msg, false);
                mEQEntry.setEnabled(false);
            }else {
                mNoSourceNotified = true;
                mSettings.setEnabled(true);
                mEQEntry.setEnabled(true);
            }
        }
    }

    public void onSettings(){
        if(mSettings.isEnabled()) {
            boolean sourceReady = mPanel.isSourceReady();
            WrapperActivity.go(this, WrapperActivity.FRAGMENT_SETTINGS, sourceReady);
        }
    }

    public void onEqEntry(){
        if(mEQEntry.isEnabled()) {
            if (mPanel.isSourceReady()) {
                WrapperActivity.go(this, WrapperActivity.FRAGMENT_EQ);
            } else {
                Toast.makeText(this, "请在播放时调节音效！", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onFirmwareUpgrading(boolean enabled) {
        mSettings.setEnabled(enabled);
        mEQEntry.setEnabled(enabled);
        mPanel.enableVolControl(enabled);
    }
}
