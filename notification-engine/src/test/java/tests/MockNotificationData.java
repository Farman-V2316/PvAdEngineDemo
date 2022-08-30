/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package tests;

import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.dataentity.notification.asset.TeamAsset;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.StickyNavModel;

/**
 * Created by anshul on 28/08/17.
 */

public class MockNotificationData {

  public static StickyNavModel getStickyNavModel(DeeplinkModel deeplinkModel) {

    StickyNavModel stickyNavModel = new StickyNavModel();
    stickyNavModel.setStickyType("cricket");


    CricketNotificationAsset asset = new CricketNotificationAsset();
    asset.setType("cricket");
    asset.setTitle("5th OD");
    asset.setLiveTitle(".LIVE | 5th ODI");
    asset.setLine1Text("6:15 PM");
    asset.setLine2Text("India won the toss and elected to bat");

    TeamAsset team1Asset = new TeamAsset();
    team1Asset.setTeamName("IND");
    team1Asset.setTeamIcon("http://newshunt.net.in/fetchdata2/groups/images/1/icon_xxhdpi.png");
    TeamAsset team2Asset = new TeamAsset();
    team2Asset.setTeamIcon("http://newshunt.net.in/fetchdata2/groups/images/12/icon_xxhdpi.png");
    team2Asset.setTeamName("BAN");

    asset.setTeam1(team1Asset);
    asset.setTeam2(team2Asset);
    asset.setStreamUrl("http://stage-api-news.dailyhunt.in/api/v2/streams/cricket?matchId=123&version=1");
    stickyNavModel.setBaseNotificationAsset(asset);

    if (deeplinkModel != null) {
      stickyNavModel.setDeeplinkUrl(deeplinkModel.getDeeplinkUrl());
      stickyNavModel.setSectionType(deeplinkModel.getSectionType());
      stickyNavModel.setLayoutType(deeplinkModel.getLayoutType());
      stickyNavModel.setFallbackToHomeOnFailure(deeplinkModel.isFallbackToHomeOnFailure());
      stickyNavModel.setBaseInfo(deeplinkModel.getBaseInfo());
      stickyNavModel.setsType("41");
      //  stickyNavModel.getBaseInfo().setExpiryTime(System.currentTimeMillis() + 30 * 1000);
      asset.setTitle(deeplinkModel.getBaseInfo().getMessage());
      asset.setLiveTitle(deeplinkModel.getBaseInfo().getMessage());
    } else {
      stickyNavModel.setBaseInfo(new BaseInfo());
      stickyNavModel.getBaseInfo().setUniqueId(101);
    }
    return stickyNavModel;
  }

  public static StickyNavModel getStickyNavModel() {
    return getStickyNavModel(null);
  }
}
