/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Html;
import android.text.Layout;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.MutableLiveData;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.cookie.CustomCookieManager;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.preference.SavedPreference;
import com.newshunt.common.helper.share.ShareAppDetails;
import com.newshunt.common.helper.share.ShareUTMHelper;
import com.newshunt.common.helper.watcher.Watcher;
import com.newshunt.common.helper.watcher.WatcherProvider;
import com.newshunt.common.util.R;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.share.ShareApplication;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.FollowViewState;
import com.newshunt.dhutil.helper.preference.AppRatePreference;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.sdk.network.connection.ConnectionManager;
import com.newshunt.sdk.network.connection.ConnectionSpeed;
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent;
import com.newshunt.sdk.network.connection.ConnectionType;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnegative;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Android platform specific utilities
 *
 * @author arun.babu
 */
public class AndroidUtils {


  private static WatcherProvider watcherProvider;
  @NotNull
  public static final MutableLiveData<ConnectionSpeedEvent> connectionSpeedLiveData =
      new MutableLiveData<ConnectionSpeedEvent>();
  private static final String LOG_TAG = "AndroidUtils";
  private static final Watcher nullWatcher = new Watcher() {
    @Override
    public void watch(Object watchedReference) {
      return;
    }
  };
  public static final int NOT_A_RESOURCE_ID = -1;
  private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  /**
   * Executor to execute the background tasks across the app that involves I/O operations
   */
  public static ExecutorService IO_THREAD_POOL = Executors.newCachedThreadPool();

  public static boolean isAppInstalled(String packageName) {
    try {
      CommonUtils.getApplication().getPackageManager().getApplicationInfo(packageName, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    } catch (Exception e) {
      // some other exception and still return false
      return false;
    }
  }


  /**
   * This method checks if the app has been disabled by the user.
   * Note: Also returns disabled true if the app is not installed.
   *
   * @param packageName
   * @return
   */
  public static boolean isAppDisabled(String packageName) {
    try {
      if (!isAppInstalled(packageName)) {
        return true;
      }
      ApplicationInfo ai =
          CommonUtils.getApplication().getPackageManager().getApplicationInfo(packageName, 0);

      return !ai.enabled;
    } catch (PackageManager.NameNotFoundException e) {
      return true;
    } catch (Exception e) {
      // some other exception and still return disabled
      return true;
    }
  }

  /**
   * Utility function to check whether view is visible on screen by atleast some percentage
   *
   * @param view              - view whose visibility to check
   * @param minPercentVisible - min percent of the view that should be visible
   * @return - true if visiblity > minPercent else false
   */
  public static boolean isViewVisibleOnScreen(View view, float minPercentVisible) {
    if (view == null || view.getVisibility() == View.GONE || view.getHeight() == 0) {
      return false;
    }

    Rect visibleRect = new Rect();
    if (!view.getGlobalVisibleRect(visibleRect)) {
      return false;
    }

    float minHeight = view.getHeight() * (minPercentVisible / 100);
    return visibleRect.height() >= minHeight;
  }

  /**
   * Function to set ellipses end style in customized way as there is some bug in android
   * function ellipses.
   *
   * @param textView
   * @author vishal.bharati
   */
  public static void setEllipseTextView(final TextView textView) {
    ViewTreeObserver vto = textView.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (textView.getLineCount() > 1) {
          int lineEndIndex = textView.getLayout().getLineEnd(0);
          if (lineEndIndex >= 3) {
            String text = textView.getText().subSequence(0, lineEndIndex - 3) + Constants
                .ELLIPSIZE_END;
            textView.setText(text);
          }
        }
      }
    });
  }

  public static void truncateAndSetTextFromHtml(NHTextView textView, String htmlText,
                                                int maxChars, String languageCode) {
    if (CommonUtils.isEmpty(htmlText)) {
      return;
    }

    if (htmlText.length() > maxChars) {
      setTextFromHtml(textView, htmlText.substring(0, maxChars), languageCode);
    } else {
      setTextFromHtml(textView, htmlText, languageCode);
    }
  }

  public static void setTextFromHtml(NHTextView textView, String htmlText, String languageCode) {
    if (CommonUtils.isEmpty(htmlText)) {
      return;
    }

    try {
      String plainTxt = Html.fromHtml(htmlText).toString();
      plainTxt = DataUtil.removeObjReplacementChar(plainTxt).trim();
      String ellipsizedText =
          plainTxt.toString().replace("\n", " ").replace("\r", " ");
      textView.setSpannableTextWithLangSpecificTypeFaceChanges(ellipsizedText, TextView.BufferType.SPANNABLE, languageCode);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static String getTextFromHtml(String htmlText) {
    if (CommonUtils.isEmpty(htmlText)) {
      return Constants.EMPTY_STRING;
    }

    try {
      String plainTxt = Html.fromHtml(htmlText).toString();
      plainTxt = DataUtil.removeObjReplacementChar(plainTxt).trim();
      String ellipsizedText =
          plainTxt.toString().replace("\n", " ").replace("\r", " ");
      return ellipsizedText;
    } catch (Exception e) {
      Logger.caughtException(e);
    }

    return Constants.EMPTY_STRING;
  }

  public static CharSequence getRichTextFromHtml(String htmlString){
    if(CommonUtils.isEmpty(htmlString)) {
      return Constants.EMPTY_STRING;
    }

    try{
      CharSequence richText = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY);
      Logger.d(LOG_TAG, "Payload msg is " + htmlString);
      if(CommonUtils.isEmpty(richText.toString())){

        Logger.d(LOG_TAG, "Html conversion failed trying with deprecated function once");
        //surrounding in try catch since Html.fromHtml(string) is deprecated beyond api 24, however, is the only way for few devices to parse html to string
        try{
          richText = Html.fromHtml(htmlString);
        }
        catch(Exception ex){
          Logger.d(LOG_TAG, "Exception during converting htmlString to richtext:- " + ex.getMessage());
        }

        if(CommonUtils.isEmpty(richText.toString())){
          Logger.d(LOG_TAG, "HTml.fromHtml(string) also failed");
        }else{
          return  richText;
        }
        return getTextFromHtml(htmlString);
      }
      return richText;
    } catch(Exception e){
      Logger.caughtException(e);
    }
    return Constants.EMPTY_STRING;
  }

  /**
   * Utility function to check if there will be overlap at the given line number and if the
   * viewToCheckForOverlapping is placed at the last line of the textview
   *
   * @param textView                  - textView
   * @param viewToCheckForOverlapping - view to check if the given textview will not overlap or
   *                                  not at the last line
   * @return - true if it will overlap, else false
   */
  public static boolean overlapsWithLastLine(TextView textView,
                                             View viewToCheckForOverlapping) {
    Layout layout = textView.getLayout();
    return layout != null &&
        overlapsWithLine(textView, viewToCheckForOverlapping, layout.getLineCount() - 1);
  }

  /**
   * Utility function to check if there will be overlap at the given line number and if the
   * viewToCheckForOverlapping is placed at the given line number
   *
   * @param textView                  - textView
   * @param viewToCheckForOverlapping - view to check if the given textview will not overlap or
   *                                  not at the given line number
   * @param lineNumber                - line number to check for overlapping
   * @return - true if it will overlap, else false
   */
  public static boolean overlapsWithLine(TextView textView, View viewToCheckForOverlapping,
                                         int lineNumber) {
    Layout layout = textView.getLayout();
    if (layout == null || lineNumber >= layout.getLineCount()) {
      return false;
    }
    float lineWidth = layout.getLineWidth(lineNumber);
    int moreOrLessWidth =
        viewToCheckForOverlapping.getWidth() - viewToCheckForOverlapping.getPaddingLeft() -
            viewToCheckForOverlapping.getPaddingRight();
    int descViewWidth =
        textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
    return (lineWidth + moreOrLessWidth) >= descViewWidth;
  }

  public static boolean overlapsWithLine(TextView textView, int overLapWidth, int lineNumber) {
    Layout layout = textView.getLayout();
    if (layout == null || lineNumber >= layout.getLineCount()) {
      return false;
    }
    float lineWidth = layout.getLineWidth(lineNumber);
    int descViewWidth =
        textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
    return (lineWidth + overLapWidth) >= descViewWidth;
  }

  public static void showKeyBoard(Context context, EditText editText) {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context
        .INPUT_METHOD_SERVICE);
    editText.requestFocus();
    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
  }

  public static void hideKeyboard(Context context, EditText editText) {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context
        .INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
  }

  public static void hideKeyBoard(Activity activity) {
    if (activity == null) {
      return;
    }
    InputMethodManager inputMethodManager = (InputMethodManager) activity
        .getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = activity.getCurrentFocus();
    if (view == null) {
      view = new View(activity);
    }
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static boolean isTableExists(SQLiteDatabase db, String tableName) {
    if (tableName == null || db == null || !db.isOpen()) {
      return false;
    }

    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
          new String[]{"table", tableName});
      if (cursor == null || !cursor.moveToFirst()) {
        return false;
      }

      int count = cursor.getInt(0);
      return count > 0;
    } catch (Exception e) {
      Logger.caughtException(e);
      return false;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }


  /**
   * Copies the text to clipboard.
   *
   * @param context
   * @param label        : User-visible label for the clip data.
   * @param textToCopy   : The actual text in the clip
   * @param toastMessage
   */
  @SuppressLint("ToastUsedDirectly")
  public static void CopyContent(Context context, String label, String textToCopy, String
      toastMessage) {
    ClipboardManager clipboard = (ClipboardManager)
        context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(label, textToCopy);
    clipboard.setPrimaryClip(clip);
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
  }

  /**
   * This method use to pause webview for pausing videos playing inside it when swipe to next story.
   *
   * @param pause
   */
  public static void toggleWebViewState(WebView newsDetailWebView2, boolean pause) {
    try {
      Class.forName(Constants.WEB_VIEW_PACKAGE).getMethod(
          pause ? Constants.ON_PAUSE : Constants.ON_RESUME, (Class[]) null).invoke(
          newsDetailWebView2, (Object[]) null);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /*
   * http://stackoverflow.com/questions/20675554/webview-rendering-issue-in-android-kitkat
   *
   */
  public static void enableHardwareAcceleration(Activity activity) {
    if (null == activity) {
      return;
    }

    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
      activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
          WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }
  }

  public static void setCookiesForWebView(WebView webView, String urlToLoad) {
    CookieManager.setAcceptFileSchemeCookies(true);
    CookieManager cookieManager = CookieManager.getInstance();
    // below lollipop - 3rd party cookies are enabled by default
    if (webView != null) {
      cookieManager.setAcceptThirdPartyCookies(webView, true);
    }
    List<HttpCookie> cookies = CustomCookieManager.getCookies(urlToLoad);
    if (cookies != null) {
      for (HttpCookie cookie : cookies) {
        String url = cookie.getDomain() + cookie.getPath();
        cookieManager.setCookie(url, cookie.toString());
      }
    }
  }

  /**
   * Forces webview cookies to be written to Persistent storage
   */
  public static boolean persistWebCookies() {
    try {
      android.webkit.CookieManager.getInstance().flush();
      return true;
    } catch (Exception e) {
      Logger.caughtException(e);
      return false;
    }
  }

  public static Handler getMainThreadHandler() {
    return mainThreadHandler;
  }

  public static int parseResourceIdFromString(String string) {
    if (CommonUtils.isEmpty(string)) {
      return NOT_A_RESOURCE_ID;
    }

    int viewId = NOT_A_RESOURCE_ID;
    try {
      viewId = Integer.parseInt(string);
    } catch (NumberFormatException e) {
      Logger.caughtException(e);
    }
    return viewId;
  }

  /**
   * Instantiates a new single threaded executor whose thread has the specified name.
   */
  public static ExecutorService newSingleThreadExecutor(final String threadName) {
    return Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, threadName);
      }
    });
  }

  /**
   * Instantiates a scheduled executor of specified pool size whose threads have the specified name.
   */
  public static ScheduledExecutorService newScheduledThreadExecutor(final int poolSize,
                                                                    final String threadName) {
    return Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {
      private int threadCount = 0;

      @Override
      public Thread newThread(Runnable r) {
        threadCount++;
        return new Thread(r, threadName + "-" + threadCount);
      }
    });
  }


  /**
   * A utility method to enable/disable capture of screen shot and screen record.
   *
   * @param window - The window with which the activity is attached.
   * @param enable - Whether to enable or disable the screen capture.
   */
  public static void enableOrDisableScreenCapture(Window window, boolean enable) {
    if (window == null) {
      return;
    }
    if (enable) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    } else {
      window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
  }

  //Centered Crop with rounded edge
  @NonNull
  public static Bitmap getRoundedBitmap(@NonNull Bitmap bitmap,
                                        @Nonnegative int width, @Nonnegative int height,
                                        @Nonnegative int radiusPx) {

    Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Paint paint = new Paint();
    final Rect rectDest = new Rect(0, 0, width, height);
    final RectF rectF = new RectF(rectDest);

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(Color.WHITE);
    canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

    float xScale = (float) width / bitmap.getWidth();
    float yScale = (float) height / bitmap.getHeight();
    float scale = Math.max(xScale, yScale);

    // Now get the size of the source bitmap when scaled
    float scaledWidth = scale * bitmap.getWidth();
    float scaledHeight = scale * bitmap.getHeight();

    // Find top left coordinates if the scaled bitmap should be centered in the new size give by
    // the parameters
    float left = (width - scaledWidth) * 0.5f;
    float top = (height - scaledHeight) * 0.5f;

    Matrix m = new Matrix();
    m.setScale(scale, scale);
    m.postTranslate(left, top);
    canvas.drawBitmap(bitmap, m, paint);

    return output;
  }

  /**
   * Helper method to create rounded rect drawable programmatically with a fill color and stroke
   * whose width and color can be specified.
   *
   * @param radius      radius of rounded corners
   * @param fillColor   fill color for the rect
   * @param strokeWidth border for the drawable
   * @param strokeColor border color
   * @return Rounded rect shape drawable
   */
  public static Drawable makeRoundedRectDrawable(final int radius, final int fillColor,
                                                 final int strokeWidth, final int strokeColor) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    if (strokeWidth > 0) {
      drawable.setStroke(strokeWidth, strokeColor);
    }
    drawable.setColor(fillColor);
    drawable.setCornerRadius(radius);
    return drawable;
  }


  /**
   * Helper method to create rounded rect drawable programmatically with a fill color and stroke
   * whose width and color can be specified.
   *
   * @param radii       radius of rounded corners
   * @param fillColor   fill color for the rect
   * @param strokeWidth border for the drawable
   * @param strokeColor border color
   * @return Rounded rect shape drawable
   */
  public static Drawable makeRoundedRectDrawable(final float[] radii, final int fillColor,
                                                 final int strokeWidth, final int strokeColor) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    if (strokeWidth > 0) {
      drawable.setStroke(strokeWidth, strokeColor);
    }
    drawable.setColor(fillColor);
    drawable.setCornerRadii(radii);
    return drawable;
  }

  /**
   * Helper method to create rounded rect drawable programaticall with a gradient and stroke
   * whose width and color can be specified
   * @param radius radius of rounded corners
   * @param startColor Start color of the gradient
   * @param endColor end color of the gradient
   * @param orientation Direction of the gradient
   * @param strokeWidth border width
   * @param strokeColor border color
   * @return
   */
  public static Drawable makeRoundedRectDrawable(final int radius,
                                                 final String startColor,
                                                 final String endColor,
                                                 final GradientDrawable.Orientation orientation,
                                                 final int strokeWidth,
                                                 final int strokeColor ) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    if (strokeWidth > 0) {
      drawable.setStroke(strokeWidth, strokeColor);
    }
    int[] colorValues = ViewUtils.getColors(Color.WHITE, startColor, endColor);
    drawable.setColors(colorValues);
    drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    drawable.setOrientation(orientation);
    drawable.setCornerRadius(radius);
    return drawable;
  }

  public static String dateToString(Date date) {
    try {
      return date.toString();
    } catch (AssertionError ex) {
      return Constants.EMPTY_STRING;
    }
  }

  /**
   * Utility function to check if given service is running
   *
   * @param context - context
   * @param serviceClass - service to check
   * @return - true if running else false
   */
  public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }


  /**
   * Helper method to restrict some features while monkey is running. This method checks the
   * BuildConfig whether or not Monkey is restricted.
   *
   * @return true if monkey restriction is ON. false otherwise.
   */
  public static boolean isInRestrictedMonkeyMode() {
    return AppConfig.getInstance().isMonkeyRestricted() && ActivityManager.isUserAMonkey();
  }

  public static boolean isAppVersionGreaterthanOrEqualto(String v1, String v2) {

    String verDelimiter = "\\.";

    try {

      if (CommonUtils.isEmpty(v1) && CommonUtils.isEmpty(v2)) {
        return true;
      } else if (CommonUtils.isEmpty(v1)) {
        // Means v2 is still there
        return false;
      } else if (CommonUtils.isEmpty(v2)) {
        // Means some part of v1 is still there
        return true;
      } else {
        List<String> v1asList = new ArrayList(Arrays.asList(v1.split(verDelimiter)));
        List<String> v2asList = new ArrayList(Arrays.asList(v2.split(verDelimiter)));

        if (!CommonUtils.isEmpty(v1asList) && !CommonUtils.isEmpty(v2asList)) {
          Long currentVerV1 = Long.parseLong(v1asList.remove(0));
          Long currentVerV2 = Long.parseLong(v2asList.remove(0));
          if (currentVerV1.equals(currentVerV2)) {

            return isAppVersionGreaterthanOrEqualto(DataUtil.parseAsString(v1asList, "."),
                DataUtil.parseAsString(v2asList, "."));

          } else {
            return currentVerV1 > currentVerV2;
          }
        }

      }
    } catch (Exception e) {
      Logger.e(LOG_TAG, "Error comparing version " + v1 + " " + v2 + " " + e);
    }
    return false;

  }
  /**
   * if connections speed is slow or 2G return true
   *
   * @return true/false
   */
  public static boolean isConnectionSlowOr2G() {
    return (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication()) ==
        ConnectionSpeed.SLOW) || (ConnectionType.TWO_G == ConnectionType.fromName
        (ConnectionInfoHelper.getConnectionType()));
  }

  public static boolean devEventsEnabled() {
    boolean logCollectionInProgress = PreferenceManager.getBoolean(Constants
        .LOG_COLLECTION_IN_PROGRESS, false);
    boolean enabledGlobally = PreferenceManager.getPreference(GenericAppStatePreference
        .ENABLE_PERFORMANCE_ANALYTICS, false);
    return logCollectionInProgress || enabledGlobally;
  }
  /**
   * <p>Takes a preference and a default map.</p>
   * <p>Reads map from preference</p>
   * <p>Creates a subject that accepts map-entries </p>
   * <p> Adds a operator that accumulates the posted map-entries to the map(read above), and writes it to preference </p>
   * <p>Returns the subject, observable pair</p>
   *
   * @param <K>        typeOf map key
   * @param <V>        typeOf map value
   * @param defValue   used if preference is not present
   * @param preference Preference to read-from and write-to
   * @param type
   * @return Consumer, Observable pair. Map entries passed to consumer will be persisted. Use observable to listen to changes.
   */
  @NonNull
  public static <K, V> Pair<Consumer<Pair<K, V>>, Observable<HashMap<K, V>>>
  buildAutoSaveMapForPref(@NonNull HashMap<K, V> defValue, @NonNull SavedPreference preference,
                          Type type) {
    String saved = PreferenceManager.getPreference(preference, Constants.EMPTY_STRING);
    if (saved == null || CommonUtils.isEmpty(saved.trim())) {
      saved = Constants.EMPTY_STRING;
    }

    // use default if preference does not exist
    final HashMap<K, V> mapFromPrefs =
        !CommonUtils.isEmpty(saved) ? CommonUtils.GSON.fromJson(saved, type) : defValue;
    // use default if gson parsing fails
    final HashMap<K, V> map = mapFromPrefs != null ? mapFromPrefs : defValue;
    final BehaviorSubject<Pair<K, V>> sub = BehaviorSubject.create();
    final Observable<HashMap<K, V>> obs = sub.scan(map, (acc, pair) -> {
      acc.put(pair.first, pair.second);
      Logger.d(LOG_TAG, "buildAutoSaveMapForPref: writing " + acc);
      PreferenceManager.savePreference(preference, CommonUtils.GSON.toJson(acc, type)); //
      // convert and
      // save
      return acc;
    }).publish().autoConnect();
    return Pair.create(sub::onNext, obs);
  }

  public static boolean areNotificationsEnabled() {
    String CHECK_OP_NO_THROW = "checkOpNoThrow";
    String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    ApplicationInfo appInfo = CommonUtils.getApplication().getApplicationInfo();

    String pkg = CommonUtils.getApplication().getPackageName();
    int uid = appInfo.uid;
    Class appOpsClass = null; /* Context.APP_OPS_MANAGER */
    Object appOps = CommonUtils.getApplication().getSystemService(Context.APP_OPS_SERVICE);
    try {
      appOpsClass = Class.forName("android.app.AppOpsManager");
      Method checkOpNoThrowMethod =
          appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
      Field opValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
      int value = opValue.getInt(Integer.class);
      Object result = checkOpNoThrowMethod.invoke(appOps, value, uid, pkg);
      return Integer.parseInt(result.toString()) == AppOpsManager.MODE_ALLOWED;
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return true;
  }

  /**
   * Utility method to Construct Intent for play store page of any package, given link to its
   * market scheme link and playstore link. If Playstore app is installed and enabled on the
   * device, playstore app is launched, using marketLink url. Else, playStoreLink will be used to
   * open the url with any browser
   *
   * @param activity      caller activity
   * @param playStoreLink Playstore type of link. Link to any package can be made using Constants
   *                      .APP_PLAY_STORE_LINK_TEMPLATE and appending the package name.
   * @param marketLink    market scheme link. Link to any package can be made using Constants
   *                      .MARKET_LINK_TEMPLATE
   */
  public static Intent getPlayStoreIntentForApp(final @NonNull Activity activity, final @NonNull
      String playStoreLink, final @NonNull String marketLink) {
    boolean useMarketLink = false;
    Intent openPlayStoreIntent = null;
    if (!AndroidUtils.isAppDisabled(Constants.GOOGLE_PLAYSTORE_PACKAGE)) {
      openPlayStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketLink));
      //First priority to launch the playstore app, if its enabled and able to handle the marketLink
      if (activity.getPackageManager().resolveActivity(openPlayStoreIntent, PackageManager
          .MATCH_DEFAULT_ONLY) != null) {
        useMarketLink = true;
        openPlayStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openPlayStoreIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      }
    }
    //If Playstore app is disabled/unavailable, try launching the url with any browser.
    if (!useMarketLink) {
      openPlayStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreLink));
    }
    return openPlayStoreIntent;
  }

  /**
   * creates mutable arraylist from collection, if predicate satisfies
   */
  public static <T> ArrayList<T> arrayListOf(@Nullable Predicate<T> predicate, Collection<T> args) {
    ArrayList<T> list = new ArrayList<>();
    for (T arg : args) {
      try {
        if (predicate == null || predicate.test(arg)) {
          list.add(arg);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return list;
  }

  /**
   * creates mutable arraylist from array, if predicate satisfies
   */
  public static <T> ArrayList<T> arrayListOf(@Nullable Predicate<T> predicate, T[] args) {
    ArrayList<T> list = new ArrayList<>();
    for (T arg : args) {
      try {
        if (predicate == null || predicate.test(arg)) {
          list.add(arg);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return list;
  }

  public static String v(Context context) {
    return Constants.EMPTY_STRING;
  }

  /**
   * function to get sharable app detail list.
   */
  public static List<ShareAppDetails> getShareableApps() {
    List<ShareAppDetails> appDetails = new ArrayList<>();

    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.setType(Constants.INTENT_TYPE_TEXT);
    PackageManager packageManager =
        CommonUtils.getApplication().getApplicationContext().getPackageManager();

    List<ResolveInfo> activityList =
        packageManager.queryIntentActivities(shareIntent, 0);

    Set<String> packageNames = new HashSet<>();
    for (final ResolveInfo resolveInfo : activityList) {
      packageNames.add(resolveInfo.activityInfo.applicationInfo.packageName);
    }

    for (final String packageName : packageNames) {
      ShareAppDetails shareAppDetail = ShareAppDetails.get(packageName);
      if (shareAppDetail == null) {
        continue;
      }
      appDetails.add(shareAppDetail);
    }

    Collections.sort(appDetails, Collections.<ShareAppDetails>reverseOrder());

    return appDetails;
  }

  public static long getLastUseTime(String packageName) {
    if (ShareApplication.FACEBOOK_APP_PACKAGE.getPackageName().equalsIgnoreCase(packageName)
        || ShareApplication.WHATS_APP_PACKAGE.getPackageName().equalsIgnoreCase(packageName)) {
      return System.currentTimeMillis();
    }
    return PreferenceManager.getLong(Constants.LAST_SHARE_TIME + packageName, 0L);
  }

  public static void saveLastUseTime(String packageName) {
    PreferenceManager.saveLong(Constants.LAST_SHARE_TIME + packageName, System.currentTimeMillis());
  }
  
  public static void updateStoryShareCount(int incrementCount) {
    int prevShareCount = PreferenceManager.getPreference(AppRatePreference.STORY_SHARED_COUNT,0);
    PreferenceManager.savePreference(AppRatePreference.STORY_SHARED_COUNT,prevShareCount+incrementCount);
  }

  public static String getAppendedShareUrl(String url) {

    Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
    uriBuilder.appendQueryParameter(ShareUTMHelper.SHORT_URL_PARAMETER_SOURCE,
        ShareUTMHelper.SHORT_URL_PARAMETER_VALUE);
    String token = PreferenceManager.getPreference(AppStatePreference.SHARE_TOKEN, Constants.EMPTY_STRING);
    if (!CommonUtils.isEmpty(token)) {
      uriBuilder.appendQueryParameter(ShareUTMHelper.SHORT_URL_PARAMETER_USER, token);
    }
    return uriBuilder.build().toString();
  }

  public static Watcher getWatcher() {
    if (null == watcherProvider) {
      return nullWatcher;
    }

    return watcherProvider.getWatcher();
  }

  public static void setWatcherProvider(WatcherProvider watcherProvider) {
    AndroidUtils.watcherProvider = watcherProvider;
  }

  /**
   * Utility method to launch play store page for any package, given link to its market scheme
   * link and playstore link. If Playstore app is installed and enabled on the device, playstore
   * app is launched, using marketLink url. Else, playStoreLink will be used to open the url
   * with any browser
   *
   * @param activity      caller activity
   * @param playStoreLink Playstore type of link. Link to any package can be made using Constants
   *                      .APP_PLAY_STORE_LINK_TEMPLATE and appending the package name.
   * @param marketLink    market scheme link. Link to any package can be made using Constants
   *                      .MARKET_LINK_TEMPLATE
   */
  public static void openPlayStoreForApp(final @NonNull Activity activity,
                                         final @NonNull String playStoreLink,
                                         final @NonNull String marketLink) {
    try {
      activity.startActivity(getPlayStoreIntentForApp(activity, playStoreLink, marketLink));
    } catch (ActivityNotFoundException e) {
      Logger.caughtException(e);
    }
  }

  public static boolean openPlayStoreForApp(Context context, String playStoreLink) {
    if (context == null || CommonUtils.isEmpty(playStoreLink) ||
        AndroidUtils.isAppDisabled(Constants.GOOGLE_PLAYSTORE_PACKAGE)) {
      return false;
    }
    Intent openPlayStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreLink));
    try {
      context.startActivity(openPlayStoreIntent);
      return true;
    } catch (ActivityNotFoundException e) {
      Logger.caughtException(e);
    }
    return false;
  }

  public static String a(Context context) {
    return Constants.NEWS_HOME_ACTION;
  }

  /**
   * Update color of drawable
   *
   * @param drawableResId
   * @param colorCode
   * @return
   */
  public static Drawable getTintedDrawable(@DrawableRes int drawableResId, String colorCode) {
    Integer color = ViewUtils.getColor(colorCode);
    if (color == null) {
      return null;
    }
    Drawable drawable =
        AppCompatResources.getDrawable(CommonUtils.getApplication(), drawableResId);
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    return drawable;
  }

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        Logger.caughtException(e);
      }
    }
  }

  @SuppressLint("ToastUsedDirectly")
  public static void startActivity(Activity activity, Intent intent) {
    try {
      activity.startActivity(intent);
    } catch (Exception e) {
      Logger.caughtException(e);
      Toast.makeText(activity, R.string.unexpected_error_message, Toast.LENGTH_SHORT).show();
    }
  }

  @SuppressLint("ToastUsedDirectly")
  public static void startSharingActivityForResult(Activity activity, Intent intent) {
    try {
      activity.startActivityForResult(intent, Constants.SHARE_REQUEST_CODE);
    } catch (ActivityNotFoundException | NullPointerException e) {
      Logger.caughtException(e);
      Toast.makeText(activity, R.string.unexpected_error_message, Toast.LENGTH_SHORT).show();
    }
  }


  public static float getMinAspectRatio() {
    int width = CommonUtils.getDimension(R.dimen.customizable_width);
    int height = CommonUtils.getDimension(R.dimen.customizable_max_height);
    if (height != 0) {
      return width / height;
    }
    return 0.0f;
  }

  public static float getMaxAspectRatio() {
    int width = CommonUtils.getDimension(R.dimen.customizable_width);
    int height = CommonUtils.getDimension(R.dimen.customizable_min_height);
    if (height != 0) {
      return width / height;
    }
    return 0.0f;
  }

  public static FollowViewState getViewState(FollowNavModel followNavModel) {
    if (followNavModel.getModel() == null) {
      return FollowViewState.NONE;
    }

    String userId = followNavModel.getUserId();
    switch (followNavModel.getModel()) {
      case BLOCKED: {
        return FollowViewState.FPV_BLOCKED;
      }
      case FOLLOWERS: {
        if (CommonUtils.isEmpty(userId) || CommonUtils.equals(AppUserPreferenceUtils.getUserId(), userId)) {
          return FollowViewState.FPV_FOLLOWERS;
        } else {
          return FollowViewState.TPV_FOLLOWERS;
        }
      }
      case FOLLOWING: {
        if (CommonUtils.isEmpty(userId) || CommonUtils.equals(AppUserPreferenceUtils.getUserId(), userId)) {
          return FollowViewState.FPV_FOLLOWING;
        } else {
          return FollowViewState.TPV_FOLLOWING;
        }
      }
    }
    return FollowViewState.NONE;
  }

  public static String getWifiMacAddress() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return "";
        }

        StringBuilder res1 = new StringBuilder();
        for (byte b : macBytes) {
          res1.append(Integer.toHexString(b & 0xFF) + ":");
        }

        if (res1.length() > 0) {
          res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
      }
    } catch (Exception ex) {

    }
    return null;
  }

  public static void launchExternalLink(Context activityContext, String url) {

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    try {
      activityContext.startActivity(intent);
    } catch (ActivityNotFoundException nfe) {
      Logger.caughtException(nfe);
    }
  }

  /**
   * Returns true if the device is locked or screen turned off (in case password not set)
   */
  public static boolean isDeviceLocked() {
    boolean isLocked;
    Context context = CommonUtils.getApplication();

    // First we check the locked state
    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    boolean inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode();

    if (inKeyguardRestrictedInputMode) {
      isLocked = true;
    } else {
      // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
      // so we need to check if screen on for this case

      PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      isLocked = !powerManager.isInteractive();
    }

    Logger.d(LOG_TAG, String.format("Now device is %s.", isLocked ? "locked" : "unlocked"));
    return isLocked;
  }

  public static boolean isNotificationDisabled() {
    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(CommonUtils.getApplication());
    boolean disabled = true;
    if (managerCompat.areNotificationsEnabled()) {
      final NotificationManager manager =
              (NotificationManager) CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
      List<NotificationChannel> channels = manager.getNotificationChannels();
      for (NotificationChannel c : channels) {
        if (!CommonUtils.isEmpty(c.getGroup()) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          @androidx.annotation.Nullable NotificationChannelGroup group =
                  manager.getNotificationChannelGroup(c.getGroup());
          if (group != null && group.isBlocked()) {
            continue;
          }
        }
        if (c.getImportance() != NotificationManager.IMPORTANCE_NONE) {
          disabled = false;
          break;
        }
      }
    }
    return disabled;
  }

}
