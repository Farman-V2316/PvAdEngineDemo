package com.newshunt.dataentity.notification

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.asset.AudioCommand
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.OptOutMeta

import java.io.Serializable

/**
 * Created by anshul on 24/08/17.
 */

class StickyNavModel<  T : BaseNotificationAsset,  Y : BaseDataStreamAsset> : DeeplinkModel(), Serializable {
    private var baseNotificationAsset: T? = null
    private var baseStreamAsset: Y? = null
    var optReason : OptReason? = null
    var stickyType: String? = null
    var priority : Int = 0
    var trayDisplayTime : Long = 0
    var isUserTriggered: Boolean = false
    var audioPlayAtStart: Boolean = false
    var optOutMeta:OptOutMeta ?= null
    var channelId: String? = null

    override fun getBaseModelType(): BaseModelType? {
        return BaseModelType.STICKY_MODEL
    }

    fun getBaseNotificationAsset(): BaseNotificationAsset? {
        return baseNotificationAsset
    }

    fun setBaseNotificationAsset(
            baseNotificationAsset: T?) {
        this.baseNotificationAsset = baseNotificationAsset
    }

    fun getBaseStreamAsset(): BaseDataStreamAsset? {
        return baseStreamAsset
    }

    fun setBaseStreamAsset(baseStreamAsset: Y?) {
        this.baseStreamAsset = baseStreamAsset
    }

    companion object {

        private const val serialVersionUID = 3311623430172749982L
    }
}

data class AudioInput(val audioUrl : String?, val audioCommand: AudioCommand?) : Serializable

enum class OptReason : Serializable {
    USER, SERVER;

    companion object {
        fun from(optReasonStr : String?) : OptReason {
            OptReason.values().forEach {
                if (CommonUtils.equalsIgnoreCase(optReasonStr, it.name)) return it
            }
            return SERVER
        }
    }
}
