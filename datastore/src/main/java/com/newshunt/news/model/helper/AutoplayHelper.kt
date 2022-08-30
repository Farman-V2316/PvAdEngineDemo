/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.helper

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AutoplayPlayerType
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUnifiedWebPlayer
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.util.*

object AutoplayHelper {

	fun getAutoplayPlayerTypes(): List<AutoplayPlayerType> {
		val autoplayPlayerTypes = ArrayList<AutoplayPlayerType>()
		autoplayPlayerTypes.add(AutoplayPlayerType(PlayerType.M3U8.getName(), true, null))
		autoplayPlayerTypes.add(AutoplayPlayerType(PlayerType.MP4.getName(), true, null))

		val playerInfo = PreferenceManager.getPreference(AppStatePreference.PLAYERS_INFO, Constants.EMPTY_STRING)
		if (!CommonUtils.isEmpty(playerInfo)) {
			val players = JsonUtils.fromJson<List<PlayerUnifiedWebPlayer>>(playerInfo,
				object : TypeToken<List<PlayerUnifiedWebPlayer>>() {

				}.type)

			if (!CommonUtils.isEmpty(players)) {
				val webSources = ArrayList<String>()
				for (unifiedWebPlayer in players!!) {
					val support = unifiedWebPlayer.androidSupport
					if (support != null && support.isAutoplaySupported &&
							unifiedWebPlayer.sourceKey != null) {
						if (AndroidUtils.isAppVersionGreaterthanOrEqualto(AppConfig.getInstance()!!.appVersion,
								support.minVersion) && (CommonUtils.isEmpty(support.excludeVersion) || !support.excludeVersion!!.contains(AppConfig.getInstance().appVersion))) {
							webSources.add(unifiedWebPlayer.sourceKey)
						}
					}
				}

				if (!CommonUtils.isEmpty(webSources)) {
					autoplayPlayerTypes.add(
						AutoplayPlayerType(PlayerType.DH_EMBED_WEBPLAYER.getName(), false, webSources))
				}
			}
		}
		return autoplayPlayerTypes
	}
}