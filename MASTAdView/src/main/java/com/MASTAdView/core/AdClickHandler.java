package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.MASTAdLog;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class AdClickHandler implements View.OnClickListener {
	final private AdViewContainer parentContainer;
	final private MASTAdLog adLog;
	final private AdData adData;

	private OpenUrlThread openUrlThread = null;

	public AdClickHandler(AdViewContainer parent) {
		parentContainer = parent;
		adLog = parentContainer.getLog();
		adData = null;
	}

	public AdClickHandler(AdViewContainer parent, AdData data) {
		parentContainer = parent;
		adLog = parentContainer.getLog();
		adData = data;
	}

	public void onClick(View v) {
		if ((adData != null) && (adData.clickUrl != null)) {
			openUrlForBrowsing(parentContainer.getContext(), adData.clickUrl);
		}
	}

	public void openUrlForBrowsing(Context context, String url) {
		if (url == null)
			return;

		Thread.State currentState = Thread.State.NEW;
		if (null != openUrlThread) {
			currentState = openUrlThread.getState();
		}

		if ((openUrlThread == null)
				|| (openUrlThread.getState().equals(Thread.State.TERMINATED))) {
			openUrlThread = new OpenUrlThread(
					parentContainer.getActivityContext(), url);
			openUrlThread.start();
		} else if (openUrlThread.getState().equals(Thread.State.NEW)) {
			openUrlThread.start();
		}
	}

	private class OpenUrlThread extends Thread {
		Context context;
		String url;

		public OpenUrlThread(Context context, String url) {
			this.context = context;
			this.url = url;
		}

		@Override
		public void run() {
			openUrlWorker(context, url);
		}
	}

	private void openUrlWorker(final Context context, final String url) {
		String lastUrl = null;
		String newUrl = url;
		URL connectURL;

		// commenting to avoid making user wait till the redirect happens
		// // Follow redirects to final resource location
		// while (!newUrl.equals(lastUrl)) {
		// lastUrl = newUrl;
		// try {
		// connectURL = new URL(newUrl);
		// HttpURLConnection conn = (HttpURLConnection) connectURL
		// .openConnection();
		// newUrl = conn.getHeaderField("Location");
		// if (newUrl == null) {
		// newUrl = conn.getURL().toString();
		// }
		// } catch (Exception e) {
		// newUrl = lastUrl;
		// }
		// }

		/*
		 * if (newUrl==null) { newUrl = url; }
		 */
		try {
			// 1) Handle for http/https
			Uri uri = Uri.parse(newUrl);
			if (parentContainer.getUseInternalBrowser()
					&& (uri.getScheme().equals("http") || uri.getScheme()
							.equals("https"))) {
				handleInternalWebViewUrl(url, context);
			}

			// 2) Handle for scheme tel
			else if (newUrl.startsWith(WebView.SCHEME_TEL)) {
				// tel:9008671876
				Intent dialer = Intent
						.createChooser(
								new Intent(Intent.ACTION_DIAL, Uri
										.parse(newUrl)), "Choose Dialer");
				context.startActivity(dialer);
			}

			// 3) Handle for scheme CallTo
			else if (newUrl.startsWith("callto:")) {
				// callto:9008671876
				String temp = newUrl.substring(newUrl.indexOf(":") + 1);
				temp = "tel:" + temp;
				Intent dialer = Intent.createChooser(new Intent(
						Intent.ACTION_DIAL, Uri.parse(temp)), "Choose Dialer");
				context.startActivity(dialer);
			}
			// 4) Handle for scheme Call with format wtai
			else if (newUrl.startsWith("wtai://wp/mc;")) {
				// wtai://wp/mc;9008671876
				String temp = newUrl.substring(newUrl.indexOf(";") + 1);
				temp = "tel:" + temp;
				Intent dialer = Intent.createChooser(new Intent(
						Intent.ACTION_DIAL, Uri.parse(temp)), "Choose Dialer");
				context.startActivity(dialer);
			}
			// 5) Handle for scheme mail
			else if (newUrl.startsWith(WebView.SCHEME_MAILTO)) {
				Intent mailer = Intent.createChooser(new Intent(
						Intent.ACTION_SENDTO, Uri.parse(newUrl)),
						"Send Message");
				context.startActivity(mailer);
			}
			// 6) Handle for geo/location
			else if (newUrl.startsWith(WebView.SCHEME_GEO)) {
				Intent geoviewer = Intent
						.createChooser(
								new Intent(Intent.ACTION_VIEW, Uri
										.parse(newUrl)), "Choose Viewer");
				context.startActivity(geoviewer);
			}
			// 7) Handle for sms
			else if (newUrl.startsWith("sms")) {
				// sms:12345678,+919008671876?body=Hello my friend
				String num = newUrl.substring(0, newUrl.indexOf("?"));
				String sms_body = newUrl.substring(newUrl.indexOf("?") + 1);
				sms_body = URLDecoder.decode(sms_body, "UTF-8");
				sms_body = sms_body.replaceFirst("body=", "");
				sendUsingDefaultSMS(num, sms_body, context);
				/*Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.putExtra("address",num);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("sms_body", sms_body);
				context.startActivity(smsIntent);*/
			}
			// 8) Handle for scheme skype
			else if (newUrl.startsWith("skype")) {
				Intent sky = new Intent("android.intent.action.VIEW");
				String usrName = newUrl.substring(newUrl.indexOf(":") + 1);
				sky.setData(Uri.parse("skype:" + usrName));
				context.startActivity(sky);
			}
			// 9) Handle for scheme market
			else if (newUrl.startsWith("market")) {
				if (null != parentContainer) {
					parentContainer.openMarket(newUrl);
				}
			}
			// 10) Handle for scheme extHttp
			else if (newUrl.startsWith("httpext")) {
				String trimUrl = newUrl.replace("httpext", "http");
				handleInternalWebViewUrl(trimUrl, context);

			}
			// for the newshunt command ads
			else if(newUrl.startsWith("nhcommand")){
				int temp1 = newUrl.indexOf(":");
				//convention of url is "nhcommand://openbook:30303"
				String nhCommand = newUrl.substring((temp1 + 3),newUrl.length());
				nhCommand = URLEncoder.encode(nhCommand,"UTF-8");
				if(parentContainer != null && parentContainer.getAdDelegate() != null && parentContainer.getAdDelegate().getRichmediaEventHandler() != null ){
				parentContainer.getAdDelegate().getRichmediaEventHandler().onRichmediaEvent(null,MASTAdConstants.CUSTOM_METHOD_NEWSHUNT_COMMAND_AD, nhCommand);
				}
			}
			// 11) default case give the handle system
			else {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(newUrl));
					context.startActivity(intent);
				} catch (Exception e) {
					adLog.log(MASTAdLog.LOG_LEVEL_ERROR,
							"openUrlInExternalBrowser", "url=" + newUrl
									+ "; error=" + e.getMessage());
				}
			}
		} catch (Exception e) {
			//// e.printStackTrace();
			handleInternalWebViewUrl(url, context);
		}
	}

	/**
	 * Function to give the handle to system
	 * 
	 * @param aUrl
	 *            : url to handle
	 * @param aContext
	 */
	private void handleInternalWebViewUrl(final String aUrl,
			final Context aContext) {
		if (null != parentContainer && null != parentContainer.getHandler()
				&& null != aContext) {
			parentContainer.getHandler().post(new Runnable() {
				@Override
				public void run() {
					try {
						// new
						// activityView(parentContainer.getActivityContext())
						// .showWebViewDialog(aContext, aUrl);

						// new InternalBrowser(aContext, aUrl).show();

						Uri adsUri = Uri.parse(aUrl);
						Intent adsIntent = new Intent(Intent.ACTION_VIEW,
								adsUri);
						aContext.startActivity(adsIntent);
					} catch (Exception e) {
						adLog.log(MASTAdLog.LOG_LEVEL_ERROR,
								"openUrlInInternalBrowser", e.getMessage());
					}
				}
			});
		}
	}

	private class activityView extends View {

		public activityView(Context context) {
			super(context);
		}

		void showWebViewDialog(Context aContext, String url) {
			String extension = MimeTypeMap.getFileExtensionFromUrl(url);
			String mimeType = MimeTypeMap.getSingleton()
					.getMimeTypeFromExtension(extension);
			if (!handleMimeType(url, mimeType, aContext)) {
				new InternalBrowser(aContext, url).show();
			}
		}

		/**
		 * Function which verifies if we there is a videoContent and handle
		 * through any applications which supoorts the same
		 * 
		 * @param u
		 * @param mimeType
		 * @param aContext
		 * @return
		 */
		private boolean handleMimeType(String u, String mimeType,
				Context aContext) {

			boolean returnValue = false;
			try {
				String url = u;
				if (mimeType != null && mimeType.startsWith("video/")) {
					Intent videoIntent = new Intent(Intent.ACTION_VIEW);
					videoIntent.setDataAndType(Uri.parse(url), mimeType);
					aContext.startActivity(videoIntent);
					returnValue = true;
				}
			} catch (Exception e) {
				//// e.printStackTrace();
			}
			return returnValue;
		}
	}

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

}
