package com.newshunt.common.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by santoshkulkarni on 23/04/18.
 */

public class DownloadService extends IntentService {
  public static final int UPDATE_PROGRESS = 8344;
  private static final String LOG_TAG = "DownloadService";
  public static final int BUFFER_SIZE = 1024;
  public DownloadService() {
    super("DownloadService");
  }

  @SuppressLint("ToastUsedDirectly")
  @Override
  protected void onHandleIntent(Intent intent) {
    String urlToDownload = intent.getStringExtra(Constants.DOWNLOAD_URL);
    String fileType = intent.getStringExtra(Constants.FILE_TYPE);
    ResultReceiver receiver = intent.getParcelableExtra(Constants.RECEIVER);
    Bundle resultData = null;
    String filePath = intent.getStringExtra(Constants.FILEPATH);
    boolean downloadSuccessfully;
    try {

      URL url = new URL(urlToDownload);
      URLConnection connection = url.openConnection();

      if (CommonUtils.isEmpty(filePath)) {
        File cacheDir = CommonUtils.getCacheDir(Constants.DH_SHARE_HIDDEN_DIR);
        filePath = cacheDir.getAbsolutePath() + Constants.FORWARD_SLASH + FileUtil
            .getHashCodeBasedFileName(urlToDownload);
      }

      long downloaded = 0;
      File fileToWrite = new File(filePath);
      if (fileToWrite.exists()) {
        downloaded = fileToWrite.length();
      }
      connection.connect();
      // this will be useful so that you can show a typical 0-100% progress bar
      long fileLength = connection.getContentLength();
      if (fileLength > downloaded) {
        Logger.d(LOG_TAG, "Downloading file from internet " + fileLength + ':' + downloaded);
        // download the file
        InputStream input = new BufferedInputStream(connection.getInputStream());

        byte[] data = new byte[BUFFER_SIZE];
        int count;

        long minimumDowloadChunktoUpdate = fileLength / 100;
        int noOfLoops = 1;
        OutputStream output = new FileOutputStream(filePath);
        while ((count = input.read(data)) != -1) {
          downloaded += count;
          // publishing the progress....
          resultData = new Bundle();
          output.write(data, 0, count);

          if (downloaded >= fileLength) {
            // Update : either last chunk or file size
            resultData.putInt(Constants.DOWNLOAD_PERCENT, (int) (downloaded * 100 / fileLength));
            receiver.send(UPDATE_PROGRESS, resultData);

          } else {
            if (downloaded >= (noOfLoops * minimumDowloadChunktoUpdate)) {
              // update
              resultData.putInt(Constants.DOWNLOAD_PERCENT, (int) (downloaded * 100 / fileLength));
              receiver.send(UPDATE_PROGRESS, resultData);
              noOfLoops += 1;
            }
          }
        }
        output.flush();
        output.close();
        input.close();
      } else {
        Logger.d(LOG_TAG, "File download skipping " + fileLength + ':' + downloaded);
        Bundle resultSuccess = new Bundle();
        resultSuccess.putInt(Constants.DOWNLOAD_PERCENT, 100);
        receiver.send(UPDATE_PROGRESS, resultSuccess);
      }

      downloadSuccessfully = true;

    } catch (Exception e) {
      downloadSuccessfully = false;
    }

    if (!downloadSuccessfully) {
      Toast.makeText(this, "Download failed, try again", Toast.LENGTH_SHORT).show();
      if (null == resultData) {
        resultData = new Bundle();
      }
      if (null != receiver) {
        resultData.putBoolean(Constants.DOWNLAOD_FAILED, true);
        resultData.putString(Constants.FILEPATH, filePath);
        receiver.send(UPDATE_PROGRESS, resultData);
      }
      return;
    }

    Bundle resultSuccess = new Bundle();
    resultSuccess.putBoolean(Constants.CAN_SHARE, true);
    resultSuccess.putInt(Constants.DOWNLOAD_PERCENT, 100);
    resultSuccess.putString(Constants.FILEPATH, filePath);
    receiver.send(UPDATE_PROGRESS, resultSuccess);
  }
}
