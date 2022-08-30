/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.language

import java.util.ArrayList

/**
 * Event fired from conversion callbacks via Appsflyer/Firebase when languages are received from
 * campaign params.
 *
 * Created by srikanth.ramaswamy on 08/16/18.
 */
class CampaignLanguageEvent(languages: List<String>) {
    val predefinedLanguages = ArrayList<String>()

    init {
        predefinedLanguages.addAll(languages)
    }
}
