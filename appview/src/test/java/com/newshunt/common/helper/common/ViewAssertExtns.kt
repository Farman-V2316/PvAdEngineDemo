/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common

import android.view.View
import android.widget.TextView
import junit.framework.Assert.assertNotNull
import org.junit.Assert
import org.mockito.Mockito

/**
 * File contains extension functions for commonly used view assertions
 *
 * @author satosh.dhanyamraju
 */

 fun <T : View?> T.assertVisible(): T {
    assertNotNull(this)
    Assert.assertEquals(View.VISIBLE, this!!.visibility)
    return this
}

 fun <T : View?> T.assertGone(): T {
    assertNotNull(this)
    Assert.assertEquals(View.GONE, this!!.visibility)
    return this
}

 infix fun <T : TextView?> T.assertText(txt: String): T {
    assertNotNull(this)
    Assert.assertEquals(txt, this!!.text.toString())
    return this
}

 infix fun <T : TextView?> T.assertText(txtRes: Int): T {
    assertNotNull(this)
    Assert.assertEquals(this!!.resources.getString(txtRes), text.toString())
    return this
}

infix fun <T : View?> T.assertTag(tag: Any?): T {
    assertNotNull(this)
    Assert.assertEquals(tag, this!!.tag)
    return this
}

fun <T> any1(type: Class<T>): T = Mockito.any<T>(type)
