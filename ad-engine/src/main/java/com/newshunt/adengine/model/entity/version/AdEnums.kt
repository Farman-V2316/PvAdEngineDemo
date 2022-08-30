package com.newshunt.adengine.model.entity.version

import com.google.gson.annotations.SerializedName
import java.io.Serializable

enum class ShareIconPosition(val positionName: String) : Serializable {
    @SerializedName("fixed")
    FIXED("fixed"),
    @SerializedName("floating")
    FLOATING("floating");

    companion object {
        @JvmStatic
        fun fromName(name: String?): ShareIconPosition {
            for (position in ShareIconPosition.values()) {
                if (position.positionName == name) {
                    return position
                }
            }
            return FIXED
        }
    }
}

enum class AdClubType(val type: String) : Serializable {
    @SerializedName("fallback")
    FALLBACK("fallback"),
    @SerializedName("sequence")
    SEQUENCE("sequence");

    companion object {
        fun fromName(name: String?): AdClubType {
            for (clubType in AdClubType.values()) {
                if (clubType.type == name) {
                    return clubType
                }
            }
            return FALLBACK
        }
    }
}

/**
 * DisplayType for Ad that can cover full screen.
 */
enum class AdUIType {
    FULL_SCREEN,
    MINI_SCREEN
}

/**
 * For zone like splash-exit, we have an option to exit the app.
 * For others, we can simply continue using the app.
 */
enum class AdLPBackAction {
    BACK_TO_APP, EXIT_APP
}