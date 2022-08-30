/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.notification.model.entity

/**
 * @author anshul.jain
 *   A invalid type for handling the cancelling of the notification at the client side.
 */
enum class NotificationInvalidType(val type: String) {
    INVALID_S_TYPE("sType is invalid. "),
    BASE_MODEL_NULL("Base Model is null. "),
    BASE_INFO_NULL("Base Info is null "),
    NOTIFICATION_PARSING_FAILED("Parsing failed for the notification "),
    EMPTY_BUNDLE("The bundle is empty. "),
    INVALID_SECTION_TYPE("The section type is invalid. "),
    INVALID_NOTIFICATION_TYPE("The notification type is invalid"),
    PUSH_ACTION("Action cannot be from PUSH. "),
    PARSING_ACTION_FAILED("Parsing failed"),
    RELEVANCE_LIMIT("Blocked Limit"),
    RELEVANCE_BLOCKED("Blocked Action Id"),
    RELEVANCE_API_LEVEL("Blocked API Level"),
    RELEVANCE_MANUFACTURER("Blocked Manufacturer"),
    RELEVANCE_NOTIFICATION_ENABLED("Blocked Noti Enabled"),
    RELEVANCE_NOTIFICATION_DISABLED("Blocked Noti Disabled"),
    RELEVANCE_CHANNEL_ENABLED("Blocked Channel Enabled"),
    RELEVANCE_CHANNEL_DISABLED("Blocked Channel Disabled"),
    RELEVANCE_DEVICE_LOCKED("Blocked Device Locked"),
    RELEVANCE_DEVICE_UNLOCKED("Blocked Device Unlocked"),
    INVALID_DATA_IN_ROUTING_ACTIVITY("Invalid notification data in notification routing activity"),
    STICKY_NOTIFICATION_REMOVED_FROM_TRAY("Sticky notification was removed from the tray. ")
}