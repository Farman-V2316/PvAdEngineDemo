/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.mergeCounts
import com.newshunt.dataentity.social.entity.TABLE_CARD
import io.reactivex.Observable
import java.util.Collections

/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class PostDao : BaseDao<Card> {

    val TAG = "PostDao"

    @VisibleForTesting
    @Query("select * from $TABLE_CARD where id = :postId")
    abstract fun postByIdLiveData(postId: String): LiveData<Card?>

    @Query("select * from $TABLE_CARD where id = :postId")
    abstract fun postEntityById(postId: String): List<Card>

    @Query("""
       select * from $TABLE_CARD where id in (:postId)
    """)
    abstract fun postEntitiesById(postId: List<String>): List<Card>

    /* TODO@amit.chaudhary
    * Visible for testing of trigger. suspect to get removed before release.
    * */
    @Query("select * from $TABLE_CARD")
    abstract fun all(): LiveData<List<Card>>

    @Query("select * from $TABLE_CARD where id=:postId")
    abstract fun getCardById(postId: String): List<Card>

    @Update
    abstract fun updateCard2s(list: List<Card>)

    @Transaction
    open fun updatePost(post: PostEntity) {
        val cards = getCardById(post.i_id())
        if (cards.isNotEmpty()) {
            cards.forEach { card ->
                post.adSpec = card.i_adSpec()
                post.moreStories = card.i_moreStories()
                post.experiment = card.i_experiments()
                post.nonLinearPostUrl = card.i_nonLinearPostUrl()
                post.localInfo = card.i_localInfo()
                post.referralString = card.i_referralString()
                post.recommendationTs = card.i_recommendationTs()
                post.groupId = card.i_groupId()
                post.articleTrack = card.i_articleTrack()
                post.publishTime = card.i_publishTime()
                post.tsString = card.i_tsString()
                post.counts = mergeCounts(card.i_counts(), post.i_counts())
                card.i_uiType()?.let { post.uiType = it }
                post.commentRepostItems = card.i_commentRepostItems()
                val newCard = post.toCard2(useUniqueId = card.uniqueId, newLevel = card.level, adId = card.i_adId())
                updateCard2s(Collections.singletonList(newCard))
            }
        }
    }

    @Transaction
    open fun incViewCount(postId: String, parentPostId: String?) {
        if (parentPostId == null) {
            postEntityById(postId).map { pe ->
                val p = (pe.i_counts() ?: Counts2()).incrementViewCount()
                updatePost(pe.withNewCounts(p))
            }
        } else {
            postEntityById(parentPostId).map { parentPe ->
                val collectionItems: List<PostEntity>? = parentPe.i_collectionItems()?.filterIsInstance(PostEntity::class.java)?.map { item ->
                    item.let {
                        if (it.i_id() == postId) {
                            it.let { pe ->
                                val p = (pe.i_counts() ?: Counts2()).incrementViewCount()
                                pe.withNewCounts(p)
                            }
                        } else {
                            it
                        }
                    }
                }
                val newPost = parentPe.copy(postEntity = parentPe.postEntity.copy(
                        collectionAsset = parentPe.postEntity.collectionAsset?.copy(collectionItem = collectionItems)))
                updatePost(newPost)
            }
        }
    }

    @Update
    abstract fun updatePost(post: Card)

    @Transaction
    open fun insIgnore(post: PostEntity) {
        insIgnore(post.toCard2())
    }
    @Transaction
    open fun insReplace(vararg t: PostEntity) {
        insReplace(t.map { it.toCard2() })
    }


    @Query("select * from $TABLE_CARD where id in (SELECT id from related_list where related_list.postId = :postId)")
    abstract fun related(postId: String): Observable<List<Card>>

    @Transaction
    open fun updateCount(postId: String, countVal: Counts2?) {
        val posts = postEntityById(postId)
        if (posts.isEmpty()) {
            Logger.e(TAG, "updateCount: no cards")
            return
        }
        posts.map { post ->
            val updatedPostEntity = post.withNewCounts(countVal)
            updatePost(updatedPostEntity)
        }
    }

    /* TODO@amit.chaudhary
    * Visible for testing of trigger. suspect to get removed before release.
    * */
    @VisibleForTesting
    @Query("""
        DELETE FROM $TABLE_CARD where id=:postId
        """)
    abstract fun deletePost(postId: String)

    @Query("""
        DELETE FROM $TABLE_CARD
        """)
    abstract fun deleteAllPost()

    @Query("select id from $TABLE_CARD where format = (:format) and subFormat = (:subFormat)")
    abstract fun postIdByFormatAndSubFormat(format: Format, subFormat: SubFormat): List<String>

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
                    AND SECTION = :section))
        AND id = :postId
    """)
    internal abstract fun lookupCard(postId: String, entityId: String, location: String, section: String): List<Card>

}