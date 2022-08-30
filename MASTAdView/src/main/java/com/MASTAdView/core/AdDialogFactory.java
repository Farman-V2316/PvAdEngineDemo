//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.R;

final public class AdDialogFactory {
	final private Context context;
	private Dialog dialog;
	final private Handler handler;
	final AdViewContainer adViewContainer;
	private Button closeButton;

	public AdDialogFactory(Context context, AdViewContainer topContainer) {
		this.context = context;
		adViewContainer = topContainer;

		handler = new Handler();
	}

	protected ViewGroup.LayoutParams createContainerLayoutParameters(
			final DialogOptions options) {
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
	}

	protected ViewGroup.LayoutParams createAdLayoutParameters(
			final DialogOptions options) {
		if ((options != null) && (options.width != null)
				&& (options.height != null)) {
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			return layoutParams;
		} else {

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			return layoutParams;
		}
	}

	protected RelativeLayout.LayoutParams createCloseLayoutParameters(
			final DialogOptions options) {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
				RelativeLayout.TRUE);
		return layoutParams;
	}

	@SuppressLint("ResourceType")
	public Dialog createDialog(final View ad, final DialogOptions options) {

		if ((options != null) && (options.hideTitlebar != null)
				&& (options.hideTitlebar)) {
			dialog = new AdFullScreenDialog(context,
					android.R.style.Theme_NoTitleBar_Fullscreen,
					adViewContainer);
		} else {

			dialog = new AdFullScreenDialog(context,
					android.R.style.Theme_NoTitleBar, adViewContainer);

		}

		if (null != adViewContainer
				&& null != adViewContainer.getLastResponseObject()
				&& adViewContainer.getLastResponseObject().mIsTransparentBgForResizedRichAd) {

			dialog.getWindow().setBackgroundDrawable(
					new ColorDrawable(android.graphics.Color.TRANSPARENT));

		} else {

			dialog.getWindow().setBackgroundDrawable(
					new ColorDrawable(Color.WHITE));
		}
		// remove ad view from any other containers (if needed)
		if (ad.getParent() != null) {
			((ViewGroup) ad.getParent()).removeView(ad);
		}

		// Create container to hold ad and close button
		LinearLayout maincontainer = new LinearLayout(context);
		LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		maincontainer.setOrientation(LinearLayout.VERTICAL);
		maincontainer.setLayoutParams(mainParams);

		if ((options != null) && (options.hideTitlebar != null)
				&& (!options.hideTitlebar)) {
			RelativeLayout headercontainer = new RelativeLayout(context);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			ImageView ivOne = new ImageView(context);
			ivOne.setId(1);
			ivOne.setPadding(5, 8, 5, 8);
			ivOne.setImageResource(R.drawable.news_back_btn_select);
			ivOne.setBackgroundColor(Color.WHITE);

			ivOne.setOnClickListener(createCloseClickListener(ad, options));
			headercontainer.addView(ivOne);

			TextView adText = new TextView(context);
			params.addRule(RelativeLayout.RIGHT_OF, ivOne.getId());
			adText.setBackgroundColor(Color.WHITE);
			String titleBarText = "";
			if (titleBarText.equalsIgnoreCase("")) {
				adText.setText("Advertisement");
			} else {
				// server set data
				adText.setText(titleBarText);
			}
			adText.setTextColor(Color.BLACK);
			adText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			adText.setPadding(90, 8, 0, 8);
			headercontainer.setBackgroundColor(Color.WHITE);
			headercontainer.addView(adText, params);

			maincontainer.addView(headercontainer);

		}

		RelativeLayout adContainer = new RelativeLayout(context);
		adContainer.setLayoutParams(createContainerLayoutParameters(options));

		// Set background color (if any)
		if ((options != null) && (options.backgroundColor != null)) {
			adContainer.setBackgroundColor(options.backgroundColor);
		}

		// Put ad in new container, and use all available space
		ad.setLayoutParams(createAdLayoutParameters(options));

		// adding data to ad container i.e webview of htm5
		adContainer.addView(ad);

		if ((options != null) && (options.noClose != null)
				&& (options.noClose == true)) {
			// skip close setup (for MRAID open method)
			closeButton = null;
		} else {
			if ((options != null) && (options.customClose != null)
					&& (options.customClose)) {
				closeButton = new Button(context);
				closeButton.setVisibility(View.GONE);
				closeButton.setText("");
				closeButton.setBackgroundColor(Color.TRANSPARENT);

				// Mraid spec requires min. 50 pixel height and width for close
				// area
				closeButton.setMinHeight(50);
				closeButton.setMinWidth(50);

				closeButton.setOnClickListener(createCloseClickListener(ad,
						options));
			} else if (adViewContainer.getCustomCloseButton() != null) {
				closeButton = adViewContainer.getCustomCloseButton();
				closeButton.setVisibility(View.GONE);
				if (closeButton.getParent() != null) {
					((ViewGroup) closeButton.getParent())
							.removeView(closeButton);
				}

				closeButton.setOnClickListener(createCloseClickListener(ad,
						options));
			} else {
				// Setup close button
				closeButton = new Button(context);
				closeButton.setVisibility(View.GONE);

				// Mraid spec requires min. 50 pixel height and width for close
				// area
				closeButton.setMinHeight(50);
				closeButton.setMinWidth(50);

				if ((options != null) && (options.closeLabel != null)) {
					closeButton.setText(options.closeLabel);
				} else {
					closeButton.setText("CLOSE"); // XXX string
				}

				closeButton.setOnClickListener(createCloseClickListener(ad,
						options));
			}

			if ((options != null) && (options.showCloseDelay != null)
					&& (options.showCloseDelay > 0)) {
				if (closeButton != null) {
					closeButton.setVisibility(View.INVISIBLE);
					Thread closeThread = new Thread() {
						public void run() {
							try {
								Thread.sleep(options.showCloseDelay * 1000);
							} catch (Exception e) {
							}
							handler.post(new Runnable() {
								public void run() {
									closeButton.setVisibility(View.VISIBLE);
								}
							});
						}
					};
					closeThread.setName("[AdDialogFactory] showCloseDelay");
					closeThread.start();
				}
			} else {
				if (closeButton != null) {
					closeButton.setVisibility(View.VISIBLE);
				}
			}

			if (closeButton != null) {
				closeButton
						.setLayoutParams(createCloseLayoutParameters(options));
				closeButton.setTag("dialogclosecheck");
				adContainer.addView(closeButton);
			}
		}

		maincontainer.addView(adContainer);
		dialog.setContentView(maincontainer);
		dialog.setOnDismissListener(new Dialog.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {

				if (options != null) {
					runRunnable(options.dismissRunnable);
				}
			}
		});

		if ((options != null) && (options.autoCloseDelay != null)
				&& (options.autoCloseDelay > 0)) {
			Thread closeThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(options.autoCloseDelay * 1000);
					} catch (Exception e) {
					}
					handler.post(new Runnable() {
						public void run() {
							closeButton.performClick(); // or dialog.dismiss?
						}
					});
				}
			};
			closeThread.setName("[AdDialogFactory] autoCloseDelay");
			closeThread.start();
		}

		return dialog;
	}

	private void setFeatureDrawableResource(int featureLeftIcon, int ic_launcher) {
		// TODO Auto-generated method stub

	}

	public void setCustomCloseonDialogCheck() {
		if (closeButton != null) {
			if (closeButton.getTag() != null) {
				Object tag = closeButton.getTag();
				if (tag instanceof String) {
					if (((String) tag).equalsIgnoreCase("dialogclosecheck")) {
						closeButton.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	private OnClickListener createCloseClickListener(final View ad,
			final DialogOptions options) {
		return new OnClickListener() {
			@Override
			public void onClick(View view) {

				dialog.dismiss();
				adViewContainer.richmediaEvent(
						MASTAdConstants.CUSTOM_METHOD_DIALOG_DISMISS, null);

				adViewContainer.getHandler().sendEmptyMessage(
						AdMessageHandler.MESSAGE_CLOSE);

				if (options != null) {
					runRunnable(options.closeRunnable); // XXX not needed???
				}
			}
		};
	}

	// for closing the dialog
	synchronized private void runRunnable(Runnable worker) {
		if (worker != null) {

			worker.run();
		}
	}

	public Dialog getDialog() {
		return dialog;
	}

	final public static class DialogOptions {
		Boolean hideTitlebar = null;
		Runnable closeRunnable = null;
		Runnable dismissRunnable = null;
		Integer backgroundColor = null;
		Integer height = null;
		Integer width = null;
		String closeLabel = null;
		Boolean customClose = null;
		Boolean noClose = null;
		Integer showCloseDelay = null;
		Integer autoCloseDelay = null;
	};
}