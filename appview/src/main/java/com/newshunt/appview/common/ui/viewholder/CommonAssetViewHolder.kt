/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.view.View
import com.newshunt.dataentity.common.asset.CommonAsset

/**
 * To implemented by cardviewholder
 * @author satosh.dhanyamraju
 */
interface CommonAssetViewHolder {
    fun curData(): Pair<View, CommonAsset?>
}