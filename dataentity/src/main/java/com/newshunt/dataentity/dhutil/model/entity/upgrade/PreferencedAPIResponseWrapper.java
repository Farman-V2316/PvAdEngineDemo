/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.upgrade;

/*
 * @author raunak.yadav
*/
public class PreferencedAPIResponseWrapper {

  private String version;
  private String originalJson;
  private String deltaJson;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getOriginalJson() {
    return originalJson;
  }

  public void setOriginalJson(String originalJson) {
    this.originalJson = originalJson;
  }

  public String getDeltaJson() {
    return deltaJson;
  }

  public void setDeltaJson(String deltaJson) {
    this.deltaJson = deltaJson;
  }
}
