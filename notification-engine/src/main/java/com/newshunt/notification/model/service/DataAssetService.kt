package com.newshunt.notification.model.service

import com.newshunt.common.helper.preference.SavedPreference
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import io.reactivex.Observable

interface DataAssetService {

    fun getMetaData(streamUrl: String, stickType: String?, prefPath: SavedPreference?): Observable<BaseNotificationAsset>
}