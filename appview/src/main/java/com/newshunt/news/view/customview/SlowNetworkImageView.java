/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.customview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.newshunt.appview.R;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.ImageSaveFailureReason;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.listener.ImageSaveCallBack;
import com.newshunt.common.view.customview.NHImageView;
import com.newshunt.common.view.customview.NHTouchImageView;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.PermissionResult;
import com.newshunt.dataentity.common.view.customview.FIT_TYPE;
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider;
import com.newshunt.news.helper.ImageUtil;
import com.newshunt.news.helper.handler.GalleryPhotoViewStatusHelper;
import com.newshunt.permissionhelper.PermissionAdapter;
import com.newshunt.permissionhelper.PermissionHelper;
import com.newshunt.permissionhelper.utilities.Permission;
import com.newshunt.sdk.network.Priority;
import com.newshunt.sdk.network.image.Image;
import com.newshunt.sdk.network.image.OnImageLoadListener;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

/**
 * Image ViewGroup to display a photo in a slow network mode with a blurred image
 * <p/>
 * This class will take care of following cases
 * <p/>
 * First this class will attempt to load the image from disk cache if present
 * else goes through the below conditions
 * <p/>
 * Lite Mode ON:
 * start loading blur photo with a view button on front
 * 1. if loading blur photo fails, still show the View button
 * 2. on click of view button, start loading regular photo
 * 3. on succes of regular photo download, dismiss blur photo
 * 4. on failure of regular photo download, dismiss blur photo and give callback to show error
 * and on retry, it starts loading regular photo
 * <p/>
 * LiteMode OFF:
 * start loading blur photo and regular photo directly
 * 1. On failure of blur photo download, do nothing
 * 2. On failure of regular photo download, give callback and implementor can show error message
 * and allow user to retry downloading regular photo again
 * <p/>
 * Good and fast network:
 * 1. Start loading regular photo without starting download for blur photo
 *
 * @author santhosh.kc
 */
public class SlowNetworkImageView extends RelativeLayout implements View.OnClickListener {

  private NHImageView blurredPhotoView;
  private NHImageView photoView;
  private NHTextView viewButton;
  private Priority blurredPhotoPriority = Priority.PRIORITY_NORMAL;
  private Priority photoPriority = Priority.PRIORITY_NORMAL;
  private String slowImageUrl;
  private String imageUrl;
  private boolean applyImagePlaceHolder;
  private Callback callback;
  private boolean isPinchZoomEnabled = false;
  private PermissionHelper storagePermissionHelper;

  /**
   * constructor
   *
   * @param context - context
   */
  public SlowNetworkImageView(Context context) {
    super(context);
  }

  /**
   * constructor
   *
   * @param context - context
   * @param attrs   - attributeSet
   */
  public SlowNetworkImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  /**
   * constructor
   *
   * @param context      - context
   * @param attrs        - attributeset
   * @param defStyleAttr - defStyleAttr
   */
  public SlowNetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  /*
   * Inflate the view content called only during construction of this view
   */
  private void init(Context context, AttributeSet attrs) {
    if (attrs != null) {
      TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable
          .SlowNetworkImageView);
      try {
        isPinchZoomEnabled = typeArray.getBoolean(R.styleable.SlowNetworkImageView_enablePinchZoom,
            isPinchZoomEnabled);
      } finally {
        typeArray.recycle();
      }
    }
    inflate(getContext(), !isPinchZoomEnabled ? R.layout.layout_slow_network_image_view :
        R.layout.news_detail_layout_slow_network_touch_image_view, this);
    blurredPhotoView = (NHImageView) findViewById(R.id.blurred_photo);
    photoView = (NHImageView) findViewById(R.id.photo);
    viewButton = findViewById(R.id.view_in_lite_mode_text);
    viewButton.setText(CommonUtils.getString(R.string.view_photo_in_lite_mode_message));
    viewButton.setOnClickListener(this);
  }

  /**
   * setting this view, this function also take care of cancelling any previous request in case
   * where this view will be used in recycler view
   *
   * @param slowImageUrl             - slow image location
   * @param imageUrl                 - regular image location
   * @param callback                 - call back to user on success/failure of download
   * @param blurredPhotoScaleFitType - scaletype of blur photo
   * @param photoScaleFitType        - scaletype of regular photo
   * @param blurredPhotoPriority     - blur photo download priority
   * @param photoPriority            - regular photo download priority
   */
  public void startLoading(String slowImageUrl, String imageUrl, final Callback callback,
                           FIT_TYPE blurredPhotoScaleFitType,
                           FIT_TYPE photoScaleFitType,
                           Priority blurredPhotoPriority, Priority photoPriority, boolean
                               applyImagePlaceHolder, boolean enableClick) {
    cancelLoading();
    blurredPhotoView.setImageDrawable(null);
    photoView.setImageDrawable(null);
    this.slowImageUrl = slowImageUrl;
    this.imageUrl = imageUrl;
    this.applyImagePlaceHolder = applyImagePlaceHolder;
    this.callback = callback;
    if (blurredPhotoScaleFitType != null) {
      blurredPhotoView.setFitType(blurredPhotoScaleFitType);
    }
    if (photoScaleFitType != null) {
      photoView.setFitType(photoScaleFitType);
    }
    if (blurredPhotoPriority != null) {
      this.blurredPhotoPriority = blurredPhotoPriority;
    }

    if (photoPriority != null) {
      this.photoPriority = photoPriority;
    }

    if (enableClick) {
      photoView.setOnClickListener(this);
    }
    loadPhotoFromCache();
  }

  @Override
  public void onClick(View viewClicked) {
    if (viewClicked.getId() == R.id.view_in_lite_mode_text) {
      viewButton.setVisibility(View.GONE);
      loadPhoto(false, true);
      GalleryPhotoViewStatusHelper.setPhotoViewAttempted(imageUrl);
    } else if (viewClicked.getId() == R.id.photo) {
      if (callback != null) {
        callback.onPhotoTouch(this, viewButton.getVisibility() == View.GONE);
      }
    }
  }


  /* ---------------------- Loading from Cache Functions ---------------------------------------/

  /**
   * Function to load photo
   * based on following conditions
   * if image is present in the cache, then directly the regular photo view with image
   * else if lite mode ON, starts loading only blur photo
   * else if lite mode off and slow network, start loading both blur and regular photos
   * else if lite mode off and good network, starts loading only regular photo
   */
  private void loadPhotoFromCache() {
    viewButton.setVisibility(View.GONE);
    if (imageUrl != null) {
      Image.Loader offlineImageLoader = Image.load(imageUrl)
          .offline(true)
          .priority(photoPriority);
      if (applyImagePlaceHolder) {
        offlineImageLoader.placeHolder(R.color.empty_image_color);
      }
      offlineImageLoader.into(photoView, new OfflineLoadListener(this, imageUrl),
          ImageView.ScaleType.MATRIX);
    }

  }

  /*
   * This class listens to loading image from cache. This class is static and holds weak
   * reference to slowNetworkImageView to avoid memory leak
   */
  private static class OfflineLoadListener implements OnImageLoadListener {

    private final WeakReference<SlowNetworkImageView> slowNetworkImageViewWeakReference;
    private final String imageLoc;

    public OfflineLoadListener(SlowNetworkImageView slowNetworkImageView, String imgLoc) {
      slowNetworkImageViewWeakReference = new WeakReference<>(slowNetworkImageView);
      imageLoc = imgLoc;
    }

    @Override
    public void onSuccess(Object resource) {
      // This is the best case of all, image is present in the cache, and give success callback
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(imageLoc, slowNetworkImageView.imageUrl)) {
          return;
        }
        Callback viewCallback = slowNetworkImageView.callback;
        if (viewCallback != null) {
          viewCallback.onPhotoDownloadSuccess(slowNetworkImageView);
        }
        GalleryPhotoViewStatusHelper.onPhotoDownloaded(slowNetworkImageView.imageUrl);
      }
    }

    @Override
    public void onError() {
      // On Error, it means image is not present in cache, so load blur or/and regular photos based
      // on network conditions (lite mode, slow network etc)
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(imageLoc, slowNetworkImageView.imageUrl)) {
          return;
        }
        new Handler(Looper.getMainLooper()).post(
            () -> slowNetworkImageView.loadPhotoBasedOnNetwork());
      }
    }
  }


    /* ------------------ reset zoom Functions -------------------------- */

  /**
   * Reset zoom and translation to initial state.
   */
  public void resetZoom() {
    if (blurredPhotoView != null && photoView != null && blurredPhotoView instanceof
        NHTouchImageView && photoView instanceof NHTouchImageView && isPinchZoomEnabled) {
      ((NHTouchImageView) blurredPhotoView).resetZoom();
      ((NHTouchImageView) photoView).resetZoom();
    }
  }

  /* ---------------------- Loading based on network conditions ------------------------------- */

  /**
   * Function to load photo
   * based on following conditions
   * if lite mode ON, starts loading only blur photo
   * else if lite mode off and slow network, start loading both blur and regular photos
   * else if lite mode off and good network, starts loading only regular photo
   */
  public void loadPhotoBasedOnNetwork() {
    boolean downloadedAttempted =
        GalleryPhotoViewStatusHelper.isPhotoAttemptedToBeDownloaded(imageUrl);
    if (downloadedAttempted && !CommonUtils.isNetworkAvailable(getContext())) {
      if (callback != null) {
        callback.onPhotoDownloadFailure(this);
      }
      return;
    }
    setUpViewsForSlowConnection(downloadedAttempted);
    boolean loadBlurPhoto = !ConnectionInfoHelper.isConnectionFast(getContext());
    loadPhoto(loadBlurPhoto, true);
  }

  private void setUpViewsForSlowConnection(boolean downloadedAttempted) {
    if (!ConnectionInfoHelper.isConnectionFast(getContext())) {
      viewButton.setVisibility(View.GONE);
      blurredPhotoView.setVisibility(View.VISIBLE);
    } else {
      viewButton.setVisibility(View.GONE);
      blurredPhotoView.setVisibility(View.GONE);
    }
  }

  /**
   * function to retry downloading regular photo again
   */
  public void retryLoadingPhotoOnError() {
    loadPhoto(false, true);
  }

  private void loadPhoto(boolean loadBlurPhoto, boolean loadRegularPhoto) {
    String imgLocation = imageUrl;
    if (loadBlurPhoto && slowImageUrl != null) {
      blurredPhotoView.setVisibility(View.VISIBLE);
      Image.Loader imageLoader = Image.load(slowImageUrl).priority(blurredPhotoPriority);
      if (applyImagePlaceHolder) {
        imageLoader.placeHolder(R.color.empty_image_color);
      }
      imageLoader.into(blurredPhotoView, new BlurredPhotoLoadListener(this, slowImageUrl),
          ImageView.ScaleType.MATRIX);
    }

    //no need to change visiblity of regular photo as it always visible
    if (loadRegularPhoto && imgLocation != null) {
      Image.Loader regularImageLoader = Image.load(imgLocation).priority(photoPriority);
      if (applyImagePlaceHolder) {
        regularImageLoader.placeHolder(R.color.empty_image_color);
      }
      regularImageLoader.into(photoView, new PhotoLoadListener(this, imgLocation),
          ImageView.ScaleType.MATRIX);
    }
  }

  /*
 * Using a static class because, Image.into() function holds this listener object as final
 * in static context, so to avoid memory leak , having this listener as static class
 */
  private static class BlurredPhotoLoadListener implements OnImageLoadListener {
    private final WeakReference<SlowNetworkImageView> slowNetworkImageViewWeakReference;
    private final String slowImageLoc;

    public BlurredPhotoLoadListener(SlowNetworkImageView slowNetworkImageView,
                                    String slowImageLocation) {
      slowNetworkImageViewWeakReference = new WeakReference<>(slowNetworkImageView);
      slowImageLoc = slowImageLocation;
    }

    @Override
    public void onSuccess(Object resource) {
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(slowImageLoc, slowNetworkImageView.slowImageUrl)) {
          //If photo corresponding to this request is scrolled out and by that time, we get
          // callback we have to discard..
          return;
        }
        if (slowNetworkImageView.blurredPhotoView.getVisibility() != View.VISIBLE) {
          slowNetworkImageView.blurredPhotoView.setVisibility(View.VISIBLE);
        }
      }
    }

    @Override
    public void onError() {
      //on failure, make the visiblity of blurphoto view to GONE
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(slowImageLoc, slowNetworkImageView.slowImageUrl)) {
          //If photo corresponding to this request is scrolled out and by that time, we get
          // callback we have to discard..
          return;
        }
        slowNetworkImageView.blurredPhotoView.setVisibility(View.GONE);
      }
    }
  }

  /*
   * Using a static class because, Image.into() function holds this listener object as final
   * in static context, so to avoid memory leak , having this listener as static class
   */
  private static class PhotoLoadListener implements OnImageLoadListener {
    private final WeakReference<SlowNetworkImageView> slowNetworkImageViewWeakReference;
    private final String imageLoc;

    public PhotoLoadListener(SlowNetworkImageView slowNetworkImageView, String imageLocation) {
      slowNetworkImageViewWeakReference = new WeakReference<>(slowNetworkImageView);
      imageLoc = imageLocation;
    }

    @Override
    public void onSuccess(Object resource) {
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(imageLoc, slowNetworkImageView.imageUrl)) {
          //If photo corresponding to this request is scrolled out and by that time, we get
          // callback we have to discard..
          return;
        }
        ImageView blurredPhoto = slowNetworkImageView.blurredPhotoView;
        Image.cancelRequest(blurredPhoto);
        blurredPhoto.setVisibility(View.GONE);

        if (slowNetworkImageView.callback != null) {
          slowNetworkImageView.callback.onPhotoDownloadSuccess(slowNetworkImageView);
        }
        GalleryPhotoViewStatusHelper.onPhotoDownloaded(imageLoc);
      }
    }

    @Override
    public void onError() {
      if (CommonUtils.isWeakReferencedObjectAvailable(slowNetworkImageViewWeakReference)) {
        SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakReference.get();
        if (!CommonUtils.equals(imageLoc, slowNetworkImageView.imageUrl)) {
          //If photo corresponding to this request is scrolled out and by that time, we get
          // callback we have to discard..
          return;
        }

        slowNetworkImageView.blurredPhotoView.setVisibility(View.GONE);
        //on error, give callback so that implementor may show error
        if (slowNetworkImageView.callback != null) {
          slowNetworkImageView.callback.onPhotoDownloadFailure(slowNetworkImageView);
        }
      }
    }
  }

  /* ------------------ cancelling loading Functions -------------------------- */

  /**
   * Function to cancel loading of the photos. Maybe called on destroy of activity / fragment
   * also, changes the status of the image views to NOT_LOADED
   */
  public void cancelLoading() {
    Image.cancelRequest(blurredPhotoView);
    Image.cancelRequest(photoView);
  }

  /* ----------------------- Saving photo to storage functions ------------------------ */

  /**
   * Function to save the photo to storage
   *
   * @param activity - hosting activity
   * @param storyId
   */
  public void savePhoto(Activity activity, PageReferrer referrer, String storyId) {
    PermissionAdapter adapter =
        new PermissionAdapter(101, activity, new DefaultRationaleProvider()) {
          @Override
          public List<Permission> getPermissions() {
            return Arrays.asList(Permission.WRITE_EXTERNAL_STORAGE);
          }

          @Override
          public void onPermissionResult(@NonNull List<Permission> grantedPermissions,
                                         @NonNull List<Permission> deniedPermissions,
                                         @NonNull List<Permission> blockedPermissions) {

            if (!deniedPermissions.isEmpty() || !blockedPermissions.isEmpty()) {
              return;
            }

            Image.load(imageUrl, true).into(
                ImageUtil.getSaveImageTarget(activity, imageUrl, storyId,
                    new ImageSaveCallBackImpl(callback, SlowNetworkImageView.this)));
          }


          @Override
          public boolean shouldShowRationale() {
            return false;
          }

          @Subscribe
          public void onPermissionResult(PermissionResult permissionResult) {
            onPermissionResultListener(permissionResult.activity,
                permissionResult.permissions);
            BusProvider.getUIBusInstance().unregister(this);
          }

        };
    storagePermissionHelper = new PermissionHelper(adapter);
    storagePermissionHelper.setReferrer(referrer);
    BusProvider.getUIBusInstance().register(adapter);
    storagePermissionHelper.requestPermissions();
  }

  public void onPermissionResultListener(Activity activity, String[] stringPermissions) {
    if (storagePermissionHelper != null) {
      storagePermissionHelper.handlePermissionCallback(activity, stringPermissions);
    }
  }

  private static class ImageSaveCallBackImpl implements ImageSaveCallBack {

    private WeakReference<Callback> callbackWeakReference;
    private WeakReference<SlowNetworkImageView> slowNetworkImageViewWeakRef;

    public ImageSaveCallBackImpl(Callback callBack, SlowNetworkImageView slowNetworkImageView) {
      if (callBack != null && slowNetworkImageView != null) {
        callbackWeakReference = new WeakReference<>(callBack);
        slowNetworkImageViewWeakRef = new WeakReference<SlowNetworkImageView>(slowNetworkImageView);
      }
    }

    @Override
    public void onImageSaveSuccess() {
      SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakRef.get();
      Callback callback = callbackWeakReference.get();
      if (slowNetworkImageView != null && callback != null) {
        callbackWeakReference.get().onPhotoSaveSuccess(slowNetworkImageView);
      }
    }

    @Override
    public void onImageSaveFailure(ImageSaveFailureReason reason) {
      SlowNetworkImageView slowNetworkImageView = slowNetworkImageViewWeakRef.get();
      Callback callback = callbackWeakReference.get();
      if (slowNetworkImageView != null && callback != null) {
        callbackWeakReference.get().onPhotoSaveFailure(slowNetworkImageView);
      }
    }
  }

  /* ------------------------ Callback to claass having this view ------------------------- */

  /**
   * callback interface
   */
  public interface Callback {
    /**
     * callback on regular photo download succes
     *
     * @param slowNetworkImageView - slowNetworkImageView
     */
    void onPhotoDownloadSuccess(SlowNetworkImageView slowNetworkImageView);

    /**
     * callback on regular photo download failure
     *
     * @param slowNetworkImageView - slowNetworkImageView
     */
    void onPhotoDownloadFailure(SlowNetworkImageView slowNetworkImageView);

    /**
     * callback on regular photo download success
     *
     * @param slowNetworkImageView - slowNetworkImageView
     */
    void onPhotoSaveSuccess(SlowNetworkImageView slowNetworkImageView);

    /**
     * callback on regular photo download failure
     *
     * @param slowNetworkImageView - slowNetworkImageView
     */
    void onPhotoSaveFailure(SlowNetworkImageView slowNetworkImageView);

    /**
     * callback on photo view clicked
     *
     * @param slowNetworkImageView  - slowNetworkImageView
     * @param regularPhotoRequested - true if photo is clicked and request to downloading regular
     *                              photo made
     */
    void onPhotoTouch(SlowNetworkImageView slowNetworkImageView, boolean regularPhotoRequested);

  }
}
