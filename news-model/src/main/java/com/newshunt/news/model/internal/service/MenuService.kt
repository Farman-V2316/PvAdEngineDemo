/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.news.model.entity.DisplayLocation
import com.newshunt.dataentity.news.model.entity.MenuEntity
import com.newshunt.dhutil.Expirable
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.helper.DislikeStoryCardEvent
import com.newshunt.news.helper.DislikeStoryHelper
import com.newshunt.news.model.internal.rest.PostDislikeApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author satosh.dhanymaraju
 */

private const val LOG_TAG = "MenuServiceImpl"

interface MenuService {
    fun dislikePost(menuEntity: Expirable<MenuEntity>, postUrl: String)

    fun allDisliked(): List<Expirable<MenuEntity>>

    fun migrateForUpgradeCases()

    fun onExit()

    fun legacyDislike(dislikeStoryCardEvent: DislikeStoryCardEvent)

    fun dislikeLocal(menuEntity: Expirable<MenuEntity>)
}


/**
 * Stores disliked stories.
 *
 * Do not instantiate directly; use Single instance provided by DislikeModule
 */
class MenuServiceImpl(initialDislikes: List<Expirable<MenuEntity>>,
                      private val runner: (() -> Unit) -> Unit,// for synchronising writes to single thread
                      private val apiCall: PostDislikeApi,
                      private val saveToPref: (List<Expirable<MenuEntity>>) -> Unit) : MenuService {
    // its a list not set. duplicates allowed - in case reco serves the card again and user
    // dislikes with different options.
    private var dislikes: List<Expirable<MenuEntity>> = initialDislikes
        private set(value) {
            runner {
                field = value
                saveToPref(value)
            }
        }

    override fun dislikePost(menuEntity: Expirable<MenuEntity>, postUrl: String) {
        apiCall.postDislike(menuEntity.value, postUrl).enqueue(object :
                Callback<ApiResponse<Any>> {
            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                Logger.d(LOG_TAG, "dislikePost: success")
            }

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                Logger.d(LOG_TAG, "dislikePost: error")
            }
        })
    }

    override fun dislikeLocal(menuEntity: Expirable<MenuEntity>) {
        // add, prune, save
        Logger.d(LOG_TAG, "local dislike ${menuEntity.value.itemId}")
        dislikes = (dislikes.filterNot { it.value.uniqueId == menuEntity.value.uniqueId } +
                listOf(menuEntity)).map { if (it.isExpired()) it.map { it.minimal() } else it }
    }

    override fun allDisliked(): List<Expirable<MenuEntity>> = dislikes

    override fun migrateForUpgradeCases() {
        // create pojo with property migrationv0_1 : true (when done)
        val migrated = PreferenceManager.getPreference(AppStatePreference.DISLIKE_MIGRATION_0_1, false)
        if (migrated) {
            return
        }
        val prefMap: Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue> = readPref()
        dislikes = prefMap.entries.map {
            it.value.dislikeStoryCardEvent?.toEntity()
                    ?: Expirable.fromTTL(0,
                            MenuEntity(
                                    it.key.storyId,
                                    if (it.key.groupId == "-1") null else it.key.groupId,
                                    "",
                                    -1,
                                    "",
                                    DisplayLocation.CARD_EXTERNAL
                                    , -1L,
                                    "NA",
                                    listOf(),
                                    listOf(),
                                    null,
                                    it.value.isPerSession
                            ))
        }
        // set migrated
        PreferenceManager.savePreference(AppStatePreference.DISLIKE_MIGRATION_0_1, true)
        //PreferenceManager.remove(AppStatePreference.DISLIKED_STORY_IDS)
    }

    override fun onExit() {
        dislikes = dislikes.filterNot { it.value.isPerSession }
    }


    override fun legacyDislike(dislikeStoryCardEvent: DislikeStoryCardEvent) {
        val newEntity = dislikeStoryCardEvent.toEntity()
        dislikes = dislikes.filterNot { it.value.uniqueId == newEntity.value.uniqueId } + listOf(newEntity)
    }

    private fun DislikeStoryCardEvent.toEntity(): Expirable<MenuEntity> {
        val e = MenuEntity(itemId = storyId ?: "", groupId = groupId, groupType = groupType
                ?: "", uiType = uiType ?: "NA", createdAt = timeStamp ?: -1)
        return Expirable(timeStamp ?: -1, -1, e)
    }

    private fun readPref(): Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue> {
        val dislikePrefMap = PreferenceManager.getPreference(AppStatePreference.DISLIKED_STORY_IDS, Constants.EMPTY_STRING)
        val type = object : TypeToken<Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue>>() {}.type
        if (CommonUtils.isEmpty(dislikePrefMap)) return emptyMap()
        return Gson().fromJson(dislikePrefMap, type)
    }
}