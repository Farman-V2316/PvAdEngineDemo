/*
 *  Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification;

import java.io.Serializable;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * @author santhosh.kc
 */
public class SocialCommentsModel extends BaseModel implements Serializable {

  private static final long serialVersionUID = -1445371378709558723L;

  private String section;

  private String itemId;

  private Map<String, String> commentParams;

  private String referrer;

  private String referrerId;

  private String title2;

  private String title3;

  private String title1;

  @Nullable
  private SocialContentMeta contentMeta;

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.SOCIAL_COMMENTS_MODEL;
  }

  @Override
  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public Map<String, String> getCommentParams() {
    return commentParams;
  }

  public void setCommentParams(Map<String, String> commentParams) {
    this.commentParams = commentParams;
  }

  public String getReferrer() {
    return referrer;
  }

  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  public String getReferrerId() {
    return referrerId;
  }

  public void setReferrerId(String referrerId) {
    this.referrerId = referrerId;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getTitle2() {
    return title2;
  }

  public void setTitle2(String title2) {
    this.title2 = title2;
  }

  public String getTitle3() {
    return title3;
  }

  public void setTitle3(String title3) {
    this.title3 = title3;
  }

  public String getTitle1() {
    return title1;
  }

  public void setTitle1(String title1) {
    this.title1 = title1;
  }

  public SocialContentMeta getContentMeta() {
    return contentMeta;
  }

  public void setContentMeta(SocialContentMeta contentMeta) {
    this.contentMeta = contentMeta;
  }

  public int getUniqueId() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    //Keep only required fields to generate unique Id.
    return "SocialCommentsModel{" +
        "section='" + section + '\'' +
        ", itemId='" + itemId + '\'' +
        ", commentParams=" + commentParams +
        ", contentMeta=" + contentMeta +
        '}';
  }
}
