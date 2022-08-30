/*
 *  *Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.deeplink.navigator

import android.content.Context
import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.news.util.NewsConstants
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.NewsNavModel

/**
 * @author santhosh.kc
 */
class EntityPreviewUtils {

    companion object {

        @JvmStatic
        fun getTopicPreviewIntent(context: Context, newsNavModel: NewsNavModel?,
                                  pageReferrer: PageReferrer?):
                Intent? {
            val newsHomeRouterInput = getRouterInputFrom(newsNavModel, pageReferrer)
            newsHomeRouterInput ?: return null

            val intent = Intent(Constants.ENTITY_OPEN_ACTION)

            intent.putExtra(NewsConstants.SHOW_SELECT_TOPIC_BUTTON,
                    AppSectionsProvider.isSectionAvailable(AppSection.NEWS))
            intent.putExtra(NewsConstants.ENTITY_KEY, newsHomeRouterInput.entityKey)
            intent.putExtra(NewsConstants.ENTITY_TYPE, newsHomeRouterInput.entityType)
            if (!CommonUtils.isEmpty(newsHomeRouterInput.subEntityKey)) {
                intent.putExtra(NewsConstants.SUB_ENTITY_KEY, newsHomeRouterInput.subEntityKey)
            }
            buildIntentExtras(intent, newsHomeRouterInput)
            return intent
        }

        @JvmStatic
        fun getLocationPreviewIntent(context: Context, newsNavModel: NewsNavModel?, pageReferrer:
        PageReferrer?) : Intent? {
            val newsHomeRouterInput = getRouterInputFrom(newsNavModel, pageReferrer)
            newsHomeRouterInput ?: return null

            val intent = Intent(Constants.ENTITY_OPEN_ACTION)
            intent.putExtra(NewsConstants.SHOW_SELECT_LOCATION_BUTTON,
                    AppSectionsProvider.isSectionAvailable(AppSection.NEWS))
            intent.putExtra(NewsConstants.ENTITY_KEY, newsHomeRouterInput.entityKey)
            intent.putExtra(NewsConstants.ENTITY_TYPE, newsHomeRouterInput.entityType)
            if (!CommonUtils.isEmpty(newsHomeRouterInput.subEntityKey)) {
                intent.putExtra(NewsConstants.SUB_ENTITY_KEY, newsHomeRouterInput.subEntityKey)
            }
            buildIntentExtras(intent, newsHomeRouterInput)
            return intent
        }

        @JvmStatic
        fun getRouterInputFrom(newsNavModel: NewsNavModel?, pageReferrer: PageReferrer?):
                NewsHomeRouterInput? {
            if (newsNavModel == null || newsNavModel.baseInfo == null) {
                return null
            }

            var entityKey = Constants.EMPTY_STRING
            var subEntityKey: String? = Constants.EMPTY_STRING
            if (!CommonUtils.isEmpty(newsNavModel.topicKey)) {
                entityKey = newsNavModel.topicKey
                subEntityKey = newsNavModel.subTopicKey
            } else if (!CommonUtils.isEmpty(newsNavModel.locationKey)) {
                entityKey = newsNavModel.locationKey
                subEntityKey = newsNavModel.subLocationKey
            }
            if (CommonUtils.isEmpty(entityKey)) {
                return null
            }

            val language: String? = newsNavModel.baseInfo.language
            val langCode: String? = newsNavModel.baseInfo.languageCode
            val edition: String? = newsNavModel.baseInfo.edition
            val notificationBackUrl: String? = newsNavModel.baseInfo.v4BackUrl

            val navigationType: NavigationType? = NavigationType.fromIndex(
                    Integer.parseInt(newsNavModel.getsType()))
            val pageType = getPageTypeFromNavigationType(navigationType, subEntityKey)
                    ?: return null
            return NewsHomeRouterInput.Builder().setUniqueId(newsNavModel.baseInfo.uniqueId)
                    .setEntityKey(entityKey).setSubEntityKey(subEntityKey).setPageType(pageType)
                    .setNavigationType(navigationType).setPageReferrer(pageReferrer).setLanguage(language)
                    .setLangCode(langCode).setEdition(edition).setNotificationBackUrl(notificationBackUrl)
                    .setDeeplinkUrl(newsNavModel.baseInfo.deeplink).setEntityType(newsNavModel.entityType).build()
        }

        @JvmStatic
        fun getPageTypeFromNavigationType(navigationType: NavigationType?,
                                          subEntityKey: String?): PageType? {
            if (navigationType == null) {
                return null
            }

            return when (navigationType) {
                NavigationType.TYPE_OPEN_TOPIC -> if (subEntityKey != null) PageType.SUB_TOPIC else PageType.TOPIC
                NavigationType.TYPE_OPEN_LOCATION -> if (subEntityKey != null) PageType.SUB_LOCATION else PageType.LOCATION
                NavigationType.TYPE_OPEN_VIRAL_TOPIC -> PageType.VIRAL
                else -> return null
            }
        }

        @JvmStatic
        fun buildIntentExtras(intent: Intent?, input: NewsHomeRouterInput?) {
            if (intent == null || input == null) {
                return
            }

            //Setting unique id, used in case of notification
            if (input.uniqueId != null) {
                intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, input.uniqueId)
            }

            //Setting PageReferrer
            if (input.pageReferrer != null) {
                intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, input.pageReferrer)
            }

            //Setting navigationType
            if (input.navigationType != null) {
                intent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE, input.navigationType.name)
            }

            //Setting edition
            if (!CommonUtils.isEmpty(input.edition)) {
                intent.putExtra(NewsConstants.EDITION_FROM_DEEPLINK_URL, input.edition)
            }

            //Setting language
            if (!CommonUtils.isEmpty(input.language)) {
                intent.putExtra(NewsConstants.LANGUAGE_FROM_DEEPLINK_URL, input.language)
            }

            //Setting language Code
            if (!CommonUtils.isEmpty(input.langCode)) {
                intent.putExtra(NewsConstants.LANGUAGE_CODE_FROM_DEEPLINK_URL, input.langCode)
            }

            //Setting notification back url
            if (!CommonUtils.isEmpty(input.notificationBackUrl)) {
                intent.putExtra(Constants.V4BACKURL, input.notificationBackUrl)
            }

            //Setting deeplink url
            if (!CommonUtils.isEmpty(input.deeplinkUrl)) {
                intent.putExtra(Constants.DEEP_LINK_URL, input.deeplinkUrl)
            }
        }
    }
}