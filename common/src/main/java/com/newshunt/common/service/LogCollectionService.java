/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.text.TextUtils;

import com.newshunt.common.helper.LogCollectionUtils;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling log collection and upload related tasks.
 *
 * @author karthik.r
 */
public class LogCollectionService extends JobIntentService {

  private static final String TAG = "LogCollectionService";
  private static final int LOG_COLLECTION_JOB_ID = 10001;

  /**
   * Starts this service to collect and upload logs. If the service is already performing a task
   * this action will be queued.
   *
   * @see IntentService
   */
  public static void startLogCollectionService(Context context) {
    enqueueWork(context, LogCollectionService.class, LOG_COLLECTION_JOB_ID,
        new Intent(context, LogCollectionService.class));
  }

  /**
   * {@inheritDoc}
   * Collect logs and upload them if required.
   */
  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    Logger.i(TAG, "LogCollectionService handle Intent");

    File logFile = LogCollectionUtils.getWriteableFile(null);
    if (logFile == null) {
      // Cannot create log file. Terminate log collection process.
      PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_IN_PROGRESS, false);
      PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_UPLOADING_PENDING, false);
      return;
    }

    boolean logCollectionInProgress =
        PreferenceManager.getBoolean(Constants.LOG_COLLECTION_IN_PROGRESS, false);
    if (logCollectionInProgress) {
      // Collect logs only if collection in progress. If upload is pending for some reason, do
      // not include additional logs.
      LogCollectionUtils.collectLogs(logFile);
    }

    long logCollectionEndTime = PreferenceManager.getLong(Constants.LOG_COLLECTION_END_TIME,
        System.currentTimeMillis());

    if (logCollectionEndTime <= System.currentTimeMillis()) {
      // No more logs to be collected. Just upload pending.
      PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_IN_PROGRESS, false);

      boolean logUploadingPending = PreferenceManager.getBoolean(Constants
          .LOG_COLLECTION_UPLOADING_PENDING, false);

      File logFileToUpload = null;

      if (logUploadingPending) {
        int count = LogCollectionUtils.getLastKnownFileNumber();
        logFileToUpload = LogCollectionUtils.getWriteableFile(
            Constants.LOG_COLLECTION_FILE_NAME + Constants.UNDERSCORE_CHARACTER + count);

        while (count > 0 && logFileToUpload.exists()) {

          if (uploadLogs(logFileToUpload)) {
            logFileToUpload.delete(); // Clear log file
            // Upload is completed for a file.
            Logger.i(TAG, "Uploading log file successful: " + logFileToUpload.getName());
          }

          // Prepare next file for upload
          count--;
          logFileToUpload = LogCollectionUtils.getWriteableFile(
              Constants.LOG_COLLECTION_FILE_NAME + Constants.UNDERSCORE_CHARACTER + count);
        }
      }

      if (logFileToUpload == null || !logFileToUpload.exists()) {
        // Reset logger state to original value and not false.
        Logger.setLoggerEnabled(AppConfig.getInstance().isLoggerEnabled());
        Logger.setFileLogger(false, null);
        PreferenceManager.saveBoolean(Constants.LOG_COLLECTION_UPLOADING_PENDING, false);
        PreferenceManager.remove(Constants.LOG_COLLECTION_AUTH_TOKEN);
        AlarmManager am =
            (AlarmManager) CommonUtils.getApplication().getSystemService(Context.ALARM_SERVICE);
        am.cancel(LogCollectionUtils.getLogCollectionOperation()); // Cancel pending alarms
      }
    } else {
      // Upload older files if any.
      int count = LogCollectionUtils.getLastKnownFileNumber();

      // Go only in ascending order. On completion, we will do descending order
      for (int i = 0; i < count; i++) {
        File logFileToUpload = LogCollectionUtils.getWriteableFile(
            Constants.LOG_COLLECTION_FILE_NAME + Constants.UNDERSCORE_CHARACTER + i);

        // Check if the file exist for current log collection
        if (logFileToUpload.exists()) {
          if (uploadLogs(logFileToUpload)) {
            logFileToUpload.delete(); // Clear log file
            // Upload is completed for a file.
            Logger.i(TAG, "Uploading log file successful: " + logFileToUpload.getName());
          } else {
            // If any of the file fails to upload, break the upload cycle and retry next time.
            break;
          }
        }
      }
    }
  }

  /**
   * Upload provided file to BE. Use only Android APIs to make http connection.
   *
   * @param uploadFile File to be uploaded
   * @return true if file upload was successful, false otherwise.
   */
  private boolean uploadLogs(File uploadFile) {
    Logger.i(TAG, "Uploading logs");
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;

    String authToken = PreferenceManager.getString(Constants.LOG_COLLECTION_AUTH_TOKEN,
        Constants.EMPTY_STRING);

    try {
      FileInputStream fileInputStream = new FileInputStream(uploadFile);
      String logUploadUrl = PreferenceManager.getString(Constants.LOG_COLLECTION_UPLOAD_URL_VALUE,
          Constants.EMPTY_STRING);
      if (TextUtils.isEmpty(logUploadUrl)) {
        // No need to retry as upload URL is missing
        return true;
      }

      URL url = new URL(logUploadUrl);

      // Open a HTTP  connection to  the URL. Do not use any library like Retrofit for HTTP
      // connection.
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty(Constants.AUTHORIZATION, authToken);
      DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

      // create a buffer of  maximum size
      bytesAvailable = fileInputStream.available();
      bufferSize = Math.min(bytesAvailable, maxBufferSize);
      buffer = new byte[bufferSize];

      // read file and write it into form...
      bytesRead = fileInputStream.read(buffer, 0, bufferSize);

      while (bytesRead > 0) {
        dos.write(buffer, 0, bufferSize);
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
      }

      int serverResponseCode = conn.getResponseCode();
      conn.getResponseMessage();
      StringBuilder sb = new StringBuilder();
      int data = conn.getInputStream().read();
      while (data != -1) {
        data = conn.getInputStream().read();
        sb.append((char) data);
      }

      fileInputStream.close();
      dos.flush();
      dos.close();
      if (serverResponseCode == Constants.HTTP_SUCCESS) {
        return true;
      }
    } catch (Exception e) {
      Logger.e(TAG, "Exception uploading", e);
    }

    return false;
  }
}
