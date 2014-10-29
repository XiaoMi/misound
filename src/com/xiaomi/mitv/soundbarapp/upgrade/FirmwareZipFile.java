package com.xiaomi.mitv.soundbarapp.upgrade;

import android.content.Context;
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by chenxuetong on 6/25/14.
 */
public class FirmwareZipFile {
    private static final String ZIP_DUF_NAME = "firmware.dfu";
    private static final String ZIP_MANIFEST = "Manifest";
    private static final int BUFFER_SIZE = 1024;

    private final File mZipFile;
    private String mDufFile;

    public FirmwareZipFile(File file){
        mZipFile = file;
    }

    public boolean unzip(Context context) {
        try {
            ZipFile zipFile = new ZipFile(mZipFile);
            ZipEntry dfu = zipFile.getEntry(ZIP_DUF_NAME);
            if(dfu != null){
                String dfuFile =  dfu.getName()+dfu.getTime();
                context.deleteFile(dfuFile);

                MessageDigest digester = null;
                try {
                    MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {e.printStackTrace();}

                byte[] buffer = new byte[BUFFER_SIZE];
                FileOutputStream out = context.openFileOutput(dfuFile, Context.MODE_PRIVATE);
                BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
                BufferedInputStream bio = new BufferedInputStream(zipFile.getInputStream(dfu));
                int read = 0;
                while((read=bio.read(buffer, 0, BUFFER_SIZE))!=-1) {
                    bos.write(buffer, 0, read);
                    //md5
                    if(digester!=null){
                        digester.update(buffer, 0, read);
                    }
                }
                bos.flush();
                bos.close();

                //check md5
                if(digester != null) {
//                    byte[] md5 = digester.digest();
//                    Properties manifest = unzipManifest(zipFile, ZIP_MANIFEST);
//                    if(!Base64.encode(md5, Base64.DEFAULT).equalsIgnoreCase(manifest.getProperty("md5"))){
//                        return false;
//                    }
                }
                mDufFile = dfuFile;
            }
        } catch (IOException e) {
            //ignore, it is not a zip file
        }
        return  true;
    }

    public String getDfuFile() {
        return mDufFile;
    }

    Properties unzipManifest(ZipFile zipFile, String name){
        Properties manifest =  new Properties() ;
        ZipEntry entry = zipFile.getEntry(ZIP_MANIFEST);
        if(entry != null) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BufferedInputStream bio = new BufferedInputStream(zipFile.getInputStream(entry));
                byte[] buffer = new byte[BUFFER_SIZE];
                int read = 0;
                while((read=bio.read(buffer)) != -1){
                    out.write(buffer, 0 ,read);
                }
                out.flush();
                JSONObject json = new JSONObject(out.toString("utf-8"));
                manifest.setProperty("build", json.getString("build"));
                manifest.setProperty("hardware", json.getString("hardware"));
                manifest.setProperty("md5", json.getString("md5"));
                manifest.setProperty("version_code", json.getString("version_code"));
                manifest.setProperty("version_name", json.getString("version_name"));
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e) { e.printStackTrace(); }
        }

        return manifest;
    }
}
