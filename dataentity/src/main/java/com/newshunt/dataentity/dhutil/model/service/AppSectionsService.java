/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.service;

import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;

import java.util.Map;

import io.reactivex.Observable;

/**
 * A Service to get Server configured App Sections
 *
 * @author santhosh.kc
 */
public interface AppSectionsService {

  Observable<AppSectionsResponse> updateDBFromServer();

  void reDownloadAppSectionIcons(String version, Map<String, String> missingUrls);
}
