package com.xiaomi.mitv.soundbarapp.player;


import android.app.Activity;
import android.content.*;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.mitv.soundbarapp.R;

import java.util.*;

/**
 * Created by chenxuetong on 9/22/14.
 */
public class Player {
    private static final String TAG = "player";
    private static IPlayerService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
    private ServiceToken mToken;
    private Context mContext;
    public Player(){}

    public void init(Activity context, ServiceConnection sc){
        mContext = context;
        mToken = bindToService(context, sc);
    }

    public void unInit(){
        if(mToken != null){
            unbindFromService(mToken);
        }
    }

    public boolean isReady(){
        return sService!=null;
    }

    public void play(long[] list, long id){
        if (sService != null) {
            try {
                int pos = -1;
                for(int i=0; i<list.length; i++){
                    if(list[i]==id) pos = i;
                }
                if(pos != -1){
                    sService.open(list, pos);
                    sService.play();
                }
            } catch (RemoteException ex) {
            }
        }
    }

    public void play(){
        if (sService != null) {
            try {
                if(sService.getQueue().length > 0) {
                    sService.play();
                }else{
                    playRandom(mContext, null);
                }
            } catch (RemoteException ex) {
            }
        }
    }

    public void stop(){
        if (sService != null) {
            try {
                sService.stop();
            } catch (RemoteException ex) {
            }
        }
    }

    public void pause(){
        if (sService != null) {
            try {
                sService.pause();
            } catch (RemoteException ex) {
            }
        }
    }

    public void next(){
        if (sService != null) {
            try {
                sService.next();
            } catch (RemoteException ex) {
            }
        }
    }

    public void prev(){
        if (sService != null) {
            try {
                sService.prev();
            } catch (RemoteException ex) {
            }
        }
    }

    public void playRandom(Context context, long[] songsId){
        long[] songIds = songsId==null?MusicUtils.getAllSongs(context):songsId;
        if(songIds==null || songIds.length == 0){
            Toast.makeText(context, R.string.playlist_no_songs, Toast.LENGTH_LONG).show();
            return;
        }

        long[] list = new long[songIds.length];
        Random random = new Random();
        int left = songIds.length;
        for(int i=0; i<list.length; i++){
            int rIndex = random.nextInt(left);
            list[i] = songIds[rIndex];
            songIds[rIndex] = songIds[left-1];
            --left;
        }

        open(list, 0);
        play();
    }

    public void sendCmd(Context context, String cmd){
        Intent i = new Intent(PlayerService.SERVICECMD);
        i.setClass(context, PlayerService.class);
        i.putExtra(PlayerService.CMDNAME, cmd);
        context.startService(i);
    }

    public void open(long[] list, int pos){
        if (sService != null) {
            try {
                sService.open(list, pos);
            } catch (RemoteException ex) {}
        }
    }

    public String getCurrentArtistName() {
        if (sService != null) {
            try {
                return sService.getArtistName();
            } catch (RemoteException ex) {}
        }
        return null;
    }

    public String getCurrentTraceName() {
        if (sService != null) {
            try {
                return sService.getTrackName();
            } catch (RemoteException ex) {}
        }
        return null;
    }

    public String getCurrentAlbumName() {
        if (sService != null) {
            try {
                return sService.getAlbumName();
            } catch (RemoteException ex) {}
        }
        return null;
    }

    public long getCurrentAlbumId(){
        if (sService != null) {
            try {
                return sService.getAlbumId();
            } catch (RemoteException ex) {}
        }
        return -1;
    }

    public long getCurrentAudioId() {
        if (sService != null) {
            try {
                return sService.getAudioId();
            } catch (RemoteException ex) {}
        }
        return -1;
    }


    public int getCurrentShuffleMode() {
        int mode = PlayerService.SHUFFLE_NONE;
        if (sService != null) {
            try {
                mode = sService.getShuffleMode();
            } catch (RemoteException ex) {}
        }
        return mode;
    }

    public void togglePartyShuffle() {
        if (sService != null) {
            int shuffle = getCurrentShuffleMode();
            try {
                if (shuffle == PlayerService.SHUFFLE_AUTO) {
                    sService.setShuffleMode(PlayerService.SHUFFLE_NONE);
                } else {
                    sService.setShuffleMode(PlayerService.SHUFFLE_AUTO);
                }
            } catch (RemoteException ex) {}
        }
    }

    /*
     * Returns true if a file is currently opened for playback (regardless
     * of whether it's playing or paused).
     */
    public boolean isMusicLoaded() {
        if (sService != null) {
            try {
                return sService.getPath() != null;
            } catch (RemoteException ex) {}
        }
        return false;
    }

    public boolean isPlaying(){
        if (sService != null) {
            try {
                return sService.isPlaying();
            } catch (RemoteException ex) {}
        }
        return false;
    }

    public long getCurrentTraceDuration(){
        if (sService != null) {
            try {
                return sService.duration();
            } catch (RemoteException ex) {}
        }
        return 0;
    }

    public void seek(long pos){
        if (sService != null) {
            try {
                sService.seek(pos);
            } catch (RemoteException ex) {}
        }
    }

    public long position() {
        if (sService != null) {
            try {
                return sService.position();
            } catch (RemoteException ex) {}
        }
        return 0;
    }

    public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    private ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, PlayerService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, PlayerService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        Log.e(TAG, "Failed to bind to service");
        return null;
    }

    private void unbindFromService(ServiceToken token) {
        if (token == null) {
            Log.e("MusicUtils", "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            Log.e("MusicUtils", "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            sService = IPlayerService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }

}
