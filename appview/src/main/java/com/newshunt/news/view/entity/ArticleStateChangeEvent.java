/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.entity;


import com.newshunt.dataentity.common.model.entity.NewsArticleState;

/**
 * @author satosh.dhanyamraju
 */
public class ArticleStateChangeEvent {
  private String articleId;
  private NewsArticleState state;

  public ArticleStateChangeEvent(String articleId, NewsArticleState state) {
    this.articleId = articleId;
    this.state = state;
  }

  public String getArticleId() {
    return articleId;
  }

  public void setArticleId(String articleId) {
    this.articleId = articleId;
  }

  public NewsArticleState getState() {
    return state;
  }

  public void setState(NewsArticleState state) {
    this.state = state;
  }
}
