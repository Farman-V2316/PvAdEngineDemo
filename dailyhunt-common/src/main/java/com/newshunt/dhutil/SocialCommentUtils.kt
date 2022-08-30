package com.newshunt.dhutil

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_BILLION
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_BILLION_COMMENTS
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_KILO
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_MILLION
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_MILLION_COMMENTS
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_THOUSAND_COMMENTS
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_TRILLION
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.ONE_TRILLION_COMMENTS
import java.util.Collections

fun displayCount(count: String) = if (CommonUtils.isValidInteger(count)) {
	if (count.toLong() == 0L)
		"" // display 0 as empty
	else
		getFormattedCountForLikesAndComments(count = count.toLong())
} else {
	count
}

fun getFormattedCountForLikesAndComments(count: Long) = when {
	count < 0 -> Constants.EMPTY_STRING
	count < ONE_THOUSAND_COMMENTS -> count.toString()
	count < ONE_MILLION_COMMENTS -> formatCount(count, ONE_THOUSAND_COMMENTS, ONE_KILO)
	count < ONE_BILLION_COMMENTS -> formatCount(count, ONE_MILLION_COMMENTS, ONE_MILLION)
	count < ONE_TRILLION_COMMENTS -> formatCount(count, ONE_BILLION_COMMENTS, ONE_BILLION)
	else -> formatCount(count, ONE_TRILLION_COMMENTS, ONE_TRILLION)
}

private inline fun formatCount(count: Long, range: Long, classifier: String): String {
	val quotient = (count / range).toInt()
	val remainderRounded = CommonUtils.roundToNearest(count % range, (range / 10L).toLong(), false)
	return "${quotient + remainderRounded / range.toDouble()}$classifier"
}


fun getSorted(data: MutableList<AllLevelCards>?): List<AllLevelCards>? {
	if (data == null) {
		return data
	}

	data.sortWith(Comparator { o1, o2 ->
		o2.postEntity.i_publishTime()?.compareTo(o1.postEntity.i_publishTime()?: 0 )?: 0
	})

	return data
}
