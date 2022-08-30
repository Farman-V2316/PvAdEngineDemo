/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.nhcommand;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.model.entity.NHCommand;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Handler of NHCommand which maintains list of other listeners
 *
 * @author maruti.borker
 */
public class NHCommandMainHandler {

  private static NHCommandMainHandler instance;
  private List<NHActivityCommandHandler> activityHandlerList;
  private List<DHCommandHandler> dhCommandHandlerList;
  private List<DHCommandHandler> dhLiveTVCommandHandlerList;
  private final static String nhCommandString = "nhcommand://";
  private final static String dhCommandString = "nhjsoncmd://";
  private final static String liveTvDhCommandString = "nhlivetvjsoncmd://";

  private NHCommandMainHandler() {
    activityHandlerList = new ArrayList<>();
    dhCommandHandlerList = new ArrayList<>();
    dhLiveTVCommandHandlerList = new ArrayList<>();
  }

  public static NHCommandMainHandler createInstance() {
    if (instance == null) {
      synchronized (NHCommandMainHandler.class) {
        if (instance == null) {
          instance = new NHCommandMainHandler();
        }
      }
    }
    return instance;
  }

  public static NHCommandMainHandler getInstance() {
    return instance;
  }

  public void addActivityHandler(NHActivityCommandHandler handler) {
    activityHandlerList.add(handler);
  }

  public void addDHCommandHandler(DHCommandHandler handler) {
    dhCommandHandlerList.add(handler);
  }

  public void addDHLiveTvCommandHandler(DHCommandHandler handler) {
    dhLiveTVCommandHandlerList.add(handler);
  }

  public boolean handle(String url, Activity parentActivity, Fragment fragment, PageReferrer
      pageReferrer) {
    if (activityHandlerList.isEmpty() || url == null || url.isEmpty() ||
        url.startsWith("http") || url.startsWith("https")) {
      return false;
    }

    try {
      url = URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return false;
    }

    // command string comes as a JSON string
    if (url.startsWith(dhCommandString)) {
      url = url.substring(dhCommandString.length());
      for (DHCommandHandler handler : dhCommandHandlerList) {
        if (handler.handle(url, parentActivity, pageReferrer)) {
          return true;
        }
      }
    } else if (url.startsWith(liveTvDhCommandString)) {
      url = url.substring(liveTvDhCommandString.length());
      for (DHCommandHandler handler : dhLiveTVCommandHandlerList) {
        if (handler.handle(url, parentActivity, pageReferrer)) {
          return true;
        }
      }
    } else {
      // for cases when it starts with nhcommand
      if (url.startsWith(nhCommandString)) {
        url = url.substring(nhCommandString.length());
      }

      int actionSeparatorPosition = url.indexOf(":");
      String action, params;

      if (actionSeparatorPosition < 0) {
        action = params = url;
      } else {
        action = url.substring(0, actionSeparatorPosition);
        params = url.substring(actionSeparatorPosition + 1);
      }

      NHCommand command = NHCommand.fromName(action);

      if (command == null) {
        return false;
      }


      for (NHActivityCommandHandler handler : activityHandlerList) {
        if (handler.handle(command, params, parentActivity, fragment, pageReferrer)) {
          return true;
        }
      }

    }
    return false;
  }
}
