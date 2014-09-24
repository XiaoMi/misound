package com.xiaomi.mitv.widget;

/**
 * Created by chenxuetong on 8/14/14.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.util.AttributeSet;
import android.view.View;
import com.xiaomi.mitv.soundbarapp.R;

public class RoundProgressBar extends View {
    private Paint mPaint;
    private int textColor;
    private float mTextSize;
    private int mMax;
    private int mProgress;
    private Drawable mProgressDrawable;
    private Drawable mBackgroundDrawable;
    private Bitmap mProgressCanvasCache;
    private int mBorderOffset = 0;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mPaint.setFilterBitmap(false);
        mPaint.setStyle(Paint.Style.STROKE);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        mProgressDrawable = typedArray.getDrawable(R.styleable.RoundProgressBar_progress);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RoundProgressBar_progressBg);
        textColor = typedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);
        mTextSize = typedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
        mMax = typedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        mProgress = 0;

        typedArray.recycle();
    }

    public void setBorderOffset(int offset){
        mBorderOffset = offset;
    }
    public int getMax() {
        return mMax;
    }
    public void setMax(int mMax) {
        if(mMax < 0){
            throw new IllegalArgumentException("max not less than 0");
        }
        this.mMax = mMax;
    }
    public int getProgress() {
        return mProgress;
    }
    public void setProgress(int progress) {
        if(progress < 0){
            throw new IllegalArgumentException("progress not less than 0");
        }
        if(progress > mMax){
            progress = mMax;
        }
        if(progress <= mMax){
            mProgress = progress;
            postInvalidate();
        }

    }
    public Drawable getProgressBg(){
        return mBackgroundDrawable;
    }

    public Drawable getProgressDrwable(){
        return mProgressDrawable;
    }

    public void setDrawable(Drawable bg, Drawable progess){
        mBackgroundDrawable = bg;
        mProgressDrawable = progess;
    }

    public int getTextColor() {
        return textColor;
    }
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    public float getTextSize() {
        return mTextSize;
    }
    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }
    public int getCenterX(){
        return getWidth()/2;
    }
    public int getCenterY(){
        return getWidth()/2;
    }
    public int getRadian() { return getWidth()/2-mBorderOffset;}
    public Point getEndPoint(){
        Rect r = getDrawableRect();
        return circlePointOf(new Point(r.width() / 2 + r.left, r.height() / 2 + r.top), 360 * mProgress / mMax, r.width() / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas, mProgress);
    }

    protected void drawProgress(Canvas canvas, int progress){
        Paint paint = getPaint();
        Rect area = getDrawableRect();

        int sc = canvas.saveLayer(new RectF(area), null,
                Canvas.MATRIX_SAVE_FLAG |
                        Canvas.CLIP_SAVE_FLAG |
                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                        Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                        Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        canvas.translate(area.left, area.top);
        mBackgroundDrawable.setBounds(0, 0, area.width(), area.height());
        mBackgroundDrawable.draw(canvas);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        Bitmap progressBmp = makeProgressArc(mProgress);
        canvas.drawBitmap(progressBmp, 0, 0, paint);
        canvas.restoreToCount(sc);
    }

    protected Paint getPaint(){
        return mPaint;
    }
    protected Rect getDrawableRect(){
        Rect r = new Rect();
        r.left = getPaddingLeft() + getPaddingLeft() + mBorderOffset;
        r.right = getWidth()-getPaddingRight() - mBorderOffset;
        r.top = getPaddingTop() + getPaddingTop() + mBorderOffset;
        r.bottom = getHeight() - getPaddingBottom() - mBorderOffset;
        return r;
    }

    private Bitmap makeProgressArc(int progress) {
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setStyle(Paint.Style.FILL);


        Rect dr = getDrawableRect();
        if(mProgressCanvasCache == null){
            mProgressCanvasCache = Bitmap.createBitmap(dr.width(), dr.height(), Bitmap.Config.ARGB_8888);
        }else{
            mProgressCanvasCache.eraseColor(Color.TRANSPARENT);
        }
        Bitmap progressBmp = mProgressCanvasCache;
        Canvas c2 = new Canvas(progressBmp);
        mProgressDrawable.setBounds(0,0,dr.width(), dr.width());
        mProgressDrawable.draw(c2);
        int progressAngle = 360*(progress)/mMax;
        if(progressAngle < 90){
            ShapeDrawable markx_360Arc = new ShapeDrawable(new ArcShape(270+progressAngle,90-progressAngle));
            markx_360Arc.getPaint().setColor(0xff000000);
            markx_360Arc.setBounds(-1,-1,dr.width()+1,dr.height()+1);
            markx_360Arc.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            markx_360Arc.draw(c2);

            ShapeDrawable mark0_270Arc = new ShapeDrawable(new ArcShape(0,270));
            mark0_270Arc.getPaint().setColor(0xff000000);
            mark0_270Arc.setBounds(-1,-1,dr.width()+1,dr.height()+1);
            mark0_270Arc.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mark0_270Arc.draw(c2);
        } else if(progressAngle==90){
            ShapeDrawable mark0_270Arc = new ShapeDrawable(new ArcShape(0,270));
            mark0_270Arc.getPaint().setColor(0xff000000);
            mark0_270Arc.setBounds(-1,-1,dr.width()+1,dr.height()+1);
            mark0_270Arc.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mark0_270Arc.draw(c2);
        } else{
            int beingAngle = progressAngle-90;
            ShapeDrawable markx_270Arc = new ShapeDrawable(new ArcShape(beingAngle,270-beingAngle));
            markx_270Arc.getPaint().setColor(0xff000000);
            markx_270Arc.setBounds(-1,-1,dr.width()+1,dr.height()+1);
            markx_270Arc.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            markx_270Arc.draw(c2);
        }
        return progressBmp;
    }

    public Point circlePointOf(Point base, int radian, int angle){
        int realAngle = radian+270;
        int dx = (int)(angle * Math.cos(realAngle * Math.PI / 180));
        int dy = (int)(angle * Math.sin(realAngle * Math.PI / 180));
        return new Point(base.x+dx, base.y+dy);
    }
}
