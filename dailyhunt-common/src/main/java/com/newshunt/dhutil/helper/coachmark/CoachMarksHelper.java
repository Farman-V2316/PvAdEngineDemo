/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.coachmark;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.FontType;
import com.newshunt.common.helper.font.FontWeight;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.preference.SavedPreference;
import com.newshunt.dataentity.common.asset.Format;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.preference.CoachMarksPreference;
import com.newshunt.news.model.usecase.MediatorUsecase;
import com.newshunt.news.model.usecase.Result0;

import java.util.concurrent.TimeUnit;

/**
 * Provides application to show coach-marks on different screens.
 *
 * @author shreyas.desai
 */
public class CoachMarksHelper {
  public static int feedToDetailLandingCounter = 0;
  public static int notificationToDetailLandingCounter = 0;
  public static int totalShownForFeedToDetail = 0;
  public static int totalShownForNotificationToDetail = 0;
  public static final int STICKY_MAX_COUNT = 4;
  private static boolean isDotVisible;
  public static String displayText = CommonUtils.getString(com.newshunt.common.util.R.string.swipe_coach_mark);
  public static LiveData<Result0<CommunicationEventsResponse>>  communicationLiveData;
  public static MediatorUsecase<Object, CommunicationEventsResponse> communicationEventUseCase;
  public static int  maxCoachMarksShow = 3;
  public static int totalSpvCount = 24;
  public static int  totalTimeElapsed = 15;
  public static long timeSpendOnDetail = 24000;
  public static int minimumFeedToDetail = 3;
  private static Dialog dialog;
  public static Handler handler = new Handler(Looper.getMainLooper());
  private CoachMarksHelper() {

  }

  public static void cleanUpValues(){
    feedToDetailLandingCounter=0;
    notificationToDetailLandingCounter = 0;
  }
  public static void showSwipeCoachMark(Context context, boolean isFromNotification) {
    dismissDialog();
    dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
    dialog.setContentView(R.layout.overlay_swipe);

    RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.overlay_layout);
    layout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        dialog.dismiss();
      }
    });

    TextView swipeCoachText = dialog.findViewById(R.id.swipe_text);
    FontHelper.setSpannableTextWithFont(swipeCoachText, swipeCoachText.getText().toString(),
        FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);

    swipeCoachText.setText(displayText);

    dialog.show();
    saveAsShown(CoachMarksPreference.PREFERENCE_COACH_MARKS_NEWS_DETAIL);
    if (isFromNotification) {
      saveTimeFrNotification();
      totalShownForNotificationToDetail++;
    } else {
      saveTimeFrFeedCard();
      totalShownForFeedToDetail++;
    }
  }

  public static void dismissDialog(){
    if (dialog != null){
      dialog.dismiss();
      dialog = null;
    }
  }

  public static boolean canShowCoachMark(Format format, boolean isNewsDetailNotSwipable, Format formatPositionPlusOne) {
    if (format != Format.VIDEO && !isNewsDetailNotSwipable && formatPositionPlusOne != Format.AD) {
      if (isFirstRun(CoachMarksPreference.PREFERENCE_COACH_MARKS_NEWS_DETAIL) ||
              (feedToDetailLandingCounter > minimumFeedToDetail && spvCount(CoachMarksPreference.PREFERENCE_SPV_COUNT) > totalSpvCount && timeElapsedForLastCoachMarkFromFeed() > totalTimeElapsed && totalShownForFeedToDetail < maxCoachMarksShow) ||
              (notificationToDetailLandingCounter > minimumFeedToDetail && spvCount(CoachMarksPreference.PREFERENCE_SPV_COUNT) > totalSpvCount && timeElapsedForLastCoachMarkFromNotification() > totalTimeElapsed && totalShownForNotificationToDetail < maxCoachMarksShow)) {
        return true;
      }
    }
    return false;
  }

  public static SavedPreference getPreferenceKeyForTooltip(int position) {
    switch (position) {
      case 1:
        return CoachMarksPreference.PREFERENCE_TOOLTIP_COACH_MARKS_TOPICS;

      case 2:
        return CoachMarksPreference.PREFERENCE_TOOLTIP_COACH_MARKS_SOURCES;

      case 3:
        return CoachMarksPreference.PREFERENCE_TOOLTIP_COACH_MARKS_LANGUAGES;
    }
    return CoachMarksPreference.EMPTY;
  }

  public static int getStickyCount() {
    return PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_STICKY_COUNT_KEY, 0);
  }

  public static void setStickyCount(int count) {
    PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_STICKY_COUNT_KEY, count);
  }

  public static int incrementStickyCount() {
    int stickyCount = getStickyCount();
    stickyCount++;
    if (stickyCount > CoachMarksHelper.STICKY_MAX_COUNT) {
      stickyCount = CoachMarksHelper.STICKY_MAX_COUNT;
    }
    setStickyCount(stickyCount);
    return stickyCount;
  }

  public static boolean isIsDotVisible() {
    return isDotVisible;
  }

  public static void setIsDotVisible(boolean isDotVisible) {
    CoachMarksHelper.isDotVisible = isDotVisible;
  }


  public static void updateSpvCount(SavedPreference savedPreference) {
    PreferenceManager.savePreference(savedPreference, spvCount(savedPreference) + 1);
  }

  public static int spvCount(SavedPreference savedPreference) {
    return PreferenceManager.getPreference(savedPreference, 0);
  }

  public static void saveAsShown(SavedPreference savedPreference) {
    PreferenceManager.savePreference(savedPreference, java.lang.Boolean.FALSE);
  }

  public static boolean isFirstRun(SavedPreference savedPreference) {
    return PreferenceManager.getPreference(savedPreference, java.lang.Boolean.TRUE);
  }

  public static int toDays(long time){
    return (int)TimeUnit.MILLISECONDS.toDays(time);
  }

  public static int timeElapsedForLastCoachMarkFromFeed(){
    return toDays(System.currentTimeMillis() - PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_LAST_COACH_MAKE_TIME_ELAPSED_FROM_FEED , 0L));
  }

  public static int timeElapsedForLastCoachMarkFromNotification(){
    return toDays(System.currentTimeMillis() - PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_LAST_COACH_MARK_TIME_ELAPSED_FROM_NOTIFICATION , 0L));
  }

  public static void resetSpvCount(){
    PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_SPV_COUNT, 0);
  }

  public static void saveTimeFrNotification(){
    PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_LAST_COACH_MARK_TIME_ELAPSED_FROM_NOTIFICATION, System.currentTimeMillis());
  }

  public static void saveTimeFrFeedCard(){
    PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_LAST_COACH_MAKE_TIME_ELAPSED_FROM_FEED, System.currentTimeMillis());
  }

}
