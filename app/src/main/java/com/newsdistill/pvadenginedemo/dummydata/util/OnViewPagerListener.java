package com.newsdistill.pvadenginedemo.dummydata.util;

public interface OnViewPagerListener {

    void onPageRelease(boolean isNext, int position);

    void onPageSelected(int position, boolean isBottom);
}