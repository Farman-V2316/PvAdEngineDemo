//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.MASTAdView.MASTAdDelegate;
import com.MASTAdView.MASTAdLog;
import com.MASTAdView.MASTAdView;
import com.MASTAdView.core.AdViewContainer.CONTENT_PROCESS_STATE;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.NhWebViewClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SetJavaScriptEnabled")
public class AdWebView extends FrameLayout {

	private MASTAdLog adLog = null;
	private AdViewContainer adViewContainer = null;
	private JavascriptInterface javascriptInterface = null;
	private MraidInterface mraidInterface = null;
	private boolean mraidLoaded = false;
	// has mraid library been loaded?
	private final Object mraidLoadSync = new Object();
	private StringBuffer defferedJavascript = null;
	private DisplayMetrics metrics;
	private boolean supportMraid = false;
	// final private boolean launchBrowserOnClicks;
	private AdClickHandler adClickHandler = null;
	private static long viewId = System.currentTimeMillis();
	Context context;
	private Timer mRescueTimer;

	private TimerTask mRescueTimerTask;
	private HTML5WebView html5WebView = null;

	public AdWebView(AdViewContainer parent, MASTAdLog log,
			DisplayMetrics metrics, boolean mraid, boolean handleClicks,
			Context context, boolean isBackgroundTransparent) {

		super(context);
		adViewContainer = parent;
		adLog = log;
		this.metrics = metrics;
		supportMraid = mraid;

		html5WebView = new HTML5WebView(context, parent,
				isBackgroundTransparent);

		defferedJavascript = new StringBuffer();

		if (supportMraid) {
			javascriptInterface = new JavascriptInterface(parent, this);
			mraidInterface = new MraidInterface(parent, this);

		}

		if (handleClicks) {
			adClickHandler = new AdClickHandler(adViewContainer);
		}

		html5WebView.setWebViewClient(new AdWebViewClient(parent.getContext()));
		addView(html5WebView.getLayout());
	}

	@Override
	protected void onLayout(final boolean changed, final int left,
			final int top, final int right, final int bottom) {
		try {
			super.onLayout(changed, left, top, right, bottom);
		} catch (Exception e) {
			// // e.printStackTrace();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean eventhandled = false;
		eventhandled = super.onTouchEvent(event);

		return eventhandled;
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
	}

	synchronized private int getIdForView() {
		viewId += 1;
		return (int) viewId;
	}

	public void setMraidLoaded(boolean value) {
		synchronized (mraidLoadSync) {
			mraidLoaded = value;
			mraidLoadSync.notify();

		}
	}

	public boolean getMraidLoaded() {
		boolean result = false;

		synchronized (mraidLoadSync) {
			result = mraidLoaded;
		}

		return result;
	}

	void checkMraidLoadedAndWait() {
		synchronized (mraidLoadSync) {
			try {

				if (!mraidLoaded) {

					initializeAndStartRescueOperation();
					mraidLoadSync.wait();
				}
			} catch (Exception e) {
			}
		}

	}

	public JavascriptInterface getJavascriptInterface() {
		return javascriptInterface;
	}

	public MraidInterface getMraidInterface() {
		return mraidInterface;
	}

	synchronized public void resetForNewAd() {
		Log.i("RMA", "resetForNewAd");
		html5WebView.stopLoading();
		html5WebView.loadUrl("about:blank");
		defferedJavascript.setLength(0);
		mraidInterface.setState(MraidInterface.STATES.LOADING);
		setMraidLoaded(false);
	}

	public void cancel() {
		if (mRescueTimerTask != null) {
			mRescueTimerTask.cancel();
		}
		if (mRescueTimer != null) {
			mRescueTimer.cancel();
		}
	}

	/**
	 * Inject string into webview for execution as javascript. NOTE: Handle
	 * carefully, this has security implications!
	 *
	 * @param str
	 *            Code string to be run; javascript: prefix will be prepended
	 *            automatically.
	 */
	synchronized public void injectJavaScript(final String str) {
		try {
			if (supportMraid) {
				if (getMraidLoaded()) {
					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "injectJavascript",
							str);
					AndroidUtils.getMainThreadHandler().post(
						new Runnable() {
							@Override
							public void run() {
								html5WebView.loadUrl("javascript:" + str);
							}
						});
				} else {
					// System.out.println("inject javascript (Deferred): " +
					// str);
					defferedJavascript.append(str);
					defferedJavascript.append("\n");
				}
			} else {
				adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "injectJavascript",
						"disabled, skipping");
			}
		} catch (Exception e) {
			adLog.log(MASTAdLog.LOG_LEVEL_DEBUG,
					"injectJavascript - exception", e.getMessage());
		}
	}

	private void initializeExpandProperties() {
		if (supportMraid) {
			List<NameValuePair> list = new ArrayList<NameValuePair>(2);

			// Add width
			String name = MraidInterface
					.get_EXPAND_PROPERTIES_name(MraidInterface.EXPAND_PROPERTIES.WIDTH);
			NameValuePair nvp = new BasicNameValuePair(name, ""
					+ AdSizeUtilities.devicePixelToMraidPoint(
							metrics.widthPixels, getContext()));
			list.add(nvp);

			// Add height
			name = MraidInterface
					.get_EXPAND_PROPERTIES_name(MraidInterface.EXPAND_PROPERTIES.HEIGHT);
			nvp = new BasicNameValuePair(name, ""
					+ AdSizeUtilities.devicePixelToMraidPoint(
							metrics.heightPixels, getContext()));
			list.add(nvp);

			mraidInterface.setExpandProperties(list);
		}
	}

	protected void defaultOnAdClickHandler(String url) {
		if (adClickHandler != null) {
			adClickHandler.openUrlForBrowsing(getContext(), url);
		}
	}

	public int getStatusBarHeight() {
		try {
			Rect rect = new Rect();
			Window window = ((Activity) (adViewContainer.getContext()))
					.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rect);
			int statusBarHeight = rect.top;
			return statusBarHeight;
		} catch (Exception ex) {
			// NA
		}

		return 0;
	}

	final private class AdWebViewClient extends NhWebViewClient {
		private Context context;

		public AdWebViewClient(Context context) {
			super();
			this.context = context;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "OverrideUrlLoading", url);
				MASTAdDelegate delegate = adViewContainer.getAdDelegate();
				if (delegate != null) {
					MASTAdDelegate.AdActivityEventHandler clickHandler = delegate
							.getAdActivityEventHandler();
					// / click event in ad for tracker
					if (clickHandler != null) {
						if (clickHandler.onAdClicked(
								(MASTAdView) adViewContainer, url) == false) {
							// If click() method returns false, continue with
							// default logic
							// tracker ->nitin
							// handling the events like call, mailto etc after
							// tracker report is being send to server from
							// clickHandler.onAdClicked
							defaultOnAdClickHandler(url);
						}
					} else {
						defaultOnAdClickHandler(url);
					}
				} else {
					defaultOnAdClickHandler(url);
				}
			} catch (Exception e) {
				adLog.log(MASTAdLog.LOG_LEVEL_ERROR,
						"shouldOverrideUrlLoading", e.getMessage());
			}

			return true;
		}

		@Override
		public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					pageStartOnThread(view, url, favicon);
				}
			}).start();

			super.onPageStarted(view, url, favicon);
		}

		private void pageStartOnThread(	WebView view, String url, Bitmap favicon){
				if (supportMraid) {
					Log.i("RMA", "Adwebview onPageStarted");
					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted",
							"loading javascript library");

					// loadUrl("javascript:" + mraidScript);

					// Wait for mraid loaded to be true, set by js bridge
					checkMraidLoadedAndWait();

					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "setting device features");
					mraidInterface.setDeviceFeatures();
					if (mraidInterface.getDeviceFeatures().isSupported(MraidInterface.FEATURES.INLINE_VIDEO))
					{
						if (!(context instanceof Activity)) {
							adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted",
									"Video support enabled, but context is not an activity, so cannot adjust web view window properties for hardware acceleration");
						}
					}

					if (defferedJavascript.length() > 0) {
						adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "injecting deferred javascript");

						// Now that mraid script is loaded, send any commands that
						// were saved from earlier
						injectJavaScript(defferedJavascript.toString());
						defferedJavascript.setLength(0);
					}

					// Initialize width/height values for expand properties (starts
					// off with screen size)
					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "initialize expand properties");
					initializeExpandProperties();

					// setScreenSize
					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "set screen size");
					try {
						JSONObject screenSize = new JSONObject();
						screenSize.put(MraidInterface.get_SCREEN_SIZE_name(MraidInterface.SCREEN_SIZE.WIDTH),
								"" + AdSizeUtilities.devicePixelToMraidPoint(metrics.widthPixels, getContext()));
						screenSize.put(MraidInterface.get_SCREEN_SIZE_name(MraidInterface.SCREEN_SIZE.HEIGHT),
								"" + AdSizeUtilities.devicePixelToMraidPoint(metrics.heightPixels, getContext()));
						injectJavaScript("mraid.setScreenSize("+ screenSize.toString() + ");");
					} catch (Exception ex) {
						adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onPageStarted",
								"Error setting screen size information.");
					}

					// setMaxSize
					adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "set max size");
					try {
						JSONObject maxSize = new JSONObject();
						maxSize.put(MraidInterface.get_MAX_SIZE_name(MraidInterface.MAX_SIZE.WIDTH),
								""+ AdSizeUtilities.devicePixelToMraidPoint(metrics.widthPixels, getContext()));
						maxSize.put(MraidInterface.get_MAX_SIZE_name(MraidInterface.MAX_SIZE.HEIGHT),
								""+ AdSizeUtilities.
										devicePixelToMraidPoint(metrics.heightPixels - getStatusBarHeight(),
												getContext()));
						injectJavaScript("mraid.setMaxSize(" + maxSize.toString()+ ");");
					} catch (Exception ex) {
						adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onPageStarted","Error setting max size information.");
						adViewContainer.setContentState(CONTENT_PROCESS_STATE.INVALID);
					}
				}

				adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "onPageStarted", "loading ad url: " + url);
		}

		@Override
		public void onPageLoaded(WebView view, String url) {
			// if(isAutoCollapse) setAdVisibility(View.VISIBLE);
			Log.i("RMA", "Adwebview onPageFinished");

			MASTAdDelegate delegate = adViewContainer.getAdDelegate();
			if (delegate != null) {
				MASTAdDelegate.AdDownloadEventHandler downloadHandler = delegate
						.getAdDownloadHandler();
				if (downloadHandler != null) {
					// downloadHandler.onDownloadEnd((MASTAdView)adViewContainer);
					downloadHandler.onAdViewable((MASTAdView) adViewContainer);
				}
			}

			if (supportMraid) {
				// setDefaultPosition
				try {
					int x = AdSizeUtilities.devicePixelToMraidPoint(
							adViewContainer.getLeft(), context);
					int y = AdSizeUtilities.devicePixelToMraidPoint(
							adViewContainer.getTop(), context);
					int w = AdSizeUtilities.devicePixelToMraidPoint(
							adViewContainer.getWidth(), context);
					int h = AdSizeUtilities.devicePixelToMraidPoint(
							adViewContainer.getHeight(), context);

					JSONObject position = new JSONObject();
					position.put(
							MraidInterface
									.get_DEFAULT_POSITION_name(MraidInterface.DEFAULT_POSITION.X),
							"" + x);
					position.put(
							MraidInterface
									.get_DEFAULT_POSITION_name(MraidInterface.DEFAULT_POSITION.Y),
							"" + y);
					position.put(
							MraidInterface
									.get_DEFAULT_POSITION_name(MraidInterface.DEFAULT_POSITION.WIDTH),
							"" + w);
					position.put(
							MraidInterface
									.get_DEFAULT_POSITION_name(MraidInterface.DEFAULT_POSITION.HEIGHT),
							"" + h);
					injectJavaScript("mraid.setDefaultPosition("
							+ position.toString() + ");");
				} catch (Exception ex) {
					adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onPageFinished",
							"Error setting default position information.");
					adViewContainer
							.setContentState(CONTENT_PROCESS_STATE.INVALID);
				}

				// Tell ad everything is ready, trigger state change from
				// loading to default

				mraidInterface.fireReadyEvent();
				// injectJavaScript("mraid.adLoaded();");
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			adViewContainer.setContentState(CONTENT_PROCESS_STATE.INVALID);
			adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onReceivedError", ""
					+ errorCode + ":" + description);

			MASTAdDelegate delegate = adViewContainer.getAdDelegate();
			if (delegate != null) {
				MASTAdDelegate.AdDownloadEventHandler downloadHandler = delegate
						.getAdDownloadHandler();
				if (downloadHandler != null) {
					downloadHandler.onDownloadError(
							(MASTAdView) adViewContainer, description);
				}
			}
		}
	}

	/**
	 * Function which is meant for rescuing the deadlock in onPageStarted. it
	 * ensures that after certain duration it will remove the lock
	 */
	void initializeAndStartRescueOperation() {
		if (null != mRescueTimerTask) {
			mRescueTimerTask.cancel();
		}
		if (null != mRescueTimer) {
			mRescueTimer.cancel();
		}
		mRescueTimer = null;
		mRescueTimerTask = null;
		mRescueTimer = new Timer();
		mRescueTimerTask = new TimerTask() {

			@Override
			public void run() {
				if (!getMraidLoaded()) {
					setMraidLoaded(true);
				}
			}
		};
		mRescueTimer.schedule(mRescueTimerTask, 2000);

	}

	public void loadDataWithBaseURL(String aBasePath, String dataOut,
			String string, String string2, String object) {
		if (html5WebView != null) {
			html5WebView.loadDataWithBaseURL(aBasePath, dataOut, string, string2, object);
		}
	}

	public void loadUrl(String string) {
		if (html5WebView != null) {
			html5WebView.loadUrl(string);
		}
	}

	public WebSettings getSettings() {
		if (html5WebView != null) {
			return html5WebView.getSettings();
		}
		return null;
	}

	public WebView getHtml5WebView() {

		return html5WebView;
	}
}