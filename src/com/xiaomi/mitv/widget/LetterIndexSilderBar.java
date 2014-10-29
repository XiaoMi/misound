package com.xiaomi.mitv.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.xiaomi.mitv.soundbarapp.R;

/**
 * Created by chenxuetong on 10/9/14.
 */
public class LetterIndexSilderBar extends View {
    private static final int TEXT_SIZE = 10;
    private static final int LETTER_TOTAL = 27;
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#" };
    private int choose = 0;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;

    public LetterIndexSilderBar(Context context) {
        super(context);
    }

    public LetterIndexSilderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LetterIndexSilderBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLetters(String[] letters){
        b = letters;
    }

    /**
     * 为SideBar设置显示字母的TextView
     * @param mTextDialog
     */
    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / LETTER_TOTAL;// 获取每一个字母的高度
        int leftPadding = getPaddingLeft();

        int offset = (LETTER_TOTAL-b.length)/2;

        for (int i = 0; i < b.length; i++) {
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2 + leftPadding;
            float yPos = singleHeight * (i+offset) + singleHeight;

            paint.setColor(Color.parseColor("#BDBCBC"));
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(TEXT_SIZE*getContext().getResources().getDisplayMetrics().scaledDensity);
            // 选中的状态
            if (i == choose) {
                Drawable d = getResources().getDrawable(R.drawable.local_music_letter_presst);
                int dw = d.getIntrinsicWidth();
                int dh = d.getIntrinsicHeight();
                int charW = (int)paint.measureText(b[i]);
                float offsetY = (float)(4*getContext().getResources().getDisplayMetrics().density);
                int sc = canvas.save();
                canvas.translate(xPos+charW/2-dw/2, yPos-offsetY-dh/2);
                d.setBounds(0,0,dw,dh);
                d.draw(canvas);
                canvas.restoreToCount(sc);
                paint.setFakeBoldText(true);
                paint.setColor(Color.parseColor("#777777"));
            }
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int pos = (int) (y / getHeight() * LETTER_TOTAL);
        final int offset = (LETTER_TOTAL-b.length)/2;
        final int c = pos-offset;
        if(c < 0 || c > b.length) return true;

        switch (action) {
            case MotionEvent.ACTION_UP:
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }

                        choose = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     *
     * @author coder
     *
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}
