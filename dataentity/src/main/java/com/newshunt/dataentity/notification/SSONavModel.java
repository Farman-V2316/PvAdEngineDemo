/*
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * @author shashikiran.nr on 7/18/2017.
 * BaseModel for SSO and deeplinks.
 */

public class SSONavModel extends BaseModel implements Serializable {
  private static final long serialVersionUID = -1613543456709750039L;

  private String url;
  private String loginType;
  private String browserType;
  private boolean useWideViewPort;
  private boolean clearHistoryOnPageLoad;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getLoginType() {
    return loginType;
  }

  public void setLoginType(String loginType) {
    this.loginType = loginType;
  }

  public String getBrowserType() {
    return browserType;
  }

  public void setBrowserType(String browserType) {
    this.browserType = browserType;
  }

  public boolean isUseWideViewPort() {
    return useWideViewPort;
  }

  public void setUseWideViewPort(boolean useWideViewPort) {
    this.useWideViewPort = useWideViewPort;
  }

  public boolean isClearHistoryOnPageLoad() {
    return clearHistoryOnPageLoad;
  }

  public void setClearHistoryOnPageLoad(boolean clearHistoryOnPageLoad) {
    this.clearHistoryOnPageLoad = clearHistoryOnPageLoad;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.SSO_MODEL;
  }
}
