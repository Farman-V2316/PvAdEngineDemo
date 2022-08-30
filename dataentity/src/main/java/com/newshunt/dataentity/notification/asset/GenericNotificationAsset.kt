package com.newshunt.dataentity.notification.asset

import com.google.gson.annotations.SerializedName
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.SnackMeta
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow
import com.newshunt.dataentity.notification.AudioInput
import com.newshunt.dataentity.notification.util.NotificationConstants
import java.io.Serializable

const val STICKY_NAV_MODEL_TYPE_FIELD = "type"

data class GenericNotificationAsset(
        val template: String,
        val rt: Int,
        val startDate: Long,
        val et: Long,
        val f: Int,
        val v: String,
        val values: GenericNotificationValue) : BaseNotificationAsset(), Serializable {
    override fun getStartTime(): Long {
        return startDate
    }

    override fun getExpiryTime(): Long {
        return et
    }

    override fun getAutoRefreshInterval(): Int {
        return rt
    }

    public fun isLoggingNotificationEventsDisabled(): Boolean{
        return (f != null) && (f >= 128) && ((f and 128) > 0)
    }
}

data class GenericDataStreamAsset(
        @SerializedName("rt") val autoRefreshInterval: Int = 15,
        @SerializedName("f") val features: Int = 2,
        @SerializedName("v") val version: Long = 0,
        @SerializedName("et") var expiryTime: Long = 0,
        val values: GenericNotificationValue) : BaseDataStreamAsset(), Serializable {

    fun isExpired(): Boolean {
        return features and 1 > 0
    }

    fun isLive(): Boolean {
        return features and 2 > 0
    }

    fun isFinished(): Boolean {
        return features and 4 > 0
    }

    override fun getScheduledExpiryTime(): Long {
        return expiryTime
    }

    fun isLoggingNotificationEventsDisabled(): Boolean{
        return (features != null) && (features >= 128) && ((features and 128) > 0)
    }
}

data class GenericNotificationValue(@SerializedName("t") val title: String,
                                    @SerializedName("s") val status: String,
                                    val header: GenericEntity,
                                    val rows: Int,
                                    val entities: List<GenericEntity>) : Serializable

data class GenericEntity(@SerializedName("n") val name: String,
                         @SerializedName("ic") val icon: String,
                         @SerializedName("d") val collapsedData: String,
                         @SerializedName("r1") val row1Data: String,
                         @SerializedName("r2") val row2Data: String,
                         @SerializedName("r3") val row3Data: String) : Serializable

data class OptInEntity(val id: String,
                       val metaUrl: String,
                       val type: String,
                       val priority: Int,
                       val startTime: Long,
                       val expiryTime: Long,
                       val channel: String,
                       val audioInput: AudioInput? = null,
                       val langfilter: String = Constants.EMPTY_STRING,
                       val channelId: String = Constants.EMPTY_STRING,
                       val forceOptIn: Boolean = false) {
    override fun hashCode(): Int {
         return (id + type).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val anotherEntity = other as? OptInEntity ?: return false
        return CommonUtils.equals(id, anotherEntity.id) && CommonUtils.equals(type, anotherEntity.type)
    }
}

data class OptOutEntity(val id: String,
                        val type: String) {
    override fun toString(): String {
        return "OptOutEntity : id :$id and type: $type"
    }
}

enum class AudioCommand(private val command: String) : Serializable{
    PLAY("PLAY"), STOP("STOP");

    companion object {

        @JvmStatic
        fun from(command : String?) : AudioCommand {
            command?.let {
                values().forEach {
                    if (CommonUtils.equals(command, it.command)) {
                        return it
                    }
                }
            }
            return STOP
        }
    }
}

enum class CommentaryState(val jsState : String) : Serializable {
    PLAYING("PLAYING"),//State which says user opted in and audio is playing
    BUFFERING("BUFFERING"),//State in which audio player is buffering to play
    STOPPED("STOPPED"),//State which says user opted in but has stopped the audio
    NOT_OPTED_IN("NOT_OPTED_IN")//State which says user not opted in for audio yet, but audio
// option exists for that notification
// null - which means audio force stopped by Backend or audio option does not exist for
// that sticky notification or used has opted out of the notification itself
}

data class OptOutMeta( val deeplink:String ?= null,
                       val snackMeta: SnackMeta ?= null) : Serializable

/**
 * OptInEntity for News sticky served via versioned API and notification flow
 */
data class NewsStickyOptInEntity(val id: String = NotificationConstants.NEWS_STICKY_OPTIN_ID,
                                 val channel: String? = Constants.EMPTY_STRING,
                                 val metaUrl: String? = Constants.EMPTY_STRING,
                                 val startTime: Long = 0L,
                                 val expiryTime: Long = 0L,
                                 val type: String = NotificationConstants.STICKY_NEWS_TYPE,
                                 val priority: Int = 0,
                                 val channelId: String = Constants.EMPTY_STRING,
                                 val refreshInterval: Long = 30L,
                                 var timeWindows: List<TimeWindow>? = null,
                                 val forceShow: Boolean = false,
                                 val languages: List<String>? = null)

const val NEWS_STICKY_QUERY_PARAM_ID = "id"
const val NEWS_STICKY_TIME_WINDOW_MACRO = "##YYYYMMDD##"



