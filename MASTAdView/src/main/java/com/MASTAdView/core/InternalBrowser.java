package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.newshunt.common.helper.common.NhWebViewClient;
import com.newshunt.common.view.customview.NhWebView;

import java.io.IOException;
import java.net.URLDecoder;

public class InternalBrowser extends Dialog {

	int ID_MAIN = 1;
	int ID_WEB = 2;
	int ID_BOTTOM = 3;

	Context _context;

	Button buttonBack;
	Button buttonForward;
	Button buttonRefresh;
	Button buttonStopRefresh;
	Button buttonOpen;

	WebView webView;
	Dialog thisDialog;

	public InternalBrowser(Context context, String url) {
		super(context);

		thisDialog = this;

		this._context = context;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		LinearLayout mailLayout = new LinearLayout(context);
		mailLayout.setId(ID_MAIN);
		mailLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mailLayout.setOrientation(LinearLayout.VERTICAL);
		mailLayout.setBackgroundColor(Color.DKGRAY);
		webView = new NhWebView(context);
		webView.setId(ID_WEB);
		webView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));
		webView.loadUrl(url);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);

		LinearLayout bottomLayout = new LinearLayout(context);
		mailLayout.setOrientation(LinearLayout.VERTICAL);
		bottomLayout.setId(ID_BOTTOM);
		bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0f));
		bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
		bottomLayout.setBackgroundDrawable(GetDrawable(_context,
				"ib_bg_down.png"));

		buttonBack = AddButton(bottomLayout, "ib_arrow_left_regular.png",
				"ib_arrow_left_press.png", "ib_arrow_left_disabled.png");
		buttonForward = AddButton(bottomLayout, "ib_arrow_right_regular.png",
				"ib_arrow_right_press.png", "ib_arrow_right_disabled.png");
		buttonRefresh = AddButton(bottomLayout, "ib_apdate_regular.png",
				"ib_apdate_press.png", null, true);
		buttonOpen = AddButton(bottomLayout, "ib_window_regular.png",
				"ib_window_press.png", null);

		mailLayout.addView(webView);
		mailLayout.addView(bottomLayout);

		setContentView(mailLayout);

		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		getWindow().setBackgroundDrawable(null);

		buttonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.goBack();
				UpdateButtons();
			}
		});

		buttonForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.goForward();
				UpdateButtons();
			}
		});

		buttonRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.reload();
			}
		});

		buttonOpen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(webView.getUrl()));
					_context.startActivity(intent);
				} catch (Exception e) {
				}

				thisDialog.dismiss();
			}
		});

		buttonStopRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.stopLoading();
			}
		});

		webView.setWebViewClient(new NhWebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// view.loadUrl(url);

				try {
					if (url.startsWith(WebView.SCHEME_TEL)) {
						// <!-- tel:9008671876-->
						Intent dialer = Intent.createChooser(new Intent(
								Intent.ACTION_DIAL, Uri.parse(url)),
								"Choose Dialer");
						_context.startActivity(dialer);
					} else if (url.startsWith("callto:")) {
						// <!-- callto:9008671876-->
						String temp = url.substring(url.indexOf(":") + 1);
						temp = "tel:" + temp;
						Intent dialer = Intent.createChooser(new Intent(
								Intent.ACTION_DIAL, Uri.parse(temp)),
								"Choose Dialer");
						_context.startActivity(dialer);
					} else if (url.startsWith("wtai://wp/mc;")) {
						// <!-- wtai://wp/mc;9008671876 -->
						String temp = url.substring(url.indexOf(";") + 1);
						temp = "tel:" + temp;
						Intent dialer = Intent.createChooser(new Intent(
								Intent.ACTION_DIAL, Uri.parse(temp)),
								"Choose Dialer");
						_context.startActivity(dialer);
					} else if (url.startsWith(WebView.SCHEME_MAILTO)) {
						Intent mailer = Intent.createChooser(new Intent(
								Intent.ACTION_SENDTO, Uri.parse(url)),
								"Send Message");
						_context.startActivity(mailer);
					} else if (url.startsWith(WebView.SCHEME_GEO)) {
						Intent geoviewer = Intent.createChooser(new Intent(
								Intent.ACTION_VIEW, Uri.parse(url)),
								"Choose Viewer");
						_context.startActivity(geoviewer);
					} else if (url.startsWith("sms")) {
						// <!-- sms:12345678,+919008671876?body=Hello my friend
						// -->
						String num = url.substring(0, url.indexOf("?"));
						String sms_body = url.substring(url.indexOf("?") + 1);
						sms_body = URLDecoder.decode(sms_body, "UTF-8");
						sms_body = sms_body.replaceFirst("body=", "");
						/*Intent smsIntent = new Intent(Intent.ACTION_VIEW);
						smsIntent.putExtra("address",num);
						smsIntent.setType("vnd.android-dir/mms-sms");
						smsIntent.putExtra("sms_body", sms_body);
						_context.startActivity(smsIntent);*/
						sendUsingDefaultSMS(num, sms_body, _context);
					} else if (url.startsWith("skype")) {
						Intent sky = new Intent("android.intent.action.VIEW");
						String usrName = url.substring(url.indexOf(":") + 1);
						sky.setData(Uri.parse("skype:" + usrName));
						_context.startActivity(sky);
					}// 9) Handle for scheme market
					else if (url.startsWith("market")) {
						openMarket(url);
					} else if (url.startsWith("httpext")) {
						String trimUrl = url.replace("httpext", "http");
						// Intent browser = Intent.createChooser(new Intent(
						// Intent.ACTION_VIEW, Uri.parse(trimUrl)),
						// "Choose Browser");
						// context.startActivity(browser);

						view.loadUrl(trimUrl);

					} else if (url.startsWith("http")
							|| url.startsWith("https")) {
						// <!-- http://play.google.com/store/search?q=newshunt
						// -->
						view.loadUrl(url);
					} else {
						view.loadUrl(url);
					}
				} catch (Exception e) {
					view.loadUrl(url);
					//// e.printStackTrace();
				}

				return true;
			}

			@Override
			public void onPageLoaded(WebView view, String url) {
				buttonRefresh.setVisibility(View.VISIBLE);
				buttonStopRefresh.setVisibility(View.GONE);
				UpdateButtons();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				buttonRefresh.setVisibility(View.GONE);
				buttonStopRefresh.setVisibility(View.VISIBLE);
			}
		});
		UpdateButtons();

	}

	void UpdateButtons() {
		buttonBack.setEnabled(webView.canGoBack());
		buttonForward.setEnabled(webView.canGoForward());
	}

	Button AddButton(LinearLayout bottomLayout, String normal, String pressed,
			String disable) {
		return AddButton(bottomLayout, normal, pressed, disable, false);
	}

	Button AddButton(LinearLayout bottomLayout, String normal, String pressed,
			String disable, boolean isStop) {
		Button button = new Button(_context);
		button.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		button.setBackgroundDrawable(GetSelector(_context, normal, pressed,
				disable));

		LinearLayout ll = new LinearLayout(_context);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));
		ll.setGravity(Gravity.CENTER);
		ll.addView(button);

		if (isStop) {
			buttonStopRefresh = new Button(_context);
			buttonStopRefresh.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			buttonStopRefresh.setBackgroundDrawable(GetSelector(_context,
					"ib_close_regular.png", "ib_close_press.png", null));
			ll.addView(buttonStopRefresh);
			buttonStopRefresh.setVisibility(View.GONE);
		}
		bottomLayout.addView(ll);
		return button;
	}

	public static StateListDrawable GetSelector(Context context, String normal,
			String pressed, String disable) {
		StateListDrawable result = new StateListDrawable();

		result.addState((new int[] { -android.R.attr.state_pressed,
				android.R.attr.state_enabled }), GetDrawable(context, normal));

		if (pressed != null)
			result.addState((new int[] { android.R.attr.state_pressed,
					android.R.attr.state_enabled }),
					GetDrawable(context, pressed));
		if (disable != null)
			result.addState((new int[] { -android.R.attr.state_enabled }),
					GetDrawable(context, disable));
		else
			result.addState((new int[] { -android.R.attr.state_enabled }),
					GetDrawable(context, normal));

		return result;
	}

	public static Drawable GetDrawable(Context context, String fileName) {
		try {
			fileName = "mraid/" + fileName;
			return Drawable.createFromStream(context.getApplicationContext()
					.getAssets().open(fileName), null);
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * private Bitmap getBitmapFromAsset(String strName) throws IOException {
	 * AssetManager assetManager = context.getAssets(); InputStream istr =
	 * assetManager.open(strName); Bitmap bitmap =
	 * BitmapFactory.decodeStream(istr); return bitmap; }
	 * 
	 * Drawable GetImage(String imageName) { try { Bitmap bitmap =
	 * getBitmapFromAsset(imageName); } catch (IOException e) { return null; }
	 * return null; }
	 */

	/**
	 * Function which opens the market link in all available apps that could
	 * 
	 * @param aUrl
	 */
	@SuppressLint("ToastUsedDirectly")
	public void sendUsingDefaultSMS(String phoneNumber,
			String smsBody, Context context) {

		// Log.i("sending sms", "body " + body + " dest" + destination);
		/*ScreenHandler.sendSMS(AppState.currentContext, destination, body,
				true, true);*/
		if(phoneNumber!=null && smsBody!=null) {
			Intent smsIntent = null;
			if(phoneNumber.contains("sms:")) {
				smsIntent = new Intent(Intent.ACTION_SENDTO ,Uri.parse(phoneNumber));
				try{
					smsBody = URLDecoder.decode(smsBody, "UTF-8");
				}catch(Exception e) {
					//// e.printStackTrace();

				}
			}else{
				smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.putExtra("address",phoneNumber);	
				smsIntent.setType("vnd.android-dir/mms-sms");
			}
			
			smsIntent.putExtra("sms_body", smsBody);
			context.startActivity(smsIntent);
		}
		if (context != null) {
			Toast.makeText(context, "Unable to send sms. Please try again later", Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("ToastUsedDirectly")
	public void openMarket(String aUrl) {
		if (null != _context) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(aUrl));

				_context.startActivity(intent);
				// No need to close the app(activity) as user
				// will
				// be able to come back after rating
				// ((Activity) context).finish();

			} catch (android.content.ActivityNotFoundException anfe) {
				Toast.makeText(_context,
						"This feature is not available in your device.",
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(
						_context,
						"Currently we are facing some problem for this feature, please try again later",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
