package com.xiaomi.mitv.soundbarapp;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;

import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.mitv.soundbar.bluetooth.BTDeviceMonitor;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;

public class SoundBarApplication extends Application {
    private String TAG ="SoundBarApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();

        SoundBarORM.addSetting(this, SoundBarORM.TryConnectCount, "2");

        Intent iDataIntent = new Intent("com.xiaomi.mitv.idata.server.INTENT_DATA_COLLECTION");
        startService(iDataIntent);

        boolean forcescanbluetooth = false;
        final String boot_start = SoundBarORM.getSettingValue(getApplicationContext(), "boot_start");
        if(boot_start != null && boot_start.equals("1")){
            forcescanbluetooth = true;
            
            Log.d("SoundBarApplication", "will start run soundbar service after 60 seconds, if you want change, please change the databases ");
        }        
        
        //start sound bar service to keep the sound bar connection
        final String value = SoundBarORM.getSettingValue(getApplicationContext(), SoundBarORM.supportMultiConnection);
        if(forcescanbluetooth || (value != null && value.equals("1"))){
            
            //one minutes later
            final int boot_delay_seconds_count = SoundBarORM.getIntValue(getApplicationContext(), SoundBarORM.boot_delay_seconds_count, 80);
        }

        BTDeviceMonitor.listen(getApplicationContext());

        XiaomiUpdateAgent agent = new XiaomiUpdateAgent();
    }
    
    BroadcastReceiver mReceiver ;
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        try{
            unregisterReceiver(mReceiver );
        }catch(Exception ne){}
    }
}
