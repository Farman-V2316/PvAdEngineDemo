/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.client;

import com.newshunt.adengine.model.entity.version.AdPosition;
import com.newshunt.adengine.util.AdsUtil;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.sdk.network.Priority;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Downloads mraid zip file, extracts files from the zip file
 *
 * @author heena.arora
 */
public class DownloadAndUnzipAd {
  private static final String ZIP_FOLDER_NAME = "/.DailyHunt/ZIP/";
  private static final String SPLASH_AD_ZIP_FOLDER_NAME = "/.DailyHunt/SPLASHAD/";
  private static final String EVERGREEN_AD_ZIP_FOLDER_NAME = "/.DailyHunt/EvergreenZip/";

  /**
   * Downloads content and notifies the user on completion.
   *
   * @author heena.arora
   */
  public interface DownloadAndUnzipRequest {

    void notify(String output);
  }

  private static final String LOG_TAG = "DownloadAndUnzipError";
  private String adBasePath;
  private DownloadAndUnzipRequest downloadAndUnzipRequest;

  public DownloadAndUnzipAd(DownloadAndUnzipRequest downloadAndUnzipRequest,
                            AdPosition adPosition) {
    this.downloadAndUnzipRequest = downloadAndUnzipRequest;
    switch (adPosition) {
      case SPLASH:
        adBasePath = AdsUtil.getAdBaseDirectory(SPLASH_AD_ZIP_FOLDER_NAME);
        break;
      case EVERGREEN:
        adBasePath = AdsUtil.getAdBaseDirectory(EVERGREEN_AD_ZIP_FOLDER_NAME);
        break;
      default:
        adBasePath = AdsUtil.getAdBaseDirectory(ZIP_FOLDER_NAME);
    }
  }

  public void run(final String zipFileUrl) {
    try {
      File baseFolder = new File(adBasePath);
      if (!baseFolder.exists()) {
        baseFolder.mkdirs();
      }
      File outputFolder = new File(getLocalFolderForUr(zipFileUrl));
      if (outputFolder.exists()) {
        outputFolder.setLastModified(System.currentTimeMillis());
        downloadAndUnzipRequest.notify(outputFolder.getAbsolutePath());
        return;
      }

      Call call = HttpClientManager.newRequestCall(zipFileUrl, Priority.PRIORITY_NORMAL);
      if (call == null) {
        return;
      }

      call.enqueue(
          new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              try {
                // TODO: (Retrofit update final url)
                File downloadedFile =
                    new File(getLocalFolderForUr(call.request().url().toString()));
                if (downloadedFile.exists()) {
                  downloadedFile.delete();
                }
                downloadAndUnzipRequest.notify(null);
              } catch (Exception ex) {
                Logger.caughtException(ex);
                downloadAndUnzipRequest.notify(null);
              }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
              if (response == null || !response.isSuccessful()) {
                if (response != null) {
                  response.close();
                }
                downloadAndUnzipRequest.notify(null);
                return;
              }
              new Thread(new Runnable() {
                @Override
                public void run() {
                  android.os.Process.setThreadPriority(
                      android.os.Process.THREAD_PRIORITY_BACKGROUND);
                  try {
                    FileUtil.deleteLeastRecentlyAccessed(adBasePath);
                    File outputFolder = new File(getLocalFolderForUr(zipFileUrl));
                    if (!outputFolder.exists()) {
                      outputFolder.mkdir();
                    }
                    String outputPath = outputFolder.getAbsolutePath();
                    saveUnzippedResponse(response.body().byteStream());
                    response.close();
                    downloadAndUnzipRequest.notify(outputPath);
                  } catch (IOException ioException) {
                    Logger.e(LOG_TAG, ioException.toString());
                    downloadAndUnzipRequest.notify(null);
                  } catch (Exception e) {
                    Logger.e(LOG_TAG, e.toString());
                    downloadAndUnzipRequest.notify(null);
                  }
                }
              }).start();
            }
          });
    } catch (Exception e) {
      Logger.caughtException(e);
      downloadAndUnzipRequest.notify(null);
    }
  }

  String getLocalFolderForUr(String zipFileUrl) {
    int start = zipFileUrl.lastIndexOf("/");
    int end = zipFileUrl.indexOf("zip");
    return adBasePath + zipFileUrl.substring(start, end - 1);
  }

  private void saveUnzippedResponse(InputStream inputStream)
      throws IOException {

    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
    ZipEntry entry = null;

    while ((entry = zis.getNextEntry()) != null) {
      if (entry.isDirectory()) {
        File extractedFile = new File(adBasePath + entry.getName());
        if (!extractedFile.exists()) {
          extractedFile.mkdir();
        }
        /**
         * Currently not unzipping the directory structure.
         * All the files will be unzipped in a Directory
         *
         **/
        continue;
      }
      byte[] buffer = new byte[2048];
      String name = entry.getName();

      File extractedFile = new File(adBasePath + name);
      if (!extractedFile.exists()) {
        extractedFile.createNewFile();
      }
      BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(
          extractedFile.getAbsolutePath()));

      int count;
      while ((count = zis.read(buffer)) != -1) {
        outputStream.write(buffer, 0, count);
      }
      closeStreams(outputStream, zis);
    }

    zis.close();
    inputStream.close();
  }

  private void closeStreams(OutputStream outputStream, ZipInputStream zipInputStream)
      throws IOException {
    outputStream.flush();
    outputStream.close();
    zipInputStream.closeEntry();
  }
}
