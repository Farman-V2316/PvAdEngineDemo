/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.newshunt.common.LogCollectionReceiver;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.service.LogCollectionService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CommonUtils responsible for system log collection
 *
 * @author karthik.r
 */
public class LogCollectionUtils {
  public static final String TAG = LogCollectionUtils.class.getSimpleName();

  private static final long DEFAULT_LOG_COLLECTION_DURATION_IN_SECONDS = 60 * 60; // One hour

  private static final long DEFAULT_LOG_COLLECTION_FREQUENCY_IN_SECONDS = 10 * 60; // 10 min

  public static final String ACTION_LOG_COLLECTION =
      AppConfig.getInstance().getPackageName() + ".logCollectionBR";

  public static void handleLogCollection(Intent intent) {
    if (intent == null || intent.getData() == null ||
        !intent.getData().getPath().contains(Constants.LOG_COLLECTION_PATH)) {
      return;
    }

    handleLogCollection(intent.getData());
  }

  public static boolean isLogCollection(String logCollectionDeeplinkUrl) {
    if (!DataUtil.isEmpty(logCollectionDeeplinkUrl)) {
      Uri dataUri = Uri.parse(logCollectionDeeplinkUrl);
      if (dataUri != null && !CommonUtils.isEmpty(dataUri.getPath()) && dataUri.getPath().contains
          (Constants.LOG_COLLECTION_PATH)) {
        LogCollectionUtils.handleLogCollection(dataUri);
        return true;
      }
    }
    return false;
  }

  public static void handleLogCollection(Uri data) {
    String metaValue = data.getQueryParameter("meta");
    String decodedMetaValue = new String(Base64.decode(metaValue, 0));
    String[] keyValuePairs = decodedMetaValue.split("&");
    long duration = DEFAULT_LOG_COLLECTION_DURATION_IN_SECONDS;
    long frequency = DEFAULT_LOG_COLLECTION_FREQUENCY_IN_SECONDS;
    String clientId = null;
    String authToken = null;
    long expiryTime = 0L;
    String bufferSize = null;
    String logUploadUrl = null;
    try {
      for (String keyValuePair : keyValuePairs) {
        String[] param = keyValuePair.split("=");
        if (param == null || param.length != 2) {
          continue;
        }

        switch (param[0]) {
          case Constants.LOG_COLLECTION_DURATION:
            duration = Long.parseLong(param[1]);
            break;
          case Constants.LOG_COLLECTION_FREQUENCY:
            frequency = Long.parseLong(param[1]);
            break;
          case Constants.LOG_COLLECTION_CLIENT_ID:
            clientId = param[1];
            break;
          case Constants.LOG_COLLECTION_AUTH_TOKEN_PARAM:
            authToken = param[1];
            break;
          case Constants.LOG_COLLECTION_BUFFER_SIZE_PARAM:
            bufferSize = param[1];
            break;
          case Constants.LOG_COLLECTION_EXPIRY_PARAM:
            expiryTime = Long.parseLong(param[1]);
            break;
          case Constants.LOG_COLLECTION_UPLOAD_URL_PARAM:
            logUploadUrl = param[1];
            break;
          default:
            break;
        }
      }
    } catch (Exception ex) {
      Logger.e(TAG, "Exception parsing log collection url", ex);
      return;
    }

    if (expiryTime != 0L && expiryTime < System.currentTimeMillis()) {
      // Log collection url expired.
      Logger.w(TAG, "Log collection URL expired");
      return;
    }

    if (DataUtil.isEmpty(authToken)) {
      // Auth token is mandatory.
      Logger.w(TAG, "Auth token missing");
      return;
    }

    if (DataUtil.isEmpty(logUploadUrl)) {
      Logger.w(TAG, "Upload URL missing");
      return;
    }

    if (clientId != null &&
        !DataUtil.equalsIgnoreCase(ClientInfoHelper.getClientId(), clientId)) {
      // Request meant for other user. However proceed if no clientId found.
      Logger.w(TAG, "Client Id doesnt match. Found: " + clientId);
      return;
    }

    // Increase log buffer size, logcat -G 4096Kb
    try {
      String targetBufferSize = "4096Kb";
      if (bufferSize != null) {
        targetBufferSize = bufferSize;
      }
      Runtime.getRuntime().exec("logcat -G " + targetBufferSize);
    } catch (Exception e) {
      // Do nothing
    }

    // Log URL will be Base64 encoded before framing meta string
    logUploadUrl = new String(Base64.decode(logUploadUrl, 0));

    long logCollectionEndTime = System.currentTimeMillis() + (duration * 1000);
    PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_IN_PROGRESS, true);
    PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_UPLOADING_PENDING, true);
    PreferenceManager.saveLong(Constants.LOG_COLLECTION_END_TIME, logCollectionEndTime);
    PreferenceManager.saveString(Constants.LOG_COLLECTION_AUTH_TOKEN, authToken);
    PreferenceManager.saveString(Constants.LOG_COLLECTION_UPLOAD_URL_VALUE, logUploadUrl);

    AlarmManager am =
        (AlarmManager) CommonUtils.getApplication().getSystemService(Context.ALARM_SERVICE);
    PendingIntent operation = LogCollectionUtils.getLogCollectionOperation();
    am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), frequency * 1000, operation);

    Logger.setLoggerEnabled(true);
    Logger.setFileLogger(true);
    Logger.i(TAG, "Scheduled log upload at : " + logCollectionEndTime + ", with collection " +
        "frequency of : " + frequency + " s");
  }

  /**
   * Return PendingIntent for starting <code>LogCollectionService</code>
   */
  public static PendingIntent getLogCollectionOperation() {
    Intent intent = new Intent(CommonUtils.getApplication(), LogCollectionReceiver.class);
    intent.setAction(ACTION_LOG_COLLECTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return PendingIntent.getBroadcast(CommonUtils.getApplication(), 0, intent, 0);
  }

  /**
   * Check if any log collection or upload is pending, if so start <code>LogCollectionService</code>
   */
  public static void checkAndEnableLogCollection() {
    boolean logCollectionInProgress = PreferenceManager.getBoolean(Constants
        .LOG_COLLECTION_IN_PROGRESS, false);
    boolean logUploadingPending =
        PreferenceManager.getBoolean(Constants.LOG_COLLECTION_UPLOADING_PENDING, false);

    if (logCollectionInProgress || logUploadingPending) {
      Logger.setLoggerEnabled(true);
      Logger.setFileLogger(true);
      // Pending upload will be taken care by LogCollectionService
      LogCollectionService.startLogCollectionService(CommonUtils.getApplication());
    }
  }

  /**
   * Collect and append system logs to provided file.
   *
   * @param logFile File to append system logs.
   */
  public static void collectLogs(File logFile) {
    Logger.i(TAG, "Collecting logs");
    try {
      String lastCollectionTime = PreferenceManager.getString(Constants
          .LOG_COLLECTION_LAST_COLLECTION_TIME, Constants.EMPTY_STRING);
      String currentTime = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());
      PreferenceManager.saveString(Constants.LOG_COLLECTION_LAST_COLLECTION_TIME, currentTime);
      int pid = android.os.Process.myPid();
      String command = "logcat -d --pid=" + pid;
      if (!DataUtil.isEmpty(lastCollectionTime)) {
        command += " -t " + lastCollectionTime;
      }
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(process.getInputStream()));

      FileWriter fw = new FileWriter(logFile, true);
      String line;
      fw.write("********** Log Collected:\n");
      boolean logEmpty = true;
      while ((line = bufferedReader.readLine()) != null) {
        fw.write(line);
        fw.write("\n");
        logEmpty = false;
      }

      if (logEmpty) {
        Logger.i(TAG, "Logs are empty. Retrying without filter.");
        command = "logcat -d";
        process = Runtime.getRuntime().exec(command);
        bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = bufferedReader.readLine()) != null) {
          fw.write(line);
          fw.write("\n");
        }
      }

      fw.close();
    } catch (IOException e) {
      Logger.e(TAG, "CollectLogs failed", e);
    }
  }

  public static File getWriteableFile(String fileName) {
    try {

      if (CommonUtils.isEmpty(fileName)) {
        fileName = PreferenceManager.getString(Constants.LOG_COLLECTION_LATEST_FILE_NAME);
      }

      if (CommonUtils.isEmpty(fileName)) {
        fileName = Constants.LOG_COLLECTION_FILE_NAME + Constants.UNDERSCORE_CHARACTER + 1;
      }

      File logDir = new File(CommonUtils.getApplication().getExternalCacheDir().getAbsolutePath() +
          File.separator + Constants.LOG_COLLECTION_DIRECTORY);
      logDir.mkdirs();

      File logFile = new File(CommonUtils.getApplication().getExternalCacheDir().getAbsolutePath() +
          File.separator + Constants.LOG_COLLECTION_DIRECTORY + File.separator + fileName);

      return logFile;
    } catch (NullPointerException ex) {
      Logger.e(TAG, "Error creating logger file");
      return null;
    }
  }

  public static File getNextLogFile() {
    int count = getLastKnownFileNumber();
    String newName =
        Constants.LOG_COLLECTION_FILE_NAME + Constants.UNDERSCORE_CHARACTER + (count + 1);
    PreferenceManager.saveString(Constants.LOG_COLLECTION_LATEST_FILE_NAME, newName);
    return getWriteableFile(newName);
  }

  public static int getLastKnownFileNumber() {
    String fileName = PreferenceManager.getString(Constants.LOG_COLLECTION_LATEST_FILE_NAME);
    if (CommonUtils.isEmpty(fileName)) {
      return 0;
    }

    int count =
        Integer.parseInt(fileName.substring(fileName.indexOf(Constants.UNDERSCORE_CHARACTER) + 1));
    return count;
  }
}
