/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.common.ui.fragment.MenuFragment
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationEventPublisher
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dhutil.toArrayList
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Amitkumar
 */
class NHJsInterfaceWithMenuClickHandling @Inject constructor(private val dislikeDao: DislikeDao,
                                                             private val fetchDao: FetchDao,
                                                             @Named("section")
                                                             private val section: String?):MenuJsInterface, NavigationEventPublisher {

    var pageReferrer: PageReferrer? = null
    private var navigationEventTarget: Long = 0L

    @JavascriptInterface
    override fun onMenuButtonClick(postJson: String,
                          locationId: String,
                          menuLocation: String,
                          entityId: String) {
        Logger.d(LOG_TAG, "On Menu button click")
        try {
            val gson = Gson()
            val type = object : TypeToken<PostEntity>() {}.type
            val post = gson.fromJson<PostEntity>(postJson, type) ?: return
            val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
            intent.putStringArrayListExtra(Constants.BUNDLE_POST_IDS, listOf(post.i_id()).toArrayList())
            intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, MenuLocation.valueOf(menuLocation))
            intent.putExtra(NewsConstants.DH_SECTION, section)
            intent.putExtra(Constants.BUNDLE_STORY, post)
            intent.putExtra(Constants.BUNDLE_LOCATION_ID, locationId)
            intent.putExtra(Constants.BUNDLE_ENTITY_ID, entityId)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null, targetId = navigationEventTarget))
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    @JavascriptInterface
    override fun canShowMenuButton(): Boolean {
        return true
    }

    @JavascriptInterface
    override fun isStoryRead(postsJson: String): String {
        try {
            val gson = Gson()
            val type = object : TypeToken<List<PostEntity>>() {}.type
            val posts = gson.fromJson<List<PostEntity>>(postsJson, type)
            val readPosts = fetchDao.fetchReadPosts(posts.map { it.id })
            val result = posts.map { readPosts.contains(it.id) }
            return JsonUtils.toJson(result)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    @JavascriptInterface
    override fun isStoryDisliked(postsJson: String): String {
        try {
            val gson = Gson()
            val type = object : TypeToken<List<PostEntity>>() {}.type
            val posts = gson.fromJson<List<PostEntity>>(postsJson, type)
            val dislikedPosts = dislikeDao.getAllDislikedFrom(posts.map { it.id })
            val result = posts.map { dislikedPosts.contains(it.id) }
            return JsonUtils.toJson(result)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }
    override fun setTargetId(uniqueId: Long) {
        navigationEventTarget = uniqueId
    }
    companion object {
        private const val LOG_TAG = "NHJsInterfaceWithMenuClickHandling"
        @JvmField
        val JS_MENU_ACTION:MutableLiveData<Bundle> = MutableLiveData()
        const val INTERFACE_NAME="menu"
        @JvmStatic
        fun create(section: String?, uniqueId: Long) = with(SocialDB.instance()) {
            NHJsInterfaceWithMenuClickHandling(dislikeDao(), fetchDao(), section).also {
                it.navigationEventTarget = uniqueId
            }
        }

        @JvmStatic
        fun bundleToCommand(bundle: Bundle?): String? {
            val card = bundle?.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset ?: kotlin.run {
                Logger.e(LOG_TAG, "Post can not be null")
                return null
            }
            val action = bundle.getString(MenuFragment.ARG_JS_ACTION_KEY)
            if (action == MenuFragment.ARG_JS_ACTION_HIDE_STORY) {
                try {
                    val jsonData = JsonObject()
                    jsonData.addProperty("format", card.i_format()?.name)
                    jsonData.addProperty("id", card.i_id())
                    jsonData.addProperty("uiType", card.i_uiType()?.name)
                    jsonData.addProperty("subFormat", card.i_subFormat()?.name)
                    val command = NHWebViewUtils.formatScript("hideStory", jsonData)
                    return command
                } catch (e: Exception) {
                    Logger.caughtException(e)
                }
            }
            return null
        }
    }
}