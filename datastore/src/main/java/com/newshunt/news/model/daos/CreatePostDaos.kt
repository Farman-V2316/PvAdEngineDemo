/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.isCommentOrRepost
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.DeleteCPEntity
import com.newshunt.dataentity.social.entity.ImageEntity
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.dataentity.social.entity.TABLE_CARD
import org.intellij.lang.annotations.Language

@Dao
abstract class ImageDao : BaseDao<ImageEntity> {
    @Query("select * from img_entity where cp_id=:postId")
    abstract fun imgbypostID(postId: Int): List<ImageEntity>

    @Query("update img_entity set server_gen_id=:serverId, img_uploaded=1 where img_path=:url")
    abstract fun updateServerState(url:String, serverId:String)

    @Query("delete from img_entity where cp_id=:postId and img_path=:imgPath")
    abstract fun removeImg(postId: Int, imgPath: String)

    @Query("delete from img_entity")
    abstract fun deleteAll()

    @Query("DELETE FROM img_entity WHERE cp_id=:cpId")
    abstract fun delete(cpId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insReplaceRX(vararg t: ImageEntity): Array<Long>

}

@Dao
abstract class CreatePostDao : BaseDao<CreatePostEntity> {
    @Query("select * from cp_entity where cpId = :postId")
    abstract fun cpbypostID(postId: Long): CreatePost?

    @Query("select post_id from cp_entity WHERE cpId = :cpId")
    abstract fun postIdByCpId(cpId: Int): String?

    @Deprecated("use fetchdao. will be moved")
    @Query("""
        SELECT * FROM cp_entity 
        WHERE ui_mode NOT IN ('COMMENT', 'REPLY') AND is_localcard_shown = 1
        ORDER BY creation_date ASC""")
    abstract fun localCards() : LiveData<List<CreatePost>>

    // TODO(satosh.dhanyamraju): should change posts table?
    @Deprecated("use fetchdao. wil be moved")
    @Query("UPDATE cp_entity SET is_localcard_shown = 0 WHERE state == 'SUCCESS'")
    abstract fun markLocalCardsAsShown()

    @Query("select * from cp_entity where cpId=:id")
    abstract fun cpentityByID(id: Int): CreatePostEntity?

    @Query("""
        SELECT * from cp_entity 
        WHERE  
            ui_mode NOT IN ('COMMENT', 'REPLY')
            AND (localnextCardId IS NULL OR group_joined IS 1)
            AND state = 'SUCCESS'
            AND creation_date > :ts
        ORDER BY creation_date DESC
        LIMIT 1
    """)
    abstract fun latestUploadedPostNotInsertedInForyouNewerThan(ts : Long) : LiveData<CreatePostEntity?>

    @Query("update cp_entity set privacy_level = :level, allow_comments = :allowComments where cpId=:postId")
    abstract fun updatePostMeta(postId: Int, level: String, allowComments: Boolean = true)

    @Query("update cp_entity set text = :body where cpId=:postId")
    abstract fun updateMeta(postId: Int, body: String)

    @Query("update cp_entity set selectedLikeType = :selectedLikeType where cpId =:cpId")
    abstract fun updateMetaInterction(cpId: String, selectedLikeType: String)

    /**
     * Triggers to update counts will be written only in columnn updates (to reduce number of triggers). So insert is insertIgnore + toggle
     *
     */
    @Transaction
    open fun toggleLike(id: String, likeType: String) {
        id.toIntOrNull()?: return
        val existingRow = selectCPByIDAndLikeType(Integer.parseInt(id), likeType)
        if (existingRow == null) {
            updateMetaInterction(id, likeType)
        } else {
            updateMetaInterction(id, "")
        }
    }

    @Query("select * from cp_entity WHERE cpId=:id AND selectedLikeType = :likeType")
    abstract fun selectCPByIDAndLikeType(id: Int, likeType: String) :CreatePostEntity?

    @Query("select * from cp_entity")
    abstract fun all(): List<CreatePost>

    @Query("select * from cp_entity")
    @VisibleForTesting
    internal abstract fun test_all(): List<CreatePostEntity>

    @Query("select * from cp_entity")
    abstract fun allLD(): LiveData<List<CreatePost>>

    @Query("select count(*) from cp_entity where parent_id = :parent_id AND state = 'SUCCESS' " +
            "ORDER BY creation_date DESC")
    abstract fun getByParent(parent_id: String): LiveData<Int>

    @Query("select * from cp_entity where parent_id = :parent_id AND state = 'SUCCESS' AND " +
            "ui_mode in (:uiModes)")
    abstract fun getCPEntityByParent(parent_id: String, uiModes: List<String>): List<CreatePost>

    @Query("select creation_date as creationDate, parent_id as parentId from cp_entity" +
            " where state = 'SUCCESS' AND  parent_post_id  = :parent_id AND parent_id <> parent_post_id")
    abstract fun getReplyCount(parent_id: String): LiveData<List<ReplyCount>>

    @Query("delete from cp_entity")
    abstract fun _deleteAll()
    @Transaction
    open fun deleteAll() {
        decCounts(all()
                .filter { it.cpEntity.uiMode.isCommentOrRepost()}
                .map {
                    /*(it.cpEntity.parentPostId?:"") to it.cpEntity.uiMode*/
                    DeleteCPEntity(it.cpEntity.parentPostId ?: "", null, it.cpEntity.uiMode)
                })
        _deleteAll()
    }

    @Transaction
    open fun decCounts(ids: List<DeleteCPEntity>) {
        val groups = ids.groupBy {
            if (it.parentId == it.parentPostId || it.parentId.isNullOrEmpty()) {
                1
            } else {
                2
            }
        }

        val list1 = groups.get(1)?.flatMap { pair ->
            lookupCard(pair.parentPostId).map {
                it to pair.mode
            }
        }?.map {
            val counts2 = it.first.i_counts() ?: Counts2()
            val decrementedCount = when {
                it.second == CreatePostUiMode.COMMENT -> counts2.decrementCommentCount()
                it.second == CreatePostUiMode.REPOST -> counts2.decrementRepostCount()
                else -> counts2
            }
            it.first.withNewCounts(decrementedCount)
        } ?: emptyList()

        val list2 = groups.get(2)?.filter { it.parentId != null }?.flatMap { pair ->
            lookupCard(pair.parentId!!).map {
                it to pair.mode to pair.parentPostId
            }
        }?.map {
            val collectionCard: Card = it.first.first
            val uiMode: CreatePostUiMode = it.first.second
            val parentPostId: String = it.second
            val childCards = collectionCard.i_collectionItems()?.filterIsInstance<PostEntity>()?.map {
                if (it.i_id() == parentPostId) {
                    val counts2 = (it.i_counts() ?: Counts2())
                    val updatedCounts = when {
                        uiMode == CreatePostUiMode.COMMENT -> counts2.decrementCommentCount()
                        uiMode == CreatePostUiMode.REPOST -> counts2.decrementRepostCount()
                        else -> counts2
                    }
                    it.withNewCounts(updatedCounts)
                } else {
                    it
                }
            }

            val childMoreCards = collectionCard.i_moreStories()?.filterIsInstance<PostEntity>()?.map {
                if (it.i_id() == parentPostId) {
                    val counts2 = (it.i_counts() ?: Counts2())
                    val updatedCounts = when {
                        uiMode == CreatePostUiMode.COMMENT -> counts2.decrementCommentCount()
                        uiMode == CreatePostUiMode.REPOST -> counts2.decrementRepostCount()
                        else -> counts2
                    }
                    it.withNewCounts(updatedCounts)
                } else {
                    it
                }
            }

            collectionCard.copy(postEntity = collectionCard.postEntity.copy(collectionAsset = collectionCard
                    .postEntity.collectionAsset?.copy(collectionItem = childCards), moreStories = childMoreCards))
        } ?: emptyList()

        updatePosts(list1.plus(list2))
    }

    @Query("DELETE FROM cp_entity WHERE cpId=:cpId")
    abstract fun _delete(cpId: Int)
    @Transaction
    open fun delete(cpId: Int) {
        cpentityByID(cpId)?.let {
            val parentPostId = it.parentPostId
            if (it.uiMode.isCommentOrRepost() && parentPostId != null) {
                decCounts(listOf(DeleteCPEntity(parentPostId, it.parentId, it.uiMode)))
            }
        }
        _delete(cpId)
    }

    @Query("SELECT * from cp_entity WHERE post_id = :postId")
    abstract fun cpEntityByPostId(postId: String): CreatePost?

    @Query("DELETE FROM cp_entity WHERE post_id=:postId")
    abstract fun _deleteByPostId(postId: String)
    @Transaction
    open fun deleteByPostId(postId: String) {
        cpEntityByPostId(postId)?.cpEntity?.let {
            val ppid = it.parentPostId
            if (it.uiMode.isCommentOrRepost() && ppid != null) {
                decCounts(listOf(/*ppid to it.uiMode*/DeleteCPEntity(ppid, it.parentId, it.uiMode)))
            }
        }
        _deleteByPostId(postId)
    }




    @Query("""
        update cp_entity set progress=:progress, is_localcard_shown=:isLocalCardShown, 
        state=:state, message=:snackbarMessage, group_joined=:groupJoined  where cpId=:cpId
    """)
    abstract fun _updatePostState(cpId: Int,
                                  progress: Int,
                                  isLocalCardShown: Boolean,
                                  state: PostUploadStatus,
                                  snackbarMessage: String?,
                                  groupJoined: Boolean?)

    @Transaction
    open fun updatePostState(cpId: Int, progress: Int,
                             isLocalCardShown: Boolean,
                             state: PostUploadStatus,
                             fetchDao: FetchDao,
                             snackbarMessage: String?,
                             groupJoined: Boolean?) {
        _updatePostState(cpId, progress, isLocalCardShown, state, snackbarMessage, groupJoined)
        postIdByCpId(cpId)?.let {
            fetchDao.updateLocalProgressStatebyPostid(it, progress, state)
        }
    }

    @Query("SELECT retry_count from cp_entity WHERE cpId=:cpId")
    abstract fun getRetryCount(cpId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun _replaceCP(vararg t: CreatePostEntity): Array<Long>

    @Transaction
    open fun replaceCP(vararg t: CreatePostEntity): Array<Long> {
        t.forEach { cp ->
            val parentPostId = cp.parentPostId
            val parentId = cp.parentId
            val uiMode = cp.uiMode
            if (uiMode.isCommentOrRepost() && parentPostId != null) {
                    val lookupCards = lookupCard(parentPostId)
                    val cardsInc = lookupCards.map { card ->
                        val counts2 = card.i_counts() ?: Counts2()
                        val updatedCounts = when {
                            uiMode == CreatePostUiMode.COMMENT -> counts2.incrementCommentCount()
                            uiMode == CreatePostUiMode.REPOST -> counts2.incrementRepostCount()
                            else -> counts2
                        }
                        card.withNewCounts(updatedCounts)
                    }
                    updatePosts(cardsInc)
                if (parentId != parentPostId && !parentId.isNullOrEmpty()) {
                    val newList = lookupCard(parentId).map { parent ->
                        val childCards = parent.i_collectionItems()?.filterIsInstance<PostEntity>()?.map {
                            if (it.i_id() == parentPostId) {
                                val counts2 = (it.i_counts() ?: Counts2())
                                val updatedCounts = when {
                                    uiMode == CreatePostUiMode.COMMENT -> counts2.incrementCommentCount()
                                    uiMode == CreatePostUiMode.REPOST -> counts2.incrementRepostCount()
                                    else -> counts2
                                }
                                it.withNewCounts(updatedCounts)
                            } else {
                                it
                            }
                        }
                        val childMoreCards = parent.i_moreStories()?.filterIsInstance<PostEntity>()?.map {
                            if (it.i_id() == parentPostId) {
                                val counts2 = (it.i_counts() ?: Counts2())
                                val updatedCounts = when {
                                    uiMode == CreatePostUiMode.COMMENT -> counts2.incrementCommentCount()
                                    uiMode == CreatePostUiMode.REPOST -> counts2.incrementRepostCount()
                                    else -> counts2
                                }
                                it.withNewCounts(updatedCounts)
                            } else {
                                it
                            }
                        }
                        parent.copy(postEntity = parent.postEntity.copy(collectionAsset = parent
                                .postEntity.collectionAsset?.copy(collectionItem = childCards), moreStories = childMoreCards))
                    }
                    updatePosts(newList)
                }
            }
        }
        return _replaceCP(*t)
    }


    @Query("SELECT * FROM $TABLE_CARD WHERE id  = :id")
    abstract fun lookupCard(id: String) : List<Card> // is a list because of levels
    @Update
    abstract fun updatePosts(list: List<Card>)

    @Update
    abstract fun update(entity: CreatePostEntity)

    @Query("SELECT * FROM local_posts_view")
    @VisibleForTesting
    abstract fun allLocalPost(): List<Card>

    @Query("SELECT COUNT(cpId) FROM  cp_entity")
    @VisibleForTesting
    internal abstract fun rowCount(): Int

    companion object {
        // delete session that are 3 days old in the table
        var delete_dangling_cp_entries_new_session = """
            DELETE FROM cp_entity WHERE creation_date <= ${System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L }
        """

        @Language("RoomSql")
        const val trigger_delete_local_from_posts = """
          
        CREATE TRIGGER trigger_delete_local_from_posts AFTER
        DELETE ON cp_entity BEGIN
        DELETE
        FROM $TABLE_CARD
        WHERE id = OLD.post_id
          AND LEVEL = 'LOCAL'; END
        """
    }
}
