/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebView;

import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.util.R;
import com.newshunt.common.view.customview.CustomScroller;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.common.view.view.DetachableWebView;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.NetworkSDK;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

/**
 * Provides utility methods for view.
 *
 * @author shreyas.desai
 */
public class ViewUtils {

  public static void screenChanged() {
    NetworkSDK.screenChanged();
  }

  /**
   * Generic method to get string in localized user language defaulting to English
   *
   * @param langKeys Map between languages and string values
   * @return localized string
   */
  public static String getUserLocalizedString(Map<String, String> langKeys, String defaultValue) {
    return getUserLocalizedString(langKeys, defaultValue, false);
  }

  /**
   * Generic method to get string in localized user language defaulting to English
   */
  public static String getUserLocalizedString(Map<String, String> langKeys, String defaultValue,
                                              boolean fontConversionRequired) {
    return getUserLocalizedStringWithLangSelection(langKeys, defaultValue,
        fontConversionRequired).second;
  }

  /**
   * @param langKeys               language translation map
   * @param defaultValue           default value of string
   * @param fontConversionRequired flag to convert font or not
   * @return pair of (selected language, Translated string)
   */
  public static Pair<String, String> getUserLocalizedStringWithLangSelection(Map<String, String>
                                                                                 langKeys, String
                                                                                 defaultValue,
                                                                             boolean fontConversionRequired) {
    String languageSelected = Constants.DEFAULT_LANGUAGE;
    if (langKeys == null || langKeys.isEmpty()) {
      return Pair.create(languageSelected,
          fontConversionRequired ? FontHelper.getFontConvertedString(defaultValue) :
              defaultValue);
    }

    String userPrimaryLanguage = AppUserPreferenceUtils.getUserPrimaryLanguage();
    String userSecondaryLanguages = AppUserPreferenceUtils.getUserSecondaryLanguages();
    String userLanguages = userPrimaryLanguage;

    if (!DataUtil.isEmpty(userSecondaryLanguages)) {
      userLanguages += Constants.COMMA_CHARACTER + userSecondaryLanguages;
    }

    String[] userLanguageArray = userLanguages.split(Constants.COMMA_CHARACTER);
    String localizedString = langKeys.get(Constants.DEFAULT_LANGUAGE);
    for (String language : userLanguageArray) {
      if (langKeys.containsKey(language)) {
        localizedString = langKeys.get(language);
        languageSelected = language;
        break;
      }
    }
    if (DataUtil.isEmpty(localizedString)) {
      localizedString = langKeys.entrySet().iterator().next().getValue();
      languageSelected = Constants.DEFAULT_LANGUAGE;
    }
    return Pair.create(languageSelected,
        fontConversionRequired ? FontHelper.getFontConvertedString(localizedString) :
            localizedString);
  }

  public static String getUserLocalizedString(Map<String, String> langKeys) {
    return getUserLocalizedString(langKeys, Constants.EMPTY_STRING);
  }

  /**
   * Generic method to set text using our font with specified spacing.
   *
   * @param textView    view in which text to be updated
   * @param langKeys    lang key combination
   * @param fontSpacing font spacing to be applied
   */
  public static void setTextWithFontSpacing(NHTextView textView, Map<String, String> langKeys,
                                            float fontSpacing, String defaultString) {
    // For NHTextView font conversion not required. It is taken care in setText
    String str = getUserLocalizedString(langKeys, defaultString, false);
    if (str != null && str.length() > 0) {
      textView.setText(str);
      textView.setLineSpacing(0, fontSpacing);
    }
  }


  /**
   * Generic method to set text using our font with specified spacing for bold title cards.
   *
   * @param textView              View in which text to be updated
   * @param langKeys              Lang key combinations
   * @param fontSpacingadd        Font spacing extra to be applied
   * @param fontSpacingMultiplier Font spacing multiplier to be applied
   * @param defaultString         Default String
   */
  public static void setTextWithFontSpacingBigCards(NHTextView textView,
                                                    Map<String, String> langKeys,
                                                    final float fontSpacingMultiplier,
                                                    final float fontSpacingadd,
                                                    String defaultString) {
    // For NHTextView font conversion not required. It is taken care in setText
    Pair<String, String> selection =
        getUserLocalizedStringWithLangSelection(langKeys, defaultString,
            false);
    if (selection.second != null && !selection.second.isEmpty()) {
      textView.setText(selection.second);
      textView.setLineSpacing(0, fontSpacingMultiplier);
      if (!Constants.DEFAULT_LANGUAGE.equals(selection.first)) {
        textView.setLineSpacing(fontSpacingadd, fontSpacingMultiplier);
      }
    }
  }


  /**
   * Generic method to set text using our font with specified spacing.
   *
   * @param textView    view in which text to be updated
   * @param text        string to convert
   * @param lineSpacing line spacing to be applied
   */
  public static void setTextWithFontSpacing(NHTextView textView, String text, float lineSpacing) {
    if (textView == null) {
      return;
    }
    if (text != null && text.length() > 0) {
      textView.setText(text);
      textView.setLineSpacing(0, lineSpacing);
    }
  }


  /**
   * Sets text using our font with standard spacing of 1.2f.
   *
   * @param textView view in which text to be updated
   * @param langKeys lang key combination
   */
  public static void setTextWithStandardSpacing(NHTextView textView, Map<String, String> langKeys) {
    setTextWithFontSpacing(textView, langKeys, 1.0f, Constants.EMPTY_STRING);
  }

  /**
   * Generic method to set text using our font with specified spacing.
   *
   * @param context       Context
   * @param textView      view in which text to be updated
   * @param langKeys      lang key combination
   * @param fontSpacing   font spacing to be applied
   * @param drawableResId resource id of image to be displayed
   */
  public static void setImageTextWithFontSpacing(Context context, NHTextView textView, Map<String,
      String> langKeys, float fontSpacing, String defaultString, int drawableResId) {
    String str = getUserLocalizedString(langKeys, defaultString, false);
    if (!DataUtil.isEmpty(str)) {
      if (drawableResId != 0) {
        ImageSpan imageSpan = new ImageSpan(context, drawableResId, DynamicDrawableSpan.ALIGN_BASELINE);
        String convertedStr = FontHelper.getFontConvertedString(str);
        Spannable spannableString = new SpannableString(Constants.DOUBLE_SPACE_STRING + convertedStr);
        spannableString.setSpan(imageSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setSpannableText(spannableString, str);
      } else {
        textView.setText(str);
      }
      textView.setLineSpacing(0, fontSpacing);
    }
  }

  public static Integer getColor(String color) {
    if (DataUtil.isEmpty(color)) {
      return null;
    }
    try {
      return Color.parseColor(color);
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    }
  }

  public static int getColor(String color, int defaultValue) {
    Integer parsedColor = getColor(color);
    if (parsedColor == null) {
      return defaultValue;
    }
    return parsedColor;
  }

  public static int[] getColors(int defValueForAnyColorFailure, String... colors) {
    if (CommonUtils.isEmpty(colors)) {
      return null;
    }

    int count = 0;
    int[] colorValues = new int[colors.length];
    for (String color : colors) {
      colorValues[count++] = getColor(color, defValueForAnyColorFailure);
    }
    return colorValues;
  }

  /**
   * Checks if color is dark or not
   * <p/>
   * Based on :- https://en.wikipedia.org/wiki/Luma_%28video%29
   */
  public static boolean isColorDark(int color) {
    double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) +
        0.114 * Color.blue(color)) / 255;
    return darkness >= 0.5;
  }

  public static void setLinearTransition(ViewPager viewPager, Context context) {
    try {
      Field mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      mScroller.set(viewPager, new CustomScroller(context, new LinearOutSlowInInterpolator(), 350));
    } catch (Exception ex) {

    }
  }

  /**
   * Method to destroy the webviews in its hierarchy
   */
  public static void deleteWebViews(View view) {
    try {
      if (view instanceof DetachableWebView ||
          Constants.OM_WEBVIEW_TAG.equals(view.getTag(R.id.omid_adview_tag_id))) {
        // Do not destroy OM tracked adView.
        // Need to hold it to execute session finish event. SDK will take care to destroy.
        return;
      }

      if (view instanceof WebView) {
        ((WebView) view).destroy();
      } else if (view instanceof ViewGroup) {
        ViewGroup viewGroup = (ViewGroup) view;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
          deleteWebViews(viewGroup.getChildAt(i));
        }
      }
    } catch (Exception e) {
      // catching the generic exception for the cases where webview.destroy is called for already
      // destroyed webview.
      Logger.caughtException(e);
    }
  }

  public static int getVisibilityPercentage(View current) {
    int viewsPortionVisible = 100;
    if (current == null || current.getHeight() == 0) {
      return 0;
    }

    Rect rect = new Rect();
    current.getLocalVisibleRect(rect);
    int height = current.getHeight();
    Logger.v("ViewUtils", "*  getVisibilityPercentage height " + height);

    if (rect.top > 0) {
      // view is partially hidden behind the top edge
      viewsPortionVisible = (height - rect.top) * 100 / height;
    } else if (rect.bottom > 0 && rect.bottom < height) {
      viewsPortionVisible = rect.bottom * 100 / height;
    } else if (rect.top < 0) {
      viewsPortionVisible = 0;
    }

    if (viewsPortionVisible < 0) {
      viewsPortionVisible = 0;
    }
    Logger.v("ViewUtils", "*  getVisibilityPercentage, percents " + viewsPortionVisible);
    return viewsPortionVisible;
  }

  public static int[] getVisibilityPercentage(View current, View parent) {
    int viewsPortionVisible = 100;
    int screenPercentageTaken;
    int[] displayData = new int[2];
    if (current == null || parent == null || current.getHeight() == 0 || parent.getHeight() == 0) {
      return displayData;
    }

    Rect rect = new Rect();
    current.getLocalVisibleRect(rect);
    Logger.v("ViewUtils", "*  getVisibilityPercentage mCurrentViewRect " + rect);

    int height = current.getHeight();
    Logger.v("ViewUtils", "*  getVisibilityPercentage height " + height);

    if (rect.top > 0) {
      // view is partially hidden behind the top edge
      viewsPortionVisible = (height - rect.top) * 100 / height;
      screenPercentageTaken = (height - rect.top) * 100 / parent.getHeight();
    } else if (rect.bottom > 0 && rect.bottom < height) {
      viewsPortionVisible = rect.bottom * 100 / height;
      screenPercentageTaken = rect.bottom * 100 / parent.getHeight();
    } else if (rect.top < 0 || rect.top > parent.getHeight()) {
      viewsPortionVisible = 0;
      screenPercentageTaken = 0;
    } else {
      screenPercentageTaken = height * 100 / parent.getHeight();
    }
    Logger.v("ViewUtils", "*  getVisibilityPercentage, percents " + viewsPortionVisible);

    displayData[0] = viewsPortionVisible;
    displayData[1] = screenPercentageTaken;
    return displayData;
  }

  public static void setScreenAwakeLock(boolean value, Context context, String logTag) {
    if (context == null) {
      return;
    }
    if (context instanceof Activity) {
      //Doing it in ui thread because this function gets called from JS callback also.
      AndroidUtils.getMainThreadHandler().post(() -> {
        Logger.d(logTag, "Setting awake lock " + value);
        if (value) {
          ((Activity) context).getWindow()
              .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
          ((Activity) context).getWindow()
              .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
      });

    }
  }

  public static void setScreenAwakeLock(boolean value, View view, String logTag) {
    if (view == null) {
      return;
    }
    if (view.getContext() instanceof Activity) {
      setScreenAwakeLock(value, view.getContext(), logTag);
      return;
    }
    AndroidUtils.getMainThreadHandler().post(() -> {
      Logger.d(logTag, "Setting awake lock on View " + value);
      view.setKeepScreenOn(value);
    });
  }

  /**
   * For cloning constraints, all children must have an id set.
   * Could be missing in case any child is added by SDK.
   *
   * @param parent
   */
  public static void setMissingViewIds(ViewGroup parent) {
    if (parent == null) {
      return;
    }
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);
      if (child != null && child.getId() == -1) {
        child.setId(View.generateViewId());
      }
    }
  }

  @NonNull
  public static GradientDrawable.Orientation getGradientType(@Nullable String gradientType) {
    try {
      return GradientDrawable.Orientation.valueOf(gradientType);
    } catch (Exception e) {
      return GradientDrawable.Orientation.BL_TR;
    }
  }

  /**
   * Use Pixelcopy to take screenshot of a view.
   * @param view view whose screenshot is to be taken
   * @param activity activity
   * @param callback callback to get the screenshot bitmap
   */
  public static void takeScreenShot(View view, Activity activity,
                                    WeakReference<Consumer<Bitmap>> callback) {
    Logger.v("ViewUtils", "Take screenshot");
    Window window = activity.getWindow();
    if (window != null && view != null) {
      Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
      int[] locationOfViewInWindow = new int[2];
      view.getLocationInWindow(locationOfViewInWindow);
      try {
        PixelCopy.request(window, new Rect(locationOfViewInWindow[0], locationOfViewInWindow[1],
                locationOfViewInWindow[0] + view.getWidth(),
                locationOfViewInWindow[1] + view.getHeight()), bitmap
            , copyResult -> {
              if (PixelCopy.SUCCESS == copyResult) {
                Consumer<Bitmap> consumer = callback.get();
                if (consumer != null) {
                  consumer.accept(bitmap);
                }
              }
            }, AndroidUtils.getMainThreadHandler());
        Logger.v("ViewUtils", "Take screenshot finish");
      } catch (IllegalArgumentException e) {
        Logger.caughtException(e);
      }
    }
  }

  /**
   * Expand the activity to cover full screen.
   * @param activity
   * @param hideStatusBar - if status bar needs to be hidden as well.
   */
  public static void enableFullScreen(Activity activity, boolean hideStatusBar) {
    Window window = activity.getWindow();
    if (window != null) {
      if (Build.VERSION_CODES.R <= Build.VERSION.SDK_INT) {
        if (hideStatusBar) {
          WindowInsetsController controller = window.getInsetsController();
          if (controller != null) {
            controller.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsets.Type.statusBars());
          }
        }
        window.setDecorFitsSystemWindows(false);
      } else {
        View decorView = window.getDecorView();
        if (decorView != null) {
          int displayFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

          // Hide the nav bar and status bar
          if (hideStatusBar) {
            displayFlags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
          }
          decorView.setSystemUiVisibility(displayFlags);
          window.setFlags(
              WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
              WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
          );
        }
      }
      if (Build.VERSION_CODES.P <= Build.VERSION.SDK_INT) {
        window.addFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
        window.getAttributes().layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
      }
      window.setStatusBarColor(activity.getResources().getColor(R.color.ad_overlay_color_dark));
    }
  }

}