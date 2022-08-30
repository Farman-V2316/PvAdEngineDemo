package com.newshunt.deeplink.navigator;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.news.model.entity.PageType;
import com.newshunt.dataentity.notification.NavigationType;

/**
 * @author santhosh.kc
 */
public class NewsHomeRouterInput {

  private Integer uniqueId;
  private String entityKey;
  private String subEntityKey;
  private PageType pageType;
  private PageReferrer pageReferrer;
  private NavigationType navigationType;
  private String language;
  private String langCode;
  private String edition;
  private String notificationBackUrl;
  private String deeplinkUrl;
  private String entityType;

  private NewsHomeRouterInput(Integer uniqueId, String entityKey, String subEntityKey,
                              PageType pageType, PageReferrer pageReferrer,
                              NavigationType navigationType, String language, String langCode,
                              String edition, String notificationBackUrl, String deeplinkUrl, String entityType) {
    this.entityKey = entityKey;
    this.subEntityKey = subEntityKey;
    this.pageType = pageType;
    this.pageReferrer = pageReferrer;
    this.navigationType = navigationType;
    this.language = language;
    this.langCode = langCode;
    this.edition = edition;
    this.uniqueId = uniqueId;
    this.notificationBackUrl = notificationBackUrl;
    this.deeplinkUrl = deeplinkUrl;
    this.entityType = entityType;
  }

  public String getEntityKey() {
    return entityKey;
  }

  public String getSubEntityKey() {
    return subEntityKey;
  }

  public PageType getPageType() {
    return pageType;
  }

  public PageReferrer getPageReferrer() {
    return pageReferrer;
  }

  public NavigationType getNavigationType() {
    return navigationType;
  }

  public String getLanguage() {
    return language;
  }

  public String getEdition() {
    return edition;
  }

  public String getLangCode() {
    return langCode;
  }

  public Integer getUniqueId() {
    return uniqueId;
  }

  public String getNotificationBackUrl() {
    return notificationBackUrl;
  }

  public String getDeeplinkUrl() {
    return deeplinkUrl;
  }

  public String getEntityType() {
    return entityType;
  }

  public static class Builder {
    private Integer uniqueId;
    private String entityKey;
    private String subEntityKey;
    private PageType pageType;
    private PageReferrer pageReferrer;
    private NavigationType navigationType;
    private String language;
    private String langCode;
    private String edition;
    private String notificationBackUrl;
    private String deeplinkUrl;
    private String entityType;

    public Builder setUniqueId(int uniqueId) {
      this.uniqueId = uniqueId;
      return this;
    }

    public Builder setEntityKey(String entityKey) {
      this.entityKey = entityKey;
      return this;
    }

    public Builder setSubEntityKey(String subEntityKey) {
      this.subEntityKey = subEntityKey;
      return this;
    }

    public Builder setPageType(PageType pageType) {
      this.pageType = pageType;
      return this;
    }

    public Builder setPageReferrer(PageReferrer pageReferrer) {
      this.pageReferrer = pageReferrer;
      return this;
    }

    public Builder setNavigationType(NavigationType navigationType) {
      this.navigationType = navigationType;
      return this;
    }

    public Builder setLanguage(String language) {
      this.language = language;
      return this;
    }

    public Builder setLangCode(String langCode) {
      this.langCode = langCode;
      return this;
    }

    public Builder setEdition(String edition) {
      this.edition = edition;
      return this;
    }

    public Builder setNotificationBackUrl(String notificationBackUrl) {
      this.notificationBackUrl = notificationBackUrl;
      return this;
    }

    public Builder setDeeplinkUrl(String deeplinkUrl) {
      this.deeplinkUrl = deeplinkUrl;
      return this;
    }

    public Builder setEntityType(String entityType) {
      this.entityType = entityType;
      return this;
    }

    public NewsHomeRouterInput build() {
      return new NewsHomeRouterInput(uniqueId, entityKey, subEntityKey, pageType, pageReferrer,
          navigationType, language, langCode, edition, notificationBackUrl, deeplinkUrl, entityType);
    }
  }

}

