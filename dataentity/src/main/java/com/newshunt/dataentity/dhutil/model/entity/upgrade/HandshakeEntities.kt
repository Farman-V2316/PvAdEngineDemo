package com.newshunt.dataentity.dhutil.model.entity.upgrade

import com.google.gson.annotations.SerializedName
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Constants.DEFAULT_MAX_PROMPT_PER_UPDATE
import com.newshunt.dataentity.common.follow.entity.FollowBlockLangConfig
import com.newshunt.dataentity.common.follow.entity.FollowSnackBarEntity
import com.newshunt.dataentity.common.model.entity.model.DomainCookieInfo
import com.newshunt.dataentity.common.model.entity.model.TimeoutValues
import com.newshunt.dataentity.common.pages.FollowFilter
import com.newshunt.dataentity.dhutil.model.entity.EntityConfiguration
import com.newshunt.dataentity.dhutil.model.entity.NonLinearConfigurations
import com.newshunt.dataentity.dhutil.model.entity.PerspectiveThresholds
import com.newshunt.dataentity.dhutil.model.entity.WakeUpPartnerAppInformationConfig
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerExistence
import com.newshunt.dataentity.dhutil.model.entity.baseurl.BaseUrl
import com.newshunt.dataentity.dhutil.model.entity.status.Version
import com.newshunt.dataentity.model.entity.CSConfig
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import java.io.Serializable

enum class Upgrade {
	LATEST, OPTIONAL, MANDATORY
}

data class HandshakeEntity(val clientId: String? = null,
						   val upgrade: Upgrade? = null,
						   val version: Version? = null,
						   val baseUrl: BaseUrl? = null,
						   val dh2DhReInstall: Boolean = false,
						   val latestAppVersion: String,
						   val encryptionKeyPath: String,
						   val encryptionKeyVersion: Int,
						   val isAstroSubscribed: Boolean = false,
						   var newUid: String? = null,
						   val cookieInfo: Map<String, String>? = null,
						   val languageColdStartCardTimesToShow: Int = 0,
						   val languageColdStartCardPosition: Int = 0,
						   val userResponseData: UserLoginResponse? = null,
						   val preloadPages: String? = null,
						   val cookieDomainsToBeCleared: List<DomainCookieInfo>? = null,
						   val acqTypeV1: String? = null,
						   val handshakeDefaultInterval: Long? = null,
						   val shareToken: String? = null,
						   val appsFlyerExistence: String = AppsFlyerExistence.DISABLED.existence,
						   val enableGzipOnPost: Boolean? = true,
						   //Min version below which mandatory update will kick in
						   val maxVersionForMandatoryUpdateV2: Int = 0,
						   //Min version below which flexible update will kick in
						   val maxVersionForFlexibleUpdateV2: Int = 0,
						   val regnResponseString: String? = null,
						   val userSeg: Map<String, String>? = emptyMap(),
						   @SerializedName("langUpdate")
						   val langUpdateInfo: LangUpdateInfo? = null,
						   val newsStickyParams: Map<String, String>? = emptyMap(),
                           val serverLocation: String?= null)

/**
 * NOTE : ALWAYS ADD DEFAULT VALUES TO PARAMS
 */
data class HandshakeConfigEntity(val version: String? = null,
								 val imageSetting: ImageReplacementSetting? = null,
								 val maxNotificationsInTray: Int = -1,
								 val rateConfig: RateConfig? = null,
								 val pullNotificationsEnabled: Boolean = true,
								 val firstTimePullDelay: Int = 0,
								 val performanceAnalyticsEnabled: Boolean = false,
								 val firstChunkRequestParams: Map<String, String> = emptyMap(),
								 val oldestStoryDisplayTimeGap: Long = 0,
								 val oldestListDisplayTimeGap: Long = 0,
								 val disableHandling408Response: Boolean = false,
								 val minTimeSpentOnViewedArticle: Long = 0,
								 val storyDetailErrorPageUrl: String? = null,
								 val shareFloatingIconType: String? = null,
								 val imageAspectRatio: Float = Constants.DEFAULT_IMG_AR,
								 val disableFirebasePerf: Boolean = false,
								 val softRelaunchDelay: Long = Constants.INVALID_TIME,
								 val hardRelaunchDelay: Long = Constants.INVALID_TIME,
								 val recentTabThreshold: Long = 0,
								 val recentDislikeThreshold: Long = 0,
								 val recentTabThresholdCount: Int = 20,
								 val recentDislikeThresholdCount: Int = 20,
								 val comScoreDelayInMilis: Long = 0,
								 val fireTrackFromCache: Boolean = false,
								 val fireComscoreFromCache: Boolean = false,
								 val startServiceForNotiImages: Boolean = false,
								 val shareBannerUrl: String? = null,
								 val shareText: String? = null,
								 val deepLinkPatternsToBeExcluded: Set<String> = emptySet(),
								 val bottomBarFixed: Boolean = false,
								 val topBarFixed: Boolean = true,
								 val notificationSwipeUrlExpiryDelay: Int = 600,
								 val notificationPrefetchArticleContent: Boolean = false,
								 val notificationPrefetchImage: Boolean = false,
								 val notificationPrefetchSwipeUrl: Boolean = false,
								 val notificationForegroundServiceDuration: Int = 4,
								 val timeouts: TimeoutValues? = null,
								 val urls: Urls? = null,
								 val noAutoRefreshOnTabRecreateSec: Int = Constants.DEFAULT_NO_AUTO_REFRESH_TAB_RECREATE,
								 val doNotAutoFetchSwipeUrl: Boolean = false,
								 val minVisibilityForAnimation: Int = 90,
								 val followSyncMinimumGap: Long = 0,
								 val savedSyncMinimumGap: Long = Constants.CONTACT_SYNC_FREQ_DEFAULT,
								 val isAutoCompleteDisabled: Boolean = false,
								 val minCharForAutoComplete: Int = 2,
								 val maxCharForAutoComplete: Int = Int.MAX_VALUE,
								 val enableSearchbar: Boolean = true,
								 val nonLinearFeedThresholds: NonLinearConfigurations? = null,
								 val perspectiveThresholds: PerspectiveThresholds? = null,
								 val maxVideoHeightRatio: Float = 1.0f,
								 val followEntity: FollowSnackBarEntity? = null,
								 val followAndBlockConfigs: List<FollowBlockLangConfig>? = null,
								 val maxNotificationsForSystemGrouping: Int = 0,
								 val durationForAudioCommentaryRefresh: Int = 0,
								 val profileConfig: ProfileConfig? = null,
								 val followFilters: List<FollowFilter>? = null,
								 val postCreationConfig: PostCreationConfig? = null,
								 val encryption: PublicEncryptionKey? = null,
								 val defaultNotificationText: String? = null,
								 val defaultNotificationDuration: Long = 0,
								 val disablePostingDummyNotification: Boolean = false,
								 val maxApiDelay: Long = Constants.DEFAULT_HTTP_CLIENT_TIMEOUT,
								 val timerPeriodInSeconds: Long = Constants.DEFALUT_TIMER_PERIOD_INSECONDS,
								 val maxErrorEventPerInterval: Long = Constants.MAX_ERROR_EVENT_PER_INTERVAL,
								 val disableErrorEvent: Boolean = false,
								 val autoplayInLite: Boolean = false,
								 val defaultAutoPlayPreference: Int = 0,
								 val showAutoPlaySettings: Boolean = false,
								 val termsSnackbarDurMs: Int = Constants.TERMS_SNACKBAR_SHOW_DELAY,
								 val entityConfiguration: EntityConfiguration? = null,
								 val idOfForYouPage: String = "",
								 val payloadRecentEntriesTimeLimit: Long = 600_000L, // in msec
								 val payloadRecentDislikesTimeLimit: Long = 600_000L,//in msec
								 val astroPageId: String? = null,
								 val csConfig: CSConfig? = null,
								 val fgSessionTimeout: Long = Constants.DEFAULT_FG_SESSION_TIMEOUT,
								 val includePublisherInShareText: Boolean = false,
								 val reportCommentPageUrl: String? = null,
								 val localCardTTL: Long? = Constants.DEFAULT_LOCAL_CARD_TTL, // in millsec
								 val pausedVideoEventDelayMs: Int = Constants.TIMESPENT_PAUSE_DELAY,
								 val cardViewVisibilityConfig: CardVisibilityConfig? = null,
								 val locationFetchInterval: Long? = null,
								 val notificationForegroundServiceFlags: Int = Constants.DEFAULT_NOTIFICATION_FG_FLAGS,
								 val notificationForegroundServiceStopDelay: Long = 2000,
								 val autoImmersiveTimeSpan: Long = 10000,
								 val autoImmersiveEnabled: Boolean = true,
								 val inAppUpdatesConfig: InAppUpdatesConfig? = null,
								 val enableDhFontsForManufacturers: List<String>? = null,
								 val langScreenType: String? = null,
								 val langScreenWaitSec: Long? = null,
								 val defaultCardSize: String? = null,
								 val preloadPages: String? = null,
								 val feedCacheDelay: Long = Constants.PREF_FEED_CACHE_DELAY_DEFAULT,/*millsec*/
								 val maximumStallPeriod: Int = 300000,
								 val nonPostingRetryDelay: Int = 900000,
								 val maximumAllowedRetriesForNotificationPrefetch: Int = 4,
								 val isNotificationPrefetchEnabled: Boolean = false,
								 val notificationFontSize: Float = 0.0f,
								 val isNotificationUngroupingEnabled: Boolean = false,
								 val maxUngroupedNotificationsInTray: Int = 50,
								 val wakeUpPartnerInformation: WakeUpPartnerAppInformationConfig? = null,
								 val wokenUpServiceForegroundNotificationDuration: Int = Constants.WOKEN_UP_SERVICE_FG_NOTIFICATION_DURATION,
								 val showInAppRatingFlow : Boolean = false,
								 val feedCardConfig: FeedCardConfig? = null,
								 val userSeg: Map<String, String>? = emptyMap(),
								 val faqsConfigUrl: String? = null,
								 val maximumNumberOfItemsInNewsSticky: Int = 5,
								 val showDisableNewsStickyForever: Boolean = true,
								 val newsStickyAutoScrollInterval: Int = 7000,
								 val notificationClearAllTimeThresholdMs: Int = Constants.NOTIFICATION_CLEAR_ALL_TIME_THRESHOLD,
								 val notificationClearAllCountThreshold: Int = Constants.NOTIFICATION_CLEAR_ALL_COUNT_THRESHOLD,
								 val defaultNewsStickyDisabledDays: Int = Constants.NEWS_STICKY_DISABLED_DEFAULT_DAYS_VALUE,
								 val initialVideoThumbnailDelayMS: Long = 10,
								 val videoThumbnailDelayMS: Long = 100,
								 val exoPlayerLoadDelayMS: Long = 100,
								 val otherPlayerLoadDelayMS: Long = 500,
								 val disableUniqueGroupForNotifications: Boolean = false,
								 val homeLoaderMinWaitTimeMs: Long = Constants.HOME_LOADER_MIN_WAIT_TIME_MS,
								 val homeLoaderMaxWaitTimeMs: Long = Constants.HOME_LOADER_MAX_WAIT_TIME_MS,
								 val inAppNotificationDisplayDurationMs: Long = Constants.IN_APP_DISPLAY_DURATION,
								 val workManagerInitAndSchedulingDelayMs: Long = Constants.WORK_MANAGER_INIT_AND_SCHEDULING_DELAY_MS,
								 val bwEstCfg : BwEstConfig,
                                 val collectionSecondItemVisiblePercentage: Int = Constants.COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE,
								 val minCollectionForRequest:Int = Constants.MIN_COLLECTION_FOR_REQUEST,
								 val showSourceLogoAtCardLevel: Boolean = false,
                                 val isTabsSwipeEnabled:Boolean = false,
								 val minMoreStoriesCount : Int = Constants.DEFAULT_MIN_MORE_STORIES_COUNT,
								 val maxMoreStoriesCount : Int = Constants.DEFAULT_MAX_MORE_STORIES_COUNT,
								 val detailCSS: String? = null)

data class CardVisibilityConfig(val viewVisibilityForSCV: Int,
								val screenVisibilityForSCV: Float,
								val viewVisibilityForTS: Int,
								val screenVisibilityForTS: Float)

/**
 * Data class to holding all configs wrt In App Updates
 */
data class InAppUpdatesConfig(//How often can the flexible update prompt be shown?
		val minGapBetweenAppUpdatePromptSecs: Long = Constants.DEFAULT_GAP_APP_UPDATE_PROMPTS,
		//How many video swipes before showing the flexible update prompt
		val spvCountInSessionForUpdatePrompt: Int = Constants.DEFAULT_SPV_APP_UPDATE_PROMPT,
		//For every update available, how many times do we nudge the user to update
		val maxPromptsPerUpdate: Int = DEFAULT_MAX_PROMPT_PER_UPDATE)

/**
 * Post body for the register success migration API
 */
data class RegistrationSuccessBody(val regnResponseString: String?)

data class RegisteredClientInfo(val newClient: Boolean)

data class FeedCardConfig(val iconsConfig: List<IconsList>?)

data class LangInfo(var userSelectedLang:String?,
					var sysSelectedLang:String?,
					var blackListedLang:String?)

data class AdjunctNotificationLangInfo(val langInfo: LangInfo?,
										val isUserToLangNotification:Boolean = false)

data class LangUpdateInfo(val langInfo: LangInfo? = null,
						  val handshakeKey:String? = null,
						  val timeStamp:Long? = null,
						  val updateType:String? = null):Serializable

enum class LangUpdateType {
	NOTIFICATION,USER;
}

enum class IconsList {
	WA_SHARE,
	COMMENT,
	REPOST,
	REACTION,
	SAVE
}