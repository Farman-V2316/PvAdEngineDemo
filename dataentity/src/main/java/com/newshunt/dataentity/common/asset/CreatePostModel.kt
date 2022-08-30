package com.newshunt.dataentity.common.asset

import com.google.gson.annotations.SerializedName
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.JsPostActionParam
import java.io.Serializable

data class ImageUpload(val code: Int, val paths: ArrayList<String>?)

data class PostCreation(
        val id: String? = "",
        val type: String? = PostType.TEXT.postType,
        val text: String? = null,
        val title: String? = null,
        @Transient val uiMode: CreatePostUiMode = CreatePostUiMode.POST,
        val userLocation: PostCurrentPlace? = null,
        val privacyLevel: String? = PostPrivacy.PUBLIC.name,
        val allowComments: Boolean = true,
        val language: String? = "en",
        val parentId: String? = null,
        val assets: PostCreateAsset? = null,
        val linkDetails: List<OEmbedResponse>? = null,
        val commentParams: HashMap<String, Any>? = null
) : Serializable

data class PostCreateAsset(
        val images: List<PostImage>? = null,
        val videos: List<PostVideo>? = null, val poll: PostPollPojo? = null
) : Serializable

data class PostImage(val id: String?,
                     val width: Int,
                     val height: Int,
                     val format: String?,
                     val orientation: Int,
                     val resolution: String?) : Serializable
data class PostVideo(
    val id: String?, val width: Int, val height: Int,
    val durationInMilliseconds: Int, val orientation: String?, val format: String?
) : Serializable

data class PostPollPojo(
    val pollTitle: String? = null,
    val duration: Long? = null,
    val options: List<PostPollOption>? = null
) : Serializable

data class PostPollOption(
    var id: String? = Constants.EMPTY_STRING,
    var title: String? = Constants.EMPTY_STRING
//    var pollPosition: Int = -1
) : Serializable

data class PollDuration(
    var displayString: String? = "",
    var duration: Long? = -1
) : Serializable

data class OEmbedResponse(
    val type: String? = "TEXT",
    val title: String? = "",
    val url: String? = "",
    val author: String? = "",
    @SerializedName("author_url")
    val authorUrl: String? = "",
    @SerializedName("provider_name")
    val providerName: String? = "",
    val description: String? = "",
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = "",
    @SerializedName("thumbnail_width")
    val thumbnailWidth: Int,
    @SerializedName("thunmbnail_height")
    val thunmbnailHeight: Int,
    val html: String? = ""
) : Serializable

data class PostCurrentPlace(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val distance: Int? = -1,
    var isUserSelected: Boolean = false,
    var isAutoLocation: Boolean = false
) : Serializable

enum class PostType(val postType: String) {
    TEXT("TEXT"),
    POLL("POLL")
}

// ALL is used for filter
enum class CreatePostUiMode { COMMENT, REPOST, POST, REPLY, ALL}

fun CreatePostUiMode.isCommentOrRepost() = this == CreatePostUiMode.COMMENT || this == CreatePostUiMode.REPOST

enum class RepostDisplayType(val index: Int){
    REPOST_HERO(100),
    REPOST_NORMAL(101), // For only Text or Text + Image(S_W_IMAGES + S_W_PHOTOGALLERY)
    REPOST_POLL(102), // For poll
    REPOST_OEMBED(103), // For OEmbed
}

