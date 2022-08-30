/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.helper.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.asset.VideoAsset;
import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.common.model.entity.model.StatusError;
import com.newshunt.sdk.network.NetworkSDK;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import okhttp3.Cache;

/**
 * Class with very generic utility methods
 *
 * @author arun.babu
 */
public class CommonUtils {
  public static boolean IS_IN_TEST_MODE = false;
  public static int followedLocationsCount = -1;
  private static boolean isLanguageSelectedOnLanguageCard = false;
  public static final Gson GSON = new Gson();
  private static final String UNEXPECTED_ERROR =
      "Unexpected error occurred. Please try again " + "later.";
  private static final String LOG_TAG = "CommonUtils";
  //WARNING: BE SURE TO OBSERVE ONLY WITH PROCESS LIFECYCLE OWNER OR REMOVE OBSERVER SOON AS DONE!!
  private static MutableLiveData<Boolean> lowMemory = new MutableLiveData<>();
  @SuppressLint("StaticFieldLeak")
  private static Application application;
    public static boolean isInFg = false; /*ActivityManager.getRunningAppProcesses() is discouraged.
     Hence using ProcessLifecyleOwner for this - https://stackoverflow.com/a/5862048*/
    public static void runInBackground(Runnable runnable) {
    AsyncTask.THREAD_POOL_EXECUTOR.execute(runnable);
  }

  public static void runInBackGroundSerially(Runnable runnable){
    AsyncTask.SERIAL_EXECUTOR.execute(runnable);
  }
  private static boolean isMainProcess;
  public static boolean workManagerInitFailed = false;

  /**
   * Check if a string is empty or null
   *
   * @param str - input string
   * @return true if string is empty or null; false otherwise
   */
  public static boolean isEmpty(@Nullable String str) {
    return str == null || str.trim().equals(Constants.EMPTY_STRING);
  }

  public static boolean isEmpty(@Nullable Collection collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  public static boolean isEmpty(Map map) {
    return map == null || map.isEmpty();
  }

  /**
   * Utility function to copy all from MapB to MapA
   * (NOTE : if destinationMap is null, then copy of sourceMap will be returned)
   *
   * @param destinationMap - destination map
   * @param sourceMap      - sourceMap
   * @param <T>            - generic key T
   * @param <V>            - generic value V
   * @return - merged Map
   */
  public static <T, V> Map<T, V> copyAll(Map<T, V> destinationMap, Map<T, V> sourceMap) {
    if (isEmpty(sourceMap)) {
      return destinationMap;
    }

    if (destinationMap == null) {
      destinationMap = new HashMap<>();
    }

    destinationMap.putAll(sourceMap);
    return destinationMap;
  }

  /**
   * Null-safe equivalent of {@code a.equals(b)}.
   *
   * @param a - object a
   * @param b - object b
   * @return - return true if equal else false
   */
  public static boolean equals(Object a, Object b) {
    return (a == null) ? (b == null) : (b != null && a.equals(b));
  }

  /**
   * Null-safe equivalent of string equalIgnore case
   *
   * @param a - string a
   * @param b - string b
   * @return - true if equalsIgnoreCase else false
   */
  public static boolean equalsIgnoreCase(String a, String b) {
    return (a == null) ? (b == null) : (b != null && a.equalsIgnoreCase(b));
  }

  public static boolean isValidInteger(String str) {
    if (isEmpty(str)) {
      return false;
    }
    return str.matches("[0-9]+");
  }

  public static int getDpFromPixels(int pixel, Context context) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pixel / scale);
  }

  public static boolean isEmptyWithoutTrim(String str) {
    return !(str != null && (str.length() > 0));
  }

  /**
   * method to check email and redirecting user to next screen
   *
   * @param input entered email address
   * @return true id input is valid else false
   */
  public static boolean validateEmailAddress(String input) {
    return Patterns.EMAIL_ADDRESS.matcher(input).matches();
  }

  /**
   * This method convets dp unit to equivalent device specific value in
   * pixels.
   *
   * @param dp      A value in dp(Device independent pixels) unit. Which we need
   *                to convert into pixels
   * @param context Context to get resources and device specific display metrics
   * @return A int value to represent Pixels equivalent to dp according to
   * device
   */
  public static int getPixelFromDP(int dp, Context context) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dp * scale);
  }

  public static int getStatusBarHeight(Context aContext) {
    int result = 0;
    if (null != aContext) {
      int resourceId = aContext.getResources().getIdentifier(
          "status_bar_height", "dimen", "android");
      if (resourceId > 0) {
        result = aContext.getResources().getDimensionPixelSize(
            resourceId);
      }
    }
    return result;
  }

  /**
   * This method is used to delete screenshots from external cache directory
   */
  public static boolean deleteTempFiles(File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory()) {
            deleteTempFiles(f);
          } else {
            f.delete();
          }
        }
      }
    }
    return file.delete();
  }

  public static boolean isVisibleAndContainsCoordinate(View aView, MotionEvent ev) {
    boolean isVisibleAndContainsCoordinate = false;
    if (null != aView) {
      if (View.VISIBLE == aView.getVisibility()) {
        int y = (int) ev.getY();
        int x = (int) ev.getX();
        Rect rect = new Rect();
        aView.getDrawingRect(rect);
        isVisibleAndContainsCoordinate = rect.contains(x, y);
      }
    }
    return isVisibleAndContainsCoordinate;
  }

  public static boolean isNetworkAvailable(Context aContext) {
    if (aContext == null) {
      return false;
    }
    ConnectivityManager connectivityManager = (ConnectivityManager) aContext.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public static int getDeviceScreenWidth() {
    if (application == null || application.getResources() == null ||
        application.getResources().getDisplayMetrics() == null) {
      return 0;
    }
    return application.getResources().getDisplayMetrics().widthPixels;
  }

  public static int getDeviceScreenWidthInDp() {
    float density = getDeviceDensity();
    if (density == 0) {
      density = 1;
    }

    return (int) (getDeviceScreenWidth() / density);
  }

  public static int getDeviceScreenHeight() {
    if (application == null || application.getResources() == null ||
        application.getResources().getDisplayMetrics() == null) {
      return 0;
    }
    return application.getResources().getDisplayMetrics().heightPixels;
  }

  public static int getAppWindowHeight(Context context) {
    return getDeviceScreenHeight() - getStatusBarHeight(context);
  }

  public static float getDeviceDensity() {
    if (application == null || application.getResources() == null ||
        application.getResources().getDisplayMetrics() == null) {
      return 0;
    }
    return application.getResources().getDisplayMetrics().density;
  }

  public static Drawable getDrawable(Context context, int resId) {
    return context.getDrawable(resId);
  }

  public static Application getApplication() {
    return application;
  }

  public static void setApplication(Application application) {
    CommonUtils.application = application;
  }

  @NonNull
  public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
    final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bmp);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bmp;
  }

  /**
   * Utility method for reading string from resources. Application context will be used.
   *
   * @param resId      - Resource id for the format string
   * @param formatArgs - (Optional) The format arguments that will be used for substitution.
   */
  public static String getString(int resId, Object... formatArgs) {
    return application.getString(resId, formatArgs);
  }

  /**
   * Utility method for reading plural strings from resources.
   *
   * @param resId      - plural string resource id
   * @param quantity   - qualifying quantity to pick which string from
   * @param formatArgs - format args in the string picked up
   * @return - a quantified string
   */
  public static String getQuantifiedString(int resId, int quantity, Object... formatArgs) {
    return application.getResources().getQuantityString(resId, quantity, formatArgs);
  }

  /**
   * Utility method for reading string array from resources. Application context will be used.
   *
   * @param resId - Resource id for the format string
   */
  public static String[] getStringArray(int resId) {
    return application.getResources().getStringArray(resId);
  }

  /**
   * Utility Method to get the string from the resource name
   *
   * @param resource Resource name of string
   * @return string value of the resource
   */
  public static String getStringFromResource(String resource) {
    String packageName = getApplication().getPackageName();
    int resId = getApplication().getResources().getIdentifier(resource, Constants.TEXT_STRING,
        packageName);
    if (resId > 0) {
      return getString(resId);
    }
    return null;
  }

  public static int getColor(int resId) {
    return application.getResources().getColor(resId);
  }

  public static int getInteger(int resId) {
    return application.getResources().getInteger(resId);
  }

  public static float getFloat(int resId) {
    TypedValue outValue = new TypedValue();
    application.getResources().getValue(resId, outValue, true);
    return outValue.getFloat();
  }

  public static int getDimension(int resId) {
    return application.getResources().getDimensionPixelSize(resId);
  }

  public static int getDimensionInDp(int resId) {
    float density = getDeviceDensity();
    if (density == 0) {
      density = 1;
    }

    return (int) (getDimension(resId) / density);
  }

  public static Drawable getDrawable(int resId) {
    return AppCompatResources.getDrawable(CommonUtils.getApplication(), resId);
  }

  public static Status createUnexpectedErrorStatus() {
    String code = Constants.EMPTY_STRING;
    //TODO:(arun.babu) to make it work using R.strings
    //String message = CommonUtils.getString(R.string.unexpected_error_message);
    String message = UNEXPECTED_ERROR;
    String description = Constants.EMPTY_STRING;
    String codeType = StatusError.UNEXPECTED_ERROR.getName();
    return new Status(code, message);
  }

  /**
   * Utility function to round the given long value, to nearest multiple of round value. floor
   * middle boolean is to tell, when given value falls at the middle of the two multiples of the
   * round value, then floor to lesser multiple, else ceil to greater multiple.
   * <p>
   * For eg,
   * if value = 90000, round = 60000;
   * floor middle = true:
   * half = (60000/2) - 1 = 29999;
   * roundedValue = ((90000 + 29999)/60000) * 60000 = 60000(multiple of 60000 < 90000);
   * floor middle = false:
   * half = (60000/2) = 30000;
   * roundedValue = ((90000 + 30000)/60000) * 60000 = 120000(multiple of 60000 > 90000);
   *
   * @param value       - value to round
   * @param round       - nearest multiple of the value to round
   * @param floorMiddle - true if given number is middle and to floor to lesser multiple of round
   * @return - round value
   */
  public static long roundToNearest(long value, long round, boolean floorMiddle) {
    if (value <= 0 || round <= 1) {
      return value;
    }
    long half = round / 2 - (floorMiddle ? 1 : 0);
    return ((value + half) / round) * round;
  }

  public static String delay(long submitTime) {
    return String.format("(%.1fms)", (System.nanoTime() - submitTime) / 1e6d);
  }

  public static boolean isTimeExpired(long time, long gap) {
    return isTimeExpired(time, gap, false);
  }

  public static boolean isTimeExpired(long time, long gap, boolean useSystemElapsedTime) {
    if (time < 0 || gap < 0) {
      throw new IllegalArgumentException(String.format("illegal arguments: time - %d, gap - %d",
          time, gap));
    }

    if (useSystemElapsedTime) {
      return time < (SystemClock.elapsedRealtime() - gap);
    }

    return time < (System.currentTimeMillis() - gap);
  }

  public static boolean isCurrentTimeInBounds(long startTime, long endTime) {
    if (startTime >= endTime) {
      return false;
    }
    long currentTime = System.currentTimeMillis();
    return currentTime >= startTime && currentTime < endTime;
  }

  public static int getMinScrollForMoreStoriesTooltip() {
    Double heightForScroll = getDeviceScreenHeight() * 0.05; // 5% of screen height
    return heightForScroll.intValue();
  }

  /**
   * This method returns the height of the activity window.
   * If the activity is not full screen then the status bar height is not included in it
   *
   * @param activity
   * @return :- Height of the activity window
   */
  public static int getScreenHeight(Activity activity) {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int screenHeight = displaymetrics.heightPixels;
    return screenHeight;
  }

  /**
   * This method returns the height of the activity window including the status and navigation bars.
   *
   * @param activity
   * @return :- Height of the activity window
   */
  public static int getRealScreenHeight(Activity activity) {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
    return displaymetrics.heightPixels;
  }

  public static int getScreenWidth(Activity activity) {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int screenWidth = displaymetrics.widthPixels;
    return screenWidth;
  }

  /**
   * Returns the action bar height for the activity
   *
   * @param activity
   * @return
   */
  public static int getActionBarHeight(Activity activity) {
    int actionBarHeight = 0;
    TypedValue tv = new TypedValue();
    if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
      actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources()
          .getDisplayMetrics());
    }
    return actionBarHeight;
  }

  public static boolean isvalidNotificationId(String id) {
    return !Constants.NOTIFICATION_DEFAULT_ID.equals(id);
  }

  public static boolean isvalidNotificationTimestamp(long timestamp) {
    return Constants.NOTIFICATION_DEFAULT_TIMESTAMP != timestamp;
  }

  public static boolean isValidChapterNumber(int chapterIndex) {
    return chapterIndex >= 0;
  }

  /**
   * Helper method to get resource id from attribute
   *
   * @param context context
   * @param attr    attribute id
   * @return returns the id of the resource
   */
  public static int getResourceIdFromAttribute(Context context, final int attr) {
    if (attr == 0) {
      return 0;
    }
    final TypedValue typedvalueattr = new TypedValue();
    context.getTheme().resolveAttribute(attr, typedvalueattr, true);
    return typedvalueattr.resourceId;
  }

  public static String toLowerCase(String data) {
    return isEmpty(data) ? data : data.toLowerCase();
  }

  /**
   * Utility method to check whether the two lists are equal
   *
   * @param firstList  the first list to compare
   * @param secondList the second list to compare
   * @return true if both the list are empty or same
   */
  public static boolean equalsList(List<?> firstList, List<?> secondList) {

    if (isEmpty(firstList)) {
      return isEmpty(secondList);
    }

    if (isEmpty(secondList)) {
      return isEmpty(firstList);
    }

    if (firstList.size() != secondList.size()) {
      return false;
    }

    for (int i = 0; i < firstList.size(); i++) {
      if (!equals(firstList.get(i), secondList.get(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Utility function to check if weakly referenced object still available
   *
   * @param weakReference to the object
   * @param <T>           - any type
   * @return - true if weakly referenced object not yet garbage collected else false
   */
  public static <T> boolean isWeakReferencedObjectAvailable(WeakReference<T> weakReference) {
    return weakReference != null && weakReference.get() != null;
  }

  public static Drawable getTintedDrawable(@DrawableRes int drawableResId,
                                           @ColorRes int colorResId) {
    Drawable drawable =
        AppCompatResources.getDrawable(CommonUtils.getApplication(), drawableResId);
    int color = ContextCompat.getColor(CommonUtils.getApplication(), colorResId);
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    return drawable;
  }

  public static Drawable getTintedDrawableColorInt(@DrawableRes int drawableResId,
                                                   @ColorInt int color) {
    Drawable drawable =
        AppCompatResources.getDrawable(CommonUtils.getApplication(), drawableResId);
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    return drawable;
  }

  public static String formatForPath(String path) {
    if (path == null) {
      return null;
    }
    if (path.startsWith(Constants.RETROFIT_BASE_URL_END_TOKEN)) {
      path = path.substring(1, path.length());
    }
    return path;
  }

  public static String formatBaseUrlForRetrofit(String urlEndPoint) {
    if (!urlEndPoint.endsWith(Constants.RETROFIT_BASE_URL_END_TOKEN)) {
      urlEndPoint = urlEndPoint + Constants.RETROFIT_BASE_URL_END_TOKEN;
    }
    return urlEndPoint;
  }

  public static boolean isPlayStoreUrl(String url) {
    if (CommonUtils.isEmpty(url)) {
      return false;
    }

    Uri uri = Uri.parse(url);
    if (uri == null || uri.getScheme() == null) {
      return false;
    }
    switch (uri.getScheme()) {
      case Constants.URL_MARKET_FORMAT:
        return true;
      case Constants.URL_HTTP_FORMAT:
      case Constants.URL_HTTPS_FORMAT:
        if (Constants.APP_PLAY_STORE_HOST.equals(uri.getHost())) {
          return true;
        }
    }
    return false;
  }

  /**
   * Kotlin code can use {@code Int.coerceIn()}, which has a similar implementation.
   *
   * @return position within [min, max] interval. Requires min<=max
   */
  @Deprecated
  public static int getIndexInRange(final int min, final int max, final int index) {
    if (min > max) {
      throw new IllegalArgumentException(
          String.format("require min<=max. received  min:%d, max:%d, index:%d", min, max, index));
    }
    return index < min ? min : index > max ? max : index;
  }

  /**
   * returns first non null value among p1, p2
   * throws, if both are null
   */
  public static <T> T firstNonNull(T p1, T p2) {
    return p1 != null ? p1 : checkNotNull(p2);
  }

  public static void check(boolean expression, @NonNull String message) {
    if (!expression) {
      throw new IllegalStateException(message);
    }
  }

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException(Constants.EMPTY_STRING);
    } else {
      return reference;
    }
  }

  /**
   * Utility method to add an item to list and make sure the list size does not exceed the limit.
   * If size exceeds the limit, oldest item is removed from the list
   *
   * @param inputList Input list to add the item to
   * @param item      New item to add
   * @param sizeLimit Size limit of the list
   * @param <T>       Generic parameter
   * @return updated list after addition
   */
  public static <T> List<T> addItemToListWithLimit(@NonNull final List<T> inputList,
                                                   @NonNull final T item, final int sizeLimit) {
    inputList.add(item);
    if (inputList.size() > sizeLimit) {
      int sizeDiff = inputList.size() - sizeLimit;
      Iterator<T> iterator = inputList.iterator();
      while (iterator.hasNext() && sizeDiff > 0) {
        iterator.next();
        iterator.remove();
        sizeDiff--;
      }
    }
    return inputList;
  }

  public static File getCacheDir(String dirName) {
    File cacheDir = CommonUtils.getApplication().getExternalCacheDir();
    if (null == cacheDir) {
      cacheDir = CommonUtils.getApplication().getCacheDir();
    }

    File httpCacheDir = null;
    if (cacheDir != null) {
      httpCacheDir = new File(cacheDir, dirName);
    }

    if (httpCacheDir != null) {
      httpCacheDir.mkdirs();
    }

    return httpCacheDir;
  }

  public static boolean isMemoryLow() {
    if (lowMemory.getValue() == Boolean.TRUE) {
      return true;
    }

    ActivityManager activityManager =
        (ActivityManager) CommonUtils.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager != null) {
      return activityManager.isLowRamDevice();
    }

    return false;
  }

  public static void setLowMemory(boolean lowMemory) {
    CommonUtils.lowMemory.postValue(lowMemory);
  }

  //TODO to be removed. Added for mock
  public static String readFromAsset(String fileName) {
    StringBuilder returnString = new StringBuilder();
    InputStream fIn = null;
    InputStreamReader isr = null;
    BufferedReader input = null;
    try {
      fIn = CommonUtils.getApplication().getAssets().open(fileName);
      isr = new InputStreamReader(fIn);
      input = new BufferedReader(isr);
      String line = "";
      while ((line = input.readLine()) != null) {
        returnString.append(line);
      }
    } catch (Exception e) {
      e.getMessage();
    } finally {
      try {
        if (isr != null) {
          isr.close();
        }
        if (fIn != null) {
          fIn.close();
        }
        if (input != null) {
          input.close();
        }
      } catch (Exception e2) {
        e2.getMessage();
      }
    }
    return returnString.toString();
  }

  public static boolean isActivityInMultiWindowMode(final Activity activity) {
    return (activity != null && activity.isInMultiWindowMode());
  }

  public static boolean isMainProcess() {
    return isMainProcess;
  }

  public static void setIsMainProcess(boolean isMainProcess) {
    CommonUtils.isMainProcess = isMainProcess;
  }

  public static LiveData<Boolean> getLowMemoryLiveData() {
    return lowMemory;
  }

  /**
   * Format the handle for display purposes. Handle is prefixed with @ in all screens
   * @param handle handle of the user
   * @return null if handle input is empty. @<handle> otherwise
   */
  public static @Nullable String formatHandleForDisplay(@Nullable String handle) {
    if (CommonUtils.isEmpty(handle)) {
      return null;
    }
    return Constants.AT_SYMBOL + handle;
  }

  public static boolean isEmptyOrSingle(@Nullable Collection collection) {
    return collection == null || collection.isEmpty() || collection.size() == 1;
  }

  /**
   * Compressing the apps on device json string
   *
   * @param string
   * @return
   * @throws IOException
   */
  public static byte[] compressString(String string) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
    GZIPOutputStream gos = new GZIPOutputStream(os);
    gos.write(string.getBytes());
    gos.close();
    byte[] compressed = os.toByteArray();
    os.close();
    return compressed;
  }

  /**
   * Decompressing the compressed byte array
   *
   * @param compressedString
   * @return
   * @throws IOException
   */
  public static String decompress(String compressedString) throws IOException {
    byte[] compressed = Base64.decode(compressedString, Base64.NO_WRAP);
    final int BUFFER_SIZE = 32;
    ByteArrayInputStream is = new ByteArrayInputStream(compressed);
    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
    StringBuilder string = new StringBuilder();
    byte[] data = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = gis.read(data)) != -1) {
      string.append(new String(data, 0, bytesRead));
    }
    gis.close();
    is.close();
    return string.toString();
  }

  public static long getCurrentEpochTimeinUTCinMillis() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
  }

  public static String getCurrentDateInString() {
    Date date = Calendar.getInstance().getTime();
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    return fmt.format(date);
  }

  public static boolean isDownloadableUrl(VideoAsset videoAsset) {
    if(videoAsset == null || CommonUtils.isEmpty(videoAsset.getDownloadVideoUrl())
        || !videoAsset.getDownloadVideoUrl().startsWith("http")
        || !videoAsset.getDownloadable() || videoAsset.getLiveStream() ) {
      return false;
    }
    return true;
  }

  /*
 Based on the "DND Start Time" and "DND End Time"  and the date for the scheduled job, this
 method returns true if the scheduled job falls between DND Time intervals, false otherwise.
  */
  public static boolean isJobInDND(Date dndStartDate, Date dndEndDate, Date
      scheduledDate) {

    if (dndStartDate == null || dndEndDate == null || scheduledDate == null) {
      return false;
    }
    //It means that the scheduled Date is between dndStartDate and dndEndDate
    if (scheduledDate.compareTo(dndStartDate) > 0 && dndEndDate.compareTo(scheduledDate) > 0) {
      return true;
    }
    return false;
  }

  //Based on this answer. http://stackoverflow.com/a/4047790/1237141
  public static double getBatteryPercent() {
    Intent batteryIntent = CommonUtils.getApplication().registerReceiver(null,
        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    double level = -1;
    if (batteryIntent == null) {
      return level;
    }
    int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    double scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    if (rawlevel >= 0 && scale > 0) {
      level = (rawlevel / scale) * 100;
    }
    return level;
  }

  //Based on this answer http://stackoverflow.com/a/7156252/1237141
  public static boolean isDeviceCharging() {
    Intent intent = CommonUtils.getApplication().registerReceiver(null, new IntentFilter(Intent
        .ACTION_BATTERY_CHANGED));
    if (intent == null) {
      return true;
    }
    int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    return plugged == BatteryManager.BATTERY_PLUGGED_AC ||
        plugged == BatteryManager.BATTERY_PLUGGED_USB;
  }


  public static String getLangCode(String language, int resId) {

    String[] langList = getStringArray(resId);
    if (langList == null) {
      return null;
    }

    for (String item: langList) {
      String[] languageMap = item.split(Constants.COMMA_CHARACTER);
      if (languageMap.length < 2) {
        continue;
      }

      if (CommonUtils.equalsIgnoreCase(language, languageMap[0])) {
        return languageMap[1];
      }
    }
    return null;
  }

  public static int getFollowedLocationsCount()
  {
    return followedLocationsCount;
  }

  /**
   * Returns true if t2 is next calendar day of t1; false, otherwise
   * @param t1 in millisec
   * @param t2 in millisec
   */
  public static boolean isNextCalDay(long t1, long t2) {
    Calendar c1 = Calendar.getInstance(Locale.getDefault());
    c1.setTime(new Date(t1));
    Calendar c2 = Calendar.getInstance(Locale.getDefault());
    c2.setTime(new Date(t2));
    int d1 = c1.get(Calendar.DAY_OF_YEAR);
    int D1 = c1.getActualMaximum(Calendar.DAY_OF_YEAR);
    int quo = d1 / D1;
    int rem = d1 % D1;
    return c2.get(Calendar.DAY_OF_YEAR) == rem + 1
            && c2.get(Calendar.YEAR) == c1.get(Calendar.YEAR) + quo;
  }

  /**
   * Returns true if t2 is atleast after x calendar day of t1; false, otherwise
   * @param t1 in millisec
   * @param t2 in millisec
   */
  public static boolean isAtleastNextXCalDay(long t1, long t2,int x) {
    Calendar c1 = Calendar.getInstance(Locale.getDefault());
    c1.setTime(new Date(t1));
    Calendar c2 = Calendar.getInstance(Locale.getDefault());
    c2.setTime(new Date(t2));
    resetCalendar(c1);
    resetCalendar(c2);
    c1.add(Calendar.DAY_OF_MONTH,x);
    return c2.compareTo(c1) >= 0;
  }

  /**
   * Returns true if t2 is exact x days after t1; else false
   * @param t1 in millisec
   * @param t2 in millisec
   */
  public static boolean isNextXCalDay(long t1, long t2,int x) {
    Calendar c1 = Calendar.getInstance(Locale.getDefault());
    c1.setTime(new Date(t1));
    Calendar c2 = Calendar.getInstance(Locale.getDefault());
    c2.setTime(new Date(t2));
    resetCalendar(c1);
    resetCalendar(c2);
    c1.add(Calendar.DATE,x);
    return c2.compareTo(c1) == 0;
  }

  private static void resetCalendar(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND,0);
  }

  public static boolean getIsLanguageSelectedOnLanguageCard()
  {
    return isLanguageSelectedOnLanguageCard;
  }

  public static void setIsLanguageSelectedOnLanguageCard(Boolean isLanguageSelectedValue) {
    isLanguageSelectedOnLanguageCard = isLanguageSelectedValue;
  }


  public static Long bigBundlePut(Object o) {
    return BigBundle.BIG_BUNDLE.put(o);
  }

  public static Object bigBundleRemove(@Nullable Long key) {
    return BigBundle.BIG_BUNDLE.get(key, true);
  }

  public static String getToggelText(String action, String sourceName) {
    StringBuilder toggelText = new StringBuilder();
    toggelText.append(action).append(" ").append(sourceName);
    return toggelText.toString();
  }

  public static GradientDrawable getLeftCorneredtBackgroundDrawable(int color) {
    float mRadius = 16f;
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    drawable.setCornerRadii(new float[]{mRadius, mRadius, 0f, 0f, 0f, 0f, mRadius, mRadius});
    drawable.setColor(color);
    return drawable;
  }

  public static GradientDrawable getRightCorneredCtaBackgroundDrawable(int color) {
    float mRadius = 16f;
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    drawable.setCornerRadii(new float[]{0f,0f,mRadius, mRadius, mRadius, mRadius,0f,0f});
    drawable.setColor(color);
    return drawable;
  }

  //https://stackoverflow.com/a/51488931
  public static int makeDropDownMeasureSpec(int measureSpec) {
    int mode;
    if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
      mode = View.MeasureSpec.UNSPECIFIED;
    } else {
      mode = View.MeasureSpec.EXACTLY;
    }
    return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode);
  }

  /**
   * clear the OkHttp cache
   * should call from Background thread
   */
  public static void clearOkHttpCache() {
    try {
      Cache cache = NetworkSDK.clientBuilder().build().cache();
      Iterator<String> itr = cache.urls();
      while (itr.hasNext()) {
        itr.next();
        itr.remove();
      }
    } catch (Throwable e) {
    }
  }


  public static boolean cronetEnabledAndLoaded() {
    return false;
  }

}