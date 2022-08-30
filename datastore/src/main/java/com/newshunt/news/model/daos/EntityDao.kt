package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.pages.*
import com.newshunt.dataentity.social.entity.*

@Dao
abstract class EntityInfoDao : BaseDao<EntityPojo> {

    @Query("SELECT * FROM $VIEW_EntityInfoView WHERE id=:id OR legacyKey=:legacyKey AND section=:section AND parentId=:parentId")
    abstract fun getEntityList(id: String, section: String, legacyKey: String? = null, parentId: String = Constants.EMPTY_STRING): LiveData<EntityInfoList?>

    @Query("DELETE FROM entityInfo")
    abstract fun cleanUpEntityInfo()

    @Query("DELETE FROM entityInfo WHERE id=:id OR legacyKey=:id AND parentId=:parentId")
    abstract fun deleteItem(id: String, parentId: String = Constants.EMPTY_STRING)

    @Query("DELETE FROM entityInfo WHERE parentId=:parentId")
    abstract fun deleteChilds(parentId: String)

    @Query("SELECT * FROM entityInfo WHERE (id=:id OR legacyKey=:id) AND parentId=:parentId")
    abstract fun getEntity(id: String, parentId: String = Constants.EMPTY_STRING): EntityPojo?

    @Transaction
    open fun insertEntityKids(entityInfoResponse: EntityInfoResponse, section: String) {

        deleteItem(entityInfoResponse.parent.id)
        insReplace(EntityPojo(pageEntity = entityInfoResponse.parent, parentId = Constants.EMPTY_STRING, section = section))

        deleteChilds(entityInfoResponse.parent.id)
        entityInfoResponse.kids?.forEach {
            insReplace(EntityPojo(pageEntity = it, parentId = entityInfoResponse.parent.id, section = section))
        }
    }

    @Transaction
    open fun clearEntity(id: String) {
        val pojo = getEntity(id)
        if (pojo != null) {
            deleteItem(pojo.pageEntity.id)
            deleteChilds(pojo.pageEntity.id)
        }
    }
}

@Dao
abstract class FollowEntityDao : BaseDao<FollowSyncEntity> {

    @Query("SELECT * FROM follow")
    abstract fun getFollowList(): List<FollowSyncEntity>

    @Query("SELECT * FROM follow WHERE isSynced=0 AND `action` NOT IN ('MANAGE' , 'JOIN')")
    abstract fun getUnsyncedFollows(): List<FollowSyncEntity>

    @Query("SELECT * FROM follow WHERE `action`=:action AND entityType IN (:entityType) AND (entitySubType IN (:entitySubType) OR entitySubType IS NULL) ORDER BY actionTime DESC")
    abstract fun getItemsToFillFilter(action: String, entityType: List<String>?, entitySubType: List<String>?): List<FollowSyncEntity>

    @Query("SELECT COUNT(entityId) FROM follow WHERE `action`='BLOCK'")
    abstract fun getBlockCount(): LiveData<Int>

    @Query("SELECT COUNT(entityId) FROM follow WHERE `action` = :action and entityType = 'SOURCE' OR entityType = 'COMMUNITY_GROUP' and `action` = :groupActionType")
    abstract fun getSourceCountByAction(action: String?, groupActionType: String?): Int

    @Query("SELECT count(*) FROM follow WHERE `action` = 'FOLLOW'and entityType = 'LOCATION'")
    abstract fun getfollowCount(): LiveData<Int>

    @Query("""
      SELECT * from follow 
      WHERE isSynced = 0 AND actionTime > :newerThan AND `action` IN ('FOLLOW', 'UNFOLLOW')
      ORDER BY actionTime DESC LIMIT 1
      """)
    abstract fun getFollowedNames(newerThan: Long): LiveData<List<FollowSyncEntity>>

    @Query("SELECT * FROM follow WHERE entityId=:id AND `action`='FOLLOW'")
    abstract fun isFollowed(id: String): FollowSyncEntity?

    @Query("SELECT * FROM follow WHERE entityId=:id AND `action`='BLOCK'")
    abstract fun isBlocked(id: String): FollowSyncEntity?

    @Query("SELECT entityId FROM follow WHERE entityId=:id AND `action`='BLOCK'")
    abstract fun isBlockedUser(id: String): LiveData<String?>

    @Query("SELECT * FROM follow WHERE entityId=:id AND `action` IN ('BLOCK','FOLLOW')")
    abstract fun isFollowedOrBlocked(id: String): FollowSyncEntity?

    @Query("SELECT entityId FROM follow WHERE entityId IN (:list) AND `action`='FOLLOW'")
    abstract fun getFollowsFromList(list: List<String>): List<String>?

    @Query("SELECT * FROM follow WHERE `action` ='FOLLOW'")
    abstract fun getAllFollows(): LiveData<List<FollowSyncEntity>>

    @Query("SELECT * FROM follow WHERE `action` ='FOLLOW' and entityType = 'LOCATION'")
    abstract fun getAllFollowedLocations(): LiveData<List<FollowSyncEntity>>

    @Query("SELECT COUNT(*) FROM follow WHERE `action` ='FOLLOW' and entityType = 'LOCATION'")
    abstract fun getAllFollowedLocationsCount(): LiveData<Int>

    @Query(" SELECT * FROM follow F1 WHERE actionTime IN (SELECT max(actionTime) FROM follow F2 WHERE entityType = 'LOCATION')")
    abstract fun getLatestFollowedLocation(): LiveData<List<FollowSyncEntity>>

    @Query("SELECT * FROM follow F1 where entityType = 'LOCATION'  ORDER BY actionTime DESC")
    abstract fun getFollowedLocationsFIFO(): LiveData<List<FollowSyncEntity>>


    @Transaction
    open fun toggleFollowItems(followSyncEntity: FollowSyncEntity) {

        val entity = getFollowEntity(followSyncEntity.actionableEntity.entityId)
        when {
            entity == null -> insReplace(followSyncEntity)
            /* blocked from menu, and clicked on follow button, it should go to follow state*/
            (entity.action == FollowActionType.BLOCK || entity.action == FollowActionType.UNBLOCK)
                    && followSyncEntity.action == FollowActionType.FOLLOW -> insReplace(followSyncEntity)
            (entity.action == FollowActionType.FOLLOW || entity.action == FollowActionType.UNFOLLOW)
                    && followSyncEntity.action == FollowActionType.BLOCK -> insReplace(followSyncEntity)
            else -> toggleFollowEntity(followSyncEntity.actionableEntity.entityId)
        }
    }


    @Transaction
    open fun toggleFollowItems(actionableEntities: List<ActionableEntity>,  actionType:FollowActionType) {

        actionableEntities.forEach { actionableEntity ->

            val followSyncEntity = FollowSyncEntity(actionableEntity, action = actionType)
            val entity = getFollowEntity(actionableEntity.entityId)
            when {
                entity == null -> insReplace(followSyncEntity)
                /* blocked from menu, and clicked on follow button, it should go to follow state*/
                (entity.action == FollowActionType.BLOCK || entity.action == FollowActionType.UNBLOCK)
                        && followSyncEntity.action == FollowActionType.FOLLOW -> insReplace(followSyncEntity)
                else -> toggleFollowEntity(followSyncEntity.actionableEntity.entityId)
            }

        }
    }

    @Transaction
    open fun updatedbAfterSync(syncTime: Long) {
        markItemSynced(syncTime)
        deleteAfterSync(syncTime, FollowActionType.UNFOLLOW, FollowActionType.UNBLOCK)
    }

    @Query("UPDATE follow SET isSynced=1 WHERE actionTime<:syncTime")
    abstract fun markItemSynced(syncTime: Long)

    @Query("SELECT * FROM follow WHERE entityId=:id")
    abstract fun getFollowEntity(id: String): FollowSyncEntity?

    @Query("DELETE FROM follow WHERE isSynced=1 AND actionTime<:syncTime AND `action` IN (:actionList)")
    abstract fun deleteAfterSync(syncTime: Long, vararg actionList: FollowActionType)

    @Query("DELETE FROM follow")
    abstract fun deleteAll()

    @Query("DELETE FROM follow WHERE isSynced=1")
    abstract fun deleteSynced()

    @Query("""
    UPDATE follow SET isSynced = 0, `action` = (CASE
    WHEN `action`='FOLLOW' THEN 'UNFOLLOW'
    WHEN `action`='UNFOLLOW' THEN 'FOLLOW'
    WHEN `action`='BLOCK' THEN 'UNBLOCK'
    WHEN `action`='UNBLOCK' THEN 'BLOCK' ELSE 'FOLLOW' END), actionTime = :ts
    WHERE entityId=:id
  """)
    abstract fun toggleFollowEntity(id: String, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM follow WHERE entityId=:id")
    abstract fun fetchEntity(id: String): LiveData<List<FollowSyncEntity?>>

    /**
     * We need union of lastN and recentX. - hence 2 subqueries and union
     */
    @Query("""
       SELECT entityId id, entityType type, entitySubType subType, actionTime 
       FROM follow 
       WHERE actionTime >= :notOlderThan AND `action` = :action ORDER BY actionTime DESC
""")
    abstract fun recentActions(notOlderThan: Long, action: String = "FOLLOW"): List<CardsPayload.P_Follow>

    @Query("""
    SELECT count(*) FROM follow WHERE `action` = 'FOLLOW'
  """)
    abstract fun userHasAnyFollows(): Boolean

    @Transaction
    open fun isSourceIdBlocked(sourceId: String): Boolean{
        val followSyncEntity = getFollowEntity(sourceId)
        if(followSyncEntity != null && followSyncEntity.action == FollowActionType.BLOCK){
            return true
        }
        return false
    }
}

@Dao
abstract class UserFollowDao : BaseDao<UserFollowEntity>, ListFetchDao<UserFollowView> {

    @Query("""
   SELECT DISTINCT uf.*,
        CASE WHEN fl.`action`= 'FOLLOW' THEN 1 ELSE 0 END isFollowing,
        CASE WHEN fl.`action`= 'BLOCK' THEN 1 ELSE 0 END isBlocked,
        CASE WHEN p.id IS NULL THEN 0 ELSE 1 END isFavorite
        FROM fetch_data f 
        LEFT JOIN userFollow uf ON f.storyId = uf.entityId
        LEFT JOIN pages p ON uf.entityId=p.id AND p.section='news'
        LEFT JOIN follow fl ON uf.entityId = fl.entityId AND `action` IN ('FOLLOW','BLOCK')
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1) AND uf.fetchEntity=:pageId
        ORDER BY f.pageNum ASC, f.indexInPage ASC
  """)
    abstract override fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int, UserFollowView>

    @Transaction
    open fun replaceFirstPage(fetchDao: FetchDao,
                              fe: FetchInfoEntity,
                              items: List<UserFollowEntity>,
                              requestUrl: String) {
        try {
            // update groupInfo table
            insReplace(items)
            // update fetch info
            fetchDao.updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, fe
                    .currentPageNum.toString(), fe.section)
            // replace fetch data
            val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId
                    ?: fetchDao.insIgnore(fe)
            fetchDao.deleteFetchDataMatching(fetchId, Constants.EMPTY_STRING)
            fetchDao.insReplaceFetchData(items.mapIndexed { index, userFollowEntity ->
                FetchDataEntity(fetchId, fe.currentPageNum, index, userFollowEntity.actionableEntity
                        .entityId, Format.ENTITY, Constants.EMPTY_STRING)
            })
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    @Transaction
    open fun appendNextPage(fetchDao: FetchDao, fe: FetchInfoEntity, items: List<UserFollowEntity>,
                            requestUrl: String) {
        val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId
                ?: fetchDao.insIgnore(fe)
        val pgNum = fetchDao.maxPageNumInFetchData(fetchId) ?: fe.currentPageNum

        // update groupInfo table
        insReplace(items)
        // update fetch info
        fetchDao.updateNpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, pgNum.toString(), fe.section)

        // append to fetch data
        fetchDao.insReplaceFetchData(items.mapIndexed { index, userFollowEntity ->
            FetchDataEntity(fetchId, pgNum, index, userFollowEntity.actionableEntity.entityId, Format.ENTITY, Constants.EMPTY_STRING)
        })
    }

    @Query("DELETE FROM userFollow WHERE fetchEntity=:entityId")
    abstract fun cleanUpUserFollowTable(entityId: String)
}

@Dao
abstract class ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(reportEntry:ReportEntity)

    @Query("DELETE FROM report")
    abstract fun clearAll()


    companion object {
        var delete_old_reported_comments_new_session = """
           DELETE FROM report WHERE entityId NOT IN (SELECT entityId FROM report ORDER BY `rowId`DESC LIMIT 3)
        """

    }
}
