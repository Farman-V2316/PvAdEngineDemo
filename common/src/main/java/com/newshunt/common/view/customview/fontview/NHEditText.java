/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.font.FEOutput;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.NHCommonTextViewUtil;
import com.newshunt.common.util.R;

/**
 * NHEditText: NewsHunt Edit Text is a UI component which takes care of issues related to NewsHunt
 * font with Edit Texts.
 * On some devices, setting of typeface on textView is not enough, a custom
 * typefaceSpace is needed to apply the right font.
 * Example devicecs: Lenovo S650 on OS version: 4.2.2
 * <p/>
 *
 * @author amit.kankani
 */
public class NHEditText extends androidx.appcompat.widget.AppCompatEditText implements NHFontView {

  private NHCommonTextViewUtil nhCommonTextUtil;
  private Callback callback;
  private int style;
  private int customFontWeight = -1;

  public NHEditText(Context context) {
    super(context, null);
    init(context, null);
  }

  public NHEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public NHEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {
    TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NHEditText);
    customFontWeight = typedArray.getInt(R.styleable.NHEditText_dh_custom_font_weight, -1);
    typedArray.recycle();
    FontHelper.initTextView(this, context, attributeSet, customFontWeight);

    if(customFontWeight != -1){
      //re-set text to apply custom_font_weight based font
      setText(this.getText().subSequence(0, this.getText().length()), nhCommonTextUtil.getBufferType());
    }
  }

  private void initCommonTextUtil() {
    if (null == nhCommonTextUtil) {
      nhCommonTextUtil = new NHCommonTextViewUtil();
    }
  }

  public void setOnEditTextKeyListener(Callback callback) {
    this.callback = callback;
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
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
      Spannable s = nhCommonTextUtil.getSpannableString(text, isIndic, style, customFontWeight);
      if (style == Typeface.BOLD) {
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        s.setSpan(boldSpan, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      super.setText(s, type);
    }
  }

  @Override
  public void setTypeface(Typeface tf) {
    // do nothing
  }

  @Override
  protected void onDetachedFromWindow() {
    callback = null;
    super.onDetachedFromWindow();
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

  @Override
  public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    if (callback == null) {
      return super.onKeyPreIme(keyCode, event);
    }
    switch (event.getAction()) {
      case KeyEvent.ACTION_UP:
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          callback.onKeyboardHide();
        }
    }
    return super.onKeyPreIme(keyCode, event);
  }

  public interface Callback {
    void onKeyboardHide();
  }
}
