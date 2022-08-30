/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.ColdStartEntity
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.Discussions
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.SavedCard
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.COL_ACTION
import com.newshunt.dataentity.model.entity.COL_FORMAT
import com.newshunt.dataentity.model.entity.TABLE_BOOKMARKS
import com.newshunt.dataentity.social.entity.AdInsertFailReason
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.AdSpecEntity
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.NullableTopLevelCard
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dataentity.social.entity.TABLE_CARD
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dataentity.social.entity.VIEW_AssocationsChildren
import com.newshunt.dataentity.social.entity.VIEW_DetailCard
import com.newshunt.dataentity.social.entity.VIEW_DiscussionsChildren
import com.newshunt.dataentity.social.entity.VIEW_FeedPage
import com.newshunt.dataentity.social.entity.VIEW_TLCard
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.isUrlThatHasAllowLocalCardsParam
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.utils.DedupUtil
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.util.ResetDedupHelperEvent
import io.reactivex.Observable
import kotlin.system.measureTimeMillis
import org.intellij.lang.annotations.Language

/**
 * This class is not extending base dao because it interacts with multiple entities. see
 * [replaceFirstPage]
 */
@Dao
abstract class FetchDao : ListFetchDao<TopLevelCard> {

    // ------------------------------------------------------------------------------------------------------------------------------------
    // SELECT
    // ------------------------------------------------------------------------------------------------------------------------------------

    /*override fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int, Card> {
        val fetchid= fetchInfo1(pageId, location, section)?.fetchInfoId
        val url = contentUrlOf(pageId, section)
        return itemsMatching(fetchid, url)
    }*/


    @Query("""
        SELECT DISTINCT 
            p.uniqueId AS uniqueId,
            p.id AS id,
            p.format AS format,
            p.level AS level,
            p.local_cpId AS local_cpId,
            p.local_creationDate AS local_creationDate,
            p.local_fetchedFromServer AS local_fetchedFromServer,
            p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
            p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
            p.local_location AS local_location,
            p.local_nextCardId AS local_nextCardId,
            p.local_pageId AS local_pageId,
            p.local_progress AS local_progress,
            p.local_section AS local_section,
            p.local_shownInForyou AS local_shownInForyou,
            p.local_status AS local_status,
            p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
            p.moreStoryCount AS moreStoryCount,
            p.postEntity AS postEntity,
            p.shareUrl AS shareUrl,
            p.source AS source,
            p.src_entityType AS src_entityType,
            p.src_id AS src_id,
            p.subFormat AS subFormat,
            p.thumbnailInfos AS thumbnailInfos,
            p.title AS title,
            p.adId AS adId,
            p.ignoreSourceBlock as ignoreSourceBlock
        FROM fetch_data f
        INNER JOIN $VIEW_TLCard p ON f.storyId = p.uniqueId
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1)
        AND ((((src_id NOT IN 
              (SELECT entityId FROM follow WHERE entityId= src_id AND entityType = src_entityType AND `action`= 'BLOCK')) 
                AND (SELECT entityType FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId) != 'SOURCECAT')
          OR (SELECT entityType FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId) = 'SOURCECAT')
          OR (ignoreSourceBlock = 1))
          AND p.id NOT IN
            (SELECT postId
             FROM dislikes)
          AND p.id NOT IN
            (SELECT postId
             FROM localdelete)
          AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId
           AND $VIEW_FeedPage.section = :section)
        ORDER BY f.pageNum ASC,
                 f.indexInPage ASC
           """)
    abstract override fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int,TopLevelCard>

    @Query("""
        
        SELECT COUNT(*) FROM(SELECT DISTINCT 
            p.uniqueId AS uniqueId,
            p.id AS id,
            p.format AS format,
            p.level AS level,
            p.local_cpId AS local_cpId,
            p.local_creationDate AS local_creationDate,
            p.local_fetchedFromServer AS local_fetchedFromServer,
            p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
            p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
            p.local_location AS local_location,
            p.local_nextCardId AS local_nextCardId,
            p.local_pageId AS local_pageId,
            p.local_progress AS local_progress,
            p.local_section AS local_section,
            p.local_shownInForyou AS local_shownInForyou,
            p.local_status AS local_status,
            p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
            p.moreStoryCount AS moreStoryCount,
            p.postEntity AS postEntity,
            p.shareUrl AS shareUrl,
            p.source AS source,
            p.src_entityType AS src_entityType,
            p.src_id AS src_id,
            p.subFormat AS subFormat,
            p.thumbnailInfos AS thumbnailInfos,
            p.title AS title,
            p.adId AS adId
        FROM fetch_data f
        INNER JOIN $VIEW_TLCard p ON f.storyId = p.uniqueId
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1)
          AND p.id NOT IN
            (SELECT postId
             FROM dislikes)
          AND p.id NOT IN
            (SELECT postId
             FROM localdelete)
          AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId
           AND $VIEW_FeedPage.section = :section)
        ORDER BY f.pageNum ASC,
                 f.indexInPage ASC)
           """)

    abstract  fun itemsMatchingUnblockedCount(pageId: String, location: String, section: String): Int


    @Query("""
        SELECT DISTINCT p.uniqueId, p.id, p.*
        FROM fetch_data f
        LEFT JOIN $VIEW_TLCard p ON f.storyId = p.uniqueId
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1)
          AND p.id NOT IN
            (SELECT postId
             FROM dislikes) 
          AND p.id NOT IN
            (SELECT postId
             FROM localdelete)
          AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId
           AND $VIEW_FeedPage.section = :section)
        ORDER BY f.pageNum ASC,
                 f.indexInPage ASC
           """)
    abstract fun _itemsMatchingLiveList(pageId: String, location: String, section: String): LiveData<List<NullableTopLevelCard>>

    fun itemsMatchingLiveList(pageId: String, location: String, section: String): LiveData<List<TopLevelCard>>  {
        return Transformations.map(_itemsMatchingLiveList(pageId, location, section)) {
            it.mapNotNull { card->
                val pe = card.postEntity
                if(pe==null) Logger.e(TAG, "itemsMatchingLiveList: Got a null")
                pe?.let { TopLevelCard(it) }
            }
        }
    }


    @Query("""
        SELECT DISTINCT p.uniqueId, p.id, p.*,
        CASE
        WHEN count_format.count is null then 0
        else count_format.count
        END as count_story_value
        FROM fetch_data f
        LEFT JOIN $VIEW_TLCard p ON f.storyId = p.uniqueId
        LEFT JOIN (SELECT COUNT($COL_FORMAT) count, $COL_FORMAT FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = 'ADD' GROUP BY $COL_FORMAT) as count_format ON count_format.$COL_FORMAT = p.subFormat
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1)
          AND p.id NOT IN
            (SELECT postId
             FROM dislikes)
          AND p.id NOT IN
            (SELECT postId
             FROM localdelete)
          AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId
           AND $VIEW_FeedPage.section = :section)
           AND f.storyId = p.uniqueId
        ORDER BY f.pageNum ASC,
                 f.indexInPage ASC
        LIMIT :limit
           """)
    abstract fun readBookmarkedItems(pageId: String, location: String, section: String, limit: Int): DataSource.Factory<Int, SavedCard>

    @Query("""
        SELECT DISTINCT p.id as id, p.video_assetId as video_assetId, p.title as title, 
        p.mm_includeCollectionInSwipe as mm_includeCollectionInSwipe,
        p.source as source, p.subFormat as subformat, p.format as format, p.shareUrl as shareUrl, p.contentImageInfo as imageDetails, p.level as level, 
        p.moreStoryCount as moreStoryCount, p.langCode as langCode, p.adId as adId
        FROM fetch_data f
        INNER JOIN (select uniqueId, id, video_assetId, title, format, source,
        mm_includeCollectionInSwipe, contentImageInfo, subFormat, src_entityType, moreStoryCount,
        shareUrl, level, src_id, langCode, adId from $TABLE_CARD) p
        ON f.storyId = p.uniqueId
        WHERE f.fetchId in (SELECT fi.col_fetchInfoId FROM fetch_info fi
          WHERE fi.col_entity_id = :fetchId AND fi.col_disp_loc = :location AND fi.section = :section)
           AND f.reqUrl IN
            (SELECT contentUrl
             FROM $VIEW_FeedPage
             WHERE $VIEW_FeedPage.id = :fetchId
             AND $VIEW_FeedPage.section = :section)
        ORDER BY f.pageNum ASC, f.indexInPage ASC
  """)
    abstract fun detailList(fetchId: String, location: String, section: String):
            LiveData<List<DetailListCard>>

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE uniqueId in 
        (SELECT uniqueId from card WHERE uniqueId in
          (SELECT storyId from fetch_data WHERE fetchId in (SELECT col_fetchInfoId from fetch_info 
          WHERE col_entity_id = :entityId AND section = :section AND col_disp_loc = :location)))
  """)
    abstract fun cardList(entityId: String, location: String, section: String): LiveData<List<Card>>

    @Query("""
        SELECT DISTINCT 
            p.uniqueId AS uniqueId,
            p.id AS id,
            p.format AS format,
            p.level AS level,
            p.local_cpId AS local_cpId,
            p.local_creationDate AS local_creationDate,
            p.local_fetchedFromServer AS local_fetchedFromServer,
            p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
            p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
            p.local_location AS local_location,
            p.local_nextCardId AS local_nextCardId,
            p.local_pageId AS local_pageId,
            p.local_progress AS local_progress,
            p.local_section AS local_section,
            p.local_shownInForyou AS local_shownInForyou,
            p.local_status AS local_status,
            p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
            p.moreStoryCount AS moreStoryCount,
            p.postEntity AS postEntity,
            p.shareUrl AS shareUrl,
            p.source AS source,
            p.src_entityType AS src_entityType,
            p.src_id AS src_id,
            p.subFormat AS subFormat,
            p.thumbnailInfos AS thumbnailInfos,
            p.title AS title,
            p.adId AS adId,
            p.ignoreSourceBlock as ignoreSourceBlock
        FROM fetch_data f
        INNER JOIN $VIEW_TLCard p ON f.storyId = p.uniqueId
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :entityId and col_disp_loc= :location and section=:section LIMIT 1)
        ORDER BY f.pageNum ASC,
                 f.indexInPage ASC
           """)
    abstract fun opCardList(entityId: String, location: String, section: String): LiveData<List<TopLevelCard>>

    @Query("""SELECT * from  $TABLE_CARD where uniqueId In (SELECT parentId from discussions d
            where child_id =:childId) LIMIT 1 """)
    abstract fun _getDiscussionParent(childId: String): Card?

    open fun getDiscussionParent(childId: String): PostEntity? =
            _getDiscussionParent(childId)?.rootPostEntity()

    @Query("SELECT * FROM ad_spec WHERE entityId IN (:entityIds)")
    abstract fun adSpecOf(entityIds: List<String>): LiveData<List<AdSpecEntity>>

    @Query("SELECT * FROM ad_spec WHERE entityId IN (:entityIds) AND section IN (:section, '${AdSpecEntity.SECTION_ANY}')")
    abstract fun adSpecOf(entityIds: List<String>, section: String): LiveData<List<AdSpecEntity>>

    @Query("""
        SELECT min(pageNum)
        FROM fetch_data
        WHERE fetchId = :fetchId
    """)

    @VisibleForTesting
    internal abstract fun firstPageNumOf(fetchId: Long): Int?

    @Query("""
        SELECT *
        FROM $TABLE_CARD
        WHERE uniqueId IN
            (SELECT storyId
             FROM fetch_data
             WHERE fetchId IN
                 (SELECT col_fetchInfoId
                  FROM fetch_info
                  WHERE col_entity_id = :entityId
                    AND col_disp_loc = :location
                    AND SECTION = :section)
               AND format = 'TICKER')
    """)
    internal abstract fun _tickersOf(entityId: String, location: String, section: String): LiveData<List<Card>>

    open fun tickersOf(entityId: String, location: String, section: String): LiveData<List<PostEntity>> {
        return Transformations.map(_tickersOf(entityId, location, section)) {
            it.mapNotNull { it.rootPostEntity() }
        }
    }


    @Query("SELECT MAX(pageNum) FROM fetch_data WHERE fetchId IN (select col_fetchInfoId FROM fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section)")
    abstract fun getMaxPageNumber(pageId: String, location: String, section: String): Int?

    @Query("""SELECT id from history where id in (:postId)""")
    abstract fun fetchReadPosts(postId: List<String>): List<String>

    @Query("select * from fetch_data where storyId  IN (SELECT uniqueId FROM $TABLE_CARD WHERE id=:postId) AND fetchId=:fetchId")
    abstract fun fetchDataByPostId(postId: String, fetchId: Long): List<FetchDataEntity>

    @Query("""SELECT * FROM fetch_data WHERE fetchId=:fetchId ORDER BY pageNum, indexInPage LIMIT 1""")
    abstract fun fetchData0(fetchId: Long): FetchDataEntity?


    @Query("""
    select * from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1
  """)
    abstract fun fetchInfo(pageId: String, location: String, section: String): FetchInfoEntity?

    @Query("select max(pageNum) + 1 from fetch_data where fetchId = :fetchId")
    abstract fun maxPageNumInFetchData(fetchId: Long): Int?


    // for tests
    @Query("select * from fetch_info")
    abstract fun allFetchInfo(): LiveData<List<FetchInfoEntity>>

    @Query("select * from fetch_data")
    abstract fun allFetchData(): LiveData<List<FetchDataEntity>>

    @Query("""
        SELECT contentUrl FROM $VIEW_FeedPage as fpv, fetch_info as fi
         WHERE fi.col_fetchInfoId = :fetchId AND fpv.id = fi.col_entity_id AND fpv.section = fi.section LIMIT 1
            """)
    abstract fun getContentUrl(fetchId: Long): String?

    @Query("""
        SELECT count(*)
        FROM fetch_data f
        WHERE f.fetchId = :fetchId
          AND f.storyId NOT IN (SELECT uniqueId FROM $TABLE_CARD WHERE id IN (SELECT postId FROM dislikes))
  """)
    abstract fun cardCount(fetchId: Long): Int

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE id = :postId
  """)
    abstract fun detailCardByPostId(postId: String): LiveData<DetailCard?>

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE id = :postId and level = :level
  """)
    abstract fun detailCardLiveByPostIdLevel(postId: String,level: String ): LiveData<DetailCard?>

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE uniqueId in 
        (SELECT uniqueId from card WHERE id  = :postId AND (:adId IS NULL OR adId = :adId) AND
         uniqueId in (SELECT storyId from fetch_data WHERE fetchId in 
        (SELECT col_fetchInfoId from fetch_info 
          WHERE col_entity_id = :entityId AND section = :section AND
           (col_disp_loc = :location OR col_disp_loc = :listLocation))))
  """)
    abstract fun detailCardByFetchInfo(entityId : String, section : String, postId : String,
                                       location : String, listLocation: String, adId: String?) : LiveData<DetailCard?>

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE id = :postId and level = :level
  """)
    abstract fun detailCardByPostIdLevel(postId: String,level: String ): DetailCard?

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE uniqueId = :uniqueId and level = :level
  """)
    abstract fun detailCardByUniqueIdLevel(uniqueId: String, level: String): DetailCard?

    @Query("""
        SELECT * FROM $VIEW_DetailCard WHERE uniqueId = :uniqueId
  """)
    abstract fun detailCardLiveByUniqueId(uniqueId: String): LiveData<DetailCard?>

    @Query("""
        SELECT * FROM $VIEW_DiscussionsChildren WHERE parentId = :uniqueId and level <> 'LOCAL'
    """)
    abstract fun discussionsForPost(uniqueId: String): LiveData<List<AllLevelCards>>

    @Query("""
        SELECT * FROM $VIEW_DiscussionsChildren WHERE parentId = :uniqueId and level <> 'LOCAL'
    """)
    abstract fun discussionsListForPost(uniqueId: String): List<AllLevelCards>

    @Query("""
        SELECT * FROM $VIEW_AssocationsChildren WHERE parentId = :uniqueId and level = 'ASSOCIATION'
    """)
    abstract fun associationForPost(uniqueId: String): LiveData<List<AllLevelCards>>

    @Query("""
        SELECT * FROM $VIEW_TLCard WHERE id = :postId
  """)
    abstract fun cardByPostId(postId: String): LiveData<TopLevelCard?>

    @Query("""
       SELECT postId FROM dislikes 
    """)
    abstract fun getDislikeStories() : LiveData<List<String>>


    @Query("""
        select * from interactions where entity_id  = :postId AND isSynced = 1 AND col_action IN (:interestedInteractions)
        """)
    abstract fun getInteractionsCount(postId: String, interestedInteractions : List<String>): LiveData<Interaction?>

    @Query("""
        SELECT * FROM additional_contents WHERE postId = :postId and type =:contentType """)
    abstract fun fetchAdditionalContents(postId: String, contentType: String):
            Observable<AdditionalContents?>


    @Query(" SELECT postEntity FROM $TABLE_CARD where id = :postId")
    abstract fun lookupById(postId: String): PostEntity?

    @Query("""
     SELECT p.*  FROM related_list r
      LEFT JOIN  $TABLE_CARD p ON r.id= p.id AND  p.level = :level
      LEFT JOIN follow fl ON fl.entityId = p.src_id AND fl.entityType = p.src_entityType
      LEFT JOIN votes v ON p.id = v.pollId
      LEFT JOIN interactions intr ON p.id = intr.entity_id AND intr.actionToggle = 1 AND intr.col_action in ('LIKE', 'LOVE', 'HAPPY', 'WOW', 'SAD', 'ANGRY')
      WHERE r.postid IN (SELECT uniqueId FROM $TABLE_CARD c LEFT JOIN fetch_data f ON c.uniqueId = f.storyId 
        WHERE c.id=:postId AND f.fetchId IN (SELECT col_fetchInfoId FROM fetch_info WHERE
            col_entity_id = :entityId AND col_disp_loc = :location AND section = :section))
      ORDER BY r.indexInPage ASC
""")
    abstract fun _related(postId: String, level: PostEntityLevel, entityId: String, location: String, section: String): LiveData<List<NullableTopLevelCard>>


    fun related(postId: String, level: PostEntityLevel, entityId: String, location: String, section: String): LiveData<List<TopLevelCard>> {
        return Transformations.map(_related(postId, level, entityId, location, section)) {
            it.mapNotNull {
                val pe = it.postEntity
                if (pe == null) {
                    Logger.e(TAG, "related: Got a null")
                }
                pe?.let {
                    TopLevelCard(it)
                }
            }
        }
    }

    /* TODO@amit.chaudhary
    * Visible for testing of trigger. suspect to get removed before release.
    * */
    @Query("select * from $TABLE_CARD")
    abstract fun all(): List<Card>

    @Query("select * from $TABLE_CARD p WHERE p.id = :postId ")
    abstract fun allPostWithid(postId: String): List<Card>

    @Query("select contentUrl from $VIEW_FeedPage where id = :pageId")
    abstract fun contentUrlForPage(pageId: String): String

    @Query("select * from $VIEW_FeedPage where id = :pageId AND section=:section")
    abstract fun lookupPage(pageId: String, section: String): FeedPage?

    @Query("select * from fetch_info where col_entity_id=:pageId and section = :section")
    abstract fun lookupFetchInfo(pageId: String?, section: String?): List<FetchInfoEntity>

    @Query("select * from $VIEW_FeedPage where id = :pageId AND section=:section")
    abstract fun LiveDataLookupPage(pageId: String, section: String): LiveData<FeedPage?>


    /* TODO@amit.chaudhary
    * Visible for testing of trigger. suspect to get removed before release.
    * */
    @VisibleForTesting
    @Query("select * from discussions")
    abstract fun allDiscussionRelation(): List<Discussions>

    @Query("SELECT id FROM $TABLE_CARD WHERE id IN (:idList) AND level='TOP_LEVEL'")
    abstract fun getPostList(idList: List<String>): List<String>?

    private fun fillLocalColdStartChildren(posts: List<PostEntity>, followEntityDao: FollowEntityDao): List<PostEntity> {
        posts.forEach { post ->
            if (post.coldStartAsset?.itemToFillFilter != null) {
                val filter = post.coldStartAsset?.itemToFillFilter
                val action = filter?.action
                val entityType = filter?.entityType
                val itemsToAdd = if (filter != null && action != null && entityType != null) {
                    followEntityDao.getItemsToFillFilter(action, entityType, filter.entitySubType)
                            .map {
                                it.actionableEntity.toColdStartEntityItem()
                            }
                } else emptyList()
                val entity = post.coldStartAsset ?: ColdStartEntity()
                entity.coldStartCollectionItems = if (entity.coldStartCollectionItems.isNullOrEmpty()) {
                    itemsToAdd
                } else {
                    val coldStartCollectionItems = entity.coldStartCollectionItems?.toMutableList()
                    coldStartCollectionItems?.addAll(itemsToAdd)
                    coldStartCollectionItems
                }
            }
        }
        return posts
    }

    // ------------------------------------------------------------------------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insIgnore(fetchInfoEntity: FetchInfoEntity): Long

    @Transaction
    open fun insReplacePosts(t: List<PostEntity>, fetchId: String) {
       insReplacePosts(t.map { it.toCard2(fetchId)})
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insReplacePosts(t: List<Card>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insReplaceFetchData(t: List<FetchDataEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insReplaceAdSpec(adSpecEntity: List<AdSpecEntity>): List<Long>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insReplaceDiscussions(discussions: List<Discussions>)

    /**
     * We insert/replace because, we depend on detail page to cleanup, which may not happen, if it crashes.
     */
    @Query("""
        INSERT OR REPLACE INTO fetch_info(col_entity_id, col_disp_loc, nextPageUrl, currentPageNum, npUrlOf1stResponse, lastViewDestroyTs, section)
        SELECT col_entity_id,
               :location,
               nextPageUrl,
               currentPageNum,
               npUrlOf1stResponse,
               lastViewDestroyTs,
               section
        FROM fetch_info
        WHERE col_fetchInfoId = :fetchId AND section=:section
    """)
    abstract fun cloneFetchInfoForLocation(fetchId: Long, location: String, section: String): Long


    @Query("""
        INSERT INTO fetch_data(fetchId, pageNum, indexInPage, storyId, format, reqUrl, receivedTs)
        SELECT :newFetchid,
               f.pageNum,
               f.indexInPage,
               f.storyId,
               f.format,
               f.reqUrl,
               f.receivedTs
        FROM fetch_data f
        LEFT JOIN (SELECT uniqueId, id FROM card) p ON f.storyId = p.uniqueId
        WHERE fetchId = :oldFetchid AND format NOT IN ("AD")
        AND (p.id IN (:mustInclude)
                OR (p.id NOT IN (SELECT postId FROM dislikes)
                    AND p.id NOT IN (SELECT postId FROM localdelete)))
    """)
    abstract fun cloneFetchDataForDiffFetchId(newFetchid: Long, oldFetchid: Long, mustInclude: List<String> = emptyList()): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNLFCItem(item: NLFCItem)

    /**
     * should not set or change level of [postEntityList]
     *
     */
    private fun insertPostWithRelationships(postEntityList: List<PostEntity>, followEntityDao: FollowEntityDao, fetchId: String) {
        insReplacePosts(fillLocalColdStartChildren(postEntityList, followEntityDao), fetchId)
    }

    //------------ LOCAL cards ----------- ----------- ----------- ----------- -----------

    @Query("""
        SELECT * FROM $TABLE_CARD
        WHERE level = 'LOCAL' AND local_creationDate > :oldts
        ORDER BY local_creationDate DESC
    """
    )
    abstract fun getLocalCardsForMyPosts(oldts: Long): List<Card>

    @Query("""
        SELECT * FROM $TABLE_CARD
        WHERE (level = 'LOCAL_COMMENT' OR level = 'LOCAL') AND id in 
          (SELECT post_id from  cp_entity WHERE  parent_id = :parentPostId)
    """
    )
    abstract fun getLocalCardForParent(parentPostId: String): List<Card>?


/*
    @Query("""
        SELECT * FROM $TABLE_CARD
        WHERE level = 'LOCAL' AND local_creationDate > :oldts AND local_pageId = :pageId AND local_section = :section
        ORDER BY local_creationDate DESC
    """
    )
    abstract fun getLocalCardsForGroup(pageId: String, section: String, oldts : Long) : List<Card>
*/

    @Query("""
        SELECT * FROM $TABLE_CARD
        WHERE level = 'LOCAL' AND (local_shownInForyou = 0 OR local_shownInForyou IS NULL)
        ORDER BY local_creationDate DESC
    """
    )
    abstract fun getLocalCardsForForyou(): List<Card>

    @Query("""
        SELECT * FROM $TABLE_CARD
        WHERE level = 'LOCAL' AND (local_shownInForyou = 0 OR local_shownInForyou IS NULL)
        AND uniqueId NOT IN (SELECT storyId FROM fetch_data 
                        WHERE fetchId = (SELECT col_fetchInfoId FROM fetch_info
                                            WHERE col_entity_id = :pageId 
                                            AND section = :section AND col_disp_loc = :location))
        ORDER BY local_creationDate DESC
    """)
    abstract fun getLocalCardsForForyouNotPresentIn(
            pageId: String,
            location: String,
            section: String
    ): List<Card>

    @Query("""
               SELECT * FROM $TABLE_CARD
        WHERE level = 'LOCAL' AND local_status IN ('FAILED', 'UPLOADING')
        AND uniqueId NOT IN (SELECT storyId FROM fetch_data 
                        WHERE fetchId = (SELECT col_fetchInfoId FROM fetch_info
                                            WHERE col_entity_id = :pageId 
                                            AND section = :section AND col_disp_loc = :location))
        ORDER BY local_creationDate DESC
    """)
    abstract fun getLocalCardsForMyPostsNotPresentIn(
            pageId: String,
            location: String,
            section: String
    ): List<Card>

    @Query("""
        SELECT * FROM $TABLE_CARD WHERE level = 'LOCAL'
    """)
    abstract fun allLocalPosts(): List<Card>

    /**
     * see [insertLocalPost]
     */
    @Transaction
    open fun replaceFirstPageWithLocalCards(fe: FetchInfoEntity,
                                            postEntityList: List<PostEntity>,
                                            followEntityDao: FollowEntityDao,
                                            requestUrl: String,
                                            adSpec: AdSpec? = null,
                                            isForyou: Boolean,
                                            isMyposts: Boolean,
                                            isGroupFeed: Boolean = false,
                                            localCardTtl: Long = 300_000 /*msec*/,
                                            isNetworkResponse: Boolean = true) {// TODO(satosh.dhanyamraju): handhshake flag
        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId
                ?: insIgnore(fe)

        val feedPage = lookupPage(fe.entityId, fe.section)

        Logger.d(TAG, "replaceFirstPageWithLocalCards: ${fe.fetchInfoId}, ${postEntityList.size}, $isForyou , $isMyposts")
        if (isMyposts) {
            val notexpiredLocalCardsDeduped =
                    if (feedPage?.contentUrl.isUrlThatHasAllowLocalCardsParam()) {
                        getLocalCardsForMyPosts(System.currentTimeMillis() - localCardTtl)
                                .mapNotNull { it.rootPostEntity() }
                                .filter { pe ->
                                    /* should not be present in the server response */
                                    postEntityList.find { it.id == pe.id } == null
                                }
                    } else {
                        Logger.v(TAG, "replaceFirstPageWithLocalCards : mypost; filter does not allow local cards")
                        emptyList()
                    }
            Logger.v(TAG, "replaceFirstPageWithLocalCards : mypost local ins ${notexpiredLocalCardsDeduped.size} cards")
            val postsWithLocalCardsOnTop = notexpiredLocalCardsDeduped + postEntityList
            replaceFirstPage(fe, postsWithLocalCardsOnTop, followEntityDao, requestUrl, adSpec)
        } else if (isForyou) {

            val localCardsForForyou = getLocalCardsForForyou().mapNotNull { it.rootPostEntity() }
            val localCardsNotPresentInResponse = localCardsForForyou.filter { lc ->
                postEntityList.find { pe ->
                    pe.id == lc.id
                } == null
            }
            Logger.v(TAG, "replaceFirstPageWithLocalCards : mypost local ins ${localCardsNotPresentInResponse.size} cards")

            val postsWithLocalCardsOnTop = localCardsNotPresentInResponse + postEntityList
            replaceFirstPage(fe, postsWithLocalCardsOnTop, followEntityDao, requestUrl, adSpec)
            if (isNetworkResponse) {
                markAsShownInForyou(localCardsForForyou.map { it.id })
            }
        }
        /*else if (isGroupFeed) {
            val notexpiredLocalCardsDeduped = getLocalCardsForGroup(fe.entityId, fe.section, System.currentTimeMillis() - localCardTtl)
                    .mapNotNull { it.rootPostEntity() }
                    .filter {pe -> *//* should not be present in the server response *//*
                        postEntityList.find { it.id == pe.id  } == null
                    }
            Logger.v(TAG, "replaceFirstPageWithLocalCards : groupfeed local ins ${notexpiredLocalCardsDeduped.size} cards")
            val postsWithLocalCardsOnTop = notexpiredLocalCardsDeduped + postEntityList
            replaceFirstPage(fe, postsWithLocalCardsOnTop, followEntityDao, requestUrl, adSpec)
        }*/
        Logger.i(TAG, "replaceFirstPageWithLocalCards: EXIT ${fe.fetchInfoId}, ${postEntityList.size}, $isForyou , $isMyposts")
    }

    @Query("""
        UPDATE $TABLE_CARD
        SET local_status = :state, local_progress = :progress
        WHERE id = :postId AND level = 'LOCAL'
    """)
    abstract fun _updateLocalProgressStatebyPostid(postId: String,
                                                   progress: Int,
                                                   state: PostUploadStatus)

    @Transaction
    open fun updateLocalProgressStatebyPostid(postId: String,
                                              progress: Int,
                                              state: PostUploadStatus) {
        val newList = readCardsWithIds(listOf(postId))
                .filter { it.i_level() == PostEntityLevel.LOCAL }
                .map {
                    it.withLocal(progress, state)
                }
        updatePosts(newList)
    }


    @Query("""
        SELECT 
        local_progress progress,
        local_status status,
        local_pageId pageId,
        local_location location,
        local_section section,
        local_shownInForyou shownInForyou,
        local_creationDate creationDate,
        local_cpId cpId,
        local_nextCardId nextCardId,
        local_fetchedFromServer fetchedFromServer,
        local_isCreatedFromMyPosts isCreatedFromMyPosts
        FROM $TABLE_CARD 
        WHERE id = :postId
    """)
    abstract fun localInfo(postId: String): LocalInfo?

    /**
     * [postEntityList] - fetched from server after successful upload
     */
    @Transaction
    open fun insertLocalPost(
            postEntityList: List<PostEntity>,
            followEntityDao: FollowEntityDao,
            fetchedFromServer: Boolean = false,
            foryouPageId: String = PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, Constants.EMPTY_STRING),
            foryouLocation: String = Constants.FETCH_LOCATION_LIST,
            foryouSection: String = PageSection.NEWS.section,
            donotmarkReadInForyou: Boolean = false
    ) {
        Logger.i(TAG, "insertLocalPost: ENTER , $fetchedFromServer, ${postEntityList.size}")
        postEntityList.forEach {
            Logger.i(TAG, "insertLocalPost: post>> ${it.title},${it.id}")

        }
        val level = /*if(fetchedFromServer) PostEntityLevel.TOP_LEVEL else*/ PostEntityLevel.LOCAL
        val posts = postEntityList.map {
            val lInfo = (localInfo(it.id) ?: it.localInfo
            ?: LocalInfo(creationDate = System.currentTimeMillis())).copy(
                    fetchedFromServer = fetchedFromServer
            )
            /*
                `After fetching from server, we don't need local-card( with level 'LOCAL')
                So change level before inserting this new (with level TOP_LEVEL) one.
                Else, both will exist.`
            */
//            if(fetchedFromServer) upgradeLocalPost(it.id)
            it.copy(level = level, localInfo = lInfo)
        }

        val cards = posts.map { it.toCard2("local") } // can pass any string. it is ignored for local level cards getUniqueId logic
        insIgnore(cards)
        updatePosts(cards)

        // nlfc foryou
        //because of insertReplace, and deletetion trigger, we should insert fetchdata everytime
        val postsNotInsertedInForyou = getLocalCardsForForyouNotPresentIn(foryouPageId, foryouLocation, foryouSection).map { it.postEntity }
        val insertedInForyou = insertLocalFetchDataAtTopOrInTheMiddle(foryouPageId, foryouLocation, foryouSection, postsNotInsertedInForyou)
        if (insertedInForyou && !donotmarkReadInForyou) markAsShownInForyou(posts.map { it.id })

        // nlfc myposts
        posts.forEach {
            with(it) {
                val createdFromMyPosts = localInfo?.isCreatedFromMyPosts
                val pageId = localInfo?.pageId
                val location = localInfo?.location
                val section = localInfo?.section
                val page = if (pageId != null && section != null) lookupPage(pageId, section) else null
                if (createdFromMyPosts == true) {
                    if (pageId != null && section != null && location != null) {
                        if (page?.contentUrl.isUrlThatHasAllowLocalCardsParam())
                            insertLocalCardFetchDataAtTop(pageId, location, section, listOf(it))
                        else
                            Logger.d(TAG, "insertLocalPost: mypost; filter doesn't allow local cards")
                    } else {
                        Logger.e(TAG, "insertLocalPost: myposts missing info $pageId, $location, $section")
                    }
                } /*else if (localInfo?.isCreatedFromOpenGroup == true) {
                    // TODO(satosh.dhanyamraju): dedup from fetchdata ?; handshake ttl? This is immediate, so may not need it.
                    val fetchInfos : List<FetchInfoEntity> = lookupFetchInfo(pageId, section)
                    fetchInfos.forEach { fi ->
                        insertLocalCardFetchDataAtTop(fi.entityId, fi.location, fi.section, listOf(it))
                    }
                }*/ else {
                    Logger.d(TAG, "insertLocalPost: ${it.id} not candidate for myposts")
                }
            }
        }
    }

    private fun insertLocalCardFetchDataAtTop(entityId: String, location: String, section: String, list: List<PostEntity>): Boolean {
        fetchInfo(entityId, location, section)?.let { fi ->
            val fetchId = fi.fetchInfoId
            fetchData0(fetchId)?.let { fd ->
                updatePageIndexBy(fetchId, fd.pageNum, -1, list.size)
                insReplaceFetchData(
                        list.sortedByDescending { it.localInfo?.creationDate }.mapIndexed { index, postEntity ->
                            FetchDataEntity(fetchId, fd.pageNum, index, postEntity.getUniqueId(fetchId), postEntity.format, fd.reqUrl)
                        }
                )
                Logger.d(TAG, "insertLocalCardFetchDataAtTop: inserted ${list.size} items")
                return true
            } ?: kotlin.run {
                Logger.e(TAG, "insertLocalCardFetchDataAtTop: no fetchdata")
                /*there are no cards - this will be the first. get the current contentUrl and insert*/
                val contentUrl = getContentUrl(fetchId) ?: ""
                insReplaceFetchData(
                        list.sortedByDescending { it.localInfo?.creationDate }.mapIndexed { index, postEntity ->
                            FetchDataEntity(fetchId, 0, index, postEntity.getUniqueId(fetchId), postEntity.format, contentUrl)
                        }
                )
                return false
            }
        } ?: kotlin.run {
            Logger.e(TAG, "insertLocalCardFetchDataAtTop: no fetchinfo")
            return false
        }
    }


    @Transaction
    open fun insertLocalFetchDataAtTopOrInTheMiddle(pageId: String,
                                                    location: String,
                                                    section: String,
                                                    list: List<PostEntity>): Boolean {
        val fi = fetchInfo(pageId, location, section) ?: run {
            Logger.e(TAG, "insertLocalFetchDataAtTopOrInTheMiddle: no fetchinfo")
            return false
        }
        val fetchId = fi.fetchInfoId
        val m = list.sortedBy { it.localInfo?.creationDate }.filter {
            it.localInfo?.nextCardId != null
        }
        val inbetweenInserted = mutableListOf<String>()
        m.forEach {
            fetchDataByPostId(it.localInfo?.nextCardId ?: "", fetchId).firstOrNull()?.let { fd ->
                updatePageIndexBy(fetchId, fd.pageNum, fd.indexInPage - 1, 1)
                insReplaceFetchData(listOf(
                        FetchDataEntity(fetchId, fd.pageNum, fd.indexInPage, it.getUniqueId(fetchId), it.format, fd.reqUrl)))
                inbetweenInserted.add(it.id)
            }
        }
        val remaining = list.filterNot { inbetweenInserted.contains(it.id) }
        if (remaining.isNotEmpty()) {
            return insertLocalCardFetchDataAtTop(pageId, location, section, remaining)
        }
        return true
    }

    @Query("""
        UPDATE $TABLE_CARD
        SET local_shownInForyou = 1
        WHERE id IN (:ids) AND level = 'LOCAL'
    """)
    abstract fun _markAsShownInForyou(ids: List<String>)

    @Query("""
        SELECT * FROM $TABLE_CARD WHERE id in (:ids)
    """)
    abstract fun readCardsWithIds(ids: List<String>): List<Card>

    @Transaction
    open fun markAsShownInForyou(ids: List<String>) {
        val updatedList = readCardsWithIds(ids).map {
            it.withShownInForyou()
        }
        updatePosts(updatedList)
    }

    @Update
    abstract fun updatePosts(list: List<Card>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insIgnore(list: List<Card>)

    //------------ LOCAL cards ----------- ----------- ----------- ----------- -----------

    @Query("""
       update fetch_data set format = :format where storyId = :uniquId
    """)
    abstract fun updateFetchDataFormat(uniquId: String, format: Format)

    @Query("SELECT id FROM fetch_data f LEFT JOIN $TABLE_CARD c ON f.storyId == c.uniqueId WHERE fetchId == :fid" )
    abstract fun readCardIdsFromFetch(fid: Long): List<String>

    @Query("SELECT postEntity FROM fetch_data f LEFT JOIN $TABLE_CARD c ON f.storyId == c.uniqueId WHERE f.format == 'POST_COLLECTION' AND fetchId == :fid" )
    abstract fun readCollectionCardsFromFetch(fid: Long): List<PostEntity>

    @Transaction
    open fun queryIds(fe: Long): Set<String> {
//        1. read all ids (join query)
//        2. query collection-pe and loop through them to collect child items
        val set = LinkedHashSet<String>()
        set.addAll(readCardIdsFromFetch(fe))
        readCollectionCardsFromFetch(fe).forEach { pe ->
            pe.collectionAsset?.collectionItem?.forEach { child ->
                set.add(child.id)
            }
        }
        Logger.d(TAG, "queryIds: $set")
        return  set
    }

    @Transaction
    open fun replaceFirstPage(fe: FetchInfoEntity,
                              postEntityList: List<PostEntity>,
                              followEntityDao: FollowEntityDao,
                              requestUrl: String,
                              adSpec: AdSpec? = null) {
        try {
            val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId
                    ?: insIgnore(fe)
            deleteFetchDataMatching(fetchId, requestUrl) // this should be done before inserting to posts table
            if (Logger.loggerEnabled()) {
                val tt = measureTimeMillis { DedupUtil.removeDuplicates(postEntityList , emptySet()) }
                Logger.d(TAG, "tt fp= $tt")
            } else {
                DedupUtil.removeDuplicates(postEntityList , emptySet())
            }
            // update posts table
            insertPostWithRelationships(postEntityList.filter {
                it.level != PostEntityLevel.LOCAL
            }, followEntityDao, fetchId.toString())

            //update adSpec data for entity
            adSpec?.let {
                insReplaceAdSpec(listOf(AdSpecEntity(entityId = fe.entityId, section = fe.section,
                        adSpec = it, inHandshake = false)))
            }

            // update fetch info
            updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, fe.currentPageNum.toString(), fe.section)

            // replace fetch data
            insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
                FetchDataEntity(fetchId, fe.currentPageNum, index, postEntity.getUniqueId(fetchId.toString()), postEntity.format, requestUrl)
            })

            BusProvider.getRestBusInstance().post(ResetDedupHelperEvent(
                    mapOf(Constants.BUNDLE_ENTITY_ID to fe.entityId,
                            Constants.BUNDLE_LOCATION_ID to fe.location,
                            NewsConstants.DH_SECTION to fe.section)))
        } catch (e: Exception) {
            Logger.caughtException(e)
            Logger.e(TAG, "DB insert failed. ", e)
            // TODO(satosh.dhanyamraju): re-throw exception, after UI is built.
        }
    }

    private fun insertAssociations(postEntityList: List<PostEntity>, fe: FetchInfoEntity) {
        val location = fe.location
        val section = fe.section

        postEntityList.forEach { pa ->
            val associations = pa.associations
            if (associations != null) {
                val uniqueId = pa.id + "_association"
                val location = "detail_${pa.id}"
                SocialDB.instance().groupDao().insReplace(GeneralFeed(uniqueId, "", "GET", section))
                val fetchEntity = FetchInfoEntity(uniqueId, location, "", 0, section = section)
                SocialDB.instance().fetchDao().insIgnore(fetchEntity)
                SocialDB.instance().fetchDao().insertStoriesinFetchDB(fetchEntity, associations)
            }
        }
    }

    @Transaction
    open fun appendNextPage(fe: FetchInfoEntity,
                            postEntityList: List<PostEntity>,
                            followEntityDao: FollowEntityDao,
                            requestUrl: String) {
        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId ?: insIgnore(fe)
        val pgNum = maxPageNumInFetchData(fetchId) ?: fe.currentPageNum
        if (Logger.loggerEnabled()) {
            val tt =  measureTimeMillis {   DedupUtil.removeDuplicates(postEntityList, queryIds(fetchId)) }
            Logger.d(TAG, "tt np= $tt")
        } else {
            DedupUtil.removeDuplicates(postEntityList, queryIds(fetchId))
        }
        insertPostWithRelationships(postEntityList, followEntityDao, fetchId.toString())

        // update fetch info
        updateNpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, pgNum.toString(), fe.section)


        // append to fetch data
        insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
            FetchDataEntity(fetchId, pgNum, index,
                    postEntity.getUniqueId(fetchId.toString()), postEntity.format, requestUrl)
        })
    }

    @Transaction
    open fun insertAdInDB(adPost: Card, prevPostId: String?, adPosCheckTs: Long,
                          entityId: String, location: String, section: String): AdInsertFailReason {
        val fetchInfo = fetchInfo(entityId, location, section)
                ?: return AdInsertFailReason.UNKNOWN_FETCH_ID

        // For 0th index, prevPostId will be null.
        // In other cases, prevPostId & its fetch-data should be non-null.
        // If there is newer data in DB, abort insert.
        val fde = prevPostId?.let {
            val fdata = fetchDataByPostId(prevPostId, fetchInfo.fetchInfoId)
            if (fdata.isNotEmpty() && fdata[0].receivedTs < adPosCheckTs) {
                return@let fdata[0]
            }
            Logger.e(TAG, "Abort ad insert. Data in DB is newer.")
            return AdInsertFailReason.FEED_DATA_CHANGED
        }
        val pageNum = fde?.pageNum ?: 1
        val prevPostindex = fde?.indexInPage ?: -1
        //update page index for all following posts
        updatePageIndexBy(fetchInfo.fetchInfoId, pageNum, prevPostindex, 1)

        Logger.d(TAG, "Ad Inserted at page : $pageNum, index : ${prevPostindex + 1}")
        //insert ad data
        insReplacePosts(listOf(adPost))
        val requestUrl = getContentUrl(fetchInfo.fetchInfoId) ?: ""
        val rows = insReplaceFetchData(listOf(FetchDataEntity(fetchInfo.fetchInfoId,
                pageNum, prevPostindex + 1, adPost.uniqueId, adPost.format, requestUrl)))[0]
        return if (rows > 0) AdInsertFailReason.NONE else AdInsertFailReason.QUERY_FAILED
    }

    @Transaction
    open fun replaceAd(adPost: PostEntity, oldAdId: String?, entityId: String,
                       location: String, section: String): Long {
        val fetchInfo = fetchInfo(entityId, location, section) ?: return -1

        val row = oldAdId?.let {
            val fdata = fetchDataByPostId(oldAdId, fetchInfo.fetchInfoId)
            if (fdata.isNotEmpty()) {
                return@let fdata[0]
            }
            null
        }
        row ?: return -1
        Logger.e(TAG, "Replace Ad. Old : $oldAdId, New : ${adPost.id}")
        val result = insReplaceFetchData(listOf(FetchDataEntity(row.fetchId,
                row.pageNum, row.indexInPage, adPost.id, adPost.format, row.reqUrl)))[0]
        insReplacePosts(listOf(adPost).map { it.toCard2().copy(uniqueId = it.id) })
        return result
    }


    @Transaction
    open fun insertNonLinearPost(nonLinearPost: NLFCItem, prevPostId: String,
                                 entityId: String, location: String, section: String) {
        Logger.d(Constants.NON_LINEAR_FEED, "insertNonLinearPost >>")
        val fetchInfo = fetchInfo(entityId, location, section) ?: return
        val fde: FetchDataEntity? = prevPostId.let {
            val fdata = fetchDataByPostId(prevPostId, fetchInfo.fetchInfoId)
            if (fdata.isNotEmpty()) {
                return@let fdata[0]
            }
            null
        }
        val pageNum = fde?.pageNum ?: 1
        val prevPostindex = fde?.indexInPage ?: -1
        //update page index for all following posts
        updatePageIndexBy(fetchInfo.fetchInfoId, pageNum, prevPostindex, 1)
        Logger.d(Constants.NON_LINEAR_FEED, "Non linear feed inserted at index : ${prevPostindex + 1} ")
        insReplaceFetchData(listOf(FetchDataEntity(fetchInfo.fetchInfoId,
                pageNum, prevPostindex + 1, nonLinearPost.postId, nonLinearPost.format, fde?.reqUrl
                ?: Constants.EMPTY_STRING)))[0]
        updateNonLinearItemInserted(nonLinearPost.postId)
    }

    @Transaction
    open fun insertNonLinearPostAt(nonLinearPost: NLFCItem, prevPostId: String, forcePosition: Int,
                                   entityId: String, location: String, section: String, url: String) {
        Logger.d(Constants.NON_LINEAR_FEED, "insertNonLinearPostAt forcePosition : $forcePosition")
        val fetchId = fetchInfo(entityId, location, section)?.fetchInfoId
                ?: insIgnore(FetchInfoEntity(entityId, location, null, 0, url, null, 0, section))
        val fetchInfo = fetchInfo(entityId, location, section) ?: return
        val fde: FetchDataEntity? = prevPostId.let {
            val fdata = fetchDataByPostId(prevPostId, fetchInfo.fetchInfoId)
            if (fdata.isNotEmpty()) {
                return@let fdata[0]
            }
            null
        }
        Logger.d(Constants.NON_LINEAR_FEED, "fde = $fde")
        val pageNum = fde?.pageNum ?: 0
        val prevPostindex = forcePosition
        val pageUrl = fde?.reqUrl ?: url
        //update page index for all following posts
        updatePageIndexBy(fetchInfo.fetchInfoId, pageNum, prevPostindex, 1)
        Logger.d(Constants.NON_LINEAR_FEED, "Non linear feed inserted at index : ${prevPostindex + 1}")
        insReplaceFetchData(listOf(FetchDataEntity(fetchInfo.fetchInfoId,
                pageNum, prevPostindex + 1, nonLinearPost.postId, nonLinearPost.format, pageUrl)))[0]
        updateNonLinearItemInserted(nonLinearPost.postId)
    }

    @Transaction
    open fun insertLanguageSelectionCard(
            offlinePost: PostEntity,
            prevPostId: String,
            entityId: String,
            location: String,
            section: String
    ) {
        val fetchInfo = fetchInfo(entityId, location, section) ?: return
        deleteFetchDataForPostMatching(fetchInfo.fetchInfoId, listOf(offlinePost.id))
        val fde: FetchDataEntity? = prevPostId.let {
            val fdata = fetchDataByPostId(prevPostId, fetchInfo.fetchInfoId)
            if (fdata.isNotEmpty()) {
                return@let fdata[0]
            }
            null
        }
        val pageNum = fde?.pageNum ?: 1
        val prevPostindex = fde?.indexInPage ?: -1
        Logger.d(Constants.OFFLINE_FEED, "Language selection card inserted at index : " +
                "${prevPostindex + 1} ")
        updatePageIndexBy(fetchInfo.fetchInfoId, pageNum, prevPostindex, 1)
        insReplacePosts(listOf(offlinePost), fetchInfo.fetchInfoId.toString())
        insReplaceFetchData(listOf(FetchDataEntity(
                fetchId = fetchInfo.fetchInfoId,
                pageNum = pageNum,
                indexInPage = prevPostindex + 1,
                storyId = "${fetchInfo.fetchInfoId}_${offlinePost.i_id()}",
                format = offlinePost.i_format(),
                reqUrl = fde?.reqUrl ?: Constants.EMPTY_STRING
        )))[0]
    }


    @Transaction
    open fun insertStoriesinFetchDB(fe: FetchInfoEntity, postEntityList: List<PostEntity>) {

        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId!!

        // update fetch info
        updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, fe.currentPageNum.toString(), fe.section)

        // replace fetch data
        insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
            val requestUrl = getContentUrl(fetchId) ?: ""
            FetchDataEntity(fetchId, fe.currentPageNum, index, postEntity.id, postEntity.format, requestUrl)
        })

    }

    @Transaction
    open fun insertDiscussionsinFetchDB(fe: FetchInfoEntity, postEntityList: List<PostEntity>) {
        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId ?: insIgnore(fe)
        val pgNum = maxPageNumInFetchData(fetchId) ?: fe.currentPageNum

        // update fetch info
        updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, pgNum.toString(), fe.section)
        // replace fetch data
        insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
            val requestUrl = getContentUrl(fetchId) ?: ""
            FetchDataEntity(fetchId, pgNum, index, postEntity.id,
                    postEntity.format, requestUrl)
        })
    }

    @Transaction
    open fun insertAssociationsinFetchDB(fe: FetchInfoEntity, postEntityList: List<PostEntity>) {
        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId ?: insIgnore(fe)
        val pgNum = maxPageNumInFetchData(fetchId) ?: fe.currentPageNum

        // update fetch info
        updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, pgNum.toString(), fe.section)
        // replace fetch data
        insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
            val requestUrl = getContentUrl(fetchId) ?: ""
            FetchDataEntity(fetchId, pgNum, index, postEntity.id, postEntity.format, requestUrl)
        })
    }

    @Transaction
    open fun insertNonLinearPostList(postEntityList: List<PostEntity>, parentId: String) {
        val postList = getPostList(postEntityList.map { it.id })
        val finalList = if (postList != null) {
            postEntityList.filterNot { postList.contains(it.id) }
        } else {
            postEntityList
        }

        insReplacePosts(finalList.map { it.toCard2().copy(uniqueId = "nlfc_${it.i_id()}") })
        finalList.forEach {
            insertNLFCItem(NLFCItem(postId = "nlfc_${it.id}",
                    parentPostId = parentId, format = it.format))
        }
    }

    @Transaction
    open fun insertRelatedVideoList(postEntityList: List<PostEntity>, parentId: String) {
        val postList = getPostList(postEntityList.map { it.id })
        val finalList = if (postList != null) {
            postEntityList.filterNot { postList.contains(it.id) }
        } else {
            postEntityList
        }

        insReplacePosts(finalList.map { it.toCard2().copy(uniqueId = "rlvideos_${it.i_id()}") })
        finalList.forEach {
            insertNLFCItem(NLFCItem(postId = "rlvideos_${it.id}",
                    parentPostId = parentId, format = it.format))
        }
    }


    @Transaction
    open fun insertPostBothChunks(fe: FetchInfoEntity, postEntityList: List<PostEntity>,
                                  followEntityDao: FollowEntityDao, adId: String? = null): Long {
        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId ?: insIgnore(fe)
        val pgNum = fe.currentPageNum
        updateFpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, fe.currentPageNum.toString(), fe.section)
        insReplaceFetchData(postEntityList.mapIndexed { index, postEntity ->
            val requestUrl = getContentUrl(fetchId) ?: ""
            FetchDataEntity(fetchId, pgNum, index,
                    postEntity.getUniqueId(fetchId.toString(), prefix = adId?.plus(Constants.UNDERSCORE_CHARACTER)),
                    postEntity.format, requestUrl)
        })

        insertPostWithRelationships(postEntityList, followEntityDao, fetchId.toString())
        insertAssociations(postEntityList, fe)
        return fetchId
    }

    open fun insertPostInFetch(fe: FetchInfoEntity, postEntity: PostEntity) {

        val fetchId = fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId ?: insIgnore(fe)
        val pgNum = fe.currentPageNum
        updateFpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, fe.currentPageNum.toString(), fe.section)
        val requestUrl = getContentUrl(fetchId) ?: ""
        val card = postEntity.toCard2(fetchId = fetchId.toString(), newLevel = PostEntityLevel.TOP_LEVEL)
        insReplaceFetchData(listOf(FetchDataEntity(fetchId, pgNum, 0,
                card.uniqueId, postEntity.format, requestUrl)))
        insReplacePosts(listOf(card))
    }

    @Transaction
    open fun cloneFetchForLocation(fetchId: Long, location: String, section: String, mustInclude: List<String> = emptyList()) {
        val newFetchId = cloneFetchInfoForLocation(fetchId, location, section)
        cloneFetchDataForDiffFetchId(newFetchId, fetchId, mustInclude)
    }


    // ------------------------------------------------------------------------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------------------------------------------------------------------------
    @Update
    internal abstract fun update(fetchInfoEntity: FetchInfoEntity)


    @Query("""
       update fetch_data set indexInPage = (indexInPage + :inc) * -1
       where fetchId = :fetchId AND pageNum = :page AND indexInPage > :indexAfter
    """)
    internal abstract fun updatePageIndex(fetchId: Long, page: Int, indexAfter: Int, inc: Int)

    @Query("""
       update fetch_data set indexInPage = indexInPage * -1 where fetchId = :fetchId
       AND pageNum = :page AND indexInPage < 0
    """)
    internal abstract fun invertPageIndex(fetchId: Long, page: Int)


    @Query("UPDATE nlfc SET isInserted=1 WHERE postId=:id")
    abstract fun updateNonLinearItemInserted(id: String)


    @Query("""
        update fetch_info set npUrlOf1stResponse = :npUrl, currentPageNum= :curPageNum, nextPageUrl = :npUrl
        where col_entity_id = :entityId and col_disp_loc = :displayLocation and section=:section
    """)
    internal abstract fun updateFpFetchInfo(entityId: String, displayLocation: String, npUrl: String?, curPageNum: String, section: String)

    @Query("""
        update fetch_info set nextPageUrl = :npUrl, currentPageNum= :curPageNum
        where col_entity_id = :entityId and col_disp_loc = :displayLocation and section=:section
    """)
    internal abstract fun updateNpFetchInfo(entityId: String, displayLocation: String, npUrl: String?, curPageNum: String, section: String)


    @Query("UPDATE fetch_info set lastViewDestroyTs = 0")
    abstract fun resetViewDestroyTime()

    /**
     * Sets nextpageUrl to null, for the matching fetch row. Used to terminate pagination
     */
    @Query("""
        UPDATE fetch_info set nextPageUrl = null
        WHERE col_entity_id = :entityId AND col_disp_loc = :location AND section = :section
    """)
    abstract fun paginationTerminated(entityId: String, location: String, section: String)

    /**
     * Workaround to avoid unique constraint failure on incrementing page indices.
     */
    @Transaction
    open fun updatePageIndexBy(fetchId: Long, page: Int, indexAfter: Int, inc: Int) {
        updatePageIndex(fetchId, page, indexAfter, inc)
        invertPageIndex(fetchId, page)
    }


    // ------------------------------------------------------------------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------------------------------------------------------------------


    @Query("DELETE FROM fetch_data where fetchId = :fetchId AND reqUrl = :requestUrl")
    internal abstract fun deleteFetchDataMatching(fetchId: Long?, requestUrl: String)

    @Query("""
       DELETE FROM fetch_data where fetchId = :fetchId AND storyId in (:storyIds)
    """)
    internal abstract fun deleteFetchDataForPostMatching(fetchId: Long?, storyIds: List<String>)


    @Query("""
       DELETE FROM fetch_data WHERE fetchId = :fetchId AND storyId in (SELECT DISTINCT uniqueId FROM card WHERE `id` IN (:storyIds))
    """)
    internal abstract fun deleteFetchDataOfPostIds(fetchId: Long?, storyIds: List<String>)

    @Query("""
        DELETE
        FROM fetch_data
        WHERE pageNum >
            (SELECT min(pageNum)
             FROM fetch_data
             WHERE fetchId = :fetchId)
          AND fetchId = :fetchId
    """)
    @VisibleForTesting
    internal abstract fun keepOnlyFPFetchData(fetchId: Long)

    @Query("SELECT id from pages where id = :pageId")
    abstract fun pageWithId(pageId: String = "91581308b67fdfbcd24028a0c513bc37"): LiveData<List<String>>


    @Query("SELECT contentUrl from pages where id = :pageId")
    abstract fun contentUrlofPageWithId(pageId: String = "91581308b67fdfbcd24028a0c513bc37")
            : LiveData<String?>


    /**
     * deletions will cascade
     */
    @Query("""
        DELETE from fetch_info WHERE col_entity_id = :entityId AND col_disp_loc = :displayLocation and section=:section
    """)
    abstract fun fullCleanupFetch(entityId: String, displayLocation: String, section: String)

    @Query("""
        DELETE
        FROM fetch_data
        WHERE (storyId = :itemId OR storyId IN (SELECT uniqueId FROM card where adId =:itemId))
          AND fetchId = (SELECT col_fetchInfoId FROM fetch_info WHERE
            col_entity_id = :entityId AND col_disp_loc = :location AND section = :section)
    """)
    abstract fun removeAd(itemId: String, entityId: String, location: String, section: String)

    @Query("""
        DELETE
        FROM fetch_data
        WHERE storyId = :itemId OR storyId IN (SELECT uniqueId FROM card where adId =:itemId)
    """)
    abstract fun removeAdOnDislike(itemId: String)

    @Query("""
        DELETE
        FROM fetch_data
        WHERE (format = "AD" OR storyId IN (SELECT uniqueId FROM card where adId IS NOT NULL))
          AND fetchId = (SELECT col_fetchInfoId FROM fetch_info WHERE
            col_entity_id = :entityId AND col_disp_loc = :location AND section = :section)
    """)
    abstract fun clearAds(entityId: String, location: String, section: String)

    @Query("""
        DELETE
        FROM fetch_data
        WHERE (format = "AD")
          AND fetchId = (SELECT col_fetchInfoId FROM fetch_info WHERE
            col_entity_id = :entityId AND col_disp_loc = :location AND section = :section)
    """)
    abstract fun clearDetailPageAds(entityId: String, location: String, section: String)

    @Query("DELETE FROM fetch_data WHERE format = 'AD' OR storyId IN (SELECT uniqueId FROM card where adId IS NOT NULL)")
    abstract fun deleteAllAds()


    @Query("DELETE FROM fetch_info WHERE col_entity_id=:entityId")
    abstract fun deleteFetchInfo(entityId: String)

    @Query("DELETE FROM fetch_info")
    abstract fun deleteAllFetchInfo()

    @Transaction
    open fun deleteFetchInfoAndLocalCards() {
        deleteAllFetchInfo()
        deleteLocalCards()
    }

    @Query("DELETE FROM fetch_info WHERE col_disp_loc <> 'list'")
    abstract fun deleteDanglingFetchInfo()

    @Query("""
        DELETE from $TABLE_CARD WHERE id in (:id) AND level = 'LOCAL'
    """)
    abstract fun deleteLocalPost(id: List<String>)

    @Query("""
        SELECT uniqueId FROM $TABLE_CARD c LEFT JOIN fetch_data f ON c.uniqueId = f.storyId 
        WHERE c.id=:postId AND f.fetchId IN (SELECT col_fetchInfoId FROM fetch_info WHERE
            col_entity_id = :entityId AND col_disp_loc = :location AND section = :section)
    """)
    abstract fun getUniqueIdFromFetch(entityId: String, location: String, section: String, postId: String): String?


    /**
     *  Delete all adSpecs except the recent 50 AND the ones received in handshake response.
     */
    @Query("""
        DELETE FROM ad_spec WHERE inHandshake = 0 AND
        id NOT IN (SELECT id from ad_spec WHERE inHandshake = 0 ORDER BY entryTs DESC LIMIT :keepCount)
        """)
    abstract fun deleteOldAdSpecs(keepCount: Int)

    @Transaction
    open fun cleanUpFetch(entityId: String, displayLocation: String, ts: Long = System.currentTimeMillis(), section: String) {
        val fetchInfo = fetchInfo(entityId, displayLocation, section) ?: kotlin.run {
            Logger.e(TAG, "cleanUpFetch: couldn't find fetchInfo")
            return
        }
        keepOnlyFPFetchData(fetchInfo.fetchInfoId)
        clearAds(entityId, displayLocation, section)
        val updatedFetchInfo = fetchInfo.copy(
                nextPageUrl = fetchInfo.npUrlOf1stResponse,
                currentPageNum = firstPageNumOf(fetchInfo.fetchInfoId) ?: 1,
                lastViewDestroyTs = ts
        )
        update(updatedFetchInfo)
    }

    @Query("""
        DELETE FROM card WHERE level = 'LOCAL' AND local_creationDate < :olderThan
    """)
    abstract fun deleteExpiredLocalCards(olderThan: Long?)

    @Query("DELETE FROM card WHERE level = 'LOCAL'")
    abstract fun deleteLocalCards()

    @Query("DELETE FROM card WHERE level = 'AD_PROXY'")
    abstract fun deleteProxyAdCards()

    @Transaction
    open fun cloneFetchForNewsDetail(entityId: String,
                                     location: String,
                                     section: String,
                                     keepIds: List<String>,
                                     cloneSuffix: String): String {
        val fetchId: Long? = fetchInfo(entityId, location, section)?.fetchInfoId
        fetchId ?: return Constants.EMPTY_STRING
        val clonedLocation = "${location}_detail"
        cloneFetchForLocation(fetchId, clonedLocation, section, keepIds)
        return clonedLocation
    }

    @Transaction
    open fun insertFetchInfoAndFetchData(fetchInfo: FetchInfoEntity,
                                         card: Card,
                                         index: Int,
                                         postDao: PostDao): Long {
        val fetchId = insIgnore(fetchInfo)
        val fetchData = FetchDataEntity(
            fetchId, 0, index, card.uniqueId,
            card.i_format() ?: Format.HTML, Constants.EMPTY_STRING
        )
        insReplaceFetchData(mutableListOf(fetchData))
        postDao.insIgnore(card.copy(level = PostEntityLevel.TOP_LEVEL))
        return fetchId
    }

    @Transaction
    open fun insertCollectionChildren(fetchDataChildren: List<FetchDataEntity>,
                                      cardChildren: List<Card>,
                                      postDao: PostDao) {
        if (fetchDataChildren.isEmpty() || cardChildren.isEmpty()) {
            return
        }
        insReplaceFetchData(fetchDataChildren)
        postDao.insIgnore(cardChildren)
    }

    companion object {

        private const val TAG = "FetchDao"

        @Language("RoomSql")
        const val trigger_delete_dangling_posts = """
            CREATE TRIGGER trigger_delete_dangling_posts AFTER DELETE ON fetch_data
            BEGIN
            DELETE FROM $TABLE_CARD WHERE uniqueId NOT IN (SELECT storyId FROM fetch_data UNION ALL SELECT postId FROM nlfc) AND level = 'TOP_LEVEL';
            END
        """

        @Language("RoomSql")
        const val trigger_delete_dangling_discussion = """
            CREATE TRIGGER trigger_delete_dangling_discussion AFTER DELETE ON discussions
            BEGIN
            DELETE FROM $TABLE_CARD WHERE id NOT IN (SELECT ${Discussions.COL_CHILD_ID} 
            FROM discussions) AND level = 'DISCUSSION';
            END
        """

        @Language("RoomSql")
        const val trigger_delete_dangling_related = """
            CREATE TRIGGER trigger_delete_dangling_related AFTER DELETE ON fetch_data
            BEGIN
            DELETE FROM related_list WHERE fetchId NOT IN (SELECT fetchId FROM fetch_data);
            END
        """

        @Language("RoomSql")
        const val trigger_del_post_not_in_related = """
            CREATE TRIGGER trigger_del_post_not_in_related AFTER DELETE ON related_list
            BEGIN
            DELETE FROM $TABLE_CARD WHERE id NOT IN (SELECT id FROM related_list) AND level = 'RELATED_STORIES';
            END
        """

        @Language("RoomSql")
        const val trigger_delete_fetchdata_of_localcards = """
            CREATE TRIGGER trigger_delete_fetchdata_of_localcards AFTER
            DELETE ON $TABLE_CARD WHEN old.level = 'LOCAL' BEGIN
            DELETE
            FROM fetch_data
            WHERE storyId = OLD.uniqueId; END
        """

        //remove ad rules for every session
        @Language("RoomSql")
        const val trigger_drop_table_immersive_ad_rule = """
            DELETE FROM immersive_ad_rule_stats
        """
    }
}