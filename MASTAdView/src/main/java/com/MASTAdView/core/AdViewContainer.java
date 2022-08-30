//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved.
//
package com.MASTAdView.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.MASTAdDelegate;
import com.MASTAdView.MASTAdDelegate.RichmediaEventHandler;
import com.MASTAdView.MASTAdLog;
import com.MASTAdView.MASTAdRequest;
import com.MASTAdView.MASTAdView;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.HtmlFontHelper;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AdViewContainer extends RelativeLayout implements ContentManager.ContentConsumer {
  /**
   * Default viewport string injected into ad view *
   */
  private static final String defaultViewportDefinition =
      "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0, user-scalable=no\"/>";
  /**
   * Default body style css injected into ad view *
   */
  private static final String defaultBodyStyle = "<style>body{margin: 0px; padding: 0px;}</style>";
  public static String mScriptPath = null;
  final private MASTAdLog adLog = new MASTAdLog(this);
  private Context context;
  private TextView adTextView;
  // private ImageView adImageView;
  private View adImageView;
  private AdWebView adWebView;
  private Button bannerCloseButton = null;
  private Button customCloseButton = null;
  private Integer defaultImageResource = null;
  private int defaltBackgroundColor = Color.TRANSPARENT;
  private int defaultTextColor = Color.BLACK;
  private boolean isShowCloseOnBanner = false;
  private boolean isShowPreviousAdOnError = false;
  private AdSizeUtilities adSizeUtilities;
  private boolean idSetInterstitial = false;
  private boolean setCustomCloseInterstitial = false;
  private MASTAdRequest adserverRequest;
  private String lastRequest;
  private AdData lastResponse;
  private int requestCounter = 0;
  private DisplayMetrics metrics = null;
  private MASTAdDelegate adDelegate;
  private int showCloseInterstitialTime = 0;
  private Handler handler;
  private AdLocationListener locationListener = null;
  private ViewTreeObserver.OnGlobalLayoutListener layoutListener = null;
  // Local notion of placement type, partically duplicating the mraid
  // interface value, needed for non-mraid ads
  private MraidInterface.PLACEMENT_TYPES adPlacementType = MraidInterface.PLACEMENT_TYPES.INLINE;
  // Reference to self
  private AdViewContainer self;
  // Use internal browser or standalone browser?
  private boolean useInternalBrowser = false;
  // Coordinates of view on screen
  private int[] coordinates = {0, 0};
  private Context mActivityContext;
  private CONTENT_PROCESS_STATE mContentState = CONTENT_PROCESS_STATE.INVALID;
  private ContainerState mContainerState = ContainerState.ACTIVITY_CONTEXT_INVALID;

  private AdRefreshState mAdRefreshState = AdRefreshState.AD_EXPIRED;

  private String mUserAgentToUse = "";

  //
  // Constructors
  //

  public AdViewContainer(Context context, Integer site, Integer zone) {
    super(context); // NOTE: needs to be an activity for orientation changes
    // to work

    initialize(context, null, false);

    adserverRequest.setProperty(MASTAdRequest.parameter_site, site);
    adserverRequest.setProperty(MASTAdRequest.parameter_zone, zone);
  }

  public AdViewContainer(Context context, Integer site, Integer zone, String aUserGentToUse,
                         boolean isInterstitial,
                         boolean isBackgroundTransparent) {
    super(context.getApplicationContext()); // NOTE: needs to be an activity
    // for orientation changes
    // to work

    initialize(context, aUserGentToUse, isBackgroundTransparent);

    adserverRequest.setProperty(MASTAdRequest.parameter_site, site);
    adserverRequest.setProperty(MASTAdRequest.parameter_zone, zone);
    adPlacementType = MraidInterface.PLACEMENT_TYPES.INLINE;
    if (isInterstitial) {
      adPlacementType = MraidInterface.PLACEMENT_TYPES.INTERSTITIAL;
    }
    if (adWebView != null) {
      adWebView.getMraidInterface().setPlacementType(adPlacementType);
    }
  }

  public AdViewContainer(Context context, Integer site, Integer zone, boolean isInterstitial) {
    super(context); // NOTE: needs to be an activity for orientation changes
    // to work

    initialize(context, null, false);

    adserverRequest.setProperty(MASTAdRequest.parameter_site, site);
    adserverRequest.setProperty(MASTAdRequest.parameter_zone, zone);

    adPlacementType = MraidInterface.PLACEMENT_TYPES.INLINE;
    if (isInterstitial) {
      adPlacementType = MraidInterface.PLACEMENT_TYPES.INTERSTITIAL;
    }
    adWebView.getMraidInterface().setPlacementType(adPlacementType);
  }

  public AdViewContainer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle); // NOTE: needs to be an activity for
    // orientation changes to work

    initialize(context, null, false);

    LayoutAttributeHandler layoutHandler = new LayoutAttributeHandler(this);
    layoutHandler.processAttributes(attrs);

    // For layout based views, peform an implicit update() as long as site
    // and zone
    // are set, so that user doesn't need to get a code reference to the
    // veiw just
    // to start an update.
    if ((adserverRequest.getProperty(MASTAdRequest.parameter_site) != null)
        && (adserverRequest.getProperty(MASTAdRequest.parameter_zone) != null)) {
      update();
    }
  }

  public AdViewContainer(Context context, AttributeSet attrs) {
    super(context, attrs); // NOTE: needs to be an activity for orientation
    // changes to work

    initialize(context, null, false);

    LayoutAttributeHandler layoutHandler = new LayoutAttributeHandler(this);
    layoutHandler.processAttributes(attrs);

    // For layout based views, peform an implicit update() as long as site
    // and zone
    // are set, so that user doesn't need to get a code reference to the
    // veiw just
    // to start an update.
    if ((adserverRequest.getProperty(MASTAdRequest.parameter_site) != null)
        && (adserverRequest.getProperty(MASTAdRequest.parameter_zone) != null)) {
      update();
    }
  }

  public AdViewContainer(Context context) {
    super(context); // NOTE: needs to be an activity for orientation changes
    // to work

    initialize(context, null, false);
  }

  //
  // Initialization/view creation
  //

  // Common initialization for various constructors; creates ad request
  // object, handler, orientation
  // listeners, display metrics, ad size helper, delegates, and views.
  private void initialize(Context activity, String aUserGentToUse,
                          boolean isBackgroundTransparent) {
    context = activity.getApplicationContext();
    setScriptPath();

    if (adserverRequest == null) {
      adserverRequest = new MASTAdRequest(adLog, context);
    }

    self = this; // save reference to the container

    // Setup handler for inter-thread communication/method invocation
    handler = new AdMessageHandler(this);

    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    // listen for orientation changes, and handle them for eveyr add view
    // orientationChangeListener = OrientationChangeListener.getInstance(
    // context, windowManager.getDefaultDisplay());
    // orientationChangeListener.addView(this);

    // Save original screen dimensions for later use
    metrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(metrics);

    // Setup ad dialog factory for interstitial/exapnded use
    // adDialogFactory = new AdDialogFactory(context);
    // adSizeUtilities = new AdSizeUtilities(this, metrics);

    // Create views for each ad type
    adTextView = createTextView(activity);

    // create view with gif
    adImageView = createImageView(activity, true);

    if (null != aUserGentToUse && aUserGentToUse.length() > 0) {
      mUserAgentToUse = aUserGentToUse;
    }
    // animation support working on other device : ntin
    adWebView = createWebView(activity, isBackgroundTransparent);

    // Setup auto parameters (such as user agent, etc.)
    setAutomaticParameters();

    setVisibility(View.GONE);
    setBackgroundColor(Color.TRANSPARENT);

    ContentManager.getInstance(this); // force create

    adDelegate = new MASTAdDelegate();

    // Set a global scroll listener which should be called when anything in
    // the view tree scrolls;
    // when this is calld, check the visibility of the ad view and update
    // the viewable property appropriately.
    getViewTreeObserver().addOnScrollChangedListener(new GlobalScrollListener(this));
  }

  static class GlobalScrollListener implements ViewTreeObserver.OnScrollChangedListener {

    WeakReference<AdViewContainer> adViewContainerWeakReference;

    private GlobalScrollListener(AdViewContainer adViewContainer) {
      adViewContainerWeakReference = new WeakReference<>(adViewContainer);
    }

    @Override
    public void onScrollChanged() {
      AdViewContainer adViewContainer = adViewContainerWeakReference.get();
      if (adViewContainer != null) {
        adViewContainer.setViewable();
      }
    }
  }

  public void removeContent() {
    // If interstitial, resized or expanded ad open, close
    close(null, false); // XXX

    // Reset all to initial state
    removeAllViews();

    // Try to recycle bitmap memory
    if (adImageView instanceof ImageView) {
      freeBitmapImageviewResouces((ImageView) adImageView);
    }
  }

  public void destroy() {
    if (adWebView != null) {
      adWebView.cancel();
      adWebView.getHtml5WebView().destroy();
    }
    adDelegate = null;
    getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
    layoutListener = null;
  }

  public void reset() {
    removeContent();

    bannerCloseButton = null;
    customCloseButton = null;

    // If listening for location updates, stop
    if (locationListener != null) {
      locationListener.stop();
      locationListener = null;
    }

    defaultImageResource = null;

    defaltBackgroundColor = Color.TRANSPARENT;
    defaultTextColor = Color.BLACK;

    isShowCloseOnBanner = false;
    isShowPreviousAdOnError = false;
    resetCustomVariables();
    adserverRequest.reset();
  }

  public void softReset() {
    if (null != adWebView) {
      adWebView.loadUrl("about:blank");
    }

    bannerCloseButton = null;
    customCloseButton = null;

    // If listening for location updates, stop
    if (locationListener != null) {
      locationListener.stop();
      locationListener = null;
    }

    defaultImageResource = null;

    defaltBackgroundColor = Color.TRANSPARENT;
    defaultTextColor = Color.BLACK;

    isShowCloseOnBanner = false;
    isShowPreviousAdOnError = false;
    resetCustomVariables();
    adserverRequest.reset();
  }

  /**
   * Function meant for reseting newly introduced variables
   */
  void resetCustomVariables() {
    mAdRefreshState = AdRefreshState.AD_INVALID;
    mContentState = CONTENT_PROCESS_STATE.INVALID;
    mContainerState = ContainerState.ACTIVITY_CONTEXT_INVALID;

  }

  private TextView createTextView(Context context) {
    TextView v = new TextView(context);

    // Child will fill the parent container; we manage the size at the
    // parent level
    v.setLayoutParams(
        new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    // apply standard properties
    v.setBackgroundColor(defaltBackgroundColor);
    v.setTextColor(defaultTextColor);

    return v;
  }

  private View createImageView(Context context, boolean withAnimataedGifSupport) {
    View v;
    if (withAnimataedGifSupport) {
      boolean handleClicks = true;
      v = new AdImageView(this, adLog, metrics, handleClicks);
      v.setLayoutParams(createAdLayoutParameters());
    } else {
      v = new ImageView(context);

      // Child will fill the parent container; we manage the size at the
      // parent level
      v.setLayoutParams(
          new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    // apply standard properties
    v.setBackgroundColor(defaltBackgroundColor);

    return v;
  }

  public AdWebView createWebView(final Context context) {
    return createWebView(context, false);
  }

  public AdWebView createWebView(final Context context, boolean isBackgroundTransparent) {
    boolean supportMraid = true;
    boolean handleClicks = true;

    AdWebView v = new AdWebView(this, adLog, metrics, supportMraid, handleClicks, context,
        isBackgroundTransparent);
    v.setLayoutParams(createAdLayoutParameters());
    v.setBackgroundColor(defaltBackgroundColor); // change nitin
    // Set a global layout listener which will be called when the layout
    // pass is completed and the view is drawn;
    // this seems to be the only reliable way to get the initial location
    // information which isn't set until the
    // layout is complete.
    layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
      public void onGlobalLayout() {
        setCurrentLocation();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
      }
    };
    getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    return v;
  }

  private RelativeLayout.LayoutParams createAdLayoutParameters() {

    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

    return layoutParams;
  }

  public AdWebView getAdWebView() {

    return adWebView;
  }

  public View getAdImageView() {
    return adImageView;
  }

  public TextView getAdTextView() {
    return adTextView;
  }

  public boolean prefetchImages() {
    // If using an imageview for images, standalone fetch is required,
    // but if using a webview variation (for animated gif support), no.
    if (adImageView instanceof ImageView) {
      return true;
    }

    return false;
  }

  // remove and leave logic in ad server request
  public String getUserAgent() {
    String userAgent = (String) adserverRequest.getProperty(MASTAdRequest.parameter_userAgent);
    if (userAgent == null) {
      if (adWebView != null) {
        userAgent = adWebView.getSettings().getUserAgentString();
      }

      if ((userAgent != null) && (userAgent.length() > 0)) {
        adserverRequest.setProperty(MASTAdRequest.parameter_userAgent, userAgent);
      }
    }

    return userAgent;
  }

  protected void setAutomaticParameters() {
    // Set SDK version
    String version = MASTAdConstants.SDK_VERSION;
    if ((version != null) && (version.length() > 0)) {
      adserverRequest.setProperty(MASTAdRequest.parameter_version, version);
    }

    // Set user agent to match what comes from a web view
    getUserAgent();
  }

  //
  // Layout management for container view
  //

  /**
   * Override method to set layout parameters so that local copy of
   * width/height can be saved.
   */
  @Override
  public void setLayoutParams(ViewGroup.LayoutParams params) {

    setVisibility(View.VISIBLE);

    super.setLayoutParams(params);
  }

  // view being notified of a size change (rotation?)
  protected void onSizeChanged(int w, int h, int ow, int oh) {
    if (adWebView != null) {

      // // making the setCurrentposition to be based on the adView not
      CheckAndSetPosition(w, h);

      // Notify ad that size has changed
      adWebView.getMraidInterface().fireSizeChangeEvent(w, h);

      if ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.LOADING)
          || (adWebView.getMraidInterface().getState() == MraidInterface.STATES.DEFAULT)) {
        // and pass event through, unless expanded/resized (where this
        // view is being covered)
        super.onSizeChanged(w, h, ow, oh);
      }
    }
  }

  //
  // Content loaders; NOTE - must call these from a UI thread!!
  //

  private void setTextContent(AdData ad) {
    // remove view from any other containers (if needed)
    if (adTextView.getParent() != null) {
      ((ViewGroup) adTextView.getParent()).removeView(adTextView);
    }

    addView(adTextView);
    adTextView.setText(ad.text);
    adTextView.setVisibility(View.VISIBLE);

    if (ad.clickUrl != null) {
      AdClickHandler clickHandler = new AdClickHandler(this, ad);
      adTextView.setOnClickListener(clickHandler);
    }

  }

  private void setImageContent(AdData ad) {
    // remove view from any other containers (if needed)
    if (adImageView.getParent() != null) {
      ((ViewGroup) adImageView.getParent()).removeView(adImageView);
    }

    addView(adImageView);

    if (adImageView instanceof ImageView) {
      ((ImageView) adImageView).setImageBitmap(ad.imageBitmap);

      if (ad.clickUrl != null) {
        AdClickHandler clickHandler = new AdClickHandler(this, ad);
        adImageView.setOnClickListener(clickHandler);
      }
    } else {
      ((AdImageView) adImageView).setImage(ad);
    }

    adImageView.setVisibility(View.VISIBLE);

    // mViewState = Mraid.STATES.DEFAULT;

    // setBackgroundColor(Color.BLACK);
  }

  private void setWebContent(String content, final String aBasePath, String aMetaData, String
      useDhFont) {
    if (adWebView == null) {
      return;
    }
    if (adWebView.getParent() != null) {
      ((ViewGroup) adWebView.getParent()).removeView(adWebView);
    }

    // If state is not loading, set that before continuing
    if (adWebView.getMraidInterface().getState() != MraidInterface.STATES.LOADING) {
      // if(adWebView.getMraidInterface().getState() !=
      // MraidInterface.STATES.DEFAULT){
      Log.i("RMA", "resetForNewAd STAE:" + adWebView.getMraidInterface().getState());
      adWebView.resetForNewAd();
      // }
    }
    // Put web view in place
    addView(adWebView);
    this.setVisibility(View.VISIBLE);

    final String dataOut = setupViewport(false, content, aMetaData, useDhFont);
    // loading ad from loacal data from sdcard
    Logger.i("RMA", "setWebContent loadDataWithBaseURL aBasePath" + aBasePath);
    adWebView.loadDataWithBaseURL(aBasePath, dataOut, "text/html", "UTF-8", null);

    mAdRefreshState = AdRefreshState.AD_SERVING;
    setContentState(CONTENT_PROCESS_STATE.COMPLETE);
  }

  // Display ad content in appropriate view based on ad type
  synchronized private void setAdContent(AdData ad) {
    Log.i("RMA", "setAdContent adviewcontent");
    if (ad == null || isContentProcessed()) {
      Log.e("RMA", "setAdContent :: skiped");
      return;
    }
    setContentState(CONTENT_PROCESS_STATE.PROCESSING);
    // ("MRAID_FLOW", "setAdContent");
    removeAllViews();

    if (ad.adType != MASTAdConstants.AD_TYPE_IMAGE) {
      // Try to recycle bitmap memory
      if (adImageView instanceof ImageView) {
        freeBitmapImageviewResouces((ImageView) adImageView);
      }
    }

    if (isShowCloseOnBanner) {
      // put close button back if requested
      if (bannerCloseButton != null) {
        addView(bannerCloseButton);
      }

      showCloseButtonWorker();
    }

    if (ad.adType == MASTAdConstants.AD_TYPE_TEXT) {
      setContentState(CONTENT_PROCESS_STATE.INVALID);
      setTextContent(ad);
      sendTrackingImpression(ad);
    } else if (ad.adType == MASTAdConstants.AD_TYPE_IMAGE) {
      setContentState(CONTENT_PROCESS_STATE.INVALID);
      setImageContent(ad);
      sendTrackingImpression(ad);
    } else // RICHMEDIA or THIRDPARTY (which uses rich media)
    {
      setWebContent(ad.richContent, ad.mBasePath, ad.mMetaData, ad.useDHFont);
    }

  }

  private void freeBitmapImageviewResouces(ImageView view) {
    // System.out.println("Free image view resources...");

    // Do everything possible to free up memory associated with the ad
    // image;
    // this can be important because android allocates and handles bitmap
    // memory
    // differently from application memory, and if you run out your app will
    // crash
    // with no way chance to resolve it.
    if (view != null) {
      Bitmap bm;
      Drawable d;

      d = view.getBackground();
      if (d != null) {
        d.setCallback(null);
        if (d instanceof BitmapDrawable) {
          bm = ((BitmapDrawable) d).getBitmap();
          view.setBackgroundDrawable(null);
          bm.recycle();
          bm = null;
        } else {
          view.setBackgroundDrawable(null);
        }
      }

      d = view.getDrawable();
      if (d != null) {
        d.setCallback(null);
        if (d instanceof BitmapDrawable) {
          bm = ((BitmapDrawable) d).getBitmap();
          view.setImageDrawable(null);
          bm.recycle();
          bm = null;
        } else {
          view.setImageDrawable(null);
        }
      }
    }
  }

  private void sendTrackingImpression(AdData ad) {
    if ((ad != null) && (ad.trackUrl != null) && (ad.trackUrl.length() > 0)) {
      // Looks like we have a tracking url, fire off worker to send
      // impression back to server
      AdData.sendImpressionInBackground(ad.trackUrl, getUserAgent());
    }
  }

  //
  // Generic ad content loader which can be called from a non-ui thread
  //

  public void setAdContentOnUi(final AdData ad) {
    handler.post(new Runnable() {
      public void run() {
        setAdContent(ad);

				/*
         * getLayoutParams().width = layoutWidth;
				 * getLayoutParams().height = layoutHeight; requestLayout();
				 */
      }
    });
  }

  /**
   * Immediately update ad view contents.
   */
  public void update() {
    MraidInterface.STATES state = adWebView.getMraidInterface().getState();
    if ((state == MraidInterface.STATES.DEFAULT) || (state == MraidInterface.STATES.LOADING)) {
      adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "update", "");
      StartLoadContent();
    } else {
      adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "update", "skipped - state not default (" + state + ")");
    }
  }

  //
  // Integration with ad-fetching/parsing
  //

  // start loading an ad from server
  public void StartLoadContent() {

    adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "StartLoadContent", "");

    // have to have a valid site & zone to request content
    if ((adserverRequest == null) ||
        (adserverRequest.getProperty(MASTAdRequest.parameter_site, 0) == 0)
        || (adserverRequest.getProperty(MASTAdRequest.parameter_zone, 0) == 0)) {
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "StartLoadContent", "site=0 or zone=0");
      return;
    }

    if ((defaultImageResource != null) && (getBackground() == null)) {
      try {
        handler.post(new SetBackgroundResourceAction(defaultImageResource));
      } catch (Exception e) {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "StartLoadContent", e.getMessage());
      }
    }

    // If expanded form of ad is being displayed, we don't load new content
    if (adWebView.getMraidInterface().getState() != MraidInterface.STATES.EXPANDED) {
      try {
        if (adWebView.getMraidInterface().getState() == MraidInterface.STATES.RESIZED) {
          // Ad view is going to reload & resize to default state; we
          // need our state to match that
          adWebView.getMraidInterface().setState(MraidInterface.STATES.DEFAULT);
        }

        // if delegate defined, invoke
        if (adDelegate != null) {
          MASTAdDelegate.AdDownloadEventHandler downloadHandler = adDelegate.getAdDownloadHandler();
          if (downloadHandler != null) {
            downloadHandler.onDownloadBegin((MASTAdView) this);
          }
        }

        String url = adserverRequest.toString(MASTAdConstants.AD_REQUEST_TYPE_XML);
        lastRequest = url;
        requestCounter++;
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "requestGet[" + String.valueOf(requestCounter) + "]",
            url);
        ContentManager.getInstance(this).startLoadContent(this, url);
      } catch (Exception e) {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "StartLoadContent.requestGet", e.getMessage());
        // interceptOnAdDownload.error(this, e.getMessage());

      }
    }
  }

  // Invoked when retrieving an ad fails
  private void setErrorResult(AdData ad) {
    if (ad.serverErrorCode != null) {
      // If the server returned an error code, and it is 404 (no ads
      // available) we treat that as informational;
      // other codes are true errors.
      if (ad.serverErrorCode == 404) {
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "requestGet result[" + String.valueOf(requestCounter)
            + "][ERROR][CODE=" + ad.serverErrorCode + "]", ad.error);
      } else {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "requestGet result[" + String.valueOf(requestCounter)
            + "][ERROR][CODE=" + ad.serverErrorCode + "]", ad.error);
      }
    } else {
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR,
          "requestGet result[" + String.valueOf(requestCounter) + "][ERROR]",
          ad.error);
    }

    if (adDelegate != null) {
      MASTAdDelegate.AdDownloadEventHandler downloadHandler = adDelegate.getAdDownloadHandler();
      if (downloadHandler != null) {
        downloadHandler.onDownloadError((MASTAdView) this, ad.error);
      }
    }

    if (defaultImageResource != null) {
      try {
        handler.post(new SetBackgroundResourceAction(defaultImageResource));
      } catch (Exception e) {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "setErrorResult", e.getMessage());
      }
    }

    // Show previous ad?
    if (lastResponse == null) {
      return;
    }
    // If supposed to show previous ad on error, but no previous content,
    // skip out
    if ((lastResponse != null) && (lastResponse.hasContent()) && (lastResponse.error == null)
        && !isShowPreviousAdOnError) {
      return;
    }

    setResult(lastResponse);

    return;
  }

  // handle result ad data after fetch from server
  synchronized public boolean setResult(final AdData ad) {
    Log.i("RMA", "setresult ADVIEWCONTAINER");
    if (ad == null) {
      AdData error = new AdData();
      error.error = MASTAdConstants.STR_NULL_AD_ERROR;
      setErrorResult(error);
      return false;
    } else if (ad.error != null) {
      setErrorResult(ad);
      return false;
    } else if (ad.hasContent() == false) {
      ad.error = MASTAdConstants.STR_NO_AD_CONTENT_ERROR;
      setErrorResult(ad);
      return false;
    } else {
      // Callback to notify that ad download completed.
      // if delegate defined, invoke
      if (adDelegate != null) {
        MASTAdDelegate.AdDownloadEventHandler downloadHandler = adDelegate.getAdDownloadHandler();
        if (downloadHandler != null) {
          downloadHandler.onDownloadEnd((MASTAdView) this);
        }
      }

      if (this.getParent() == null) {
        // View not currently included in any layout, don't try to show
        // content for now, just save it
        lastResponse = ad;
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG,
            "requestGet result[" + String.valueOf(requestCounter) + "]",
            ad.toString());
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "setResult",
            "no parent for ad view, skipping display for now...");
        return false;
      }

      adLog.log(MASTAdLog.LOG_LEVEL_DEBUG,
          "requestGet result[" + String.valueOf(requestCounter) + "]",
          ad.toString());
    }

    try {
      if (ad.adType == MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY) {
        notifyExternalThirdPartyAd(ad);
      } else {
        setAdContentOnUi(ad);
      }
    } catch (Exception e) {
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "StartLoadContent", e.getMessage());
      return false;
    }

    // Not an error, so remember this as the new "last" ad
    lastResponse = ad;

    return true;
  }

  // Client side third party (SDK) ad "redirect"
  private void notifyExternalThirdPartyAd(AdData ad) {
    if ((ad != null) && (adDelegate != null) &&
        (adDelegate.getThirdPartyRequestHandler() != null)) {
      try {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", ad.getAdTypeName());

        if ((ad.externalCampaignProperties != null) && (!ad.externalCampaignProperties.isEmpty())) {
          Iterator<NameValuePair> i = ad.externalCampaignProperties.iterator();
          NameValuePair nvp;
          while (i.hasNext()) {
            nvp = i.next();
            if (nvp != null) {
              params.put(nvp.getName(), nvp.getValue());
            }
          }
        }

        adDelegate.getThirdPartyRequestHandler().onThirdPartyEvent((MASTAdView) this, params);
      } catch (Exception e) {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onThirdPartyRequest", e.getMessage());
      }
    }
  }

  private synchronized void setScriptPath() {

    // commentig this as we are accessing it via assets folder, check
    // setupViewport for more info

    // if (mScriptPath == null) {
    //
    // mScriptPath = FileUtils.copyTextFromJarIntoAssetDir(context,
    // "/mraid.js", "/mraid.js");
    // }

  }

  // Create viewport for showing ad
  public String setupViewport(boolean headerOnly, String body, String aMetaData, String useDHFont) {
    StringBuffer data = new StringBuffer("<html><head>");

    // Insert our javascript bridge library; this is always required
    data.append("<style>*{margin:0;padding:0}</style>");
    data.append("<script src=\"");
    mScriptPath = "file:///android_asset/mraid/mraid.js";
    data.append(mScriptPath);
    data.append("\" type=\"text/javascript\"></script>");
    data.append(getInjectionHeaderCode(aMetaData));
    data.append("</head><body>");
    if (!headerOnly && (body != null)) {
      boolean isUseDHFont = DataUtil.parseBoolean(useDHFont, false);
      if (isUseDHFont) {
        // In case of Urdu Ad, server takes care of setting text-Alignment to right.
        // So no special handling required for Urdu here.
        boolean isUrdu = false;
        body = HtmlFontHelper.wrapToFontHTML(/*selectCopyDisabled*/true, body, Constants
            .EMPTY_STRING, Constants.EMPTY_STRING, isUrdu, false, null,0);
      }

      data.append(body);
    }

    // content added as part of the getting the callBack for AdLoaded
    // Completely
    data.append(
        "<script> var onloadbeforemraid = window.onload; function callmraidloaded() { mraid.adLoaded(); if(onloadbeforemraid !== null) { onloadbeforemraid(); } } window.onload=callmraidloaded;</script>");
    data.append("</body></html>");
    // data.append("<script> window.onload = mraid.adLoaded();</script>");

    // System.out.println("SetupViewport: final string: " +
    // data.toString());
    return data.toString();
  }

  public void setAdInBackground(){
    if (null != adSizeUtilities) {
      adSizeUtilities.setAdInBackground();
    }
  }

  // Called when close() method is invoked from ad view
  public String close(Bundle ignoredBundle, boolean aForceClose) {
    String retValue = null;
    MraidInterface.STATES adState = adWebView.getMraidInterface().getState();
    // ("MRAID_FLOW", "mraid close ");
    if (adPlacementType == MraidInterface.PLACEMENT_TYPES.INTERSTITIAL) {
      closeInterstitial();
      if ((customCloseButton != null) && (customCloseButton.getParent() != null)) {
        ((ViewGroup) customCloseButton.getParent()).removeView(customCloseButton);
      }

      // add close button again???
    } else if (adState == MraidInterface.STATES.EXPANDED) {
      // System.out.println("Closing expanded ad view...");
      // ("MRAID_FLOW", "mraid close EXPANDED ");
      // dismiss dialog containing expanded view
      if (null != adSizeUtilities) {
        adSizeUtilities.dismissDialog();
      }

      // In the case of a 2-part creative, the original adview was left in
      // place,
      // which means none of this is needed.
      ViewGroup parent = (ViewGroup) adWebView.getParent();
      if (parent != this) {
        // Remove adview from temporary container
        if (parent != null) {
          parent.removeView(adWebView);
        }

        // Reset layout parameters
        adWebView.setLayoutParams(createAdLayoutParameters());

        // Move ad view back to normal container
        this.addView(adWebView);
        refreshParent();
        if (null != adSizeUtilities) {
          adSizeUtilities.clearExpandedAdView();
        }
      }

      if (aForceClose) {
        // if force close don't worry make it to 'invalid' state
        // adWebView.getMraidInterface().setState(
        // MraidInterface.STATES.INVALID);
        retValue = "StateHandled";
      } else {
        // Return to default state
        adWebView.getMraidInterface().setState(MraidInterface.STATES.DEFAULT);
      }
      adWebView.getMraidInterface().fireSizeChangeEvent(this.getWidth(), this.getHeight());
      // adWebView.getMraidInterface().fireSizeChangeEvent(AdSizeUtilities.devicePixelToMraidPoint(this.getWidth(),
      // context),
      // AdSizeUtilities.devicePixelToMraidPoint(this.getHeight(),
      // context));
    } else if (adState == MraidInterface.STATES.RESIZED) {
      int resizeToX;
      int resizeToY;
      // ("MRAID_FLOW", "mraid close RESIZED ");
      /*
       * if ((resizeOldWidth != 0) && (resizeOldWidth != 0)) { resizeToX =
			 * resizeOldWidth; resizeToY = resizeOldHeight; resizeOldWidth = 0;
			 * resizeOldHeight = 0; } else { resizeToX = this.getWidth();
			 * resizeToY = this.getHeight(); }
			 */
      resizeToX = this.getWidth();
      resizeToY = this.getHeight();

      // Remove adview from temporary container
      ViewGroup parent = (ViewGroup) adWebView.getParent();
      if (parent != null) {
        parent.removeView(adWebView);

        // Also put screen content back in place, unoding change made
        // when resize was first done
        if (null != adSizeUtilities) {
          adSizeUtilities.undoResize();
        }

        // Now remove parent, which was a temporary container created
        // for the expand/resize
        /*
         * ViewGroup pparent = (ViewGroup)parent.getParent(); if
				 * (pparent != null) { pparent.removeView(parent); }
				 */
      }

      // Reset layout parameters
      // adWebView.setLayoutParams(createAdLayoutParameters());
      RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) adWebView.getLayoutParams();
      lp.setMargins(0, 0, 0, 0);
      lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
      lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;

      // Move ad view back to normal container
      this.addView(adWebView);

      refreshParent();

      if (aForceClose) {
        // if force close don't worry make it to 'invalid' state
        // adWebView.getMraidInterface().setState(
        // MraidInterface.STATES.INVALID);
        retValue = "StateHandled";
      } else {
        // Return to default state
        adWebView.getMraidInterface().setState(MraidInterface.STATES.DEFAULT);
      }

      // adWebView.getMraidInterface().fireSizeChangeEvent(this.getWidth(),
      // this.getHeight());
      adWebView.getMraidInterface().fireSizeChangeEvent(resizeToX, resizeToY);
      // adWebView.getMraidInterface().fireSizeChangeEvent(AdSizeUtilities.devicePixelToMraidPoint(this.getWidth(),
      // context),
      // AdSizeUtilities.devicePixelToMraidPoint(this.getHeight(),
      // context));
    } else if (MraidInterface.STATES.DEFAULT.equals(adState)) {
      if (adDelegate != null) {
        MASTAdDelegate.AdActivityEventHandler activityEventHandler =
            adDelegate.getAdActivityEventHandler();
        if (activityEventHandler != null) {
          activityEventHandler.onAdCollapsed((MASTAdView) this);
        }
      }
    } else {
      // Ignored, NOT an error, per spec.
    }

    return retValue;
  }

  //
  // Javascript interaction
  //

  public void closeInterstitial() {
    if (adPlacementType == MraidInterface.PLACEMENT_TYPES.INTERSTITIAL) {
      MraidInterface.STATES adState = adWebView.getMraidInterface().getState();
      if ((adState == MraidInterface.STATES.DEFAULT) ||
          (adState == MraidInterface.STATES.LOADING)) // Non-MRAID
      // ad
      // state
      // never
      // goes
      // beyond
      // loading
      {
        // System.out.println("Closing interstitial ad view...");

        // dismiss dialog containing interstitial view
        if (null != adSizeUtilities) {
          adSizeUtilities.dismissDialog();
        }

        // Make view invisible
        // adWebView.setVisibility(View.GONE);

        // set state to hidden
        adWebView.getMraidInterface().setState(MraidInterface.STATES.HIDDEN);

        // Notify ad that viewable state has changed
        adWebView.getMraidInterface().setViewable(false);
        adWebView.getHtml5WebView().removeAllViews();
        adWebView.getHtml5WebView().destroy();
      } else {
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "AdViewContainer",
            "Attempt to close interstitial with state not default, ignored");
      }
    } else {
      adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "AdViewContainer",
          "Attempt to close interstitial with wrong placement");

    }
  }

  // ZZZ Can we invoke this when view scrolls in/out of screen (viewport)???
  private void setViewable() {
    AdWebView webView = adWebView;
    Rect scrollBounds = new Rect();
    this.getHitRect(scrollBounds);

    if (webView.getLocalVisibleRect(scrollBounds)) {
      // View is within the visible window
      adWebView.getMraidInterface().setViewable(true);
    } else {
      // View is not within the visible window
      adWebView.getMraidInterface().setViewable(false);
    }
  }

  // Hide an interstitial ad view
  public String hide(Bundle data) {

    // if(isInterstitial() && !isExpanded) InterstitialClose();
    if (adPlacementType == MraidInterface.PLACEMENT_TYPES.INTERSTITIAL) {
      adWebView.setVisibility(View.GONE);
      return null;
    }

    return "Hide called for ad that is not interstitial";
  }

  // Show interstitial ad view
  public void showInterstitial(int withDuration, boolean bIsTitleBarEnable) {
    /*
     * if (adPlacementType != MraidInterface.PLACEMENT_TYPES.INTERSTITIAL) {
		 * adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "AdViewContainer",
		 * "Attempt to show interstitial with wrong placement"); return; }
		 */

    // Set ad state to default if it is hidden
    if (adWebView != null) {
      MraidInterface mraid = adWebView.getMraidInterface();

      if (mraid.getState() == MraidInterface.STATES.HIDDEN) {
        mraid.setState(MraidInterface.STATES.DEFAULT);
      }
    }
    // create dialog object for showing interstitial ad : nitin
    if (null != adSizeUtilities) {
      adSizeUtilities.showInterstitialDialog(showCloseInterstitialTime, withDuration,
          setCustomCloseInterstitial,
          bIsTitleBarEnable);
    }
  }

  public String playVideo(Bundle data) {
    String mediaUri = null;
    try {
      mediaUri = data.getString(AdMessageHandler.PLAYBACK_URL);
    } catch (Exception ex) {
      String error = "Error getting playback uri for video: " + ex.getMessage();
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "AdViewContainer.playVideo", error);
      return error;
    }

    if (mediaUri != null) {
      return adWebView.getMraidInterface().getDeviceFeatures().playVideo(mediaUri);
    } else {
      String error = "No playback uri for video found, skipping...";
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "AdViewContainer.playVideo", error);
      return error;
    }
  }

  public String createCalendarEvent(Bundle data) {
    try {
      String description = data.getString(MraidInterface
          .get_CALENDAR_EVENT_PARAMETERS_name(
              MraidInterface.CALENDAR_EVENT_PARAMETERS.DESCRIPTION));
      if (description == null) {
        description = "";
      }

      String summary = data.getString(MraidInterface
          .get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.SUMMARY));
      if (summary == null) {
        summary = "";
      }

      String location = data.getString(MraidInterface
          .get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.LOCATION));
      if (location == null) {
        location = "";
      }

      String start = data.getString(MraidInterface
          .get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.START));
      if (start == null) {
        String error = "Missing calendar event start date/time, cannot continue";
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "AdViewContainer.createCalendar", error);
        return error;
      }

      String end = data.getString(MraidInterface
          .get_CALENDAR_EVENT_PARAMETERS_name(MraidInterface.CALENDAR_EVENT_PARAMETERS.END));
      if (end == null) {
        String error = "Missing calendar event end date/time, cannot continue";
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "AdViewContainer.createCalendar", error);
        return error;
      }

      return adWebView.getMraidInterface().getDeviceFeatures()
          .createCalendarInteractive(description, location, summary, start, end);
    } catch (Exception ex) {
      String error = "Error getting parameters for calendar event: " + ex.getMessage();
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "AdViewContainer.createCalendar", error);
      return error;
    }
  }

  /**
   * Resize the ad view container; this method is invoked by the javascript
   * interface and runs on the UI thread via a handler invocation, with data
   * passed as part of the message bundle.
   */
  public String resize(Bundle data) {
    // resizeOldWidth = this.getWidth();
    // resizeOldHeight = this.getHeight();

    // You can only invoke resize from the default ad state, or from the
    // resized state (to further change the size)
    if ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.DEFAULT)
        || (adWebView.getMraidInterface().getState() == MraidInterface.STATES.RESIZED)) {
      return adSizeUtilities.startResize(data);
    }

    return MASTAdConstants.STR_RICHMEDIA_ERROR_RESIZE;
  }

  // private int resizeOldWidth = 0;
  // private int resizeOldHeight = 0;

  public String open(Bundle data) {
    String url = data.getString(AdMessageHandler.OPEN_URL);
    if (adDelegate != null) {
      MASTAdDelegate.AdActivityEventHandler clickHandler = adDelegate.getAdActivityEventHandler();
      if (clickHandler != null) {
        clickHandler.onAdClicked((MASTAdView) this, url);
      }
    }
    try {
      // bypassing all open calls to be handled by common handled i.e
      // openInBackgroundThread
      // Pass options for dialog through to creator
//      AdDialogFactory.DialogOptions options = new AdDialogFactory.DialogOptions();
//      options.backgroundColor = Color.BLACK;
//      options.noClose = true; // no add-on close function, just
//      // browser default

// Commenting this to handle clicks by individual viewholders/views
//      return adSizeUtilities.openInBackgroundThread(options, url);

      return null;
      // url = URLDecoder.decode(url, "UTF-8");
      // Uri uri = Uri.parse(url);
      //
      // // for "action" urls (sms, tel, mailto) just invoke, for others
      // do a
      // // fetch/open
      // if (uri.getScheme().equalsIgnoreCase("mailto")) {
      // MailTo mt = MailTo.parse(url);
      // Intent i = new Intent(Intent.ACTION_SEND);
      // i.setType("text/plain");
      // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // i.putExtra(Intent.EXTRA_EMAIL, new String[] { mt.getTo() });
      // i.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
      // i.putExtra(Intent.EXTRA_CC, mt.getCc());
      // i.putExtra(Intent.EXTRA_TEXT, mt.getBody());
      // context.startActivity(i);
      //
      // return null;
      // } else if (uri.getScheme().equalsIgnoreCase("sms")) {
      // Intent i = new Intent(Intent.ACTION_VIEW);
      // i.setType("vnd.android-dir/mms-sms");
      //
      // // android doesn't parse these urls correctly for all os
      // // versions...
      // String phoneNumber = url.substring(4);
      // i.putExtra("address", phoneNumber);
      // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // // smsIntent.putExtra("sms_body","Body of Message");
      //
      // context.startActivity(i);
      //
      // return null;
      // } else if (uri.getScheme().equalsIgnoreCase("tel")) {
      // Intent i = new Intent(Intent.ACTION_DIAL); // could use
      // // ACTION_CALL to
      // // immedidately
      // // place the call,
      // // but this is
      // // better
      // i.setData(uri);
      // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // context.startActivity(i);
      //
      // return null;
      // } else {
      // // Pass options for dialog through to creator
      // AdDialogFactory.DialogOptions options = new
      // AdDialogFactory.DialogOptions();
      // options.backgroundColor = Color.BLACK;
      // options.noClose = true; // no add-on close function, just
      // // browser default
      //
      // return adSizeUtilities.openInBackgroundThread(options, url);
      // }
    } catch (Exception e) {
      adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "openUrlInExternalBrowser",
          "url=" + url + "; error=" + e.getMessage());
      return e.getMessage();
    }
  }

  public String expand(Bundle data) {
    // You can only invoke expand from the default or expanded ad states

    // NH : also alow resize state
    try {
      if ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.DEFAULT)
          || (adWebView.getMraidInterface().getState() == MraidInterface.STATES.RESIZED)) {
        return adSizeUtilities.startExpand(data);
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
    return MASTAdConstants.STR_RICHMEDIA_ERROR_EXPAND; // new, more specific
    // error
  }

  public String updateOrientationProperties(Bundle data) {
    if (adWebView.getMraidInterface().getState() == MraidInterface.STATES.EXPANDED) {
      return adSizeUtilities.setOrientationProperties(data);
    }

    return null;
  }

  /**
   * Get current injection header code string.
   *
   * @param aMetaData
   * @return Current injection header value.
   */
  private String getInjectionHeaderCode(String aMetaData) {
    // Default fragment, revised as of 2.12 SDK
    String retString = defaultViewportDefinition + defaultBodyStyle;
    if (null != aMetaData && aMetaData.trim().length() > 0) {
      retString = aMetaData + defaultBodyStyle;
    }
    return retString;
  }

  //
  // Injection header
  //

  /**
   * Provide access to the diagnostic log object created internal to this view
   *
   * @return MASTAdLog usable for diagnostics debug logging
   */
  public MASTAdLog getLog() {
    return adLog;
  }

  public Handler getHandler() {
    return handler;
  }

  public String getLastRequest() {
    return lastRequest;
  }

  //
  // Misc
  //

  public AdData getLastResponseObject() {
    return lastResponse;
  }

  public String getLastResponse() {
    return lastResponse.responseData;
  }

  public MASTAdRequest getAdRequest() {
    return adserverRequest;
  }

  public MASTAdDelegate getAdDelegate() {
    return adDelegate;
  }

  public Integer getDefaultImageResource() {
    return defaultImageResource;
  }

  public void setDefaultImageResource(Integer resource) {
    defaultImageResource = resource;
  }

  protected void onAttachedToWindow() {
    Logger.i("RMA", "Adviecontainer onattachtowindow adviewcontainer hash code:" + this.hashCode());
    adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "Attached to Window", "");

    // StartLoadContent(getContext(), this);

    super.onAttachedToWindow();

    if (adDelegate != null) {
      MASTAdDelegate.AdActivityEventHandler activityHandler =
          adDelegate.getAdActivityEventHandler();
      if (activityHandler != null) {
        activityHandler.onAdAttachedToActivity((MASTAdView) this);
      }
    }

    // ??? If the ad content was downloaded while the view was not attached
    // to a window
    // they we attempt to "install" it now; however, need to make sure we
    // don't get into
    // a race condition between set content completing and this method at
    // the same time.
    if (!isContentProcessed()) {
      if ((lastResponse != null)
          && (lastResponse.hasContent())
          && ((adWebView == null) ||
          ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.LOADING) || (adWebView
              .getMraidInterface().getState() == MraidInterface.STATES.DEFAULT)))) {
        if (lastResponse.adType == MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY) {
          notifyExternalThirdPartyAd(lastResponse);
        } else {
          setAdContent(lastResponse); // If we have ad content from
          // "before", load it
        }
        // ("MRAID_FLOW",
        // "onAttachedWindow :: Reseting Variables");
      }
    }
    if ((adWebView != null)
        &&
        ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.LOADING) || (adWebView
            .getMraidInterface().getState() == MraidInterface.STATES.DEFAULT))) {
      // making the setCurrentposition to be based on the adView not the
      // webview
      CheckAndSetPosition(adWebView.getWidth(), adWebView.getHeight());

      // adWebView.getLocationOnScreen(coordinates); //
      // getLocationInWindow()
      // // for relative
      // ("MRAID_FLOW", "onAttachedToWindow ");
      // adWebView.getMraidInterface()
      // .setCurrentPosition(coordinates[0], coordinates[1],
      // adWebView.getWidth(), adWebView.getHeight());
      //
      // adWebView.getMraidInterface().setCurrentPosition(
      // adWebView.getLeft(), adWebView.getTop(),
      // adWebView.getWidth(), adWebView.getHeight());
    }

    // Notify ad that viewable state has changed
    if (adWebView != null) {
      adWebView.getMraidInterface().setViewable(true);
    }
    // setViewable(adWebView); // if only there was a reliable way to get
    // notified if we're scrolling
  }

  protected void onDetachedFromWindow() {

    adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "Detached from Window", "");
    /*
     * if (image!=null) { image.recycle(); image = null; }
		 */

    if (locationListener != null) {
      locationListener.stop();
      locationListener = null;
    }

    // stop loading any content
    ContentManager.getInstance(this).stopLoadContent(this);

    super.onDetachedFromWindow();

    if (adDelegate != null) {
      MASTAdDelegate.AdActivityEventHandler activityHandler =
          adDelegate.getAdActivityEventHandler();
      if (activityHandler != null) {
        // If the activity is going away, and this callback attempts to
        // do things with the UI
        // it can fail, so protected against an exception.
        try {
          activityHandler.onAdDetachedFromActivity((MASTAdView) this);
        } catch (Exception e) {
          adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "onAdDetachedFromActivity - exceptioin",
              e.getMessage());
        }
      }
    }

    // Notify ad that viewable state has changed
    if (adWebView != null) {
      adWebView.getMraidInterface().setViewable(false);
    }
  }

  synchronized public void setLocationDetection(boolean detect, Integer minWaitMillis,
                                                Float minMoveMeters) {
    if (detect) {
      int isAccessFineLocation =
          context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
      if (isAccessFineLocation == PackageManager.PERMISSION_GRANTED) {
        locationListener = new AdLocationListener(context, minWaitMillis, minMoveMeters,
            LocationManager.GPS_PROVIDER, Looper.getMainLooper(), adLog) {
          public void fail(String m) {
            // Nothing (legacy)
          }

          public void success(Location location) {
            try {
              double latitude = location.getLatitude();
              adserverRequest.setProperty(MASTAdRequest.parameter_latitude,
                  Double.toString(latitude));

              double longitude = location.getLongitude();
              adserverRequest.setProperty(MASTAdRequest.parameter_longitude,
                  Double.toString(longitude));

              adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection changed", "(" + latitude + ";"
                  + longitude + ")");
            } catch (Exception e) {
              adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection", e.getMessage());
            }
          }
        };

        if (locationListener.isAvailable()) {
          adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection",
              "Start listening for location updates");
          locationListener.start();
        } else {
          adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection",
              "Location updates not available");
          locationListener.stop();
          locationListener = null;
        }
      } else {
        adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection", "No permission for GPS");
      }
    } else {
      // If listening, stop
      if (locationListener != null) {
        locationListener.stop();
        locationListener = null;
      }

      adLog.log(MASTAdLog.LOG_LEVEL_DEBUG, "LocationDetection",
          "Stop listening for location updates");
    }
  }

  synchronized public boolean getLocationDetection() {
    // If listener was successfully created and reports it is available, yes
    if ((locationListener != null) && (locationListener.isAvailable())) {
      return true;
    }

    // otherwise, no
    return false;
  }

	/*
   * public int getAutoCloseInterstitialTime() { return
	 * autoCloseInterstitialTime; }
	 *
	 *
	 * public void setAutoCloseInterstitialTime(int time) {
	 * autoCloseInterstitialTime = time; }
	 *
	 *
	 * public int getShowCloseInterstitialTime() { return
	 * showCloseInterstitialTime; }
	 *
	 *
	 * public void setShowCloseInterstitialTime(int time) {
	 * showCloseInterstitialTime = time; }
	 */

  public boolean getUseInternalBrowser() {
    return useInternalBrowser;

  }

  public void setUseInternalBrowser(boolean flag) {
    useInternalBrowser = flag;
  }

  private void showCloseButtonWorker() {
    Thread closeThread = new Thread() {
      public void run() {
        final int visible;
        if (isShowCloseOnBanner) {
          visible = View.VISIBLE;
          try {
            Thread.sleep(showCloseInterstitialTime * 1000);
          } catch (Exception e) {
          }
        } else {
          visible = View.GONE;
        }

        handler.post(new Runnable() {
          public void run() {
            // Create close button for banner
            if (bannerCloseButton == null) {
              bannerCloseButton = createCloseButton(context, createCloseClickListener());
              self.addView(bannerCloseButton);
            }

            bannerCloseButton.setVisibility(visible);
          }
        });
      }
    };
    closeThread.setName("[AdViewContainer] showCloseButton");
    closeThread.start();
  }

  public void showCloseButton(boolean flag, int afterDelay) {
    showCloseInterstitialTime = afterDelay;

    boolean change = false;
    if (flag != isShowCloseOnBanner) {
      change = true;
    }

    isShowCloseOnBanner = flag;

    if (change) {
      showCloseButtonWorker();
    }
  }

  private OnClickListener createCloseClickListener() {
    return new OnClickListener() {
      @Override
      public void onClick(View v) {
        handler.post(new Runnable() {
          @Override
          public void run() {
            // For interstitial ad, dismiss dialog;
            // for banner, remove the view (and if
            // richmedia, set state to hidden?)
            if (adPlacementType == MraidInterface.PLACEMENT_TYPES.INTERSTITIAL) {
              // dismiss dialog containing interstitial view
              adSizeUtilities.dismissDialog();
            } else {
              // Remove ad container from parent, hiding it
              ViewGroup parent = (ViewGroup) self.getParent();
              if (parent != null) {
                // Remove container from parent
                parent.removeView(self);
              }
            }
          }
        });
      }
    };
  }

  public Button createCloseButton(Context c, View.OnClickListener clickListener) {
    Button b;

    if (customCloseButton == null) {
      b = new Button(c);
      b.setMinHeight(50);
      b.setMinWidth(50);
      b.setVisibility(View.GONE); // default is not present

      b.setText("Close"); // string, allow customizing
    } else {
      b = customCloseButton;
    }

    // Position button in upper right
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
    b.setLayoutParams(layoutParams);

    b.setOnClickListener(clickListener);

    return b;
  }

  public Button getCustomCloseButton() {
    return customCloseButton;
  }

  public void setCustomCloseButton(Button closeButton) {
    customCloseButton = closeButton;
  }

  public void onOrientationChange(int orientationAngle, int screenOrientation) {
    // Update size/position (triggering a size change event if appropriate)
    WindowManager windowManager = (WindowManager) (context.getApplicationContext())
        .getSystemService(Context.WINDOW_SERVICE);
    if (metrics == null) {
      metrics = new DisplayMetrics();
    }
    windowManager.getDefaultDisplay().getMetrics(metrics);
    adSizeUtilities.setMetrics(metrics);

    // Update current position values
    // adWebView.getMraidInterface().setCurrentPosition(adWebView.getLeft(),
    // adWebView.getTop(), w, h);
    if (adWebView != null) {
      // making the setCurrentposition to be based on the adView not the
      // webview
      CheckAndSetPosition(adWebView.getWidth(), adWebView.getHeight());

      // adWebView.getLocationOnScreen(coordinates); //
      // getLocationInWindow()
      // // for relative
      //
      // Logger.d("MRAID_FLOW", "onOrientationChange 1.0");
      //
      // adWebView.getMraidInterface()
      // .setCurrentPosition(coordinates[0], coordinates[1],
      // adWebView.getWidth(), adWebView.getHeight());
      // adWebView.getMraidInterface().setCurrentPosition(
      // adWebView.getLeft(), adWebView.getTop(),
      // adWebView.getWidth(), adWebView.getHeight());
    }

    // adWebView.getMraidInterface().setOrientation(orientationAngle);

    // If ad is expanded and a 2 part creative caused a new view to be
    // created,
    // inject events into that one also.
    if ((adWebView.getMraidInterface().getState() == MraidInterface.STATES.EXPANDED)
        && (adSizeUtilities.getExpandedAdView() != null)) {
      AdWebView expandedWebView = adSizeUtilities.getExpandedAdView();
      // expandedWebView.getMraidInterface().setCurrentPosition(expandedWebView.getLeft(),
      // expandedWebView.getTop(), expandedWebView.getWidth(),
      // expandedWebView.getHeight());

      expandedWebView.getMraidInterface().setCurrentPosition(coordinates[0], coordinates[1],
          adWebView.getWidth(), adWebView.getHeight());

      // expandedWebView.getMraidInterface().setOrientation(orientationAngle);
    }
  }

  // //close, customclose,adload are handle by this
  public void richmediaEvent(String method, String parameter) {
    String encodedProperties = parameter;
    String sCloseCustom = null;
    List<NameValuePair> expandProperties = null;
    if (method.equals("setExpandProperties") && idSetInterstitial) {
      URI propertiesUri = null;
      try {
        propertiesUri = new URI("http://expand.properties?" + encodedProperties);
      } catch (URISyntaxException e) {
        // TODO Auto-generated catch block
        // // e.printStackTrace();
      }

      expandProperties = URLEncodedUtils.parse(propertiesUri, "UTF-8");
    }
    // custom close :nitin
    sCloseCustom = JavascriptInterface.getListValueByName(expandProperties, "useCustomClose");
    if (sCloseCustom != null) {
      if (sCloseCustom.equals("true")) {
        setCustomCloseonInterstitialDialog();
        setCustomCloseInterstitial = true;
      }
    }

    if (adDelegate != null) {
      // checkIfAdLoadedMethodAndEnableView(method);
      RichmediaEventHandler handler = adDelegate.getRichmediaEventHandler();
      if (handler != null) {
        handler.onRichmediaEvent((MASTAdView) this, method, parameter);
      }
    }
  }

  public void setCustomCloseonInterstitialDialog() {
    if (adSizeUtilities != null && mActivityContext != null) {
      if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
        adSizeUtilities.setCustomCloseonInterstitialDialog();
      } else {
        ((Activity) mActivityContext).runOnUiThread(new Runnable() {

          @Override
          public void run() {
            adSizeUtilities.setCustomCloseonInterstitialDialog();
          }
        });
      }
    }

  }

  public void setInterstitialFlag() {
    idSetInterstitial = true;
  }

  private boolean isContentProcessed() {
    boolean status = false;
    if ((CONTENT_PROCESS_STATE.COMPLETE == mContentState) ||
        (CONTENT_PROCESS_STATE.PROCESSING == mContentState)) {
      status = true;
    }
    return status;
  }

  public void setContentState(CONTENT_PROCESS_STATE aContentState) {
    mContentState = aContentState;
  }

  private void refreshParent() {
    if (null != this.getRootView()) {

      // this.getParent().requestLayout();
      this.getRootView().requestLayout();
      this.getRootView().invalidate();
      this.getRootView().refreshDrawableState();
    } else {

      this.requestLayout();
    }
  }

  public void setActivityContext(Context aContext, String aPlacmentType) {
    Log.i("RMA", "AdviewContainer setActivityContext");
    mActivityContext = aContext;
    initializeActivityDependentObjects();
    setContainerState(ContainerState.ACTIVITY_CONTEXT_ATTACHED);
    setCurrentPlacementContext(aPlacmentType);
  }

  public Context getActivityContext() {
    return mActivityContext;
  }

  public void initializeActivityDependentObjects() {
    // Setup ad dialog factory for interstitial/exapnded use
    // adDialogFactory = new AdDialogFactory(context);
    adSizeUtilities = new AdSizeUtilities(this, metrics);
  }

  public void setCurrentLocation() {

    if ((this != null) && (this.adWebView != null)
        && (this.adWebView.getMraidInterface().getState() == MraidInterface.STATES.DEFAULT)) {
      CheckAndSetPosition(adWebView.getWidth(), adWebView.getHeight());
    }
  }

  public void CheckAndSetPosition(int aWidth, int aHeight) {
    int[] tempCoOrdinate = {-1111, -1111};
    this.getLocationOnScreen(tempCoOrdinate);

    if (tempCoOrdinate[0] != -1111 && null != adWebView) {
      coordinates[0] = tempCoOrdinate[0];
      coordinates[1] = tempCoOrdinate[1];
    } else {
      if (null != adWebView) {
        adWebView.getLocationOnScreen(tempCoOrdinate);

      }
    }

    // this will not harm as the coordinates will change only if its
    // valid
    adWebView.getMraidInterface()
        .setCurrentPosition(coordinates[0], coordinates[1], aWidth, aHeight);
  }

  public ContainerState getContainerState() {
    return mContainerState;
  }

  public void setContainerState(ContainerState aActivityContextState) {
    mContainerState = aActivityContextState;
  }

  public AdRefreshState getAdRefreshState() {
    return mAdRefreshState;
  }

  public void setAdRefreshState(AdRefreshState aAdRefreshState) {
    mAdRefreshState = aAdRefreshState;
  }

  /**
   * Function which decides whether to complete the provided action
   *
   * @param aAction : Action to be checked
   * @return if true, proceed with action else ignore
   */
  public boolean isContainerReadyForAction(int aAction) {
    boolean readyStatus = false;
    switch (aAction) {
      case AdMessageHandler.MESSAGE_RESIZE:
      case AdMessageHandler.MESSAGE_EXPAND:
        if (ContainerState.ACTIVITY_CONTEXT_INTERMEDIATE != getContainerState()) {
          readyStatus = true;
        }
        if (AdRefreshState.AD_EXPIRED == mAdRefreshState) {
          readyStatus = false;
        }
        break;
      case AdMessageHandler.MESSAGE_OPEN:
      case AdMessageHandler.MESSAGE_PLAY_VIDEO:
      case AdMessageHandler.MESSAGE_CREATE_EVENT:
      case AdMessageHandler.MESSAGE_ORIENTATION_PROPERTIES:
        if (ContainerState.ACTIVITY_CONTEXT_INTERMEDIATE != getContainerState()) {
          readyStatus = true;
        }
        break;
      case AdMessageHandler.MESSAGE_CLOSE:
      case AdMessageHandler.MESSAGE_HIDE:
      case AdMessageHandler.MESSAGE_RAISE_ERROR:
      case AdMessageHandler.MESSAGE_SET_AD_IN_BG:
      default:
        readyStatus = true;
        break;

    }
    return readyStatus;
  }

  /**
   * reports where is the ad being placed
   *
   * @param aCurrentPlacementContext
   */
  public void setCurrentPlacementContext(String aCurrentPlacementContext) {
    if (null != adWebView && null != adWebView.getMraidInterface()) {
      adWebView.getMraidInterface().setCurrentPlacementContext(aCurrentPlacementContext);
    }
  }

  /**
   * Function which opens the market link in all available apps that could
   *
   * @param aUrl
   */
  @SuppressLint("ToastUsedDirectly")
  public void openMarket(String aUrl) {
    if (null != mActivityContext) {
      try {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(aUrl));

        mActivityContext.startActivity(intent);
        // No need to close the app(activity) as user
        // will
        // be able to come back after rating
        // ((Activity) context).finish();

      } catch (android.content.ActivityNotFoundException anfe) {
        Toast.makeText(mActivityContext, "This feature is not available in your device.",
            Toast.LENGTH_LONG)
            .show();
      } catch (Exception e) {
        Toast.makeText(mActivityContext,
            "Currently we are facing some problem for this feature, please try again later",
            Toast.LENGTH_LONG).show();
      }
    }
  }

  /**
   * Function which gives the UserAgent to be used as per NewProtocol of
   * UserAgent tracking
   *
   * @return : useragent to use
   */
  public String getUserAgentToUse() {
    return mUserAgentToUse;
  }

  enum CONTENT_PROCESS_STATE {
    PROCESSING, COMPLETE, INVALID
  }

  // meant for context switching, scenarios
  public enum ContainerState {
    ACTIVITY_CONTEXT_INVALID, ACTIVITY_CONTEXT_ATTACHED, ACTIVITY_CONTEXT_INTERMEDIATE
  }

  // meant for knowing the status on AdTimeSlot
  public enum AdRefreshState {
    AD_SERVING, AD_EXPIRED, AD_INVALID
  }

  private class SetBackgroundResourceAction implements Runnable {
    private Integer backgroundResource;

    public SetBackgroundResourceAction(Integer backgroundResource) {
      this.backgroundResource = backgroundResource;
    }

    @Override
    public void run() {
      try {
        if (backgroundResource != null) {
          self.setBackgroundResource(backgroundResource);
          self.setBackgroundColor(0);
        }
      } catch (Exception e) {
        adLog.log(MASTAdLog.LOG_LEVEL_ERROR, "SetBackgroundResourceAction", e.getMessage());
      }
    }
  }
}
