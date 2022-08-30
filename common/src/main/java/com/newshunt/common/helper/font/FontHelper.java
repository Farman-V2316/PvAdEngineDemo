/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;

import com.google.android.material.snackbar.Snackbar;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.util.R;
import com.newshunt.common.view.customview.CustomToastView;
import com.newshunt.common.view.customview.fontview.NHFontView;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to set the font for all devices
 *
 * @author maruti.borker
 */
public class FontHelper {

  private static final String TAG = FontHelper.class.getSimpleName();
  private static final String SANS_SERIF_FONT = "sans-serif";
  private static final String SYSTEM_FONT_MAP = "sSystemFontMap";
  public static Typeface notoFontRegular = null;
  public static Typeface notoFontBold = null;
  public static Map<String, String>  fontMapping = new HashMap<>();
  public static Map<String, HashMap<Integer, Typeface>>  typeFaceToLangMapping = new HashMap<>();

  public static void setupTextView(TextView textView, FontType fontType, FontWeight fontWeight) {
    if (textView == null) {
      return;
    }

    Typeface typeface = FontCache.getInstance().getFontTypeFace(textView.getContext(), fontType);
    if (textView instanceof NHFontView) {
      NHFontView nhFontView = (NHFontView) textView;
      nhFontView.setCurrentTypeface(typeface);
    }

    textView.setTypeface(typeface);
    textView.getPaint().setTypeface(typeface);
    textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
  }

  public static void setStyle(TextView textView, int style) {
    if (style == Typeface.BOLD) {
      FontHelper.setupTextView(textView, FontType.NEWSHUNT_BOLD, FontWeight.BOLD);
    } else if (style == Typeface.NORMAL) {
      FontHelper.setupTextView(textView, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
    }
  }

  /**
   * A utility method to init all the views which extend NhFontView.
   *  @param textView - an instance of @NhFontView
   * @param context  - The View context.
   * @param attrs    - The attribute set of the view.
   * @param customFontWeightEnumValue - custom font weight supplied in xml
   * if only style is defined it will be converted to fontWeight Normal to Normal and Bold to Bold, if both are defined fontweight will win
   */
  public static void initTextView(TextView textView, Context context, AttributeSet attrs, int customFontWeightEnumValue) {

    if (textView == null) {
      return;
    }

    FontType fontType = FontType.NEWSHUNT_REGULAR;
    FontWeight fontWeight = FontWeight.NORMAL;
    // attrs can be null when the user programatically creates an object of NhTextView by only
    // passing context object. And ideally context should not be null.
    if (attrs == null || context == null) {
      FontHelper.setupTextView(textView, fontType, fontWeight);
      return;
    }

    int[] ATTRS = new int[]{android.R.attr.textStyle};
    TypedArray typedArray = context.obtainStyledAttributes(attrs, ATTRS);

    if (typedArray == null || typedArray.length() == 0) {
      FontHelper.setupTextView(textView, fontType, FontWeight.NORMAL);
      return;
    }

    int value = typedArray.getInteger(0, Typeface.NORMAL);
    fontWeight = FontWeight.getFontWeightForEnumValue(customFontWeightEnumValue);
    typedArray.recycle();

    if (value == Typeface.BOLD) {
      fontType = FontType.NEWSHUNT_BOLD;
      //If custom weight is not defined and style is defined as BOLD, autoselect bold customFontWeight
      if(customFontWeightEnumValue == -1){
        fontWeight = FontWeight.BOLD;
      }
    }else if(value == Typeface.NORMAL){
      //If custom weight is not defined and style is defined as NORMAL, autoselect normal customFontWeight
      if(customFontWeightEnumValue == -1){
        fontWeight = FontWeight.NORMAL;
      }
    }
    FontHelper.setupTextView(textView, fontType, fontWeight);
  }

  public static void setSpannableTextWithFont(TextView textView, String text, FontType fontType, FontWeight fontWeight) {
    if (null == text) {
      text = Constants.EMPTY_STRING;
    } else {
      //TODO: Find better solution - NHTextview should take care about Spannable string
      text = getFontConvertedString(text);
    }
    Typeface typeface = FontCache.getInstance().getFontTypeFace(textView.getContext(), fontType);
    // change the text
    Spannable spannableString = new SpannableString(text);
    spannableString.setSpan(new NHTypefaceSpan("", typeface), 0, text.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    textView.setText(spannableString);
  }

  public static String getFontConvertedString(String baseString) {
    if (baseString != null && baseString.length() > 0) {
      FEOutput fontEngineOutput;
      fontEngineOutput = FEWrapper.convertToFontIndices(baseString);
      baseString = fontEngineOutput.getFontIndicesString().toString();
    }
    return baseString;
  }

  public static FEOutput convertToFontIndices(String data) {
    return FEWrapper.convertToFontIndices(data);
  }

  public static boolean enableDhFont() {
    return FEWrapper.dhFontEnabled();
  }

  /**
   * Based on :- http://stackoverflow.com/a/28900749/716912
   */
  public static void overrideDefaultFont(Context context,
                                         String staticTypefaceFieldName, FontType fontType) {

    final Typeface newTypeface = FontCache.getInstance().getFontTypeFace(context, fontType);

    Map<String, Typeface> newMap = new HashMap<String, Typeface>();
    newMap.put(SANS_SERIF_FONT, newTypeface);
    try {
      final Field staticField = Typeface.class.getDeclaredField(SYSTEM_FONT_MAP);
      staticField.setAccessible(true);
      staticField.set(null, newMap);
    } catch (NoSuchFieldException e) {
      //don't do anything
    } catch (IllegalAccessException e) {

    }
  }

  /**
   * custom toast view for displaying exit message by selected language.
   */
  public static void showCustomFontToast(Context context, String exitMessage, int displayTime) {
    showCustomFontToast(context, exitMessage, displayTime, Gravity.BOTTOM);
  }

  public static void showCustomFontToast(Context context, String exitMessage,
                                         int displayTime, int gravity) {
    if (null == context) {
      return;
    }

    //Below API 19, notifications are always enabled and there is no settings to disable them.
    if (!AndroidUtils.areNotificationsEnabled()) {
      showCustomToast(context, exitMessage, displayTime);
    } else {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        Toast toast = Toast.makeText(context, exitMessage, displayTime);
        toast.show();
      } else {
        Toast toast = Toast.makeText(context, exitMessage, displayTime);
        View view = toast.getView();
        TextView textView = (TextView) view.findViewById(android.R.id.message);
        textView.setPadding(0, 10, 0, 10);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                CommonUtils.getDimensionInDp(R.dimen.custom_toast_text_size));
        textView.setText(FontHelper.getFontConvertedString(exitMessage));
        FontHelper.setupTextView(textView, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
        FontHelper.setSpannableTextWithFont(textView, textView.getText().toString(),
                FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
        toast.setGravity(gravity | Gravity.CENTER, 10, 30);
        toast.show();
      }
    }
  }

  @SuppressLint("ToastUsedDirectly")
  public static void showCustomFontToast(Context context, Spanned exitMessage,
                                         int displayTime, int gravity) {
    if (null == context) {
      return;
    }

    if (!AndroidUtils.areNotificationsEnabled()) {
      showCustomToast(context, exitMessage.toString(), displayTime);
    } else {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        Toast toast = Toast.makeText(context, exitMessage, displayTime);
        toast.show();
      } else {
        Toast toast = Toast.makeText(context, exitMessage, displayTime);
        View view = toast.getView();
        TextView textView = view.findViewById(android.R.id.message);
        textView.setPadding(0, 10, 0, 10);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
            CommonUtils.getDimensionInDp(R.dimen.custom_toast_text_size));

        FontHelper.setupTextView(textView, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
        FontHelper.setText(exitMessage, TextView.BufferType.NORMAL, textView);
        toast.setGravity(gravity | Gravity.CENTER, 10, 30);
        toast.show();
      }
    }
  }

  public static void setText(CharSequence text, TextView.BufferType type, TextView textView) {
    if (text == null || CommonUtils.isEmpty(text.toString())) {
      textView.setText(Constants.EMPTY_STRING);
      return;
    }

    boolean isIndic = false;
    if (!(text instanceof Spannable)) {
      FEOutput fontEngineOutput;
      fontEngineOutput = FontHelper.convertToFontIndices(text.toString());
      text = fontEngineOutput.getFontIndicesString().toString();
      isIndic = fontEngineOutput.isSupportedLanguageFound();
    }

    NHCommonTextViewUtil nhCommonTextUtil = new NHCommonTextViewUtil();
    Spannable s = nhCommonTextUtil.getSpannableString(text, isIndic, Typeface.NORMAL, FontWeight.NORMAL.getWeightEnumValue());
    textView.setText(s, type);
  }

  /**
   * Show a custom defined toast.
   *
   * @param context           - Activity context to inflate the layout
   * @param exitMessage       - Toast message
   * @param toastDurationType - Toast duration
   */
  private static void showCustomToast(final Context context, String exitMessage,
                                      int toastDurationType) {

    int displayTime = Constants.TOAST_LENGTH_SHORT;
    if (toastDurationType == Toast.LENGTH_LONG) {
      displayTime = Constants.TOAST_LENGTH_LONG;
    }
    CustomToastView.makeToast(context, exitMessage, displayTime);
  }

  /**
   * Show a custom defined toast with background and bottom margin.
   *
   * @param context           - Activity context to inflate the layout
   * @param toastMessage      - Toast message
   * @param toastDurationType - Toast duration
   * @param layoutId          - layout for custom toast
   * @param bottomMargin      - Bottom margin
   */
  public static void showCustomToast(final Context context, String toastMessage,
                                     int toastDurationType, int layoutId, int bottomMargin) {
    int displayTime = Constants.TOAST_LENGTH_SHORT;
    if (toastDurationType == Toast.LENGTH_LONG) {
      displayTime = Constants.TOAST_LENGTH_LONG;
    }
    CustomToastView.makeToast(context, toastMessage, displayTime, layoutId, bottomMargin);
  }

  /**
   * A custom method for showing the snackbar. This method will ensure of rendering the textviews
   * showed in Snackbar in our fonts.
   */
  public static Snackbar showCustomSnackBar(@NonNull View view, @NonNull String message,
                                            int duration, String actionMessage,
                                            View.OnClickListener actionOnClickListener) {

    Snackbar snackbar =
        Snackbar.make(view, FontHelper.getFontConvertedString(message), duration);
    if (!CommonUtils.isEmpty(actionMessage)) {
      snackbar.setAction(FontHelper.getFontConvertedString(actionMessage), actionOnClickListener);
    }

    // Apply font
    View sbView = snackbar.getView();
    TextView sbText =
        (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
    sbText.setMaxLines(3);
    FontHelper.setupTextView(sbText, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);

    TextView sbAction =
        (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_action);
    FontHelper.setupTextView(sbAction, FontType.NEWSHUNT_BOLD, FontWeight.BOLD);
    snackbar.show();
    return snackbar;
  }



  public static void setupButtonText(Button button, FontType fontType) {
    if (null == button) {
      return;
    }
    Typeface typeface = FontCache.getInstance().getFontTypeFace(button.getContext(), fontType);
    button.setTypeface(typeface);
  }

  public static Typeface getTypeFaceFor(String langCode, int customFontWeight){
    if(CommonUtils.isEmpty(langCode)){
      langCode = ClientInfoHelper.getClientInfo().getAppLanguage();
    }
    if(typeFaceToLangMapping.containsKey(langCode) && typeFaceToLangMapping.get(langCode).containsKey(customFontWeight)){
      return typeFaceToLangMapping.get(langCode).get(customFontWeight);
    }else if(typeFaceToLangMapping.containsKey(langCode) && typeFaceToLangMapping.get(langCode).containsKey(0)){
      return typeFaceToLangMapping.get(langCode).get(0);
    }else if(typeFaceToLangMapping.get("default").containsKey(customFontWeight)){
      return typeFaceToLangMapping.get("default").get(customFontWeight);
    }else{
      return typeFaceToLangMapping.get("default").get(0);
    }
  }

  public static void initializeFont(Context context, String appLanguage) {
    try {
      //This check is to avoid multiple times creation of typefaces, typefaces take time to get garbage collected
      if(typeFaceToLangMapping.size() == 0){

        typeFaceToLangMapping.clear();
        fontMapping.clear();
        HashMap<Integer, Typeface> notoHashmap = new HashMap<>();
        notoHashmap.put(0, ResourcesCompat.getFont(context, R.font.noto_sans_normal));
        notoHashmap.put(1, ResourcesCompat.getFont(context, R.font.noto_sans_medium));
        notoHashmap.put(2, ResourcesCompat.getFont(context, R.font.noto_sans_semibold));
        notoHashmap.put(3, ResourcesCompat.getFont(context, R.font.noto_sans_bold));
        notoHashmap.put(4, ResourcesCompat.getFont(context, R.font.noto_sans_extrabold));
        typeFaceToLangMapping.put("default", notoHashmap);

        HashMap<Integer, Typeface> libreHashmap = new HashMap<>();
        libreHashmap.put(0, ResourcesCompat.getFont(context, R.font.libre_franklin_normal));
        libreHashmap.put(1, ResourcesCompat.getFont(context, R.font.libre_franklin_medium));
        libreHashmap.put(2, ResourcesCompat.getFont(context, R.font.libre_franklin_semibold));
        libreHashmap.put(3, ResourcesCompat.getFont(context, R.font.libre_franklin_bold));
        libreHashmap.put(4, ResourcesCompat.getFont(context, R.font.libre_franklin_extrabold));
        typeFaceToLangMapping.put("en", libreHashmap);

        fontMapping.put("en", "file:///android_res/font/libre_franklin");
        fontMapping.put("default", "file:///android_res/font/noto_sans");
      }
//      notoFontBold = Typeface.create(Constants.DEFAULT_FONT_NAME, Typeface.BOLD);
//      notoFontRegular = getTypeFaceFor(appLangCode, 0);
    } catch (Exception ex) {
      Logger.e(TAG, "Exception loading fonts:", ex);
    }

    FontsContractCompat.FontRequestCallback callback =
        new FontsContractCompat.FontRequestCallback() {
          @Override
          public void onTypefaceRetrieved(Typeface typeface) {
            if (typeface.isBold()) {
              notoFontBold = typeface;
            } else {
              notoFontRegular = typeface;
            }
          }

          @Override
          public void onTypefaceRequestFailed(int reason) {
            Logger.e(TAG, "Fonts Request Failed");
          }
        };

    android.os.Handler handler = new android.os.Handler(Looper.getMainLooper()) {

      @Override
      public void handleMessage(Message msg) {

      }
    };

    FontRequest request = new FontRequest("com.google.android.gms.fonts",
        "com.google.android.gms",
        "Noto Sans",
        R.array.com_google_android_gms_fonts_certs);

    //FontsContractCompat.requestFont(context, request, callback, handler);
  }

  public static String getFontName(String langCode){
    String language = langCode;
    if(CommonUtils.isEmpty(language)){
      language = ClientInfoHelper.getClientInfo().getAppLanguage();
    }

    if(fontMapping.containsKey(langCode)){
      return fontMapping.get(langCode);
    }else{
      return fontMapping.get("default");
    }
  }
}
