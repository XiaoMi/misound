package com.xiaomi.mitv.soundbarapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.mitv.soundbar.DefaultMisoundDevice;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbar.protocol.TraceInfo0x816;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.fragment.MainEntryFragment;
import com.xiaomi.mitv.soundbarapp.fragment.PanelFragment;
import com.xiaomi.mitv.soundbarapp.player.PlayerFragment;

/**
 * Created by chenxuetong on 8/20/14.
 */
public class MainActivity2 extends FragmentActivity implements PanelFragment.PanelListener, PlayerFragment.OnPlayerStateListener{
    private static final String PANEL_TAG = "panel";
    private static final String ENTRY_WITH_PLAYER_TAG = "entry_player";
    private static final String ENTRY_TAG = "entry";

    private PanelFragment mPanel;
    private MainEntryFragment mEntries;
    private boolean mNoSourceNotified = false;
    private boolean mPhoneConnected2Bar = false;
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

        mEntries = showDefaultEntries(false);

        XiaomiUpdateAgent.update(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        A2dpProfile.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new DefaultMisoundDevice(this).release();
        A2dpProfile.close();
//        android.os.Process.killProcess(android.os.Process.myPid());
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
    public void onPanelRefreshed(TraceInfo0x816 info) {
        mEntries = setupEntryFragment(info);
        mEntries.setSourceReady(BarInfoUtils.haveSource(info));

//        if(!isPhoneConnected2Bar()){
//            Fragment player = getSupportFragmentManager().findFragmentByTag(ENTRY_WITH_PLAYER_TAG);
//            if(player != null) {
//                PlayerFragment playerFragment = (PlayerFragment) player;
//                playerFragment.stopMusic();
//            }
//        }
    }

    @Override
    public void onMusicPlayState(boolean playing) {
        refreshDeviceInfoDelay(5000);
    }

    @Override
    public void onMusicChanged() {}

    public boolean isPhoneConnected2Bar(){
        return mPhoneConnected2Bar;
    }

    private MainEntryFragment setupEntryFragment(TraceInfo0x816 info){
        A2dpProfile thisPhone = new A2dpProfile(this);
        BluetoothDevice bar = A2dpProfile.getBarDevice(this);
        mPhoneConnected2Bar = BarInfoUtils.isA2dpConnected(info) && thisPhone.isConnected(bar);

        if(mPanel!=null && !mNoSourceNotified){
            if(!mPanel.supportNewUi()) {
                mNoSourceNotified = true;
                mEntries.enableSettings(false);
                mEntries.enableEq(false);

                String msg = "音响音效和设置功能需要固件4.0.4以上版本！";
                showAlert("提示", msg, false);
            }else {
                mEntries.enableSettings(true);
                mEntries.enableEq(true);
            }
        }
        return mEntries;
    }

    public MainEntryFragment showDefaultEntries(boolean stop){
        Fragment player = getSupportFragmentManager().findFragmentByTag(MainEntryFragment.PLAYER_TAG);
        if(player != null) {
            PlayerFragment playerFragment = (PlayerFragment) player;
            if (stop) playerFragment.stopMusic();
        }

        Fragment enties = getSupportFragmentManager().findFragmentByTag(ENTRY_TAG);
        if(enties==null){
            enties = new MainEntryFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_entry_container, enties, ENTRY_TAG)
                    .commit();
        }
        return (MainEntryFragment)enties;
    }

    public void onFirmwareUpgrading(boolean enabled) {
        mEntries.enableSettings(enabled);
        mEntries.enableEq(enabled);
        mPanel.enableVolControl(enabled);
    }

    public void showEq(){
        if(mEntries != null){
            mEntries.showEq();
        }
    }

    public void showSettings(){
        if(mEntries != null){
            mEntries.showSettings();
        }
    }

    public boolean supportNewUi(){
        String ver = SoundBarORM.getSettingValue(this, SoundBarORM.dfuCurrentVersion);
        return ver.compareTo("4.0.4")>=0;
    }

    public boolean supportSource(){
        String ver = SoundBarORM.getSettingValue(this, SoundBarORM.dfuCurrentVersion);
        return ver.compareTo("4.0.4")>=0;
    }

    public void refreshDeviceInfoDelay(long delay){
        mPanel.refreshBarPanel(delay);
    }
}
