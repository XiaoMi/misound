package com.xiaomi.mitv.widget;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenxuetong on 9/29/14.
 */
public class GainView extends View {
    private int[] mGains;
    private Paint mPain;
    public GainView(Context context) {
        this(context, null);
    }

    public GainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GainView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGains = new int[]{0,0,0,0,0,0,0};
        mPain = new Paint();
    }

    public void setGains(int[] gains){
        if(gains.length != 5) return;
        for (int i=0; i<gains.length; i++){
            mGains[i+1] = gains[i]/60;
        }
        postInvalidate();
    }

    public void updateGain(int index, int gain){
        if(index<1 || index>5) return;
        mGains[index] = gain;
        postInvalidate();
    }

    private int[] getXIndex(){
        int[] xs = new int[7];
        int realWidth = getWidth()-getPaddingLeft()-getPaddingRight();
        int step = realWidth/(xs.length-1);
        for(int i=0; i<xs.length; i++){
            xs[i] = step*i;
        }
        return xs;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int[] gains = new int[mGains.length];
        for (int i=0; i<mGains.length; i++){
            gains[i] = -mGains[i];
        }

        int[] xs = getXIndex();
        int[] ys = new int[xs.length];
        int half_height = (getHeight()-getPaddingTop()-getPaddingBottom())/2;
        int centerY = half_height;
        for(int i=0; i<ys.length; i++){
            ys[i] = half_height*gains[i]/12+centerY;
        }

        double min=Double.MAX_VALUE;
        double max=Double.MIN_VALUE;
        for(int i=0;i<xs.length;i++){
            if(xs[i]>max) max=xs[i];
            if(xs[i]<min) min=xs[i];
        }

        int sc = canvas.save();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        canvas.translate(left, top);
        Paint p = mPain;
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(Color.parseColor("#959595"));
        canvas.drawLine(0, centerY, getWidth()-getPaddingRight(), centerY, p);

        p.setStrokeWidth(3);
        p.setColor(Color.WHITE);

        PointF[] points = new PointF[xs.length];
        for (int i=0; i<points.length; i++){
            points[i] = new PointF();
            points[i].x = xs[i];
            points[i].y = ys[i];
        }

        Path path = new Path();
        createCurve(points, path);
        canvas.drawPath(path, p);

        p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(1);
        p.setColor(Color.LTGRAY);
        p.setTextSize(8*getContext().getResources().getDisplayMetrics().scaledDensity);
        p.setFakeBoldText(true);
        float density = getContext().getResources().getDisplayMetrics().density;
        canvas.drawText("+12db", 5, 10*density, p);
        canvas.drawText("-12db", 5, getHeight()-getPaddingBottom()-3*density, p);
        canvas.restoreToCount(sc);
    }

    void createCurve(PointF[] originPoint,Path path){
        int originCount = originPoint.length;
        float scale = 0.6f;
        PointF midpoints[] = new PointF[originCount];
        //生成中点
        for(int i = 0 ;i < originCount ; i++){
            int nexti = (i + 1) % originCount;
            midpoints[i] = new PointF();
            midpoints[i].x = (originPoint[i].x + originPoint[nexti].x)/2.0f;
            midpoints[i].y = (originPoint[i].y + originPoint[nexti].y)/2.0f;
        }

        //平移中点
        PointF extrapoints[] = new PointF[2 * originCount];
        for(int i = 0 ;i < originCount ; i++){
            int nexti = (i + 1) % originCount;
            int backi = (i + originCount - 1) % originCount;
            if(i==0) backi=0;
            PointF midinmid = new PointF();
            midinmid.x = (midpoints[i].x + midpoints[backi].x)/2.0f;
            midinmid.y = (midpoints[i].y + midpoints[backi].y)/2.0f;
            float offsetx = originPoint[i].x - midinmid.x;
            float offsety = originPoint[i].y - midinmid.y;
            int extraindex = 2 * i;
            extrapoints[extraindex] = new PointF();
            extrapoints[extraindex].x = midpoints[backi].x + offsetx;
            extrapoints[extraindex].y = midpoints[backi].y + offsety;
            //朝 originPoint[i]方向收缩
            float addx = (extrapoints[extraindex].x - originPoint[i].x) * scale;
            float addy = (extrapoints[extraindex].y - originPoint[i].y) * scale;
            extrapoints[extraindex].x = originPoint[i].x + addx;
            extrapoints[extraindex].y = originPoint[i].y + addy;

            int extranexti = (extraindex + 1)%(2 * originCount);
            extrapoints[extranexti] = new PointF();
            extrapoints[extranexti].x = midpoints[i].x + offsetx;
            extrapoints[extranexti].y = midpoints[i].y + offsety;
            //朝 originPoint[i]方向收缩
            addx = (extrapoints[extranexti].x - originPoint[i].x) * scale;
            addy = (extrapoints[extranexti].y - originPoint[i].y) * scale;
            extrapoints[extranexti].x = originPoint[i].x + addx;
            extrapoints[extranexti].y = originPoint[i].y + addy;
        }

        PointF controlPoint[] = new PointF[4];
        //生成4控制点，产生贝塞尔曲线
        boolean first = true;
        for(int i = 0 ;i < originCount ; i++){
            controlPoint[0] = originPoint[i];
            int extraindex = 2 * i;
            controlPoint[1] = extrapoints[extraindex + 1];
            if(controlPoint[1].x < originPoint[i].x) controlPoint[1] = originPoint[i];

            int extranexti = (extraindex + 2) % (2 * originCount);
            controlPoint[2] = extrapoints[extranexti];
            if(controlPoint[2].x < originPoint[i].x) controlPoint[2] = originPoint[i];

            int nexti = (i + 1) % originCount;
            controlPoint[3] = originPoint[nexti];
            if(controlPoint[3].x < originPoint[i].x) controlPoint[3] = originPoint[i];
            float u = 1;
            while(u >= 0){
                float px = bezier3funcX(u,controlPoint);
                float py = bezier3funcY(u,controlPoint);
                //u的步长决定曲线的疏密
                u -= 0.005;
                if(first){
                    path.moveTo(px, py);
                    first = false;
                }
                else path.lineTo(px, py);
            }
        }
    }
    //三次贝塞尔曲线
    float bezier3funcX(float uu,PointF[] controlP){
        float part0 = controlP[0].x * uu * uu * uu;
        float part1 = 3 * controlP[1].x * uu * uu * (1 - uu);
        float part2 = 3 * controlP[2].x * uu * (1 - uu) * (1 - uu);
        float part3 = controlP[3].x * (1 - uu) * (1 - uu) * (1 - uu);
        return part0 + part1 + part2 + part3;
    }
    float bezier3funcY(float uu,PointF[] controlP){
        float part0 = controlP[0].y * uu * uu * uu;
        float part1 = 3 * controlP[1].y * uu * uu * (1 - uu);
        float part2 = 3 * controlP[2].y * uu * (1 - uu) * (1 - uu);
        float part3 = controlP[3].y * (1 - uu) * (1 - uu) * (1 - uu);
        return part0 + part1 + part2 + part3;
    }
}
