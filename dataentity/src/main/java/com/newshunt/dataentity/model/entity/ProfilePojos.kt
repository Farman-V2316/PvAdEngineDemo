/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.model.entity

import android.net.Uri
import android.webkit.URLUtil
import androidx.room.Embedded
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.news.view.entity.Gender
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import java.io.Serializable

/**
 * A common file to declare all profile related POJOs
 * Created by srikanth.ramaswamy on 04/24/2019.
 */

const val BUNDLE_PROFILE_TAB = "bundle_profile_tab"
const val BUNDLE_PROFILE_VIEW_STATE = "bundle_profile_view_state"
const val KEY_DIALOG_OPTIONS = "KEY_DLG_OPTIONS"
const val SHOW_DIALOG_IMAGES = "SHOW_DLG_IMAGES"
const val FILTER_OPTION_DIALOG = "FILTER_OPTION_DIALOG"
//Key to pass in bundle while launching ProfileActivity
const val PROFILE_USER_DATA_KEY = "USER_DATA"
const val BUNDLE_PROFILE_PREFERRED_TAB_TYPE = "profilePreferredTabType"
const val BUNDLE_DEFAULT_PREFERRED_TAB_ID = "profilePreferredTabId"

const val HISTORY_DATE_PATTERN = "dd MMM, yyyy"
const val HISTORY_TIME_PATTERN = "h:mm a"
const val DEFAULT_PROFILE_NAME_CHAR_LIMIT = 30
const val DEFAULT_DESCRIPTION_CHAR_LIMIT = 400
const val DEFAULT_MAX_CARDS_GUEST = 5
const val DEFAULT_SIGNIN_BEFORE_LOGIN = 5
const val DEFAULT_MAX_RETRIES_OTP = 3
const val DEFAULT_MIGRATION_PING_DELAY = 30 * 60 //30 mins
const val MAX_FILTERS_COUNT = 2
const val DISABLED_FILTER_ALPHA = 0.35f
const val ENABLED_FILTER_ALPHA = 1.0f

/**
 * Various time filters available
 */
enum class TimeFilter : Serializable {
    NINETY_DAYS,
    THIRTY_DAYS,
    SEVEN_DAYS
}

data class RunTimeProfileFilter(val filterPosition: Int = 0,
                                val filterOption: Serializable? = null) : Serializable

/**
 * Base class for all Profile API responses
 */
open class UserBaseProfile : Serializable {
    var name: String? = null   //Name of the user
    var handle: String? = null  //handle
    var subTitle:String? = null //subtitle
    var profileImage: String? = null //profile image
    var userId: String = Constants.EMPTY_STRING //User id
    var sourceId: String? = null
    var persona: ProfilePersona? = null //Persona: CREATOR/NORMAL
    var privacy: SocialPrivacy? = SocialPrivacy.PUBLIC //Privacy: PUBLIC/PRIVATE
    var bio: String? = null //user bio/description
    var location: String? = null //User selected location in json string format
    @Embedded(prefix = "loc_")
    var uiLocation: PostCurrentPlace? = null //User selected location POJO
    var taggingPermission: AccountPermission? = AccountPermission.ALLOWED //Can the user be tagged?
    var invitesPermission: AccountPermission? = AccountPermission.ALLOWED //Can user receive invites?
    var badgeType: String? = Constants.EMPTY_STRING

    fun isProfileVerified(): Boolean = badgeType.isNullOrEmpty().not()
    /**
     * Method to tell whether this user is a creator
     */
    fun isCreator(): Boolean {
        persona?.let {
            it.types?.apply {
                if (this.isNotEmpty()) {
                    this.forEach {
                        if (it.type == ProfilePersonaType.CREATOR) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * Method to tell whether this user's privacy is PRIVATE
     */
    fun isPrivateProfile() = (privacy == SocialPrivacy.PRIVATE)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserBaseProfile) return false

        if (name != other.name) return false
        if (handle != other.handle) return false
        if (profileImage != other.profileImage) return false
        if (userId != other.userId) return false
        if (sourceId != other.sourceId) return false
        if (persona != other.persona) return false
        if (privacy != other.privacy) return false
        if (bio != other.bio) return false
        if (location != other.location) return false
        if (uiLocation != other.uiLocation) return false
        if (taggingPermission != other.taggingPermission) return false
        if (invitesPermission != other.invitesPermission) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (handle?.hashCode() ?: 0)
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        result = 31 * result + userId.hashCode()
        result = 31 * result + (sourceId?.hashCode() ?: 0)
        result = 31 * result + (persona?.hashCode() ?: 0)
        result = 31 * result + privacy.hashCode()
        result = 31 * result + (bio?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (uiLocation?.hashCode() ?: 0)
        result = 31 * result + (taggingPermission?.hashCode() ?: 0)
        result = 31 * result + (invitesPermission?.hashCode() ?: 0)
        return result
    }
}

/**
 * Profile Base API response POJO
 */
open class UserProfile : UserBaseProfile(), Serializable {
    var followingCount: String = Constants.ZERO_STRING //Following count for this user
    var followersCount: String = Constants.ZERO_STRING //Followers count for this user
    var blockedCount: String = Constants.ZERO_STRING //Count of blocked sources
    var profileShareUrl: String? = null //Share url for this profile
    var bannerImageUrl:String? = null // banner image url
    var tabs: List<ProfileTabs>? = null //Tabs added for this profile viewing
    var defaultTabType: ProfileTabType? = null //Default tab to open
    var defaultTabId: String? = null //Default tab Id to open
    val entityType: String? = null  //Entity type for the user
    val entitySubType: String? = null //Entity sub type for the user
    var isFollowing: Boolean = false //Am I following this profile? Modified by client
    var additionalStats: String? = null
    var bioUrl:String? = null
    var moreText:String? = null
    var isNerProfile: Boolean? = false


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProfile) return false
        if (!super.equals(other)) return false

        if (followingCount != other.followingCount) return false
        if (followersCount != other.followersCount) return false
        if (blockedCount != other.blockedCount) return false
        if (profileShareUrl != other.profileShareUrl) return false
        if (tabs != other.tabs) return false
        if (defaultTabType != other.defaultTabType) return false
        if (entityType != other.entityType) return false
        if (entitySubType != other.entitySubType) return false
        if (isFollowing != other.isFollowing) return false
        if (additionalStats != other.additionalStats) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + followingCount.hashCode()
        result = 31 * result + followersCount.hashCode()
        result = 31 * result + blockedCount.hashCode()
        result = 31 * result + (profileShareUrl?.hashCode() ?: 0)
        result = 31 * result + (tabs?.hashCode() ?: 0)
        result = 31 * result + (defaultTabType?.hashCode() ?: 0)
        result = 31 * result + (entityType?.hashCode() ?: 0)
        result = 31 * result + (entitySubType?.hashCode() ?: 0)
        result = 31 * result + isFollowing.hashCode()
        result = 31 * result + (additionalStats?.hashCode() ?: 0)
        return result
    }
}

data class ProfileFilter(val type: String?, val name: String?,
                         val options: List<ProfileFilterOption>?) : Serializable {

    fun fromOptionName(value: String?): ProfileFilterOption? {
        options?.forEach {
            if (CommonUtils.equals(it.value, value)) {
                return it
            }
        }
        return options?.firstOrNull()
    }

    override fun equals(other: Any?): Boolean {
        return other is ProfileFilter && CommonUtils.equals(type, other.type) && CommonUtils.equals(name,
                other.name) && CommonUtils.equals(options, other.options)
    }

    override fun hashCode(): Int {
        return (type + name).hashCode()
    }
}

data class ProfileFilterOption(val displayName: String? = null, val value: String? = null,
                               val iconUrl: String? = null, var allowLocalPost: Boolean? = null) : Serializable {

    fun isValid(): Boolean {
        return !CommonUtils.isEmpty(displayName) && !CommonUtils.isEmpty(value)
    }

    override fun equals(other: Any?): Boolean {
        return other is ProfileFilterOption && CommonUtils.equals(displayName, other.displayName) &&
                CommonUtils.equals(value, other.value) && CommonUtils.equals(iconUrl, other.iconUrl)
    }

    override fun hashCode(): Int {
        return (displayName + value + iconUrl).hashCode()
    }
}

/**
 * Detailed response of the user's profile, deriving from UserBaseProfile
 */
open class MyProfile : UserLoginResponse(), Serializable {
    private var gender: String? = null //Gender of the user
    var dob: String? = null //Date of birth of the user
    var handleEditable = true //Whether or not handle is editable
    var handleNextEditTime: Long? = null //When was the handle edited last?
    var handleEditPrompt: String? = null
    var handleSavePrompt: String? = null
    var mobileDetail: ProfileContact? = null //Mobile number and its status
    var emailDetail: ProfileContact? = null //Email id and its status

    fun gender(): Gender? {
        return Gender.getGender(gender)
    }
}

/**
 * A wrapper class to pass the API response from ViewModel to UI. If success, response is non
 * null and code and message will be empty. If failure, response will be null and code and
 * message will have valid description
 */
data class UIResponseWrapper<T>(val response: T?, //Success case data from ApiResponse
                                val code: String?, //Error code. Client should not care
                                val message: String?) //UI displayable message

/**
 * Tab configuration for Profile View
 */
data class ProfileTabs(val name: String,   //Name in app lang. Server controlled
                       val id:String? = null, // id for each profile tab.
                        val tabType: ProfileTabType = ProfileTabType.HISTORY, //Profile type
                       val tabData:String? = null,
                        val contentUrl: String? = null, //Content url for the tab,
                        val filters: List<ProfileFilter>?,
                        val entityLayout: String? // entity layout type for webFragment vs cardsFragment.
                        ) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other !is ProfileTabs) {
            return false
        }
        return name == other.name &&
                tabType == other.tabType &&
                contentUrl == other.contentUrl &&
                CommonUtils.equals(filters, other.filters)
    }

    override fun hashCode(): Int {
        return (name + tabType).hashCode()
    }

    fun getValidFilters(capacity: Int = MAX_FILTERS_COUNT): List<ProfileFilter>? {
        val validFilters = ArrayList<ProfileFilter>()
        var count = 0
        filters?.let {
            for (filter in it) {
                if (!CommonUtils.isEmpty(filter.type) && !CommonUtils.isEmpty(filter.name)) {

                    val validOptions = ArrayList<ProfileFilterOption>()
                    filter.options?.filterTo(validOptions, { option -> option.isValid() })

                    if (!CommonUtils.isEmpty(validOptions)) {
                        validFilters.add(filter.copy(options = validOptions))
                        if (++count == capacity) {
                            break
                        }
                    }
                }
            }
        }
        return validFilters
    }

    /**
     * Appends a certain url param based on filters.
     */
    fun contentUrlWithAllowLocalCardParam() : String? {
        val url = contentUrl?:return null
        val validUrl = if(URLUtil.isValidUrl(url)) url else return url
        val allowLocalPost = getValidFilters()?.mapNotNull { filter ->
            val option = filter.options?.find { it.value == filter.name }
            option?.allowLocalPost
        }?.any { it }?:false


        return if (!allowLocalPost)
            validUrl
        else
            Uri.parse(validUrl)
                    .buildUpon()
                    .appendQueryParameter(Constants.URL_PARAM_ALLOW_LOCAL_CARD, Constants.YES)
                    .toString()
    }
}

/**
 * Every user can have multiple personas.
 */
data class ProfilePersona(val types: List<Persona>?) : Serializable

/**
 * User's profile persona
 */
data class Persona(val type: ProfilePersonaType = ProfilePersonaType.NORMAL, //Persona type
                   val context: String? = null) : Serializable

/**
 * POST body of the update Profile API
 */
data class UpdateProfileBody(val name: String, //Name of the user
                             val handle: String, //Edited handle
                             val gender: String?, //Gender
                             val dob: String?, //DOB of the user
                             val privacy: String, //Privacy setting of the user
                             var taggingPermission: AccountPermission, //Can the user be tagged?
                             val invitesPermission: AccountPermission, //Can the user receive invites to groups?
                             val bio: String?, //bio/description
                             var profileImage: String?, //Profile image
                             val location: String?) //Location json string
/**
 * POST body for the bookmarking API
 */

data class BookmarkList(val items: List<BookmarkBody>)

data class BookmarkBody(val itemId: String = Constants.EMPTY_STRING, //ID of the item
                        val format: String? = null,  //Format of the item to bookmark
                        val subFormat: String? = null, //Sub format of the item to bookmark
                        val action: BookMarkAction, //Action: ADD/DELETE
                        val groupType: String? = null,
                        val timestamp: Long? = System.currentTimeMillis())


/**
 * POST body for profile base API
 */
data class ProfileBaseAPIBody(val userId: String?, //user id
                              val appLang: String, //App language
                              val clickedProfileName: String?, //Name of of the profile opened
                              val clickedProfileImg: String?, //image url of the profile opened
                              val handle: String? = null) { //handle of the profile opened

    /**
     * Function to get POJO that is valid for Server.
     *
     * validation rule: userId and userHandle needs to be mutually exclusive
     */
    fun createServerPostBody(): ProfileBaseAPIBody {
        return this.copy(userId = this.userId,
                handle = if (!CommonUtils.isEmpty(this.userId)) null else this.handle)
    }
}

/**
 * Maintains mutually exclusive userid/handle duo
 */
data class ProfileUserIdInfo(val userId: String?, val handle: String?)

/**
 * User migration status API response
 */
data class UserMigrationStatusResponse(val userMigrationCompleted: Boolean? = null, //User migration completed?
                                       var nextPingDelaySecs: Long? = DEFAULT_MIGRATION_PING_DELAY.toLong()) //When to  ping this API next?

/**
 * Enumeration of the different personas supported
 */
enum class ProfilePersonaType : Serializable {
    CREATOR,
    NORMAL
}

/**
 * Enumeration of different tabs supported
 */
enum class ProfileTabType(val pageType: PageType,
                          val deeplinkValue: String, val fpv: Boolean = false,
                          val referrer: NhAnalyticsReferrer) : Serializable {
    SAVED(PageType.PROFILE_SAVED, "saved", true, ProfileReferrer.SAVED), //SAVED is always FPV
    HISTORY(PageType.PROFILE_HISTORY, "history", true, ProfileReferrer.HISTORY), //HISTORY is always FPV
    FPV_ACTIVITY(PageType.PROFILE_ACTIVITY, "activity", true, ProfileReferrer.ACTIVITY), //FPV Activity
    GENERIC_WEB(PageType.GENERIC_ACTIVITY,"activity_web",true,ProfileReferrer.ACTIVITY),// GENERIC ACTIVITY WEB
    GENERIC_HASHTAG(PageType.GENERIC_ACTIVITY,"activity_hashtag",true,ProfileReferrer.ACTIVITY),// GENERIC ACTIVITY WEB
    TPV_ACTIVITY(PageType.PROFILE_TPV_RESPONSES, "activity", referrer = ProfileReferrer.TPV_RESPONSES), //TPV Activity
    FPV_POSTS(PageType.PROFILE_MY_POSTS, "posts", true, referrer = ProfileReferrer.MY_POSTS), //FPV: my posts
    TPV_POSTS(PageType.PROFILE_TPV_POSTS, "posts", referrer = ProfileReferrer.TPV_POSTS); //TPV: his posts

    companion object {
        fun from(tabType: String?): ProfileTabType? {
            tabType ?: return null
            values().forEach { if (CommonUtils.equals(tabType, it.name)) return it }
            return null
        }

        fun fromDeeplinkValue(tabType: String?, fpv: Boolean): ProfileTabType? {
            tabType ?: return null
            values().forEach {
                if (CommonUtils.equals(tabType, it.deeplinkValue) && fpv == it.fpv)
                    return it
            }
            return null
        }
    }
}

/**
 * Enumeration of social privacy setting
 */
enum class SocialPrivacy {
    PUBLIC,
    PRIVATE;

    companion object {
        @JvmStatic
        fun fromName(privacy: String?): SocialPrivacy {
            for( socialPrivacy in values()) {
                if(socialPrivacy.name == privacy) {
                    return socialPrivacy
                }
            }
            return PUBLIC
        }
    }
}

/**
 * Enumeration of book mark action: ADD/DELETE
 */
enum class BookMarkAction {
    ADD,
    DELETE;

    companion object {
        @JvmStatic
        fun from(action: String?): BookMarkAction {
            action ?: return ADD

            values().forEach {
                if (it.name == action) {
                    return it
                }
            }
            return ADD
        }
    }
}

enum class AccountPermission {
    ALLOWED,
    NOT_ALLOWED;

    companion object {
        @JvmStatic
        fun fromName(permissionString: String?): AccountPermission {
            for( permission in values()) {
                if(permission.name == permissionString) {
                    return permission
                }
            }
            return ALLOWED
        }
    }
}

/**
 * POST body for delete activities API
 */
data class DeleteUserInteractionsPostBody(val deleteAllActivity: Boolean,
                                          val activities: List<String>? = null) : Serializable
/**
 * User's contact information
 */
data class ProfileContact(val id: String?, //ID: Email/Phone number etc.
                          val isVerified: Boolean = false,
                          val isEditable: Boolean = true): Serializable

/**
 * Wrapper for handle and response in a single object
 */
data class HandleAvailabilityResponse(val handle: String,
                                      val response: ApiResponse<Any?>)

/**
 * Wrapper for handle and uiResponseWrapper in a single object
 */
data class HandleAvailabilityUIResponseWrapper(val handle: String,
                                               val uiResponseWrapper: UIResponseWrapper<Int>)
