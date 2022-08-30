/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/

package com.newshunt.dataentity.common.model.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * Retrofit response to API from ShareTextMappingsAPI.
 *
 * @author shashikiran.nr on 9/28/2017.
 */

public class ShareTextMappingResponse extends BaseDataResponse implements Serializable {

  private static final long serialVersionUID = -6091805214887046876L;

  private String version;

  private Map<String, String> shareTextMappings;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Map<String, String> getShareTextMappings() {
    return shareTextMappings;
  }

  public void setShareTextMappings(Map<String, String> shareTextMappings) {
    this.shareTextMappings = shareTextMappings;
  }

  public String getShareTextMappingByLang(String storyCardLangKey) {
    if (storyCardLangKey == null) {
      return null;
    }

    if (shareTextMappings == null) {
      return null;
    }

    return shareTextMappings.get(storyCardLangKey);
  }
}
