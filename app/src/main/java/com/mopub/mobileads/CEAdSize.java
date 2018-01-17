package com.mopub.mobileads;


import android.content.Context;
import android.util.TypedValue;

public final class CEAdSize {
    public static final String CEADSIZE = "CEAdSize";

    public static final int FULL_WIDTH = -1;  //Constant that will cause the width of the ad to match the width of the device in the portrait orientation.
    public static final int AUTO_HEIGHT = -2; //SDK determine the height of the ad. example: new CEAdSize(300, AUTO_HEIGHT)
    public static final int AUTO_WIDTH = -3;  //SDK determine the width of the ad. example: new CEAdSize(AUTO_WIDTH, 300)

    public static final CEAdSize SMART_FULL_WIDTH = new CEAdSize(FULL_WIDTH, AUTO_HEIGHT);
    public static final CEAdSize SMART_AUTO = new CEAdSize(AUTO_WIDTH, AUTO_HEIGHT); //The width and height of ad will be determined by the value of ad unit.

    private final int mWidth;
    private final int mHeight;

    /*
     * Create a new CEAdSize
     *
     * @param width The width of the ad in density-independent pixels
     * @param height The height of the ad in density-independent pixels
     */
    public CEAdSize(int width, int height) {
        if (width < 0 && width != FULL_WIDTH && width != AUTO_WIDTH) {
            throw new IllegalArgumentException((new StringBuilder()).append("Invalid width for Size: ").append(width).toString());
        } else if (height < 0 && height != AUTO_HEIGHT) {
            throw new IllegalArgumentException((new StringBuilder()).append("Invalid height for Size: ").append(height).toString());
        } else {
            mWidth = width;
            mHeight = height;
        }
    }

    /*
     * Compares this CEAdSize with the specified object and indicates if they are equal.
     *
     * @param obj The specified object and indicates if they are equal.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof CEAdSize)) {
            return false;
        } else {
            CEAdSize CEAdSize = (CEAdSize) obj;
            return mWidth == CEAdSize.mWidth && mHeight == CEAdSize.mHeight;
        }
    }

    /*
     * Returns the height of this CEAdSize in density-independent pixels.
     */
    public int getHeight() {
        return mHeight;
    }

    /*
     * Returns the height of this CEAdSize in physical pixels.
     */
    public int getHeightInPixels(Context context) {
        if (context == null) {
            return 0;
        }
        switch (mHeight) {
            case AUTO_HEIGHT:
                return AUTO_HEIGHT;
            default:
                return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)mHeight, context.getResources().getDisplayMetrics());
        }
    }

    /*
     * Returns the width of this CEAdSize in density-independent pixels.
     */
    public int getWidth() {
        return mWidth;
    }

    /*
     * Returns the width of this CEAdSize in physical pixels.
     */
    public int getWidthInPixels(Context context) {
        if (context == null) {
            return 0;
        }
        switch (mWidth) {
            case AUTO_WIDTH:
                return AUTO_WIDTH;
            case FULL_WIDTH:
                return context.getResources().getDisplayMetrics().widthPixels;
            default:
                return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)mWidth, context.getResources().getDisplayMetrics());
        }
    }

    /*
     * Returns whether this CEAdSize is auto-height.
     */
    public boolean isAutoHeight() {
        return mHeight == AUTO_HEIGHT;
    }

    /*
     * Returns whether this CEAdSize is full-width.
     */
    public boolean isFullWidth() {
        return mWidth == FULL_WIDTH;
    }
}
