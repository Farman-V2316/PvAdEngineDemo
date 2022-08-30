/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.group.buildLocationForGroupDao
import com.newshunt.appview.common.group.mapGroupInfoResponse
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupInviteConfig
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.InvitationPostBody
import com.newshunt.dataentity.model.entity.InviteConfigWithGroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.model.entity.SettingsPostBody
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.dhutil.model.internal.service.InviteConfigService
import com.newshunt.news.model.daos.MemberDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Named

/**
 * use case implementation to make API call to get group info
 * <p>
 * Created by srikanth.ramaswamy on 09/18/2019.
 */
class FetchGroupInfoUsecase @Inject constructor(private val groupService: GroupService,
                                                private val insertGroupInfoUsecase: InsertGroupInfoUsecase) : Usecase<GroupBaseInfo, GroupInfo> {
    override fun invoke(requestedGroupBaseInfo: GroupBaseInfo): Observable<GroupInfo> {
        return getInfo(requestedGroupBaseInfo)
                .map { apiResponse ->
                    mapGroupInfoResponse(apiResponse, requestedGroupBaseInfo.userId)
                }.flatMap { groupInfo ->
                    insertGroupInfoUsecase.invoke(groupInfo)
                }
    }

    private fun getInfo(requestedGroupBaseInfo: GroupBaseInfo): Observable<ApiResponse<GroupInfo>> {
        requestedGroupBaseInfo.handle?.let {
            if (it.isNotEmpty()) {
                return groupService.getInfoWithHandle(it)
            }
        }
        return groupService.getInfo(requestedGroupBaseInfo.id)
    }
}

/**
 * use case implementation to make API call to join a group
 * <p>
 * Created by srikanth.ramaswamy on 09/20/2019.
 */
class JoinGroupUsecase @Inject constructor(private val groupService: GroupService,
                                           private val insertGroupInfoUsecase: InsertGroupInfoUsecase) : Usecase<GroupBaseInfo, GroupInfo> {
    override fun invoke(requestedGroupBaseInfo: GroupBaseInfo): Observable<GroupInfo> {
        return groupService.join(requestedGroupBaseInfo.id)
                .map { apiResponse ->
                    mapGroupInfoResponse(apiResponse, requestedGroupBaseInfo.userId)
                }.flatMap { groupInfo ->
                    insertGroupInfoUsecase.invoke(groupInfo)
                }
    }
}

/**
 * use case implementation to make API call to leave a group
 * <p>
 * Created by srikanth.ramaswamy on 09/20/2019.
 */
class LeaveGroupUsecase @Inject constructor(private val groupService: GroupService,
                                            private val deleteGroupInfoUsecase: DeleteGroupInfoUsecase) : Usecase<GroupBaseInfo, Boolean> {
    override fun invoke(requestedGroupBaseInfo: GroupBaseInfo): Observable<Boolean> {
        return groupService.leave(requestedGroupBaseInfo.id)
                .flatMap {
                    deleteGroupInfoUsecase.invoke(requestedGroupBaseInfo)
                }
    }
}

/**
 * usecase implementation to run query on group info dao
 *
 * Created by srikanth.ramaswamy on 09/23/2019.
 */
class ReadGroupInfoUsecase @Inject constructor() : MediatorUsecase<GroupBaseInfo, GroupInfo?> {

    private val liveData = MediatorLiveData<Result0<GroupInfo?>>()
    override fun execute(requestedGroupBaseInfo: GroupBaseInfo): Boolean {
        requestedGroupBaseInfo.handle?.let {
            if (it.isNotEmpty()) {
                liveData.addSource(SocialDB.instance().groupInfoDao().fetchWithHandle(it,
                        requestedGroupBaseInfo.userId).distinctUntilChanged()) {
                    liveData.value = Result0.success(it)
                }
                return true
            }
        }
        liveData.addSource(SocialDB.instance().groupInfoDao().fetch(requestedGroupBaseInfo.id,
                requestedGroupBaseInfo.userId).distinctUntilChanged()) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<GroupInfo?>> = liveData
}

/**
 * usecase implementation to insert a GroupInfo into group info table
 *
 * Created by srikanth.ramaswamy on 09/23/2019.
 */
class InsertGroupInfoUsecase @Inject constructor(private val insertIntoGroupDaoUsecase: InsertIntoGroupDaoUsecase) : Usecase<GroupInfo, GroupInfo> {
    private val LOG_TAG = "InsertGroupInfoUsecase"
    override fun invoke(groupInfo: GroupInfo): Observable<GroupInfo> {
        return groupInfo.contentUrl?.let { contentUrl ->
            val feedInfo = GeneralFeed(buildLocationForGroupDao(GroupLocations.G_D, groupInfo.id), contentUrl,
                    Constants.HTTP_POST, PageSection.GROUP.section)
            insertIntoGroupDaoUsecase.invoke(listOf(feedInfo))
        }?.map {
            SocialDB.instance().groupInfoDao().insReplace(groupInfo)
            Logger.d(LOG_TAG, "inserted groupInfo ${groupInfo.id} to DB")
            groupInfo
        } ?: throw IllegalStateException("Invalid content url")
    }
}

/**
 * usecase implementation to delete group from table
 *
 * Created by srikanth.ramaswamy on 09/23/2019.
 */
class DeleteGroupInfoUsecase @Inject constructor() : Usecase<GroupBaseInfo, Boolean> {
    private val LOG_TAG = "DeleteGroupInfoUsecase"
    override fun invoke(groupInfo: GroupBaseInfo): Observable<Boolean> {
        return Observable.fromCallable {
            SocialDB.instance().groupInfoDao().delete(groupInfo.id, groupInfo.userId)
            Logger.d(LOG_TAG, "deleted group ${groupInfo.id}")
            true
        }
    }
}

/**
 * usecase implementation to delete group
 *
 * Created by srikanth.ramaswamy on 09/23/2019.
 */
class DeleteGroupUsecase @Inject constructor(private val groupService: GroupService,
                                             private val deleteGroupInfoUsecase: DeleteGroupInfoUsecase) : Usecase<GroupInfo, Boolean> {
    override fun invoke(groupInfo: GroupInfo): Observable<Boolean> {
        return groupService.delete(groupInfo.id)
                .flatMap {
                    deleteGroupInfoUsecase.invoke(groupInfo)
                }
    }
}

class UpdateSettingsUsecase @Inject constructor(private val groupService: GroupService,
                                                private val insertGroupInfoUsecase: InsertGroupInfoUsecase) : Usecase<SettingsPostBody, GroupInfo> {
    override fun invoke(info: SettingsPostBody): Observable<GroupInfo> {
        return groupService.updateSetting(info).map { apiResponse ->
            mapGroupInfoResponse(apiResponse, info.userId)
        }.flatMap { groupInfo ->
            insertGroupInfoUsecase.invoke(groupInfo)
        }
    }
}

/**
 * usecase implementation to invite users to a group
 *
 * Created by srikanth.ramaswamy on 09/27/2019.
 */
class GroupInviteUsecase @Inject constructor(private val groupService: GroupService,
                                             @Named("groupId")
                                             private val groupId: String,
                                             private val memberDao: MemberDao) : Usecase<List<Member>, Int> {
    override fun invoke(members: List<Member>): Observable<Int> {
        return Observable.fromCallable {
            val userIds = members.map {
                it.userId
            }
            InvitationPostBody(groupId, userIds)
        }.flatMap {
            groupService.invite(it)
        }.map {
            members.forEach { member ->
                memberDao.update(MembershipStatus.INVITED, member.m_id)
            }
            it.code
        }
    }
}

/**
 * usecase implementation to read InviteConfig versioned API response. First try to fetch from
 * cache. If not found in cache, hit the network
 *
 * Created by srikanth.ramaswamy on 09/29/2019.
 */
class ReadInviteConfigUsecase @Inject constructor(private val inviteConfigService: InviteConfigService) : Usecase<Unit, GroupInviteConfig> {
    override fun invoke(input: Unit): Observable<GroupInviteConfig> {
        return inviteConfigService.getConfigFromNetworkIfNoCache()
    }
}

/**
 * usecase implementation to add content url into group feed table
 *
 * Created by srikanth.ramaswamy on 09/30/2019.
 */
class InsertIntoGroupDaoUsecase @Inject constructor(): Usecase<List<GeneralFeed>, List<String>> {
    override fun invoke(feedInfos: List<GeneralFeed>): Observable<List<String>> {
        return Observable.fromCallable {
            val pageIds = ArrayList<String>()
            feedInfos.forEach {
                pageIds.add(it.id)
            }
            Logger.d("InsertIntoGroupDaoUsecase", "Saving fetch info $feedInfos")
            //Insert the id, content url and method into group dao.
            SocialDB.instance().groupDao().insReplace(feedInfos)
            return@fromCallable pageIds
        }
    }
}

/**
 * Usecase implementation to zip group info and invite config as response. Invitations activity
 * and viewmodel need both groupinfo and inviteconfig incase of deeplink. Hence chaining the 2
 * usecases and giving a zipped response through this usecase
 */
class ReadInviteConfigHybridUsecase @Inject constructor(private val fetchGroupInfoUsecase: FetchGroupInfoUsecase,
                                                        private val readInviteConfigUsecase: ReadInviteConfigUsecase) : Usecase<GroupBaseInfo, InviteConfigWithGroupInfo> {
    private val LOG_TAG = "ReadInviteConfigHybridUsecase"

    override fun invoke(groupInfo: GroupBaseInfo): Observable<InviteConfigWithGroupInfo> {
        val grpInfoObservable = if (groupInfo is GroupInfo) {
            Logger.d(LOG_TAG, "Group info already available")
            Observable.just(groupInfo)
        } else {
            Logger.d(LOG_TAG, "Need to fetch GroupInfo from N/W")
            fetchGroupInfoUsecase.invoke(groupInfo)
        }

        val inviteConfigObservable = readInviteConfigUsecase.invoke(Unit)
        return Observable.zip(grpInfoObservable, inviteConfigObservable, BiFunction { grpInfo, invConfig ->
            Logger.d(LOG_TAG, "Zipping InviteConfig with GroupInfo")
            InviteConfigWithGroupInfo(inviteConfig = invConfig, groupInfo = grpInfo)
        })
    }
}