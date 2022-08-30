/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

import com.dailyhunt.tv.players.utils.TelephonyUtil;
import com.newshunt.appview.common.helper.UserActionHelper;
import com.newshunt.appview.common.video.utils.DHVideoUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.customview.NHBaseActivity;
import com.newshunt.common.view.view.UniqueIdHelper;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dhutil.helper.AppSettingsProvider;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.newshunt.helper.player.PlaySettingsChangedEvent;
import com.newshunt.helper.player.PlayerControlHelper;
import com.newshunt.news.helper.PageViewStore;
import com.newshunt.news.view.entity.ActivityKillerEvent;
import com.newshunt.notification.sqlite.NotificationDB;
import com.squareup.otto.Subscribe;

/**
 * Base class for all News section activities.
 * <p/>
 * Handles the no connectivity error here using an anonymous Object.
 * This workaround is required because @Subscribe works only on the main class and not the parent
 * classes. This workaround was suggested at :- https://github.com/square/otto/issues/26
 *
 * @author maruti.borker
 */
public abstract class NewsBaseActivity extends NHBaseActivity {
  private static final String ACTIVITY_ID = "ACTIVITY_ID";
  private int activityId;
  private boolean uiBusRegistered;
  private int systemUIVisibilityFlag = 0;
  private TelephonyUtil telephonyUtil;
  private Boolean isInDetailView = false;

  private final Object activityKiller = new Object() {
    @Subscribe
    public void onSelfDestructionEventReceived(final ActivityKillerEvent event){
      //User has landed on a newer instance. Kill this one, not needed anymore.
      if (event.getNewActivityId() != getActivityId()) {
        finish();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Setting up day or night Theme
    int themeID = ThemeUtils.getPreferredTheme().getThemeId();
    setTheme(themeID);

    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      activityId = savedInstanceState.getInt(ACTIVITY_ID);
    } else {
      activityId = UniqueIdHelper.getInstance().generateUniqueId();
    }

    telephonyUtil = new TelephonyUtil(CommonUtils.getApplication(), activityId);
    PreferenceManager.savePreference(GenericAppStatePreference.APP_CURRENT_TIME,
        System.currentTimeMillis());

    if (getIntent() != null) {
      if (getIntent().getBooleanExtra(Constants.FLAG_STICKY_NOTIFICATION_LANDING, false)) {
        //User has clicked on sticky notification, send an event to other landing pages in our stack
        BusProvider.getUIBusInstance().post(new ActivityKillerEvent(getActivityId()));
        BusProvider.getUIBusInstance().register(activityKiller);
        uiBusRegistered = true;
      }
    }

    PageViewStore.init();
  }

  public int getActivityId() {
    return activityId;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    try {
      super.onSaveInstanceState(outState);
      outState.putInt(ACTIVITY_ID, activityId);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    AppSectionsProvider.INSTANCE.getAppSectionsObserver()
        .observe(this, appSectionsResponse -> setAppSectionInfo());
    CommonUtils.runInBackground(() -> {
      int count = NotificationDB.instance().getNotificationDao().getUnseenNotificationCount();
      AppSettingsProvider.INSTANCE.getNotificationLiveData().postValue(count > 0);
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    telephonyUtil.registerCallListener();
  }

  @Override
  protected void onStop() {
    super.onStop();
    telephonyUtil.unregisterCallListener();
  }

  @Override
  protected void onDestroy() {
    if (uiBusRegistered) {
      BusProvider.getUIBusInstance().unregister(activityKiller);
      uiBusRegistered = false;
    }
    super.onDestroy();
  }

  protected void setAppSectionInfo() {
    UserAppSection prevNewsAppSection = AppSectionsProvider.INSTANCE
        .getAnyUserAppSectionOfType(AppSection.NEWS);
    if (prevNewsAppSection == null) {
      return;
    }
    AppUserPreferenceUtils.setAppSectionSelected(prevNewsAppSection);
  }

  /**
   * Used in case of video fullscreen
   *
   * @param isFullScreen
   */
  public void toggleUIForFullScreen(boolean isFullScreen) {
    if (isFullScreen) {
      systemUIVisibilityFlag = DHVideoUtils.hideSystemUI(this);
    } else {
      DHVideoUtils.showSystemUI(this, systemUIVisibilityFlag);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN: {
          if (volume == 1) {
            onDeviceVolumeChanged(true);
          }
        }
        break;
        case KeyEvent.KEYCODE_VOLUME_UP: {
          onDeviceVolumeChanged(false);
        }
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onPause() {
    super.onPause();
    UserActionHelper.INSTANCE.getUserActionLiveData().setValue(NhAnalyticsUserAction.MINIMIZE);
  }

  private void onDeviceVolumeChanged(Boolean isMute) {
    //Unmute the video if playing in detail
    if (!isMute && isInDetailView) {
      BusProvider.getUIBusInstance()
          .post(new PlaySettingsChangedEvent(isMute, Constants.EMPTY_STRING));
      return;
    }
    //Unmute the video if playing in List
    if (isMute == PlayerControlHelper.INSTANCE.isListMuteMode()) {
      return;
    }
    BusProvider.getUIBusInstance()
        .post(new PlaySettingsChangedEvent(isMute, Constants.EMPTY_STRING));
  }

  public void setIsInDetailView(Boolean flag) {
    isInDetailView = flag;
  }

}
