/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.news.model.usecase.GetHomePageUsecase;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.pages.PageEntity;
import com.newshunt.dataentity.common.pages.PageSection;
import com.newshunt.deeplink.navigator.NewsHomeRouterInput;
import com.newshunt.deeplink.navigator.NewsNavigator;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.news.util.NewsConstants;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * An Helper class to do news home routing, whether to land in News Home ideate tab or to go
 * entity preview
 *
 * @author santhosh.kc
 */
public class NewsHomeRouter {

  private final Context context;
  private Callback callback;
  private NewsHomeRouterInput newsHomeRouterInput;
  private NewsNavModel newsNavModel;

  public NewsHomeRouter(Context context,Bundle bundle) {
    this.context = context;
    newsHomeRouterInput = NewsHomeRouterHelper.getRouterInputFrom(bundle);
  }

  public NewsHomeRouter(Context context, NewsNavModel newsNavModel, PageReferrer pageReferrer) {
    this.context = context;
    this.newsNavModel = newsNavModel;

    newsHomeRouterInput = NewsHomeRouterHelper.getRouterInputFrom(newsNavModel, pageReferrer);
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public void startRouting() {

    if (callback == null) {
      return;
    }

    if (newsHomeRouterInput == null) {
      callback.onRoutingFailure();
      return;
    }

    GetHomePageUsecase usecase = new GetHomePageUsecase();
    usecase.invoke(PageSection.NEWS.getSection())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(this::handleNewsPageResponse)
        .subscribe();
  }

  private void handleNewsPageResponse(List<PageEntity> pageList) {
    if (callback == null) {
      return;
    }

    Intent targetIntent = NewsHomeRouterHelper.getRoutingIntent(context, newsHomeRouterInput,
        pageList);
    targetIntent.putExtra(NewsConstants.IS_ADJUNCT_LANG_NEWS,newsNavModel.isAdjunct());
    targetIntent.putExtra(NewsConstants.ADJUNCT_POPUP_DISPLAY_TYPE,newsNavModel.getPopupDisplayType());
    targetIntent.putExtra(NewsConstants.ADJUNCT_LANGUAGE,newsNavModel.getLanguage());

    NewsNavigator.setDeeplinkUrls(targetIntent, newsNavModel);
    callback.onRoutingSuccess(targetIntent, newsNavModel);
  }

  /**
   * Callbac interface
   */
  public interface Callback {
    /**
     * on routing success callback
     *
     * @param routedIntent - routing intent
     */
    void onRoutingSuccess(Intent routedIntent, BaseModel baseModel);

    /**
     * on routing failure callback
     */
    void onRoutingFailure();
  }

}
