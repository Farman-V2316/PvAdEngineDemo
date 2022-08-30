/*
 * Created by Rahul Ravindran at 31/5/19 5:57 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.app.helper

import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer

interface DHGameLauncherUseCase {
    fun launch(referrer: PageReferrer) {}
    fun launchWithIntent(deeplinkUrl: String?, pageReferrer: PageReferrer? = null): Intent?
}

object DHGameProvider : DHGameLauncherUseCase {
    private var implementer: DHGameLauncherUseCase? = null

    fun setProvider(provider: DHGameLauncherUseCase) {
        this.implementer = provider
    }


    override fun launch(referrer: PageReferrer) {
        this.implementer?.launch(referrer)
    }

    override fun launchWithIntent(deeplinkUrl: String?, pageReferrer: PageReferrer?): Intent? {
        return this.implementer?.launchWithIntent(deeplinkUrl, pageReferrer)
    }
}