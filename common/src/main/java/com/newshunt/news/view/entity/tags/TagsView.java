/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.entity.tags;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.util.R;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.common.view.customview.fontview.SpanSupportedView;

/**
 * @author: bedprakash on 14/11/17.
 */

public class TagsView extends NHTextView implements SpanSupportedView {

  public TagsView(Context context) {
    super(context);
    init();
  }

  public TagsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TagsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setHighlightColor(CommonUtils.getColor(R.color.share_transparent));
  }

  @NonNull
  @Override
  public Spannable applySpan(@NonNull Spannable text) {
    String[] tags = text.toString().split(Constants.SPACE_STRING);
    int startIndex = 0;
    for (String tag : tags) {
      //setting spans on tags
      int endIndex = startIndex + tag.length();
      text.setSpan(new TextClickableSpan(tag), startIndex, endIndex,
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      startIndex = endIndex + Constants.SPACE_STRING.length();
    }
    setMovementMethod(LinkMovementMethod.getInstance());
    return text;
  }


  public static class TextClickableSpan extends ClickableSpan {
    private static final String TAG = "TextClickableSpan";
    public final String s;

    TextClickableSpan(String s) {
      this.s = s;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
      ds.setUnderlineText(false);
    }
    @Override
    public void onClick(View widget) {
      Logger.d(TAG, "Clicked " + s);
    }
  }

}