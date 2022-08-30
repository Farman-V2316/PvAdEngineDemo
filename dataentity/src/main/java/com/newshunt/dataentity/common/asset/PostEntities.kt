/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.chronologicallyCompareTo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.Track
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.news.model.entity.server.asset.AnimationType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLandingType
import com.newshunt.dataentity.news.model.entity.server.asset.PostState
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dataentity.social.entity.VIEW_AllLevelCards
import com.newshunt.dataentity.social.entity.VIEW_AssocationsChildren
import com.newshunt.dataentity.social.entity.VIEW_DiscussionsChildren
import java.io.Serializable
import java.util.Observable


data class PostEntity(@ColumnInfo(name = COL_ID) var id: String = Constants.EMPTY_STRING,
                      var type: String = Constants.EMPTY_STRING,
                      var format: Format = Format.HTML,
                      var subFormat: SubFormat = SubFormat.STORY,
                      var uiType: UiType2 = UiType2.NORMAL,
                      @Embedded(prefix = "src_")
                      var source: PostSourceAsset? = null,
                      var publishTime: Long? = null,
                      var recommendationTs: Long? = null,
                      var showPublishDate: Boolean = false,
                      var langCode: String? = null,
                      var title: String? = null,
                      var titleEnglish: String? = null,
                      var detailViewFeatureMask:Int = 0,
                      var content: String? = null,
                      var content2: String? = null,
                      var contentType: String? = null,
                      var contentImage: String? = null,
                      var disclaimer: String? = null,
                      var detailUiType: UiType2? = null,
                      var detailAttachLocation: DetailAttachLocation? = DetailAttachLocation.NORMAL,
                      var landingType: CardLandingType? = null,
                      @Embedded(prefix = "card_lbl_")
                      var cardLabel: CardLabel2? = null,
                      var cardLocation: String? = null,
                      var shareUrl: String? = null,
                      var supplementUrl: String? = null,
                      var viewOrder: Int? = null,
                      var moreCoverageCount: Int? = null,
                      var experiment: Map<String, String>? = null,
                      var idParams: Map<String, String>? = null,
                      var onClickUseDeeplink: Boolean = false,
                      var allowComments: Boolean = true,
                      var excludeFromSwipe: Boolean = false,
                      var isSelectAndCopyPasteDisabled: Boolean? = false,
                      var selectionReason: String? = null,
                      var referralString: String? = null,
                      var tsString: String? = null,
                      var showTsAlways: Boolean = false,
                      var showTsIcon: Boolean = false,
                      var widgetLoadUrl: String? = null,
                      var contentBaseUrl: String? = null,
                      var moreContentLoadUrl: String? = null,
                      var autoRefreshInterval: Long? = null,
                      var moreCoverageNextPageUrl: String? = null,
                      var wordCount: Int? = null,
                      var imageCount: Int? = null,
                      var location: String? = null,
                      var postPrivacy: PostPrivacy? = null,
                      var deeplinkUrl: String? = null,
                      var publisherStoryUrl: String? = null,
                      var isClickOutEnabled: Boolean? = false,
                      var beaconUrl: String? = null,
                      var npCoverageText: String? = null,
                      @Embedded(prefix = "share_")
                      var shareParams: ShareParam2? = null,
                      @Embedded(prefix = "track_")
                      var articleTrack: Track? = null,
                      @Embedded(prefix = "poll_")
                      var poll: PollAsset? = null,
                      @Embedded(prefix = "viral_")
                      var viral: ViralAsset? = null,
                      var thumbnailInfos: List<ImageDetail>? = null,
                      var hashtags: List<HastTagAsset>? = null,
                      var moreCoverageIcons: List<String>? = null,
                      @Embedded(prefix = "video_")
                      var videoAsset: VideoAsset? = null,
                      var immersiveUrl: String? = null,
                      var defaultPlayUrl: String? = null,
                      @Embedded(prefix = "count_")
                      var counts: Counts2? = null,
                      @Embedded(prefix = "og_")
                      var linkAsset: LinkAsset? = null,
                      @Embedded(prefix = "repost_")
                      var repostAsset: RepostAsset? = null,
                      var groupId: String? = null,
                      var showVideoIcon: Boolean = false,
                      var isApprovalPending: Boolean? = false,
                      @Embedded(prefix = "webcard_")
                      var webCard2: WebCard2? = null,
                      @Embedded(prefix = "cold_")
                      var coldStartAsset: ColdStartEntity? = null,
                      var childFetchUrl: String? = null,
                      var childCount: Int? = null,
        /*Multimedia collection card*/
                      @Embedded(prefix = "mm_")
                      var collectionAsset: CollectionEntity? = null,
                      var tickers: List<Ticker2>? = null,
                      @Ignore
                      var moreStories: List<PostEntity>? = null,
                      @Ignore
                      var associations: List<PostEntity>? = null,
                      @ColumnInfo(name = COL_LEVEL)
                      var level: PostEntityLevel = PostEntityLevel.TOP_LEVEL,
                      var nonLinearPostUrl: String? = null,
                      @Embedded(prefix = "interaction_")
                      var userInteraction: UserInteraction? = null,
                      var tickerRefreshTime: Int? = 5_000,
                      var tickerUrl: String? = null,
                      var localLastTickerRefreshTime: Long = 0,
                      var adSpec: AdSpec? = null,
                      var maxImageViewportHeightPercentage: Int = 0,
                      var contentImageInfo: ImageDetail? = null,
                      @Embedded(prefix = "local_")
                      var localInfo: LocalInfo? = null,
                      var subText: String? = null,
                      @Ignore
                      var isFollowin: Boolean? = false,
                      @Ignore
                      var isReported: Boolean? = false,
                      @Ignore
                      var selectedLikeType: String? = null,
                      @Ignore
                      var isRead: Boolean? = false,
                      @Ignore
                      var additionalContents: List<AdditionalContents>? = null,
                      var isFocusableInTouchMode: Boolean? = false,
                      @Ignore
                      var commentRepostItems: List<PostEntity>? = null,
                      var repostNudgeText: String?=null,
                      var totalThumbnails: Int = 0,
                      var state: PostState? = PostState.PUBLISHED,
                      var isDeleted: Boolean = false,
                      var detailPageWidgetOrderId: String? = null,
                      var message: String? = null,
                      var groupJoined: Boolean = false,
                      var isFromCache : Boolean = false,
                      var ignoreSourceBlock: Boolean = false,
                      var removePadding: Boolean? = false,
                      var adjunctLang: String?= null,
                      var referrerItemId: String?= null,
                      var referrerEntitySourceType: String?= null,
                      var titleFirstAksharCharCount:Int?=0,
                      var c1FirstAksharCharCount:Int?=0,
                      var byLine:String?= null,
                      var distancingSpec: DistancingSpec? = null,
                      var impressionData: String? = null

) : Serializable, CommonAsset, AnyCard {

    override fun i_id(): String = id
    override fun i_type(): String = type
    override fun i_format(): Format = format
    override fun i_subFormat(): SubFormat = subFormat
    override fun i_uiType(): UiType2 = uiType
    override fun i_detailUiType(): UiType2? = detailUiType
    override fun i_subText(): String? = subText

    override fun i_detailAttachLocation(): DetailAttachLocation? = detailAttachLocation

    override fun i_source(): PostSourceAsset? = source
    override fun i_langCode(): String? = langCode
    override fun i_publishTime(): Long? = publishTime
    override fun i_recommendationTs(): Long? = recommendationTs
    override fun i_isFollowin(): Boolean? = isFollowin
    override fun i_isReported(): Boolean? = isReported
    override fun i_title(): String? = title
    override fun i_englishTitle(): String? = titleEnglish
    override fun i_contentType(): String? = contentType
    override fun i_content(): String? = content
    override fun i_content2(): String? = content2
    override fun i_disclaimer(): String? = disclaimer
    override fun i_contentImage(): String? = contentImage
    override fun i_shareUrl(): String? = shareUrl
    override fun i_thumbnailUrls(): List<String>? = thumbnailInfos?.map { it.url }
    override fun i_thumbnailUrlDetails(): List<ImageDetail>? = thumbnailInfos
    override fun i_maxImageViewportHeightPercentage(): Int = maxImageViewportHeightPercentage
    override fun i_videoAsset(): VideoAsset? = videoAsset
    override fun i_immersiveUrl(): String? = immersiveUrl
    override fun i_counts(): Counts2? = counts
    override fun i_cardLabel(): CardLabel2? = cardLabel
    override fun i_cardLocation(): String? = cardLocation
    override fun i_viral(): ViralAsset? = viral
    override fun i_tsString(): String? = tsString
    override fun i_referralString(): String? = referralString
    override fun i_repostNudgeText(): String? = repostNudgeText
    override fun i_showTsAlways(): Boolean = showTsAlways
    override fun i_idParams(): Map<String, String>? = idParams
    override fun i_showTsIcon(): Boolean = showTsIcon
    override fun i_postLocation(): String? = location
    override fun i_showPublishDate(): Boolean = showPublishDate
    override fun i_poll(): PollAsset? = poll
    override fun i_postPrivacy(): PostPrivacy? = postPrivacy
    override fun i_deeplinkUrl(): String? = deeplinkUrl
    override fun i_pollSelectedOptionId(): String? = null
    override fun i_reason(): String? = null
    override fun i_selectedLikeType(): String? = selectedLikeType
    override fun i_linkAsset(): LinkAsset? = linkAsset
    override fun i_moreStories(): List<PostEntity>? = moreStories
    override fun i_npCoverageText(): String? = npCoverageText
    override fun i_groupId(): String? = groupId
    override fun i_isApprovalPending() = isApprovalPending ?: false
    override fun i_showVideoIcon(): Boolean = showVideoIcon
    override fun i_moreCoverageNextPageUrl(): String? = moreCoverageNextPageUrl
    override fun i_hashtags(): List<HastTagAsset>? = hashtags
    override fun i_childFetchUrl(): String? = childFetchUrl
    override fun i_childCount(): Int? = childCount
    override fun i_moreContentLoadUrl(): String? = moreContentLoadUrl
    override fun i_isSelectAndCopyPasteDisabled(): Boolean? = isSelectAndCopyPasteDisabled
    override fun i_tickerRefreshTime(): Int? = tickerRefreshTime
    override fun i_isRead(): Boolean? = isRead
    override fun i_tickerUrl(): String? = tickerUrl
    override fun i_DetailPageWidgetOrderId(): String? = detailPageWidgetOrderId
    override fun i_distancingSpec(): DistancingSpec? = distancingSpec
    override fun i_experiments(): Map<String, String>? {
        return experiment
    }

    override fun i_postState(): PostState? {
        return state
    }

    override fun i_isDeleted(): Boolean {
        return isDeleted
    }

    override fun i_isFocusableInTouchMode(): Boolean? {
        return isFocusableInTouchMode
    }

    override fun i_commentRepostItems(): List<PostEntity>? = commentRepostItems

    /*COLD START PROPERTIES START*/

    override fun i_moreCoverageIcons(): List<String>? = moreCoverageIcons
    override fun i_moreCoverageCount(): Int? = moreCoverageCount
    override fun i_selectText(): String? = coldStartAsset?.selectText
    override fun i_unSelectText(): String? = coldStartAsset?.unSelectText
    override fun i_hideSelection(): Boolean? = coldStartAsset?.hideSelection
    override fun i_viewAllDeeplink(): String? = coldStartAsset?.viewAllDeeplink
    override fun i_shortTitle(): String? = coldStartAsset?.shortTitle
    override fun i_viewallText(): String? = coldStartAsset?.viewAllText
    override fun i_widgetType(): String? = coldStartAsset?.widgetType
    override fun i_itemToFillFilter(): ItemToFilter? = coldStartAsset?.itemToFillFilter
    override fun i_removeOnScroll(): Boolean? = coldStartAsset?.removeOnScroll
    override fun i_showPlusIcon(): Boolean? = coldStartAsset?.showPlusIcon
    override fun i_searchContext(): String? = coldStartAsset?.searchContext
    override fun i_carouselProperties(): CarouselProperties2? = collectionAsset?.carouselProperties
    override fun i_collectionItems(): List<CommonAsset>? = collectionAsset?.collectionItem
    override fun i_hideTitle(): Boolean? = collectionAsset?.hideTitle
    override fun i_repostAsset(): RepostAsset? = repostAsset
    override fun i_userInteractionAsset(): UserInteraction? = userInteraction
    override fun i_nonLinearPostUrl(): String? = nonLinearPostUrl
    override fun i_webcard(): WebCard2? = webCard2
    override fun i_onClickUseDeeplink(): Boolean? = onClickUseDeeplink
    override fun i_landingType(): CardLandingType? = landingType

    override fun i_wordCount(): Int? = wordCount
    override fun i_imageCount(): Int? = imageCount

    override fun i_level(): PostEntityLevel = level
    override fun i_contentImageInfo(): ImageDetail? {
        return contentImageInfo
    }

    override fun i_ticker(): List<Ticker2>? = tickers

    override fun i_collection_viewAllText(): String? = collectionAsset?.carouselProperties?.i_viewMoreText()

    // Ads meta
    override fun i_adSpec(): AdSpec? = adSpec

    override fun i_allowComments(): Boolean = allowComments

    override fun i_coldStartAsset(): ColdStartEntity? = coldStartAsset
    override fun i_articleTrack(): Track? = articleTrack
    override fun i_localInfo() = localInfo

    override fun i_progress(): Int? = i_localInfo()?.progress
    override fun i_status(): String? = i_localInfo()?.status
    override fun i_ignoreSourceBlock(): Boolean = ignoreSourceBlock

    override fun i_removePadding(): Boolean? = removePadding
    override fun i_adjunctLang(): String? = adjunctLang

    override fun toAnyCard() = this

    fun withNewCounts(counts2: Counts2?): PostEntity {
        val counts3 = counts2?.copy(
                COMMENTS = Card.maxOf(i_counts()?.COMMENTS, counts2.COMMENTS),
                REPOST = Card.maxOf(i_counts()?.REPOST, counts2.REPOST),
                SHARE = Card.maxOf(i_counts()?.SHARE, counts2.SHARE),
                VIEWS = Card.maxOf(i_counts()?.VIEWS, counts2.VIEWS),
                TOTAL_LIKE = Card.maxOf(i_counts()?.TOTAL_LIKE, counts2.TOTAL_LIKE))
        return this.copy(counts = counts3)
    }

    fun toCard2(fetchId: String = Constants.EMPTY_STRING, useUniqueId: String? = null,
                newLevel: PostEntityLevel? = null, idParam: String? = null, adId: String? = null): Card {
        return Card(
                useUniqueId ?: getUniqueId(fetchId, newLevel),
                idParam?: id,
                source?.id,
                source?.entityType,
                source,
                newLevel ?: level,
                moreStories?.size ?: 0,
                videoAsset?.assetId,
                format,
                collectionAsset?.includeCollectionInSwipe,
                null,
                contentImageInfo,
                shareUrl,
                title,
                langCode,
                localInfo,
                subFormat,
                adId,
                this,
                ignoreSourceBlock = this.ignoreSourceBlock,
        )
    }

    fun toCard2(fetchId: Long) = toCard2(fetchId.toString())
    fun toCard(fetchId: Long) = TopLevelCard (
            AllLevelCards(
                    toCard2(fetchId), isFollowin = null, isRead = null, isReported = null
            )
    )

    fun getUniqueId(fetchId: String, newLevel: PostEntityLevel? = null, prefix: String? = null): String {
        val uId = when {
            format == Format.AD -> id
            (newLevel ?: level) == PostEntityLevel.LOCAL -> joinFetchIdAndPostId("local", id)
            else -> joinFetchIdAndPostId(fetchId, id)
        }
        return prefix?.plus(uId) ?: uId
    }

    fun getUniqueId(fetchId: Long) = getUniqueId(fetchId.toString())

    override fun rootPostEntity() : PostEntity? = this


    override fun i_entityCollection(): List<EntityItem>? {
        return coldStartAsset?.coldStartCollectionItems
    }

    override fun copyWith(isFollowin: Boolean?, selectedLikeType: String?,
                          pollSelectedOptionId: String?, isRead: Boolean?, coldStartItems:
                          List<ColdStartEntityItem>?, collectionItems: List<PostEntity>?): PostEntity{
        return this.copy(
                isFollowin = isFollowin,
                selectedLikeType = selectedLikeType,
                isRead = isRead,
                coldStartAsset = coldStartAsset?.copy(coldStartCollectionItems = coldStartItems),
                collectionAsset = collectionAsset?.copy(collectionItem = collectionItems)
        )
    }

    override fun i_totalThumbnails(): Int? {
        return totalThumbnails
    }

    override fun i_isFromCache(): Boolean {
        return isFromCache
    }

    override fun i_referrerItemId(): String?  = referrerItemId

    override fun i_referrerEntitySourceType(): String? = referrerEntitySourceType

    override fun i_richTextChunk1(): Boolean {
        return detailViewFeatureMask != null && detailViewFeatureMask and RICH_CHUNK1_FEATURE_MASK > 0
    }

    override fun i_richTextChunk2(): Boolean {
        return detailViewFeatureMask != null && detailViewFeatureMask and RICH_CHUNK2_FEATURE_MASK > 0
    }

    override fun i_enableBigtextChunk1(): Boolean {
      return detailViewFeatureMask != null && detailViewFeatureMask and BIGTEXT_CHUNK1_FEATURE_MASK > 0
    }

    override fun i_enableBigtextTitle(): Boolean {
        return detailViewFeatureMask != null && detailViewFeatureMask and BIGTEXT_TITLE_FEATURE_MASK > 0
    }

    override fun i_byline(): String? {
        return byLine
    }

    override fun i_c1FirstAksharCharCount(): Int? {
        return c1FirstAksharCharCount
    }

    override fun i_titleFirstAksharCharCount(): Int? {
        return titleFirstAksharCharCount
    }
    fun  updateCacheFlagAndGet(isCached: Boolean): PostEntity {
        isFromCache = isCached
        collectionAsset?.collectionItem?.forEach { child ->
            child.isFromCache = isCached
        }
        return this
    }
    companion object {
        const val COL_ID = "id"
        const val COL_LEVEL = "level"
        const val RICH_CHUNK1_FEATURE_MASK = 1
        const val RICH_CHUNK2_FEATURE_MASK = 2
        const val BIGTEXT_CHUNK1_FEATURE_MASK = 4
        const val BIGTEXT_TITLE_FEATURE_MASK = 8
        private const val serialVersionUID: Long = 1L

        fun joinFetchIdAndPostId(fetchId: String, postId: String): String {
            return "${fetchId}_${postId}"
        }

    }
}

data class DistancingSpec(val ads: Int, val collection: Int, val nestedCollection: Int, val top: Int):Serializable

/**
 * Do not rename values. They are persisted.
 */
enum class PostEntityLevel {
    TOP_LEVEL,
    RELATED_STORIES,
    DISCUSSION,
    ASSOCIATION,
    HISTORY,
    LOCAL,
    LOCAL_COMMENT,
    AD_PROXY
}

data class WebCard2(val aspectRatio: Float = 0f) : Serializable

@Entity(tableName = "discussions",
        primaryKeys = [Discussions.COL_PARENT_ID, Discussions.COL_CHILD_ID],
        foreignKeys = [
            ForeignKey(entity = Card::class,
                    parentColumns = ["uniqueId", PostEntity.COL_LEVEL],
                    childColumns = [Discussions.COL_PARENT_ID, Discussions.COL_LEVEL], onDelete = CASCADE)])
data class Discussions @JvmOverloads constructor(
        @ColumnInfo(name = COL_PARENT_ID) val parentId: String,
        @ColumnInfo(name = COL_CHILD_ID) val childId: String,
        @ColumnInfo(name = COL_LEVEL) val level: PostEntityLevel = PostEntityLevel.TOP_LEVEL,
        val index: Int
) {
    companion object {
        const val COL_PARENT_ID = "parentId"
        const val COL_CHILD_ID = "child_id"
        const val COL_LEVEL = "discussionslevel"
    }
}

@Entity(tableName = "associations",
        primaryKeys = [Associations.COL_PARENT_ID, Associations.COL_CHILD_ID],
        foreignKeys = [
            ForeignKey(entity = Card::class,
                    parentColumns = ["uniqueId", PostEntity.COL_LEVEL],
                    childColumns = [Associations.COL_PARENT_ID, Associations.COL_LEVEL], onDelete = CASCADE)])
data class Associations @JvmOverloads constructor(
        @ColumnInfo(name = COL_PARENT_ID) val parentId: String,
        @ColumnInfo(name = COL_CHILD_ID) val childId: String,
        @ColumnInfo(name = COL_LEVEL) val level: PostEntityLevel = PostEntityLevel.TOP_LEVEL,
        val index: Int
) {
    companion object {
        const val COL_PARENT_ID = "parentId"
        const val COL_CHILD_ID = "child_id"
        const val COL_LEVEL = "discussionslevel"
    }
}

@DatabaseView(value = """
    SELECT * from (SELECT * from discussions where 
      child_id NOT IN (SELECT postId from  localdelete)) m 
    LEFT JOIN (SELECT * from $VIEW_AllLevelCards WHERE level='DISCUSSION') p ON m.child_id = p.id  ORDER BY m.`index` ASC
""", viewName = VIEW_DiscussionsChildren)
data class DiscussionsChildren(
        @Embedded
        val discussions: Discussions?,
        @Embedded
        val postEntity: AllLevelCards?
)

@DatabaseView(value = """
    SELECT * from (SELECT * from associations) m 
    LEFT JOIN $VIEW_AllLevelCards p ON m.child_id = p.id  ORDER BY m.`index` ASC
""", viewName = VIEW_AssocationsChildren)
data class AssociationsChildren(
        @Embedded
        val associations: Associations?,
        @Embedded
        val postEntity: AllLevelCards?
)

data class RepostAsset(val id: String = Constants.EMPTY_STRING,
                       val type: String = Constants.EMPTY_STRING,
                       val format: Format = Format.HTML,
                       val thumbnailInfos: List<ImageDetail>? = null,
                       val subFormat: SubFormat = SubFormat.STORY,
                       val uiType: UiType2 = UiType2.NORMAL,
                       val content: String? = null,
                       @Embedded(prefix = "src_")
                       val source: PostSourceAsset? = null,
                       val title: String? = null,
                       val maxImageViewportHeightPercentage:Int=0,
                       val titleEnglish: String? = null,
                       val imageCount: Int? = null,
                       @Embedded(prefix = "og_")
                       val linkAsset: LinkAsset?,
                       @Embedded(prefix = "viral_")
                       val viral: ViralAsset? = null,
                       @Embedded(prefix = "video_")
                       var videoAsset: VideoAsset? = null,
                       var showVideoIcon: Boolean? = null) : CommonAsset, Serializable {
    override fun i_id(): String = id
    override fun i_type(): String = type
    override fun i_format(): Format? = format
    override fun i_subFormat(): SubFormat? = subFormat
    override fun i_uiType(): UiType2? = uiType
    override fun i_thumbnailUrls(): List<String>? = thumbnailInfos?.map { it.url }
    override fun i_thumbnailUrlDetails(): List<ImageDetail>? = thumbnailInfos
    override fun i_maxImageViewportHeightPercentage(): Int = maxImageViewportHeightPercentage
    override fun i_content(): String? = content

    override fun i_source(): PostSourceAsset? = source
    override fun i_title(): String? = title
    override fun i_linkAsset(): LinkAsset? = linkAsset
    override fun i_viral(): ViralAsset? = viral
    override fun i_videoAsset(): VideoAsset? = videoAsset
    override fun i_showVideoIcon(): Boolean = showVideoIcon ?: false

    fun withModifiedUiTypeForLocalCard() : RepostAsset {
        val newUiType = if (title.isNullOrEmpty() && content.isNullOrEmpty()) UiType2.HERO
        else UiType2.NORMAL
        return copy(uiType = newUiType)
    }

}

data class PostSourceAsset(val id: String? = null,
                           val catId: String? = null,
                           val handle: String? = null,
                           val displayName: String? = null,
                           val legacyKey: String? = null,
                           val sourceName: String? = null,
                           val playerKey: String? = null, //Used only for player JS download
                           val entityImageUrl: String? = null,
                           val icon: String? = null,
                           val imageUrl: String? = null,
                           val type: String? = null,
                           @Embedded(prefix = "c_")
                           var counts: Counts2? = null,
                           val entityType: String? = null,
                           val feedType: String? = null,
                           val deeplinkUrl: String? = null,
                           val nameEnglish: String? = null) : Serializable

data class PostSuggestedFollow(val sourceId: String? = null,
                               val sourceName: String? = null,
                               val displayName: String? = null,
                               val icon: String? = null,
                               val imageUrl: String? = null,
                               val type: String? = null)

data class DiscussionResponse(val rows: List<PostEntity>? = null,
                              val count: Int? = null,
                              val firstPageUrl: String? = null,
                              val pageMarker: String? = null,
                              val pageNumber: Int? = null,
                              val filter: List<DiscussionFilter>? = null,
                              val nextPageUrl: String? = null)

data class LikeAsset(val description: String?,
                     val entityKey: String?,
                     val actionableEntity: ActionableEntity?,
                     val pageScrollValue: String?,
                     val actionTime: Long?,
                     val action: String? = null)

data class Author(val description: String?,
                  val handle: String? = null,
                  val name: String?,
                  val profileImage: String?)

data class DiscussionFilter(val displayName: String,
                            val filterValue: String)

enum class PostPrivacy : Serializable {
    PRIVATE, PUBLIC;
}

data class PostMeta(val privacyLevel: PostPrivacy,
                    val allowComments: Boolean) : Serializable


data class PollAsset(val pollTitle: String? = null,
                     val startDate: Long? = null,
                     val endDate: Long? = null,
                     val options: List<PollOptions>? = null,
                     val showResult: Boolean = false,
                     val responseCount: Int? = null,
                     val interactionUrl: String?,
                     val originalStartDate: Long? = null) : Serializable

data class PollAssetResponse(val pollTitle: String? = null,
                             val startDate: Long? = null,
                             val endDate: Long? = null,
                             val options: List<PollOptions>? = null,
                             val showResult: Boolean = false,
                             val responseCount: Int? = null,
                             val interactionUrl: String? = null,
                             val selectedOption: String? = null) {
    fun toPollAsset() = PollAsset(pollTitle, startDate, endDate, options, showResult, responseCount, interactionUrl)
}

data class HastTagAsset(val name: String,
                        val url: String,
                        val type: String?) : Serializable

data class PollOptions(val id: String,
                       val title: String,
                       val count: Int? = 0) : Serializable

data class LinkAsset(val name: String? = null,
                     val type: String? = null,
                     val version: String? = null,
                     val title: String? = null,
                     val url: String? = null,
                     val author: String? = null,
                     val authorUrl: String? = null,
                     val providerName: String? = null,
                     val description: String? = null,
                     val thumbnailUrl: String? = null,
                     val thumbnailWidth: String? = null,
                     val thumbnailHeight: String? = null,
                     val html: String? = null) : Serializable

data class ViralAsset(
        @Embedded(prefix = "bg_")
        val backgroundOption: BackgroundOption2? = null,
        val itemText: String? = null,
        val itemTextColor: String? = null,
        /*TODO :: can be list of string only*//*
        val tags: List<String>,*/
        val topics: List<String>? = null,
        val nsfw: Boolean = false,
        val aspectRatio: Float? = null,
        val vhSourcePageUrl: String? = null,
        val relatedUrl: String? = null,
        val downloadUrl: String? = null) : Serializable

data class CollectionEntity(
        @Ignore
        var collectionItem: List<PostEntity>? = null,
        var includeCollectionInSwipe: Boolean = false,
        @Embedded(prefix = "carousel_prop_")
        var carouselProperties: CarouselProperties2? = null,
        var hideTitle: Boolean = false) : Serializable

data class CarouselProperties2(val animationType: AnimationType? = null,
                               val autoSwipeListIntervalInSeconds: Long? = null,
                               val autoSwipeDetailIntervalInSeconds: Long? = null,
                               val circular: Boolean? = null,
                               val relateUrl: String? = null,
                               val aspectRatio: Float? = null,
                               val actionButtonText: String? = null,
                               val actionButtonIcon: String? = null,
                               val viewMoreText: String? = null,
                               val nextPageUrl: String? = null,
                               val backgroundColorDay:String? = null,
                               val backgroundColorNight:String? = null,
                               val backgroundImageUrl: String? = null,
                               val viewAllUrl: String? = null,
                               val nightModeBackgroundImageUrl: String? = null) : Serializable, CollectionProperties {

    override fun i_viewMoreText(): String? {
        return viewMoreText
    }

    override fun i_viewAllUrl(): String? {
        return viewAllUrl
    }

    override fun i_viewMoreUrl(): String? {
        return nextPageUrl
    }
}

data class VideoAsset(val assetId: String? = null,
                      val type: String? = null,
                      val playerType: String? = null,
                      val duration: String? = null,
                      val videoDurationInSecs: Int = 0,
                      val httpsEnabled: Boolean = false,
                      val autoplayable: Boolean = false,
                      val url: String? = null,
                      val adUrl: String? = null,
                      val viewBeaconUrl: String? = null,
                      val playBeaconUrl: String? = null,
                      val srcVideoId: String? = null,
                      val width: Int = 0,
                      val height: Int = 0,
                      val useEmbed: Boolean = false,
                      val disableAds: Boolean = false,
                      val loopCount: Int = 0,
                      val isGif: Boolean = false,
                      val hideControl: Boolean = false,
                      val liveStream: Boolean = false,
                      val applyPreBufferSetting: Boolean = false,
                      val replaceableParams: Map<String, String>? = null,
                      val downloadable: Boolean = false,
                      val downloadVideoUrl: String? = null,
                      val inExpandMode: Boolean = false,
                      var downloadRequestId: Long? = null,
                      var localVideoFilePath: String? = null,

                      var isPrefetch: Boolean = false,
                      var prefetchDurationInSec: Float = 9F,// Prefetch 10sec of video

                      var streamCachedDuration: Float = 0F,//video cached duration in sec
                      var streamCachedUrl: String? = null, //once video is cached
                      var streamCachedStatus: StreamCacheStatus? = null,
                      var streamDownloadPercentage: Float = 0F,
                      var configType: ConfigType? = null,
                      var itemIndex: Int? = null,
                      var variantIndex: Int? = null,
                      var isForceVariant: Boolean? = null,
                      var cacheType: ItemCacheType? = null,
                      var downloadErrorMsg: String? = null,
                      var selectedQuality: String? = null) : Serializable

data class CardLabel2(var type: com.newshunt.dataentity.news.model.entity.server.asset.CardLabelType? = null,
                      var text: String? = null,
                      var bgType: com.newshunt.dataentity.news.model.entity.server.asset.CardLabelBGType? = null,
                      var bgColor: String? = null,
                      var fgColor: String? = null,
                      var ttl: Long? = null) : Serializable {

    companion object {
        private const val serialVersionUID = -5023120575674028754L
    }
}

data class ShareParam2(var shareTitle: String? = null,
                       var shareDescription: String? = null,
                       val sourceName: String? = null) : Serializable


data class BackgroundOption2(var type: BackgroundType2 = BackgroundType2.BG_COLOR,
                             var bgColor: String? = null, /*FOR type = BG_COLOR*/
                             var startColor: String? = null, /* FOR type = GRADIENT*/
                             var midColor: String? = null,/* FOR type = GRADIENT*/
                             var endColor: String? = null,/* FOR type = GRADIENT*/
                             var gradientType: String? = "BL_TR",
                             var borderColor: String? = null,
                             /** Gradient orientation @see
                             GradientDrawable.Orientation*/
                             var imageUrl: String? = null, /*background as image type=BG_IMAGE*/
                             var width: Int = 0, /*original image width*/
                             var height: Int = 0, /*original image height*/
                             var fitType: String? = FIT_TYPE.TOP_CROP.name) : Serializable

enum class BackgroundType2 {
    BG_COLOR, GRADIENT, IMAGE_BG
}

enum class AssetType2 {
    POST, REPOST, COMMENT, JSON_DATA, QUESTION_MULTI_CHOICES
}

val DEFAULT = EntityConfig2(Constants.ZERO_STRING, 0)

data class Counts2(
        @Embedded(prefix = "story_") var STORY: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "sources_") var SOURCES: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "follow_") var FOLLOW: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "like_") var LIKE: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "comments_") var COMMENTS: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "views_") var VIEWS: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "share_") var SHARE: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "sad_") var SAD: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "happy_") var HAPPY: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "love_") var LOVE: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "angry_") var ANGRY: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "wow_") var WOW: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "total_like_") var TOTAL_LIKE: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "watch_") var WATCH: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "download_") var DOWNLOAD: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "repost_") var REPOST: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "total_pending_approvals_") var TOTAL_PENDING_APPROVALS: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "invites_") var INVITES: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "post_approvals_") var POST_APPROVALS: EntityConfig2? = DEFAULT,
        @Embedded(prefix = "member_approvals_") var MEMBER_APPROVALS: EntityConfig2? = DEFAULT) : Serializable {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun incCount(inp: EntityConfig2?): EntityConfig2 {
        val newConfig = inp ?: EntityConfig2("0")
        val intval = when {
            newConfig.value.isEmpty() -> "1" // incremented (treating it as 0)
            else -> newConfig.value.trim().toIntOrNull()?.inc()?.toString() ?: newConfig.value
        }
        val newsTs = (newConfig.ts ?: 0) + 1
        return newConfig.copy(value = intval, ts = newsTs)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun decCount(inp: EntityConfig2?): EntityConfig2 {
        val newConfig = inp ?: EntityConfig2("0")
        val newval = newConfig.value.trim().toIntOrNull()?.dec()?.coerceAtLeast(0)?.toString()
                ?: newConfig.value
        val newsTs = (newConfig.ts?:0) + 1
        return newConfig.copy(newval, ts = newsTs)
    }

    fun incrementCommentCount(): Counts2 = this.copy(COMMENTS = incCount(COMMENTS))

    fun decrementCommentCount(): Counts2 = this.copy(COMMENTS = decCount(COMMENTS))

    fun decrementRepostCount(): Counts2 = this.copy(REPOST = decCount(REPOST))

    fun incrementRepostCount() : Counts2 = this.copy(REPOST = incCount(REPOST))

    fun incrementShareCount() : Counts2 = this.copy(SHARE = incCount(SHARE))

    fun incrementTotalLikeCount() : Counts2 = this.copy(TOTAL_LIKE = incCount(TOTAL_LIKE))

    fun decrementTotalLikeCount() : Counts2 = this.copy(TOTAL_LIKE = decCount(TOTAL_LIKE))


    fun incrementViewCount() = this.copy(VIEWS = incCount(VIEWS))

}

fun mergeCounts(c1: Counts2?, c2: Counts2?): Counts2? {
    if (c1 == null) {
        return c2
    }
    if (c2 == null) {
        return c1
    }
    return Counts2(
            STORY = mergeCountEntity(c1.STORY, c2.STORY),
            SOURCES = mergeCountEntity(c1.SOURCES, c2.SOURCES),
            FOLLOW = mergeCountEntity(c1.FOLLOW, c2.FOLLOW),
            LIKE = mergeCountEntity(c1.LIKE, c2.LIKE),
            COMMENTS = mergeCountEntity(c1.COMMENTS, c2.COMMENTS),
            VIEWS = mergeCountEntity(c1.VIEWS, c2.VIEWS),
            SHARE = mergeCountEntity(c1.SHARE, c2.SHARE),
            SAD = mergeCountEntity(c1.SAD, c2.SAD),
            HAPPY = mergeCountEntity(c1.HAPPY, c2.HAPPY),
            LOVE = mergeCountEntity(c1.LOVE, c2.LOVE),
            ANGRY = mergeCountEntity(c1.ANGRY, c2.ANGRY),
            WOW = mergeCountEntity(c1.WOW, c2.WOW),
            TOTAL_LIKE = mergeCountEntity(c1.TOTAL_LIKE, c2.TOTAL_LIKE),
            WATCH = mergeCountEntity(c1.WATCH, c2.WATCH),
            DOWNLOAD = mergeCountEntity(c1.DOWNLOAD, c2.DOWNLOAD),
            REPOST = mergeCountEntity(c1.REPOST, c2.REPOST),
            TOTAL_PENDING_APPROVALS = mergeCountEntity(c1.TOTAL_PENDING_APPROVALS, c2.TOTAL_PENDING_APPROVALS),
            INVITES = mergeCountEntity(c1.INVITES, c2.INVITES),
            MEMBER_APPROVALS = mergeCountEntity(c1.MEMBER_APPROVALS, c2.MEMBER_APPROVALS),
            POST_APPROVALS = mergeCountEntity(c1.POST_APPROVALS, c2.POST_APPROVALS)
    )
}

fun mergeCountEntity(e1: EntityConfig2?, e2: EntityConfig2?): EntityConfig2? {
    if (e1 == null) {
        return e2
    }
    if (e2 == null) {
        return e1
    }
    val t1 = e1.ts ?: 0
    val t2 = e2.ts ?: 0
    if (t1 > t2) {
        return e1
    } else {
        return e2
    }
}

enum class CountType : Serializable {
    STORY,
    SOURCES,
    FOLLOW,
    LIKE,
    COMMENTS,
    VIEWS,
    SHARE,
    SAD,
    LOVE,
    ANGRY,
    WOW,
    TOTAL_LIKE,
    WATCH,
    DOWNLOAD,
    REPOST,
    TOTAL_PENDING_APPROVALS
}

data class EntityConfig2(val value: String, val ts: Long? = null) : Serializable

data class ApprovalCounts(@Embedded(prefix = "total_pending_approvals_") var TOTAL_PENDING_APPROVALS: EntityConfig2? = DEFAULT,
                          @Embedded(prefix = "invites_") var INVITES: EntityConfig2? = DEFAULT,
                          @Embedded(prefix = "post_approvals_") var POST_APPROVALS: EntityConfig2? = DEFAULT,
                          @Embedded(prefix = "member_approvals_") var MEMBER_APPROVALS: EntityConfig2? = DEFAULT)
/*

data class FeedbackTags2(val list: List<L1L2Mapping2>?, // card level override of master options
                         val detail: List<String>?, // card level override of master options
                         val hideType: HideType?,
                         val hideIcon: String,
                         val hideText: String,
                         val hideButtonText: String,
                         val hideDescription: String) : Serializable

data class L1L2Mapping2(
        val l1Key: String,
        val l2Options: List<String> // list of l2keys
) : Serializable
*/

enum class NodeType2 : Serializable {
    TOPIC, GROUP, SOURCE, CATEGORY;
}


/*data class TickerNode2(
        var tickers: List<Ticker2>? = null,
        var refreshTime: Int? = null,
        var viewOrder: Int? = null,
        var tickerHeight: Int? = -1,
        var error: String? = null,
        var swipeInterval: Long = 5000L,
        var eventsSent: HashSet<String>) : Observable(), Serializable {
    fun isEventsSent(tickerId: String): Boolean {
        return if (CommonUtils.isEmpty(tickerId)) {
            true
        } else eventsSent.contains(tickerId)
    }
}*/

data class Ticker2(
        val id: String = Constants.EMPTY_STRING,
        var type: String = Constants.EMPTY_STRING,
        var format: Format = Format.TICKER,
        var subFormat: SubFormat = SubFormat.HTML,
        var uiType: UiType2 = UiType2.NORMAL,
        var isHardwareAccelerated: Boolean? = false,
        @Embedded(prefix = "bg_")
        var backgroundOption: BackgroundOption2? = null,
        val deeplinkUrl: String? = null,
        var imageUrl: String? = null,
        var content: String? = null,
        var tickerHeight: Int? = -1,
        var contentBaseUrl: String? = null,
        var experiment: Map<String, String>? = null
) : CommonAsset, Observable(), Serializable {
    override fun i_id(): String = id

    override fun i_type(): String = type

    override fun i_format(): Format? = format

    override fun i_subFormat(): SubFormat? = subFormat

    override fun i_uiType(): UiType2? = uiType

    override fun i_deeplinkUrl(): String? {
        return deeplinkUrl
    }

    override fun i_experiments(): Map<String, String>? {
        return experiment
    }

    fun isTickerTypeImage(): Boolean {
        return !CommonUtils.isEmpty(imageUrl)
    }
}


/*Cold start Asset to handle server response*/
data class ColdStartEntity(
        var id: String = Constants.EMPTY_STRING,
        var selectText: String? = null,
        var unSelectText: String? = null,
        var actionType: String? = null,
        var hideSelection: Boolean? = null,
        var viewAllDeeplink: String? = null,
        var shortTitle: String? = null,
        var widgetType: String? = null,
        @Embedded
        var itemToFillFilter: ItemToFilter? = null,
        var removeOnScroll: Boolean? = null,
        var showPlusIcon: Boolean? = null,
        var searchContext: String? = null,
        var viewAllText: String? = null,

        /*Make sure @Ignore is from androidx.room.Ignore*/
        @Ignore
        var coldStartCollectionItems: List<ColdStartEntityItem>? = null) : Serializable

data class ItemToFilter(val entityType: List<String> = emptyList(),
                        val entitySubType: List<String> = emptyList(),
                        val action: String? = null) : Serializable

interface BaseDetailList : Serializable {
    fun i_id(): String
    fun i_video_assetId(): String?
    fun i_mm_includeCollectionInSwipe(): Boolean?
    fun i_format(): Format?
    fun i_level(): PostEntityLevel?
    fun i_langCode(): String? {
        return null
    }
    fun i_adId(): String? = null
}

/**
 * Details page adapter to operate on.
 */
data class DetailListCard(
        var id: String,
        var video_assetId: String?,
        var mm_includeCollectionInSwipe: Boolean?,
        var format: Format = Format.HTML,
        var imageDetails: ImageDetail?,
        var shareUrl: String?,
        var level: PostEntityLevel,
        val source: PostSourceAsset?,
        val subformat: SubFormat?,
        val type: String?,
        val title: String?,
        val moreStoryCount: Int?,
        val langCode: String?,
        val adId: String?
) : BaseDetailList {
    override fun i_id(): String {
        return id
    }

    override fun i_video_assetId(): String? {
        return video_assetId
    }

    override fun i_mm_includeCollectionInSwipe(): Boolean? {
        return mm_includeCollectionInSwipe
    }

    override fun i_format(): Format? {
        return format
    }

    fun i_subFormat(): SubFormat? {
        return subformat
    }

    fun i_type(): String? {
        return type
    }

    override fun i_level(): PostEntityLevel? {
        return level
    }

    override fun i_langCode(): String? {
        return langCode
    }

    override fun i_adId(): String? = adId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailListCard

        if (id != other.id) return false
        if (format != other.format) return false
        if (subformat != other.subformat) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + (subformat?.hashCode() ?: 0)
        return result
    }
}

data class ColdStartEntityItem(
        var entityId: String = Constants.EMPTY_STRING,
        var entityType: String = Constants.EMPTY_STRING,
        var iconUrl: String? = null,
        var imageUrl:String?=null,
        var entityImageUrl: String? = null,
        var displayName: String? = null,
        var handle: String? = null,
        var deeplinkUrl: String? = null,
        var entitySubType: String? = null,
        var memberApproval: SettingState? = null,
        var isSelected: Boolean? = null,
        var isGroupSelected: Boolean? = null,
        val nameEnglish: String? = null,
        val experiment: Map<String, String>? = null,
        val nativeCardType: String? = null) : EntityItem, Serializable {

    override fun i_entityId(): String? {
        return entityId
    }

    override fun i_imageUrl(): String? {
        return imageUrl
    }

    override fun i_entityType(): String? {
        return entityType
    }

    override fun i_iconUrl(): String? {
        return iconUrl
    }

    override fun i_entityImageUrl(): String? {
        return entityImageUrl
    }

    override fun i_displayName(): String? {
        return displayName
    }

    override fun i_handle(): String? {
        return handle
    }

    override fun i_deeplinkUrl(): String? {
        return deeplinkUrl
    }

    override fun i_entitySubType(): String? {
        return entitySubType
    }

    override fun isCreateGroupCard(): Boolean? {
        return ColdStartActionButtonType.fromName(nativeCardType) == ColdStartActionButtonType.CREATE_GROUP
    }

    override fun i_memberApproval(): SettingState? {
        return memberApproval
    }

    override fun i_selected(): Boolean {
        return (if (i_entityType() == ColdStartEntityType.COMMUNITY_GROUP.name) {
            isGroupSelected
        } else {
            isSelected
        })?:false
    }

    override fun i_nameEnglish(): String? {
        return nameEnglish
    }

    override fun i_isImportContactCard(): Boolean {
        return ColdStartActionButtonType.fromName(nativeCardType) == ColdStartActionButtonType.IMPORT_CONTACTS
    }


    override fun i_experiment() : Map<String, String>? {
        return experiment
    }


    companion object {
        const val ENTITY_ID_FOLLOW_MORE = "entity_id_follow_more"
    }
}


enum class SubFormat {
    STORY,
    S_W_VIDEO,
    S_W_IMAGES,
    S_W_PHOTOGALLERY,
    RICH_PHOTOGALLERY,
    S_W_ATTACHED_IMAGES,
    TVVIDEO,
    VHMEME,
    VHTEXT,
    VHMEMETEXT,
    VHGIF,
    SINGLE_SELECT,
    ENTITY,
    PENDING_APPROVAL,
    HTML,
    VIDEO,
    IMAGE,
    ASTRO,
    AUTOPLAY,
    AD,
    TICKER,
    WEB,
    COLD_START_HEADER_CARD,
    /*Language select card*/
    LANGUAGE_SELECT,
    WEBITEM_HTML,
    LOCATION,
    TVGIF,
    ADS_STORY,
    WEB_ADJUNCT,
    HTML_AND_VIDEO,
    VIRAL_AND_VIDEO
}

enum class Format {
    HTML,
    VIDEO,
    EMBEDDED_VIDEO,
    IMAGE, POLL,
    COLLECTION,
    POST_COLLECTION,
    MEMBER,
    GROUP_INVITE,
    NATIVE_CARD,
    ENTITY,
    BANNER,
    AD,
    TICKER,
    LOCAL,
    PHOTO,
    WEB,
    EXTRA /*For card which inserted from client only*/,
    WEBITEM,
    PLACEHOLDER,
    LANGUAGE,
    ADS,
    NESTED_COLLECTION
}

enum class UiType2 {
    NORMAL,
    HERO,
    GRID,
    GRID_2,
    GRID_3,
    GRID_5,
    GRID_4,
    AUTOPLAY,
    VH_FIT_BACKGROUND,
    GRP_MEMBER,
    GRP_MEMBER_SUGGESTION,
    MEMBER_INVITATION,
    HORIZONTAL_BAR,
    CAROUSEL_1,
    CAROUSEL_2,
    CAROUSEL_5,
    CAROUSEL_3,
    CAROUSEL_4,
    CAROUSEL_6,
    CAROUSEL_7,//Video Carousel
    CAROUSEL_8,//Autoplay Carousel
    CAROUSEL_9,//For dynamic number of contents in collection of collection cards or square card carousel
    TAGS,
    TICKER,
    BIG,
    LIST,
    VH_BIG,
    VH_SMALL,
    USER_INTERACTION,
    HERO_DYNAMIC,
    SUBSCRIBE
}


enum class DetailAttachLocation {

    ATTACHED_IMAGES_TOP,
    ATTACHED_IMAGES_BOTTOM,
    NORMAL

}

/**
 * Display type for Posts.
 * (Exclude indices used in @see{AdDisplayType})
 */
enum class PostDisplayType(val index: Int) {
    SIMPLE_POST(0),
    IMAGES_2(1),
    IMAGES_3(2),
    IMAGES_4(3),
    IMAGES_5(4),
    VIRAL(5),
    POLL(6),
    POLL_RESULT(7),
    MEMBER_INFO(8),
    SIMPLE_POST_LOW(9),
    REPOST_BIG_IMAGE(10),
    REPOST_NORMAL(11),
    REPOST_POLL(12),
    OG_ITEM(13),
    GROUP_INVITE(14),
    REPOST_OG(15),
    QMC_CAROUSEL1(17), // carousel list with icon item
    QMC_CAROUSEL2(18), // carousel list with medium banner item
    QMC_CAROUSEL3(19), // carousel list with banner item
    QMC_CAROUSEL4(20), // carousel list with big banner item
    QMC_CAROUSEL5(21),
    QMC_CAROUSEL6(22),
    QMC_GRID(23), // grid list with icon item
    QMC_GRID_2(24), // grid list with banner item
    QMC_CREATE_GROUP(25),  // Creator card item for question multi choice will not come in primary list
    QMC_TAGS(26),  // question multi choice with tags item
    AUTOPLAY_EXO(27),
    AUTOPLAY_WEB(28),
    POST_COLLECTION_HTML(29),
    POST_COLLECTION_VIDEO(30),
    POST_COLLECTION_IMAGE(31),
    FOOTER(32),
    ASTRO(33),
    POST_COLLECTION_AUTOPLAY(34),
    ENTITY_INFO(35),
    APPROVAL_CARD(36), //pending approvals card for posts
    USER_INTERACTION(37),
    BANNER(38),
    TICKER(39),
    POST_COLLECTION_SAVED_STORIES(40),
    POST_COLLECTION_SAVED_VIDEOS(41),
    SAVED_STORY_LIST_ITEM(42),
    SAVED_VIDEO_LIST_ITEM(43),
    SAVED_ITEMS_LIST(44),
    LOCAL_POLL(45),
    LOCAL_NORMAL(46),
    LOCAL_OG(47),
    LOCAL(48),
    SEARCH_PHOTO_GRID(49),
    REPOST_VIRAL(50),
    SIMPLE_WEB(51),
    LANGUAGE_SELECT_CARD(52),
    DATE_SEPARATOR(53),
    LOGIN_NUDGE(54),
    GUEST_USER(55),
    EMPTY(56),
    QMC_FOLLOW_MORE(57),
    COLD_START_HEADER_CARD(58),
    SIMPLE_POST_DYNAMIC_HERO(59),
    QMC_IMPORT_CONTACTS(60),
    LOCATION_SELECT_CARD(61),
    HTML_AND_VIDEO_CAROUSEL(62),
    SQUARE_CARD_CAROUSEL(63),
    COLLECTION_OF_COLLECTION_CARD(64),
    COLLECTION_OF_COLLECTION_CARD_ITEM(65);
}

// These indices not to be used for other display types in list
enum class AdDisplayType(val index: Int) {
    //Ads related display types
    APP_DOWNLOAD(10000),
    EXTERNAL_SDK(10001),
    HTML_AD(10002),
    EXTERNAL_NATIVE_PGI(10003),
    NATIVE_AD(10004),
    NATIVE_HIGH_AD(10005),
    NATIVE_DFP_AD(10006),
    NATIVE_DFP_HIGH_AD(10007),
    AD_FB_NATIVE(10010),
    AD_FB_NATIVE_HIGH(10011),
    IMA_VIDEO_AD(10012),
    IMAGE_LINK(10013),
    HTML_AD_FULL(10014),
    PGI_ARTICLE_AD(10016),
    EMPTY_AD(10017),
    NATIVE_ENHANCED_HIGH_AD(10018),
    IMAGE_LINK_FULL(10019)
}

enum class PostSourceType {
    ICC, //Creators
    UGC, //Users (who are not creator)
    OGC, //Existing publisher
    PGC //do not know
}

enum class ColdStartEntityType {
    LOCATION,
    SOURCE,
    HASHTAG,
    PROFILE,
    COMMUNITY_GROUP
}


interface CommonAsset : Serializable {
    fun i_id(): String
    fun i_type(): String
    fun i_format(): Format?
    fun i_subFormat(): SubFormat?
    fun i_uiType(): UiType2?
    fun i_maxImageViewportHeightPercentage(): Int {
        return 0
    }

    fun i_contentImageInfo(): ImageDetail? {
        return null
    }
    fun i_source(): PostSourceAsset? {
        return null
    }

    fun i_langCode(): String? {
        return null
    }

    fun i_publishTime(): Long? {
        return null
    }

    fun i_recommendationTs(): Long? {
        return null
    }

    fun i_isFollowin(): Boolean? {
        return null
    }

    fun i_isReported(): Boolean? {
        return null
    }

    fun i_isDeleted() : Boolean {
        return false
    }

    fun i_title(): String? {
        return null
    }

    fun i_englishTitle(): String? {
        return null
    }

    fun i_contentType(): String? {
        return null
    }

    fun i_content(): String? {
        return null
    }

    fun i_contentImage(): String? {
        return null
    }

    fun i_detailUiType(): UiType2? {
        return null
    }

    fun i_detailAttachLocation(): DetailAttachLocation? {
        return null
    }

    fun i_progress(): Int? {
        return null
    }

    fun i_status(): String? {
        return null
    }

    fun i_shareUrl(): String? {
        return null
    }

    fun i_thumbnailUrls(): List<String>? {
        return null
    }

    fun i_thumbnailUrlDetails(): List<ImageDetail>? {
        return null
    }

    fun i_videoAsset(): VideoAsset? {
        return null
    }

    fun i_immersiveUrl(): String? {
        return null
    }

    fun i_tickerRefreshTime(): Int? {
        return null
    }

    fun i_counts(): Counts2? {
        return null
    }

    fun i_cardLabel(): CardLabel2? {
        return null
    }

    fun i_cardLocation(): String?
    {
        return null
    }

    fun i_viral(): ViralAsset? {
        return null
    }

    fun i_tsString(): String? {
        return null
    }

    fun i_referralString(): String? {
        return null
    }

    fun i_repostNudgeText(): String? {
        return null
    }
    fun i_showTsAlways(): Boolean {
        return false
    }

    fun i_tickerUrl(): String? {
        return null
    }

    fun i_DetailPageWidgetOrderId(): String? {
        return null
    }

    fun i_isSelectAndCopyPasteDisabled(): Boolean? {
        return false
    }

    fun i_idParams(): Map<String, String>? {
        return null
    }

    fun i_showTsIcon(): Boolean {
        return false
    }

    fun i_postLocation(): String? {
        return null
    }

    fun i_showPublishDate(): Boolean {
        return false
    }

    fun i_poll(): PollAsset? {
        return null
    }

    fun i_postPrivacy(): PostPrivacy? {
        return null
    }

    fun i_deeplinkUrl(): String? {
        return null
    }

    fun i_pollSelectedOptionId(): String? {
        return null
    }

    fun i_reason(): String? {
        return null
    }

    fun i_selectedLikeType(): String? {
        return null
    }

    fun i_linkAsset(): LinkAsset? {
        return null
    }

    fun i_moreStories(): List<PostEntity>? {
        return null
    }

    fun i_isRead(): Boolean? {
        return null
    }

    fun i_npCoverageText(): String? {
        return null
    }

    fun i_moreCoverageCount(): Int? {
        return null
    }

    fun i_moreCoverageIcons(): List<String>? {
        return null
    }

    fun i_contentBaseUrl(): String? {
        return null
    }

    fun i_disclaimer(): String? {
        return null
    }

    fun i_moreContentLoadUrl(): String? {
        return null
    }

    fun i_repostAsset(): RepostAsset? {
        return null
    }

    fun i_isFollowable(): Boolean = true

    fun i_groupId(): String? {
        return null
    }

    fun i_showVideoIcon(): Boolean {
        return false
    }

    fun i_isApprovalPending(): Boolean = false
    fun i_content2(): String? = null
    fun i_moreCoverageNextPageUrl(): String? = null
    fun i_hashtags(): List<HastTagAsset>? = null
    fun i_collectionItems(): List<CommonAsset>? {
        return null
    }

    fun i_postState() : PostState? {
        return PostState.PUBLISHED
    }

    fun i_commentRepostItems(): List<PostEntity>? = null

    fun i_ticker(): List<Ticker2>? {
        return null
    }

    fun i_hideTitle(): Boolean? = null
    fun i_carouselProperties(): CarouselProperties2? {
        return null
    }

    fun i_experiments(): Map<String, String>? = null

    fun i_isFocusableInTouchMode(): Boolean? = false

    /*COLD START FUNCTIONS START*/

    fun i_coldStartAsset(): ColdStartEntity? = null

    fun i_selectText(): String? {
        return null
    }

    fun i_unSelectText(): String? {
        return null
    }

    fun i_hideSelection(): Boolean? {
        return null
    }

    fun i_viewAllDeeplink(): String? {
        return null
    }

    fun i_shortTitle(): String? {
        return null
    }

    fun i_viewallText(): String? {
        return null
    }

    fun i_widgetType(): String? {
        return null
    }

    fun i_itemToFillFilter(): ItemToFilter? {
        return null
    }

    fun i_removeOnScroll(): Boolean? {
        return null
    }

    fun i_showPlusIcon(): Boolean? {
        return null
    }

    fun i_searchContext(): String? {
        return null
    }

    fun i_entityCollection(): List<EntityItem>? {
        return null
    }

    fun i_childFetchUrl(): String? {
        return null
    }

    fun i_childCount(): Int? {
        return 0
    }

    fun i_nonLinearPostUrl(): String? = null

    /*COLD START FUNCTION END*/
    fun i_userInteractionAsset(): UserInteraction? = null

    fun i_wordCount(): Int? {
        return null
    }

    fun i_imageCount(): Int? {
        return null
    }

    fun i_collection_viewAllText(): String? {
        return null
    }

    fun i_webcard(): WebCard2? {
        return null
    }

    fun i_level(): PostEntityLevel {
        return PostEntityLevel.TOP_LEVEL
    }

    fun i_adId(): String? {
        return null
    }

    fun i_adSpec(): AdSpec? = null

    fun i_allowComments(): Boolean {
        return true
    }

    fun i_isFavourite(): Boolean? {
        return null
    }

    fun i_articleTrack(): Track? {
        return null
    }

    fun i_landingType(): CardLandingType? {
        return null
    }

    fun i_onClickUseDeeplink(): Boolean? {
        return null
    }
    fun i_localInfo() : LocalInfo? = null

    fun i_ignoreSourceBlock() : Boolean{
        return false
    }

    fun i_removePadding() : Boolean? {
        return null
    }

    fun i_adjunctLang() : String? {
        return null
    }

    fun copyWith(isFollowin: Boolean? = null,
                 selectedLikeType: String? = null,
                 pollSelectedOptionId: String? = null,
                 isRead: Boolean? = false,
                 coldStartItems: List<ColdStartEntityItem>?,
                 collectionItems: List<PostEntity>?): CommonAsset {
        return this
    }

    fun toAnyCard() : AnyCard? = null
    fun rootPostEntity(): PostEntity? = null

    fun isLocalCard() = i_format() == Format.LOCAL

    fun isSavedCarousel() =
            i_format() == Format.POST_COLLECTION && i_uiType() == UiType2.CAROUSEL_6

    val isViralCard : Boolean
        get() = i_format() == Format.IMAGE

    fun i_subText(): String? {
        return null
    }

    fun i_parentPostId(): String? {
        return null
    }

    fun i_totalThumbnails(): Int? {
        return 0
    }
    fun i_isFromCache(): Boolean {
        return false
    }

    fun i_referrerItemId(): String? {
        return null
    }

    fun i_referrerEntitySourceType(): String? {
        return null
    }

    fun i_distancingSpec():DistancingSpec?{
        return null
    }

    fun i_richTextChunk1(): Boolean {
        return true
    }
    fun i_richTextChunk2(): Boolean {
        return false
    }

    fun i_enableBigtextChunk1(): Boolean? {
        return true
    }
    fun i_enableBigtextTitle(): Boolean? {
        return true
    }
    fun i_titleFirstAksharCharCount():Int?{
        return null
    }
    fun i_c1FirstAksharCharCount():Int?{
        return null
    }
    fun i_byline(): String? {
        return null
    }
}

/**
 * Helper function to print id and view-count.
 * Not expected to be implemented or overriden
 */
fun CommonAsset.i_VC() = "uid=${(this as? Card)?.uniqueId?:i_id()}, VIEW=${i_counts()?.VIEWS}"


interface EntityItem {
    fun i_entityId(): String? {
        return null
    }

    fun i_imageUrl():String?{
        return null
    }

    fun i_entityType(): String? {
        return null
    }

    fun i_iconUrl(): String? {
        return null
    }

    fun i_entityImageUrl(): String? {
        return null
    }

    fun i_displayName(): String? {
        return null
    }

    fun i_handle(): String? {
        return null
    }

    fun i_deeplinkUrl(): String? {
        return null
    }

    fun i_entitySubType(): String? {
        return null
    }

    fun isCreateGroupCard(): Boolean? {
        return null
    }

    fun i_selected(): Boolean {
        return false
    }

    fun i_childFetchUrl(): String? {
        return null
    }

    fun i_memberApproval(): SettingState? {
        return null
    }

    fun i_nameEnglish(): String? {
        return null
    }

    fun i_isImportContactCard(): Boolean {
        return false
    }

    fun i_experiment() : Map<String, String>? {
        return null
    }
}

interface CollectionProperties {
    fun i_viewMoreText(): String? {
        return null
    }

    fun i_viewMoreUrl(): String? {
        return null
    }

    fun i_viewAllUrl(): String? {
        return null
    }
}

data class UserInteraction(val activityId: String? = null,
                           val activityType: String? = null,
                           val activityTime: Long? = 0,
                           val activityDeeplink: String? = null,
                           val nsfw: Boolean = false,
                           val htmlTitle: String? = null) : Comparable<UserInteraction>, Serializable {
    override fun compareTo(other: UserInteraction): Int {
        return activityTime?.chronologicallyCompareTo(other.activityTime ?: 0) ?: -1
    }

    override fun equals(other: Any?): Boolean {
        return other is UserInteraction && CommonUtils.equals(activityId, other.activityId)
    }

    override fun hashCode(): Int {
        return activityId.hashCode()
    }
}

data class MinimizedPostEntity(var id: String = Constants.EMPTY_STRING,
                               var type: String = Constants.EMPTY_STRING,
                               val smallNonHtmlContent: String? = null, //Content with removing html
        // formating from first chunk at maxlength of 50
                               var format: Format = Format.HTML,
                               var contentType: String? = null,
                               var langCode: String? = null,
                               var landingType: CardLandingType? = null,
                               var subFormat: SubFormat = SubFormat.STORY,
                               var uiType: UiType2 = UiType2.NORMAL,
                               var source: PostSourceAsset? = null,
                               var title: String? = null,
                               var contentImage: String? = null,
                               var detailUiType: UiType2 = UiType2.NORMAL,
                               var detailAttachLocation: DetailAttachLocation? = DetailAttachLocation.NORMAL,
                               var shareUrl: String? = null,
                               var maxImageViewportHeightPercentage:Int = 0,
                               var allowComments: Boolean = true,
                               val experiments: Map<String, String>? = null,
                               var deeplinkUrl: String? = null,
                               var poll: PollAsset? = null,
                               var viral: ViralAsset? = null,
                               var thumbnailInfos: List<ImageDetail>? = null,
                               var repostAsset: RepostAsset? = null,
                               var videoAsset: VideoAsset? = null,
                               var immersiveUrl: String? = null,
                               var nonLinearPostUrl: String? = null,
                               var cardLocation: String? = null,
                               var linkAsset: LinkAsset? = null,
                               var groupId: String? = null,
                               var postPrivacy: PostPrivacy? = null) : Serializable, CommonAsset {
    override fun i_id(): String = id
    override fun i_type(): String = type
    override fun i_contentType(): String? = contentType
    override fun i_langCode(): String? = langCode
    override fun i_landingType(): CardLandingType? = landingType
    override fun i_content(): String? = smallNonHtmlContent
    override fun i_format(): Format = format
    override fun i_subFormat(): SubFormat = subFormat
    override fun i_uiType(): UiType2 = uiType
    override fun i_detailUiType(): UiType2 = detailUiType
    override fun i_detailAttachLocation(): DetailAttachLocation? = detailAttachLocation
    override fun i_source(): PostSourceAsset? = source
    override fun i_title(): String? = title
    override fun i_contentImage(): String? = contentImage
    override fun i_shareUrl(): String? = shareUrl
    override fun i_thumbnailUrls(): List<String>? = thumbnailInfos?.map { it.url }
    override fun i_experiments(): Map<String, String>? = experiments
    override fun i_thumbnailUrlDetails(): List<ImageDetail>? = thumbnailInfos
    override fun i_maxImageViewportHeightPercentage(): Int = maxImageViewportHeightPercentage
    override fun i_videoAsset(): VideoAsset? = videoAsset
    override fun i_immersiveUrl(): String? = immersiveUrl
    override fun i_nonLinearPostUrl(): String? = nonLinearPostUrl
    override fun i_cardLocation(): String? = cardLocation
    override fun i_viral(): ViralAsset? = viral
    override fun i_poll(): PollAsset? = poll
    override fun i_deeplinkUrl(): String? = deeplinkUrl
    override fun i_pollSelectedOptionId(): String? = null
    override fun i_reason(): String? = null
    override fun i_selectedLikeType(): String? = null
    override fun i_linkAsset(): LinkAsset? = linkAsset
    override fun i_groupId(): String? = groupId
    override fun i_repostAsset(): RepostAsset? = repostAsset
    override fun i_allowComments(): Boolean = allowComments
    override fun i_postPrivacy(): PostPrivacy? = postPrivacy
}


fun RepostAsset.toMinimizedRepostAsset(): RepostAsset {
    return this.copy(content = null)
}


data class ParentIdHolderCommenAsset(val parentPostId: String?,
                                     val asset: CommonAsset) : CommonAsset by asset, Serializable {
    override fun i_parentPostId(): String? {
        return parentPostId
    }
}

fun PostSourceAsset.getSourceDeeplink(withCategory: Boolean = false): String {
    if (CommonUtils.isEmpty(this.id)) {
        return Constants.EMPTY_STRING
    }
    val stringBuilder = StringBuilder()
    stringBuilder.append(Constants.SOURCE_DEEPLINK_INITIALS)
            .append(id)
            .append(Constants.DEEPLINK_SOURCE_IDENTIFIER)
            .append(id)
    if (!CommonUtils.isEmpty(catId) && withCategory) {
        stringBuilder.append(Constants.FORWARD_SLASH)
                .append(catId)
                .append(Constants.DEEPLINK_CATEGORY_IDENTIFIER)
                .append(catId)
    }
    return stringBuilder.toString()
}

enum class ColdStartActionButtonType {
    CREATE_GROUP,
    IMPORT_CONTACTS;

    companion object {
        @JvmStatic
        fun fromName(type: String?): ColdStartActionButtonType? {
            for (buttonType in values()) {
                if (buttonType.name == type) {
                    return buttonType
                }
            }
            return null
        }
    }
}

/**
 *  Defines video playing from cache or network
 */
enum class ItemCacheType(val type: String, val ascIndex: Int, val descIndex: Int) {
    //All cache enabled videos Ex: Foryou/deeplink/notification
    PREFETCH("prefetch", 1, 1),
    //From network
    NETWORK("network", 2, 2);
}

/**
 * Used to maintain the download status of video stream
 */
enum class StreamCacheStatus(val value: Int) {
    COMPLETE(1),
    PARTIAL(10),
    STARTED(100),
    NOT_DOWNLOADED(1000)
}

enum class ConfigType {
    BUZZ_LIST,
    NEWS_LIST,
    VIDEO_DETAIL_V
}