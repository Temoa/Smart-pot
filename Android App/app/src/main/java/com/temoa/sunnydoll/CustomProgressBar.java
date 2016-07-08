package com.temoa.sunnydoll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Temoa
 * on 2016/4/8 18:06
 */
public class CustomProgressBar extends View {

    private float mSweepAngle = 56;
    private String bgArcColor = "#111111";
    private int w, h;
    private Paint mInsideArcPaint, mOutsideArcPaint, mTextPaint;
    private RectF mRectF;
    private PaintFlagsDrawFilter mDrawFilter;

    public CustomProgressBar(Context context) {
        this(context, null, 0);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInsideArcPaint = new Paint();
        mOutsideArcPaint = new Paint();
        mTextPaint = new Paint();
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        w = measureValue(widthMeasureSpec);
        h = measureValue(heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int length;
        if (w > h) {
            length = w;
        } else {
            length = h;
        }
        mRectF = new RectF(
                (float) (length * 0.1),
                (float) (length * 0.1),
                (float) (length * 0.9),
                (float) (length * 0.9));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);

        mInsideArcPaint.setAntiAlias(true);
        mInsideArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mInsideArcPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mInsideArcPaint.setStrokeWidth(dipToPx(8));
        mInsideArcPaint.setStyle(Paint.Style.STROKE);

        mOutsideArcPaint.setAntiAlias(true);
        mOutsideArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutsideArcPaint.setColor(Color.parseColor(bgArcColor));
        mOutsideArcPaint.setStrokeWidth(dipToPx(8));
        mOutsideArcPaint.setStyle(Paint.Style.STROKE);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(dipToPx(18));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(getResources().getColor(android.R.color.black));

        String showText = "hello";
        //整个圆弧
        canvas.drawArc(mRectF, 120, 300, false, mOutsideArcPaint);
        //当前进度的圆弧
        canvas.drawArc(mRectF, 120, mSweepAngle, false, mInsideArcPaint);
        //圆弧中的文字
        canvas.drawText(showText, 0, showText.length(), w / 2, w / 2 + (dipToPx(12) / 4), mTextPaint);
    }

    private int measureValue(int measureValue) {
        int result;
        int specMode = MeasureSpec.getMode(measureValue);
        int specSize = MeasureSpec.getSize(measureValue);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 300;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public void progressValue(float progressValue) {
        mSweepAngle = (progressValue / 100) * 180;
        Log.d("CustomView", progressValue / 100 + "");
        invalidate();
    }
}
