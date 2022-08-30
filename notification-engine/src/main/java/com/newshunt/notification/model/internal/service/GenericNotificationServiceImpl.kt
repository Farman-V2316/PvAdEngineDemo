package com.newshunt.notification.model.internal.service

import com.newshunt.notification.model.internal.rest.StreamAPI
import com.newshunt.notification.model.service.GenericNotificationService
import com.newshunt.common.helper.EmptyCookieJar
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.model.retrofit.RestAdapters
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import okhttp3.CookieJar
import java.lang.Exception

class GenericNotificationServiceImpl() : GenericNotificationService {

  private val emptyCookieJar: CookieJar = EmptyCookieJar()

  override fun getGenericNotificationData(streamUrl: String, priority: Priority,
                                          version: String): Observable<BaseNotificationAsset> {

    val clientBuilder = RestAdapters.getOkHttpClientBuilder(
        true, 30000, 30000, priority, streamUrl)
    clientBuilder.cookieJar(emptyCookieJar)

    val dataStreamAPI = RestAdapters.getBuilder(UrlUtil.getBaseUrl(streamUrl),
        clientBuilder.build()).build().create(StreamAPI::class.java)
    return dataStreamAPI.getGenericNotificationData(streamUrl)
        .map {t ->  if (t == null) throw Exception() else t.data}
  }

 }