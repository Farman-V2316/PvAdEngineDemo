/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.builder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.exoplayer2.util.LongArray;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.sticky.StickyAudioPlayControlsKt;
import com.newshunt.dataentity.notification.asset.CommentaryState;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.dataentity.notification.asset.CricketScoreAsset;
import com.newshunt.dataentity.notification.asset.TeamAsset;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.R;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.notification.helper.NotificationDefaultChannelHelperKt;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.app.NotificationCompat;

import static com.newshunt.notification.helper.NotificationDefaultChannelHelperKt.createDefaultChannel;


/**
 * Created by anshul on 24/08/17.
 */

public class StickyNotificationLayoutBuilder {

  private static final String TAG = "StickyNotifications";
  private Context context;
  private NotificationLayoutType layoutType;
  private PendingIntent targetIntent, dismissedIntent, refreshIntent, playIntent, stopIntent;
  private StickyNavModel stickyNavModel;
  private Bitmap team1IconBitmap, team2IconBitmap;
  private HashMap<String, Bitmap> brandingcache;

  private int brandingCount;
  private boolean showLiveCommentaryView;

  public StickyNotificationLayoutBuilder(Context context, StickyNavModel stickyNavModel,
                                         NotificationLayoutType layoutType,
                                         PendingIntent targetIntent, PendingIntent refreshIntent,
                                         PendingIntent dismissedIntent,
                                         PendingIntent playIntent,
                                         PendingIntent stopIntent, Integer brandingCount,
                                         Bitmap team1IconBitmap,
                                         Bitmap team2IconBitmap,
                                         boolean showLiveCommentaryView) {
    this.context = context;
    this.stickyNavModel = stickyNavModel;
    this.layoutType = layoutType;
    this.targetIntent = targetIntent;
    this.refreshIntent = refreshIntent;
    this.dismissedIntent = dismissedIntent;
    this.playIntent = playIntent;
    this.stopIntent = stopIntent;
    this.team1IconBitmap = team1IconBitmap;
    this.team2IconBitmap = team2IconBitmap;
    this.brandingCount = brandingCount;
    this.showLiveCommentaryView = showLiveCommentaryView;
  }

  public Notification build(boolean buildHeadsUpNotification, String state) {
    createDefaultChannel();

    switch (layoutType) {

      case NOTIFICATION_TYPE_STICKY_CRICKET:
        return buildNotificationLayoutOfTypeCricketSticky(buildHeadsUpNotification,
            state);

    }

    return null;
  }


  public void setBrandingCache(HashMap<String, Bitmap> hashmap) {
    this.brandingcache = hashmap;
  }


  private Notification buildNotificationLayoutOfTypeCricketSticky(
      boolean enableHeadsUpNotification, String state) {


    CricketNotificationAsset cricketNotificationAsset =
        (CricketNotificationAsset) stickyNavModel.getBaseNotificationAsset();

    CricketDataStreamAsset cricketDataStreamAsset = (CricketDataStreamAsset) stickyNavModel
        .getBaseStreamAsset();

    if (cricketDataStreamAsset == null) {
      cricketDataStreamAsset = new CricketDataStreamAsset();
    }

    Log.d(TAG, "inside buildNotificationLayoutOfTypeCricketSticky:");

    if (stickyNavModel == null || stickyNavModel
        .getBaseInfo() == null || !(stickyNavModel.getBaseNotificationAsset() instanceof
        CricketNotificationAsset)) {
      Logger.d(TAG, "stickyNavModel is null :? " + (stickyNavModel == null));
      if (stickyNavModel != null) {
        Logger.d(TAG,
            "stickyNavModel.baseInfo is null? : " + (stickyNavModel.getBaseInfo() == null));
        Logger.d(TAG,
            "stickymodel.notif asset is instanceof cricket notif asset?: " +
                (stickyNavModel.getBaseNotificationAsset() instanceof CricketNotificationAsset));
      }

      return null;
    }


    RemoteViews remoteViews;

    remoteViews = new RemoteViews(context.getPackageName(), R.layout.sticky_remote_layout);

    RemoteViews expandedViews = new RemoteViews(context.getPackageName(), R.layout
        .cricket_sticky_expanded_layout);

    setRemoteViews(remoteViews, cricketNotificationAsset, cricketDataStreamAsset, state);

    setRemoteViews(expandedViews, cricketNotificationAsset, cricketDataStreamAsset, state);


    String channelId = NotificationDefaultChannelHelperKt.getChannelId(stickyNavModel.getChannelId());
    if(channelId == null){
      Logger.d(TAG, "channel Id is null exiting");
      return null;
    }
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
        channelId);
    builder.setSmallIcon(R.mipmap.app_notification_icon);
    builder.setContentIntent(targetIntent);
    builder.setDeleteIntent(dismissedIntent);
    builder.setCustomBigContentView(expandedViews);
    builder.setOngoing(true);
    builder.setOnlyAlertOnce(true);
    builder.setCustomContentView(remoteViews);
    //application of unique groupId will be controlled by static config
    if(!PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      builder.setGroup(stickyNavModel.getBaseNotificationAsset().getId() + "_" +System.currentTimeMillis());
    }
    builder.setPriority(stickyNavModel.getBaseInfo().getPriority());

    if (team1IconBitmap != null && team2IconBitmap != null) {
      setTeamIcons(remoteViews);
      setTeamIcons(expandedViews);
    }
    return builder.build();
  }

  private void setRemoteViews(RemoteViews remoteViews,
                              CricketNotificationAsset cricketNotificationAsset,
                              CricketDataStreamAsset cricketDataStreamAsset, String state) {


    TeamAsset team1Asset = cricketNotificationAsset.getTeam1();

    TeamAsset team2Asset = cricketNotificationAsset.getTeam2();

    // Set the title of the match.
    String title = cricketNotificationAsset.getTitle();

    if (!cricketDataStreamAsset.isExpired() && showLiveCommentaryView && StickyAudioPlayControlsKt.STICKY_AUDIO_COMMENTARY_ENABLED) {
      remoteViews.setViewVisibility(R.id.commentary_view, View.VISIBLE);
      if (cricketNotificationAsset.getState() == CommentaryState.PLAYING) {
        remoteViews.setOnClickPendingIntent(R.id.commentary_view, stopIntent);
        remoteViews.setImageViewResource(R.id.commentary_view, com.newshunt.dhutil.R.drawable.ic_commentary_stop);
      } else if (cricketNotificationAsset.getState() == CommentaryState.BUFFERING) {
        remoteViews.setOnClickPendingIntent(R.id.commentary_view, stopIntent);
        remoteViews.setImageViewResource(R.id.commentary_view, com.newshunt.dhutil.R.drawable.ic_commentary_buffering);
      } else {
        remoteViews.setOnClickPendingIntent(R.id.commentary_view, playIntent);
        remoteViews.setImageViewResource(R.id.commentary_view, R.drawable.ic_commentry_play);
      }
    } else {
      remoteViews.setViewVisibility(R.id.commentary_view, View.GONE);
    }

    if (cricketDataStreamAsset.isLive() || cricketDataStreamAsset.isFinished()) {
      if (cricketDataStreamAsset.isFinished()) {
        remoteViews.setViewVisibility(R.id.refresh_layout, View.GONE);

      } else {
        title = cricketNotificationAsset.getLiveTitle();
        remoteViews.setViewVisibility(R.id.refresh_layout, View.VISIBLE);
      }
      remoteViews.setViewVisibility(R.id.line1Tv, View.GONE);
      remoteViews.setViewVisibility(R.id.line2Tv, View.GONE);
      remoteViews.setViewVisibility(R.id.matchStateTv, View.VISIBLE);
      if (CommonUtils.isEmpty(state)) {
        state = cricketDataStreamAsset.getState();
        remoteViews.setInt(R.id.refresh_btn, "setBackgroundResource", R.drawable.ic_refresh);
      } else {
        remoteViews.setInt(R.id.refresh_btn, "setBackgroundResource", R.drawable.ic_updating);
      }
      remoteViews.setTextViewText(R.id.matchStateTv, state);
    } else {
      remoteViews.setViewVisibility(R.id.refresh_layout, View.GONE);
      remoteViews.setViewVisibility(R.id.line2Tv, View.VISIBLE);
      remoteViews.setViewVisibility(R.id.line1Tv, View.VISIBLE);
      remoteViews.setTextViewText(R.id.line1Tv, cricketNotificationAsset.getLine1Text());
      remoteViews.setTextViewText(R.id.line2Tv, cricketNotificationAsset.getLine2Text());
      remoteViews.setViewVisibility(R.id.matchStateTv, View.GONE);
    }

    if (!CommonUtils.isEmpty(title)) {
      CharSequence charSequenceTitle = Html.fromHtml(title);
      if (charSequenceTitle != null) {
        remoteViews.setViewVisibility(R.id.titleTv, View.VISIBLE);
        remoteViews.setTextViewText(R.id.titleTv, charSequenceTitle);
      } else {
        remoteViews.setViewVisibility(R.id.titleTv, View.INVISIBLE);
      }
    } else {
      remoteViews.setViewVisibility(R.id.titleTv, View.INVISIBLE);
    }


    if (team1Asset != null && team2Asset != null) {

      //Set the names of the two teams.
      remoteViews.setTextViewText(R.id.leftNameTv, team1Asset.getTeamName());
      remoteViews.setTextViewText(R.id.rightNameTv, team2Asset.getTeamName());
      remoteViews.setTextViewText(R.id.leftTeamPlaceHolderTv, team1Asset.getTeamName());
      remoteViews.setTextViewText(R.id.rightTeamPlaceHolderTv, team2Asset.getTeamName());
    }

    CricketScoreAsset team1FirstInningsScoreAsset = cricketDataStreamAsset
        .getTeam1FirstInningsScore();
    String team1FirstInningsScore = Constants.EMPTY_STRING;
    if (team1FirstInningsScoreAsset != null) {
      team1FirstInningsScore = getTeamRuns(team1FirstInningsScoreAsset);
    }
    remoteViews.setTextViewText(R.id.leftScoreTv1, team1FirstInningsScore);

    boolean isInnings2ScoreAvailable = false;
    CricketScoreAsset team1SecondInningsScoreAsset = cricketDataStreamAsset
        .getTeam1SecondInningsScore();
    if (team1SecondInningsScoreAsset != null) {
      String team1SecondInningsScore = getTeamRuns(team1SecondInningsScoreAsset);
      remoteViews.setViewVisibility(R.id.leftScoreTv2, View.VISIBLE);
      remoteViews.setTextViewText(R.id.leftScoreTv2, team1SecondInningsScore);
      isInnings2ScoreAvailable = true;
    } else {
      remoteViews.setViewVisibility(R.id.leftScoreTv2, View.GONE);
    }

    CricketScoreAsset team2FirstInningsScoreAsset = cricketDataStreamAsset
        .getTeam2FirstInningsScore();
    String team2FirstInningsScore = Constants.EMPTY_STRING;
    if (team2FirstInningsScoreAsset != null) {
      team2FirstInningsScore = getTeamRuns(team2FirstInningsScoreAsset);
    }
    remoteViews.setTextViewText(R.id.rightScoreTv1, team2FirstInningsScore);

    CricketScoreAsset team2SecondInningsScoreAsset = cricketDataStreamAsset
        .getTeam2SecondInningsScore();
    if (team2SecondInningsScoreAsset != null) {
      String team2SecondInningsScore = getTeamRuns(team2SecondInningsScoreAsset);
      remoteViews.setViewVisibility(R.id.rightScoreTv2, View.VISIBLE);
      remoteViews.setTextViewText(R.id.rightScoreTv2, team2SecondInningsScore);
      isInnings2ScoreAvailable = true;
    } else {
      remoteViews.setViewVisibility(R.id.rightScoreTv2, View.GONE);
    }

    // Set the overs for the first team
    Float firstTeamOvers = 0f;
    if (team1SecondInningsScoreAsset != null) {
      firstTeamOvers = team1SecondInningsScoreAsset.getOvers();
    } else if (team1FirstInningsScoreAsset != null) {
      firstTeamOvers = team1FirstInningsScoreAsset.getOvers();
    }

    if (Float.compare(firstTeamOvers, 0f) != 0) {
      remoteViews.setViewVisibility(R.id.leftOversTv, View.VISIBLE);
      remoteViews.setTextViewText(R.id.leftOversTv,
          Constants.OPENING_BRACES + Float.toString(firstTeamOvers) +
              Constants.CLOSING_BRACES);
    } else {
      remoteViews.setViewVisibility(R.id.leftOversTv, View.INVISIBLE);
    }

    //Set the overs for the second  team.
    Float secondTeamOvers = 0f;
    if (team2SecondInningsScoreAsset != null) {
      secondTeamOvers = team2SecondInningsScoreAsset.getOvers();
    } else if (team2FirstInningsScoreAsset != null) {
      secondTeamOvers = team2FirstInningsScoreAsset.getOvers();
    }

    if (Float.compare(secondTeamOvers, 0f) != 0) {
      remoteViews.setViewVisibility(R.id.rightOversTv, View.VISIBLE);
      remoteViews.setTextViewText(R.id.rightOversTv, "(" + Float.toString(secondTeamOvers) + ")");
    } else {
      remoteViews.setViewVisibility(R.id.rightOversTv, View.INVISIBLE);
    }

    ArrayList<String> balls = cricketDataStreamAsset.getBalls();

    remoteViews.removeAllViews(R.id.ll_balls);

    if (!CommonUtils.isEmpty(balls)) {
      int displayballs = 0;

      for (int i = 0; i < balls.size(); i++) {

        if (TextUtils.equals(balls.get(i), "|")) {
          RemoteViews overbreaker =
              new RemoteViews(context.getPackageName(), R.layout.remoteview_overbreaker);
          remoteViews.addView(R.id.ll_balls, overbreaker);
          continue;
        }

        if (displayballs >= 6) {
          break;
        }
        RemoteViews textView =
            new RemoteViews(context.getPackageName(), R.layout.remote_text_view_ball);
        textView.setTextViewText(R.id.ball, balls.get(i));
        setTextViewColor(textView, R.id.ball, balls.get(i));

        if (displayballs == 5) {
          textView.setViewVisibility(R.id.lastball, View.VISIBLE);
        } else {
          textView.setViewVisibility(R.id.lastball, View.INVISIBLE);
        }
        remoteViews.addView(R.id.ll_balls, textView);
        displayballs++;

      }
    }


    //Set 1st innings and over - text size if any team 2nd innings score is available, else default
    if (isInnings2ScoreAvailable) {
      remoteViews.setTextViewTextSize(R.id.leftScoreTv1, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_two_innings_score_text_size));
      remoteViews.setTextViewTextSize(R.id.leftOversTv, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_two_innings_overs_text_size));
      remoteViews.setTextViewTextSize(R.id.rightScoreTv1, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_two_innings_score_text_size));
      remoteViews.setTextViewTextSize(R.id.rightOversTv, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_two_innings_overs_text_size));
    } else {
      remoteViews.setTextViewTextSize(R.id.leftScoreTv1, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_one_inning_score_text_size));
      remoteViews.setTextViewTextSize(R.id.leftOversTv, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_one_inning_overs_text_size));
      remoteViews.setTextViewTextSize(R.id.rightScoreTv1, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_one_inning_score_text_size));
      remoteViews.setTextViewTextSize(R.id.rightOversTv, TypedValue.COMPLEX_UNIT_PX, CommonUtils
          .getDimension(R.dimen.notification_one_inning_overs_text_size));
    }

    remoteViews.setOnClickPendingIntent(R.id.refresh_btn, refreshIntent);
    remoteViews.setOnClickPendingIntent(R.id.cross_btn, dismissedIntent);


    if (!CommonUtils.isEmpty(cricketNotificationAsset.getBranding()) &&
        brandingcache.get(cricketNotificationAsset.getBranding().get(brandingCount)) != null) {
      remoteViews.setImageViewBitmap(R.id.brandingImage,
          brandingcache.get(cricketNotificationAsset.getBranding().get(brandingCount)));
    }


  }

  private void setTextViewColor(RemoteViews remoteViews, int ball, String s) {

    switch (s) {

      case "W":
      case "w":
        remoteViews.setInt(ball, "setBackgroundResource", R.drawable.wicket_background_drawable);
        break;

      case "6":
      case "4":
        remoteViews.setInt(ball, "setBackgroundResource", R.drawable.boundary_background_drawable);
        break;
      default:
        remoteViews.setInt(ball, "setBackgroundResource", R.drawable.ball_background_color);

    }
  }

  private void setTeamIcons(RemoteViews remoteViews) {
    remoteViews.setImageViewBitmap(R.id.left_image, AndroidUtils.getRoundedBitmap
        (team1IconBitmap, CommonUtils.getDimension(R.dimen.notification_flag_width),
            CommonUtils.getDimension(R.dimen.notification_flag_width),
            CommonUtils.getDimension(R.dimen.notification_round_flag_radius)));
    remoteViews.setImageViewBitmap(R.id.right_image, AndroidUtils.getRoundedBitmap
        (team2IconBitmap, CommonUtils.getDimension(R.dimen.notification_flag_width),
            CommonUtils.getDimension(R.dimen.notification_flag_width),
            CommonUtils.getDimension(R.dimen.notification_round_flag_radius)));
    //If team icon are loaded, then show team name below team image
    remoteViews.setViewVisibility(R.id.leftTeamPlaceHolderTv, View.GONE);
    remoteViews.setViewVisibility(R.id.rightTeamPlaceHolderTv, View.GONE);
    remoteViews.setViewVisibility(R.id.leftNameTv, View.VISIBLE);
    remoteViews.setViewVisibility(R.id.rightNameTv, View.VISIBLE);
  }


  private String getTeamRuns(CricketScoreAsset cricketScoreAsset) {
    return cricketScoreAsset.getWickets() == 10 ?
        cricketScoreAsset.getRuns().toString() :
        cricketScoreAsset.getRuns() + Constants.FORWARD_SLASH + cricketScoreAsset.getWickets();
  }

}
