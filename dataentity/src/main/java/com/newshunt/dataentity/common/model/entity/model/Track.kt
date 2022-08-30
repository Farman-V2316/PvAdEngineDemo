package com.newshunt.dataentity.common.model.entity.model

import java.io.Serializable

data class Track(var url: String? = null,
                 var comscoreUrls: List<String>? = null) : Serializable