package com.newshunt.dataentity.notification

import com.newshunt.common.helper.common.Constants
import java.io.Serializable

/**
 * @author anshul.jain
 */
class ExploreNavModel(val id: String, val type: String, val name: String, val requestMethod:
String?) : BaseModel() {
    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.EXPLORE_MODEL
    }
}

class FollowNavModel
@JvmOverloads
constructor(val tabType: String? = null, val subTabType: String? = null,
            val promotionId: String? = null, val userId: String? = null, val model:
            FollowModel? = null) :
        BaseModel
        () {

    val isTPV
        get() = userId != null

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.FOLLOW_MODEL
    }
}

class GroupNavModel
@JvmOverloads
constructor() : BaseModel() {

    var groupId: String = Constants.EMPTY_STRING
    var handle: String = Constants.EMPTY_STRING
    var subType: String = Constants.EMPTY_STRING

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.GROUP_MODEL
    }
}


enum class FollowModel {
    FOLLOWING,
    FOLLOWERS,
    BLOCKED;
}

enum class FollowViewState : Serializable {
    NONE,
    FPV_FOLLOWING,
    FPV_FOLLOWERS,
    TPV_FOLLOWING,
    TPV_FOLLOWERS,
    FPV_BLOCKED;

    fun isFPV() = (this == FPV_FOLLOWERS || this == FPV_FOLLOWING || this == FPV_BLOCKED)

    fun isTPV() = (this == TPV_FOLLOWERS || this == TPV_FOLLOWING)

    fun isFollowing() = (this == FPV_FOLLOWING || this == TPV_FOLLOWING)

    fun isFollowers() = (this == FPV_FOLLOWERS || this == TPV_FOLLOWERS)

    fun isBlocked() = (this == FPV_BLOCKED)

}