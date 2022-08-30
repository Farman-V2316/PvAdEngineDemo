/*
 * Created by Rahul Ravindran at 8/10/19 10:44 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LinkAsset
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.OEmbedResponse
import com.newshunt.dataentity.common.asset.PollAsset
import com.newshunt.dataentity.common.asset.PollOptions
import com.newshunt.dataentity.common.asset.PostCreateAsset
import com.newshunt.dataentity.common.asset.PostCreation
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostPollPojo
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.PostType
import com.newshunt.dataentity.common.asset.RepostAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import java.io.Serializable

const val CACHE_DEFAULT_TIME =  86400000L
const val MAX_IMAGE_COUNT = 5
const val DEFAULT_RETRY_LIMIT = 2
const val MAX_POLL_OPTION_LENGTH = 25
const val DEFAULT_IMAGE_COMPRESS_QUALITY = 70
const val DEFAULT_NOTIFICATION_REMOVAL_DELAY = 3000L

@Entity(
    tableName = "img_entity",
    indices = [Index(value = ["img_path"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = CreatePostEntity::class,
        parentColumns = ["cpId"],
        childColumns = ["cp_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val imgId: Long = 0L,
    @ColumnInfo(name = "cp_id") val cpId: Long,
    @ColumnInfo(name = "server_gen_id") val serverGenId: String = "",
    @ColumnInfo(name = "img_path") val imgPath: String,
    @ColumnInfo(name = "img_width") val imgWidth: Int,
    @ColumnInfo(name = "img_height") val imgHeight: Int,
    @ColumnInfo(name = "img_res") val imgRes: String?,
    @ColumnInfo(name = "img_orientation") val imgOrientation: Int,
    @ColumnInfo(name = "img_extension") val imgFormat: String,
    @ColumnInfo(name = "img_uploaded") val imgUploaded: Boolean = false) : Serializable

@Entity(
    tableName = "cp_entity"
)
data class CreatePostEntity(
    @PrimaryKey(autoGenerate = true) val cpId: Int = 0,
    @ColumnInfo(name = "post_id") var postId: String = "",
    var type: String? = PostType.TEXT.postType,
    @ColumnInfo(name = "ui_mode") val uiMode: CreatePostUiMode,
    var text: String? = "",
    val title: String? = "",
    @ColumnInfo(name = "privacy_level") val privacyLevel: String? = PostPrivacy.PUBLIC.name,
    @ColumnInfo(name = "allow_comments") val allowComments: Boolean = true,
    val language: String?,
    val progress: Int = 0,
    val selectedLikeType: String? = null,
    @ColumnInfo(name = "format") val format: Format = Format.LOCAL,
    @ColumnInfo(name = "sub_format") val subFormat: SubFormat = SubFormat.STORY,
    @ColumnInfo(name = "ui_type") var uiType: String = UiType2.NORMAL.name,
    @Embedded(prefix = "user_") var userData: PostSourceAsset? = null,
    @Embedded(prefix = "loc_") var userLocation: PostCurrentPlace? = null,
    @Embedded(prefix = "oemb_") var oemb: OEmbedResponse? = null,
    @Embedded(prefix = "poll_") var poll: PostPollPojo? = null,
    @ColumnInfo(name = "repost_asset") var repostAsset: RepostAsset? = null,
    @ColumnInfo(name = "parent_id") val parentId: String? = "",
    @ColumnInfo(name ="parent_post_id") val parentPostId:String? = "",
    @ColumnInfo(name = "comment_params") val commentParams: HashMap<String, Any> = hashMapOf(),
    @ColumnInfo(name = "comment_deleted") val commentDelete: Boolean? = false, // TODO: @karthik @SD comment deleted locally update the field
    @ColumnInfo(name = "state") var state: PostUploadStatus = PostUploadStatus.CREATE,
    @ColumnInfo(name = "retry_count") var retryCount: Int = 0,
    @ColumnInfo(name = "notification_id") var notificationId: Int = 0,
    @ColumnInfo(name = "is_localcard_shown") var isLocalcardShown: Boolean = false,
    @ColumnInfo(name = "creation_date") val creationDate: Long = System.currentTimeMillis(),
    @Embedded(prefix = "local") val localInfo: LocalInfo? = null,
    @ColumnInfo(name="message") val message: String? = null,
    @ColumnInfo(name="group_joined") val groupJoined: Boolean? = false) :
    Serializable {

    fun buildPojo(assetPojo: PostCreateAsset?): PostCreation {
        var asset: PostCreateAsset? = null
        if (this.poll != null) {
            asset = PostCreateAsset(
                poll = this.poll
            )
        } else {
            asset = assetPojo
        }

        return PostCreation(
            id = postId,
                //  Changes are done to send the proper parentId(item_id) even in the case of
                //  carsouel item repost (other than reply) considering the parentId and the
                //  parentPostId is always same.
            parentId = if(this.uiMode == CreatePostUiMode.REPLY) this.parentId else this.parentPostId,
            commentParams = if (this.commentParams.isEmpty()) null else this.commentParams,
            allowComments = this.allowComments,
            assets = asset,
            language = this.language,
            privacyLevel = this.privacyLevel,
            text = this.text,
            title = this.title,
            type = this.type,
            userLocation = this.userLocation,
            linkDetails = if (this.oemb != null) listOf(this.oemb!!) else null,
            uiMode = this.uiMode
        )
    }
}

data class DeleteCPEntity(
        val parentPostId: String,
        val parentId: String?,
        val mode: CreatePostUiMode
)

@DatabaseView(value = "SELECT * from all_level_cards_view WHERE level = 'LOCAL'",
        viewName = VIEW_LocalPosts)
data class LocalPostView(@Embedded val postEntity: PostEntity)

data class ReplyCount(val creationDate : Long, val parentId: String)

data class CreatePost(
    @Embedded val cpEntity: CreatePostEntity,
    @Relation(entityColumn = "cp_id", parentColumn = "cpId", entity = ImageEntity::class)
    val images: List<ImageEntity> = emptyList()
//    @Relation(entityColumn = "id", parentColumn = "cpId", entity = LocalPostView::class)
//    val postEntities: List<AllLevelCards> = emptyList()
) {

    /**
     * On upload success, we can directly use card returned from server, no need to local-card specific UI
     */
//    fun toCommonAsset() : CommonAsset = postEntities.firstOrNull() ?: toLocalCard()
    fun toPostWithLocalInfo() : PostEntity{
        val userData: UserLoginResponse? = null
        val oEmbedData: OEmbedResponse? = cpEntity.oemb
        val pollAsset = cpEntity.poll
        val thumbnailUrls = mutableListOf<ImageDetail>()
        images.map {
            thumbnailUrls.add(ImageDetail(it.imgPath, it.imgWidth.toFloat(), it.imgHeight.toFloat()))
        }
        return PostEntity(
                        title = cpEntity.text,
                        content = cpEntity.text,
                        id = cpEntity.postId,
                        publishTime = cpEntity.creationDate,
                        location = cpEntity.userLocation?.name,
                        type = cpEntity.uiMode.name,
                        showPublishDate = true,
                        format = Format.LOCAL,
                        subFormat = SubFormat.STORY,
                        uiType = if (cpEntity.poll != null) UiType2.HORIZONTAL_BAR else UiType2.NORMAL,
                        source = cpEntity.userData,
                        thumbnailInfos = if (thumbnailUrls.isNotEmpty()) thumbnailUrls else null,
                        linkAsset = if (oEmbedData != null)
                            LinkAsset(
                                    name = oEmbedData.providerName,
                                    type = oEmbedData.type,
                                    title = oEmbedData.title,
                                    url = oEmbedData.thumbnailUrl,
                                    author = oEmbedData.author,
                                    authorUrl = oEmbedData.authorUrl,
                                    description = oEmbedData.description,
                                    thumbnailWidth = oEmbedData.thumbnailWidth.toString(),
                                    thumbnailHeight = oEmbedData.thunmbnailHeight.toString(),
                                    html = oEmbedData.html
                            ) else null,
                        poll = if (pollAsset != null) {
                            val currentime = System.currentTimeMillis()
                            PollAsset(
                                    pollTitle = pollAsset.pollTitle,
                                    options = pollAsset.options?.map {
                                        PollOptions(it.id!!, it.title!!)
                                    },
                                    responseCount = 0,
                                    interactionUrl = "",
                                    startDate = currentime,
                                    endDate = currentime + (cpEntity.poll?.duration ?: 0),
                                    originalStartDate = currentime
                            )} else null,
                        repostAsset = cpEntity.repostAsset?.withModifiedUiTypeForLocalCard(),
                        localInfo = cpEntity.localInfo?.copy(
                                progress = cpEntity.progress,
                                status = cpEntity.state.name,
                                creationDate = System.currentTimeMillis(),
                                cpId = cpEntity.cpId
                        ) ?:
                        LocalInfo(
                                progress = cpEntity.progress,
                                status = cpEntity.state.name,
                                creationDate = System.currentTimeMillis(),
                                cpId = cpEntity.cpId,
                                nextCardId = cpEntity.localInfo?.nextCardId
                        )
                )
    }

}

data class CreatePostID(val id: Long, val operation:CP_OP) {
    companion object {
        val CP_ID_NOT_FOUND = CreatePostID(-1, CP_OP.NO_OP)
    }
    enum class CP_OP{UPDATE, ADD, DELETE, NO_OP}
}


enum class PostUploadStatus {
    CREATE, UPLOADING, SUCCESS, FAILED
}