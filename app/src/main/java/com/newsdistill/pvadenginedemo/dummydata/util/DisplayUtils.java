package com.newsdistill.pvadenginedemo.dummydata.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayUtils {

    private static DisplayUtils instance = null;

    public static DisplayUtils getInstance() {
        if (instance == null) {
            instance = new DisplayUtils();
        }
        return instance;
    }

    public int heightPx;
    public int widthPx;
    public float density;
    public float screenHeight;
    public float screenWidth;
    public float visibleScreenHeight;
    public float visibleScreenHeightMinusBottomNav;
    public float aspectRatio;
    public float scaleDensity;
    public static int defVideoCardImageHeight;

    public double getDefVideoCardImageHeight() {
        return getWidthPx() * 0.5;
    }
    public double getDefBannerAdImageHeight() {
        return getWidthPx() * 0.222;
    }
    public float getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public float getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
    }

    public float getVisibleScreenHeight() {
        return visibleScreenHeight;
    }

    public void setVisibleScreenHeight(float visibleScreenHeight) {
        this.visibleScreenHeight = visibleScreenHeight;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public float getScaleDensity() {
        return scaleDensity;
    }

    public void setScaleDensity(float scaleDensity) {
        this.scaleDensity = scaleDensity;
    }

    public float getVisibleScreenHeightMinusBottomNav() {
        return visibleScreenHeightMinusBottomNav;
    }

    public void setVisibleScreenHeightMinusBottomNav(float visibleScreenHeightMinusBottomNav) {
        this.visibleScreenHeightMinusBottomNav = visibleScreenHeightMinusBottomNav;
    }

    public static float getDeviceDisplayWidthInDp(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    public static float getDeviceDisplayHeightInDp(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
    }

    public static int getDeviceDisplayHeightInPx(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static int getDeviceDisplayWidthInPx(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.widthPixels;
    }


    public static float convertDpToPx(int dp, Context context) {
        if (context == null) {
            return dp;
        }
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int convertDpToPx(int dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float convertSpToPx(int sp, Context context) {
        if (context == null) {
            return sp;
        }
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, r.getDisplayMetrics());
    }


    public static float convertPxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public String toString() {
        return "DisplayUtils{" +
                "screenHeight=" + screenHeight +
                ", screenWidth=" + screenWidth +
                ", visibleScreenHeight=" + visibleScreenHeight +
                ", visibleScreenHeightMinusBottomNav=" + visibleScreenHeightMinusBottomNav +
                ", aspectRatio=" + aspectRatio +
                ", scaleDensity=" + scaleDensity +
                '}';
    }

    public int getHeightPx() {
        return heightPx;
    }

    public void setHeightPx(int heightPx) {
        this.heightPx = heightPx;
    }

    public int getWidthPx() {
        return widthPx;
    }

    public void setWidthPx(int widthPx) {
        this.widthPx = widthPx;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }
}