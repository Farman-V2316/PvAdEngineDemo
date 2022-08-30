package com.newshunt.news.util

import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.EventsInfo

object CommunicationHelper {

  const val LOG_TAG = "CommunicationHelper"

  @JvmStatic
  fun minLaunch(eventsInfo: EventsInfo):Int {
    if (eventsInfo.precondition == null || !eventsInfo.precondition!!.containsKey(if (AppConfig.getInstance()!!.isGoBuild)
          NewsConstants.COMM_MIN_OCCURENCES_GO
        else
          NewsConstants.COMM_MIN_OCCURENCES))
      return -1
    try {
      return Integer.parseInt(eventsInfo.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
        NewsConstants.COMM_MIN_OCCURENCES_GO
      else
        NewsConstants.COMM_MIN_OCCURENCES]!!)
    }
    catch (ex:Exception) {
      Logger.d(LOG_TAG,
          "minLaunch: invalid num: " + (eventsInfo.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
            NewsConstants.COMM_MIN_OCCURENCES_GO
          else
            NewsConstants.COMM_MIN_OCCURENCES] ?: error("")))
    }

    return -1
  }

  @JvmStatic
  fun title(eventsInfo: EventsInfo):String? {
    return if (eventsInfo.activity != null && eventsInfo.activity!!.attributes != null) {
      eventsInfo.activity!!.attributes[NewsConstants.COMM_TITLE]
    } else Constants.EMPTY_STRING
  }

  @JvmStatic
  fun message(eventsInfo: EventsInfo):String? {
    return if (eventsInfo.activity != null && eventsInfo.activity!!.attributes != null) {
      eventsInfo.activity!!.attributes[NewsConstants.COMM_MESSAGE]
    } else Constants.EMPTY_STRING
  }

  @JvmStatic
  fun prefKey(eventsInfo: EventsInfo):String {
    return eventsInfo.event?: Constants.EMPTY_STRING + eventsInfo.id
  }

  @JvmStatic
  fun getDisplayTime(eventsInfo: EventsInfo):Int {
    if (eventsInfo.activity != null && eventsInfo.activity!!.attributes != null)
    {
      val displayTime = eventsInfo.activity!!.attributes[NewsConstants.COMM_DISPLAY_TIME]
      if (!CommonUtils.isEmpty(displayTime))
      {
        try
        {
          return Integer.parseInt(displayTime!!)
        }
        catch (ex:Exception) {
          Logger.d(LOG_TAG,
              ("displayTime: invalid num: " + eventsInfo.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
                NewsConstants.COMM_MIN_OCCURENCES_GO
              else
                NewsConstants.COMM_MIN_OCCURENCES]!!))
        }
      }
    }
    return -1
  }

  @JvmStatic
  fun getGapCount(eventsInfo: EventsInfo):Int {
    if (eventsInfo.precondition == null || !eventsInfo.precondition!!.containsKey(NewsConstants.COMM_GAP_COUNT))
      return NewsConstants.TOOL_TIP_DEFAULT_GAP_COUNT
    try
    {
      return Integer.parseInt(eventsInfo.precondition!![NewsConstants.COMM_GAP_COUNT] ?: error(""))
    }
    catch (ex:Exception) {
      Logger.d(LOG_TAG,
          "gap: invalid num: " + (eventsInfo.precondition!![NewsConstants.COMM_GAP_COUNT] ?: error("")))
    }

    return NewsConstants.TOOL_TIP_DEFAULT_GAP_COUNT
  }

}