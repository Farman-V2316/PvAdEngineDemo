/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.appindexing;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

import java.net.URISyntaxException;

import static com.newshunt.dhutil.helper.common.DailyhuntConstants.URL_HTTPS_FORMAT;
import static com.newshunt.dhutil.helper.common.DailyhuntConstants.URL_HTTP_FORMAT;

/**
 * @author anshul.jain on 5/25/2016.
 */
public class AppIndexingHelper {

  private static final String APP_INDEX_URI_SCHEME = "android-app://" + CommonUtils.getApplication()
      .getPackageName() + "/";
  private static final String APP_INDEX_HTTP_FORMAT = "http/";
  private static final String APP_INDEX_HTTPS_FORMAT = "https/";
  private static GoogleApiClient client;

  /**
   * Method to get the Uri from the given url
   *
   * @param url : The given url which is of scheme http:// or https://
   * @Return Uri : Return uri of the format android-app://<package-name>/<scheme>/host_url
   */
  public static Uri getUri(String url) throws Exception {
    url = appendAppIndexingQueryParam(url);
    if(CommonUtils.isEmpty(url)){
      return null;
    }
    if (url.contains(URL_HTTPS_FORMAT)) {
      url = url.replace(URL_HTTPS_FORMAT, APP_INDEX_HTTPS_FORMAT);
    } else if (url.contains(URL_HTTP_FORMAT)) {
      url = url.replace(URL_HTTP_FORMAT, APP_INDEX_HTTP_FORMAT);
    }
    url = APP_INDEX_URI_SCHEME + url;
    return Uri.parse(url);
  }

  /**
   * Connect to Google API client
   */
  public static void connectToGoogleClient() {
    Context context = CommonUtils.getApplication();
    client = new GoogleApiClient.Builder(context).addApi(AppIndex.API).build();
    try {
      client.connect();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }


  /**
   * Disconnect the Google api client
   */
  public static void disconnetGoogleClient() {
    if (client != null) {
      client.disconnect();
    }
  }

  /**
   * Appending quary parameter for App indexing referrer
   *
   * @param uri - app indexing url
   * @return app indexing url with fromAppIndexing referrer parameter
   * @throws URISyntaxException
   */
  private static String appendAppIndexingQueryParam(String uri) throws URISyntaxException {
    if(CommonUtils.isEmpty(uri)){
      return null;
    }
    Uri.Builder builtUri = Uri.parse(uri)
        .buildUpon().appendQueryParameter(Constants.APP_INDEXING_REFERRER, "true");
    return builtUri.toString();
  }

  /*
  Method to get the action Object for app Indexing.
   */
  private static Action getAction(String appIndexingTitle, Uri
      appIndexingUri) throws Exception {

    Thing object = new Thing.Builder()
        .setName(appIndexingTitle)
        .setUrl(appIndexingUri)
        .build();
    return new Action.Builder(Action.TYPE_VIEW)
        .setObject(object)
        .setActionStatus(Action.STATUS_TYPE_COMPLETED)
        .build();
  }

  /**
   * Method to start indexing of News Article/Book detail
   * @param appIndexingTitle : The text which appears in google autocomplete search results.
   * @param appIndexingUri   : The uri related to the news article/book detail
   * @param APPINDEX_TAG     : Tag used for logging purpose.
   */
  public static void startAppIndexing(String appIndexingTitle, Uri
      appIndexingUri, final String APPINDEX_TAG) throws Exception {

    if (client == null) {
      Logger.e(APPINDEX_TAG, "Client is null");
      return;
    }

    if(appIndexingUri == null){
      Logger.e(APPINDEX_TAG, "App Indexing Uri is null");
      return;
    }
    final String logDisplayStr = "for the following title : " + appIndexingTitle +
        " and appIndexingUri: " + appIndexingUri;

    PendingResult<Status> status = AppIndex.AppIndexApi.start(client, getAction(appIndexingTitle,
        appIndexingUri));

    status.setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        if (null == status) {
          return;
        }
        if (status.isSuccess()) {
          Logger.d(APPINDEX_TAG, "Success : " + logDisplayStr);
        } else if (status.isInterrupted()) {
          Logger.d(APPINDEX_TAG, "Interruppted : " + logDisplayStr);
        } else {
          Logger.d(APPINDEX_TAG, "Cancelled : " + logDisplayStr);
        }
      }
    });
  }


  /**
   * Generate the App indexing title in Specific format
   * <Title> : <AppIndexing Description> "/n/n" <name in english>
   *
   * @param name                   name of the topic
   * @param tabName                name of the tab
   * @param appIndexingDescription app indexing description
   * @param nameInEnglish          name in english format
   * @return required format of the title
   */
  public static String generateAppIndexingTitle(String name, String tabName,
                                                String appIndexingDescription,
                                                String nameInEnglish) {
    StringBuilder appIndexingTitle = new StringBuilder();

    if (!CommonUtils.isEmpty(tabName)) {
      appIndexingTitle.append(tabName).append(" : ");
    } else if (!CommonUtils.isEmpty(name)) {
      appIndexingTitle.append(name).append(" : ");
    }

    if (!CommonUtils.isEmpty(appIndexingDescription)) {
      appIndexingTitle.append(appIndexingDescription);
    }

    if (!CommonUtils.isEmpty(nameInEnglish)) {
      appIndexingTitle.append(Constants.NEW_LINE).append(Constants.NEW_LINE).append(nameInEnglish);
    }

    return appIndexingTitle.toString();
  }


  /**
   * Method to stop indexing of News Article/Book detail
   * @param appIndexingTitle : The text which appears in google autocomplete search results.
   * @param appIndexingUri   : The uri related to the news article/book detail
   * @param APPINDEX_TAG     : Tag used for logging purpose.
   */
  public static void endAppIndexing(String appIndexingTitle,
                                    Uri appIndexingUri, final String APPINDEX_TAG)
      throws Exception {

    if (client == null) {
      Logger.e(APPINDEX_TAG, "Client is null");
      return;
    }

    if(appIndexingUri == null){
      Logger.e(APPINDEX_TAG, "App Indexing Uri is null");
      return;
    }

    final String logDisplayStr =
        " for the following title : " + appIndexingTitle + " and appIndexingUri: " +
            appIndexingUri;

    PendingResult<Status> result = AppIndex.AppIndexApi.end(client, getAction(appIndexingTitle,
        appIndexingUri));

    result.setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        if (null == status) {
          return;
        }
        if (status.isSuccess()) {
          Logger.d(APPINDEX_TAG, "Success : " + logDisplayStr);
        } else if (status.isInterrupted()) {
          Logger.d(APPINDEX_TAG, "Interruppted : " + logDisplayStr);
        } else {
          Logger.d(APPINDEX_TAG, "Cancelled : " + logDisplayStr);
        }
      }
    });

    disconnetGoogleClient();
  }

  public static boolean isAppIndexingEnabled() {
    return (AppConfig.getInstance().isAppIndexingEnabled()) && !AppConfig.getInstance()
        .isVariantBuild();
  }

}
