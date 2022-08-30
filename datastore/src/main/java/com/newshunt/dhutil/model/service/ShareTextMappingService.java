/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.service;

import com.newshunt.dataentity.common.model.entity.ShareTextMappingResponse;
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode;

import io.reactivex.Observable;

/**
 * Interface to get the share text mapping configuration.
 *
 * @author shashikiran.nr on 3/7/2017.
 */

public interface ShareTextMappingService {

  Observable<ShareTextMappingResponse> getShareTextMapping(VersionMode versionMode);

}
