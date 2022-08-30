/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.helper.font

import android.os.Build
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import main.java.com.newshunt.fontengine16bit.FontEngine
import main.java.com.newshunt.fontengine16bit.FontEngineOutput

/**
 * FontEngine wrapper. Should be the only class directly using FontEngine library.
 * To be used by [FontHelper]
 *
 * @author satosh.dhanyamraju
 */
internal object FEWrapper {
    private val LOG_TAG: String = "FEWrapper"
    @JvmStatic
    fun convertToFontIndices(data: String): FEOutput {
        val output = FEOutput(data, false)
        if (!dhFontEnabled()) return output
        return kotlin.runCatching {
            FontEngine.ConvertFromUnicodeToFontIndices(data)?.toFEOutput() ?: output
        }.getOrDefault(output)
    }

    @JvmStatic
    fun dhFontEnabled(): Boolean {
        val stringSet = PreferenceManager.getStringSet(GenericAppStatePreference.ENABLE_DH_FOR_MANUFACTURERS.name, HashSet<String>())
        val enable = stringSet.contains("All")
                || stringSet.any { it.equals(Build.MANUFACTURER, true) }
        Logger.d(LOG_TAG, "enableDhFont: $enable")
        return enable
    }

    private fun FontEngineOutput.toFEOutput() =
            FEOutput(fontIndicesString, isSupportedLanguageFound)
}