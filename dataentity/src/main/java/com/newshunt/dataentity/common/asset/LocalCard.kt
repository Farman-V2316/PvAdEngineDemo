/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import java.io.Serializable

/**
 * For view to render extra progressbar and disable some actions
 * @author satosh.dhanyamraju
 */
data class LocalCard(val postAsset: PostEntity,
                     val progress: Int?,
                     val status: String?,
                     val nextCardId: String?) : Serializable, CommonAsset by postAsset {
    override fun i_progress(): Int? = progress
    override fun i_status(): String? = status
}


/**
 * Any change in structure need changes to [FetchDao.localInfo] query and [ListAllLevelCards]
 * @author satosh.dhanyamraju
 */
data class LocalInfo(
        val progress: Int? = null,
        val status: String? = null,
        val pageId: String? = null,
        val location: String? = null,
        val section: String? = null,
        val shownInForyou: Boolean? = null,
        val creationDate : Long? = null,
        val cpId : Int? = null,
        val nextCardId: String? = null,
        val fetchedFromServer : Boolean? = null,
        val isCreatedFromMyPosts : Boolean? = null,
        val isCreatedFromOpenGroup : Boolean? = null) : Serializable