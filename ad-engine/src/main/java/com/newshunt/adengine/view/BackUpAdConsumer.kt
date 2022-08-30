/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view

/**
 * Any viewholder that is prone to ad failure can register
 * for receiving existing backup ads.
 *
 * @author raunak.yadav
 */
interface BackUpAdConsumer {
    fun onBackupAdFetched(success: Boolean)
}

