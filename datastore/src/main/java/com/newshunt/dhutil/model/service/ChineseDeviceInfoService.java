/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.service;

import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfoResponse;
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode;

import io.reactivex.Observable;

/**
 * @author  shashikiran.nr on 3/7/2017.
 */

public interface ChineseDeviceInfoService {

  Observable<ChineseDeviceInfoResponse> getStoredChineseDeviceInfo(VersionMode versionMode);

}
