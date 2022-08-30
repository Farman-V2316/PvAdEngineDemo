/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author satosh.dhanymaraju
 */
class CommentSectionDataTest {
    @Test
    fun `returns null if present and new if not present`() {
        assertEquals(null, getCommentSectionFor(arrayListOf<Any>(1, "", CommentSectionPojo())))
        assertTrue(getCommentSectionFor(arrayListOf(1, "")) is CommentSectionPojo)
    }
}