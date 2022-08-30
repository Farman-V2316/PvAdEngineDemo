package com.MASTAdView.core;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

import com.MASTAdView.MASTAdConstants;

public class AdFullScreenDialog extends Dialog {

	AdViewContainer adViewContainer = null;
	
	public AdFullScreenDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener , AdViewContainer adViewContainer) {
		super(context, cancelable, cancelListener);
		this.adViewContainer = adViewContainer;
		// TODO Auto-generated constructor stub
	}
	public AdFullScreenDialog(Context context, int theme, AdViewContainer adViewContainer) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.adViewContainer = adViewContainer;
	}
	public AdFullScreenDialog(Context context , AdViewContainer adViewContainer) {
		super(context);
		this.adViewContainer = adViewContainer;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			adViewContainer.richmediaEvent(MASTAdConstants.CUSTOM_METHOD_DIALOG_DISMISS, null);
		}
		return super.onKeyDown(keyCode, event);
	}
}
