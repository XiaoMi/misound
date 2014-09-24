package com.xiaomi.mitv.soundbarapp.faq;

import android.content.Context;
import com.xiaomi.mitv.content.FaqData;
import com.xiaomi.mitv.soundbarapp.provider.DataProvider;
import com.xiaomi.mitv.utils.IOUtil;

import java.io.*;

/**
 * Created by chenxuetong on 7/3/14.
 *
 * [
 *     {
 *         id:12111,
 *         title:q1.
 *         content:abcd
 *     },
 *     {
 *         id:22111,
 *         title:q2.
 *         content:abcd
 *     }
 * ]
 *
 */
public class FaqDataStore {
    private static final String ASSETS_DEFAULT_DATA_FILE = "faq/faq_list";
    private static final String QA_DATA_FILE_NAME = "faq_list";

    private final Context mContext;

    public FaqDataStore(Context context) {
        mContext = context;
    }


    public FaqData load() {
        InputStream input = null;
        try {
            input = getDataInputStream();
            return FaqData.loadString(new String(IOUtil.readInputAsBytes(input), "utf-8"));
        }catch (IOException e){
            return null;
        } finally {
            if(input!=null)try {input.close();}catch (IOException e){}
        }
    }

    public FaqData loadRemote() {
        DataProvider provider = new DataProvider();
        FaqData data = provider.queryFaqList();

        if(data != null) {
            File dataFile = new File(mContext.getFilesDir().getAbsolutePath() + "/" + QA_DATA_FILE_NAME);
            if (dataFile.exists()){
                dataFile.delete();
            }
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(dataFile);
                fout.write(data.toString().getBytes("utf-8"));
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(fout!=null) try{fout.close();}catch (Exception e){}
            }
        }
        return data;
    }

    public void refresh() {
        loadRemote();
    }


    private InputStream getDataInputStream() throws IOException {
        File dataFile = new File(mContext.getFilesDir().getAbsolutePath()+ "/"+QA_DATA_FILE_NAME);
        if(dataFile.exists()){
            return new FileInputStream(dataFile);
        }else{
            return mContext.getAssets().open(ASSETS_DEFAULT_DATA_FILE);
        }
    }
}
