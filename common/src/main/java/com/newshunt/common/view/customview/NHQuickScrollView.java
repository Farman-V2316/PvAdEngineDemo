/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SectionIndexer;

import com.newshunt.common.util.R;

import java.lang.ref.WeakReference;

/**
 * View to display list of alphabets and let user jump to selected position on associated
 * {@code NHIndexedRecyclerView} based on sections provided by associated {@code
 * NHQuickScrollAdapter}.
 *
 * @author karthik.r on 17/8/2016.
 */
public class NHQuickScrollView extends View {

  private static final long LAST_FADE_TIMEOUT_DELAY = 10;

  private static final int STATE_HIDDEN = 0;
  private static final int STATE_SHOWING = 1;

  private boolean[] positionWithValue;
  private float mIndexbarWidth;
  private float mIndexbarMargin;
  private float mDensity;
  private float mScaledDensity;
  private NHIndexedRecyclerView mRecyclerView = null;
  private NHQuickScrollAdapter mIndexer = null;
  private String[] mSections = null;
  private RectF mIndexbarRect;
  private int mCurrentSection = -1;
  private int mState = STATE_HIDDEN;
  private float mAlphaRate;
  private NHQuickScrollListener listener;
  private Paint previewPaint;
  private Paint indexHighlightPaint;
  private Paint indexHiddenPaint;

  private Handler mHandler = new AnimationHandler(new WeakReference<>(this));
  private Paint whitePaint;

  public interface NHQuickScrollAdapter extends SectionIndexer {
    boolean[] getPositionWithValue();
  }

  public interface NHQuickScrollListener {
    void onQuickScroll(int position);
  }

  public void setQuickScrollListener(NHQuickScrollListener listener) {
    this.listener = listener;
  }

  public NHQuickScrollView(Context context) {
    super(context);
  }

  public NHQuickScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NHQuickScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public NHQuickScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setQuickScrollEnabled(boolean enabled) {
    setVisibility(enabled ? View.VISIBLE : View.GONE);
  }

  public void init(Context context, NHIndexedRecyclerView recyclerView) {
    mDensity = context.getResources().getDisplayMetrics().density;
    mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    mRecyclerView = recyclerView;
    setAdapter((NHQuickScrollAdapter) mRecyclerView.getAdapter());

    mIndexbarWidth = 20 * mDensity;
    mIndexbarMargin = 5 * mDensity;

    previewPaint = new Paint();
    previewPaint.setColor(Color.RED);
    previewPaint.setAlpha(96);
    previewPaint.setAntiAlias(true);
    previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));

    indexHighlightPaint = new Paint();
    indexHighlightPaint.setColor(getResources().getColor(R.color.nhquickscroll_text_color));
    indexHighlightPaint.setAntiAlias(true);
    indexHighlightPaint.setTextSize(12 * mScaledDensity);

    indexHiddenPaint = new Paint();
    indexHiddenPaint.setColor(Color.LTGRAY);
    indexHiddenPaint.setAntiAlias(true);
    indexHiddenPaint.setTextSize(12 * mScaledDensity);

    whitePaint = new Paint();
    whitePaint.setColor(Color.WHITE);
    whitePaint.setAntiAlias(true);
    whitePaint.setTextSize(12 * mScaledDensity);
  }

  public void setAdapter(NHQuickScrollAdapter adapter) {
    if (adapter != null) {
      mIndexer = adapter;
      mSections = (String[]) mIndexer.getSections();
      setPositionWithValue(adapter.getPositionWithValue());
      invalidate();
    }
  }

  private void setState(int state) {
    mHandler.removeMessages(mState);
    mState = state;
    mAlphaRate = 0.6f;
    fade(800);
  }

  static class AnimationHandler extends Handler {

    private final WeakReference<NHQuickScrollView> viewWeakReference;

    AnimationHandler(WeakReference<NHQuickScrollView> viewWeakReference) {
      this.viewWeakReference = viewWeakReference;
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      NHQuickScrollView view = viewWeakReference.get();
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

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mSections != null && mSections.length > 0) {
      // Preview is shown when mCurrentSection is set
      if (mCurrentSection >= 0) {
        previewPaint.setAlpha((int) (mAlphaRate * 255));
        float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
        float radius = sectionHeight > mIndexbarWidth ? mIndexbarWidth / 2 : sectionHeight / 2;
        canvas.drawCircle(mIndexbarRect.left + mIndexbarWidth / 2,
            mIndexbarRect.top + mIndexbarMargin + sectionHeight * mCurrentSection +
                sectionHeight / 2, radius, previewPaint);
      }

      float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
      float paddingTop =
          (sectionHeight - (indexHighlightPaint.descent() - indexHighlightPaint.ascent())) / 2;
      for (int i = 0; i < mSections.length; i++) {
        Paint currentPaint = positionWithValue[i] ? indexHighlightPaint : indexHiddenPaint;
        if (mCurrentSection == i) {
          currentPaint = whitePaint;
        }

        float paddingLeft = (mIndexbarWidth - currentPaint.measureText(mSections[i])) / 2;
        canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft,
            mIndexbarRect.top + mIndexbarMargin + sectionHeight * i + paddingTop -
                currentPaint.ascent(), currentPaint);
      }
    }
  }

  public void setCurrentSelection(int currentSelection) {
    mRecyclerView.setCurrentSelection(currentSelection);
    this.mCurrentSection = currentSelection;
    setState(STATE_SHOWING);
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        InputMethodManager imm =
            (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);

        // Determine which section the point is in, and move the list to that section
        gotoScrollPosition(ev);
        return true;
      case MotionEvent.ACTION_MOVE:
        // If this event moves inside index bar
        gotoScrollPosition(ev);
        return true;
    }

    return true;
  }

  private void gotoScrollPosition(MotionEvent ev) {
    // Determine which section the point is in, and move the list to that section
    int currentSelectionMove = getSectionByPoint(ev.getY());
    setCurrentSelection(currentSelectionMove);
    final int targetPosition = mIndexer.getPositionForSection(currentSelectionMove);
    mRecyclerView.scrollToPosition(targetPosition);

    if (listener != null) {
      listener.onQuickScroll(currentSelectionMove);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mIndexbarRect =
        new RectF(w - mIndexbarMargin - mIndexbarWidth, mIndexbarMargin, w - mIndexbarMargin,
            h - mIndexbarMargin);
  }

  private int getSectionByPoint(float y) {
    if (mSections == null || mSections.length == 0) {
      return 0;
    }
    if (y < mIndexbarRect.top + mIndexbarMargin) {
      return 0;
    }
    if (y >= mIndexbarRect.top + mIndexbarRect.height() - mIndexbarMargin) {
      return mSections.length - 1;
    }
    return (int) ((y - mIndexbarRect.top - mIndexbarMargin) /
        ((mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length));
  }

  public void setPositionWithValue(boolean[] positionWithValue) {
    this.positionWithValue = positionWithValue;
  }
}