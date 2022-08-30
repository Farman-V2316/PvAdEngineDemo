package com.newshunt.dataentity.notification;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.io.Serializable;

/**
 * Created by bedprakash.rout on 4/7/2016.
 */
public class BaseModel implements Serializable {
  private static final long serialVersionUID = 1614796724587958123L;

  private static final int NEWS_MODEL_HASH_CODE = 1001;
  private static final int BOOKS_MODEL_HASH_CODE = 1002;
  private static final int APP_MODEL_HASH_CODE = 1004;
  private static final int TV_MODEL_HASH_CODE = 1005;
  private static final int LIVETV_MODEL_HASH_CODE = 1006;


  //TODO(bedprakash.rout):Make BaseInfo parent for BaseModel
  private BaseInfo baseInfo;
  protected String sType;
  private boolean isFullSync;
  private int filterValue;
  private String description;
  private String stickyItemType = NotificationConstants.STICKY_NONE_TYPE;
  private boolean disableEvents;
  private boolean isSeen = false;

  public BaseInfo getBaseInfo() {
    return baseInfo;
  }

  public void setBaseInfo(BaseInfo baseInfo) {
    this.baseInfo = baseInfo;
  }

  //All the classes extending BaseModel should return the proper BaseModelType.
  public BaseModelType getBaseModelType() {
    return null;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof BaseModel && ((BaseModel) object).baseInfo != null &&
        baseInfo != null &&
        baseInfo.getUniqueId() == ((BaseModel) object).baseInfo.getUniqueId();
  }

  @Override
  public int hashCode() {
    switch (baseInfo.getSectionType()) {
      case NEWS:
        return NEWS_MODEL_HASH_CODE;
      case BOOKS:
        return BOOKS_MODEL_HASH_CODE;
      case TV:
        return TV_MODEL_HASH_CODE;
      case LIVETV:
        return LIVETV_MODEL_HASH_CODE;
      default:
        return APP_MODEL_HASH_CODE;
    }
  }

  public String getsType() {
    return sType;
  }

  public void setsType(String sType) {
    this.sType = sType;
  }

  public String getItemId() {
    return Constants.EMPTY_STRING + baseInfo.getUniqueId();
  }

  public String getCategoryId() {
    return null;
  }

  public String getPublisherId() {
    return null;
  }

  public String getTopicId() {
    return null;
  }

  public boolean isFullSync() {
    return isFullSync;
  }

  public void setFullSync(boolean fullSync) {
    isFullSync = fullSync;
  }

  public int getFilterValue() {
    return filterValue;
  }

  public void setFilterValue(int filterValue) {
    this.filterValue = filterValue;
  }

  public String getDescription(){
    return description;
  }

  public void setDescription(String description){
    this.description = description;
  }

  public String getStickyItemType(){
    return stickyItemType;
  }

  public void setStickyItemType(String stickyType){
    this.stickyItemType = stickyType;
  }

  public boolean isLoggingNotificationEventsDisabled(){
    return disableEvents;
  }

  public void setDisableEvents(boolean disableEvents){
    this.disableEvents = disableEvents;
  }

  public void setSeen(boolean seen) {
    this.isSeen = seen;
  }

  public boolean isSeen() {
    return isSeen;
  }
}
