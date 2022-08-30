package com.newshunt.dhutil

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.util.R
import com.newshunt.dataentity.common.helper.common.CommonUtils

object UiUtils {

	@JvmStatic
	fun getStringForEntityText(numOfFollowers: String?, numOfStories: String?): String? {
		val followers = numOfFollowers.nullIfEmptyOrZero()?.plus(Constants.SPACE_STRING)?.plus(CommonUtils.getString(R.string.followers))
		val stories = numOfStories.nullIfEmptyOrZero()?.plus(Constants.SPACE_STRING)?.plus((CommonUtils.getString(R.string.stories)))
		return if (followers != null && stories != null) "$followers · $stories" else followers
			?: stories
	}
}