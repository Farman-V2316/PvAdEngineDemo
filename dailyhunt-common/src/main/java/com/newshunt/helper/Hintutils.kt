package com.newshunt.helper

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.newshunt.common.helper.common.LaunchSearch
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.searchhint.entity.SearchHint
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.searchhint.HintsService

/**
 * Created by karthik.r on 2019-08-26.
 */

private const val STR_DEFAULT = "DEFAULT"
private const val LOG_TAG = "HintServiceEntity"
private val DEFAULT_HINT = SearchHint("", "screen: Global", STR_DEFAULT, STR_DEFAULT)

fun launchGlobalSearch(context: Context,
                       referrer: PageReferrer?,
                       searchPayloadContext: SearchPayloadContext? = null,
                       launchSearch: LaunchSearch = LaunchSearch(DEFAULT_HINT.key, DEFAULT_HINT.displayText, referrer, searchPayloadContext)) {
    CommonNavigator.launchSearch(context, launchSearch, SearchRequestType.NEWS)
}

fun hintFor(pageIdAndType: Pair<String?, String?> = STR_DEFAULT to STR_DEFAULT,
            hintData: List<SearchHint> = emptyList()) : SearchHint {
    val id = pageIdAndType.first ?: STR_DEFAULT
    val type = pageIdAndType.second ?: STR_DEFAULT
    return hintData?.find {
        // look for exact match
        it.pageId == id && it.pageType == type
    } ?: hintData?.find {
        // look for default
        it.pageId == STR_DEFAULT && it.pageType == STR_DEFAULT
    } ?: DEFAULT_HINT // build a default
}

fun AppCompatActivity.setUpSearchbarHint(search: View,
                                         searchLocation: SearchLocation,
                                         referrer: () -> PageReferrer?,
                                         searchPayloadContext: SearchPayloadContext? = null) {
    HintsService.performHintSync(searchLocation).observe(this, Observer {
        val searchHint = if (it?.isNotEmpty() == true) it[0] else DEFAULT_HINT
        search.tag = searchHint.key
        (search as? TextView)?.text = searchHint.displayText
        search.setOnClickListener { v ->
            if (v.tag != null) {
                val launchSearch = LaunchSearch(v.tag.toString(), searchHint.displayText,
                        referrer(), searchPayloadContext)
                launchGlobalSearch(this, referrer(), launchSearch = launchSearch)
            } else {
                Logger.e(LOG_TAG, "launch search failed. tag is null.")
            }
        }
    })
}

fun BaseFragment.setUpSearchbarHint(search: View, searchLocation: SearchLocation, referrer:
() -> PageReferrer?, searchPayloadContext: SearchPayloadContext? = null) {
    HintsService.performHintSync(searchLocation).observe(this, Observer {
        val searchHint = if (it?.isNotEmpty() == true) it[0] else DEFAULT_HINT
        search.tag = searchHint.key
        (search as? TextView)?.text = searchHint.displayText
        search.setOnClickListener { v ->
            if (v.tag != null) {
                val launchSearch = LaunchSearch(v.tag.toString(), searchHint.displayText,
                        referrer(), searchPayloadContext)
                launchGlobalSearch(requireContext(), referrer() ,launchSearch = launchSearch)
            } else {
                Logger.e(LOG_TAG, "launch search failed. tag is null.")
            }
        }
    })
}

