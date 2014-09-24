package com.xiaomi.mitv.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 8/18/14.
 */
public class RoundAnimationView extends View {
    private static final String TAG = "miwidget";
    private int mStepGap;
    private int mBeginTransparent;
    private int mEndTransparent;
    private int mColor;
    private int mSteps;
    private Drawable mImage;
    private boolean mAnimationStopped = true;

    public RoundAnimationView(Context context) {
        this(context, null);
    }

    public RoundAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundAnimationView);
        mColor = typedArray.getColor(R.styleable.RoundAnimationView_color, Color.WHITE);
        mStepGap = typedArray.getInt(R.styleable.RoundAnimationView_stepGap, 30);
        mBeginTransparent = typedArray.getInt(R.styleable.RoundAnimationView_beginTransparent, 100);
        mEndTransparent = typedArray.getInt(R.styleable.RoundAnimationView_endTransparent, 20);
        mImage = typedArray.getDrawable(R.styleable.RoundAnimationView_img);
//        mImageWidth = (int)typedArray.getDimension(R.styleable.RoundAnimationView_imgWidth, 48);
//        mImageHeight = (int)typedArray.getDimension(R.styleable.RoundAnimationView_imgHeight, 48);
        typedArray.recycle();
        mSteps = -1;
    }

    public void setImage(int rid){
        Drawable d = getContext().getResources().getDrawable(rid);
        if(d != null){
            mImage = d;
        }
        postInvalidate();
    }

    public void startAnimation(){
        mAnimationStopped = false;
    }

    public void stopAnimation(){
        mAnimationStopped = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        doAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int sc = canvas.save();

        if(!mAnimationStopped) {
            int w = getDrawWidth();
            int h = getDrawHeight();

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
            canvas.translate(getWidth() - w, getHeight() - h);
            for (int step = 0; step <= mSteps; step++) {
                int radian = makeRadianForStep(step);
                paint.setColor(makeColor(step));
                canvas.drawCircle(getCenterX(), getCenterY(), radian, paint);
            }
        }

        //draw center icon
        if(mImage != null){
            int imgW = mImage.getIntrinsicWidth();
            int imgH = mImage.getIntrinsicHeight();
            canvas.translate(getCenterX()-imgW/2, getCenterY()-imgH/2);
            mImage.setBounds(new Rect(0,0,imgW, imgH));
            mImage.draw(canvas);
        }

        canvas.restoreToCount(sc);
    }

    private void doAnimation(){
        int radian = makeRadianForStep(mSteps);
        if(radian>getMaxRadian()/2){
            mSteps = -1;
        }else {
            mSteps++;
        }
        invalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                doAnimation();
            }
        }, 400);
    }

    private int makeColor(int step){
        int steps = getMaxRadian()/4/ mStepGap;
        int alpha = 255 * (Math.max(mBeginTransparent, mEndTransparent) - step * Math.abs(mBeginTransparent-mEndTransparent)/steps)/100;
        if(alpha <= mEndTransparent) alpha=mEndTransparent;
        return Color.argb(alpha, Color.red(mColor), Color.green(mColor), Color.blue(mColor));
    }

    private int getCenterX(){
        return getWidth()/2;
    }
    private int getCenterY(){
        return getHeight()/2;
    }

    private int makeRadianForStep(int step){
        int imgW = mImage.getIntrinsicWidth();
        int imgH = mImage.getIntrinsicHeight();
        int baseRadian = (int)Math.sqrt(Math.pow(imgH/2, 2) + Math.pow(imgW/2, 2))+5;
        return (int)(mStepGap*step*Math.pow(1.1,step)) + baseRadian;
    }

    private int getMaxRadian(){
        return (int)(getDrawWidth()*Math.sqrt(2));
    }

    private int getDrawWidth(){
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getDrawHeight(){
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }
}
