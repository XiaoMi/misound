package com.xiaomi.mitv.soundbarapp.provider;

import com.xiaomi.mitv.content.FirmwareVersion;
import com.xiaomi.mitv.content.GalaxyProvider;
import com.xiaomi.mitv.content.FaqData;

import java.util.*;

/**
 * Created by chenxuetong on 6/25/14.
 */
public class DataProvider {
    private GalaxyProvider mProvider = new GalaxyProvider();
    public List<FirmwareVersion> listVersion(){
        return mProvider.listVersion();
    }

    public FirmwareVersion queryNewVersion(int localVersion){
        return mProvider.queryNewVersion(localVersion);
    }

    public FaqData queryFaqList(){
        return mProvider.queryFaqList();
    }


    //check if the app run in dev mode via the config file on SDCARD
    private boolean isInDevMode(){
        return mProvider.isInDevMode();
    }
}
