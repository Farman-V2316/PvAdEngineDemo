package com.newshunt.appview.common.postcreation.analytics.entity

enum class CreatePostActionType(val value: String) {
    NONE("none"),
    IMAGE_ATTACH("image_attach"),
    POLL_CREATION("poll_creation"),
    LOCATION_ATTACHED("location_attached"),
    HANDLE_COMPOSE("handle_compose"),
    HASHTAG_COMPOSE("hashtag_compose"),
    POST_PRIVACY("post_privacy")
}

enum class CreatePostImageAttachmentType(val value: String) {
    NONE("none"),
    CAMERA("camera"),
    GALLERY("gallery")
}

enum class CreatePostPublishStatus(val value: String){
    SUCCESS("success"),
    FAILURE("failure")
}

enum class CreatePostHomeEntranceActionType(val value: String){
    CREATE_POST_ICON("create_post_icon"),
    REPOST_CLICK("repost_click"),
    COMMENTS_CLICK("comments_click"),
    REPLY_CLICK("reply_click"),
    NONE("none")
}