/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.entity

import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * Contains data classes and functions related to displaying comments section as a card.
 *
 * @author satosh.dhanymaraju
 */

class CommentSectionPojo()

/**
 * Returns comments-section-pojo, if not present in input items. Returns null if present
 *
 */
fun getCommentSectionFor(items: MutableList<Any>): Any? {
    if (hasCommentSection(items)) {
        return null
    }
    return CommentSectionPojo()
}

inline fun hasCommentSection(items: MutableList<Any>) = items.any(::isCommentSection)

inline  fun isCommentSection(any: Any) = any is CommentSectionPojo

class CommentSectionViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
