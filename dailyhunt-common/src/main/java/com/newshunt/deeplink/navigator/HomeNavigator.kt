package com.newshunt.deeplink.navigator

import android.content.Intent
import androidx.lifecycle.MutableLiveData

object HomeNavigator {

	val bottomBarLiveData = MutableLiveData<HomeNavigationEntity>()



	@JvmStatic
	fun launchSection(intent: Intent) {
		val entity = HomeNavigationEntity(intent)
		bottomBarLiveData.postValue(entity)
	}

}

data class HomeNavigationEntity(val intent: Intent,
                                val timeStamp: Long = System.currentTimeMillis())