/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.common.view

/**
 * Card can clear bitmaps from views to avoid OOMs.
 *
 * @author raunak.yadav
 */
interface ClearableCard {
    fun recycleView()
}