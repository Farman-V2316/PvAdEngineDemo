package com.newshunt.common.model.repo

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.model.apis.InteractionAPI
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.asset.CountType
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.InteractionPayload
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import io.reactivex.Observable

class InteractionsRepo {

  fun syncLikes() : Observable<Any> {
    return Observable.fromCallable {
      val addedLikes = SocialDB.instance().interactionsDao().unsyncedAddedLikes()
      val deletedLikes =  SocialDB.instance().interactionsDao().unsyncedDeletedLikes()
      return@fromCallable InteractionPayload(addedLikes, deletedLikes)
    }.flatMap {
      if (CommonUtils.isEmpty(it.items) && CommonUtils.isEmpty(it.itemsDeleted)) return@flatMap Observable.just(1)

      val syncStartTime = System.currentTimeMillis()
      RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getSecureSocialFeaturesUrl(),
          Priority.PRIORITY_HIGHEST, this).create(InteractionAPI::class.java).postLikes(it).map {
        SocialDB.instance().interactionsDao().markSyncedItems(syncStartTime,
            listOf(LikeType.LIKE.name, LikeType.ANGRY.name, LikeType.HAPPY.name,LikeType.LOVE.name, LikeType.SAD.name, LikeType.WOW.name))
      }
    }
  }

  fun syncShares() : Observable<Any> {
    return Observable.fromCallable {
      return@fromCallable InteractionPayload(SocialDB.instance().interactionsDao().unsyncedShares())
    }.flatMap {
      if (CommonUtils.isEmpty(it.items) && CommonUtils.isEmpty(it.itemsDeleted)) return@flatMap Observable.just(1)
      val syncStartTime = System.currentTimeMillis()
      RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getSecureSocialFeaturesUrl(),
          Priority.PRIORITY_HIGHEST, this).create(InteractionAPI::class.java).postShares(it).map {
        SocialDB.instance().interactionsDao().markSyncedItems(syncStartTime, listOf(CountType.SHARE.name))
      }
    }
  }

  fun getAllLikesFromServer() : Observable<List<Interaction>> {
    return RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getSecureSocialFeaturesUrl(),
        Priority.PRIORITY_HIGHEST, this).create(InteractionAPI::class.java).getLikes().map { response ->
      val likeList = mutableListOf<Interaction>()
      response.data.rows?.let {
        likeList.addAll(response.data.rows.map { item ->
          Interaction(entityId = item.entityId, entityType = item.entityType, action = item.action, ts = item.actionTime,
              isSynced = true)
        })
        SocialDB.instance().interactionsDao().insReplace(likeList)
      }
      likeList
    }
  }

  fun resetLikes() : Observable<Any> {
    return Observable.fromCallable {
      SocialDB.instance().interactionsDao().likeTypes
    }

  }


}