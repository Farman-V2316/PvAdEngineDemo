/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.emoji.text.EmojiCompat;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FEOutput;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.FontWeight;
import com.newshunt.common.helper.font.NHCommonTextViewUtil;
import com.newshunt.common.util.R;

import javax.annotation.Nullable;

/**
 * NHTextView: NewsHunt Text View is a UI component which takes care of issues related to NewsHunt
 * font with text views.
 * On some devices, setting of typeface on textView is not enough, a custom
 * typefaceSpace is needed to apply the right font.
 * Example devicecs: Lenovo S650 on OS version: 4.2.2
 * <p/>
 *
 * @author amit.kankani
 */
public class NHTextView extends AppCompatTextView implements NHFontView {

  private NHCommonTextViewUtil nhCommonTextUtil;
  private boolean supportsEmoji;
  private int style;
  private String originalText = null;
  private boolean _keeporiginalText = false;
  private int customFontWeight = -1;
  private boolean isFontWeightCaculated;
  private CharSequence textCpy;

  public NHTextView(Context context) {
    super(context, null);
    if (!isInEditMode()) {
      init(context, null);
    }
  }

  public NHTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!isInEditMode()) {
      init(context, attrs);
    }
  }

  public NHTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (!isInEditMode()) {
      init(context, attrs);
    }
  }

  private void init(Context context, AttributeSet attributeSet) {
    TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NHTextView);
    customFontWeight = typedArray.getInt(R.styleable.NHTextView_dh_custom_font_weight, -1);
    isFontWeightCaculated = true;
    FontHelper.initTextView(this, context, attributeSet, customFontWeight);
    setText(textCpy, nhCommonTextUtil.getBufferType());
    textCpy = null;
    supportsEmoji = typedArray.getBoolean(R.styleable.NHTextView_supportsEmoji, false);
    typedArray.recycle();
  }

  private void initCommonTextUtil() {
    if (null == nhCommonTextUtil) {
      nhCommonTextUtil = new NHCommonTextViewUtil();
    }
  }

  public void setSpannableText(Spannable text, String originalString) {
    setSpannableText(text, originalString, BufferType.SPANNABLE);
  }

  public void setSpannableText(Spannable text, String originalString, BufferType bufferType) {
    boolean isIndic = false;
    if (text != null && text.length() > 0) {
      FEOutput fontEngineOutput;
      fontEngineOutput = FontHelper.convertToFontIndices(originalString);
      isIndic = fontEngineOutput.isSupportedLanguageFound();
    }

    initCommonTextUtil();
    setPadding(isIndic);
    Spannable s = nhCommonTextUtil.getSpannableString(text, isIndic, style, customFontWeight);

    if (this instanceof SpanSupportedView) {
      s = ((SpanSupportedView) this).applySpan(s);
    }

    if (style == Typeface.BOLD) {
      StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
      s.setSpan(boldSpan, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    super.setText(s, bufferType);
  }

  public void setSpannableTextWithLangSpecificTypeFaceChanges(Spannable text, String originalString, BufferType bufferType, String langCode) {
    boolean isIndic = false;
    if (text != null && text.length() > 0) {
      FEOutput fontEngineOutput;
      fontEngineOutput = FontHelper.convertToFontIndices(originalString);
      isIndic = fontEngineOutput.isSupportedLanguageFound();
    }

    initCommonTextUtil();
    setPadding(isIndic);
    Spannable s = nhCommonTextUtil.getSpannableStringForLang(text, isIndic, style, langCode, customFontWeight);

    if (this instanceof SpanSupportedView) {
      s = ((SpanSupportedView) this).applySpan(s);
    }

    if (style == Typeface.BOLD) {
      StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
      s.setSpan(boldSpan, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    super.setText(s, bufferType);
  }

  public void setSpannableTextWithLangSpecificTypeFaceChanges(CharSequence text, BufferType bufferType, String langCode) {
    boolean isIndic = false;
    if (text != null && text.length() > 0) {
      FEOutput fontEngineOutput;
      fontEngineOutput = FontHelper.convertToFontIndices(text.toString());
      isIndic = fontEngineOutput.isSupportedLanguageFound();
    }

    initCommonTextUtil();
    setPadding(isIndic);
    Spannable s = nhCommonTextUtil.getSpannableStringForLang(text, isIndic, style, langCode, customFontWeight);

    if (this instanceof SpanSupportedView) {
      s = ((SpanSupportedView) this).applySpan(s);
    }

    if (style == Typeface.BOLD) {
      StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
      s.setSpan(boldSpan, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    super.setText(s, bufferType);
  }

  @Override
  public void setText(CharSequence text, TextView.BufferType type) {
    if(!isFontWeightCaculated){
      textCpy = new SpannableString(text);
      return;
    }
    if (text == null) {
      text = Constants.EMPTY_STRING;
    }
    if(_keeporiginalText) {
      originalText = text.toString();
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

    setText(text, type, isIndic);
  }

  protected void setText(CharSequence text, BufferType type, boolean isIndic) {
    if (supportsEmoji) {
      text = getEmojiString(text);
    }

    initCommonTextUtil();
    setPadding(isIndic);
    if (nhCommonTextUtil.setTextRequired(text, type)) {
      Spannable s = nhCommonTextUtil.getSpannableString(text, isIndic, style, customFontWeight);

      /**
       * This is done as an option to set spans after font engine conversion.
       * As font engine returns String this is done to add an support for any textview to set span post font conversion.
       */
      if (this instanceof SpanSupportedView) {
        s = ((SpanSupportedView) this).applySpan(s);
      }

      if (style == Typeface.BOLD) {
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        s.setSpan(boldSpan, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      super.setText(s, type);
    }
  }

  private CharSequence getEmojiString(CharSequence charSequence) {

    int emojiState;
    try {
      emojiState = EmojiCompat.get().getLoadState();
    } catch (Exception e) {
      Logger.caughtException(e);
      return charSequence;
    }

    if (emojiState != EmojiCompat.LOAD_STATE_SUCCEEDED) {
      return charSequence;
    }

    try {
      charSequence = EmojiCompat.get().process(charSequence);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return charSequence;
  }

  @Override
  public void setTypeface(Typeface tf) {
    // Do nothing.
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
  public boolean onTouchEvent(MotionEvent event) {
    try {
      return super.onTouchEvent(event);
    } catch (Exception ex) {
      Logger.caughtException(ex);
      // Catch Security Exception, Activity Not found exception, Malformed URL exception, NPE
    }

    // Consume the event after exception
    return true;
  }

  public void keepOriginalText(boolean originalTextFlag) {
    this._keeporiginalText = originalTextFlag;
  }

  @Nullable
  public String getOriginalText() {
    if (_keeporiginalText && originalText != null) {
      return  originalText;
    }
    CharSequence text = getText();
    if (text != null) {
      return text.toString();
    }
    return null;
  }

  public void setCustomFontWeight(FontWeight customFontWeight) {
    this.customFontWeight = customFontWeight.getWeightEnumValue();
    if(this.getText() != null && this.getText().length() >0){
      setText(this.getText(), nhCommonTextUtil.getBufferType());
    }
  }
}
