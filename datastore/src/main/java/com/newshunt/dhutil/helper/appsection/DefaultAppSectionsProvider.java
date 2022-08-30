/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.appsection;

import android.text.TextUtils;

import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author santhosh.kc
 */
public class DefaultAppSectionsProvider {

  private static DefaultAppSectionsProvider instance;

  public static final String DEFAULT_LIVE_TV_SECTION_ID = "livetv";

  private DefaultAppSectionsProvider() {

  }

  public static DefaultAppSectionsProvider getInstance() {
    if (instance == null) {
      synchronized (DefaultAppSectionsProvider.class) {
        if (instance == null) {
          instance = new DefaultAppSectionsProvider();
        }
      }
    }
    return instance;
  }

  public List<AppSectionInfo> getDefaultAppSections() {

    List<AppSectionInfo> defaultAppSectionInfos = new ArrayList<>();
    Boolean isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,
        false);
    for (DefaultAppSection defaultAppSection : DefaultAppSection.values()) {
      AppSectionInfo defaultAppSectionInfo = new AppSectionInfo();
      defaultAppSectionInfo.setType(defaultAppSection.type);
      defaultAppSectionInfo.setId(defaultAppSection.id);
      defaultAppSectionInfo.setTitle(defaultAppSection.title);
      defaultAppSectionInfo.setLangfilter(defaultAppSection.langfilter);
      defaultAppSectionInfo.setBgType(defaultAppSection.bgType);
      defaultAppSectionInfo.setBgColor(defaultAppSection.bgColor);
      defaultAppSectionInfo.setBgColorNight(defaultAppSection.bgColorNight);
      defaultAppSectionInfo.setStrokeColor(defaultAppSection.strokeColor);
      defaultAppSectionInfo.setStrokeColorNight(defaultAppSection.strokeColorNight);
      defaultAppSectionInfo.setDeeplinkUrl(defaultAppSection.deeplinkUrl);
      if(isRegistered) {
        defaultAppSectionInfos.add(defaultAppSectionInfo);
      }else{
        if(! TextUtils.equals(defaultAppSection.id, DefaultAppSectionsProvider.DefaultAppSection.FOLLOW.id))
        defaultAppSectionInfos.add(defaultAppSectionInfo);
      }
    }
    return defaultAppSectionInfos;
  }

  public enum DefaultAppSection {
    NEWS_SECTION(AppSection.NEWS, "news", "Home"),
    TV_SECTION(AppSection.TV, "tv", "Videos"),
    FOLLOW(AppSection.FOLLOW, "follow", "Follow");


    private AppSection type;
    private String id;
    private String title;
    private String deeplinkUrl;
    private String langfilter, bgType, strokeColor, strokeColorNight, bgColor, bgColorNight;

    DefaultAppSection(AppSection type, String id, String title) {
      this.type = type;
      this.id = id;
      this.title = title;
    }

    DefaultAppSection(AppSection type, String id, String title,String deeplink) {
      this.type = type;
      this.id = id;
      this.title = title;
      this.deeplinkUrl = deeplink;
    }

    DefaultAppSection(AppSection type, String id, String title, String langfilter, String bgType,
                      String strokeColor, String strokeColorNight, String bgColor, String bgColorNight) {
      this.type = type;
      this.id = id;
      this.title = title;
      this.langfilter = langfilter;
      this.bgType = bgType;
      this.strokeColor = strokeColor;
      this.strokeColorNight = strokeColorNight;
      this.bgColor = bgColor;
      this.bgColorNight = bgColorNight;
    }

    public String getId() {
      return id;
    }
  }
}
