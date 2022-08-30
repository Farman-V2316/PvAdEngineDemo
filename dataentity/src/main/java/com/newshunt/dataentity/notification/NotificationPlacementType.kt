package com.newshunt.dataentity.notification

enum class NotificationPlacementType {
    TRAY_ONLY,
    INBOX_ONLY,
    TRAY_AND_INBOX
}

enum class NotificationCtaTypes {
    FOLLOW,
    REPLY,
    SHARE,
    REPOST,
    COMMENT,
    JOIN;


    companion object {
        @JvmStatic
        open fun fromName(name: String?): NotificationCtaTypes? {
            for (notCtaType in values()) {
                if (notCtaType.name.equals(name, ignoreCase = true)) {
                    return notCtaType
                }
            }
            return null
        }
    }
}
