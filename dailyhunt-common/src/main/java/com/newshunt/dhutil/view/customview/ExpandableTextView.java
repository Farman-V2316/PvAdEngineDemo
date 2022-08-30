/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.TextTruncator2;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.view.listener.TextDescriptionSizeChangeListener;

/**
 * This Class is a RelativeLayout, a combination of TextView and a "More" button to show the
 * complete text. By default, it shows an ellipsized part of the text. Users of this class can
 * customize the number of lines of text they want to show, by default. This class also notifies
 * the caller via a listener to notify when the user presses More/Less buttons.
 * <p>
 * Created by srikanth.ramaswamy on 03/11/17.
 */
public class ExpandableTextView extends NHTextView {

  private TextTruncator2 textTruncator2;
  private boolean descriptionTextExpanded;  //Flag indicates whether or not description is expanded.

  private String moreText =
      CommonUtils.getString(com.newshunt.common.util.R.string.photo_gallery_description_more) + "       ";
  //adding extra space as hack to give buffer width for more text, as sometimes more button is
  // not exactly ending in the last line, instead moved to the next line

  private int collapsedMaxLines = DEFAULT_MAX_LINES; //Customizable collapsedMaxLines
  private static final int DEFAULT_MAX_LINES = 5;

  private int readMoreTextColor;
  private int readMoreTextStyle;
  private String uniqueID; //unique ID of the item represented by this View.

  private TextDescriptionSizeChangeListener textDescriptionSizeChangeListener;

  public ExpandableTextView(Context context) {
    super(context);
    buildView(context, null, 0, 0);
  }

  public ExpandableTextView(Context context,
                            @Nullable AttributeSet attrs) {
    super(context, attrs);
    buildView(context, attrs, 0, 0);
  }

  public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    buildView(context,attrs,defStyleAttr,0);
  }

  private void buildView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    fetchLayoutParameters(context, attrs, defStyleAttr, defStyleRes);
  }

  private void fetchLayoutParameters(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
    if (context == null || attrs == null) {
      return;
    }
    TypedArray array =
        context.obtainStyledAttributes(attrs, com.newshunt.dhutil.R.styleable.ExpandableText,
            defStyleAttr, defStyleRes);
    int count = array.getIndexCount();
    for (int i = 0; i < count; i++) {
      int index = array.getIndex(i);

      if (index == com.newshunt.dhutil.R.styleable.ExpandableText_desc_collapsed_max_lines) {
        collapsedMaxLines = array.getInt(index, DEFAULT_MAX_LINES);
      } else if (index == com.newshunt.dhutil.R.styleable.ExpandableText_more_text_color) {
        readMoreTextColor = CommonUtils.getColor(array.getResourceId(index, R.color
            .color_white));
      } else if (index == R.styleable.ExpandableText_more_text_style) {
        readMoreTextStyle = array.getInt(index, Typeface.NORMAL);
      }
    }
    array.recycle();
  }

  public void setText(String text, final boolean isHTMLString, final String uniqueID) {
    boolean expanded = false;
    this.uniqueID = uniqueID;

    if (textDescriptionSizeChangeListener != null) {
      expanded = textDescriptionSizeChangeListener.isStoryExpanded(uniqueID);
    }
    descriptionTextExpanded = expanded;
    getViewTreeObserver().addOnPreDrawListener(this);

    if (!CommonUtils.isEmpty(text)) {
      if (isHTMLString) {
        text = AndroidUtils.getTextFromHtml(text);
      }
      setText(text);
      textTruncator2 =
          new TextTruncator2(text, collapsedMaxLines, this, moreText);
    } else {
      setText(Constants.EMPTY_STRING);
    }
  }

  public void setFontColor(int descColor, int moreLessColor) {
    setTextColor(descColor);
    readMoreTextColor = moreLessColor;
  }

  public void setFontSize(int unit, int size) {
    setTextSize(unit, size);
  }

  @Override
  public void setMaxLines(int maxLines) {
    //DO NOTHING
  }

  /**
   * Checks if the text needs to be truncated, ellipsized, whether or not "more" button needs to
   * be shown.
   */
  private boolean handleTextTruncation() {

    if (textTruncator2 == null || descriptionTextExpanded || !textTruncator2.truncatesText()) {
      return false;
    }

    SpannableString spannableString = new SpannableString(textTruncator2.getTruncatedString());
    ClickableSpan clickableSpan = new ClickableSpan() {
      @Override
      public void onClick(View widget) {
        descriptionTextExpanded = true;
        if (textDescriptionSizeChangeListener != null) {
          textDescriptionSizeChangeListener.onDescriptionExpanded(true,uniqueID); //handling of expanded view should be done by listener and should not expand by default.
          return;
        }
        setText(textTruncator2.getUnTruncatedString());
      }

      @Override
      public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(readMoreTextColor);
        ds.setUnderlineText(false);
        ds.setTypeface(Typeface.defaultFromStyle(readMoreTextStyle));
      }
    };
    int start = textTruncator2.getTruncatedPosition();
    int end = textTruncator2.getTruncatedString().length();
    start = start > end? end : start;
    spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    setText(spannableString);
    setMovementMethod(LinkMovementMethod.getInstance());
    getViewTreeObserver().removeOnPreDrawListener(this);
    return true;
  }

  @Override
  public boolean onPreDraw() {
    return !handleTextTruncation() && super.onPreDraw();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getViewTreeObserver().addOnPreDrawListener(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    getViewTreeObserver().removeOnPreDrawListener(this);
    super.onDetachedFromWindow();
  }

  /**
   * Register listener to get notified when user taps more/less button.
   *
   * @param listener
   */
  public void setTextDescriptionSizeChangeListener(final TextDescriptionSizeChangeListener
                                                       listener) {
    textDescriptionSizeChangeListener = listener;
  }

  public void setMoreText(String text) {
    if (text == null)
      return;
    this.moreText = text;
  }

}
