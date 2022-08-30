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

public class TeamAsset implements Serializable {

  private String id;

  private String teamName;

  private String teamIcon;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTeamIcon() {
    return teamIcon;
  }

  public void setTeamIcon(String teamIcon) {
    this.teamIcon = teamIcon;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }
}
