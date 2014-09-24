package com.xiaomi.mitv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.utils.Log;

/**
 * Created by chenxuetong on 8/26/14.
 */
public class MiSwitch extends RelativeLayout {
    private static final String TAG = "MiSwitch";
    private ImageView mLeft;
    private ImageView mRight;
    private boolean mChecked= false;
    private OnCheckChangedListener mCheckListener;

    public interface OnCheckChangedListener{
        public void onChanged(boolean checked);
    }

    public MiSwitch(Context context) {
        this(context, null);
    }

    public MiSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View layout = View.inflate(getContext(), R.layout.settings_switcher_view, null);
        mLeft = (ImageView)layout.findViewById(R.id.left);
        mRight = (ImageView)layout.findViewById(R.id.right);
        addView(layout);

        setOnClickListener(mListener);
        updateView();
    }

    public void setOnCheckChangedListener(OnCheckChangedListener l){
        mCheckListener = l;
    }

    public void setChecked(boolean checked){
        mChecked = checked;
        updateView();
    }

    public boolean isChecked(){
        return mChecked;
    }

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.logD(TAG, "before onClick() on: " + v);
            boolean checked = mChecked;
            mChecked = !mChecked;
            if(checked != mChecked){
                updateView();
                if(mCheckListener!=null) mCheckListener.onChanged(mChecked);
            }
            Log.logD(TAG, "after onClick() checked: " + mChecked);
        }
    };

    private void updateView(){
        if(mChecked){
            mLeft.setVisibility(GONE);
            mRight.setVisibility(VISIBLE);
        }else{
            mLeft.setVisibility(VISIBLE);
            mRight.setVisibility(GONE);
        }
    }
}
