/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.helper.preference.FollowBlockPreference
import com.newshunt.news.model.daos.FollowBlockRecoDao
import com.newshunt.news.model.daos.FollowEntityDao
import io.reactivex.Observable
import java.io.Serializable
import javax.inject.Inject
/**
 * @author aman.roy
 * Usecase classes for follow and block suggestion for implicit and explicit signals.
 */
const val FOLLOW_BLOCK_CONFIG_ID = "followBlockConfig"

class FollowBlockUpdateUsecase @Inject constructor(val followBlockRecoDao: FollowBlockRecoDao):Usecase<SourceFollowBlockEntity?,Boolean> {
    override fun invoke(p1: SourceFollowBlockEntity?): Observable<Boolean> {
        return Observable.fromCallable {
            p1?.let { update ->
                val userId = AppUserPreferenceUtils.getUserId()
                val isCreator = userId.isNotEmpty() && update.sourceId == userId
                if(isCreator) return@fromCallable false

                val existingEntity =  followBlockRecoDao.getSourceFollowBlockEntity(p1.sourceId)
                val updatedEntity = existingEntity?.let { updateFollowBlockEntity(it,update)} ?: update
                followBlockRecoDao.insReplace(updatedEntity)
                return@fromCallable true
            }
            false
        }
    }

    private fun updateFollowBlockEntity(entity1:SourceFollowBlockEntity,entity2:SourceFollowBlockEntity):SourceFollowBlockEntity {
        return entity1.let {
            SourceFollowBlockEntity(sourceId = it.sourceId,
            pageViewCount = it.pageViewCount+entity2.pageViewCount,
            shareCount = it.shareCount+entity2.shareCount,
            showLessCount = it.showLessCount+entity2.showLessCount,
            reportCount = it.reportCount+entity2.reportCount,
            configData = entity2.configData,
            postSourceEntity = it.postSourceEntity ?: entity2.postSourceEntity,
            updateTimeStamp = System.currentTimeMillis(),
            sourceLang = it.sourceLang,
            showImplicitBlockDialogCount = it.showImplicitBlockDialogCount,
            showImplicitFollowDialogCount = it.showImplicitFollowDialogCount,
            updateType = entity2.updateType) // Updated SourceFollowBlockEntity should define what is the updatetype i.e. follow or block
        }
    }
}

class GetFollowBlockUpdateUsecase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao):Usecase<String,SourceFollowBlockEntity?> {
    override fun invoke(p1: String): Observable<SourceFollowBlockEntity?> {
        return Observable.fromCallable {
            followBlockRecoDao.getSourceFollowBlockEntityDesc()
        }
    }
}

class UpdateFollowBlockImplictDialogCountUsecase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao):BundleUsecase<Unit> {
    override fun invoke(p1: Bundle): Observable<Unit> {
        return Observable.fromCallable {
            val sourceId = p1.getString(Constants.BUNDLE_SOURCE_ID)
            val isBlock = p1.getBoolean(Constants.BUNDLE_SOURCE_BLOCK)
            sourceId ?: return@fromCallable
            if(isBlock) {
                followBlockRecoDao.incrementShowImplicitBlockDialogCount(sourceId)
            } else {
                followBlockRecoDao.incrementShowImplicitFollowDialogCount(sourceId)
            }
        }
    }
}

class ImplicitFollowTriggerUsecase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao,
                                                       private val followEntityDao: FollowEntityDao):BundleUsecase<SourceFollowBlockEntity?> {
    override fun invoke(p1: Bundle): Observable<SourceFollowBlockEntity?> {
        val sourceFollowBlockEntity = p1.getSerializable(FOLLOW_ITEM) as? SourceFollowBlockEntity
        val lang = sourceFollowBlockEntity?.sourceLang
        val entityId = sourceFollowBlockEntity?.sourceId
        return Observable.fromCallable {
            if(lang.isNullOrEmpty() || entityId.isNullOrEmpty()) {
                return@fromCallable null
            }
            if(!PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,false)) {
                return@fromCallable null
            }
            val followEntity = followEntityDao.isFollowedOrBlocked(entityId)
            followEntity?.let {
                return@fromCallable null
            }

            val followBlockLangConfig = followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                FollowBlockPrefUtil.getConfigFromLangType(it,lang)
            }

            val maxFollowCount = sourceFollowBlockEntity.showImplicitFollowDialogCount
            val followConfig = followBlockLangConfig?.implicitSignal?.follow
            if(maxFollowCount >= (followConfig?.maxLifeTimeCap ?: Constants.DEFAULT_IMPLICIT_FOLLOW_MAX_CAP)) {
                return@fromCallable null
            }

            val softFollowSignalInSession = FollowBlockPrefUtil.getSoftFollowSignalInSession()
            if(softFollowSignalInSession >= (followConfig?.maxPerSession ?: Constants.DEFAULT_IMPLICT_FOLLOW_SOFT_BLOCK_SIGNAL)) {
                return@fromCallable null
            }

            if (FollowBlockPrefUtil.isInImplicitFollowCoolOffPeriod(followConfig)) {
                 return@fromCallable null
            }


            if (sourceFollowBlockEntity.shareCount >= (followConfig?.numberOfShares ?: Constants.DEFAULT_IMPLICIT_FOLLOW_SHARE_COUNT) ||
                sourceFollowBlockEntity.pageViewCount >= (followConfig?.numberOfPageViews ?: Constants.DEFAULT_IMPLICIT_FOLLOW_PV_COUNT)) {
                return@fromCallable sourceFollowBlockEntity
            } else {
                return@fromCallable null
            }
        }
    }

    companion object {
        const val FOLLOW_ITEM = "followItem"
    }
}

class ImplicitBlockTriggerUsecase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao,
                                                      private val followEntityDao: FollowEntityDao) : BundleUsecase<SourceFollowBlockEntity?> {
    override fun invoke(p1: Bundle): Observable<SourceFollowBlockEntity?> {
        val sourceFollowBlockEntity = p1.getSerializable(BLOCK_ITEM) as? SourceFollowBlockEntity
        val lang = sourceFollowBlockEntity?.sourceLang
        val entityId = sourceFollowBlockEntity?.sourceId
        return Observable.fromCallable {
            if (lang.isNullOrEmpty() || entityId.isNullOrEmpty()) {
                return@fromCallable null
            }
            if(!PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,false)) {
                return@fromCallable null
            }
            val blockEntity = followEntityDao.isFollowedOrBlocked(entityId)
            blockEntity?.let {
                return@fromCallable null
            }

            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(it, lang)
                }

            val maxBlockCount = sourceFollowBlockEntity.showImplicitBlockDialogCount
            val blockConfig = followBlockLangConfig?.implicitSignal?.block
            if (maxBlockCount >= (blockConfig?.maxLifeTimeCap
                    ?: Constants.DEFAULT_IMPLICIT_FOLLOW_MAX_CAP)
            ) {
                return@fromCallable null
            }

            val softBlockSignalInSession = FollowBlockPrefUtil.getSoftBlockSignalInSession()
            if(softBlockSignalInSession >= (blockConfig?.maxPerSession ?: Constants.DEFAULT_IMPLICT_FOLLOW_SOFT_BLOCK_SIGNAL)) {
                return@fromCallable null
            }

            if (FollowBlockPrefUtil.isInImplicitBlockCoolOffPeriod(blockConfig)) {
                return@fromCallable null
            }

            if (sourceFollowBlockEntity.showLessCount >= (blockConfig?.numberOfDislikes
                ?: Constants.DEFAULT_IMPLICIT_BLOCK_COUNT) ||
                    sourceFollowBlockEntity.reportCount >= (blockConfig?.numberOfDislikes
                ?: Constants.DEFAULT_IMPLICIT_BLOCK_COUNT)) {
                return@fromCallable sourceFollowBlockEntity
            } else {
                return@fromCallable null
            }
        }
    }


    companion object {
        const val BLOCK_ITEM = "blockItem"
    }
}

class ExplicitFollowBlockTriggerUsecase @Inject constructor(
    private val followBlockRecoDao: FollowBlockRecoDao,
    private val followEntityDao: FollowEntityDao
) : BundleUsecase<ExplicitWrapperObject?> {
    override fun invoke(p1: Bundle): Observable<ExplicitWrapperObject?> {
        val lang = p1.getString(LANG)
        val entityId = p1.getString(ITEM_ID)
        val asset = p1.getSerializable(ITEM) as? CommonAsset
        return Observable.fromCallable {
            if (lang.isNullOrEmpty() || entityId.isNullOrEmpty() || asset == null) {
                return@fromCallable null
            }

            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(it, lang)
                }

            if (followBlockLangConfig?.disableFollowBlockRecommendationAPICalls == true) {
                return@fromCallable null
            }

            val followConfig = followBlockLangConfig?.explicitSignal?.follow
            val blockConfig = followBlockLangConfig?.explicitSignal?.block


            val maxNumberOfFollowConfig =
                followConfig?.maxLifeTimeCap ?: Constants.DEFAULT_EXPLICIT_FOLLOW_MAX_CAP
            val maxNumberOfBlockConfig =
                blockConfig?.maxLifeTimeCap ?: Constants.DEFAULT_EXPLICIT_BLOCK_MAX_CAP
            if (followEntityDao.isBlocked(entityId) != null) {
                val blockCount =
                    followEntityDao.getSourceCountByAction(FollowActionType.BLOCK.name,null)

                if (FollowBlockPrefUtil.isInCoolOffPeriodExplicitBlockSignal(blockConfig)) {
                    return@fromCallable null
                }
                return@fromCallable if (maxNumberOfBlockConfig >= blockCount) ExplicitWrapperObject(asset,FollowActionType.BLOCK.name) else null

            } else if (followEntityDao.isFollowed(entityId) != null) {
                val followCount =
                    (followEntityDao.getSourceCountByAction(FollowActionType.FOLLOW.name, FollowActionType.JOIN.name))
                if (FollowBlockPrefUtil.isInCoolOffPeriodExplicitFollowSignal(followConfig)) {
                    return@fromCallable null
                }
                return@fromCallable if (maxNumberOfFollowConfig >= followCount) ExplicitWrapperObject(asset,FollowActionType.FOLLOW.name) else null

            }

            return@fromCallable null


        }
    }


    companion object {
        const val LANG = "lang"
        const val ITEM_ID = "itemId"
        const val ITEM = "item"
    }
}

class ColdSignalUseCase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao,
                                            private val followEntityDao: FollowEntityDao) :
    BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val lang = p1.getString(APP_LANG)

            if (lang.isNullOrEmpty()) {
                return@fromCallable false
            }
            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(it, lang)
                }

            if (followBlockLangConfig?.disableFollowBlockRecommendationAPICalls == true) {
                return@fromCallable false
            }

            val followConfig = followBlockLangConfig?.coldStart?.follow

            val maxColdSignalCount = FollowBlockPrefUtil.getColdFollowSignalInLifetime()
            if (maxColdSignalCount >= (followConfig?.maxLifeTimeCap
                    ?: Constants.DEFAULT_COLD_FOLLOW_MAX_CAP)
            ) {
                return@fromCallable false
            }

            val initialSessionsToSkp =
                followConfig?.initialSessionToSkip ?: Constants.DEFAULT_INITIAL_SESSION_TO_SKIP

            val totalSessionCounts = AppUserPreferenceUtils.getAppLaunchCount()
            if (totalSessionCounts != 0 && totalSessionCounts < initialSessionsToSkp) {
                return@fromCallable false
            }

            val coldFollowCount = PreferenceManager.getPreference(
                FollowBlockPreference.COLD_FOLLOW_SIGNAL_THIS_SESSION,
                0
            )

            if (coldFollowCount != 0) {
                return@fromCallable false
            }

            val sessionCounts = FollowBlockPrefUtil.getMaxSessions()

            val maxSessionCounts = followConfig?.minSessionGap ?: Constants.DEFAULT_MIN_SESSION_GAP

            if (FollowBlockPrefUtil.isInColdCoolOffPeriod(followConfig) && (sessionCounts != 0
                        && sessionCounts <= maxSessionCounts)
            ) {
                return@fromCallable false
            }

            val followCount =
                followEntityDao.getSourceCountByAction(FollowActionType.FOLLOW.name,FollowActionType.JOIN.name)

            val maxFollowEntityCount =
                followConfig?.maxFollowCapCS ?: Constants.DEFAULT_COLD_SIGNAL_FOLLOW_MAX_CAP
            if (followCount >= maxFollowEntityCount) {
                return@fromCallable false
            }

            return@fromCallable true

        }


    }


    companion object {
        const val APP_LANG = "appLang"
    }
}


class MinCardPositionUseCase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao) :
    Usecase<String, Int?> {
    override fun invoke(p1: String): Observable<Int?> {
        return Observable.fromCallable {
            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(
                        it,
                        lang = p1
                    )
                }
            return@fromCallable followBlockLangConfig?.explicitSignal?.follow?.minCardPosition ?:Constants.DEFAULT_EXPLICIT_SIGNAL_MIN_CARD_POSITION
        }
    }

}

class CardPositionUseCase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao) :
    Usecase<String, Int?> {
    override fun invoke(p1: String): Observable<Int?> {
        return Observable.fromCallable {
            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(
                        it,
                        lang = p1
                    )
                }
            return@fromCallable followBlockLangConfig?.coldStart?.follow?.cardPosition ?:Constants.DEFAULT_COLD_SIGNAL_CARD_POSITION
        }
    }
}

class BottomBarDurationUseCase @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao) :
    Usecase<String, Int?> {
    override fun invoke(p1: String): Observable<Int?> {
        return Observable.fromCallable {
            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(
                        it,
                        lang = p1
                    )
                }
            return@fromCallable followBlockLangConfig?.implicitSignal?.bottomDrawerDuration ?:Constants.DEFAULT_IMPLICIT_BOTTOM_BAR_DURATION
        }
    }
}

class ResetTotalSessionCountsSessions @Inject constructor(private val followBlockRecoDao: FollowBlockRecoDao) :
    BundleUsecase<Unit> {
    override fun invoke(p1: Bundle): Observable<Unit> {
        return Observable.fromCallable {
            val lang = p1.getString(APP_LANG)

            if (lang.isNullOrEmpty()) {
                return@fromCallable
            }
            val followBlockLangConfig =
                followBlockRecoDao.getSourceFollowBlockEntity(FOLLOW_BLOCK_CONFIG_ID)?.configData?.let {
                    FollowBlockPrefUtil.getConfigFromLangType(it, lang)
                }
            val followConfig = followBlockLangConfig?.coldStart?.follow

            val sessionCounts = FollowBlockPrefUtil.getMaxSessions()

            val maxSessionCounts = followConfig?.minSessionGap ?: Constants.DEFAULT_MIN_SESSION_GAP

            if (sessionCounts > maxSessionCounts) {
                FollowBlockPrefUtil.resetColdMaxSessionPref()
                return@fromCallable
            }
        }
    }

    companion object {
        const val APP_LANG = "appLang"
    }

}

data class ExplicitWrapperObject(val data:CommonAsset? = null, val action:String? = null):Serializable






