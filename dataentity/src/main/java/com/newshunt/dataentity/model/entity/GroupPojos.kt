/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.model.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.Status
import com.newshunt.dataentity.model.entity.Member.Companion.COL_MEMBER_UI_TYPE
import com.newshunt.dataentity.model.entity.Member.Companion.GROUP_MEMBERSHIP_STATUS
import java.io.Serializable


const val GROUP_INFO_KEY = "GROUP_INFO"
const val GROUP_ID_KEY = "GROUP_ID"
const val GROUP_MY_ROLE = "GROUP_MY_ROLE"
const val GROUP_INFO_ID = "id"
const val GROUP_USER_ID = "userId"
const val GROUP_INFO_TABLE_NAME = "groupinfo"
const val GROUP_ID_QUERY_PARAM_KEY = "groupId"
const val DELETE_GROUP = "Delete"
const val LEAVE_GROUP = "Leave"
const val BUNDLE_APPROVAL_PREFERRED_TAB_TYPE = "approvalPreferredTabType"
const val BUNDLE_MEMBERS_INVITES = "BUNDLE_MEMBERS_INVITES"
const val MEMBER_LIST_TAB_TYPE = "member_list"
const val INVITATION_TAB_TYPE = "invitation"
const val MEMBER_APPROVAL_TAB_TYPE = "member_approval"
const val INVITATION_APPROVAL_TAB_TYPE = "invitation_approval"
const val POST_APPROVAL_TAB_TYPE = "post_approval"

const val MEMBERS_TABLE_NAME = "members"
const val SMS_BODY = "sms_body"
const val PHONEBOOK_SEARCH_QUERY = "phonebook_search_query"

/**
 * Basic Group meta
 */
open class GroupBaseInfo : Serializable {
    var id: String = Constants.EMPTY_STRING //group id
    var userId: String = Constants.EMPTY_STRING //User id of the user
    var handle: String? = null //handle of the group
    var name: String? = null //Name of the group
    var description: String? = null //description of the group
    var coverImage: String? = null //cover image of the group
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupBaseInfo) return false

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (handle != other.handle) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (coverImage != other.coverImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + (handle?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (coverImage?.hashCode() ?: 0)
        return result
    }
}

/**
 * All data that defines a group completely.
 */
@Entity(tableName = GROUP_INFO_TABLE_NAME, primaryKeys = [GROUP_INFO_ID])
open class GroupInfo : GroupBaseInfo(), Serializable, SocialHandleInfo, AnyCard {
    var shareUrl: String? = null //share url of the group
    var contentUrl: String? = null //content url for posts in the group
    var referralString: String? = null
    var metadata: String? = null
    var additionalStats: String? = null //Meta data of the group: ex: no. of members
    var topMembersPhotos: List<String>? = null //Latest members in the group
    var memberApproval: SettingState? = SettingState.NOT_REQUIRED //member join needs approval?
        get() = field ?: SettingState.NOT_REQUIRED
    var postApproval: SettingState? = SettingState.NOT_REQUIRED //post needs approval?
        get() = field ?: SettingState.NOT_REQUIRED
    var privacy: SocialPrivacy? = SocialPrivacy.PUBLIC //can this group be searched?
        get() = field ?: SocialPrivacy.PUBLIC
    var status: String? = null //status active/inactive etc.
    var createdBy: String? = null //owner's userid
    var createdOnMillis: Long? = 0L //created time epoch
    var lastUpdatedOnMillis: Long? = 0L //last updated time epoch
    var userRole: MemberRole? = MemberRole.NONE //this user's role
        get() = field ?: MemberRole.NONE
    var membership: MembershipStatus? = MembershipStatus.NONE //Status of approval for this user
        get() = field ?: MembershipStatus.NONE
    var canDelete = false //Can this user delete the group?
    var membersCount = 0 //Total member could in group

    override fun s_id(): String = id
    override fun s_groupId(): String? = id
    override fun s_handle(): String? = handle
    override fun s_displayName(): String? = name
    override fun s_metaData(): String? = metadata
    override fun s_profileImage(): String? = coverImage
    override fun s_referralString(): String? = referralString

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupInfo) return false
        if (!super.equals(other)) return false

        if (shareUrl != other.shareUrl) return false
        if (contentUrl != other.contentUrl) return false
        if (referralString != other.referralString) return false
        if (metadata != other.metadata) return false
        if (additionalStats != other.additionalStats) return false
        if (topMembersPhotos != other.topMembersPhotos) return false
        if (status != other.status) return false
        if (createdBy != other.createdBy) return false
        if (createdOnMillis != other.createdOnMillis) return false
        if (lastUpdatedOnMillis != other.lastUpdatedOnMillis) return false
        if (canDelete != other.canDelete) return false
        if (membersCount != other.membersCount) return false
        if (memberApproval != other.memberApproval) return false
        if (postApproval != other.postApproval) return false
        if (privacy != other.privacy) return false
        if (userRole != other.userRole) return false
        if (membership != other.membership) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (shareUrl?.hashCode() ?: 0)
        result = 31 * result + (contentUrl?.hashCode() ?: 0)
        result = 31 * result + (referralString?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (additionalStats?.hashCode() ?: 0)
        result = 31 * result + (topMembersPhotos?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (createdBy?.hashCode() ?: 0)
        result = 31 * result + (createdOnMillis?.hashCode() ?: 0)
        result = 31 * result + (lastUpdatedOnMillis?.hashCode() ?: 0)
        result = 31 * result + canDelete.hashCode()
        result = 31 * result + membersCount
        result = 31 * result + (memberApproval?.hashCode() ?: 0)
        result = 31 * result + (postApproval?.hashCode() ?:0)
        result = 31 * result + (privacy?.hashCode() ?:0)
        result = 31 * result + (userRole?.hashCode() ?:0)
        result = 31 * result + (membership?.hashCode() ?:0)
        return result
    }
}

@Entity(tableName = MEMBERS_TABLE_NAME)
data class Member(
        @ColumnInfo(name = COL_MEMBER_ID) @PrimaryKey(autoGenerate = true) val m_id: Long = 0L,
        val groupId: String = Constants.EMPTY_STRING,
        val membership: MembershipStatus? = MembershipStatus.NONE,
        val metadata: String? = null,
        val referralString: String? = null
) : UserBaseProfile(), SocialHandleInfo, AnyCard {
    var role: MemberRole? = null
        get() = field ?: MemberRole.NONE
    var uiType: UiType2 = UiType2.GRP_MEMBER

    override fun s_id(): String = userId
    override fun s_groupId(): String? = groupId
    override fun s_handle(): String? = handle
    override fun s_displayName(): String? = name
    override fun s_badge(): String? = role?.name
    override fun s_metaData(): String? = metadata
    override fun s_profileImage(): String? = profileImage
    override fun s_referralString(): String? = referralString
    override fun is_profileVerified(): Boolean = badgeType.isNullOrEmpty().not()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        if (!super.equals(other)) return false

        if (m_id != other.m_id) return false
        if (groupId != other.groupId) return false
        if (membership != other.membership) return false
        if (metadata != other.metadata) return false
        if (referralString != other.referralString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + m_id.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + membership.hashCode()
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (referralString?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val COL_MEMBER_ID = "member_id"
        const val GROUP_MEMBERSHIP_STATUS = "membership"
        const val COL_MEMBER_UI_TYPE = "uiType"
    }
}

interface SocialHandleInfo {
    fun s_id(): String
    fun s_groupId(): String?
    fun s_handle(): String?
    fun s_displayName(): String?
    fun s_badge(): String? = Constants.EMPTY_STRING
    fun s_metaData(): String?
    fun s_profileImage(): String?
    fun s_referralString(): String?
    fun is_profileVerified(): Boolean? = false
}

/**
 * Types of role assigned to group members.
 * Role's authority increases as ordinal increases.
 */
enum class MemberRole {
    NONE,
    MEMBER,
    ADMIN,
    OWNER;

    companion object {
        @JvmStatic
        fun fromName(role: String?): MemberRole {
            for (memberRole in values()) {
                if (memberRole.name == role) {
                    return memberRole
                }
            }
            return NONE
        }
    }

    fun isOwnerOrAdmin(): Boolean {
        return this == OWNER || this == ADMIN
    }

    fun isMember(): Boolean {
        return when (this) {
            ADMIN, OWNER, MEMBER -> {
                true
            }
            else -> {
                false
            }

        }
    }
}

enum class ApprovalAction : Serializable {
    APPROVED,
    DECLINED,
    BLOCKED
}

enum class MembershipStatus : Serializable {
    INVITED, // user has been invited
    AWAITED, //user has requested to join
    APPROVED,
    DECLINED,
    BLOCKED,
    NONE;

    companion object {
        @JvmStatic
        fun fromName(status: String?): MembershipStatus {
            for (membershipStatus in values()) {
                if (membershipStatus.name == status) {
                    return membershipStatus
                }
            }
            return NONE
        }
    }
}

enum class ReviewItem(val deepLinkValue: String?) {
    GROUP_POST("posts"),
    GROUP_MEMBER("members"),
    GROUP_INVITATION("invites");

    companion object {

        @JvmStatic
        fun from(tabType: String): ReviewItem {
            values().forEach {
                if (CommonUtils.equals(tabType, it.deepLinkValue))
                    return it
            }
            return GROUP_POST
        }
    }

}

data class ApprovalTab(var entityId: String?,
                       val name: String?,   //Name in app lang. Server controlled
                       val tabType: ReviewItem? = ReviewItem.GROUP_INVITATION,
                       val contentUrl: String? = null) : Serializable

enum class SettingState {
    REQUIRED,
    NOT_REQUIRED;

    companion object {
        @JvmStatic
        fun fromName(settingState: String?): SettingState {
            for (state in values()) {
                if (state.name == settingState) {
                    return state
                }
            }
            return NOT_REQUIRED
        }
    }
}


enum class EditMode {
    CREATE,
    UPDATE
}

enum class ChangedSettingsName {
    MEMBER_APPROVAL,
    POST_APPROVAL,
    PRIVACY
}

data class ReviewActionBody(val id: String? = null, // id of item being reviewed
                            @Transient val reviewItem: ReviewItem, //for internal use
                            val groupId: String? = null,
                            var action: ApprovalAction? = null)

class SettingsPostBody : Serializable {
    var id: String = Constants.EMPTY_STRING
    @Transient
    var userId: String = Constants.EMPTY_STRING
    var name: String? = null
    var value: String? = null
}

data class ChangeRolePostBody(val userId: String,
                              val groupId: String,
                              val role: MemberRole,
                              @Transient val myUserId: String = Constants.EMPTY_STRING) : Serializable

data class ImageResponseBody(var code: Int, var status: Status?, var paths: List<String>?)

const val SUCCESS = "success"
const val NOT_FOUND = "not_found"
const val INVALID_SIZE = "invalid_size"

/**
 * To specify the calling view for a paginated list.
 * The names are intentionally short since we dont need to read it any where and its used as keys
 * in db along with group ids.
 */
enum class GroupLocations {
    G_D, //Group detail
    G_M_L, //Group member list
    G_M_L_I, //Group member list invite
    G_A_M,  // Approvals_Member
    G_A_I,  // Approvals_Invites
    G_A_P,   // Approvals_Post
    BOOKMARKS_LIST
}

/**
 * Configuration of the invitation screen. Defines the list of apps to show, invitation message,
 * content url to fetch the suggested members etc.
 */
data class GroupInviteConfig(val version: String = Constants.EMPTY_STRING,
                             val inviteMediums: List<InvitationMedium>,
                             val contentUrl: String,
                             val invitationMsg: String? = Constants.EMPTY_STRING) : Serializable

/**
 * Various medium through which invitations can be shared
 */
enum class InvitationOption : Serializable {
    PHONE_BOOK,
    APP,
    COPY_LINK,
    GENERIC_SHARE
}

/**
 * Metadata for each invitation medium
 */
data class InvitationMedium(val invitationOption: InvitationOption,
                            val invitationAppData: InvitationAppData) : Serializable

/**
 * Metadata for the apps through which invitations can be shared
 */
data class InvitationAppData(val pkgName: String?,
                             val iconUrl: String?,
                             val name: String) : Serializable

/**
 * Post body of the invitation API
 */
data class InvitationPostBody(val groupId: String,
                              val userIds: List<String>)

data class ApprovalTabsInfo(val version: String = Constants.EMPTY_STRING,
                            val tabs: List<ApprovalTab>?) : Serializable

/**
 * Helper POJO to combine GroupInviteConfig and GroupInfo in a single object
 */
data class InviteConfigWithGroupInfo(val inviteConfig: GroupInviteConfig,
                                     val groupInfo: GroupInfo)