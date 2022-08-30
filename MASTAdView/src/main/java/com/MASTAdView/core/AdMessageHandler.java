//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

final public class AdMessageHandler extends Handler {
	// Messages sent to and processed by handler
	public static final int MESSAGE_RESIZE = 1000;
	public static final int MESSAGE_CLOSE = 1001;
	public static final int MESSAGE_HIDE = 1002;
	// public static final int MESSAGE_SHOW = 1003;
	public static final int MESSAGE_EXPAND = 1004;
	public static final int MESSAGE_ANIMATE = 1005;
	public static final int MESSAGE_OPEN = 1006;
	public static final int MESSAGE_PLAY_VIDEO = 1007;
	public static final int MESSAGE_CREATE_EVENT = 1008;
	public static final int MESSAGE_RAISE_ERROR = 1009;
	public static final int MESSAGE_ORIENTATION_PROPERTIES = 1010;
	public static final int MESSAGE_SHOW_TOAST = 1011;
	public static final int MESSAGE_SET_AD_IN_BG = 1012;
	// public static final int MESSAGE_PLAY_AUDIO = xxxx;

	// Keys for information passed around in data object
	public static final String ERROR_MESSAGE = "error.Message";
	public static final String ERROR_ACTION = "error.Action";
	public static final String RESIZE_HEIGHT = "resize.Height";
	public static final String RESIZE_WIDTH = "resize.Width";
	public static final String EXPAND_URL = "expand.Url";
	public static final String OPEN_URL = "open.Url";
	public static final String PLAYBACK_URL = "playback.Url";
	public static final String TOAST_TEXT = "toastText";

	final private AdViewContainer adView;
	private MraidInterface mraidInterface = null;

	public AdMessageHandler(AdViewContainer parent) {
		super();
		adView = parent;
	}

	// Handle messages asking functions to be performed on the UI thread;
	// primarily used by JavaScript interface
	// so that operations such as open/expand/resize can be performed on the UI
	// thread. Can also be used by other
	// background threads to run code on UI thread when needed.
	@SuppressLint("ToastUsedDirectly")
	@Override
	synchronized public void handleMessage(Message msg) {
		try {
			String error = null;

			Bundle data = msg.getData();

			if (mraidInterface == null) {
				if (adView.getAdWebView() == null) {
					return;
				}
				mraidInterface = adView.getAdWebView().getMraidInterface();
			}

			switch (msg.what) {
			case MESSAGE_RESIZE: {
				if (adView.isContainerReadyForAction(msg.what)) {
					error = adView.resize(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {

					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_RESIZE);
				}
				break;
			}
			case MESSAGE_CLOSE: {
				if (adView.isContainerReadyForAction(msg.what)) {
					try {
						error = adView.close(data, false);
					} catch (Exception e) {
						// TODO: handle exception
					}
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_CLOSE);
				}
				break;
			}
			case MESSAGE_SET_AD_IN_BG:{
				if (adView.isContainerReadyForAction(msg.what)) {
					try {
						 adView.setAdInBackground();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			case MESSAGE_HIDE: {
				if (adView.isContainerReadyForAction(msg.what)) {
					error = adView.hide(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_HIDE);
				}
				break;
			}
			/*
			 * case MESSAGE_SHOW: { error = adView.show(data); if (error !=
			 * null) { mraidInterface.fireErrorEvent(error, "show"); } break; }
			 */
			case MESSAGE_EXPAND: {
				if (adView.isContainerReadyForAction(msg.what)) {

					error = adView.expand(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_EXPAND);
				}
				break;
			}
			case MESSAGE_OPEN: {
				if (adView.isContainerReadyForAction(msg.what)) {

				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				error = adView.open(data);
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_OPEN);
				}
				break;
			}
			case MESSAGE_PLAY_VIDEO: {
				if (adView.isContainerReadyForAction(msg.what)) {

					error = adView.playVideo(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_PLAYVIDEO);
				}
				break;
			}
			case MESSAGE_CREATE_EVENT: {
				if (adView.isContainerReadyForAction(msg.what)) {
					error = adView.createCalendarEvent(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface.fireErrorEvent(error,
							MraidInterface.MRAID_ERROR_ACTION_CREATE_EVENT);
				}
				break;
			}
			case MESSAGE_RAISE_ERROR: {
				if (adView.isContainerReadyForAction(msg.what)) {
					String errorMessage = data.getString(ERROR_MESSAGE);
					String action = data.getString(ERROR_ACTION);
					mraidInterface.fireErrorEvent(errorMessage, action);
				}
				break;
			}
			case MESSAGE_ORIENTATION_PROPERTIES: {
				if (adView.isContainerReadyForAction(msg.what)) {
					error = adView.updateOrientationProperties(data);
				} else {
					error = MraidInterface.MRAID_ERROR_NO_ACTION_HANDLED;
				}
				if (error != null) {
					mraidInterface
							.fireErrorEvent(
									error,
									MraidInterface.MRAID_ERROR_ACTION_SET_ORIENTATION_PROPERTIES);
				}
				break;
			}
			case MESSAGE_SHOW_TOAST: {
				try {
					Toast.makeText(adView.getContext().getApplicationContext(),
							"Saved in Gallery", Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					// // e.printStackTrace();
				}
				break;
			}
			}
		} catch (Exception e) {
			// TODO: handle exception
			//e.printStackTrace();
		}
		super.handleMessage(msg);
	}

}
