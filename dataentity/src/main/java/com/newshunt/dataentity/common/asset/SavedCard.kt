/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import androidx.room.Embedded
import com.newshunt.dataentity.social.entity.AllLevelCards
import java.io.Serializable

/**
 * For saved-carousels
 * @author satosh.dhanyamraju
 */
data class SavedCard (
        @Embedded val card: AllLevelCards,
        val count_story_value : String
) : CommonAsset by card, Serializable