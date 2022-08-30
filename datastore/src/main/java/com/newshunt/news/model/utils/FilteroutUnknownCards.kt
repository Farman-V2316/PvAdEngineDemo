/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import android.os.SystemClock
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.util.LangInfoRepo
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.model.sqlite.SocialDB
import javax.inject.Inject
import javax.inject.Named

/**
 * @author satosh.dhanyamraju
 */
class FilteroutUnknownCards @Inject constructor(@Named("listType")
                                                private val listType: String?,
                                                @Named("listTransformType")
                                                private val listTransformType: ListTransformType,
                                                private val invalidCardsLogger: InvalidCardsLogger,
                                                private val cardDao: CardDao,
                                                ): TransformNewsList {
    val TAG = "FilteroutUnknownCards"

    override fun transf(list: List<AnyCard>): List<AnyCard> {
        return when (listType) {
            Format.MEMBER.name -> list
            Format.GROUP_INVITE.name -> list
            Format.ENTITY.name -> list
            else -> {
                val postList = list.filterIsInstance<PostEntity>()
                when (listTransformType) {
                    ListTransformType.PROFILE_ACTIVITIES -> {
                        handleActivitiesTransformations(postList)
                    }
                    ListTransformType.PROFILE_SAVED -> {
                        handleSavedTransformations(postList)
                    }
                    ListTransformType.PROFILE_SAVED_CAROUSEL -> {
                        handleSavedCarouselTransformations(postList)
                    }
                    ListTransformType.DEFAULT -> {
                        handleDefaultTransformations(postList)
                    }
                }
            }
        }
    }

    private fun handleDefaultTransformations(postList: List<PostEntity>): List<PostEntity> {
        Logger.d(TAG, "handleDefaultTransformations: blocks = ${cardDao.blockedSourceIds()}")
        val p1 = postList.mapNotNull {
            when {
                it.i_format() == Format.TICKER -> it.copy(localLastTickerRefreshTime = SystemClock
                        .uptimeMillis())
                it.i_format() == Format.POST_COLLECTION && it.i_uiType() != UiType2.CAROUSEL_6
                        && it.i_collectionItems().isNullOrEmpty() -> {
                    Logger.e(TAG, "removed empty col. id=${it.i_id()}")
                    invalidCardsLogger.log("empty collection", "id:${it.i_id()}")
                    null
                }
                it.i_subFormat() == SubFormat.WEB_ADJUNCT && !it.i_adjunctLang().isNullOrEmpty() && LangInfoRepo.isUserOrSystemOrBlackListedLanguageForWebCard(it.i_adjunctLang()!!) -> {
                    Logger.e(TAG,"removed adjunct web card")
                    null
                }
                else -> it
            }
        }
        val p2 = modifyMoreStoriesBasedOnBlocks(p1)
        return  p2
    }

    /**
     * if a card-having-more-stories is from a blocked source,
     * replace it with the first card, whose source is not blocked, from its moreStories
     */
    private fun modifyMoreStoriesBasedOnBlocks(list: List<PostEntity>): List<PostEntity> {
        /*
        1. Create a list of card + moreStories,
        2. delete items from blocked sources
        3. In the remaining list, 1st card should be the main card, other cards are its moreStories,
            if resultant size is within limits. If not, first card will be hero without moreStories
        */
        val blockedSrcIds = cardDao.blockedSourceIds() // we just need to check id; as per CardsAdapter
        val newList = mutableListOf<PostEntity>()
        list.forEach {
            if (it.source?.id in blockedSrcIds && !it.moreStories.isNullOrEmpty()) {
                val l1 = mutableListOf(it)
                l1.addAll(it.moreStories?: emptyList())
                it.moreStories = null
                Logger.d(TAG, "modifyMoreStoriesBasedOnBlocks: updating morestories of ${l1.map { it.i_id() }}")
                val iter = l1.iterator()
                while (iter.hasNext()) {
                    val item = iter.next()
                    if(item.source?.id in blockedSrcIds)
                        iter.remove()
                }
                Logger.d(TAG, "modifyMoreStoriesBasedOnBlocks: after removing blocks ${l1.map { it.i_id() }}")
                if(l1.size == 1) {
                    newList.add(l1.first())
                } else if(l1.size > 1) { // size > 0
                    val prefMin = PreferenceManager.getInt(Constants.PREF_MIN_MORE_STORIES_COUNT, Constants.DEFAULT_MIN_MORE_STORIES_COUNT)
                    val prefMax = PreferenceManager.getInt(Constants.PREF_MAX_MORE_STORIES_COUNT, Constants.DEFAULT_MAX_MORE_STORIES_COUNT)
                    // if min and max are satisfied, it will have moreStories. Else, it will be normal card
                    if ((l1.size - 1) in prefMin..prefMax) { // l1 includes parent card too. So need to do size - 1
                        val hero = l1.first()
                        val ms = mutableListOf<PostEntity>()
                        ms.addAll(l1.subList(1, l1.size))
                        newList.add(
                            hero.copy(
                                moreStories = ms,
                                moreCoverageIcons = it.moreCoverageIcons,
                                moreCoverageCount = ms.size
                            )
                        )
                    } else { // not in the limits
                        newList.add(l1.first())
                    }
                }
            }
            else newList.add(it)
        }
        return newList
    }

    private fun handleActivitiesTransformations(postList: List<PostEntity>): List<PostEntity> {
        return postList.map {
            it.id = it.userInteraction?.activityId ?: it.id
            it
        }
    }

    private fun handleSavedTransformations(postList: List<PostEntity>): List<PostEntity> {
        val deletedBookmarks = SocialDB.instance().bookmarkDao().getBookmarkIds(BookMarkAction.DELETE)
        //Filter out the deleted bookmarks from the list
        return postList.filter {
            !deletedBookmarks.contains(it.id)
        }
    }

    private fun handleSavedCarouselTransformations(postList: List<PostEntity>): List<PostEntity> {
        val deletedBookmarks = SocialDB.instance().bookmarkDao().getBookmarkIds(BookMarkAction.DELETE)
        return postList.map { postEntity ->
            if (postEntity.format == Format.POST_COLLECTION) {
                //Filter out the deleted bookmarks from the list
                postEntity.collectionAsset?.collectionItem?.filter {
                    !deletedBookmarks.contains(it.id)
                }
            }
            postEntity
        }
    }
}