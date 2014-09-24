package com.xiaomi.mitv.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 8/14/14.
 */
public class RoundSeekBar extends RoundProgressBar {
    private static final String TAG = "miwidget";
    private Drawable mThumbDrawable;
    private Thumb mThumb;
    private OnSeekBarChangeListener mListener;
    private Drawable mSeekingProgressBg;
    private Drawable mSeekingProgrss;

    private Drawable mDefaultProgressBg;
    private Drawable mDefaultProgrss;


    public interface OnSeekBarChangeListener {
        void onSeekBegin();
        void onSeekChanged(RoundSeekBar bar, int progres, boolean fromUser);
        void onSeekEnd();
    }

    public RoundSeekBar(Context context) {
        this(context, null);
    }

    public RoundSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setProgress(0);
        //获取自定义属性和默认值
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundSeekBar);
        mThumbDrawable = typedArray.getDrawable(R.styleable.RoundSeekBar_thumb);
        int width = (int)typedArray.getDimension(R.styleable.RoundSeekBar_thumbWidth, 32);
        int height = (int)typedArray.getDimension(R.styleable.RoundSeekBar_thumbHeight,32);
        mSeekingProgrss = typedArray.getDrawable(R.styleable.RoundSeekBar_progress2);
        mSeekingProgressBg = typedArray.getDrawable(R.styleable.RoundSeekBar_progressBg2);
        mThumb = new Thumb(width, height);
        setBorderOffset(Math.max(mThumb.width, mThumb.height)/2);
        typedArray.recycle();

        mDefaultProgrss = getProgressDrwable();
        mDefaultProgressBg = getProgressBg();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Point thumbPos = getEndPoint();
        mThumb.x = thumbPos.x;
        mThumb.y = thumbPos.y;
        Log.d(TAG, "onSizeChanged() " + w + "," +h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mThumb.seeking && mSeekingProgressBg!=null && mSeekingProgrss!=null){
            setDrawable(mSeekingProgressBg, mSeekingProgrss);
        }else{
            setDrawable(mDefaultProgressBg, mDefaultProgrss);
        }

        int progress = getProgress();
        drawProgress(canvas, progress);

        double angle = progress*360/getMax() - 90;
        mThumb.x = (float)(getCenterX() + getRadian()*Math.cos(angle*Math.PI/180));
        mThumb.y= (float)(getCenterY() + getRadian()*Math.sin(angle*Math.PI/180));

        int sc = canvas.save();
        canvas.translate(mThumb.x-mThumb.width/2, mThumb.y-mThumb.height/2);
        Rect bound = new Rect(0,0,mThumb.width, mThumb.height);
        mThumbDrawable.setBounds(bound);
        mThumbDrawable.draw(canvas);
        canvas.restoreToCount(sc);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return mThumb.checkActionDown(event.getX(), event.getY());
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mThumb.seeking = true;
                return mThumb.try2Move(event.getX(), event.getY());
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return mThumb.checkActionUp();
            }
        }
        return super.onTouchEvent(event);
    }

    public void setThumbDrawableResource(int rid){
        mThumbDrawable = getContext().getResources().getDrawable(rid);
        postInvalidate();
    }
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l){
        mListener = l;
    }

    void printSamples(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        for (int h = 0; h < historySize; h++) {
            Log.d(TAG, String.format("At time %d:", ev.getHistoricalEventTime(h)));
            for (int p = 0; p < pointerCount; p++) {
                Log.d(TAG, String.format("  pointer %d: (%f,%f)",
                        ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h)));
            }
        }
        System.out.printf("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            Log.d(TAG, String.format("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p)));
        }
    }

    private final class Thumb{
        private float x;
        private float y;
        private int width;
        private int height;

        private boolean seeking;

        private Thumb(int w, int h){
            seeking = false;
            x = 0;
            y = 0;
            width = w;
            height = h;
        }

        public boolean try2Move(float nx, float ny){
            if(!seeking){
                return false;
            }
            double angle = Math.atan((ny-getCenterY())/(nx-getCenterX())) * 180/Math.PI;
            if(nx-getCenterX()<0) angle += 180;
//            double len = length(new PointF(getCenterX(), getCenterY()), new PointF(nx, ny));
//            double gap = len - getRadian();
//            x = (float)(nx - gap * Math.cos(angle*Math.PI/180));
//            y = (float)(ny - gap * Math.sin(angle*Math.PI/180));

            int newProgress = (int)(getMax() * (angle+90) / 360);

            //check cycle
            int oldProgress = getProgress();
            if(Math.abs(newProgress-oldProgress) > getMax()/2){
                if(oldProgress>getMax()/2) newProgress = getMax();
                if(oldProgress<getMax()/2) newProgress = 0;
            }
            if(newProgress>=getMax()){
                newProgress = getMax();
            }
            if(newProgress != getProgress()) {
                setProgress(newProgress);
                if (mListener != null) mListener.onSeekChanged(RoundSeekBar.this, newProgress, true);
            }
            return true;
        }

        public boolean checkActionDown(float nx, float ny) {
            RectF rf = getRange();
            if(rf.contains(nx, ny)){
                this.x = nx;
                this.y = ny;
                seeking = true;
                if(mListener!=null) mListener.onSeekBegin();
                return true;
            }
            return false;
        }

        public boolean checkActionUp() {
            if(seeking){
                seeking = false;
                if(mListener!=null) mListener.onSeekEnd();
                postInvalidate();
                return true;
            }
            return false;
        }

        private RectF getRange(){
            return new RectF(x-width/2, y-height/2, x+width/2, y+height/2);
        }

        private double length(PointF a, PointF b){
            return Math.sqrt(Math.pow((a.x-b.x), 2) + Math.pow((a.y-b.y), 2));
        }
    }
}
