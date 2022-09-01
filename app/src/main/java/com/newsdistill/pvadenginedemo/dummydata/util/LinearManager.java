package com.newsdistill.pvadenginedemo.dummydata.util;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class LinearManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {

    private PagerSnapHelper pagerSpaner;
    private final String TAG = "LinearManager";

    private OnViewPagerListener viewPagerListener;
    private int diffY = 0;

    public LinearManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        pagerSpaner = new PagerSnapHelper();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        view.addOnChildAttachStateChangeListener(this);
        pagerSpaner.attachToRecyclerView(view);
    }


    @Override
    public void onChildViewDetachedFromWindow(View view) {
        int position = getPosition(view);
        if (0 < diffY) {
            viewPagerListener.onPageRelease(true, position);
        } else {
            viewPagerListener.onPageRelease(false, position);
        }
    }


    @Override
    public void onChildViewAttachedToWindow(View view) {
        int position = getPosition(view);
/*        if(position == 0) {
            viewPagerListener.onPageSelected(position, false);
        }*/
        viewPagerListener.onPageSelected(position, false);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (RecyclerView.SCROLL_STATE_IDLE == state) {
            View view = pagerSpaner.findSnapView(this);
            if (view != null) {
                int position = getPosition(view);
                viewPagerListener.onPageSelected(position, position == getItemCount() - 1);
            }
        }
        super.onScrollStateChanged(state);
    }

    public void setOnViewPagerListener(OnViewPagerListener listener) {
        viewPagerListener = listener;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            diffY = dy;
            return super.scrollVerticallyBy(dy, recycler, state);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}
