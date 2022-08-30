/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import java.io.Serializable;

/**
 * Data related to Open Measurement SDK.
 *
 * @author raunak.yadav
 */
public class OmSdkConfig implements Serializable {
  private static final long serialVersionUID = -5923676155971851069L;

  private boolean enabled;
  private String version;
  private String serviceJSUrl;
  private String sessionClientJSUrl;

  public boolean isEnabled() {
    return enabled;
  }

  public String getVersion() {
    return version;
  }

  public String getServiceJSUrl() {
    return serviceJSUrl;
  }

  public String getSessionClientJSUrl() {
    return sessionClientJSUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OmSdkConfig that = (OmSdkConfig) o;

    return enabled == that.enabled && version != null && version.equals(that.version);
  }

  @Override
  public int hashCode() {
    return version == null ? 0 : version.hashCode();
  }
}