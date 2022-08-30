package com.dailyhunt.tv.players.model.entities;

/**
 * Created by santoshkulkarni on 27/08/16.
 */
public class CallState {

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  private int state;

  private int screenId;

  public int getScreenId() {
    return screenId;
  }

  public void setScreenId(int screenId) {
    this.screenId = screenId;
  }
}
