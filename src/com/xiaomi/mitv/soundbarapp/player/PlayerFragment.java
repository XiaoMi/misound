package com.xiaomi.mitv.soundbarapp.player;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaomi.mitv.soundbar.bluetooth.A2dpProfile;
import com.xiaomi.mitv.soundbarapp.MainActivity2;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.WrapperActivity;
import com.xiaomi.mitv.soundbarapp.fragment.A2dpDlg;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.soundbarapp.util.ConfirmActivityDlg;
import com.xiaomi.mitv.soundbarapp.util.Worker;

/**
 * Created by chenxuetong on 9/22/14.
 */
public class PlayerFragment extends BaseFragment implements View.OnClickListener {
    private static final int REFRESH = 1;
    private static final int QUIT = 2;
    private static final int GET_ALBUM_ART = 3;
    private static final int ALBUM_ART_DECODED = 4;
    private View mMainView;
    private ImageView mAlbum;
    private TextView mTraceName;
    private TextView mArtist;
    private ImageView mPause;
    private ImageView mNext;
    private ProgressBar mSeeker;
    private TextView mCurrentTime;
    private TextView mTotalTime;

    private long mDuration;
    private boolean mPaused = false;
    private Player mPlayer = new Player();
    private Worker mDeamonWorker;
    private AlbumArtHandler mAlbumArtHandler;
    private OnPlayerStateListener mStateListener;

    public interface OnPlayerStateListener{
        public void onMusicPlayState(boolean playing);
        public void onMusicChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDeamonWorker = new Worker("album art worker");
        mAlbumArtHandler = new AlbumArtHandler(mDeamonWorker.getLooper());

        mMainView = inflater.inflate(R.layout.player_layout, container, false);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAlbum = findViewbyId(R.id.player_album_logo);
        mTraceName = findViewbyId(R.id.player_trace_name);
        mArtist = findViewbyId(R.id.player_artist);
        mPause = findViewbyId(R.id.player_pause);
        mPause.setOnClickListener(this);
        mNext = findViewbyId(R.id.player_next);
        mNext.setOnClickListener(this);
        mSeeker = findViewbyId(R.id.player_seeker);
//        mSeeker.setOnSeekBarChangeListener(mSeekListener);
        mSeeker.setMax(1000);
        mSeeker.setProgress(0);
        ((ImageView)findViewbyId(R.id.player_list)).setOnClickListener(this);
        mCurrentTime = findViewbyId(R.id.player_seeker_time);
        mTotalTime = findViewbyId(R.id.player_seeker_duration);

        mPlayer.init(getActivity(), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                updateTraceInfo();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        });

        mMainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isReadyToPlay()){
                    showConnectConform();
                }else{
                    if(getActivity() instanceof MainActivity2)
                        WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_PLAYLIST);
                }
            }
        });

        //show list only in MainActivity
        Activity activity = getActivity();
        if(!(activity instanceof MainActivity2)){
            ((View)findViewbyId(R.id.player_list_container)).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        mPaused = false;
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(PlayerService.META_CHANGED);
        f.addAction(PlayerService.QUEUE_CHANGED);
        f.addAction(PlayerService.PLAYSTATE_CHANGED);
        getActivity().registerReceiver(mTrackListListener, f);

        updateTraceInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }

    @Override
    public void onPause() {
        mPaused = true;
        mHandler.removeMessages(REFRESH);
        getActivity().unregisterReceiver(mTrackListListener);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPlayer.unInit();
        mDeamonWorker.quit();
    }

    public void setStateListener(OnPlayerStateListener l){
        mStateListener = l;
    }

    private void queueNextRefresh(long delay) {
        if (!mPaused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    public void stopMusic(){
        if(mPlayer.isPlaying()) mPlayer.pause();
    }

    private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PlayerService.META_CHANGED)) {
                updateTraceInfo();
                queueNextRefresh(1);
                if(mStateListener!=null) mStateListener.onMusicChanged();
            }else if (action.equals(PlayerService.PLAYSTATE_CHANGED)) {
                updatePlayPauseIcon();
                if(mStateListener!=null) mStateListener.onMusicPlayState(mPlayer.isPlaying());
            }
        }
    };

    private void updateTraceInfo(){
        if(mPlayer.isMusicLoaded()) {
            long songid = mPlayer.getCurrentAudioId();
            long albumid = mPlayer.getCurrentAlbumId();
            String traceName = mPlayer.getCurrentTraceName();
            String artistName = mPlayer.getCurrentArtistName();
            if(traceName == null) traceName = "未命名歌曲";
            if(artistName == null) artistName = "未知歌手";
            mTraceName.setText(traceName);
            mArtist.setText(artistName);
            mAlbumArtHandler.removeMessages(GET_ALBUM_ART);
            mAlbumArtHandler.obtainMessage(GET_ALBUM_ART, new AlbumSongIdWrapper(albumid, songid)).sendToTarget();
            mDuration = mPlayer.getCurrentTraceDuration();
            mTotalTime.setText(MusicUtils.makeTimeString(getActivity(), mDuration / 1000));
        }
    }

    @Override
    public void onClick(View v) {
        if(isReadyToPlay()) {
            switch (v.getId()) {
                case R.id.player_next:
                    mPlayer.next();
                    break;
                case R.id.player_pause:
                    boolean playing = mPlayer.isPlaying();
                    if (playing) {
                        mPlayer.pause();
                    } else {
                        mPlayer.play();
                    }
                    updatePlayPauseIcon();
                    break;
                case R.id.player_list:
                    WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_PLAYLIST);
                    break;
            }
        }else{
            showConnectConform();
        }
    }

    private boolean isReadyToPlay(){
        Activity container = getActivity();
        if(container instanceof MainActivity2) {
            MainActivity2 activity = (MainActivity2) getActivity();
            return activity.isPhoneConnected2Bar() || !activity.supportNewUi();
        }
        return true;
    }

    private void showConnectConform(){
        ConfirmActivityDlg.show(getActivity(), R.string.play_precondition, R.string.player_switch, new ConfirmActivityDlg.onAction() {
            @Override
            public void onConfirmed(boolean yes) {
                final MainActivity2 context = (MainActivity2)getActivity();
                A2dpDlg dlg = new A2dpDlg(context, new A2dpProfile(context));
                dlg.show(new Runnable() {
                    @Override
                    public void run() {
                        mPlayer.playRandom(context, null);
                        context.refreshDeviceInfoDelay(0);
                    }
                }, true);
            }
        });
    }

    private void updatePlayPauseIcon(){
        boolean playing = mPlayer.isPlaying();
        if(playing) {
            mPause.setImageResource(R.drawable.ic_media_pause);
        }else{
            mPause.setImageResource(R.drawable.ic_media_play);
        }
    }

    private long refreshNow() {
        if(!mPlayer.isReady())
            return 500;
        long pos = mPosOverride < 0 ? mPlayer.position() : mPosOverride;
        if ((pos >= 0) && (mDuration > 0)) {
            mCurrentTime.setText(MusicUtils.makeTimeString(getActivity(), pos / 1000));
            int progress = (int) (1000 * pos / mDuration);
            mSeeker.setProgress(progress);

            if (mPlayer.isPlaying()) {
//                mCurrentTime.setVisibility(View.VISIBLE);
            } else {
                // blink the counter
                int vis = mCurrentTime.getVisibility();
//                mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                return 500;
            }
        } else {
            mCurrentTime.setText("--:--");
            mSeeker.setProgress(0);
        }
        // calculate the number of milliseconds until the next full second, so
        // the counter can be updated at just the right time
        long remaining = 1000 - (pos % 1000);

        // approximate how often we would need to refresh the slider to
        // move it smoothly
        int width = mSeeker.getWidth();
        if (width == 0) width = 320;
        long smoothrefreshtime = mDuration / width;

        if (smoothrefreshtime > remaining) return remaining;
        if (smoothrefreshtime < 20) return 20;
        return smoothrefreshtime;
    }

//    private long mStartSeekPos = 0;
//    private long mLastSeekEventTime;
    private long mPosOverride = -1;
//    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
//        private boolean mFromTouch = false;
//        public void onStartTrackingTouch(SeekBar bar) {
//            mLastSeekEventTime = 0;
//            mFromTouch = true;
//        }
//        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
//            if (!fromuser || (!mPlayer.isReady())) return;
//            long now = SystemClock.elapsedRealtime();
//            if ((now - mLastSeekEventTime) > 250) {
//                mLastSeekEventTime = now;
//                mPosOverride = mDuration * progress / 1000;
//                mPlayer.seek(mPosOverride);
//                // trackball event, allow progress updates
//                if (!mFromTouch) {
//                    refreshNow();
//                    mPosOverride = -1;
//                }
//            }
//        }
//        public void onStopTrackingTouch(SeekBar bar) {
//            mPosOverride = -1;
//            mFromTouch = false;
//        }
//    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALBUM_ART_DECODED:
                    mAlbum.setImageBitmap((Bitmap) msg.obj);
                    mAlbum.getDrawable().setDither(true);
                    break;

                case REFRESH:
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
            }
        }
    };

    public class AlbumArtHandler extends Handler {
        private long mAlbumId = -1;

        public AlbumArtHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {
            long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
            long songid = ((AlbumSongIdWrapper) msg.obj).songid;
            if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0)) {
                // while decoding the new image, show the default album art
                Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
                mHandler.removeMessages(ALBUM_ART_DECODED);
                mHandler.sendMessageDelayed(numsg, 300);
                // Don't allow default artwork here, because we want to fall back to song-specific
                // album art if we can't find anything for the album.
                Bitmap bm = MusicUtils.getArtwork(getActivity(), songid, albumid, false);
                if (bm == null) {
                    bm = MusicUtils.getArtwork(getActivity(), songid, -1);
                    albumid = -1;
                }
                if (bm != null) {
                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, bm);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessage(numsg);
                }
                mAlbumId = albumid;
            }
        }
    }

    private static class AlbumSongIdWrapper {
        public long albumid;
        public long songid;
        AlbumSongIdWrapper(long aid, long sid) {
            albumid = aid;
            songid = sid;
        }
    }
}
