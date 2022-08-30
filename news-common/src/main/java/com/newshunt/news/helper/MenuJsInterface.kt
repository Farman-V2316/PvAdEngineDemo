package com.newshunt.news.helper

interface MenuJsInterface {
    fun onMenuButtonClick(postJson: String,
                          locationId: String,
                          menuLocation: String,
                          entityId: String)

    fun canShowMenuButton(): Boolean
    fun isStoryRead(postsJson: String): String

    fun isStoryDisliked(postsJson: String): String
}