
package com.xiaomi.mitv.soundbarapp.upgrade;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.csr.gaia.android.library.gaia.Gaia;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mitv.soundbar.api.IDFUUpdate;
import com.xiaomi.mitv.soundbar.api.ISoundBarStateTracker;
import com.xiaomi.mitv.soundbarapp.R;

import java.io.File;
import java.io.FilenameFilter;

public class UpdateActivity extends Activity {

    Button begin_update_service, bind_service,start_bind_service, fetch_gaia_version;
    
    
    final String TAG = "DFU UpdateActivity";
    ProgressBar pb;
    EditText    log_service;
    CheckBox    check_showlog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        
        begin_update_service = (Button)findViewById(R.id.begin_update_service);
        begin_update_service.setOnClickListener(selectDFUClick);
        
        bind_service         = (Button)findViewById(R.id.bind_service);
        bind_service.setOnClickListener(bindClick);
        
        start_bind_service  = (Button)this.findViewById(R.id.start_bind_service);
        start_bind_service.setOnClickListener(selectDFUClick);
        
        fetch_gaia_version = (Button)this.findViewById(R.id.fetch_gaia_version);
        fetch_gaia_version.setOnClickListener(fetchVersionClick);
        
        Button close_connection = (Button)this.findViewById(R.id.close_connection);
        close_connection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                try {
//                } catch (RemoteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
        });

        check_showlog = (CheckBox)this.findViewById(R.id.check_showlog);
        
        log_service = (EditText)this.findViewById(R.id.log_service);
               
        
        pb = (ProgressBar)findViewById(R.id.update_progress);      
        
        this.setTitle("DFU update TEST");
    }    
    
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
    View.OnClickListener fetchVersionClick = new View.OnClickListener() {        
        @Override
        public void onClick(View v) {
//            if(dfuUpdate != null){
//                try {
//                    dfuUpdate.requestModuleVersion();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//         }
        }
    };
    
    View.OnClickListener bindClick = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            //bindService
            Intent intent = new Intent("com.xiaomi.mitv.soundbar.ACTION_DFU_UPDATE");
            bindService(intent, sc,  Context.BIND_AUTO_CREATE);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update, menu);
        return true;
    }
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent intent = null;
//        switch(item.getItemId()){
//            case R.id.action_soundbar:
//                intent = new Intent(UpdateActivity.this, SoundBarActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.action_feedback:
//            	Util.startFeedback( UpdateActivity.this);
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener selectDFUClick = new View.OnClickListener() {        
        @Override
        public void onClick(final View v) {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File[] files = downloadDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".dfu");
                }
            });
            if (files == null) {
                Toast.makeText(UpdateActivity.this, "No *.dfu files found in Downloads folder", Toast.LENGTH_LONG).show();
                return;
            }

            final CharSequence[] items = new CharSequence[files.length];
            int i = 0;
            for (File f : files) {
                items[i++] = f.getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
            builder.setTitle("Select a DFU file")

                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if(v.getId() == R.id.begin_update_service){
                                if(dfuUpdate != null){
                                    try {
                                        //dfuUpdate.requestDFUUpdate(files[item].getAbsolutePath(), "10.0.5", check_showlog.isChecked());
                                        dfuUpdate.requestDFUUpdate(files[item].getAbsolutePath(), "", check_showlog.isChecked());
                                    } catch (RemoteException e) {
                                        Log.d(TAG, "fail to call requestDFUUpdate");
                                        e.printStackTrace();
                                    }
                                }
                            }else if(v.getId() == R.id.start_bind_service){
                                Intent intent = new Intent("com.xiaomi.mitv.soundbar.ACTION_DFU_UPDATE");
                                intent.putExtra("ACTION_PATH", files[item].getAbsolutePath());
                                intent.putExtra("showlog", check_showlog.isChecked());
                                startService(intent);
                            }
                        }
                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();


        }
    };    
    
    ISoundBarStateTracker stateTracker = new ISoundBarStateTracker.Stub() {      
        @Override
        public void onState(int state, String msg) throws RemoteException {
            log("state="+state + " msg="+msg);
        }
        
        @Override
        public void onProgress(int steps) throws RemoteException {
            uiHandler.obtainMessage(0, steps).sendToTarget();;
        }

        @Override
        public void log(String msg) throws RemoteException {
            uiHandler.obtainMessage(1, msg).sendToTarget();
        }

        @Override
        public void onCommand(int command_id, String result, boolean suc)
                throws RemoteException {
            switch(command_id){
                case Gaia.COMMAND_GET_APPLICATION_VERSION:
                    log("COMMAND_GET_APPLICATION_VERSION = "+result);
                    break;
                case Gaia.COMMAND_CHANGE_VOLUME:
                    log("COMMAND_CHANGE_VOLUME  called return "+result);
                    break;
            }            
        }

        @Override
        public void connected() throws RemoteException {
            
        }

        @Override
        public void disConnected() throws RemoteException {
                        
        }

        @Override
        public void deviceFounded(boolean got) throws RemoteException {
                        
        }
    };
    
    Handler uiHandler = new Handler(){

        @Override
        public void dispatchMessage(Message msg) {
            switch(msg.what){
                case 0:{
                    pb.setProgress((Integer)(msg.obj));
                    break;
                }
                case 1:{
                    log((String)(msg.obj));
                    break;
                }
            }
            super.dispatchMessage(msg);
        }        
    };
    
    
    
    IDFUUpdate dfuUpdate;
    ServiceConnection sc = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dfuUpdate = IDFUUpdate.Stub.asInterface(service);
            log("bind successfully");
            
            try {
                dfuUpdate.registerStateTracker(stateTracker);
            } catch (RemoteException e) {                
                e.printStackTrace();
            }
            
            bind_service.setEnabled(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                        
        }        
    };

    StringBuilder sb = new StringBuilder();
    private void log(String msg){
        Log.d(TAG, msg);
        
        sb.insert(0,  "\n");
        sb.insert(0, msg);
        log_service.setText(sb.toString());        
        
    }
    @Override
    protected void onDestroy() {        
        super.onDestroy();    
        try {
            if(dfuUpdate != null)
            dfuUpdate.unRegisterStateTracker();
        } catch (RemoteException e) {            
            e.printStackTrace();
        }
        try{
            unbindService(sc);
        }catch(Exception ne){}
    }
}
