/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.newshunt.common.helper.appupgrade.InAppUpdateAvailability;
import com.newshunt.common.helper.appupgrade.InAppUpdateHelper;
import com.newshunt.common.helper.appupgrade.InAppUpdateHelperProvider;
import com.newshunt.common.helper.appupgrade.UpdateType;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.ApplicationStatus;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.DownloadWebData;
import com.newshunt.common.helper.common.DummyDisposable;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.SetLocaleUtil;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.contentprovider.PreferenceContentProvider;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.sticky.StickyAudioPlayControlInterfaceProvider;
import com.newshunt.common.helper.sticky.StickyAudioPlayControlsKt;
import com.newshunt.common.view.view.DisplayStickAudio;
import com.newshunt.common.view.view.FragmentCallback;
import com.newshunt.dataentity.common.JsPhoneNumber;
import com.newshunt.dataentity.common.model.entity.ConfigurationChangedEvent;
import com.newshunt.dataentity.common.model.entity.LifeCycleEvent;
import com.newshunt.dataentity.common.model.entity.PermissionResult;
import com.newshunt.dataentity.common.model.entity.SettingsChangeEvent;
import com.newshunt.dataentity.dhutil.model.entity.PhoneSelectorInterface;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Base Activity for all other activities
 *
 * @author arun.babu
 */
public class NHBaseActivity extends AppCompatActivity implements DisplayStickAudio, PhoneSelectorInterface {
  protected boolean isMandatoryUpdateSupported = true;
  private boolean isFlexibleUpdateSupported = false;
  public static final int REQ_CODE_UPGRADE = 952;
  private boolean isMandatoryUpdatePromptRequested = false;
  private boolean restartPending = false;

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  private static final String TAG = "NHBaseActivity";

  private final Object webDownload = new Object() {
    @Subscribe
    public void DownloadWebDataReceived(final DownloadWebData downloadWebData) {
      FontHelper.showCustomFontToast(NHBaseActivity.this,
          String.format(NHBaseActivity.this.getString(
              com.newshunt.common.util.R.string.webview_downloading_file_toast),
              downloadWebData.filename), Toast.LENGTH_SHORT);
    }
  };

  private final Object themeChangeListener = new Object() {
    @Subscribe
    public void onThemeChange(final SettingsChangeEvent event) {
      if (event.getChangeType() == SettingsChangeEvent.ChangeType.THEME) {
        onDeviceThemeChanged();
      }
    }
  };

  protected void onDeviceThemeChanged(){
    if(getLifecycle() == null || getLifecycle().getCurrentState() == null) {
      return;
    }
    if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)){
      recreate();
    } else {
      restartPending = true;
    }
  }

  private View stickyAudioFloatingView;
  protected boolean isAttachedToWindow;
  private static final int CREDENTIAL_PICKER_REQUEST = 10077;

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    isAttachedToWindow = true;

    if (doNotShowStickyAudioFloatingWidget()) {
      return;
    }

    if (StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface() != null) {
      StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
          .onActivityResumed(this,
              StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
                  .getAudioCommentaryLiveData()
                  .getValue());
    }
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttachedToWindow = false;
  }

  @Override
  public boolean isAttachedToWindow() {
    return isAttachedToWindow;
  }

  @Override
  public Context getWindowContext() {
    return this;
  }

  @Override
  public View getFloatingView() {
    return stickyAudioFloatingView;
  }

  @Override
  public void setFloatingView(View floatingView) {
    stickyAudioFloatingView = floatingView;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //Set app language based on selected language.
    SetLocaleUtil.updateLanguage();

    // arun.babu : 07 Feb 2015 : 'can Clean RAM' false to ensure RAM is not cleared redundantly
    ApplicationStatus.setCanCleanRAM(false);

    ViewUtils.screenChanged();

    super.onCreate(savedInstanceState);

    getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentCallback(), true);
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(hashCode(), LifeCycleEvent.CREATED));
    BusProvider.getUIBusInstance().register(themeChangeListener);
    observeInAppUpdates();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    noforceDark();
  }

  private void noforceDark() {
    View cv = findViewById(android.R.id.content);
    View rv = null;
    if(cv != null) {
      rv = cv.getRootView();
    }
    if (rv != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rv.setForceDarkAllowed(false);
      }
    }

  }


  @Override
  protected void onPause() {
    BusProvider.getUIBusInstance().unregister(webDownload);
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(hashCode(), LifeCycleEvent.PAUSED));

    if (!doNotShowStickyAudioFloatingWidget() &&
        StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface() != null) {
      StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
          .getAudioCommentaryLiveData()
          .removeObservers(this);
      StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
          .onActivityPaused(this);
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if(restartPending) {
      restartPending = false;
      recreate();
      return;
    }
    BusProvider.getUIBusInstance().register(webDownload);
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(hashCode(), LifeCycleEvent.RESUMED));
    try {
      CookieManager.getInstance().setAcceptCookie(true);
    } catch (Exception e) {
      Logger.caughtException(e);
    }

    if (!doNotShowStickyAudioFloatingWidget() &&
        StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface() != null) {
      //to update the state of the floating window preference on activity resume
      onStickyAudioCommentaryStateChanged(
          StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
              .getAudioCommentaryLiveData()
              .getValue());
      StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
          .getAudioCommentaryLiveData()
          .observe(this, this::onStickyAudioCommentaryStateChanged);
    }
  }

  protected boolean doNotShowStickyAudioFloatingWidget() {
    return !StickyAudioPlayControlsKt.STICKY_AUDIO_COMMENTARY_ENABLED;
  }

  protected void onStickyAudioCommentaryStateChanged(Object data) {
    if (isAttachedToWindow && StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface() != null) {
      StickyAudioPlayControlInterfaceProvider.INSTANCE.getStickyPlayControlInterface()
          .onActivityResumed(this, data);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    persistCookies();
  }

  public static void persistCookies() {
    Observable.fromCallable(AndroidUtils::persistWebCookies)
        .subscribeOn(Schedulers.io())
        .subscribe(new DummyDisposable<Boolean>());
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(SetLocaleUtil.updateResources(newBase));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    BusProvider.getUIBusInstance().post(new PermissionResult(this, permissions));
  }

  @Override
  public void showPhoneNumberDialog() {
    HintRequest hintRequest = new HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build();

    CredentialsOptions options = new CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build();
    CredentialsClient credentialsClient = Credentials.getClient(this, options);
    PendingIntent intent = credentialsClient.getHintPickerIntent(hintRequest);
    try {
      startIntentSenderForResult(intent.getIntentSender(),
              CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0);
    } catch (IntentSender.SendIntentException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CREDENTIAL_PICKER_REQUEST) {
      if (resultCode == RESULT_OK && data != null) {
        Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
        if (credential != null) {
          Logger.d(TAG, "number selected" + credential.getId());
          BusProvider.getUIBusInstance().post(new JsPhoneNumber(credential.getId()));
        } else {
          BusProvider.getUIBusInstance().post(new JsPhoneNumber());
        }
      } else {
        Logger.d(TAG, "No number selected");
        BusProvider.getUIBusInstance().post(new JsPhoneNumber());
      }
    }
    if (requestCode == REQ_CODE_UPGRADE && resultCode == Activity.RESULT_CANCELED &&
            InAppUpdateHelperProvider.INSTANCE.getInAppUpdateHelper() != null) {
        InAppUpdateHelperProvider.INSTANCE.getInAppUpdateHelper().userCancelledUpdate();
    }
  }

  @Override
  protected void onDestroy() {
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(hashCode(), LifeCycleEvent.DESTROYED));
    if (stickyAudioFloatingView != null) {
      WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
      if (windowManager != null) {
        windowManager.removeView(stickyAudioFloatingView);
      }
      stickyAudioFloatingView = null;
    }
    BusProvider.getUIBusInstance().unregister(themeChangeListener);
    super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    BusProvider.getUIBusInstance().post(new ConfigurationChangedEvent());
  }

  public void setFlexibleUpdateSupported(boolean flexibleUpdateSupported) {
    isFlexibleUpdateSupported = flexibleUpdateSupported;
  }

  private void observeInAppUpdates() {
    if (InAppUpdateHelperProvider.INSTANCE.getInAppUpdateHelper() != null) {
      InAppUpdateHelper updateHelper = InAppUpdateHelperProvider.INSTANCE.getInAppUpdateHelper();
      updateHelper.getInAppUpdateAvailability()
          .observe(this, inAppUpdateAvailability -> {
            if (inAppUpdateAvailability == InAppUpdateAvailability.UPDATE_IN_PROGRESS) {
              updateHelper.continueUpdate(this, REQ_CODE_UPGRADE);
            } else if (inAppUpdateAvailability == InAppUpdateAvailability.MANDATORY_UPDATE_AVAILABLE &&
                isMandatoryUpdateSupported) {
              if (!isMandatoryUpdatePromptRequested) {
                //Avoid requesting mandatory prompt multiple times in same activity
                isMandatoryUpdatePromptRequested = true;
                updateHelper.startUpdate(this, REQ_CODE_UPGRADE, UpdateType.MANDATORY_UPDATE);
              }
            } else if (inAppUpdateAvailability == InAppUpdateAvailability.FLEXIBLE_UPDATE_AVAILABLE &&
                isFlexibleUpdateSupported) {
              updateHelper.startUpdate(this, REQ_CODE_UPGRADE, UpdateType.FLEXIBLE_UPDATE);
            }
          });
    }
  }
}
