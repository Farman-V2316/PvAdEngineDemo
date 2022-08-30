/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.font;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;

import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;

/**
 * Common Text View CommonUtils to be used by NHTextView and NHButton
 * <p/>
 *
 * @author amit.kankani on 7/28/2015.
 */
public class NHCommonTextViewUtil {

  private Typeface currentTypeface = null;
  private int topPaddingOriginal = 0;
  private boolean topPaddingOriginalRecorded = false;
  private CharSequence text = Constants.EMPTY_STRING;
  private TextView.BufferType bufferType;
  private boolean isLastPaddingIndic = false;

  public SpannableString getSpannableString(CharSequence text, boolean isIndic, int style, int customFontWeight) {
    if (null == text) {
      text = Constants.EMPTY_STRING;
    }
    return getSpannableStringForLang(text, isIndic, style, ClientInfoHelper.getClientInfo().getAppLanguage(), customFontWeight);
  }

  //Use this function for lang dependent font selection,
  // areas of UI which have lang dependant fonts should use this function and pass correct langCode
  public SpannableString getSpannableStringForLang(CharSequence text, boolean isIndic, int style, String langCode, int customFontWeight) {
    if (null == text) {
      text = Constants.EMPTY_STRING;
    }

    SpannableString spannableString;
    if (text instanceof SpannableString) {
      spannableString = (SpannableString) text;
    }
    else {
      spannableString = new SpannableString(text);
    }

    spannableString.setSpan(new NHTypefaceSpan("", getCurrentTypeface(isIndic, style, langCode, customFontWeight)), 0, text
        .length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    return spannableString;
  }

  public void setPadding(TextView textView, boolean isIndic) {
    if (!topPaddingOriginalRecorded) {
      topPaddingOriginalRecorded = true;
      topPaddingOriginal = textView.getPaddingTop();
    }

    if (isLastPaddingIndic == isIndic) {
      return;
    }

    if (isIndic) {
      isLastPaddingIndic = true;
      textView.setPadding(textView.getPaddingLeft(),
          topPaddingOriginal + (int) (textView.getTextSize() * Constants.FONT_PADDING_FACTOR),
          textView.getPaddingRight(), textView.getPaddingBottom());
    }
    else {
      isLastPaddingIndic = false;
      textView.setPadding(textView.getPaddingLeft(), topPaddingOriginal, textView.getPaddingRight(),
          textView.getPaddingBottom());
    }
  }

  private Typeface getCurrentTypeface(boolean isIndic, int style, String langCode, int customFontWeight) {
    if (isIndic) {
      return currentTypeface;
    } else {
      if(customFontWeight != FontWeight.NOT_DEFINED.getWeightEnumValue()){
        return FontHelper.getTypeFaceFor(langCode, customFontWeight);
      }else{
        if (currentTypeface == null) {
          if (style == Typeface.BOLD) {
            return FontHelper.getTypeFaceFor(langCode, FontWeight.BOLD.getWeightEnumValue());
          }
          else {
            return FontHelper.getTypeFaceFor(langCode, FontWeight.NORMAL.getWeightEnumValue());
          }
        } else if (currentTypeface.isBold() || style == Typeface.BOLD) {
          return FontHelper.getTypeFaceFor(langCode, FontWeight.BOLD.getWeightEnumValue());
        } else {
          return FontHelper.getTypeFaceFor(langCode, FontWeight.NORMAL.getWeightEnumValue());
        }
      }
    }
  }

  public void setCurrentTypeface(Typeface currentTypeface) {
    this.currentTypeface = currentTypeface;
  }

  public boolean setTextRequired(CharSequence text, TextView.BufferType bufferType) {
    if (null == text) {
      text = Constants.EMPTY_STRING;
    }

    if (text.equals(this.text) && bufferType == this.bufferType && !CommonUtils.equals(this.text,
        Constants.EMPTY_STRING)) {
      //Needs to be optimized TODO
      return true;
    } else {
      this.text = text;
      this.bufferType = bufferType;
      return true;
    }
  }

  public TextView.BufferType getBufferType(){
    return this.bufferType;
  }
}
