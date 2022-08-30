/*
 * Created by Rahul Ravindran at 26/9/19 7:08 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.pref;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * News specific preferences.
 *
 * @author shreyas.desai
 */
public enum NewsPreference implements SavedPreference {
    USER_PREF_FONT_SIZE("UserPrefFontSize", PreferenceType.NEWS),
    USER_PREF_TITLE_FONT_SIZE("UserPrefTitleFontSize", PreferenceType.NEWS),
    USER_PREF_FONT_PROGRESS("UserPrefFontProgress", PreferenceType.NEWS),
    NEWSPAGE_USER_VERSION("newspage_user_version", PreferenceType.NEWS),
    NEWSPAGE_SERVER_VERSION("newspage_server_version", PreferenceType.NEWS),
    NEWS_PAGE_SYNC_TIME("newspage_sync_time", PreferenceType.NEWS),
    NEWS_COMM_DISPLAY_EVENTS("news_communication_events_displayed", PreferenceType.NEWS),
    HEADLINES_URL("headlines_url", PreferenceType.NEWS),
    NEWSPAGE_PULL_LASTN("newspage_pull_lastn", PreferenceType.NEWS),
    NEWSPAGE_PULL_LAST_TIMES("newspage_pull_last_times", PreferenceType.NEWS),
    NEWS_LAUNCH_COACH_MARK("news_launch_coach_mark", PreferenceType.NEWS),
    NEWS_FIRST_CHUNK_CDN_PARAMS("news_first_chunk_cdn", PreferenceType.NEWS),
    STORY_PAGE_MAX_VIEWED_COUNT("story_page_max_viewed_count", PreferenceType.NEWS),
    STORY_PAGE_MIN_TIME_SPENT("story_page_min_time_spent", PreferenceType.NEWS),
    STORY_PAGE_IDS_RECENTLY_VIEWED("story_page_ids_recently_viewed", PreferenceType.NEWS),
    OPT_IN_OUT_CLIENT_STATE("opt_in_out_client_state", PreferenceType.NEWS),
    OPT_IN_OUT_SERVER_STATE("opt_in_out_server_state", PreferenceType.NEWS),
    APP_UPGRADE_PAGE_SYNC("app_upgrade_page_sync", PreferenceType.NEWS),
    IMAGE_DOWNLOAD_QUALITIES("image_download_qualities", PreferenceType.NEWS),
    BOOK_FILE_DELETE_COMPLETE("book_file_delete_complete", PreferenceType.NEWS),
    FEED_INBOX_REQUEST_KEY("feed_inbox_request_key", PreferenceType.NEWS),
    LAST_ACCESS_TAB_THRESHOLD("last_access_tab_threshold", PreferenceType.NEWS),
    LAST_DISLIKE_THRESHOLD("last_dislike_threshold", PreferenceType.NEWS),
    NEWS_PAGES_RECENTLY_VIEWED("news_pages_recently_viewed", PreferenceType.NEWS),
    RECENT_TAB_THRESHOLD_COUNT("recent_tab_threshold_count", PreferenceType.NEWS),
    RECENT_DISLIKE_THRESHOLD_COUNT("recent_dislike_threshold_count", PreferenceType.NEWS),
    NO_AUTO_REFRESH_ONTAB_RECREATE_SEC("no_auto_refresh_ontab_recreate_sec", PreferenceType.NEWS),
    DONOT_AUTOFETCH_SWIPEURL("doNot_AutoFetch_SwipeUrl", PreferenceType.NEWS),
    SCROLL_CONDITIONS_FOR_RELATED_STORIES("scroll_conditions_for_related_stories", PreferenceType.NEWS),
    TIMESPENT_CONDITIONS_FOR_RELATED_STORIES("timepsent_conditions_for_related_stories", PreferenceType.NEWS);

    private String name;
    private PreferenceType preferenceType;

    NewsPreference(String name, PreferenceType preferenceType) {
        this.name = name;
        this.preferenceType = preferenceType;
    }

    @Override
    public PreferenceType getPreferenceType() {
        return preferenceType;
    }

    @Override
    public String getName() {
        return name;
    }
}
