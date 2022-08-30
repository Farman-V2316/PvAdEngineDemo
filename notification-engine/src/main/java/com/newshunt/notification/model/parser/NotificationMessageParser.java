/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.parser;

import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_FLUSH_BLACKLIST_LANGUAGE;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.LanguageUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.dataentity.notification.AdjunctLangNavModel;
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel;
import com.newshunt.dataentity.notification.FlushNavModel;
import com.newshunt.dataentity.notification.InAppNotificationModel;
import com.newshunt.dataentity.notification.SilentNotificationModel;
import com.newshunt.dataentity.notification.SilentVersionedApiTriggerModel;
import com.newshunt.dataentity.notification.SilentVersionedApiUpdateModel;
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.notification.model.entity.NotificationFilterType;
import com.newshunt.notification.model.entity.NotificationInvalidType;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.NotificationSectionType;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.StickyNavModelType;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import androidx.annotation.Nullable;

/**
 * Parses notification message of different types
 * <p/>
 * Sample notification message types (just for the reference)
 * simple wake up
 * NH#^$YYMMDDHH~21~<messageToBeShown>
 * NH#^$YYMMDDHH~21~<messageToBeShown>~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to News:
 * NH#^$YYMMDDHH~22~<messageToBeShown>~fkey~npkey~ctkey~newsid~notifySrc~expTimeSrc~unimsg~
 * To open NewsItem
 * NH#^$YYMMDDHH~22~message~fKey~npKey~ctKey~news~unicodemessage~
 * <p/>
 * wake up to Books Details:
 * NH#^$YYMMDDHH~23~<messageToBeShown>~bookid~
 * NH#^$YYMMDDHH~23~<messageToBeShown>~bookid~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to Books Collections (discarded due to client issue for 2nd book released clients):
 * NH#^$YYMMDDHH~24~message~collectionKey~
 * NH#^$YYMMDDHH~24~message~collectionKey~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to Books Collections
 * NH#^$YYMMDDHH~25~message~collectionKey~language~
 * NH#^$YYMMDDHH~25~message~collectionKey~language~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to Book Home Page
 * NH#^$YYMMDDHH~26~message~language~
 * NH#^$YYMMDDHH~26~message~language~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to Book Payment Page
 * NH#^$YYMMDDHH~28~<messageToBeShown>~bookid~
 * NH#^$YYMMDDHH~28~<messageToBeShown>~bookid~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up to Cart
 * NH#^$YYMMDDHH~31~<messageToBeShown>~notifySrc~expTimeSrc~unimsg~
 * <p/>
 * wake up  to Topic
 * NH#^$YYMMDDHH~40~message~topicKey~unicodemessage~
 *
 * @author santosh.kulkarni
 */
public class NotificationMessageParser {

    public static NavigationModel parseNotificationMessage(Bundle extras, long timeStamp) {

        NavigationModel notificationDetails = new NavigationModel();
        notificationDetails.setTimeStamp(timeStamp);
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();

        // currently we get entire notification params in message key and Imagelink in imageLink key
        // ignoring other default params
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;

            switch (key) {
                case NotificationConstants.MESSAGE:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        // Currently its always set to small notification , once we
                        // start supporting big notification , based on params in message this value will change.
                        parseDetails(value, notificationDetails);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.IMAGELINK:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setImageLink(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.BIGIMAGELINK:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setBigImageLink(value);
                        notificationDetails.setLayoutType(NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.BIGTEXT:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setBigText(value);
                        notificationDetails.setLayoutType(NotificationLayoutType.NOTIFICATION_TYPE_BIG_TEXT);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.PRIORITY:
                    try {
                        value = extras.getString(key);
                        notificationDetails.setPriority(Integer.parseInt(value));
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.IMAGELINK_V2:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setImageLinkV2(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.BIGIMAGELINK_V2:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setBigImageLinkV2(value);
                        notificationDetails.setLayoutType(NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.INBOXIMAGELINK:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setInboxImageLink(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.LANGUAGECODE:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        if (LanguageUtils.isUrdu(value)) {
                            //Lang is Urdu
                            notificationDetails.setUrdu(true);
                        }
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.DISPLAYTIME:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4DisplayTime(Long.parseLong(value));
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.ISINTERNETREQUIRED:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4IsInternetRequired(Boolean.parseBoolean(value));
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.EXPIRYTIME:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setExpiryTime(Long.parseLong(value));
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.NOTIFICATION_ID_SWIPEURL_PREFIX:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4SwipeUrl(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.NOTIFICATION_ID_BACKURL_PREFIX:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4BackUrl(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.NOTIFICATION_ID_SWIPE_LOGIC_PREFIX:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4SwipePageLogic(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;

                case NotificationConstants.NOTIFICATION_ID_SWIPE_LOGIC_ID_PREFIX:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setV4SwipePageLogicId(value);
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;
                case NotificationConstants.NOTIFICATION_DONOT_AUTO_FETCH_SWIPEURL:
                    try {
                        value = extras.getString(key);
                        value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                        notificationDetails.setDoNotAutoFetchSwipeUrl(value.equals("true"));
                    } catch (Exception e) {
                        Logger.caughtException(e);
                    }
                    break;
            }
        }
        return notificationDetails;
    }

    public static LiveTVNavModel parseLiveTVNotificationMessage(Bundle extras,
                                                                long receivedTimeStamp) {

        LiveTVNavModel notificationDetails = new LiveTVNavModel();
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        // currently we get entire notification params in message key and Imagelink in imageLink key
        // ignoring other default params
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;

            if (key.equals(NotificationConstants.MESSAGE_V2)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Gson gson = new Gson();

                    notificationDetails = gson.fromJson(value, LiveTVNavModel.class);
                    if (notificationDetails == null || notificationDetails.getBaseInfo() == null) {
                        return null;
                    }

                    if (!CommonUtils.isEmpty(notificationDetails.getBaseInfo().getBigImageLink()) || !CommonUtils
                            .isEmpty(notificationDetails.getBaseInfo().getBigImageLinkV2())) {
                        notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                                .NOTIFICATION_TYPE_BIG_PICTURE);
                    } else {
                        notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                                .NOTIFICATION_TYPE_SMALL);
                    }

                    notificationDetails.setDate(System.currentTimeMillis());
                    notificationDetails.getBaseInfo().setSectionType(NotificationSectionType.LIVETV);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }

            }
        }
        if (notificationDetails != null && notificationDetails.getBaseInfo() != null) {
            // Below code is to get unique id for notification to avoid duplicate notifications
            notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());

            if (Long.compare(notificationDetails.getBaseInfo().getTimeStamp(), 0) == 0) {
                //we set time stamp as received timestamp only if it is not populated from payload
                notificationDetails.getBaseInfo().setTimeStamp(receivedTimeStamp);
            }
            notificationDetails.setLayoutType(notificationDetails.getBaseInfo().getLayoutType());
            notificationDetails.setSectionType(notificationDetails.getBaseInfo().getSectionType());
        }

        return notificationDetails;
    }

    public static TVNavModel parseTVNotificationMessage(Bundle extras, long receivedTimeStamp) {

        TVNavModel notificationDetails = new TVNavModel();
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        // currently we get entire notification params in message key and Imagelink in imageLink key
        // ignoring other default params
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;

            if (key.equals(NotificationConstants.MESSAGE_V2)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Gson gson = new Gson();

                    notificationDetails = gson.fromJson(value, TVNavModel.class);

                    if (null != notificationDetails.getBaseInfo()) {
                        if (!CommonUtils.isEmpty(notificationDetails.getBaseInfo().getBigImageLink()) || !CommonUtils
                                .isEmpty(notificationDetails.getBaseInfo().getBigImageLinkV2())) {
                            notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                                    .NOTIFICATION_TYPE_BIG_PICTURE);
                        } else {
                            notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                                    .NOTIFICATION_TYPE_SMALL);
                        }
                    }

                    notificationDetails.setDate(System.currentTimeMillis());
                    notificationDetails.getBaseInfo().setSectionType(
                            NotificationSectionType.TV);
                    //notificationDetails.setsType();
                    //notificationDetails.getBaseInfo().setsType(NotificationSectionType.TV.name());
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }

            }
        }
        // Below code is to get unique id for notification to avoid duplicate notifications

        try {

            NavigationType type = NavigationType.fromIndex(Integer.parseInt
                    (notificationDetails.getsType()));
            if (type == NavigationType.TYPE_TV_OPEN_TO_DETAIL) {
                if (!CommonUtils.isEmpty(notificationDetails.getUnitId())) {
                    notificationDetails.getBaseInfo().setUniqueId(Integer.parseInt(notificationDetails
                            .getUnitId()));
                } else {
                    notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());
                }
            } else {
                notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());
            }

        } catch (Exception e) {
            notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());
        }

        if (notificationDetails.getBaseInfo() != null &&
                Long.compare(notificationDetails.getBaseInfo().getTimeStamp(), 0) == 0) {
            //we set time stamp as received timestamp only if it is not populated from payload
            notificationDetails.getBaseInfo().setTimeStamp(receivedTimeStamp);
        }
        notificationDetails.setLayoutType(notificationDetails.getBaseInfo().getLayoutType());
        notificationDetails.setSectionType(notificationDetails.getBaseInfo().getSectionType());

        return notificationDetails;
    }

    /**
     * A utility method for parsing Sticky notifications.
     * This method will returns all the types of sticky notifications that it does not understand.
     *
     * @param extras
     * @return
     */
    @Nullable
    public static StickyNavModel parseStickyNotificationMessage(Bundle extras) {

        if (extras == null || extras.keySet() == null) {
            return null;
        }

        StickyNavModel stickyNavModel = new StickyNavModel();
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;
            if (CommonUtils.equalsIgnoreCase(key, NotificationConstants.MESSAGE_V3)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    Type type =
                            new TypeToken<StickyNavModel<CricketNotificationAsset, CricketDataStreamAsset>>() {
                            }.getType();
                    stickyNavModel = gson.fromJson(value, type);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                }
                break;
            }
        }

        if (stickyNavModel == null || stickyNavModel.getBaseInfo() == null ||
                !StickyNavModelType.CRICKET.getStickyType().equals(stickyNavModel.getStickyType())) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.INVALID, "Either sticky notification is null or the sticky " +
                            "notification type is not cricket");
            return null;
        }

        stickyNavModel.getBaseInfo().setType(extras.getString(NotificationConstants.TYPE));
        stickyNavModel.getBaseInfo().setSubType(stickyNavModel.getStickyType());

        BaseNotificationAsset baseNotificationAsset = stickyNavModel.getBaseNotificationAsset();
        if (baseNotificationAsset == null || CommonUtils.isEmpty(baseNotificationAsset.getId())) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.INVALID, "Either baseNotificationAsset is null or " +
                            "id of the match is 0");
            return null;
        }

        stickyNavModel.getBaseInfo().setUniqueId((baseNotificationAsset.getType() + baseNotificationAsset.getId()).hashCode());
        return stickyNavModel;
    }


    public static NewsNavModel parseNewsNotificationMessage(Bundle extras, long timeStamp) {

        NewsNavModel notificationDetails = new NewsNavModel();
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V2)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Gson gson = new Gson();
                    notificationDetails = gson.fromJson(value, NewsNavModel.class);
                    notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                            .NOTIFICATION_TYPE_SMALL);
                    notificationDetails.getBaseInfo().setSectionType(
                            NotificationSectionType.NEWS);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }
            }
        }
        // Below code is to get unique id for notification to avoid duplicate notifications
        try {
            notificationDetails.getBaseInfo().setUniqueId(Integer.parseInt(notificationDetails
                    .getNewsId()));
        } catch (Exception e) {
            notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());
        }
        notificationDetails.getBaseInfo().setTimeStamp(timeStamp);
        return notificationDetails;
    }

    public static FlushNavModel parseFlushNotificationMessage(Bundle extras) {
        FlushNavModel flushNavModel = new FlushNavModel();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V5)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    flushNavModel = gson.fromJson(value, FlushNavModel.class);
                    NotificationUtils.setLayoutType(flushNavModel.getBaseInfo());
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                    return null;
                }
            }
        }
        return flushNavModel;
    }

    public static AdjunctLangNavModel parseAdjunctLangNotificationMessage(Bundle extras,String msgType) {
        AdjunctLangNavModel adjunctLangNavModel = new AdjunctLangNavModel();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V5)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    adjunctLangNavModel = gson.fromJson(value, AdjunctLangNavModel.class);
                    if(msgType.equals(NOTIFICATION_TYPE_FLUSH_BLACKLIST_LANGUAGE)) {
                        adjunctLangNavModel.getBaseInfo().setLanguage(Constants.ALL_CHARACTER);
                    }
                    NotificationUtils.setLayoutType(adjunctLangNavModel.getBaseInfo());
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                    return null;
                }
            }
        }
        return adjunctLangNavModel;
    }

    public static AdjunctLangStickyNavModel parseAdjunctLangStickyNotificationMessage(Bundle extras) {
        AdjunctLangStickyNavModel adjunctLangNavModel = new AdjunctLangStickyNavModel();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V6)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    adjunctLangNavModel = gson.fromJson(value, AdjunctLangStickyNavModel.class);
                    NotificationUtils.setLayoutType(adjunctLangNavModel.getBaseInfo());
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                    return null;
                }
            }
        }
        return adjunctLangNavModel;
    }

    public static InAppNotificationModel parseInAppNotificationMessage(Bundle extras) {
        InAppNotificationModel inAppNotificationModel = new InAppNotificationModel();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V6)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    inAppNotificationModel = gson.fromJson(value, InAppNotificationModel.class);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                    return null;
                }
            }
        }
        return inAppNotificationModel;
    }



    public static DeeplinkModel parseDeeplinkNotificationMessage(Bundle extras) {


        DeeplinkModel notificationDetails = new DeeplinkModel();
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V3)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value);
                    Gson gson = new Gson();
                    notificationDetails = gson.fromJson(value, DeeplinkModel.class);
                    NotificationUtils.setLayoutType(notificationDetails.getBaseInfo());
                } catch (Exception e) {
                    Logger.caughtException(e);
                    String payload = JsonUtils.getJsonString(extras);
                    NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                            NotificationFilterType.INVALID,
                            NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
                    return null;
                }
            }
        }
        String deeplinkUrl = notificationDetails.getDeeplinkUrl();
        if (!CommonUtils.isEmpty(deeplinkUrl)) {
            notificationDetails.getBaseInfo().setUniqueId(deeplinkUrl.hashCode());
        } else {
            notificationDetails.getBaseInfo().setUniqueId((int) System.currentTimeMillis());
        }
        return notificationDetails;
    }

    public static void parseDetails(String message, NavigationModel details) {

        if (message == null) {
            return;
        }

        if (message.equals(NotificationConstants.EMPTY_STRING)) {
            return;
        }
        message = message.substring(message.indexOf(NotificationConstants.APP_IDENTIFIER) +
                NotificationConstants.APP_IDENTIFIER.length(), message.length());
        StringTokenizer tokenizer = new StringTokenizer(message,
                NotificationConstants.PARAM_DELIMITER);

        parseNotificationMessageHeader(tokenizer, details);
        updateNotificationIdAndTimeStamp(message, NotificationConstants.PARAM_DELIMITER, details);

        NavigationType type = NavigationType.fromIndex(Integer.parseInt
                (details.getsType()));
        if (type == null) {
            return;
        }

        switch (type) {
            case TYPE_OPEN_APP:
                parseNotificationOfTypeAppOpen(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.APP);
                break;
            case TYPE_OPEN_BOOKHOME:
                parseNotificationOfTypeBooksHome(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.BOOKS);
                break;
            case TYPE_OPEN_BOOKDETAILS:
            case TYPE_OPEN_BOOK_PAYMENT:
                parseNotificationOfTypeOpenBookDetailOrPayment(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.BOOKS);
                break;
            case TYPE_OPEN_BOOKLIST:
                parseNotificationOfTypeOpenBookList(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.BOOKS);
                break;
            case TYPE_OPEN_MYLIBRARY:
                parseNotificationOfTypeOpenToLibrary(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.BOOKS);
                break;
            case TYPE_OPEN_NEWS_LIST:
                parseNotificationOfTypeOpenToNewsList(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.NEWS);
                break;
            case TYPE_OPEN_NEWS_LIST_CATEGORY:
                parseNotificationOfTypeOpenToNewsListCategory(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.NEWS);
                break;
            case TYPE_OPEN_NEWSITEM:
                parseNotificationOfTypeOpenToNewsItem(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.NEWS);
                break;
            case TYPE_OPEN_CART:
                parseNotificationOfTypeOPenToCart(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.BOOKS);
                break;

            case TYPE_OPEN_TOPIC:
                parseNotificationOfTypeOpenToTopic(tokenizer, details);
                details.setNotificationSectionType(NotificationSectionType.NEWS);
                break;

        }
    }

    private static void updateNotificationIdAndTimeStamp(String message,
                                                         String notificationParamDelimiter,
                                                         NavigationModel details) {
        String[] notificationParts = message.split(notificationParamDelimiter);
        for (String notificationPart : notificationParts) {
            if (notificationPart.startsWith(NotificationConstants.NOTIFICATION_ID_PREFIX)) {
                String id = notificationPart.substring(
                        NotificationConstants.NOTIFICATION_ID_PREFIX.length());
                details.setId(id);
                try {
                    //Updating timeStamp with notification sent time from payload
                    long timeStamp = Long.parseLong(id);
                    details.setTimeStamp(timeStamp);
                } catch (NumberFormatException e) {
                    //TimeStamp is set immediately on receive, here we are overwriting it, So incase of
                    // exception no need to set any default value.
                    Logger.caughtException(e);
                }
                break;
            }
        }
    }

    /**
     * Parse message till the messageType
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationMessageHeader(StringTokenizer tokenizer,
                                                      NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            try {
                //Ignoring the expiry value which we are getting by V1 message format which is ~ seperated.
                Long.parseLong(tokenizer.nextToken());
            } catch (Exception e) {
                Logger.caughtException(e);
            }
        }

        if (tokenizer.hasMoreElements()) {
            details.setsType(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setMsg(tokenizer.nextToken());
        }
    }

    /**
     * Method used to parse notification message of type book home
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeBooksHome(StringTokenizer tokenizer,
                                                        NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setBookLanguage(tokenizer.nextToken());
        }
        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }
        if (tokenizer.hasMoreElements()) {
            try {
                tokenizer.nextToken();
            } catch (Exception e) {
                Logger.caughtException(e);
            }
        }
        parseUnicodeMsg(tokenizer, details);
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }
    }

    /**
     * Method used to parse notification message of type app open
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeAppOpen(StringTokenizer tokenizer,
                                                      NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    /**
     * Method used to parse notification message of type open to cart
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOPenToCart(StringTokenizer tokenizer,
                                                         NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    /**
     * Method used to parse notification message of type open BookDetail Or Payment
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenBookDetailOrPayment(StringTokenizer tokenizer,
                                                                      NavigationModel details) {

        if (tokenizer.hasMoreElements()) {
            details.setBookId(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }
        parseUnicodeMsg(tokenizer, details);
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }
    }

    /**
     * Used for parsing Unicode msg from the GCM message.
     *
     * @param tokenizer : Message
     * @param details   : Our object
     */

    public static void parseUnicodeMsg(StringTokenizer tokenizer,
                                       NavigationModel details) {
        if (tokenizer.hasMoreElements()) {

            details.setUniMsg(tokenizer.nextToken());
            if (!CommonUtils.isEmpty(details.getUniMsg())) {
                try {

                    String message = new String(details.getUniMsg().getBytes(),
                            "UTF-8");
                    details.setUniMsg(FontHelper.getFontConvertedString(message));
                } catch (Exception e) {
                }
            }
        }
    }


    /**
     * Method used to parse notification message of type open to Booklist
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenBookList(StringTokenizer tokenizer,
                                                           NavigationModel details) {

        if (tokenizer.hasMoreElements()) {
            details.setBookListId(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setBookLanguage(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        parseUnicodeMsg(tokenizer, details);

        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    /**
     * Method used to parse notification message of type open to library
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenToLibrary(StringTokenizer tokenizer,
                                                            NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        parseUnicodeMsg(tokenizer, details);
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    /**
     * Method used to parse notification message of type open to news topic
     *
     * @param tokenizer
     * @param details
     */
    private static void parseNotificationOfTypeOpenToTopic(StringTokenizer tokenizer,
                                                           NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setTopicKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }
    }

    /**
     * Method used to parse notification message of type open to news list
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenToNewsList(StringTokenizer tokenizer,
                                                             NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setfKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNpKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }


    /**
     * Method used to parse notification message of type open to news list category
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenToNewsListCategory(StringTokenizer tokenizer,
                                                                     NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setfKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNpKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setCtKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }
        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    /**
     * Method used to parse notification message of type open to news item
     *
     * @param tokenizer
     * @param details
     */
    public static void parseNotificationOfTypeOpenToNewsItem(StringTokenizer tokenizer,
                                                             NavigationModel details) {
        if (tokenizer.hasMoreElements()) {
            details.setfKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNpKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setCtKey(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNewsId(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setNotifySrc(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            tokenizer.nextToken();
        }

        if (tokenizer.hasMoreElements()) {
            details.setUniMsg(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setGroupType(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setChannelId(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setChannelGroupId(tokenizer.nextToken());
        }

        if (tokenizer.hasMoreElements()) {
            details.setPromoId(tokenizer.nextToken());
        }

    }

    public static SilentNotificationModel parseSilentNotification(Bundle extras, long timestamp){
        SilentNotificationModel notificationDetails = null;
        Set<String> keys = extras.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V6)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Gson gson = new Gson();
                    notificationDetails = gson.fromJson(value, SilentNotificationModel.class);
                    notificationDetails.getBaseInfo().setLayoutType(NotificationLayoutType
                        .NOTIFICATION_TYPE_SILENT);
                    notificationDetails.getBaseInfo().setSectionType(
                        NotificationSectionType.SILENT);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }
            }
        }

        return notificationDetails;
    }

    public static SilentVersionedApiUpdateModel parseSilentVersionedApiUpdateModel(Bundle extras) {
        SilentVersionedApiUpdateModel notificationDetails = null;
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V6)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Type type = new TypeToken<SilentVersionedApiUpdateModel>() {}.getType();
                    notificationDetails = JsonUtils.fromJson(value, type);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }
            }
        }
        return notificationDetails;
    }

    public static SilentVersionedApiTriggerModel parseSilentVersionedApiTriggerModel(Bundle extras) {
        SilentVersionedApiTriggerModel notificationDetails = null;
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            String value;
            if (key.equals(NotificationConstants.MESSAGE_V6)) {
                try {
                    value = extras.getString(key);
                    value = URLDecoder.decode(value, NotificationConstants.ENCODING);
                    Type type = new TypeToken<SilentVersionedApiTriggerModel>() {}.getType();
                    notificationDetails = JsonUtils.fromJson(value, type);
                } catch (Exception e) {
                    Logger.caughtException(e);
                    return null;
                }
            }
        }
        return notificationDetails;
    }
}
