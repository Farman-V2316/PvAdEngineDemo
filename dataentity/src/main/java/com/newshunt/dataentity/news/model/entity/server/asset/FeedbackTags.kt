/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity.server.asset

import java.io.Serializable

/**
 *
 * Contains pojos related to showing dislike options
 *
 * @author satosh.dhanymaraju
 */


class L1L2Mapping(
        val l1Key: String,
        val l2Options: List<String> // list of l2keys
) : Serializable

enum class HideType { CROSS, HIDE }

// TODO (satosh.dhanyamraju): remove list, detail and their consumers
data class FeedbackTags(val list: List<L1L2Mapping>?, // card level override of master options
                        val detail: List<String>?, // card level override of master options
                        val hideType: HideType?,
                        val hideIcon: String,
                        val hideText: String,
                        val hideButtonText: String,
                        val hideDescription: String) : Serializable