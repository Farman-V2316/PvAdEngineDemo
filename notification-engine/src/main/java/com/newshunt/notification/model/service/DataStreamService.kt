package com.newshunt.notification.model.service

import com.newshunt.common.helper.preference.SavedPreference
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.DataStreamResponse
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

interface DataStreamService {

  fun getStreamData(streamUrl: String, priority: Priority,
                    version: String,
                    stickType: String?,
                    prefPath: SavedPreference?): Observable<DataStreamResponse>
}

interface GenericNotificationService {
  fun getGenericNotificationData(streamUrl: String, priority: Priority,
                                 version: String) : Observable<BaseNotificationAsset>
}