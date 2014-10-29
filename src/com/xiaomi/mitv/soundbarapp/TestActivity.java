package com.xiaomi.mitv.soundbarapp;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.xiaomi.mitv.soundbarapp.player.PlayerFragment;

/**
 * Created by chenxuetong on 9/24/14.
 */
public class TestActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Fragment f = getSupportFragmentManager().findFragmentByTag("player");
        if(f == null){
            f = new PlayerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, f, "player")
                    .commit();
        }
    }
}
