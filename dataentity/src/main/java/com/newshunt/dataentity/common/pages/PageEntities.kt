package com.newshunt.dataentity.common.pages

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.TabEntity
import com.newshunt.dataentity.news.model.entity.server.asset.ShareParam
import com.newshunt.dataentity.social.entity.TABLE_ADD_PAGE
import com.newshunt.dataentity.social.entity.VIEW_EntityInfoView
import java.io.Serializable


enum class PageSection(val section: String) {
  NEWS("news") , TV("tv"), FOLLOW("follow"), PROFILE("profile"), GROUP("group"), SEARCH("search"), LOCAL("local")


}
@Entity(tableName = "pages" , primaryKeys = ["id", "section"])
data class S_PageEntity(
    @Embedded val pageEntity: PageEntity,
    val section: String
) {
  companion object {
    const val COL_ID = "id"
  }
}

data class PageEntity(val id: String,
                      val name: String?,
                      val displayName: String? = null,
                      val entityType: String,
                      val subType: String? = null,
                      val entityLayout: String?,
                      val contentUrl: String?,
                      val entityInfoUrl: String? = null,
                      val handle: String? = null,
                      val deeplinkUrl: String? = null,
                      val moreContentLoadUrl: String? = null,
                      val entityImageUrl: String? = null,
                      @Embedded(prefix = "share_") val shareParams: ShareParam? = null,
                      val shareUrl: String? = null,
                      val nameEnglish: String? = null,
                      val moreText:String? = null,
                      val description:String? = null,
                      val subTitle:String? = null,
                      val descriptionUrl:String? = null,
                      var defaultTabId:String? = null,
                      val appIndexDescription: String? = null,
                      val isRemovable: Boolean = false,
                      val allowReorder: Boolean = false,
                      val isServerDetermined: Boolean = false,
                      var viewOrder: Int = 0,
                      val contentRequestMethod: String? = null,
                      val enableWebHistory: Boolean = false,
                      val badgeType: String? = null,
                      @Embedded(prefix = "header_") val header: Header? = null,
                      @Embedded(prefix = "counts_") val counts: Counts2? = null,
                      val isFollowable: Boolean = false,
                      val legacyKey:String?=null,
                      val createPostText: String? = null,
                      val createPostType: String? = null,
                      val showParentInTab: Boolean = false,
                      val carouselUrl : String? = null,
                      @Embedded(prefix = "promotion_") val campaignMeta: CampaignMeta? = null) : Serializable, TabEntity {


    fun toActionableEntity() : ActionableEntity {
      return ActionableEntity(entityId = id, entityType = entityType, entitySubType = subType,
          entityImageUrl = entityImageUrl, iconUrl = header?.logoUrl, displayName = displayName,
          deeplinkUrl = deeplinkUrl, handle = handle, nameEnglish = nameEnglish)
    }

    fun equalsForHome(oldPageEntity: PageEntity) : Boolean {
      return when {
            oldPageEntity.id != id -> false
            oldPageEntity.contentUrl != contentUrl -> false
            oldPageEntity.name != name -> false
            oldPageEntity.displayName != displayName -> false
            else -> oldPageEntity.contentRequestMethod == contentRequestMethod
        }
    }

    fun toFollowActionableEntity() : FollowSyncEntity {
      return FollowSyncEntity(actionableEntity = toActionableEntity(), action = FollowActionType.FOLLOW)
    }

    override fun getName1(): String? = name

    override fun getTabType(): String = entityType

    override fun getTabId(): String = id

    override fun getTabLayout(): String? = entityLayout
}


data class PageResponse(val version: String,
                        val rows : List<PageEntity>) : Serializable

data class Header(
    val bannerImageUrl:String? = null,
    val headerType: String,
    val logoUrl: String? = null,
    val hideLogo: Boolean = true,
    val hideMastHead: Boolean = true) : Serializable

data class CampaignMeta(
    val iconUrl: String? = null,
    val colourGradient: List<GradientItem>? = null,
    val indicatorColour: List<GradientItem>? = null,
    val publishTime: Long? = null,
    val expiryTime: Long? = null) : Serializable

data class GradientItem(
    val color: String? = null,
    val position: Float = 0f) : Serializable


@Entity(tableName = "pagesync", primaryKeys = ["id", "section"])
data class PageSyncEntity(val id: String,
                          val entityType: String,
                          val viewOrder: Int,
                          val mode: String,
                          val section: String,
                          val isServerDetermined: Boolean = false) : Serializable


data class PageSyncList(val added: List<PageSyncEntity>?= null,
                        val modified: List<PageSyncEntity>? = null,
                        val deleted: List<PageSyncEntity>?= null) : Serializable

data class PageSyncBody(val pages: PageSyncList) : Serializable

@Entity(tableName = TABLE_ADD_PAGE)
data class AddPageEntity(@PrimaryKey val id: String,
                         val mode: String,
                         val displayName: String,
                         val time: Long = System.currentTimeMillis(),
                         val entityType: String) {

  fun toPageEntity() : PageEntity = PageEntity(id = id, displayName = displayName, entityType = entityType, contentUrl = null, entityLayout = null, name = displayName)
}

@Entity(tableName = "pageabletopics" ,primaryKeys = ["id","section"])
data class TopicsEntity(@Embedded val pageEntity: PageEntity,
                        val section: String) : Serializable

data class PageableTopicsEntity(@Embedded val pageEntity: PageEntity,
                                var isFavorite: Boolean = false,
                                var isFollowed: Boolean = false) : Serializable


data class EntityInfoResponse(val parent: PageEntity,
                              val kids: List<PageEntity>?,
                              val version: String) : Serializable

@Entity(tableName = "entityInfo", primaryKeys = ["id", "parentId"])
data class EntityPojo(@Embedded val pageEntity: PageEntity,
                      var parentId: String,
                      val section: String) : Serializable

@DatabaseView(value = """
  SELECT e.* , CASE WHEN f.`action`='FOLLOW' THEN 1 ELSE 0 END isFollowed
  FROM entityInfo e LEFT JOIN follow f ON f.entityId = e.id
""", viewName = VIEW_EntityInfoView)
data class EntityInfoView(@Embedded val pageEntity: PageEntity,
                          var parentId: String,
                          val isFollowed: Boolean = false,
                          val section: String) : Serializable, CommonAsset {

    override fun i_format(): Format? = null
    override fun i_subFormat(): SubFormat? = null
    override fun i_uiType(): UiType2? = null
    override fun i_type(): String = pageEntity.entityType
    override fun i_id(): String = pageEntity.id
    override fun i_isFollowin(): Boolean = isFollowed
    override fun i_isFollowable(): Boolean = pageEntity.isFollowable

    fun isHeaderBanner(): Boolean = CommonUtils.equals(pageEntity.header?.headerType, "BANNER")
    fun isHeaderProfile(): Boolean = CommonUtils.equals(pageEntity.header?.headerType, "PROFILE")
    fun isHeaderProfileBanner(): Boolean = CommonUtils.equals(pageEntity.header?.headerType, "PROFILE_BANNER")
    fun isVerifiedUser(): Boolean = pageEntity.badgeType != null
    fun nameEnglish(): String? = pageEntity.nameEnglish
}

data class EntityInfoList(@Embedded val parent : EntityInfoView) : Serializable{
  @Relation(parentColumn = "id", entityColumn = "parentId", entity = EntityInfoView::class) var kids: List<EntityInfoView>?=null

}

enum class EntityType {
  HASHTAG, LOCATION, SOURCE, HEADLINE
}