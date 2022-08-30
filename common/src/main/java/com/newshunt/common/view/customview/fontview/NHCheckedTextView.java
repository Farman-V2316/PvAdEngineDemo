/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckedTextView;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.font.FEOutput;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.NHCommonTextViewUtil;
import com.newshunt.common.util.R;

/**
 * NHCheckedTextView: NewsHunt Checked Text View is a UI component which takes care of issues
 * related to NewsHunt font with Checked text views.
 * On some devices, setting of typeface on textView is not enough, a custom
 * typefaceSpace is needed to apply the right font.
 * Example devicecs: Lenovo S650 on OS version: 4.2.2
 * <p/>
 *
 * @author amit.kankani
 */
public class NHCheckedTextView extends AppCompatCheckedTextView implements NHFontView {
  private NHCommonTextViewUtil nhCommonTextUtil;
  private int style;
  private int customFontWeight = -1;

  public NHCheckedTextView(Context context) {
    super(context, null);
    init(context, null);
  }

  public NHCheckedTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public NHCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {
    TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NHCheckedTextView);
    customFontWeight = typedArray.getInt(R.styleable.NHCheckedTextView_dh_custom_font_weight, -1);
    typedArray.recycle();
    FontHelper.initTextView(this, context, attributeSet, customFontWeight);

    if(customFontWeight != -1){
      //re-set text to apply custom_font_weight based font
      setText(this.getText(), nhCommonTextUtil.getBufferType());
    }
  }

  private void initCommonTextUtil() {
    if (null == nhCommonTextUtil) {
      nhCommonTextUtil = new NHCommonTextViewUtil();
    }
  }

  @Override
  public void setText(CharSequence text, TextView.BufferType type) {
    if (text == null) {
      text = Constants.EMPTY_STRING;
    }
    boolean isIndic = false;
    if (!(text instanceof Spannable)) {
      if (text != null && text.length() > 0) {
        FEOutput fontEngineOutput;
        fontEngineOutput = FontHelper.convertToFontIndices(text.toString());
        text = fontEngineOutput.getFontIndicesString().toString();
        isIndic = fontEngineOutput.isSupportedLanguageFound();
      }
    }
    initCommonTextUtil();
    setPadding(isIndic);
    if (nhCommonTextUtil.setTextRequired(text, type)) {
      super.setText(nhCommonTextUtil.getSpannableString(text, isIndic, style, customFontWeight), type);
    }
  }

  @Override
  public void setTypeface(Typeface tf) {
    // do nothing
  }

  @Override
  public void setTypeface(Typeface tf, int style) {
    FontHelper.setStyle(this, style);
    this.style = style;
  }

  @Override
  public void setCurrentTypeface(Typeface currentTypeface) {
    initCommonTextUtil();
    nhCommonTextUtil.setCurrentTypeface(currentTypeface);
  }

  @Override
  public void setPadding(boolean isIndic) {
    initCommonTextUtil();
    nhCommonTextUtil.setPadding(this, isIndic);
  }
}
