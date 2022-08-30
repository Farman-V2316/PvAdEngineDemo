/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.service;

import com.newshunt.dataentity.common.model.entity.NewsAppJSResponse;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import io.reactivex.Observable;

/**
 * Service for getting News App Java scripts
 *
 * @author santhosh.kc
 */
public interface NewsAppJSProviderService {

  Observable<ApiResponse<NewsAppJSResponse>> updateDBFromServer();

  Observable<ApiResponse<NewsAppJSResponse>> getAppJSScripts();
}
