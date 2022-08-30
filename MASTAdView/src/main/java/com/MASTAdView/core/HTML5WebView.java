package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MASTAdView.R;
import com.newshunt.common.helper.common.NhWebViewClient;
import com.newshunt.common.view.customview.NhWebView;

public class HTML5WebView extends NhWebView {

	private MyWebChromeClient mWebChromeClient;
	private View mCustomView;
	private FrameLayout mCustomViewContainer;
	private FrameLayout mContentView;
	private FrameLayout mBrowserFrameLayout;
	private FrameLayout mLayout;
	AdViewContainer parentAdViewContainer;

	static final String LOGTAG = "HTML5WebView";

	private void init(Context context, AdViewContainer parent,
			boolean isBackgroundTransparent) {
		mLayout = new FrameLayout(context);
		parentAdViewContainer = parent;

		mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(context)
				.inflate(R.layout.custom_screen, null);
		mContentView = (FrameLayout) mBrowserFrameLayout
				.findViewById(R.id.main_content);
		mCustomViewContainer = (FrameLayout) mBrowserFrameLayout
				.findViewById(R.id.fullscreen_custom_content);

		mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

		// Configure the webview
		WebSettings s = getSettings();
		s.setBuiltInZoomControls(false);
		s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		s.setUseWideViewPort(true);
		setVerticalScrollBarEnabled(false);
		s.setLoadWithOverviewMode(true);
		// s.setSavePassword(true);
		s.setSaveFormData(true);
		s.setJavaScriptEnabled(true);
		s.setAllowFileAccess(true);
		s.setMediaPlaybackRequiresUserGesture(false);
		s.setPluginState(PluginState.ON);

		s.setSupportMultipleWindows(false);
		mWebChromeClient = new MyWebChromeClient();
		setWebChromeClient(mWebChromeClient);

		setWebViewClient(new NhWebViewClient());

		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		// enable navigator.geolocation
		// s.setGeolocationEnabled(true);
		// s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

		// enable Web Storage: localStorage, sessionStorage
		s.setDomStorageEnabled(true);

		this.setBackgroundColor(Color.TRANSPARENT);
		if (isBackgroundTransparent) {
			mLayout.setBackgroundColor(Color.TRANSPARENT);
			mContentView.setBackgroundColor(Color.TRANSPARENT);
			mBrowserFrameLayout.setBackgroundColor(Color.TRANSPARENT);
			mCustomViewContainer.setBackgroundColor(Color.TRANSPARENT);
			LinearLayout main_content_container = (LinearLayout) mBrowserFrameLayout
					.findViewById(R.id.main_content_container);
			main_content_container.setBackgroundColor(Color.TRANSPARENT);
		}
		mContentView.addView(this);
	}

	/*
	 * public HTML5WebView(Context context,AdViewContainer adViewContainer) {
	 * super(context); adViewContainerMain= adViewContainer; init(context); }
	 */
	public HTML5WebView(Context context, AdViewContainer parentAdViewContainer,
			boolean isBackgroundTransparent) {

		super(context);
		Log.i("RMA", "HTML5Webview");
		init(context, parentAdViewContainer, isBackgroundTransparent);
	}

	public HTML5WebView(Context context, AttributeSet attrs,
			AdViewContainer parentAdViewContainer) {
		super(context, attrs);
		init(context, parentAdViewContainer, false);
	}

	public HTML5WebView(Context context, AttributeSet attrs, int defStyle,
			AdViewContainer parentAdViewContainer) {
		super(context, attrs, defStyle);
		init(context, parentAdViewContainer, false);
	}

	public FrameLayout getLayout() {

		if (this.getParent() != null) {
			ViewGroup v = (ViewGroup) this.getParent();
			v.removeView(this);
		}

		mContentView.addView(this);
		return mLayout;
	}

	public boolean inCustomView() {
		return (mCustomView != null);
	}

	public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((mCustomView == null) && canGoBack()) {
				goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MyWebChromeClient extends WebChromeClient {

		@SuppressLint("NewApi")
		@Override
		public void onShowCustomView(View view,
				WebChromeClient.CustomViewCallback callback) {
			callback.onCustomViewHidden();
			removeAllViews();
			removeAllViewsInLayout();
			super.onShowCustomView(view, callback);

		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			try {
				((Activity) parentAdViewContainer.getActivityContext())
						.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
								newProgress * 100);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		// @SuppressLint("NewApi")
		/*
		 * @Override public void onShowCustomView(View view,
		 * WebChromeClient.CustomViewCallback callback) { // Log.i(LOGTAG,
		 * "here in on ShowCustomView");
		 * HTML5WebView.this.setVisibility(View.GONE);
		 * 
		 * // if a view already exists then immediately terminate the new one if
		 * (mCustomView != null) { callback.onCustomViewHidden(); return; }
		 * 
		 * mCustomViewContainer.addView(view); mCustomView = view;
		 * mCustomViewCallback = callback;
		 * mCustomViewContainer.setVisibility(View.VISIBLE);
		 * 
		 * }
		 * 
		 * 
		 * public void onPageFinished(WebView webview, String url){
		 * super.onPageFinished(webview, url);
		 * setProgressBarIndeterminateVisibility(false); }
		 * 
		 * 
		 * // @SuppressLint("NewApi")
		 * 
		 * @Override public void onHideCustomView() {
		 * System.out.println("customview hideeeeeeeeeeeeeeeeeeeeeeeeeee"); if
		 * (mCustomView == null) return;
		 * 
		 * // Hide the custom view. mCustomView.setVisibility(View.GONE);
		 * 
		 * // Remove the custom view from its container.
		 * mCustomViewContainer.removeView(mCustomView); mCustomView = null;
		 * mCustomViewContainer.setVisibility(View.GONE);
		 * mCustomViewCallback.onCustomViewHidden();
		 * 
		 * HTML5WebView.this.setVisibility(View.VISIBLE);
		 * HTML5WebView.this.goBack(); // Log.i(LOGTAG, "set it to webVew"); }
		 * 
		 * @Override public View getVideoLoadingProgressView() { //
		 * Log.i(LOGTAG, "here in on getVideoLoadingPregressView");
		 * 
		 * if (mVideoProgressView == null) { LayoutInflater inflater =
		 * LayoutInflater .from(parentAdViewContainer.getActivityContext());
		 * mVideoProgressView = inflater.inflate(
		 * R.layout.video_loading_progress, null); } return mVideoProgressView;
		 * }
		 * 
		 * @Override public void onReceivedTitle(WebView view, String title) {
		 * ((Activity) parentAdViewContainer.getActivityContext())
		 * .setTitle(title); }
		 * 
		 * @Override public void onProgressChanged(WebView view, int
		 * newProgress) { ((Activity)
		 * parentAdViewContainer.getActivityContext()).getWindow()
		 * .setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100); }
		 * 
		 * // @SuppressLint("NewApi")
		 * 
		 * @Override public void onGeolocationPermissionsShowPrompt(String
		 * origin, GeolocationPermissions.Callback callback) {
		 * callback.invoke(origin, true, false); }
		 */
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final JsResult result) {

			try {
				if (message
						.contains(MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED)) {
					result.confirm();

				} else {

					// ensure that the current activity context takes the dialog
					// not
					// application dialog
					new AlertDialog.Builder(
							parentAdViewContainer.getActivityContext())
							.setTitle("Alert")
							.setMessage(message)
							.setPositiveButton(android.R.string.ok,
									new AlertDialog.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											result.confirm();
										}
									}).setCancelable(false).create().show();
				}
			} catch (Exception e) {
				// // e.printStackTrace();
			}

			return true;

			// Handle alert message from javascript
			// return super.onJsAlert(view, url, message, result);
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				final JsResult result) {
			try {
				new AlertDialog.Builder(
						parentAdViewContainer.getActivityContext())
						.setTitle("Confirm")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.cancel();
									}
								}).create().show();
			} catch (Exception e) {
				// // e.printStackTrace();
			}
			return true;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, final JsPromptResult result) {
			try {
				final LayoutInflater factory = LayoutInflater
						.from(parentAdViewContainer.getActivityContext());
				final View v = factory.inflate(
						R.layout.javascript_prompt_dialog, null);

				((TextView) v.findViewById(R.id.prompt_message_text))
						.setText(message);
				((EditText) v.findViewById(R.id.prompt_input_field))
						.setText(defaultValue);

				new AlertDialog.Builder(
						parentAdViewContainer.getActivityContext())
						.setTitle("Prompt")
						.setView(v)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										String value = ((EditText) v
												.findViewById(R.id.prompt_input_field))
												.getText().toString();
										result.confirm(value);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										result.cancel();
									}
								})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									public void onCancel(DialogInterface dialog) {
										result.cancel();
									}
								}).show();
			} catch (Exception e) {
				// // e.printStackTrace();
			}

			return true;
		}
	}

	static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
}