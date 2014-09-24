package com.xiaomi.mitv.soundbarapp.upgrade;

import android.content.Context;
import android.util.Log;
import com.xiaomi.mitv.content.FirmwareVersion;
import com.xiaomi.mitv.soundbarapp.provider.DataProvider;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;

import java.io.*;
import java.net.*;

/**
 * Created by chenxuetong on 6/24/14.
 * the functions in this class are synchronized call
 */
public class FirmwareManager {
    private static final String TAG = "Soundbar_firmware";
    private static final int BUFFER_SIZE = 1024;
    private Context mContext;

    public interface ProgressListener{
        void onProgress(int total, int finished);
    }

    public FirmwareManager(Context context){
        mContext = context;
    }
    //check if the firmware need to upgrade, if true return the Version info,
    //otherwise null
    public FirmwareVersion getNewFirmware(){
        DataProvider provider = new DataProvider();
        return provider.queryNewVersion(getLocalVersionCode());
    }

    public int getLocalVersionCode(){
        return SoundBarORM.getIntValue(mContext, SoundBarORM.dfuCurrentVersionCode, -1);
    }

    public String download(FirmwareVersion version, ProgressListener listener){
        String localFile = null;
        if(version != null){
            boolean downloadOk = true;
            try {
//                if(listener!=null) listener.onBeingDownload(version.size);
                localFile = downloadDFU(version.url, version, listener);
                File savedFile = new File(mContext.getFilesDir(), localFile);
                if(!savedFile.exists()){
                    return null;
                }
                localFile = savedFile.getAbsolutePath();
//                if(listener!=null) listener.onEndDownload(true);
            } catch (MalformedURLException e){
                downloadOk = false;
                Log.e(TAG, "Bad duf url: " + version.url);
                e.printStackTrace();
            } catch (IOException e) {
                downloadOk = false;
                Log.e(TAG, "Failed to download duf firmware file!");
                e.printStackTrace();
            } catch (Throwable t){
                downloadOk = false;
                Log.e(TAG, "Failed to download duf firmware file!");
                t.printStackTrace();
            }

            if(!downloadOk){
//                if(listener!=null) listener.onEndDownload(false);
                return null;
            }
//            if(listener!=null) listener.onBeingUpgrade();

        }
        return localFile;
    }

    //download the dfu file according to url, and save it as file to return
    private String downloadDFU(String url, FirmwareVersion info, ProgressListener listener) throws IOException{
        URL duf = new URL(url);

        Proxy proxy = null;
        URLConnection connection = null;

        if(proxy != null){
            connection = duf.openConnection(proxy);
        }else{
            connection = duf.openConnection();
        }

        connection.setConnectTimeout(10000); //10s
        InputStream in = connection.getInputStream();

        int totalRead = 0;
        //save duf to a local file
        String localFile = "firmware_" + info.codeName + "_" + info.code + ".bin";
        mContext.deleteFile(localFile);
        FileOutputStream saveTo = mContext.openFileOutput(localFile, Context.MODE_PRIVATE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        while((read=in.read(buffer)) != -1){
            totalRead += read;
            saveTo.write(buffer, 0, read);
            if(read>200 && listener!=null) listener.onProgress(info.size, totalRead);
        }
        if(listener!=null) listener.onProgress(info.size, info.size);
        saveTo.flush();
        saveTo.close();

        String firmwareFile = localFile;
        //try to unzip it
        if(url.toLowerCase().contains(".zip")) {
            FirmwareZipFile pack = new FirmwareZipFile(new File(mContext.getFilesDir().getAbsolutePath(), localFile));
            if(pack.unzip(mContext)) {
                firmwareFile = pack.getDfuFile();
            }
        }
        return firmwareFile;
    }
}
