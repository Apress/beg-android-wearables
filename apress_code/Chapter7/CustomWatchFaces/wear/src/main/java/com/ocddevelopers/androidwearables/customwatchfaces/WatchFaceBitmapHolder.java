package com.ocddevelopers.androidwearables.customwatchfaces;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Selects the appropriate image for a watch face element based on whether the watch is in
 * interactive or ambient mode.
 */
public class WatchFaceBitmapHolder {
    public enum WatchState { INTERACTIVE, AMBIENT }
    private int mInteractiveId, mAmbientId, mLowBitAmbientId;
    private int mBurnInAmbientId, mLowBitAndBurnInId;
    private Bitmap mInteractiveScaledBitmap, mAmbientScaledBitmap;
    private WatchState mWatchState;
    private Context mContext;
    private int mPrevWidth, mPrevHeight;
    private int mRefWidth, mRefHeight;

    public WatchFaceBitmapHolder(Context context, int interactiveResID, int ambientResId,
                                 int lowBitResId, int burnInResId, int lowBitAndBurnInResId) {
        mContext = context;
        mInteractiveId = interactiveResID;
        mAmbientId = ambientResId;
        mLowBitAmbientId = lowBitResId;
        mBurnInAmbientId = burnInResId;
        mLowBitAndBurnInId = lowBitAndBurnInResId;
        mWatchState = WatchState.INTERACTIVE;
        mPrevWidth = -1;
        mPrevHeight = -1;
        mRefWidth = 320;
        mRefHeight = 320;
    }

    public void setReferenceSize(int refWidth, int refHeight) {
        mRefWidth = refWidth;
        mRefHeight = refHeight;
    }

    public void prepareBitmaps(int width, int height, boolean lowBitAmbient,
                               boolean burnInProtection) {
        if(mPrevWidth == width && mPrevHeight == height) {
            return;
        }

        mPrevWidth = width;
        mPrevHeight = height;

        int ambientId = mAmbientId;
        if(lowBitAmbient && burnInProtection) {
            ambientId = mLowBitAndBurnInId;
        } else if(lowBitAmbient && !burnInProtection) {
            ambientId = mLowBitAmbientId;
        } else if(!lowBitAmbient && burnInProtection) {
            ambientId = mBurnInAmbientId;
        }


        Resources res = mContext.getResources();
        Bitmap interactiveBitmap = ((BitmapDrawable)res.getDrawable(mInteractiveId)).getBitmap();
        Bitmap ambientBitmap = ((BitmapDrawable)res.getDrawable(ambientId)).getBitmap();

        int scaledWidth = interactiveBitmap.getWidth() * width / mRefWidth;
        int scaledHeight = interactiveBitmap.getHeight() * height / mRefHeight;

        // note: by convention, interactiveBitmap and ambientBitmap should have the same dimensions
        mInteractiveScaledBitmap = Bitmap.createScaledBitmap(interactiveBitmap,
                scaledWidth, scaledHeight, true);
        mAmbientScaledBitmap = Bitmap.createScaledBitmap(ambientBitmap, scaledWidth,
                scaledHeight, true);
    }

    public WatchState getWatchState() {
        return mWatchState;
    }

    public void setWatchState(WatchState watchState) {
        mWatchState = watchState;
    }
    public Bitmap getBitmap() {
        if(mWatchState == WatchState.INTERACTIVE) {
            return mInteractiveScaledBitmap;
        } else {
            return mAmbientScaledBitmap;
        }
    }
}
