/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.domain

import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.disposables.Disposable

/**
 * @author Amitkumar
 */
interface NotificationChannelUsecase {

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestNotificationChannelData() : Disposable

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncChangedConfig() : Disposable
}