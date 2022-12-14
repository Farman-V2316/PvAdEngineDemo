//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.MASTAdDelegate;
import com.MASTAdView.MASTAdLog;
import com.MASTAdView.MASTAdView;
import com.newshunt.sdk.network.NetworkExecutorService;
import com.newshunt.sdk.network.Priority;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

// The javascript interface class exposes java methods so that they can be invoked from the javascript layer/ad creative
final public class JavascriptInterface {
	// Name used to attach java methods to javacsript; invocation of a java
	// method from js
	// looks like: AdWebView.method(parameters)
	final private String JAVASCRIPT_METHOD_PREFIX = "AdWebView";

	final private AdViewContainer adView;
	final private AdWebView webView;
	final private Context context;
	final private MASTAdLog adLog;

	private List<NameValuePair> orientationProperties = null;
	final private Object orientationSyncObject = new Object();

	private List<NameValuePair> expandProperties = null;
	final private Object expandSyncObject = new Object();

	private List<NameValuePair> resizeProperties = null;
	final private Object resizeSyncObject = new Object();

	// Construct interface
	public JavascriptInterface(AdViewContainer container, AdWebView webView) {
		adView = container;
		this.webView = webView;
		context = adView.getContext();
		adLog = adView.getLog();

		// Setup javascript -> java interface
		webView.getHtml5WebView().addJavascriptInterface(this, JAVASCRIPT_METHOD_PREFIX);
	}

	//
	// Methods exposed to javascript from this class; these are for use by the
	// javascript code
	// to make calls back to the java app.
	//

	// Log a message
  @android.webkit.JavascriptInterface
	public void log(String message) {
		adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
				"log message=" + message);
		adView.richmediaEvent("log", message);
	}

	// Open (new) URL in full-screen internal (or external, by setting) browser
	// window;
	// the target URL does NOT expect to operate in an MRAID environment.
  @android.webkit.JavascriptInterface
	public void open(String url) {
		adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface", "open");
		// adView.richmediaEvent("open", url);
		adView.richmediaEvent(MASTAdConstants.CUSTOM_METHOD_OPEN, url);

		synchronized (this) {
			if (url != null) {
				// Notify ad view to perform open on UI thread
				Message msg = adView.getHandler().obtainMessage(
						AdMessageHandler.MESSAGE_OPEN);
				Bundle data = new Bundle();
				data.putString(AdMessageHandler.OPEN_URL, url);
				msg.setData(data);
				adView.getHandler().sendMessage(msg);
			}
		}
	}

	// Allow an ad to downgrade its state, and fire a state change event
  @android.webkit.JavascriptInterface
	public void close() {
		adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface", "close");
		adView.richmediaEvent("close", null);

		synchronized (this) {
			adView.getHandler()
					.sendEmptyMessage(AdMessageHandler.MESSAGE_CLOSE);
		}
	}

	// Allow an ad to downgrade its state, and fire a state change event
	@android.webkit.JavascriptInterface
	public void setAdInBackground() {
		adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface", "setAdInBackground");
		adView.richmediaEvent("setAdInBackground", null);

		synchronized (this) {
			adView.getHandler()
					.sendEmptyMessage(AdMessageHandler.MESSAGE_SET_AD_IN_BG);
		}
	}

	// This is a custom Method call registration meant for listening to
	// completetion of ad Load
  @android.webkit.JavascriptInterface
	public void adLoaded() {
		//Log.i("RMA","JAVASRIPTINTEFACE Adloaded adview hascode->"+adView.hashCode());
		adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface", "adLoaded");
		adView.richmediaEvent("adLoaded", null);
		// synchronized(this)
		// {
		// adView.getHandler().sendEmptyMessage(AdMessageHandler.MESSAGE_CLOSE);
		// }
	}

	// Used by javascript code to pass orientation properties into the java app
  @android.webkit.JavascriptInterface
	public void setOrientationProperties(String encodedProperties) {
		adView.richmediaEvent("setOrientationProperties", encodedProperties);

		synchronized (orientationSyncObject) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
					"setOrientationProperties: " + encodedProperties);

			// The encoded property string is made up of key=value fragments
			// joined with an '&',
			// with each key and value portion being uri encoded (on the
			// javascript side.) We
			// can use a standard android library routine to parse this by
			// making it look like
			// a real URI.
			try {
				URI propertiesUri = new URI("http://orientation.properties?"
						+ encodedProperties);
				// List<NameValuePair> properties =
				// URLEncodedUtils.parse(propertiesUri, "UTF-8");
				// expandProperties = createMapFromList(properties);
				orientationProperties = URLEncodedUtils.parse(propertiesUri,
						"UTF-8");

				// Notify ad view to update orientation on UI thread
				Message msg = adView.getHandler().obtainMessage(
						AdMessageHandler.MESSAGE_ORIENTATION_PROPERTIES);
				Bundle data = convertOrientationProperties(orientationProperties);
				msg.setData(data);
				adView.getHandler().sendMessage(msg);
			} catch (Exception ex) {
				adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "JavascriptInterface",
						"Exception setting orientation properties from javascript: "
								+ ex.getMessage() + " using: "
								+ encodedProperties);
			}
		}
	}

	// Used by javascript code to pass expand properties into the java app
  @android.webkit.JavascriptInterface
	public void setExpandProperties(String encodedProperties) {
		adView.richmediaEvent("setExpandProperties", encodedProperties);

		synchronized (expandSyncObject) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
					"setExpandProperties: " + encodedProperties);

			// The encoded property string is made up of key=value fragments
			// joined with an '&',
			// with each key and value portion being uri encoded (on the
			// javascript side.) We
			// can use a standard android library routine to parse this by
			// making it look like
			// a real URI.
			try {
				URI propertiesUri = new URI("http://expand.properties?"
						+ encodedProperties);
				// List<NameValuePair> properties =
				// URLEncodedUtils.parse(propertiesUri, "UTF-8");
				// expandProperties = createMapFromList(properties);
				expandProperties = URLEncodedUtils
						.parse(propertiesUri, "UTF-8");
			} catch (Exception ex) {
				adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "JavascriptInterface",
						"Exception setting expand properties from javascript: "
								+ ex.getMessage() + " using: "
								+ encodedProperties);
			}
		}
	}

	// Full-screen, modal view of ad in-app with support for 1 or 2 part
	// creatives;
	// SDK-enforced tap-to-close area in fixed (top-right) location; relative
	// alignment.
	// Ad state will change to expanded upon success (firing the state change
	// event.)
  @android.webkit.JavascriptInterface
	public void expand(String url) {
		adView.richmediaEvent("expand", url);

		synchronized (expandSyncObject) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
					"expand");

			// Notify ad view to perform resize on UI thread
			Message msg = adView.getHandler().obtainMessage(
					AdMessageHandler.MESSAGE_EXPAND);
			Bundle data = convertExpandDimensions(expandProperties); // pass
																		// expand
																		// properties,
																		// convert
																		// pixel
																		// dimensions
			if (url != null) {
				data.putString(AdMessageHandler.EXPAND_URL, url);
			}
			msg.setData(data);
			adView.getHandler().sendMessage(msg);
		}
	}

	// Used by javascript code to pass resize properties into the java app
  @android.webkit.JavascriptInterface
	public void setResizeProperties(String encodedProperties) {
		
		StringTokenizer tokenizer = new StringTokenizer(
				encodedProperties, "=");
		if (tokenizer.hasMoreElements()) {
			String temp = tokenizer.nextToken();
			 // System.out.println("====");
		}
		if (tokenizer.hasMoreElements()) {
			 String temp2 = tokenizer.nextToken();
			 if(temp2.startsWith("0")){
				 encodedProperties = "width=320&height=224&customClosePosition=top-right&offsetX=0&offsetY=0&allowOffscreen=false"; 
			 }
			 // System.out.println("====");
		}
		if (tokenizer.hasMoreElements()) {
			 String temp3 = tokenizer.nextToken();
			 // System.out.println("====");
			 if(temp3.startsWith("NaN"))
			 encodedProperties = "width=320&height=224&customClosePosition=top-right&offsetX=0&offsetY=0&allowOffscreen=false"; 
		}
		
		

		adView.richmediaEvent("setResizeProperties", encodedProperties);

		synchronized (resizeSyncObject) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
					"setResizeProperties: " + encodedProperties);

			// The encoded property string is made up of key=value fragments
			// joined with an '&',
			// with each key and value portion being uri encoded (on the
			// javascript side.) We
			// can use a standard android library routine to parse this by
			// making it look like
			// a real URI.
			try {
				URI propertiesUri = new URI("http://resize.properties?"
						+ encodedProperties);
				// List<NameValuePair> properties =
				// URLEncodedUtils.parse(propertiesUri, "UTF-8");
				// resizeProperties = createMapFromList(properties);
				resizeProperties = URLEncodedUtils
						.parse(propertiesUri, "UTF-8");
			} catch (Exception ex) {
				adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "JavascriptInterface",
						"Exception setting resize properties from javascript: "
								+ ex.getMessage() + " using: "
								+ encodedProperties);
			}
		}
	}

	// Non-modal dynamic size view for ad content; SDK-enforced tap-to-close
	// area with
	// adjustable position; absolute positioning possible; resize direction
	// selectable.
	// Ad state will change to resized upon success (firing the state change
	// event,
	// as well as the size change event.)
  @android.webkit.JavascriptInterface
	public void resize() {
		adView.richmediaEvent("resize", null);

		synchronized (resizeSyncObject) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
					"resize");

			if ((resizeProperties == null) || (resizeProperties.isEmpty())) {
				// It is an error not not set the resize properties before
				// calling this method
				webView.getMraidInterface().fireErrorEvent(
						"Resize parameters not set", "resize");
				return;
			}

			// Notify ad view to perform resize on UI thread
			Message msg = adView.getHandler().obtainMessage(
					AdMessageHandler.MESSAGE_RESIZE);
			Bundle data = convertResizeDimensionsToPixels(resizeProperties); // pass
																				// resize
																				// properties,
																				// convert
																				// pixel
																				// dimensions
			msg.setData(data);
			adView.getHandler().sendMessage(msg);
		}
	}
  @android.webkit.JavascriptInterface
	public void createCalendarEntry(String encodedProperties) {
		adView.richmediaEvent("createCalendarEntry", encodedProperties);

		synchronized (this) {
			boolean approved = false;
			MASTAdDelegate delegate = adView.getAdDelegate();
			if (delegate != null) {
				MASTAdDelegate.FeatureSupportHandler approvalHandler = delegate
						.getFeatureSupportHandler();
				if (approvalHandler != null) {
					// move this to UI thread???
					approved = approvalHandler.shouldAddCalendarEntry(
							(MASTAdView) adView, encodedProperties);
				}
			}

			if (approved) {
				adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "JavascriptInterface",
						"createCalendarEntry: " + encodedProperties);

				// The encoded property string is made up of key=value fragments
				// joined with an '&',
				// with each key and value portion being uri encoded (on the
				// javascript side.) We
				// can use a standard android library routine to parse this by
				// making it look like
				// a real URI.
				try {
					JSONObject properties = new JSONObject(encodedProperties);
					Bundle dataBundle = calendarParameteresToDataBundle(properties);

					// Notify ad view to perform action on UI thread
					Message msg = adView.getHandler().obtainMessage(
							AdMessageHandler.MESSAGE_CREATE_EVENT);
					msg.setData(dataBundle);
					adView.getHandler().sendMessage(msg);
				} catch (Exception ex) {
					String error = "Exception creating calendar event javascript: "
							+ ex.getMessage() + " using: " + encodedProperties;
					adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "JavascriptInterface",
							error);
					webView.getMraidInterface().fireErrorEvent(error,
							"createCalendarEvent");
				}
			}
		}
	}
  @android.webkit.JavascriptInterface
	public void playVideo(String uri) {
		adView.richmediaEvent("playVideo", uri);

		synchronized (this) {
			// Notify ad view to perform action on UI thread
			Message msg = adView.getHandler().obtainMessage(
					AdMessageHandler.MESSAGE_PLAY_VIDEO);
			Bundle data = new Bundle();
			data.putString(AdMessageHandler.PLAYBACK_URL, uri);
			msg.setData(data);
			adView.getHandler().sendMessage(msg);
		}
	}

	private File makePictureFile(String extension) {
		long now = System.currentTimeMillis();

		File folder;
		folder = HigherApiUtility.getPublicFolderForPictures();

		// Make sure the Pictures directory exists.
		folder.mkdirs();

		File path = new File(folder, "picture-" + now + extension);
		return path;
	}

  @android.webkit.JavascriptInterface
	public boolean storePicture(final String uri) {
		adView.richmediaEvent("storePicture", uri);

		synchronized (this) {
			boolean approved = false;
			MASTAdDelegate.FeatureSupportHandler approvalHandler = adView
					.getAdDelegate().getFeatureSupportHandler();
			if (approvalHandler != null) {
				// XXX move this to UI thread???
				approved = approvalHandler.shouldStorePicture(
						(MASTAdView) adView, uri);
			}

			if (approved) {
				Runnable workerRunnable = new Runnable() {
					public void run() {
						try {

							AdData aAdData = null;
							if (null != adView) {
								aAdData = adView.getLastResponseObject();
							}
							InputStream is = AdData.fetchUrl(uri, aAdData);
							File outputFile = makePictureFile(".jpg"); // XXX
																		// parse
																		// extension
																		// from
																		// URI???
							if (outputFile != null) {
								FileUtils.writeToDisk(is, outputFile);
								is.close();
								showToast(context, "Saved in Gallery");
								// Tell the media scanner about the new file so
								// that it is
								// immediately available to the user.
                HigherApiUtility.ScanFileAfterWrite(
                    context, outputFile, adLog);

                return;
							}
						} catch (Exception ex) {
							adLog.log(
									MASTAdLog.LOG_LEVEL_ERROR,
									"JavascriptInterface storePicture exception",
									ex.getMessage());
							webView.getMraidInterface()
									.fireErrorEvent(
											"Error storing picture: "
													+ ex.getMessage(),
											"storePicture");
						}

						// Should not get here
						webView.getMraidInterface().fireErrorEvent(
								"Storing picture failed for: " + uri,
								"storePicture");
					}

					/**
					 * Show a toast on Success
					 * 
					 * @param context
					 * @param string
					 */
					private void showToast(Context context, String string) {
						try {
							Message msg = adView.getHandler().obtainMessage(
									AdMessageHandler.MESSAGE_SHOW_TOAST);
							Bundle data = new Bundle();
							data.putString(AdMessageHandler.TOAST_TEXT, string);
							msg.setData(data);
							adView.getHandler().sendMessage(msg);
						} catch (Exception e) {
							//// e.printStackTrace();
						}

					}
				};

				NetworkExecutorService networkExecutorService =
						new NetworkExecutorService(Priority.PRIORITY_NORMAL, null);
				networkExecutorService.submit(workerRunnable);
				return true;
			} else {
				adLog.log(MASTAdLog.LOG_LEVEL_DEBUG,
						"JavascriptInterface storePicture", "not allowed");
				webView.getMraidInterface().fireErrorEvent(
						"Storing picture not allowed for: " + uri,
						"storePicture");
			}
		}

		return false;
	}

	//
	// Allow bridge to set mraid loaded flag
	//
  @android.webkit.JavascriptInterface
	public void mraidLoaded() {
		webView.setMraidLoaded(true);
	}

	//
	// Support functions
	//

	private Bundle calendarParameteresToDataBundle(JSONObject properties) {
		Bundle data = new Bundle();

		try {
			if (properties != null) {
				String name;
				String value;

				// description
				name = MraidInterface
						.get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.DESCRIPTION);
				value = properties.getString(name);
				data.putString(name, value);

				// summary
				name = MraidInterface
						.get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.SUMMARY);
				value = properties.getString(name);
				data.putString(name, value);

				// location
				name = MraidInterface
						.get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.LOCATION);
				value = properties.getString(name);
				data.putString(name, value);

				// start
				name = MraidInterface
						.get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.START);
				value = properties.getString(name);
				data.putString(name, value);

				// end
				name = MraidInterface
						.get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.END);
				value = properties.getString(name);
				data.putString(name, value);
			}
		} catch (Exception ex) {
			adLog.log(
					MASTAdLog.LOG_LEVEL_ERROR,
					"JavascriptInterface",
					"Exception processing calendar event properties from javascript: "
							+ ex.getMessage() + " using: "
							+ properties.toString());
		}

		return data;
	}

  @android.webkit.JavascriptInterface
	public static String getListValueByName(List<NameValuePair> list, String name) {
		if ((list != null) && (name != null)) {
			Iterator<NameValuePair> i = list.iterator();
			NameValuePair nvp;
			while (i.hasNext()) {
				nvp = i.next();
				if ((nvp != null) && (nvp.getName() != null)
						&& (nvp.getName().compareTo(name) == 0)) {
					return nvp.getValue();
				}
			}
		}

		return null;
	}

	private Bundle convertExpandDimensions(List<NameValuePair> list) {
		Bundle data = new Bundle();

		if (list != null) {
			Iterator<NameValuePair> i = list.iterator();
			NameValuePair nvp;
			Integer pixels;
			while (i.hasNext()) {
				nvp = i.next();
				if ((nvp != null) && (nvp.getName() != null)) {
					if (nvp.getName()
							.compareTo(
									MraidInterface
											.get_EXPAND_PROPERTIES_name(MraidInterface.EXPAND_PROPERTIES.HEIGHT)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else if (nvp
							.getName()
							.compareTo(
									MraidInterface
											.get_EXPAND_PROPERTIES_name(MraidInterface.EXPAND_PROPERTIES.WIDTH)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else {
						data.putString(nvp.getName(), nvp.getValue());
					}
				}
			}
		}

		return data;
	}
		
	private Bundle convertOrientationProperties(List<NameValuePair> list) {
		Bundle data = new Bundle();

		if (list != null) {
			Iterator<NameValuePair> i = list.iterator();
			NameValuePair nvp;
			while (i.hasNext()) {
				nvp = i.next();
				if ((nvp != null) && (nvp.getName() != null)) {
					data.putString(nvp.getName(), nvp.getValue());
				}
			}
		}

		return data;
	}

	private Bundle convertResizeDimensionsToPixels(List<NameValuePair> list) {
		// System.out.println("Converting resize properites to pixel values");

		Bundle data = new Bundle();

		if (list != null) {
			Iterator<NameValuePair> i = list.iterator();
			NameValuePair nvp;
			Integer pixels;
			while (i.hasNext()) {
				nvp = i.next();
				if ((nvp != null) && (nvp.getName() != null)) {
					if (nvp.getName()
							.compareTo(
									MraidInterface
											.get_RESIZE_PROPERTIES_name(MraidInterface.RESIZE_PROPERTIES.HEIGHT)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else if (nvp
							.getName()
							.compareTo(
									MraidInterface
											.get_RESIZE_PROPERTIES_name(MraidInterface.RESIZE_PROPERTIES.WIDTH)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else if (nvp
							.getName()
							.compareTo(
									MraidInterface
											.get_RESIZE_PROPERTIES_name(MraidInterface.RESIZE_PROPERTIES.OFFSET_X)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else if (nvp
							.getName()
							.compareTo(
									MraidInterface
											.get_RESIZE_PROPERTIES_name(MraidInterface.RESIZE_PROPERTIES.OFFSET_Y)) == 0) {
						int value = 0;
						String nvpValue = nvp.getValue();
						if (nvpValue != null)
							value = Integer.parseInt(nvpValue);

						pixels = AdSizeUtilities.mraidPointToDevicePixel(value,
								context);
						data.putString(nvp.getName(), pixels.toString());
					} else {
						data.putString(nvp.getName(), nvp.getValue());
					}
				}
			}
		}

		return data;
	}
}
