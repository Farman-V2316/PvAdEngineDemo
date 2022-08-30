package com.newshunt.news.model.repo

import androidx.lifecycle.LiveData
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowEntityPayloadItem
import com.newshunt.dataentity.common.pages.FollowPayload
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.FollowSyncResponse
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.apis.FollowAPI
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject

class FollowRepo @Inject constructor(private val followEntityDao: FollowEntityDao) {

    fun toggleFollow(actionableEntity: ActionableEntity, action: String?): Observable<Any> {
        return Observable.fromCallable {
            val actionType = getActionTypeFromAction(action)
            followEntityDao.toggleFollowItems(FollowSyncEntity(actionableEntity, action = actionType))
        }.flatMap {
            syncFollowAndBlocks()
        }
    }


    fun toggleFollows(actionableEntities: List<ActionableEntity>, action: String?):
            Observable<Any> {
        return Observable.fromCallable {
            val actionType = getActionTypeFromAction(action)
            followEntityDao.toggleFollowItems(actionableEntities, actionType)

        }.flatMap {
            syncFollowAndBlocks()
        }
    }

    private fun getActionTypeFromAction(action: String?):FollowActionType {
        return when(action) {
            null,FollowActionType.FOLLOW.name -> FollowActionType.FOLLOW
            FollowActionType.UNFOLLOW.name -> FollowActionType.UNFOLLOW
            FollowActionType.UNBLOCK.name -> FollowActionType.UNBLOCK
            else -> FollowActionType.BLOCK
        }
    }

    fun insertActionableEntity(actionableEntity: ActionableEntity, action: FollowActionType): Observable<Any> {
        return Observable.fromCallable {
            followEntityDao.insReplace(FollowSyncEntity(actionableEntity = actionableEntity, action = action))
        }.flatMap {
            syncFollowAndBlocks()
        }
    }

    private fun syncFollowAndBlocks(): Observable<Any> {
        return Observable.fromCallable {
            return@fromCallable followEntityDao.getUnsyncedFollows()
        }.flatMap {
            val startTime = System.currentTimeMillis()
            RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getUserServiceSecuredBaseUrl(), Priority.PRIORITY_HIGHEST, this)
                    .create(FollowAPI::class.java).postFollows(FollowPayload(it.map { follow ->
                        FollowEntityPayloadItem(entityType = follow.actionableEntity.entityType, entityId = follow.actionableEntity.entityId,
                                action = follow.action.name, actionTime = follow.actionTime, entitySubType = follow.actionableEntity.entitySubType)
                    })).map {
                        SocialDB.instance().followEntityDao().updatedbAfterSync(startTime)
                    }
        }
    }

    fun getFollowAndBlocks(userId: String, isFirstPage: Boolean): Observable<FollowSyncResponse> {
        return RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getApplicationUrl(), Priority.PRIORITY_HIGHEST, this)
                .create(FollowAPI::class.java).getFollows(userId).map {
                    if (isFirstPage) {
                        SocialDB.instance().followEntityDao().deleteAll()
                    }
                    val followList = it.data.rows
                    SocialDB.instance().followEntityDao().insReplace(followList.map { it.toFollowSyncEntity().copy(isSynced = true) })
                    it.data
                }
    }

    fun fetchEntity(entityId: String): LiveData<List<FollowSyncEntity?>> {
        return SocialDB.instance().followEntityDao().fetchEntity(entityId)
    }

    fun fetchBlockId(entityId: String): LiveData<String?>{
        return SocialDB.instance().followEntityDao().isBlockedUser(entityId)
    }
}