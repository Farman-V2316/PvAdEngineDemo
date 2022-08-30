/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SectionIndexer;

import java.lang.ref.WeakReference;

/**
 * RecyclerView with support for sectioned index and indicators in sectioned quick scroll.
 *
 * @author karthik.r on 16/8/2016.
 */
public class NHIndexedRecyclerView extends RecyclerView {

  private static final long LAST_FADE_TIMEOUT_DELAY = 10;
  private SectionIndexer sectionIndexer;
  private int mCurrentSection = -1;
  private float mPreviewPadding;
  private float mDensity;
  private int mState = STATE_HIDDEN;
  private float mAlphaRate;
  private float mScaledDensity;
  private int mListViewWidth;
  private int mListViewHeight;

  private ScrollStateChangedListener scrollStateChangedListener;

  private static final int STATE_HIDDEN = 0;
  private static final int STATE_SHOWING = 1;
  private Paint previewPaint;
  private Paint previewTextPaint;

  public interface ScrollStateChangedListener {
    void onScrollStateChanged(int state);
  }

  private final Handler mHandler = new AnimationHandler(this);

  public NHIndexedRecyclerView(Context context) {
    super(context);
    init(context);
  }

  public NHIndexedRecyclerView(Context context,
                               @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public NHIndexedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    mDensity = context.getResources().getDisplayMetrics().density;
    mPreviewPadding = 5 * mDensity;

    previewPaint = new Paint();
    previewPaint.setColor(Color.RED);
    previewPaint.setAlpha(96);
    previewPaint.setAntiAlias(true);
    previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));

    previewTextPaint = new Paint();
    previewTextPaint.setColor(Color.WHITE);
    previewTextPaint.setAntiAlias(true);
    previewTextPaint.setTextSize(50 * mScaledDensity);

  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    // Preview is shown when mCurrentSection is set
    previewPaint.setAlpha((int) (mAlphaRate * 255));

    if (mCurrentSection >= 0) {
      float previewTextWidth = previewTextPaint.measureText((String) sectionIndexer
          .getSections()[mCurrentSection]);
      float previewSize =
          2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
      RectF previewRect = new RectF((mListViewWidth - previewSize) / 2
          , (mListViewHeight - previewSize) / 2
          , (mListViewWidth - previewSize) / 2 + previewSize
          , (mListViewHeight - previewSize) / 2 + previewSize);

      canvas.drawOval(previewRect, previewPaint);
      canvas.drawText((String) sectionIndexer.getSections()[mCurrentSection],
          previewRect.left + (previewSize - previewTextWidth) / 2 - 1,
          previewRect.top + mPreviewPadding - previewTextPaint.ascent() + 1,
          previewTextPaint);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mListViewWidth = w;
    mListViewHeight = h;
  }

  @Override
  public void scrollToPosition(int position) {
    LayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof LinearLayoutManager) {

      LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
      linearLayoutManager.scrollToPositionWithOffset(position, 0);
    } else if (layoutManager instanceof GridLayoutManager) {
      GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
      gridLayoutManager.scrollToPositionWithOffset(position, 0);
    } else {
      super.scrollToPosition(position);
    }
  }

  private void setState(int state) {
    mHandler.removeMessages(mState);
    mState = state;
    mAlphaRate = 0.6f;
    fade(800);
  }

  @Override
  public void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);
    sectionIndexer = (SectionIndexer) adapter;
  }

  @Override
  public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (scrollStateChangedListener != null) {
      scrollStateChangedListener.onScrollStateChanged(state);
    }
  }

  public void setScrollStateChangedListener(ScrollStateChangedListener scrollStateChangedListener) {
    this.scrollStateChangedListener = scrollStateChangedListener;
  }

  public void setCurrentSelection(int currentSelection) {
    this.mCurrentSection = currentSelection;
    setState(STATE_SHOWING);
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    mCurrentSection = -1;
    return super.onTouchEvent(e);
  }

  static class AnimationHandler extends Handler {

    private final WeakReference<NHIndexedRecyclerView> viewWeakReference;

    AnimationHandler(NHIndexedRecyclerView indexedRecyclerView) {
      this.viewWeakReference = new WeakReference<NHIndexedRecyclerView>(indexedRecyclerView);
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      NHIndexedRecyclerView view = viewWeakReference.get();
      // view can be null as it is stored as weakreference.
      if (view == null) {
        return;
      }
      view.mAlphaRate -= view.mAlphaRate * 0.2;
      if (view.mAlphaRate < 0.1) {
        view.mAlphaRate = 0;
        view.mCurrentSection = -1;
        view.mState = STATE_HIDDEN;
        view.invalidate();
      } else {
        view.invalidate();
        view.fade(LAST_FADE_TIMEOUT_DELAY);
      }
    }
  }

  private void fade(long delay) {
    mHandler.removeMessages(mState);
    mHandler.sendEmptyMessageAtTime(mState, SystemClock.uptimeMillis() + delay);
  }

}
