/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.share;

import android.net.Uri;

import com.newshunt.dataentity.common.model.entity.ShareContentType;

import java.io.Serializable;

/**
 * Share information object.
 *
 * @author chetan.kumar
 */
public class ShareContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private String title;
  private String shareUrl;
  private String langTitles;
  private String content;
  // Subject is for email subject part.
  private String contentLanguage;
  private String packageName;
  private String subject;
  private boolean displayViaDailyhunt = true;
  private Uri fileUri;
  private String shareUi;
  private boolean isAds;
  private String sourceName;
  private ShareContentType shareContentType = ShareContentType.TEXT;
  private String imageUrl;
  private String sourceId;
  private String sourceImageUrl;
  private String sourceLang;
  private ShareAPIParams shareAPIParams;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getShareUrl() {
    return shareUrl;
  }

  public void setShareUrl(String shareUrl) {
    this.shareUrl = shareUrl;
  }

  public String getLangTitles() {
    return langTitles;
  }

  public void setLangTitles(String langTitles) {
    this.langTitles = langTitles;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public boolean isDisplayViaDailyhunt() {
    return displayViaDailyhunt;
  }

  public void setDisplayViaDailyhunt(boolean displayViaDailyhunt) {
    this.displayViaDailyhunt = displayViaDailyhunt;
  }

  public Uri getFileUri() {
    return fileUri;
  }

  public void setFileUri(Uri fileUri) {
    this.fileUri = fileUri;
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(String contentLanguage) {
    this.contentLanguage = contentLanguage;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getShareUi() {
    return shareUi;
  }

  public void setShareUi(String shareUi) {
    this.shareUi = shareUi;
  }

  public boolean isAds() {
    return isAds;
  }

  public void setAds(boolean ads) {
    isAds = ads;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }


  public String generateFooter() {
    return ShareFactory.getStoryShareFooterText(langTitles, sourceName);
  }

  public ShareContentType getShareContentType() {
    return shareContentType;
  }

  public void setShareContentType(ShareContentType shareContentType) {
    this.shareContentType = shareContentType;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public ShareAPIParams getShareAPIParams() {
    return shareAPIParams;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getSourceImageUrl() {
    return sourceImageUrl;
  }

  public void setSourceImageUrl(String sourceImageUrl) {
    this.sourceImageUrl = sourceImageUrl;
  }

  public String getSourceLang() {
    return sourceLang;
  }

  public void setSourceLang(String sourceLang) {
    this.sourceLang = sourceLang;
  }
}
