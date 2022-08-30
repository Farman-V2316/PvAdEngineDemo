package com.newshunt.dataentity.common.pages

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.follow.entity.FollowBlockConfigWrapper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.MemberRole
import java.io.Serializable

const val TABLE_SOURCE_FOLLOW_BLOCK = "source_follow_block"
enum class FollowActionType  {
  FOLLOW, UNFOLLOW, BLOCK, UNBLOCK, MANAGE, JOIN
}

data class ActionableEntity(val entityId: String,
                            val entityType: String,
                            val entitySubType: String? = null,
                            val displayName: String? = null,
                            val entityImageUrl: String? = null,
                            val iconUrl: String? = null,
                            val handle: String? = null,
                            val deeplinkUrl: String? = null,
                            val badgeType: String? = null,
                            val memberRole: String? = null,
                            @Embedded(prefix = "counts_") val counts: Counts2? = null,
                            val experiment: Map<String, String>? = null,
                            val nameEnglish: String? = null) : Serializable, AnyCard {
  fun toColdStartEntityItem() = ColdStartEntityItem(
          entityId = entityId,
          entityType = entityType,
          iconUrl = iconUrl,
          entityImageUrl = entityImageUrl,
          displayName = displayName,
          handle = handle,
          deeplinkUrl = deeplinkUrl,
          entitySubType =  entitySubType,
          nativeCardType = null,
          nameEnglish = nameEnglish
  )

  fun toLocationItem() = Location(PageEntity(id = entityId,
          entityType = entityType,
//          iconUrl = iconUrl,
          entityImageUrl = entityImageUrl,
          displayName = displayName,
          deeplinkUrl = deeplinkUrl,
          subType = entitySubType,
          nameEnglish = nameEnglish,
          name = nameEnglish,
          entityLayout = null,
          contentUrl = ""))
}


@Entity(tableName = "report")
data class ReportEntity(@PrimaryKey(autoGenerate = true) var rowId: Int = 0, val entityId: String) : Serializable


@Entity(tableName = "follow", primaryKeys = ["entityId"])
data class FollowSyncEntity(@Embedded val actionableEntity: ActionableEntity,
                            var action:FollowActionType,
                            val actionTime:Long=System.currentTimeMillis(),
                            var isSynced:Boolean= false) : Serializable

data class FollowResponseItem(val entityId: String,
                              val entityType: String,
                              val entitySubType: String? = null,
                              val displayName: String? = null,
                              val entityImageUrl: String? = null,
                              val iconUrl: String? = null,
                              val handle: String? = null,
                              val deeplinkUrl: String? = null,
                              val counts: Counts2? = null,
                              var action:FollowActionType,
                              val actionTime:Long=System.currentTimeMillis(),
                              var isSynced:Boolean= false,
                              val nameEnglish: String? = null) {
  fun toFollowSyncEntity() = FollowSyncEntity(
      actionableEntity = ActionableEntity(entityId,
              entityType,
              entitySubType,
              displayName,
              entityImageUrl,
              iconUrl,
              handle,
              deeplinkUrl,
              counts = counts,
              nameEnglish = nameEnglish),
      action = action,
      actionTime = actionTime,
      isSynced = isSynced
  )
}

data class FollowSyncResponse(val version: String,
                              val rows: List<FollowResponseItem>,
                              val nextPageUrl: String?)

data class FollowPayload(val follows : List<FollowEntityPayloadItem>)

data class FollowEntityPayloadItem(val entityId: String,
                                   val entityType: String,
                                   val action: String,
                                   val actionTime: Long,
                                   val entitySubType: String?,
                                   val attributes: FollowAttributes = FollowAttributes())

data class FollowAttributes(val reason: String = "USER")

data class FollowFilter(val displayText: String, val value: String)

@Entity(tableName = "userFollow" , primaryKeys = ["entityId", "fetchEntity"])
data class UserFollowEntity(val fetchEntity: String, @Embedded val actionableEntity: ActionableEntity)

data class UserFollowView(@Embedded val actionableEntity: ActionableEntity,
                          val isFollowing: Boolean = false,
                          val isFavorite: Boolean = false,
                          val isBlocked: Boolean = false) : CommonAsset {
  override fun i_id(): String = actionableEntity.entityId

  override fun i_type(): String = actionableEntity.entityType

  override fun i_format(): Format? = Format.ENTITY

  override fun i_subFormat(): SubFormat? = null

  override fun i_uiType(): UiType2? = null

  override fun i_isFollowin(): Boolean? = isFollowing

  override fun i_isFollowable(): Boolean = actionableEntity.entityType != ColdStartEntityType.COMMUNITY_GROUP.name

  override fun i_deeplinkUrl(): String? = actionableEntity.deeplinkUrl

  override fun i_isFavourite(): Boolean? = isFavorite

  fun isVerifiedUser(): Boolean = !CommonUtils.isEmpty(actionableEntity.badgeType)

  fun memberText() : String {
    return when(actionableEntity.memberRole) {
      MemberRole.ADMIN.name -> {
        MemberRole.ADMIN.name
      }
      MemberRole.OWNER.name -> {
        MemberRole.OWNER.name
      } else -> {
        Constants.EMPTY_STRING
      }
    }
  }
}

@Entity(tableName = TABLE_SOURCE_FOLLOW_BLOCK)
data class SourceFollowBlockEntity(@PrimaryKey val sourceId:String, // for sourceId="config_data", we will get the config data
                                   val pageViewCount:Int = 0,
                                   val shareCount:Int = 0,
                                   val showLessCount:Int = 0,
                                   val reportCount:Int = 0,
                                   val updateType: FollowActionType?=null,
                                   val showImplicitFollowDialogCount:Int = 0,
                                   val showImplicitBlockDialogCount:Int = 0,
                                   val configData: FollowBlockConfigWrapper? = null,
                                   val postSourceEntity:PostSourceAsset?=null,
                                   val sourceLang:String,
                                   val updateTimeStamp:Long) : Serializable
