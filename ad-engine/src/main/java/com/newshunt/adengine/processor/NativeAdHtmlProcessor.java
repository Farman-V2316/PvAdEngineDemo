/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.processor;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.core.AdData;
import com.newshunt.adengine.client.DownloadAndUnzipAd;
import com.newshunt.adengine.model.AdReadyHandler;
import com.newshunt.adengine.model.entity.BaseAdEntity;
import com.newshunt.adengine.model.entity.NativeAdHtml;
import com.newshunt.adengine.model.entity.version.AdRequest;
import com.newshunt.adengine.util.AdLogger;
import com.newshunt.common.helper.common.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Processes {@link NativeAdHtml}.
 * Involves downloading and unzipping content from server.
 *
 * @author shreyasd.desai
 */
public class NativeAdHtmlProcessor implements
    DownloadAndUnzipAd.DownloadAndUnzipRequest, BaseAdProcessor {
  private static final String LOG_TAG = "NativeAdHtmlParser";

  private AdReadyHandler adReadyHandler;
  private NativeAdHtml nativeAdHtml;
  private NativeAdHtml.CoolAd coolAd;
  private boolean coolAdTagAlreadyProcessed;

  NativeAdHtmlProcessor(AdReadyHandler adReadyHandler,
                        BaseAdEntity baseAdEntity) {
    this(adReadyHandler, baseAdEntity, false);
  }

  NativeAdHtmlProcessor(AdReadyHandler adReadyHandler,
                        BaseAdEntity baseAdEntity, boolean coolAdTagAlreadyProcessed) {
    this.adReadyHandler = adReadyHandler;
    this.nativeAdHtml = (NativeAdHtml) baseAdEntity;
    this.coolAd = nativeAdHtml.getCoolAd();
    this.coolAdTagAlreadyProcessed = coolAdTagAlreadyProcessed;
  }

  @Override
  public void processAdContent(AdRequest adRequest) {
    if (nativeAdHtml.getCoolAd() != null && !nativeAdHtml.getCoolAd().getZipped()) {
      AdLogger.d(LOG_TAG, "Sending unzipped ad " + nativeAdHtml.getType());
      processMASTAdView();
    } else if (nativeAdHtml.getCoolAd() != null &&
        nativeAdHtml.getCoolAd().getContent().getData() != null) {
      downloadAndUnzipAd(nativeAdHtml.getCoolAd());
    } else {
      AdLogger.d(LOG_TAG, "Html ad received has no zipped content but flag is true");
      adReadyHandler.onReady(null);
    }
  }

  private void processMASTAdView() {
    final AdData offlineAdData = new AdData();
    offlineAdData.adType = MASTAdConstants.AD_TYPE_RICHMEDIA;
    offlineAdData.mMetaData = nativeAdHtml.getCoolAd().getMeta();
    offlineAdData.useDHFont = nativeAdHtml.getUseDhFont();
    offlineAdData.mBasePath = nativeAdHtml.getContentBaseUrl();

    if (!nativeAdHtml.getCoolAd().getZipped()) {
      offlineAdData.richContent = nativeAdHtml.getCoolAd().getContent().getData();
    } else if (nativeAdHtml.getCoolAd().getContent().getUnzippedPath() != null) {
      offlineAdData.richContent = nativeAdHtml.getCoolAd().getRichContent();
      offlineAdData.mBasePath = "file://" + nativeAdHtml.getCoolAd().getContent()
          .getMainFile();

    }
    nativeAdHtml.setVideoAd(nativeAdHtml.getCoolAd().isVideoAd());
    nativeAdHtml.setMastAdViewData(offlineAdData);
    adReadyHandler.onReady(nativeAdHtml);
  }

  public void downloadAndUnzipAd(NativeAdHtml.CoolAd coolAd) {
    DownloadAndUnzipAd downloadAndUnzipAd =
        new DownloadAndUnzipAd(this, nativeAdHtml.getAdPosition());
    AdLogger.d(LOG_TAG, "Download zip  ad from url = " +
        coolAd.getContent().getData());
    downloadAndUnzipAd.run(coolAd.getContent().getData());
  }


  @Override
  public void notify(String outputFilePath) {
    if (coolAd.getContent() == null || coolAd.getContent().getMainFile() == null) {
      AdLogger.d(LOG_TAG, "Zip download failed");
      AdLogger.w(LOG_TAG, "Sending null to ad ready handler for zipped ad");
      adReadyHandler.onReady(null);
      return;
    }

    if (outputFilePath == null) {
      coolAd.getContent().setMainFile(null);
      AdLogger.w(LOG_TAG, "Sending null to ad ready handler for zipped ad");
      adReadyHandler.onReady(null);
      return;
    }

    //cool ad tag is already processed if this process request is coming for splash ad which was
    // already processed and saved.
    if (!coolAdTagAlreadyProcessed) {
      AdLogger.v(LOG_TAG, "cool ad not already processed");
      coolAd.getContent().setUnzippedPath(outputFilePath);
      coolAd.getContent()
          .setMainFile(outputFilePath + "/" + coolAd.getContent().getMainFile());
      coolAd.setRichContent(getRichAdContent(coolAd.getContent().getMainFile()));

      if (coolAd.getZipsubcontent() != null) {
        createSubContent(coolAd.getZipsubcontent(), outputFilePath);
      }
    }

    AdLogger.d(LOG_TAG, "Sending zipped ad with main file path = " +
        coolAd.getContent().getMainFile());
    processMASTAdView();
  }


  public void createSubContent(NativeAdHtml.ZipSubContentTag zipSubContentTag, String path) {
    //todo mukesh need to check with sub content if value is getting obfuscated in release
    if (zipSubContentTag.getData() == null) {
      AdLogger.d(LOG_TAG, "createSubContent data is null");
      return;
    }
    AdLogger.d(LOG_TAG, "createSubContent data" + zipSubContentTag.getData());
    File file = new File(path + "/" + zipSubContentTag.getName());
    OutputStream outputStream = null;
    try {
      if (!file.exists()) {
        file.createNewFile();
      }
      outputStream = new FileOutputStream(file);
      outputStream.write(java.net.URLDecoder.decode(zipSubContentTag.getData(), "UTF-8")
          .getBytes());
      outputStream.flush();
    } catch (IOException e) {
      Logger.e(LOG_TAG, e.toString());
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          Logger.e(LOG_TAG, e.toString());
        }
      }
    }
  }

  public String getRichAdContent(String mainFilePath) {
    String content = "";
    InputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(
          mainFilePath));
      byte[] buffer = new byte[inputStream.available()];
      inputStream.read(buffer);
      inputStream.close();

      content = new String(buffer);
    } catch (IOException e) {
      Logger.d(LOG_TAG, e.toString());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          Logger.d(LOG_TAG, e.toString());
        }
      }
    }
    return content;
  }

}
