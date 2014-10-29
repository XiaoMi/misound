package com.xiaomi.mitv.soundbarapp.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 10/9/14.
 */
public class ConfirmActivityDlg extends Activity {
    private static int sMsgResId = -1;
    private static int sOkResId = -1;
    private static onAction sCallback = null;

    public static interface onAction{
        public void onConfirmed(boolean ret);
    }

    public static void show(Activity from, int msgId, onAction callback) {
        sMsgResId = msgId;
        sCallback = callback;
        from.startActivity(new Intent(from, ConfirmActivityDlg.class));
        from.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void show(Activity from, int msgId, int okId, onAction callback) {
        sMsgResId = msgId;
        sCallback = callback;
        sOkResId = okId;
        from.startActivity(new Intent(from, ConfirmActivityDlg.class));
        from.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_layout);
        TextView msgView = (TextView)findViewById(R.id.confirm_alert_msg);
        if(sMsgResId!=-1) msgView.setText(sMsgResId);
        Button ok = (Button)findViewById(R.id.confirm_alert_ok);
        if(sOkResId!=-1) ok.setText(sOkResId);
        findViewById(R.id.confirm_alert_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
        findViewById(R.id.confirm_alert_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConfirmed();
                quit();
            }
        });

        View content = findViewById(R.id.confirm_content);
        Animation in = AnimationUtils.loadAnimation(this, R.anim.slider_in_bottom);
        content.setAnimation(in);
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        View content = findViewById(R.id.confirm_content);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.slider_out_bottom);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                quit();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        content.setAnimation(out);
        content.setVisibility(View.GONE);
    }

    private void doConfirmed() {
        if(sCallback==null) return;
        sCallback.onConfirmed(true);
        sMsgResId = -1;
        sCallback=null;
    }

    private void quit(){
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
