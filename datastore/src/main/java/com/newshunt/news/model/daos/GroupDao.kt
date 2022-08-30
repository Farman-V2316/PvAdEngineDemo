/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.model.entity.GROUP_ID_QUERY_PARAM_KEY
import com.newshunt.dataentity.model.entity.GROUP_INFO_ID
import com.newshunt.dataentity.model.entity.GROUP_INFO_TABLE_NAME
import com.newshunt.dataentity.model.entity.GROUP_USER_ID
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.MEMBERS_TABLE_NAME
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.VIEW_FeedPage

/**
 * All group related Daos can be placed in this file
 * <p>
 * Created by srikanth.ramaswamy on 09/23/2019.
 */
@Dao
interface GroupInfoDao : BaseDao<GroupInfo>, ListFetchDao<GroupInfo> {

    @Query("""
        SELECT DISTINCT g.id, g.*
        FROM fetch_data f
        LEFT JOIN groupinfo g ON f.storyId = g.id
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1) 
        AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId
                        AND section = :section)
        ORDER BY f.pageNum ASC, f.indexInPage ASC
""")
    override fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int, GroupInfo>

    @Query("SELECT * FROM $GROUP_INFO_TABLE_NAME WHERE $GROUP_INFO_ID = :groupId AND $GROUP_USER_ID = :userId")
    fun fetch(groupId: String, userId: String): LiveData<GroupInfo?>

    @Query("SELECT * FROM $GROUP_INFO_TABLE_NAME WHERE handle = :handle AND $GROUP_USER_ID = :userId")
    fun fetchWithHandle(handle: String, userId: String): LiveData<GroupInfo?>

    @Query("DELETE FROM $GROUP_INFO_TABLE_NAME WHERE $GROUP_INFO_ID = :groupId AND $GROUP_USER_ID = :userId")
    fun delete(groupId: String, userId: String)

    @Query("DELETE FROM $GROUP_INFO_TABLE_NAME")
    fun deleteAll()

    @Transaction
    fun replaceFirstPage(fetchDao: FetchDao,
                         fe: FetchInfoEntity,
                         items: List<GroupInfo>,
                         requestUrl: String) {
        try {
            // update groupInfo table
            insReplace(items)
            // update fetch info
            fetchDao.updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, fe
                    .currentPageNum.toString(), fe.section)
            // replace fetch data
            val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId?:fetchDao.insIgnore(fe)
            fetchDao.deleteFetchDataMatching(fetchId, requestUrl)
            fetchDao.insReplaceFetchData(items.mapIndexed { index, groupInfo ->
                FetchDataEntity(fetchId, fe.currentPageNum, index, groupInfo.id, Format
                        .GROUP_INVITE, requestUrl)
            })
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    @Transaction
    fun appendNextPage(fetchDao: FetchDao,
                       fe: FetchInfoEntity,
                       items: List<GroupInfo>,
                       requestUrl: String) {
        val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId?:
                fetchDao.insIgnore(fe)
        val pgNum = fetchDao.maxPageNumInFetchData(fetchId) ?: fe.currentPageNum

        // update groupInfo table
        insReplace(items)
        // update fetch info
        fetchDao.updateNpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, pgNum.toString(), fe.section)

        // append to fetch data
        fetchDao.insReplaceFetchData(items.mapIndexed { index, groupInfo ->
            FetchDataEntity(fetchId, pgNum, index, groupInfo.id, Format.GROUP_INVITE, requestUrl)
        })
    }
}

/**
 * All members related Daos can be placed in this file
 *
 * @author raunak.yadav
 */
@Dao
interface MemberDao : BaseDao<Member>, ListFetchDao<Member> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(members: List<Member>): List<Long>

    @Query("""
        SELECT DISTINCT m.member_id, m.*
        FROM fetch_data f LEFT JOIN members m 
        ON f.storyId = m.member_id
        WHERE f.fetchId = (select col_fetchInfoId from fetch_info where col_entity_id= :pageId and col_disp_loc= :location and section=:section LIMIT 1) 
        AND f.reqUrl = (SELECT contentUrl FROM $VIEW_FeedPage WHERE $VIEW_FeedPage.id = :pageId 
                        AND section = :section)
        ORDER BY f.pageNum ASC, f.indexInPage ASC
  """)
    override fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int, Member>

    @Query("DELETE FROM $MEMBERS_TABLE_NAME WHERE $GROUP_ID_QUERY_PARAM_KEY = :groupId AND " +
            "$GROUP_USER_ID = :userId AND ${Member.GROUP_MEMBERSHIP_STATUS} = :membership")
    fun delete(groupId: String, userId: String, membership: MembershipStatus)

    @Query("DELETE FROM $MEMBERS_TABLE_NAME")
    fun deleteAll()

    @Transaction
    fun replaceFirstPage(fetchDao: FetchDao,
                         fe: FetchInfoEntity,
                         items: List<Member>,
                         requestUrl: String) {
        try {
            //Insert into member table
            val memberIds = insertReplace(items)
            // update fetch info
            fetchDao.updateFpFetchInfo(fe.entityId, fe.location, fe.npUrlOf1stResponse, fe
                    .currentPageNum.toString(), fe.section)
            // replace fetch data
            val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId
                    ?: fetchDao.insIgnore(fe)
            fetchDao.deleteFetchDataMatching(fetchId, requestUrl)
            fetchDao.insReplaceFetchData(memberIds.mapIndexed { index, memberId ->
                FetchDataEntity(fetchId, fe.currentPageNum, index, memberId.toString(), Format.MEMBER, requestUrl)
            })
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    @Transaction
    fun appendNextPage(fetchDao: FetchDao, fe: FetchInfoEntity, items: List<Member>, requestUrl: String) {
        val fetchId = fetchDao.fetchInfo(fe.entityId, fe.location, fe.section)?.fetchInfoId?:fetchDao.insIgnore(fe)
        val pgNum = fetchDao.maxPageNumInFetchData(fetchId) ?: fe.currentPageNum

        //Insert into member table
        val memberIds = insertReplace(items)
        // update fetch info
        fetchDao.updateNpFetchInfo(fe.entityId, fe.location, fe.nextPageUrl, pgNum.toString(), fe.section)

        // append to fetch data
        fetchDao.insReplaceFetchData(memberIds.mapIndexed { index, memberId ->
            FetchDataEntity(fetchId, pgNum, index, memberId.toString(), Format.MEMBER, requestUrl)
        })
    }

    @Query("UPDATE $MEMBERS_TABLE_NAME SET ${Member.GROUP_MEMBERSHIP_STATUS} = :membership WHERE ${Member.COL_MEMBER_ID} =:memberId")
    fun update(membership: MembershipStatus, memberId: Long)
}