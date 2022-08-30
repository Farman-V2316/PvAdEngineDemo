/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.appsection;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.BaseDataResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrofit response to API {@link com.newshunt.dhutil.model.internal.rest.AppSectionsAPI}
 *
 * @author santhosh.kc
 */
public class AppSectionsResponse extends BaseDataResponse {

  private String version;
  private String bgColor;
  private String bgColorNight;

  public AppSectionsResponse() {

  }

  public AppSectionsResponse(AppSectionsResponse copyFrom) {
    setUniqueRequestId(copyFrom.getUniqueRequestId());
    setError(copyFrom.getError());

    version = copyFrom.version;
    bgColor = copyFrom.bgColor;
    bgColorNight = copyFrom.bgColorNight;
    if (CommonUtils.isEmpty(copyFrom.sections)) {
      return;
    }
    sections = new ArrayList<>();

    for (AppSectionInfo copySectionInfo : copyFrom.sections) {
      sections.add(new AppSectionInfo(copySectionInfo));
    }
  }

  private List<AppSectionInfo> sections;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<AppSectionInfo> getSections() {
    return sections;
  }

  public void setSections(List<AppSectionInfo> sections) {
    this.sections = sections;
  }

  public String getBgColor() {
    return bgColor;
  }

  public void setBgColor(String bgColor) {
    this.bgColor = bgColor;
  }

  public String getBgColorNight() {
    return bgColorNight;
  }

  public void setBgColorNight(String bgColorNight) {
    this.bgColorNight = bgColorNight;
  }
}
