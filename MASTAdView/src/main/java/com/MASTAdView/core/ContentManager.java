//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.MASTAdLog;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.model.retrofit.UnifiedDns;
import com.newshunt.sdk.network.NetworkExecutorService;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

final public class ContentManager {
  //private static final String INSTALLATION = "INSTALLATION";
  private volatile String autoDetectParameters = "";
  private String userAgent = "";
  private static ContentManager instance;
  private static boolean isSimAvailable;
  final private HashMap<ContentConsumer, ContentParameters> senderParameters =
      new HashMap<ContentConsumer, ContentParameters>();
  //private String id = null;
  //private boolean useSystemDeviceId = false;
  final private Context context;
  final private AdParser parser;
  private static final String LOAD_CONTENT_TAG = "[ContentManager] LoadContent";


  synchronized static public ContentManager getInstance(ContentConsumer consumer) {
    if (instance == null) {
      instance = new ContentManager(consumer);
    }

    return instance;
  }


  // Calers must implement this interface to provide the needed data
  public interface ContentConsumer {
    String getUserAgent();

    Context getContext();

    boolean prefetchImages();

    boolean setResult(AdData ad);
  }


  private ContentManager(ContentConsumer consumer) {
    userAgent = consumer.getUserAgent();
    this.context = consumer.getContext().getApplicationContext();
    runInitDefaultParameters();
    parser = new AdParser(consumer.prefetchImages());
  }


  private void runInitDefaultParameters() {
    Thread thread = new Thread() {
      @Override
      public void run() {
        initDefaultParameters();
      }
    };
    thread.setName("[ContentManager] InitDefaultParameters");
    thread.start();
  }


  public String getAutoDetectParameters() {
    return autoDetectParameters;
  }


  public static boolean isSimAvailable() {
    return isSimAvailable;
  }


  public void startLoadContent(ContentConsumer consumer, String url) {
    if (senderParameters.containsKey(consumer)) {
      stopLoadContent(consumer);
    }

    ContentParameters parameters = new ContentParameters();
    parameters.sender = consumer;
    parameters.url = url;
    //parameters.w = w;
    //parameters.h = h;

    senderParameters.put(consumer, parameters);

    ContentRunnable contentRunnable = new ContentRunnable(parameters);
    NetworkExecutorService networkExecutorService =
        new NetworkExecutorService(Priority.PRIORITY_NORMAL, LOAD_CONTENT_TAG);
    networkExecutorService.submit(contentRunnable);

  }


  public void stopLoadContent(ContentConsumer consumer) {
    if (senderParameters.containsKey(consumer)) {
      senderParameters.get(consumer).sender = null;
      NetworkSDK.cancel(LOAD_CONTENT_TAG);
      senderParameters.remove(consumer);
    }
  }


  final private class ContentParameters {
    public String url;
    public ContentConsumer sender;
  }


  final private class ContentRunnable implements Runnable {
    final ContentParameters parameters;
    boolean isCanceled = false;

    public ContentRunnable(ContentParameters parameters) {
      this.parameters = parameters;
    }

    @Override
    public void run() {
      try {

        // Optionally use built-in ads for testing
        /*
        if (useBuiltinTestAds)
				{
					String adText = null;
					
					int random = (int)(System.currentTimeMillis() % 3); // 0 or 1 or 2
					if (random == 0)
					{
						// Text
						adText = CommonUtils.readRawAsset(context, R.raw.test_text_xml);
					}
					else if (random == 1)
					{
						// Image
						adText = CommonUtils.readRawAsset(context, R.raw.test_image_xml);
					}
					else
					{
						// Richmedia
						adText = CommonUtils.readRawAsset(context, R.raw.test_richmedia_xml);
					}
					
					if (adText != null)
					{
						AdParser parser = new AdParser();
						AdData ad = parser.parse(adText);
						if (parameters.sender != null)
						{
							parameters.sender.setResult(ad);
							return;
						}
					}
				}
				*/

        System.setProperty("http.keepAlive", "false");

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(parameters.url);
        if (CommonUtils.isEmpty(userAgent)) {
          requestBuilder.addHeader("User-Agent", System.getProperty("http.agent"));
        } else {
          requestBuilder.addHeader("User-Agent", userAgent);
        }
        requestBuilder.addHeader("Connection", "close");
        Request request = requestBuilder.build();

        OkHttpClient.Builder builder = NetworkSDK.clientBuilder(Priority.PRIORITY_NORMAL, null);
        builder.connectTimeout(MASTAdConstants.AD_RELOAD_PERIOD, TimeUnit.MILLISECONDS);
        builder.dns(UnifiedDns.INSTANCE);
        builder.readTimeout(MASTAdConstants.DEFAULT_REQUEST_TIMEOUT, TimeUnit.SECONDS);
        Call call = builder.build().newCall(request);
        Response response = call.execute();

        if (response == null || !response.isSuccessful() ||
            response.code() != Constants.HTTP_SUCCESS) {
          if (response != null) {
            response.close();
          }
          setErrorResult("Response code = " + String.valueOf(response.code()));
          stopLoadContent(parameters.sender);
          return;
        }

        InputStream inputStream = response.body().byteStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 1024);
        String responseValue = "";

        if (!isCanceled) {
          responseValue = readInputStream(bufferedInputStream);
        }

        bufferedInputStream.close();
        inputStream.close();
        response.close();

        AdData ad = parser.parseAdData(responseValue);
        if (isCanceled) {
          ad.error = "Canceled";
        } else {
          ad.responseData = responseValue;
        }

        if (parameters.sender != null) {
          parameters.sender.setResult(ad);
        }
      } catch (IOException e) {
        setErrorResult(e.toString() + ": " + e.getMessage());
      }

      stopLoadContent(parameters.sender);
    }


    private void setErrorResult(String message) {
      MASTAdLog logger = new MASTAdLog(null);
      logger.log(MASTAdLog.LOG_LEVEL_ERROR, "ContentManager", message);

      if (parameters.sender != null) {
        AdData error = new AdData();
        error.error = message;
        parameters.sender.setResult(error);
      }
    }


    public void cancel() {
      isCanceled = true;
    }


    private String readInputStream(BufferedInputStream in) throws IOException {
      char[] buffer = new char[1024];
      StringBuilder out = new StringBuilder();
      Reader reader = new InputStreamReader(in, Constants.TEXT_ENCODING_UTF_8);
      for (int n; (n = reader.read(buffer)) != -1; ) {
        if (isCanceled) {
          return "";
        }
        out.append(buffer, 0, n);
      }
      return out.toString();
    }
  }

  private void initDefaultParameters() {
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    isSimAvailable = tm.getSimState() > TelephonyManager.SIM_STATE_ABSENT;

    autoDetectParameters = "";

    if (tm != null) {
      String networkOperator = tm.getNetworkOperator();
      if ((networkOperator != null) && (networkOperator.length() > 3)) {
        String mcc = networkOperator.substring(0, 3);
        String mnc = networkOperator.substring(3);
        autoDetectParameters += "&mcc=" + mcc;
        autoDetectParameters += "&mnc=" + mnc;

      }
      //adserverRequest.setMCC(tm.getNetworkCountryIso());
      //tm.getNetworkOperator()
    }

		/*
		if ((deviceIdMd5 != null) && (deviceIdMd5.length() > 0))
		{
			autoDetectParameters += "&"+MASTAdRequest.parameter_device_id+"=" + deviceIdMd5;
		}
		*/
  }

	/*
	private synchronized String makeDeviceId(Context context) {
		if (id == null) {
			File installation = new File(context.getFilesDir(), INSTALLATION);
			try {
				if (!installation.exists())
					writeInstallationFile(installation);
				id = readInstallationFile(installation);
			} catch (Exception e) {
				id = "1234567890";
			}
		}
		
		return id;
	}

	
	synchronized public boolean getUseSystemDeviceId()
	{
		return useSystemDeviceId;
	}
	
	
	synchronized public void setUseSystemDeviceId(boolean value)
	{
		boolean changed = false;
		if (useSystemDeviceId != value)
		{
			changed = true;
		}
		useSystemDeviceId = value;
		
		if (changed)
		{
			runInitDefaultParameters();
		}
	}
	
	
	synchronized public void setDeviceId(String value)
	{
		boolean changed = false;
		if ((id != null) && (id.compareTo(value) != 0))
		{
			changed = true;
		}
		id = value;
		
		if (changed)
		{
			runInitDefaultParameters();
		}
	}
	
	private static String readInstallationFile(File installationFile) throws IOException {
		RandomAccessFile f = new RandomAccessFile(installationFile, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installationFile) throws IOException {
		FileOutputStream out = new FileOutputStream(installationFile);
		String id = UUID.randomUUID().toString();
		out.write(id.getBytes());
		out.close();
	}
	*/
}
