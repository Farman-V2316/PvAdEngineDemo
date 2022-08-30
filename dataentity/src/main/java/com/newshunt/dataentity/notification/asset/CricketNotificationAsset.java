/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.notification.asset;

import java.io.Serializable;

/**
 * @author shrikant on 27/08/17.
 */

public class CricketNotificationAsset extends BaseNotificationAsset implements Serializable {

  private String title;

  private String liveTitle;

  private String line1Text;

  private String line2Text;

  private TeamAsset team1;

  private TeamAsset team2;

  private int f = -1;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLiveTitle() {
    return liveTitle;
  }

  public void setLiveTitle(String liveTitle) {
    this.liveTitle = liveTitle;
  }

  public String getLine1Text() {
    return line1Text;
  }

  public void setLine1Text(String line1Text) {
    this.line1Text = line1Text;
  }

  public String getLine2Text() {
    return line2Text;
  }

  public void setLine2Text(String line2Text) {
    this.line2Text = line2Text;
  }

  public TeamAsset getTeam1() {
    return team1;
  }

  public void setTeam1(TeamAsset team1) {
    this.team1 = team1;
  }

  public TeamAsset getTeam2() {
    return team2;
  }

  public void setTeam2(TeamAsset team2) {
    this.team2 = team2;
  }

  public boolean isLoggingNotificationEventsDisbaled(){
    return (f >= 128 && ((f & 128)>0));
  }

  public void setF(int f){
    this.f = f;
  }


}
