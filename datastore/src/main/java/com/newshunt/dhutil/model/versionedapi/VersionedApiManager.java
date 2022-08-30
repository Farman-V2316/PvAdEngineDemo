/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.versionedapi;


import android.util.Pair;

import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity;

/**
 * Manager to handle list of Versioned APIs.
 *
 * @author maruti.borker
 */
public interface VersionedApiManager {
  Pair<VersionedApiEntity, Boolean> addOrUpdateVersionedApi(VersionedApiEntity versionedApiEntity);

  boolean checkVersionedApiExistsAndUpdate(VersionedApiEntity versionedApiEntity);

  VersionedApiEntity getVersionedApiEntity(VersionedApiEntity versionedApiEntity);

}
