package com.newshunt.dataentity.dhutil.model.entity.asset;

import java.io.Serializable;
import java.util.Map;


/**
 * User preference represented in only basic required fields
 *
 * @author piyush.rai
 */
public class PreferenceAsset implements Serializable {

  private static final long serialVersionUID = -203298077503761829L;

  private String key;
  private Map<String, String> titles;
  private ImageDetail imageDetail;
  private String backgroundColor;
  private String textColor;
  private String sourceNameUni;
  private int status;
  private String mode;
  private String reason;
  private long timestamp;
  private String entityType;
  private long pk;

  public PreferenceAsset() {
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public ImageDetail getImageDetail() {
    return imageDetail;
  }

  public void setImageDetail(ImageDetail imageDetail) {
    this.imageDetail = imageDetail;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  public Map<String, String> getTitles() {
    return titles;
  }

  public void setTitles(Map<String, String> titles) {
    this.titles = titles;
  }

  public String getSourceNameUni() {
    return sourceNameUni;
  }

  public void setSourceNameUni(String sourceNameUni) {
    this.sourceNameUni = sourceNameUni;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public long getPk() {
    return pk;
  }

  public void setPk(long pk) {
    this.pk = pk;
  }
}