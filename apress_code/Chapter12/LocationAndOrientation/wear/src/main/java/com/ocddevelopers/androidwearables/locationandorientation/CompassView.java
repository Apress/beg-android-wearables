package com.ocddevelopers.androidwearables.locationandorientation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draws a compass that is rotated to the given azimuth.
 */
public class CompassView extends View {
    private Bitmap mCompass;
    private float mAzimuth;
    private int mWidth, mHeight;
    private Rect mDestRect;
    private Paint mBitmapPaint;
    private int mOffsetX, mOffsetY;
    private int mBackgroundColor;

    public CompassView(Context context) {
        super(context);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        mCompass = BitmapFactory.decodeResource(res, R.drawable.compass_wear);
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setFilterBitmap(true);
        mBackgroundColor = Color.parseColor("#004c3f");
    }

    public float getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(float azimuth) {
        if(mAzimuth != azimuth) {
            mAzimuth = azimuth;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = measureDimension(widthMeasureSpec, mCompass.getWidth());
        int height = measureDimension(heightMeasureSpec, mCompass.getHeight());
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    private int measureDimension(int measureSpec, int idealSize) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if(specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = idealSize;
            if(specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        mOffsetX = getPaddingLeft();
        mOffsetY = getPaddingTop();

        // scale bitmap to right size
        mDestRect = new Rect(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackgroundColor);
        canvas.translate(mOffsetX, mOffsetY);
        canvas.rotate(-mAzimuth, mWidth/2f, mHeight/2f);
        canvas.drawBitmap(mCompass, null, mDestRect, mBitmapPaint);
    }
}
