/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.google.gson.reflect.TypeToken
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.client.AttributeFilter
import com.newshunt.analytics.entity.ClientType
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.analytics.entity.NhAnalyticsDevEvent
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam
import com.newshunt.analytics.entity.NhAnalyticsPVType
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.share.ShareHelper
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.helper.share.ShareUtils
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.DynamicEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.analytics.SessionInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEventParam
import com.newshunt.dataentity.news.analytics.StorySupplementSectionPosition
import com.newshunt.dataentity.news.model.entity.server.asset.AssetType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLandingType
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dataentity.notification.NotificationCtaTypes
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.helper.NewsDetailTimespentHelper
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.NonLinearStore
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.listener.CardEventsCallback
import com.newshunt.socialfeatures.helper.analytics.NHAnalyticsSocialCommentsEventParam
import com.newshunt.socialfeatures.helper.analytics.NHSocialAnalyticsEvent
import com.newshunt.socialfeatures.util.SocialFeaturesConstants

/**
 * Created by karthik.r on 2019-11-11.
 */
object AnalyticsHelper2 {

    private const val WIDGET_DISPLAY_TYPE_LIST = "LIST"
    private const val WIDGET_TYPE_COMMENTS = "COMMENTS"
    private const val WIDGET_TYPE_COMMENT = "COMMENT"

    private var timeSpentParams = HashMap<String, Any>()
    private val workerHandler: Handler

    private fun logRunnable(runnable: () -> Unit) {
        workerHandler.post(runnable)
    }

    init {
        val handlerThread = HandlerThread("AnalyticsClient2")
        handlerThread.start()
        workerHandler = Handler(handlerThread.looper)
    }

    fun logStoryListViewEvent(newsPageEntity: PageEntity?,
                              referrer: PageReferrer?, tabIndex: Int,
                              pageNumber: String, isFromCache: Boolean,
                              storyCount: Int, latestPageNumber: String,
                              eventSection: NhAnalyticsEventSection, referrerRaw: String?,
                              eventMap: HashMap<NhAnalyticsEventParam, Any?>?,
                              referrerProviderlistener: ReferrerProviderlistener? = null,
                              referrerFlow: PageReferrer?,
                              timeTakenForNetwork: Long) {
        logRunnable {
            val eventParams: HashMap<NhAnalyticsEventParam, Any?> = eventMap ?: HashMap()
            eventParams[NhAnalyticsNewsEventParam.CARD_COUNT] = storyCount
            eventParams[NhAnalyticsNewsEventParam.LATEST_PAGENUMBER] = latestPageNumber
            if (!CommonUtils.isEmpty(referrerRaw)) {
                appendReferrerRaw(eventParams, referrerRaw!!)
            }

            if (newsPageEntity != null) {
                addNewsPageEntityParams(eventParams, newsPageEntity)
            }


            val operatorName = DeviceInfoHelper.getOperatorName(CommonUtils.getApplication())
            if (!DataUtil.isEmpty(operatorName)) {
                eventParams[AnalyticsParam.NETWORK_SERVICE_PROVIDER] = operatorName
            }

            if (referrerFlow != null && referrerFlow.referrer != null) {
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                eventParams[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
            }

            eventParams[NhAnalyticsNewsEventParam.PAGE_NUMBER] = pageNumber
            eventParams[NhAnalyticsNewsEventParam.TABINDEX] = tabIndex
            eventParams[NhAnalyticsNewsEventParam.TABNAME] = newsPageEntity?.name
            // need to add this param only when true
            if (isFromCache) {
                eventParams[AnalyticsParam.IS_FROM_CACHE] = true
            }
            if (timeTakenForNetwork > 0) {
                eventParams[AnalyticsParam.TIME_TAKEN_FOR_NETWORK_OPERATION] = timeTakenForNetwork
            }
            SearchAnalyticsHelper.addSearchParams(eventSection, eventParams)
            referrerProviderlistener?.extraAnalyticsParams?.let {
                eventParams.putAll(it)
            }
            /* When switching news-buzz sections, referrer should be HASHTAG and not NEWS. bug30783 */
            val referrer1 =
                    if (referrer?.referrer is RunTimeReferrer
                            && referrerProviderlistener?.providedReferrer?.referrer is RunTimeReferrer
                            && pageNumber == Constants.COUNT_ONE_STRING)
                        referrerProviderlistener.latestPageReferrer
                    else referrer
            AnalyticsClient.log(NhAnalyticsNewsEvent.STORY_LIST_VIEW, eventSection, eventParams, referrer1)
        }
    }


    fun logStoryCardViewEvent(item: CommonAsset,
                              referrer: PageReferrer?,
                              itemPosition: Int,
                              uiTypeDisplayed: String,
                              cardEventsCallback: CardEventsCallback?,
                              referrerProviderlistener: ReferrerProviderlistener?,
                              postMulti: Boolean = false, isPerspective: Boolean = false,
                              parentStory: CommonAsset? = null) {

        logRunnable {
            if (cardEventsCallback == null ||
                    !cardEventsCallback.isAnalyticsEventsDisabled(itemPosition)) {
                val pageReferrer = PageReferrer(referrer)
                val eventParams : MutableMap<NhAnalyticsEventParam, Any?> = HashMap()
                if (!CommonUtils.isEmpty(uiTypeDisplayed)) {
                    eventParams[AnalyticsParam.UI_TYPE] = uiTypeDisplayed
                }

                SearchAnalyticsHelper.addSearchParams(getReferrerEventSectionFrom(referrerProviderlistener), eventParams)
                fillUpBasicBaseAssetData(item, eventParams, itemPosition)
                fillUpBaseAsssetData(item, eventParams)
                fillUpCardViewParams(item, eventParams)
                eventParams[NhAnalyticsAppEventParam.APP_ID] = Constants.APP_ID
                eventParams[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item.i_source()?.id
                SearchAnalyticsHelper.addSearchParams(getReferrerEventSectionFrom(referrerProviderlistener), eventParams)

                if (referrerProviderlistener != null && !CommonUtils.isEmpty(referrerProviderlistener.extraAnalyticsParams)) {
                    eventParams.putAll(referrerProviderlistener.extraAnalyticsParams!!)
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }
                if (isPerspective) {
                    eventParams.put(AnalyticsParam.COLLECTION_NAME, Constants.MORE_STORIES_COLLECTION_NAME)
                    eventParams.put(AnalyticsParam.COLLECTION_ID, parentStory?.i_id())
                }
                CardSeenStatusRepo.DEFAULT.markSeen(item.i_id())
                if (postMulti) {
                    AnalyticsClient._logMultiDynamic(NhAnalyticsAppEvent.STORY_CARD_VIEW,
                            getReferrerEventSectionFrom(referrerProviderlistener),
                            eventParams, dynamicMap, pageReferrer)
                } else {
                    AnalyticsClient._logDynamic(NhAnalyticsAppEvent.STORY_CARD_VIEW,
                            getReferrerEventSectionFrom(referrerProviderlistener),
                            eventParams, dynamicMap, null, pageReferrer, false)
                }
            }
        }
    }

    fun logStoryPageTimeSpentViewEvent(item: CommonAsset,
                                       referrerFlow: PageReferrer?,
                                       referrerLead: PageReferrer?,
                                       referrer: PageReferrer?,
                                       referrerRaw: String?,
                                       mapParam: HashMap<NhAnalyticsEventParam, Any?>?,
                                       section: NhAnalyticsEventSection,
                                       timeSpentEventId: Long = -1) {
        if (timeSpentEventId != -1L) {
            logRunnable {
                val map: HashMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                // add lead and flow here
                if (referrerLead != null && referrerLead.referrer != null) {
                    map[AnalyticsParam.REFERRER_LEAD] = referrerLead.referrer.referrerName
                    map[AnalyticsParam.REFERRER_LEAD_ID] = referrerLead.id
                }
                if (referrerFlow != null && referrerFlow.referrer != null) {
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                    map[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
                }
                if (referrerRaw != null) {
                    appendReferrerRaw(map, referrerRaw)
                }

                map[AnalyticsParam.ITEM_ID] = item.i_id()
                map[AnalyticsParam.ITEM_PUBLISHER_ID] = item.i_source()?.id
                map[AnalyticsParam.ASSET_TYPE] = item.i_type()
                map[NhAnalyticsNewsEventParam.PV_ACTIVITY] = NhAnalyticsPVType.STORY_DETAIL.name
                        .toLowerCase()
                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item.i_source()?.id
                fillStoryPageViewParams(item, map)
                AnalyticsClient.addStateParamsAndPermanentParams(map)
                AnalyticsClient.addConnectionParams(map)
                SearchAnalyticsHelper.addSearchParams(section, map)

                if (referrer != null) {
                    NhAnalyticsAppState.addReferrerParams(referrer, map)
                }

                val stringParams = AttributeFilter.filterForNH(map)
                stringParams.putAll(NhAnalyticsAppState.getInstance().globalExperimentParams)
                val experiments = item.i_experiments()
                if (experiments != null) {
                    stringParams.putAll(experiments)
                }

                stringParams.putAll(NhAnalyticsAppState.getInstance().globalExperimentParams)
                stringParams[NhAnalyticsAppEventParam.REFERRER.getName()] = referrer?.referrer
                stringParams[NhAnalyticsAppEventParam.REFERRER_ID.getName()] = referrer?.id
                stringParams[NhAnalyticsAppEventParam.SUB_REFERRER_ID.getName()] = referrer?.subId
                stringParams[NhAnalyticsAppEventParam.REFERRER_ACTION.getName()] = referrer?.referrerAction
                stringParams[NewsDetailTimespentHelper.HASHTAG_SEEN] = Constants.NO
                stringParams[NewsDetailTimespentHelper.PERSPECTIVE_SEEN] = Constants.NO
                if (item.i_format() == Format.VIDEO) {
                    timeSpentParams = stringParams as HashMap<String, Any>
                }

                NewsDetailTimespentHelper.getInstance().postCreateTimespentEvent(timeSpentEventId, stringParams)
                val wordCount = item.i_wordCount() ?: 0
                val imageCount = item.i_imageCount() ?: 0
                val pvActivity = if (item.i_type() == AssetType2.COMMENT.name)
                    NhAnalyticsPVType.COMMENT_DETAIL.name.toLowerCase() else
                    NhAnalyticsPVType.STORY_DETAIL.name.toLowerCase()

                logTimespentEvent(timeSpentEventId, NhAnalyticsNewsEventParam.PV_ACTIVITY.getName(),
                        pvActivity)
                logTimespentEvent(timeSpentEventId, AnalyticsParam.IMAGECOUNT.getName(),
                        Integer.toString(imageCount))
                logTimespentEvent(timeSpentEventId, NhAnalyticsNewsEventParam.WORDCOUNT.getName(),
                        Integer.toString(wordCount))
                logTimespentEvent(timeSpentEventId, NewsConstants.DH_SECTION, section.name)
            }
        }
    }

    fun logCommentsWidgetViewEvent(eventSection: NhAnalyticsEventSection?,
                                   mapParam: MutableMap<NhAnalyticsEventParam, Any>?,
                                   dynamicMap: Map<String, String>,
                                   referrer: PageReferrer?,
                                   position: StorySupplementSectionPosition?, isfullPage: Boolean) {
        var map = mapParam
        if (referrer == null || eventSection == null) {
            return
        }

        if (map == null) {
            map = java.util.HashMap()
        }

        map[NhAnalyticsAppEventParam.REFERRER] = referrer.referrer
        map[NhAnalyticsAppEventParam.REFERRER_ID] = referrer.id
        map[NhAnalyticsAppEventParam.SUB_REFERRER_ID] = referrer.subId
        map[NhAnalyticsAppEventParam.REFERRER_ACTION] = referrer.referrerAction
        map[NhAnalyticsNewsEventParam.WIDGET_TYPE] = if (isfullPage)
            WIDGET_TYPE_COMMENT
        else
            WIDGET_TYPE_COMMENTS
        map[NhAnalyticsNewsEventParam.WIDGET_DISPLAY_TYPE] = WIDGET_DISPLAY_TYPE_LIST
        if (position != null) {
            map[NhAnalyticsNewsEventParam.WIDGET_PLACEMENT] = position.getName()
        }

        AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.CARD_WIDGET_VIEW, eventSection, map, dynamicMap,
                referrer,
                false)
    }

    fun logCollectionViewEvent(item: CommonAsset,
                              referrerFlow: PageReferrer?,
                              referrerLead: PageReferrer?,
                              referrer: PageReferrer?,
                              referrerRaw: String?,
                              mapParam: HashMap<NhAnalyticsEventParam, Any?>?,
                              section: NhAnalyticsEventSection) {
        logRunnable {
            val map: HashMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()

            // add lead and flow here
            if (referrerLead != null && referrerLead.referrer != null) {
                map[AnalyticsParam.REFERRER_LEAD] = referrerLead.referrer.referrerName
                map[AnalyticsParam.REFERRER_LEAD_ID] = referrerLead.id
            }
            if (referrerFlow != null && referrerFlow.referrer != null) {
                map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                map[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
            }
            if (referrerRaw != null) {
                //map[AnalyticsParam.REFERRER_RAW] = referrerRaw
                appendReferrerRaw(map, referrerRaw)
            }

            fillCollectionViewParams(item, map)


            AnalyticsClient._logDynamic(NhAnalyticsNewsEvent.COLLECTION_PREVIEW_VIEW, section,
                    map, item.i_experiments(), null, referrer, false)
        }
    }


    private fun fillCollectionViewParams(item: CommonAsset, map: MutableMap<NhAnalyticsEventParam,
            Any?>) {

        map.put(AnalyticsParam.ITEM_COUNT, item.i_collectionItems()?.size)
        map.put(AnalyticsParam.GROUP_TYPE, Constants.GROUP_TYPE_COLLECTION)
        map.put(AnalyticsParam.GROUP_ID, item.i_id())
        map.put(AnalyticsParam.ASSET_TYPE, item.i_type())
        map.put(AnalyticsParam.CONTENT_TYPE, item.i_contentType())
        map.put(AnalyticsParam.UI_TYPE, item.i_uiType())
        map.put(AnalyticsParam.WIDGET_LANGUAGE, item.i_langCode())
    }


    fun logCollectionPlayEvent(item: CommonAsset,
                               referrerFlow: PageReferrer?,
                               referrerLead: PageReferrer?,
                               referrer: PageReferrer?,
                               referrerRaw: String?,
                               mapParam: HashMap<NhAnalyticsEventParam, Any?>?,
                               section: NhAnalyticsEventSection) {
        logRunnable {
            val map: HashMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()

            // add lead and flow here
            if (referrerLead != null && referrerLead.referrer != null) {
                map[AnalyticsParam.REFERRER_LEAD] = referrerLead.referrer.referrerName
                map[AnalyticsParam.REFERRER_LEAD_ID] = referrerLead.id
            }
            if (referrerFlow != null && referrerFlow.referrer != null) {
                map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                map[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
            }
            if (referrerRaw != null) {
                //map[AnalyticsParam.REFERRER_RAW] = referrerRaw
                appendReferrerRaw(map, referrerRaw)
            }

            fillCollectionViewParams(item, map)


            AnalyticsClient._logDynamic(NhAnalyticsNewsEvent.COLLECTION_PREVIEW_CLICK, section,
                    map, item.i_experiments(), null, referrer, false)
        }
    }

    fun logTimespentEvent(timeSpentEventId: Long = -1, paramName: String, paramValue: String) {
        logRunnable {
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    paramName, paramValue)
        }
    }

    fun logStoryCardClickEvent(item: CommonAsset,
                               referrer: PageReferrer?,
                               itemPosition: Int,
                               cardEventsCallback: CardEventsCallback?,
                               referrerProviderListener: ReferrerProviderlistener?,
                               postMulti: Boolean = false, isPerspective: Boolean = false,
                               parentStory: CommonAsset? = null
    ) {
        logRunnable {
            if (cardEventsCallback == null ||
                    !cardEventsCallback.isAnalyticsEventsDisabled(itemPosition)) {

                val pageReferrer = PageReferrer(referrer)
                val eventParams : MutableMap<NhAnalyticsEventParam, Any?> = HashMap()
                fillUpBasicBaseAssetData(item, eventParams, itemPosition)
                fillUpBaseAsssetData(item, eventParams)
                fillUpCardViewParams(item, eventParams)
                eventParams[NhAnalyticsAppEventParam.APP_ID] = Constants.APP_ID
                SearchAnalyticsHelper.addSearchParams(
                        getReferrerEventSectionFrom(referrerProviderListener), eventParams)

                if (referrerProviderListener != null &&
                        !CommonUtils.isEmpty(referrerProviderListener.extraAnalyticsParams)) {
                    eventParams.putAll(referrerProviderListener.extraAnalyticsParams!!)
                }
                if (isPerspective) {
                    eventParams.put(AnalyticsParam.COLLECTION_NAME, Constants.MORE_STORIES_COLLECTION_NAME)
                    eventParams.put(AnalyticsParam.COLLECTION_ID, parentStory?.i_id())
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }

                if (postMulti) {
                    AnalyticsClient._logDynamic(NhAnalyticsAppEvent.STORY_CARD_CLICK,
                            getReferrerEventSectionFrom(referrerProviderListener),
                            eventParams, dynamicMap, null, pageReferrer, false)
                } else {
                    AnalyticsClient._logDynamic(NhAnalyticsAppEvent.STORY_CARD_CLICK,
                            getReferrerEventSectionFrom(referrerProviderListener),
                            eventParams, dynamicMap, null, pageReferrer, false)
                }
            }
        }
    }

    fun logLikeStoryEvent(eventSection: NhAnalyticsEventSection?, storyId: String, pageReferrer: PageReferrer?,
                          mapParam: MutableMap<NhAnalyticsEventParam, Any?>?, item: CommonAsset?,
                          parentItem: CommonAsset?, referrerFlow: PageReferrer?, likeType: LikeType, groupInfo: GroupInfo?,
                          isComment: Boolean?, commentType : String?) {
        logRunnable {

            if (eventSection != null && pageReferrer != null) {
                val map: MutableMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = storyId

                if (item != null) {
                    val itemType: String? = item.i_type()
                    fillAssetParams(map, item.i_langCode(), itemType,
                            item.i_source()?.id)
                    fillUpNonLinearParams(item, map)
                }

                if (item?.i_selectedLikeType() != null) {
                    map[NHAnalyticsSocialCommentsEventParam.LIKE_EMOJI_TYPE] = SocialFeaturesConstants.UNLIKE_EMOJI_TYPE
                } else if (!CommonUtils.isEmpty(likeType.name)) {
                    map[NHAnalyticsSocialCommentsEventParam.LIKE_EMOJI_TYPE] = likeType.name.toLowerCase()
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item?.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }


                if (referrerFlow != null) {
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                }
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item?.i_source()?.type
                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item?.i_source()?.id


                if (isComment == true) {
                    map[AnalyticsParam.ITEM_TYPE] = commentType
                    map[NHAnalyticsSocialCommentsEventParam.COMMENT_ITEM_ID] = storyId
                    map[NHAnalyticsSocialCommentsEventParam.PARENT_ITEM_ID] = parentItem?.i_id()
                }

                val event = if (isComment == true) NHSocialAnalyticsEvent.COMMENT_LIKED else NHSocialAnalyticsEvent.STORY_LIKED
                AnalyticsClient._logDynamic(event, eventSection, map,
                        dynamicMap, null, pageReferrer, false)
            }
        }
    }


    @JvmStatic
    fun logStoryRepostClick(eventSection: NhAnalyticsEventSection?, storyId: String, pageReferrer: PageReferrer?,
                            mapParam: MutableMap<NhAnalyticsEventParam, Any?>?, item: CommonAsset?, groupInfo: GroupInfo?) {
        logRunnable {
            if (eventSection != null && pageReferrer != null) {
                val map: MutableMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = storyId

                if (item != null) {
                    val itemType: String? = item.i_type()
                    fillAssetParams(map, item.i_langCode(), itemType,
                            item.i_source()?.id)
                    fillUpNonLinearParams(item, map)
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item?.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item?.i_source()?.type
                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item?.i_source()?.id
                map[NHProfileAnalyticsEventParam.TARGET_COMMUNITY_ID] = groupInfo?.id ?: Constants.EMPTY_STRING
                AnalyticsClient._logDynamic(NHSocialAnalyticsEvent.STORY_REPOST_CLICK, eventSection, map,
                        dynamicMap, null, pageReferrer, false)
            }
        }
    }

    @JvmStatic
    fun logCommentClick(eventSection: NhAnalyticsEventSection?, storyId: String, pageReferrer: PageReferrer?,
                            mapParam: MutableMap<NhAnalyticsEventParam, Any?>?, item: CommonAsset?, type:String) {
        logRunnable {
            if (eventSection != null && pageReferrer != null) {
                val map: MutableMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = storyId

                if (item != null) {
                    val itemType: String? = item.i_type()
                    fillAssetParams(map, item.i_langCode(), itemType,
                            item.i_source()?.id)
                    fillUpNonLinearParams(item, map)
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item?.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item?.i_source()?.type
                map[NhAnalyticsAppEventParam.TYPE] = type
                AnalyticsClient._logDynamic(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, eventSection, map,
                        dynamicMap, null, pageReferrer, false)
            }
        }
    }

    @JvmStatic
    fun logOgClick(eventSection: NhAnalyticsEventSection?, storyId: String, pageReferrer:
    PageReferrer?, mapParam: MutableMap<NhAnalyticsEventParam, Any?>?, item: CommonAsset?, groupInfo: GroupInfo?) {
        logRunnable {
            if (eventSection != null && pageReferrer != null) {
                val map: MutableMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = storyId

                if (item != null) {
                    val itemType: String? = item.i_type()
                    fillAssetParams(map, item.i_langCode(), itemType,
                            item.i_source()?.id)
                    fillUpNonLinearParams(item, map)
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item?.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item?.i_source()?.type
                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item?.i_source()?.id
                map[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT] = NhAnalyticsAppEvent.OG_CLICK.isPageViewEvent.toString()
                map[NhAnalyticsAppEventParam.OG_URL] = item?.i_linkAsset()?.url
                AnalyticsClient._logDynamic(NhAnalyticsAppEvent.OG_CLICK, eventSection, map,
                        dynamicMap, null, pageReferrer, false)
            }
        }
    }

    @JvmStatic
    fun logPollClick(eventSection: NhAnalyticsEventSection?, storyId: String, pageReferrer:
    PageReferrer?, mapParam: MutableMap<NhAnalyticsEventParam, Any?>?, item: CommonAsset?, groupInfo: GroupInfo?) {
        logRunnable {
            if (eventSection != null && pageReferrer != null) {
                val map: MutableMap<NhAnalyticsEventParam, Any?> = mapParam ?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = storyId

                if (item != null) {
                    val itemType: String? = item.i_type()
                    fillAssetParams(map, item.i_langCode(), itemType,
                            item.i_source()?.id)
                    fillUpNonLinearParams(item, map)
                }

                val dynamicMap = HashMap<String, String>()
                val experiments = item?.i_experiments()
                if (experiments != null) {
                    dynamicMap.putAll(experiments)
                }
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item?.i_source()?.type
                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = item?.i_source()?.id
                map[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT] = NhAnalyticsAppEvent.POLL_CLICK
                        .isPageViewEvent.toString()
                AnalyticsClient._logDynamic(NhAnalyticsAppEvent.POLL_CLICK, eventSection, map,
                        dynamicMap, null, pageReferrer, false)
            }
        }
    }


    fun logStorySharedEvent(packageName: String?, shareUiParam: ShareUi?,
                            post: CommonAsset?,
                            referrer: PageReferrer?, eventSection: NhAnalyticsEventSection?, groupInfo: GroupInfo?) {
        logRunnable {

            if (post?.i_type() != null) {
                val map : MutableMap<NhAnalyticsEventParam, Any?> = HashMap()
                map[NhAnalyticsNewsEventParam.SHARE_TYPE] = packageName?:ShareHelper.PLATFORM_DEFAULT_SHARE_TYPE
                var shareUi = shareUiParam
                if (shareUi == ShareUi.FLOATING_ICON) {
                    shareUi = ShareUtils.getShareUiForFloatingIcon()
                }

                if (null != shareUi) {
                    map[NhAnalyticsNewsEventParam.SHARE_UI] = shareUi.shareUiName
                }

                map[AnalyticsParam.ITEM_ID] = post.i_id()
                map[AnalyticsParam.ITEM_TYPE] = post.i_type()
                map[AnalyticsParam.ITEM_PUBLISHER_ID] = post.i_source()?.id
                map[AnalyticsParam.ITEM_LANGUAGE] = post.i_langCode()
                fillUpBaseAsssetData(post, map)

                if (referrer != null) {
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrer.referrer
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrer.id
                }

                map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = post?.i_source()?.id
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = post.i_source()?.type
                SearchAnalyticsHelper.addSearchParams(eventSection ?: NhAnalyticsEventSection.NEWS, map)
                AnalyticsClient._logDynamic(NhAnalyticsAppEvent.STORY_SHARED, eventSection,
                        map, post.i_experiments(), null, referrer, false)
            }
        }
    }

    fun logStorySharedEvent(packageName: String?, shareUiParam: ShareUi?,
                            pageEntity: PageEntity,
                            referrer: PageReferrer?, eventSection: NhAnalyticsEventSection?) {
        logRunnable {
                val map : MutableMap<NhAnalyticsEventParam, Any?> = HashMap()
                map[NhAnalyticsNewsEventParam.SHARE_TYPE] = packageName?:ShareHelper.PLATFORM_DEFAULT_SHARE_TYPE
                var shareUi = shareUiParam
                if (shareUi == ShareUi.FLOATING_ICON) {
                    shareUi = ShareUtils.getShareUiForFloatingIcon()
                }

                if (null != shareUi) {
                    map[NhAnalyticsNewsEventParam.SHARE_UI] = shareUi.shareUiName
                }

                map[AnalyticsParam.ITEM_ID] = pageEntity.id
                map[AnalyticsParam.ITEM_TYPE] = pageEntity.entityType

                if (referrer != null) {
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrer.referrer
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrer.id
                }

                SearchAnalyticsHelper.addSearchParams(eventSection ?: NhAnalyticsEventSection.NEWS, map)
                AnalyticsClient._logDynamic(NhAnalyticsAppEvent.STORY_SHARED, eventSection,
                        map, hashMapOf(), null, referrer, false)
            }
    }

    fun logStoryCommentedEvent(eventSection : NhAnalyticsEventSection?,
                               mapParam: MutableMap<NhAnalyticsEventParam, Any?>?,
                               comment: CreatePostEntity?, referrer: PageReferrer?,
                               post: CommonAsset?,
                               referrerFlow: PageReferrer?) {
        logRunnable {
            if (eventSection != null && comment != null && referrer != null) {
                val map : MutableMap<NhAnalyticsEventParam, Any?> = mapParam?: HashMap()
                map[NHAnalyticsSocialCommentsEventParam.COMMENT_ITEM_ID] = comment.cpId
                map[NHAnalyticsSocialCommentsEventParam.PARENT_ITEM_ID] = comment.parentId

                // TODO : Figure out different between comment and reply to comment.
                map[NHAnalyticsSocialCommentsEventParam.ITEM_TYPE] = if (CommonUtils.isEmpty(comment.parentId))
                    SocialFeaturesConstants.COMMENT_TYPE_MAIN
                else
                    SocialFeaturesConstants.COMMENT_TYPE_REPLY

                var experimentMap: Map<String, String>? = null
                if (post != null) {
                    map[AnalyticsParam.ITEM_ID] = post.i_id()
                    experimentMap = post.i_experiments()
                    fillUpNonLinearParams(post, map)
                }
                if (referrerFlow != null) {
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer
                    map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                }
                AnalyticsClient._logDynamic(NHSocialAnalyticsEvent.STORY_COMMENTED, eventSection,
                        map, experimentMap, null, referrer, false)
            }
        }
    }

    fun logStoryListTimeSpentEvent(pageEntity:PageEntity?,
                                   currentPageReferrer: PageReferrer?,
                                   providedReferrer: PageReferrer?,
                                   tabIndex: Int,
                                   startTime: Long,
                                   pvActivity: NhAnalyticsPVType,
                                   exitAction: NhAnalyticsUserAction,
                                   section: NhAnalyticsEventSection,
                                   sectionId :String,
                                   fetchDao: FetchDao? = null,
                                   referrerFlow:PageReferrer?=null) {
        logRunnable {
            val eventParams: MutableMap<NhAnalyticsEventParam, Any?> =  HashMap()

            val timeSpent = SystemClock.elapsedRealtime() - startTime

            if (pageEntity != null) {
                addNewsPageEntityParams(eventParams, pageEntity)
                eventParams[NhAnalyticsNewsEventParam.PAGE_LAYOUT] = pageEntity.entityLayout
                eventParams[NhAnalyticsNewsEventParam.TABTYPE] = CommonUtils.toLowerCase(pageEntity.entityType)
                eventParams[NhAnalyticsNewsEventParam.TABNAME] = pageEntity.displayName
                eventParams[NhAnalyticsNewsEventParam.TABITEM_ID] = pageEntity.id
            }
            val pageNumber = fetchDao?.getMaxPageNumber(pageEntity?.id?:Constants.EMPTY_STRING, Constants.FETCH_LOCATION_LIST , sectionId) ?:0
            eventParams[NhAnalyticsNewsEventParam.LATEST_PAGENUMBER] = pageNumber
            val referrer: PageReferrer?
            referrer = if (providedReferrer?.referrer is RunTimeReferrer) {
                currentPageReferrer
            } else {
                if (pageNumber > 0) providedReferrer else currentPageReferrer
            }
            if (referrer != null) {
                referrer.referrerAction = NhAnalyticsAppState.getInstance().action
            }
            eventParams[NhAnalyticsNewsEventParam.PAGE_NUMBER] = pageNumber
            eventParams[NhAnalyticsNewsEventParam.TABINDEX] = tabIndex
            eventParams[NhAnalyticsNewsEventParam.PV_ACTIVITY] = pvActivity.name.toLowerCase()
            eventParams[AnalyticsParam.TIMESPENT] = timeSpent
            eventParams[NhAnalyticsNewsEventParam.EXIT_ACTION] = exitAction
            eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow?.referrer?.referrerName
            eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow?.id
            AnalyticsClient.log(NhAnalyticsAppEvent.TIMESPENT_PVACTIVITY, section, eventParams, referrer)

        }
    }

    fun logEvent(event: NhAnalyticsEvent,
                 section: NhAnalyticsEventSection,
                 paramsMap: Map<NhAnalyticsEventParam, Any?>?,
                 referrerObj: PageReferrer?, client: ClientType?) {
        logRunnable {
            AnalyticsClient._log(event, section, paramsMap, referrerObj, client)
        }
    }

    fun logExploreButtonClickEvent(referrer: PageReferrer?, buttonType: NewsExploreButtonType?, section: String) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            if (referrer == null || buttonType == null) {
                return@logRunnable
            }
            eventParams[NhAnalyticsNewsEventParam.BUTTON_TYPE] = buttonType.buttonType
            AnalyticsClient.addStateParamsAndPermanentParams(eventParams)
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
                getSection(section), eventParams, referrer)
        }
    }

    fun logExploreButtonLocationClickEvent(referrer: PageReferrer?,eventType:String?,section: PageSection)
    {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            if (referrer == null || eventType == null)
            {
                return@logRunnable
            }
            eventParams[NhAnalyticsNewsEventParam.TYPE] = eventType
            AnalyticsClient.addStateParamsAndPermanentParams(eventParams)
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
                getSection(section.section),eventParams,referrer)
        }
    }

    fun logImportFollowDoneClick(followCount: Int, referrer: PageReferrer?, type: String) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TYPE] = type
            eventParams[NhAnalyticsNewsEventParam.FOLLOWING_COUNT] = followCount
            AnalyticsClient.addStateParamsAndPermanentParams(eventParams)
            AnalyticsClient.addConnectionParams(eventParams)
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
                NhAnalyticsEventSection.APP, eventParams, referrer)
        }
    }

    fun logCreateGroupCardClick(referrer: PageReferrer?, event: NhAnalyticsEvent, type: String) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TYPE] = type
            AnalyticsClient.log(event, NhAnalyticsEventSection.GROUP, eventParams, referrer)
        }
    }

    fun logCreateGroupEvent(referrer: PageReferrer?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsAppEventParam.TYPE] = Constants.CREATE_GROUP
            AnalyticsClient.log(NhAnalyticsGroupEvent.UI_CREATE_GROUP,
                    NhAnalyticsEventSection.GROUP, eventParams, referrer)
        }
    }

    fun logEditPhotoClickEvent(referrer: PageReferrer?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsAppEventParam.TYPE] = Constants.EDIT_PHOTO
            AnalyticsClient.log(NhAnalyticsGroupEvent.UI_CREATE_GROUP,
                    NhAnalyticsEventSection.GROUP, eventParams, referrer)
        }
    }

    fun logJoinGroupClickEVent(referrer: PageReferrer?, buttonType: NewsExploreButtonType,
                               groupInfo: GroupInfo? = null) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.BUTTON_TYPE] = buttonType.buttonType
            groupInfo?.id?.let {
                eventParams[NhAnalyticsAppEventParam.GROUP_ID] = it
            }
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection
                    .GROUP, eventParams, referrer)
        }
    }

    fun logGroupSettingsClickEvent(referrer: PageReferrer?, groupInfo: GroupInfo?, newValue:
    String? = null, oldValue: String? = null, type: String, userProfile: MemberRole) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            newValue?.let {
                eventParams[NhAnalyticsAppEventParam.NEW_VALUE] = it
            }
            oldValue?.let {
                eventParams[NhAnalyticsAppEventParam.OLD_VALUE] = it
            }
            groupInfo?.id?.let {
                eventParams[NhAnalyticsAppEventParam.GROUP_ID] = it
            }
            eventParams[NhAnalyticsDialogEventParam.USER_PROFILE] = userProfile.name
            eventParams[NhAnalyticsAppEventParam.TYPE] = type
            AnalyticsClient.log(NhAnalyticsGroupEvent.GROUP_SETTING, NhAnalyticsEventSection
                    .GROUP, eventParams, referrer)
        }

    }

    fun logApprovalCardClickEvent(referrer: PageReferrer?, type: NewsExploreButtonType,
                                  referrerId: String, counts: String?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.BUTTON_TYPE] = type.buttonType
            referrer?.id = referrerId
            counts?.let {
                eventParams[NhAnalyticsAppEventParam.APPROVALS_PENDING] = it
            }
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection.GROUP, eventParams, referrer)
        }
    }

    fun logInviteScreenShown(referrer: PageReferrer?, groupId: String) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsAppEventParam.GROUP_ID] = groupId
            eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.INVITE
            AnalyticsClient.log(NhAnalyticsGroupEvent.INVITE_SCREEN_SHOWN,
                    NhAnalyticsEventSection.GROUP, eventParams, referrer)
        }
    }

    fun logInviteOptionClicked(type: String) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsAppEventParam.TYPE] = type
            AnalyticsClient.log(NhAnalyticsGroupEvent.GROUP_INVITE, NhAnalyticsEventSection
                    .GROUP, eventParams, PageReferrer(NhGenericReferrer.INVITE_SCREEN))
        }
    }

    fun logInviteMemberClick(position: Int, userId: String?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            userId?.let {
                eventParams[NhAnalyticsAppEventParam.MEMBER_USER_ID] = it
            }
            eventParams[NhAnalyticsAppEventParam.POSITION] = position
            AnalyticsClient.log(NhAnalyticsGroupEvent.GROUP_INVITE, NhAnalyticsEventSection
                    .GROUP, eventParams, PageReferrer(NhGenericReferrer.INVITE_SCREEN))
        }
    }

    fun logSentInvitationClick(phoneNo: List<String>, invitesSent: Int) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsAppEventParam.INVITES_SENT] = invitesSent
            eventParams[NhAnalyticsAppEventParam.PHONE_NOS] = phoneNo.toString()
            AnalyticsClient.log(NhAnalyticsGroupEvent.GROUP_INVITE, NhAnalyticsEventSection
                    .GROUP, eventParams, PageReferrer(NhGenericReferrer.INVITE_SCREEN))
        }
    }

    fun logEntityListViewForMemberLists(listType: String, referrer: PageReferrer, groupInfo: GroupInfo?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TABTYPE] = listType
            groupInfo?.userRole?.name?.let {
                eventParams[NhAnalyticsDialogEventParam.USER_PROFILE] = it
            }
            AnalyticsClient.log(NhAnalyticsAppEvent.ENTITY_LIST_VIEW, NhAnalyticsEventSection
                    .GROUP, eventParams, referrer)
        }
    }

    fun logGroupHomeEvent(referrer: PageReferrer?, groupInfo: GroupInfo?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TABTYPE] = Constants.GROUP
            groupInfo?.let {
                eventParams[NhAnalyticsNewsEventParam.TABITEM_ID] = it.id
            }
            AnalyticsClient.log(NhAnalyticsGroupEvent.GROUP_HOME, NhAnalyticsEventSection.GROUP,
                    eventParams, referrer)
        }
    }

    /**
     * Private helper methods below.
     */


    private fun addNewsPageEntityParams(newsPageEntityMap: MutableMap<NhAnalyticsEventParam, Any?>?,
                                        pageEntity: PageEntity?) {

        if (pageEntity == null || newsPageEntityMap == null) {
            return
        }

        newsPageEntityMap[NhAnalyticsNewsEventParam.TABTYPE] = CommonUtils.toLowerCase(pageEntity.entityType)
        newsPageEntityMap[NhAnalyticsNewsEventParam.TABITEM_ID] = pageEntity.id

        if (!CommonUtils.isEmpty(pageEntity.displayName)) {
            newsPageEntityMap[NhAnalyticsNewsEventParam.TABNAME] = pageEntity.displayName
        }
    }

    private fun getSourceKey(story: CommonAsset, parentStory: CommonAsset?): String {
        var sourceKey = Constants.EMPTY_STRING
        if (story.i_source()?.id != null) {//need no null check for baseAsset, as it would have
            // been validated in first step itself
            sourceKey = story.i_source()?.id ?: Constants.EMPTY_STRING
        } else if (parentStory != null && parentStory.i_source()?.id != null) {
            //need null check, because story without any parent can be shared
            sourceKey = parentStory.i_source()?.id ?: Constants.EMPTY_STRING
        }

        return sourceKey
    }

    private fun getReferrerEventSectionFrom(referrerProviderlistener: ReferrerProviderlistener?): NhAnalyticsEventSection {
        return if (referrerProviderlistener == null)
            NhAnalyticsEventSection.NEWS
        else
            referrerProviderlistener.referrerEventSection
    }

    private fun fillAssetParams(map: MutableMap<NhAnalyticsEventParam, Any?>,
                                langCode: String?, itemType: String?, sourceKey: String?) {
        map[AnalyticsParam.ITEM_LANGUAGE] = langCode
        map[AnalyticsParam.ITEM_TYPE] = itemType
        map[AnalyticsParam.ITEM_PUBLISHER_ID] = sourceKey
    }

    private fun fillStoryPageViewParams(item: CommonAsset, map: MutableMap<NhAnalyticsEventParam, Any?>) {
        fillUpBaseAsssetData(item, map)
        fillUpNonLinearParams(item, map)

        if (!CommonUtils.isEmpty(item.i_langCode())) {
            map[AnalyticsParam.ITEM_LANGUAGE] = item.i_langCode()
        }

        map[NhAnalyticsAppEventParam.SOURCE_TYPE] = item.i_source()?.type

        val operatorName = DeviceInfoHelper.getOperatorName(CommonUtils.getApplication())
        if (!DataUtil.isEmpty(operatorName)) {
            map[AnalyticsParam.NETWORK_SERVICE_PROVIDER] = operatorName
        }

        map[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT] = NhAnalyticsAppEvent.STORY_PAGE_VIEW.isPageViewEvent.toString()
    }

    private fun fillUpNonLinearParams(item: CommonAsset, map: MutableMap<NhAnalyticsEventParam, Any?>) {
        val parent = NonLinearStore.getParentId(item.i_id()) ?: return
        val extraData = HashMap<String, Any>()
        extraData[AnalyticsParam.PARENT_ITEM_TS.getName()] = parent.timeSpent
        extraData[AnalyticsParam.PARENT_ITEM_ID.getName()] = parent.parentId
        map[AnalyticsParam.EXTRA_DATA_CLIENT] = JsonUtils.toJson(extraData)
    }

    private fun fillUpBaseAsssetData(item: CommonAsset, eventParams: MutableMap<NhAnalyticsEventParam, Any?>) {
        eventParams[AnalyticsParam.ITEM_TYPE] = item.i_type()

        if (!eventParams.containsKey(AnalyticsParam.UI_TYPE) && item.i_uiType() != null) {
            val uitype = item.i_uiType()?.name
            eventParams[AnalyticsParam.UI_TYPE] = uitype
        }

        eventParams[AnalyticsParam.FORMAT] = item.i_format()
        eventParams[AnalyticsParam.SUB_FORMAT] = item.i_subFormat()
        eventParams[AnalyticsParam.TYPE] = item.i_type()

        if (item.i_cardLabel() != null && item.i_cardLabel()?.type != null) {
            eventParams[AnalyticsParam.CARD_LABEL] = item.i_cardLabel()?.type?.name
        }

        if (!CommonUtils.isEmpty(item.i_groupId())) {
            eventParams[AnalyticsParam.GROUP_ID] = item.i_groupId()
        }

        if (!CommonUtils.isEmpty(item.i_contentType())) {
            eventParams[AnalyticsParam.CONTENT_TYPE] = item.i_contentType()
        }

        if (item.i_landingType()!=null) {
            eventParams[NhAnalyticsNewsEventParam.LANDING_TYPE] = item.i_landingType()
        }

        if (!CommonUtils.isEmpty(item.i_type())) {
            eventParams[AnalyticsParam.ASSET_TYPE] = item.i_type()
        }

        /**
         * Added only for the items which are of asset type PHOTO.
         */
        if (AssetType.PHOTO.name == item.i_type() && !CommonUtils.isEmpty(item.i_childFetchUrl())) {
            eventParams[AnalyticsParam.IMAGECOUNT] = item.i_childCount()
        }

        fillUpNonLinearParams(item, eventParams)
    }

    private fun fillUpBasicBaseAssetData(item: CommonAsset,
                                         eventParams: MutableMap<NhAnalyticsEventParam, Any?>,
                                         itemPosition: Int,
                                         isNhEventIsFromCache: Boolean = false) {

        eventParams[AnalyticsParam.ITEM_ID] = item.i_id()
        eventParams[AnalyticsParam.ITEM_TYPE] = item.i_type()

        if (!CommonUtils.isEmpty(item.i_langCode())) {
            eventParams[AnalyticsParam.ITEM_LANGUAGE] = item.i_langCode()
        }

        eventParams[AnalyticsParam.ITEM_PUBLISHER_ID] = item.i_source()?.id
        eventParams[AnalyticsParam.CARD_POSITION] = itemPosition.toString()
        if (isNhEventIsFromCache || item.i_isFromCache()) {
            eventParams[AnalyticsParam.IS_FROM_CACHE] = true
        }
        eventParams[NhAnalyticsAppEventParam.SOURCE_TYPE] = item.i_source()?.type
    }

    private fun fillUpCardViewParams(item: CommonAsset,
                                     eventParams: MutableMap<NhAnalyticsEventParam, Any?>,
                                     isNhEventIsFromCache: Boolean = false) {

        if (isNhEventIsFromCache || item.i_isFromCache()) {
            eventParams[AnalyticsParam.IS_FROM_CACHE] = true
        }

        if (!CommonUtils.isEmpty(item.i_moreStories())) {
            eventParams[AnalyticsParam.COLLECTION_TYPE] = DailyhuntConstants.SIMILAR_STORIES
            eventParams[AnalyticsParam.COLLECTION_ITEM_TYPE] = DailyhuntConstants.SIMILAR_STORY_MAIN

            val collectionCount = (item.i_moreStories()?.size ?: 0) + 1
            eventParams[AnalyticsParam.COLLECTION_ITEM_COUNT] = collectionCount.toString()
        }

        fillUpNonLinearParams(item, eventParams)
    }

    fun getSection(section: String): NhAnalyticsEventSection {
        return when(section) {
            PageSection.TV.section -> NhAnalyticsEventSection.TV
            PageSection.FOLLOW.section -> NhAnalyticsEventSection.FOLLOW
            PageSection.GROUP.section -> NhAnalyticsEventSection.GROUP
            PageSection.PROFILE.section -> NhAnalyticsEventSection.PROFILE
            PageSection.SEARCH.section -> NhAnalyticsEventSection.SEARCH
            NhAnalyticsEventSection.APP.eventSection -> NhAnalyticsEventSection.APP
            else -> NhAnalyticsEventSection.NEWS
        }
    }

    @JvmStatic
    fun appendReferrerRaw(eventMap : HashMap<NhAnalyticsEventParam, Any?>, referrerRaw: String) {
        val type = object : TypeToken<Map<String, String>>() {}.type
        val map = JsonUtils.fromJson<Map<String,String>>(referrerRaw, type)
        if (map != null && map.isNotEmpty()) {
            map.entries.forEach {
                eventMap[DynamicEventParam("referrer_" + it.key)] = it.value
            }
        }
    }

    @JvmStatic
    fun logTabSelectionViewEvent(referrer: PageReferrer?, pageType: String?, section: String) {
        logRunnable {
            val event = NhAnalyticsNewsEvent.TABSELECTION_VIEW
            val eventParams = java.util.HashMap<NhAnalyticsEventParam, Any>()
            if (pageType == null) {
                return@logRunnable
            }
            eventParams[NhAnalyticsNewsEventParam.TABTYPE] = CommonUtils.toLowerCase(pageType)
            AnalyticsClient.log(event, getSection(section), eventParams, referrer)
        }
    }

    @JvmStatic
    fun logTabItemAddedOrRemoved(pageReferrer: PageReferrer, entity: PageEntity?,
                                 isRemoved: Boolean, section: String) {
        if (entity == null) {
            return
        }

        val eventParams = java.util.HashMap<NhAnalyticsEventParam, Any?>()
        addNewsPageEntityParams(eventParams, entity)
        eventParams[NhAnalyticsNewsEventParam.TABINDEX] = entity.viewOrder
        eventParams[NhAnalyticsNewsEventParam.TABITEM_ATTRIBUTION] = NewsConstants.TABITEM_ATTRIBUTION
        AnalyticsClient.log(
            if (isRemoved) NhAnalyticsNewsEvent.TABITEM_REMOVED else NhAnalyticsNewsEvent.TABITEM_ADDED,
           getSection(section), eventParams, pageReferrer)
    }

    @JvmStatic
    fun logEntityCardView(item: ActionableEntity, position: Int,
                          pageReferrer: PageReferrer?, section: String, isFPV: Boolean, model:
                          String,
                          parent: CommonAsset? = null) {

        logRunnable {
            val eventParams = hashMapOf<NhAnalyticsEventParam, Any?>(
                    AnalyticsParam.ENTITY_ID to item.entityId,
                    AnalyticsParam.ITEM_ID to item.entityId,
                    AnalyticsParam.CARD_POSITION to position,
                    AnalyticsParam.UI_TYPE to parent?.i_uiType(),
                    AnalyticsParam.ITEM_TYPE to item.entityType,
                    AnalyticsParam.ENTITY_NAME to item.displayName,
                    AnalyticsParam.ENTITY_TYPE to (item.entityType),
                    AnalyticsParam.ENTITY_SOURCE_TYPE to (item.entitySubType),
                    AnalyticsParam.ENTITY_TYPE to (item.entityType),
                    AnalyticsParam.CAROUSEL_ID to parent?.i_id(),
                            AnalyticsParam.ENTITY_POSITION to position,
                    NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE to if (isFPV) Constants.FPV else Constants.TPV,
                    NhAnalyticsNewsEventParam.FOLLOW_TYPE to if (model == FollowModel.FOLLOWERS.name) Constants.FOLLOWER else Constants.FOLLOWING)

            val eventSection = if (PageSection.PROFILE.section == section) NhAnalyticsEventSection.FOLLOW else getSection(section)
            SearchAnalyticsHelper.addSearchParams(eventSection, eventParams)

            AnalyticsClient._logDynamic(NhAnalyticsAppEvent.ENTITY_CARD_VIEW, eventSection,
                    eventParams, parent?.i_experiments(), null, pageReferrer, false)
        }
    }

    @JvmStatic
    fun logEntityListViewEventForColdStart(asset: CommonAsset,
                                           placement: String?,
                                           section: String,
                                           pageReferrer: PageReferrer?,
                                           position: Int) {
        logRunnable {
            val eventParams = hashMapOf<NhAnalyticsEventParam, Any?>(
                    AnalyticsParam.ITEM_TYPE to Constants.ITEM_TYPE_QMC,
                    AnalyticsParam.UI_TYPE to asset.i_uiType(),
                    AnalyticsParam.ASSET_TYPE to "question_multi_choices",
                    AnalyticsParam.CARD_TYPE to asset.i_landingType(),
                    AnalyticsParam.ITEM_ID to asset.i_id(),
                    AnalyticsParam.CARD_POSITION to position,
                    AnalyticsParam.REFERRER_ITEM_ID to (asset.i_referrerItemId()),
                    AnalyticsParam.REFERRER_ENTITY_SOURCE_TYPE to (asset.i_referrerEntitySourceType()),
                    NhAnalyticsNewsEventParam.WIDGET_TYPE to asset.i_coldStartAsset()?.widgetType,
                    NhAnalyticsNewsEventParam.WIDGET_DISPLAY_TYPE to asset.i_uiType()?.name,
                    NhAnalyticsNewsEventParam.WIDGET_PLACEMENT to placement,
                    NhAnalyticsNewsEventParam.LIST_ITEM_COUNT to asset.i_entityCollection()?.size)

            val referrerAction = NhAnalyticsAppState.getInstance().action
            pageReferrer?.let {
                it.referrerAction=it.referrerAction?:referrerAction
            }
            if (referrerAction != null) {
                eventParams[NhAnalyticsAppEventParam.REFERRER_ACTION] = referrerAction.name
            }
            AnalyticsClient._logDynamic(NhAnalyticsAppEvent.ENTITY_LIST_VIEW,
                    getSection(section),
                    eventParams,
                    asset.i_experiments(), null,
                    pageReferrer,
                    false)
        }
    }

    @JvmStatic
    fun logEntityListViewEventForLikeList(section: String,
                                          pageReferrer: PageReferrer?,
                                          referrerFlow: PageReferrer?,
                                          referrerType: String?) {
        logRunnable {
            val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
            if (referrerFlow != null && referrerFlow.referrer != null) {
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                eventParams[NhAnalyticsAppEventParam.REFERRER_TYPE] = referrerType
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                eventParams[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
            }

            AnalyticsClient.log(NhAnalyticsAppEvent.ENTITY_LIST_VIEW,
                    getSection(section),
                    eventParams,
                    pageReferrer)
        }
    }

    @JvmStatic
    fun logEntityCardViewEvent(item: EntityItem, section: String, pageReferrer: PageReferrer?,
                               parent: CommonAsset?, position: Int, cardPosition: Int,
                               landingType: CardLandingType) {
        logRunnable {
            val eventParams = hashMapOf<NhAnalyticsEventParam, Any?>(
                    AnalyticsParam.ENTITY_ID to item.i_entityId(),
                    AnalyticsParam.ITEM_ID to item.i_entityId(),
                    AnalyticsParam.CARD_POSITION to cardPosition,
                    AnalyticsParam.UI_TYPE to parent?.i_uiType(),
                    AnalyticsParam.CARD_TYPE to landingType,
                    AnalyticsParam.ITEM_TYPE to item.i_entityType(),
                    AnalyticsParam.ENTITY_NAME to item.i_displayName(),
                    AnalyticsParam.ENTITY_TYPE to (item.i_entityType()),
                    AnalyticsParam.ENTITY_POSITION to position,
                    AnalyticsParam.ENTITY_SOURCE_TYPE to item.i_entitySubType(),
                    AnalyticsParam.CAROUSEL_ID to parent?.i_id())

            val eventSection = getSection(section)
            SearchAnalyticsHelper.addSearchParams(eventSection, eventParams)
            val referrerAction = NhAnalyticsAppState.getInstance().action
            if (referrerAction != null) {
                eventParams[NhAnalyticsAppEventParam.REFERRER_ACTION] = referrerAction.name
            }

            AnalyticsClient._logDynamic(NhAnalyticsAppEvent.ENTITY_CARD_VIEW, eventSection,
                    eventParams, parent?.i_experiments(), null, pageReferrer, false)
        }
    }

    @JvmStatic
    fun logEntityListView(filter: String, model: String, isFPV: Boolean, referrer: PageReferrer?, section: String) {
        logRunnable {
            val params = mutableMapOf<NhAnalyticsEventParam, Any?>(
                    NhAnalyticsNewsEventParam.FILTER_TYPE to filter,
                    NhAnalyticsNewsEventParam.FOLLOW_TYPE to if (model == FollowModel.FOLLOWERS.name)
                        Constants.FOLLOWER else Constants.FOLLOWING,
                    NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE to if (isFPV) Constants.FPV else Constants.TPV
            )
            AnalyticsClient.log(NhAnalyticsAppEvent.ENTITY_LIST_VIEW, getSection(section),
                    params, referrer)
        }
    }

    @JvmStatic
    fun logSearchEntityListView(referrer: PageReferrer?, section: String, pageEntityData: PageEntity?, referrerFlow: PageReferrer?) {
        logRunnable {
            val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
            eventParams[NhAnalyticsNewsEventParam.TABNAME] = pageEntityData?.name
            eventParams[NhAnalyticsNewsEventParam.TABITEM_ID] = pageEntityData?.id
            if (referrerFlow != null && referrerFlow.referrer != null) {
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
                eventParams[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
                eventParams[NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID] = referrerFlow.subId
            }
            AnalyticsClient.log(NhAnalyticsAppEvent.ENTITY_LIST_VIEW, getSection(section), eventParams, referrer)
        }
    }

    @JvmStatic
    fun logEntityCardClick(actionableEntity: ActionableEntity, position: Int, isFPV: Boolean, model: String, section: String, pageReferrer: PageReferrer?) {
        val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
        eventParams[AnalyticsParam.ENTITY_ID] = actionableEntity.entityId
        eventParams[AnalyticsParam.ENTITY_TYPE] = actionableEntity.entityType
        eventParams[AnalyticsParam.ENTITY_SUBTYPE] =  actionableEntity.entitySubType
        eventParams[AnalyticsParam.ENTITY_NAME] = actionableEntity.displayName
        eventParams[AnalyticsParam.ENTITY_POSITION] =  position
        eventParams[NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE] = if (isFPV) Constants.FPV else Constants.TPV
        eventParams[NhAnalyticsNewsEventParam.FOLLOW_TYPE] = if (model == FollowModel.FOLLOWERS.name) Constants.FOLLOWER else Constants.FOLLOWING
        val eventSection = if (PageSection.PROFILE.section == section) NhAnalyticsEventSection.FOLLOW else getSection(section)
        AnalyticsClient.logDynamic(NhAnalyticsAppEvent.ENTITY_CARD_CLICK, eventSection, eventParams, null, pageReferrer, false)
    }

    @JvmStatic
    fun logEntityCardClick(actionableEntity: ActionableEntity, position: Int, section: String, pageReferrer: PageReferrer?, parent: CommonAsset? = null) {
        val carouselId: String? = parent?.i_id()
        val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
        eventParams[AnalyticsParam.ENTITY_ID] = actionableEntity.entityId
        eventParams[AnalyticsParam.ENTITY_TYPE] = actionableEntity.entityType
        eventParams[AnalyticsParam.ENTITY_SUBTYPE] =  actionableEntity.entitySubType
        eventParams[AnalyticsParam.ENTITY_NAME] = actionableEntity.displayName
        eventParams[AnalyticsParam.ENTITY_POSITION] =  position
        eventParams[AnalyticsParam.CAROUSEL_ID] = carouselId
        eventParams[AnalyticsParam.UI_TYPE] = parent?.i_uiType()
        val eventSection = if (PageSection.PROFILE.section == section) NhAnalyticsEventSection.FOLLOW else getSection(section)
        AnalyticsClient.logDynamic(NhAnalyticsAppEvent.ENTITY_CARD_CLICK, eventSection, eventParams, parent?.i_experiments(), pageReferrer, false)
    }


    @JvmStatic
    fun logCollectionEventViewItemInCard(collection: CommonAsset?,
                                         pageReferrer: PageReferrer?,
                                         position: Int,
                                         mapParam: HashMap<NhAnalyticsEventParam, Any?>?,
                                         section: String) {
        logRunnable {
            val item = collection?.i_collectionItems()?.getOrNull(position) ?: return@logRunnable

            val eventParams = mapParam ?: HashMap()
            fillUpBasicBaseAssetData(item, eventParams, position)
            fillUpBaseAsssetData(item, eventParams)
            fillUpCardViewParams(item, eventParams)
            val experiments = item.i_experiments()
            CardSeenStatusRepo.DEFAULT.markSeen(item.i_id())
            if (experiments != null)
                AnalyticsClient.logDynamic(NhAnalyticsAppEvent.STORY_CARD_VIEW, getSection(section),
                        eventParams, experiments, pageReferrer, false)
            else
                AnalyticsClient.log(NhAnalyticsAppEvent.STORY_CARD_VIEW, getSection(section),
                        eventParams, pageReferrer)
        }
    }

    @JvmStatic
    fun logBottomSheetExpand(item: CommonAsset?, eventSection: NhAnalyticsEventSection,
                             pageReferrer: PageReferrer?,
                             eventParams: HashMap<NhAnalyticsEventParam, Any?>) {
        eventParams[AnalyticsParam.ITEM_ID] = item?.i_id()
        AnalyticsClient.log(NhAnalyticsAppEvent.VIDEO_SCROLL_UP,
                eventSection, eventParams, pageReferrer)
    }

    fun logDownloadEvent(event: NhAnalyticsAppEvent?, section: NhAnalyticsEventSection?,
                         params: Map<NhAnalyticsEventParam, Any?>, referrer: PageReferrer?,
                         dynamicParams: Map<String, String>?) {
        if (event == null || CommonUtils.isEmpty(params) || section == null) {
            return
        }
        val pageReferrer = PageReferrer(referrer)
        pageReferrer.referrerAction = NhAnalyticsUserAction.CLICK
        AnalyticsClient.logDynamic(event, section, params, dynamicParams, pageReferrer, false)
    }

    @JvmStatic
    fun logFeatureNudgeEvent(type:String) {
        logRunnable {
            val map = HashMap<NhAnalyticsEventParam, Any>()
            map[NhAnalyticsAppEventParam.TYPE] = type
            AnalyticsClient.log(NhAnalyticsNewsEvent.FEATURE_NUDGE,
                NhAnalyticsEventSection.APP, map, PageReferrer(NhGenericReferrer.COACHMARK))
        }
    }

    @JvmStatic
    fun logWalkThroughExploreButtonClickEvent(type: String,
                                              additionalParams: Map<NhAnalyticsEventParam, Any>? = null,
                                              referrer: PageReferrer? = null) {
        logRunnable {
            val map = HashMap<NhAnalyticsEventParam, Any>()
            map[NhAnalyticsAppEventParam.TYPE] = type
            additionalParams?.let {
                map.putAll(it)
            }
            val pageReferrer = referrer ?: PageReferrer(NhGenericReferrer.WALKTHROUGH)
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
                NhAnalyticsEventSection.APP, map, pageReferrer)
        }
    }

    @JvmStatic
    fun logFollowButtonClickEvent(actionableEntity: ActionableEntity, referrer: PageReferrer?, isFollowing: Boolean, section: String) {
        logRunnable {
            val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
            eventParams[AnalyticsParam.ITEM_TYPE] = actionableEntity.entityType
            eventParams[AnalyticsParam.ITEM_ID] = actionableEntity.entityId
            eventParams[AnalyticsParam.ITEM_NAME] = actionableEntity.displayName ?: Constants.EMPTY_STRING
            eventParams[NhAnalyticsAppEventParam.SOURCE_TYPE] = actionableEntity.entitySubType?: Constants.EMPTY_STRING
            eventParams[NhAnalyticsAppEventParam.TYPE] = if (isFollowing) Constants.TYPE_FOLLOWED else Constants.TYPE_UNFOLLOWED
            val dynamicMap = HashMap<String, String>()
            val experiments = actionableEntity.experiment
            if (experiments != null) {
                dynamicMap.putAll(experiments)
            }
            val eventSection = if (section == PageSection.PROFILE.section) NhAnalyticsEventSection.FOLLOW else getSection(section)
            AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, eventSection, eventParams, dynamicMap, null, referrer, false)
        }
    }
    @JvmStatic
    fun logFollowBlockCorosalClickEvent(actionableEntity: ActionableEntity, referrer: PageReferrer?, isBlocked: Boolean, section: String) {
        logRunnable {
            val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
            eventParams[AnalyticsParam.ITEM_TYPE] = actionableEntity.entityType
            eventParams[AnalyticsParam.ITEM_ID] = actionableEntity.entityId
            eventParams[AnalyticsParam.ITEM_NAME] = actionableEntity.displayName ?: Constants.EMPTY_STRING
            eventParams[NhAnalyticsAppEventParam.SOURCE_TYPE] = actionableEntity.entitySubType?: Constants.EMPTY_STRING
            eventParams[NhAnalyticsAppEventParam.TYPE] = if (isBlocked) Constants.BLOCKED else Constants.UNBLOCKED
            val dynamicMap = HashMap<String, String>()
            val experiments = actionableEntity.experiment
            if (experiments != null) {
                dynamicMap.putAll(experiments)
            }
            val eventSection = if (section == PageSection.PROFILE.section) NhAnalyticsEventSection.FOLLOW else getSection(section)
            AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, eventSection, eventParams, dynamicMap, null, referrer, false)
        }
    }

    @JvmStatic
    fun logForegroundSessionStartEvent(sessionInfo: SessionInfo) {
        val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
        eventParams[AnalyticsParam.ITEM_ID] = sessionInfo.id
        AnalyticsClient.log(NhAnalyticsAppEvent.FG_SESSION_START, NhAnalyticsEventSection.APP, eventParams)
    }

    @JvmStatic
    fun logForegroundSessionEndEvent(sessionInfo: SessionInfo) {
        val eventParams = mutableMapOf<NhAnalyticsEventParam, Any?>()
        eventParams[AnalyticsParam.ITEM_ID] = sessionInfo.id
        eventParams[NhAnalyticsAppEventParam.SESSION_LENGTH] = (sessionInfo.endTime - sessionInfo.startTime)
        eventParams[NhAnalyticsAppEventParam.FG_SESSION_ID] = PreferenceManager.getPreference(AppStatePreference.FG_SESSION_ID, "")
        AnalyticsClient.log(NhAnalyticsAppEvent.FG_SESSION_END, NhAnalyticsEventSection.APP, eventParams)
    }

    @JvmStatic
    fun getParamsForCardSeenEvent(asset: CommonAsset, eventParam: MutableMap<NhAnalyticsEventParam, Any?>,
                                  position: Int, referrerFlow: PageReferrer?, referrerRaw: String?, timeSpent:Long,
                                  isSCV: Boolean, isSPV: Boolean, referrer: PageReferrer?) : MutableMap<String, Any?>{
        fillUpBasicBaseAssetData(asset, eventParam, position)
        fillStoryPageViewParams(asset, eventParam)
        if(referrerFlow != null) {
            eventParam[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow.referrer.referrerName
            eventParam[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow.id
        }
        if (!CommonUtils.isEmpty(referrerRaw)) {
            eventParam[AnalyticsParam.REFERRER_RAW] = referrerRaw
        }
        eventParam[AnalyticsParam.TIMESPENT] = timeSpent
        eventParam[AnalyticsParam.IS_SCV] = isSCV
        eventParam[AnalyticsParam.IS_SPV] = isSPV
        NhAnalyticsAppState.addReferrerParams(referrer, eventParam)
        eventParam[NhAnalyticsAppEventParam.IS_IN_FG] = CommonUtils.isInFg
        return AttributeFilter.filterForNH(eventParam)
    }

    @JvmStatic
    fun getTimespentParams(): Map<String, Any> {
        return timeSpentParams
    }

    @JvmStatic
    fun logImportContactsShown(referrer: PageReferrer?) {
        AnalyticsClient.log(NhAnalyticsAppEvent.IMPORT_SHOWN, NhAnalyticsEventSection.APP, null, referrer)
    }

    @JvmStatic
    fun logImportContactsAllowClick(referrer: PageReferrer?) {
        AnalyticsClient.log(NhAnalyticsAppEvent.IMPORT_ACTION, NhAnalyticsEventSection.APP, null, referrer)
    }

    @JvmStatic
    fun logVHNSFWAcceptanceEvent(itemId: String?, section: String) {
        val map: MutableMap<NhAnalyticsEventParam, Any> = java.util.HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = ExploreButtonType.NSFW_ALLOWED
        val referrer = PageReferrer(NhGenericReferrer.STORY_DETAIL, itemId)
        AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, getSection(section),
                map, referrer)
    }

    /**
     * Method to trigger VH item menu click event for Viral Item Menu
     *
     * @param asset
     */
    @JvmStatic
    fun logVHItemMenuClickEvent(asset: CommonAsset?, section: String) {
        asset?.i_viral() ?: return
        val map: MutableMap<NhAnalyticsEventParam, Any> = java.util.HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = ExploreButtonType.THREE_DOTS
        val referrer = PageReferrer(NhGenericReferrer.STORY_DETAIL)
        map[AnalyticsParam.ITEM_ID] = asset.i_id()
        referrer.id = asset.i_id()
        AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, getSection(section),
                map, referrer)
    }

    @JvmStatic
    fun logCarouselEventReportButtonClicked(asset: CommonAsset?,
                                           position: Int,
                                           pageReferrer: PageReferrer?,
                                           referrerProviderlistener: ReferrerProviderlistener?) {
        if (asset == null) {
            return
        }
        val eventParams = HashMap<NhAnalyticsEventParam, Any?>()
        fillUpBasicBaseAssetData(asset, eventParams, position)
        fillUpBaseAsssetData(asset, eventParams)
        fillUpCardViewParams(asset, eventParams)
        eventParams.put(NhAnalyticsAppEventParam.TYPE, "collection_menu")
        eventParams.put(AnalyticsParam.COLLECTION_ID, asset.i_id())
        eventParams.put(AnalyticsParam.COLLECTION_TYPE, asset.i_type())
        AnalyticsClient._logDynamic(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK,
                getReferrerEventSectionFrom(referrerProviderlistener),
                eventParams, asset.i_experiments(), null, pageReferrer, false)
    }

    @JvmStatic
    fun logNotificationCtaClickEvent(cta: NotificationCtaTypes, referrer: PageReferrer?) {
        logRunnable {
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.CLICK_TYPE] = cta.name
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
                NhAnalyticsEventSection.NOTIFICATION, eventParams, referrer)
        }
    }

    fun logAdjunctLangCardViewEvent(adjunctLang: String, tickerType: String?){
        val map: MutableMap<NhAnalyticsEventParam, Any> = HashMap()
        tickerType?.let {
            map[NhAnalyticsAppEventParam.TICKER_TYPE] = tickerType
        }
        map[NhAnalyticsAppEventParam.ADJUNCT_LANGUAGE] = adjunctLang
        AnalyticsClient.log(NhAnalyticsNewsEvent.TICKER_VIEW, NhAnalyticsEventSection.NEWS, map)
    }

    fun logAdjunctLangSnackbarViewEvent(adjunctLang: String, langFlow:String?){
        val map: MutableMap<NhAnalyticsEventParam, Any> = HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = Constants.ADJUNCT_LANGUAGE
        map[NhAnalyticsAppEventParam.ADJUNCT_LANGUAGE] = adjunctLang
        val referrer = PageReferrer(RunTimeReferrer(langFlow, null), null)
        AnalyticsClient.log(NhAnalyticsAppEvent.SNACKBAR_VIEW, NhAnalyticsEventSection.NEWS, map,referrer)
    }

    @JvmStatic
    fun logAdjunctLangCardCtaClickEvent(adjunctLang: String, actionType: String,tickerType: String?){
        val map: MutableMap<NhAnalyticsEventParam, Any> = HashMap()
        tickerType?.let {
            map[NhAnalyticsAppEventParam.TICKER_TYPE] = tickerType
        }
        map[NhAnalyticsAppEventParam.ADJUNCT_LANGUAGE] = adjunctLang
        map[NhAnalyticsAppEventParam.ACTION_TYPE] = actionType
        AnalyticsClient.log(NhAnalyticsNewsEvent.TICKER_CLICK, NhAnalyticsEventSection.NEWS, map)
    }

    fun logAdjunctLangSnackbarSettingsClick(langFlow:String?){
        val map: MutableMap<NhAnalyticsEventParam, Any> = HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = Constants.ADJUNCT_LANGUAGE_SNACKBAR_SETTINGS
        val referrer = PageReferrer(RunTimeReferrer(langFlow, null), null)
        AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection.APP, map,referrer)
    }

    fun logFollowBlockSnackbarViewEvent(referrer: PageReferrer?,type:String) {
        val map:MutableMap<NhAnalyticsEventParam,Any> = HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = type
        referrer?.let {
            map[NhAnalyticsAppEventParam.REFERRER] = referrer
        }
        AnalyticsClient.log(NhAnalyticsAppEvent.SNACKBAR_VIEW, NhAnalyticsEventSection.APP,map,referrer)
    }

    fun logFollowBlockSnackbarUndoEvent(referrer: PageReferrer?,type:String,actionType:String) {
        val map:MutableMap<NhAnalyticsEventParam,Any> = HashMap()
        map[NhAnalyticsAppEventParam.TYPE] = type
        referrer?.let {
            map[NhAnalyticsAppEventParam.REFERRER] = referrer
        }
        map[NhAnalyticsAppEventParam.ACTION_TYPE] = actionType
        AnalyticsClient.log(NhAnalyticsAppEvent.SNACKBAR_ACTION, NhAnalyticsEventSection.APP,map,referrer)
    }

    //TODO priya debug second chunk missing in notification flow
    @JvmStatic
    fun logDevCustomErrorEvent(message : String ) {
        val loggerEnabled = AppConfig.getInstance() != null && AppConfig.getInstance()
            .isLoggerEnabled
        val map = hashMapOf("error_type" to "Second Chunk Missing",
            "Error" to message
        )
        if (loggerEnabled) {
            AnalyticsClient.logDynamic(
                NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP,
                null, map , false
            )
        }
    }

}