/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.app.analytics.MarkStoryCardClickUsecase
import com.newshunt.appview.R
import com.newshunt.appview.common.group.getPreferredApprovalTab
import com.newshunt.appview.common.postcreation.view.customview.PostDeleteDialog
import com.newshunt.appview.common.profile.helper.makeBookmarkApiPostBody
import com.newshunt.appview.common.profile.model.usecase.MarkInteractionDeletedUsecase
import com.newshunt.appview.common.profile.model.usecase.PostBookmarksUsecase
import com.newshunt.appview.common.ui.fragment.ImportFollowFragment
import com.newshunt.appview.common.ui.helper.CardClickEventHelper
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.viewholder.PerspectiveState
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.common.model.usecase.ToggleLikeUsecase
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.ColdStartEntityType
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.getSourceDeeplink
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.UserFollowView
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents
import com.newshunt.dataentity.dhutil.model.entity.upgrade.IconsList
import com.newshunt.dataentity.model.entity.ApprovalAction
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dataentity.news.model.entity.server.asset.CardLandingType
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dataentity.notification.FollowNavModel
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.deeplink.DeeplinkUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.RepostLiveEvent
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.disableMomentarily
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper
import com.newshunt.dhutil.helper.browser.NHBrowserUtil
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.take1
import com.newshunt.dhutil.toArrayList
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.LikeEmojiBindingUtils.Companion.showLikePopup
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.toMinimizedCommonAsset
import com.newshunt.news.model.usecase.AnswerPollUsecase
import com.newshunt.news.model.usecase.ClearLanguageSelectionCard
import com.newshunt.news.model.usecase.CloneFetchForNewsDetailUsecase
import com.newshunt.news.model.usecase.DeleteLocalCardUseCase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.usecase.OpenOtherPerspectiveUsecase
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.AuthOrchestrator
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.SSO
import java.io.Serializable
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Class to handle all card clicks
 *
 * @author satosh.dhanyamraju
 */
class CardClickDelegate @Inject constructor(private val entityId: String,
                                            private val postId: String,
                                            private val pageEntity: PageEntity?,
                                            private val sourceId: String?,
                                            private val sourceType: String?,
                                            private val timeSpentEventId: Long,
                                            private val toggleFollowUseCase: MediatorUsecase<Bundle, Boolean>,
                                            private val toggleLikeUsecase: MediatorUsecase<Bundle, Boolean>,
                                            private val shareUsecase: MediatorUsecase<Bundle, Boolean>,
                                            private val answerPollUsecase: MediatorUsecase<Bundle, String>,
                                            private val approvalActionMediatorUC: MediatorUsecase<ReviewActionBody, Boolean>,
                                            private val cloneFetchForNewsDetailUsecase: Provider<CloneFetchForNewsDetailUsecase>,
                                            private val joinGroupUsecase: MediatorUsecase<GroupBaseInfo, GroupInfo>,
                                            private val deleteBookmarkUsecase: MediatorUsecase<BookmarkList, Boolean>,
                                            private val openCollectionUsecase: MediatorUsecase<Bundle, String?>,
                                            private val lifecycleOwner: LifecycleOwner,
                                            private val section: String,
                                            private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>,
                                            private val location: String,
                                            private val markInteractionDeletedUC:
                                            MediatorUsecase<String, Boolean>,
                                            private val listType: String?,
                                            private val searchPayloadContext: SearchQuery?,
                                            private val auth: AuthOrchestrator,
                                            private val menuListLocation: MenuLocation,
                                            private val groupInfo: GroupInfo?,
                                            private val clearLanguageSelectionCard: MediatorUsecase<Bundle, Any>,
                                            private val isForyouPage: Boolean,
                                            private val deleteLocalCardUseCase: MediatorUsecase<Bundle, Boolean>,
                                            private val isMyPostsPage: Boolean,
                                            @Named("incrementViewcountUsecase")
                                            private val incrementViewcountUsecase: MediatorUsecase<Bundle, Boolean>,
                                            private val terminateNudgeUsecase: MediatorUsecase<CardNudgeTerminateType, Boolean>
                                            )
    : ClickDelegate {

    var referrer: PageReferrer? = null
    var referrerFlow: PageReferrer? = null
    var fragmentBundle: Bundle? = null
    var referrerProviderListener: ReferrerProviderlistener?=null
    var uniqueId: Int = -1
    var isInDetail: Boolean = false
    var lastKnownOPFetchId: String? = null

    override fun onAutoPlayVideoClick(view: View, item: CommonAsset?, parent: CommonAsset?, videoPlayerProvider: VideoPlayerProvider?, contentAdDelegate: ContentAdDelegate?) {
        Logger.d("cardsViewModel", "onViewClick1 " + videoPlayerProvider)
        videoPlayerProvider?.onAutoPlayCardClick()
        parent?.let {
            contentAdDelegate?.onCardClick()
            onCollectionCardClick(view, item, parent, videoPlayerProvider)
            return
        }
        item?.let {
            onClick(view, item, videoPlayerProvider, contentAdDelegate = contentAdDelegate)
        }
    }

    override fun onViewClick(view: View, item: Any) {
        Logger.d("cardsViewModel", "view clicked $item")
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        onViewClick(view, item, args, null)
    }

    override fun onViewClick(view: View) {
        when (view.id) {
            R.id.action_create_post -> {
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(CommonNavigator
                        .getPostCreationIntent(null, null, null, referrer), callback = null))
            }

            R.id.action_home -> {
                val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
                val navigationIntent = NavigationEvent(CommonNavigator.getNewsHomeIntent(view.context,
                        false, prevNewsAppSection?.id, prevNewsAppSection?.appSectionEntityKey,
                        null,
                        false))
                NavigationHelper.navigationLiveData.postValue(navigationIntent)
            }
            else -> {
                // DO NOTHING
            }
        }
    }

    override fun onClickPerspective(view: View, item: Any, state: PerspectiveState) {
        state.collapsed = !state.collapsed
        if (!state.collapsed) {
            AnalyticsHelper2.logExploreButtonClickEvent(referrer, NewsExploreButtonType.OTHER_PERSPECTIVE, section)
        }

    }

    override fun onOpenPerspective(view: View, item: Any, parentId: String, childId: String,
                                   section: String, pageReferrer: PageReferrer?) {
        openCollectionUsecase.execute(bundleOf(Constants.BUNDLE_POST_ID to parentId,
                Constants.BUNDLE_LOCATION_ID to section))
        openCollectionUsecase.data().observe(lifecycleOwner, Observer {
            if (it.isSuccess && it.getOrNull() != null && it.getOrNull() != lastKnownOPFetchId) {
                lastKnownOPFetchId = it.getOrNull()
                openCollectionUsecase.data().removeObservers(lifecycleOwner)
                val fetchId = it.getOrNull()!!
                val intent = Intent()
                intent.action = Constants.OP_DETAIL_ACTION
                intent.putExtra(Constants.PARENT_STORY_ID, parentId)
                intent.putExtra(Constants.STORY_ID, childId)
                intent.putExtra(Constants.PAGE_ID, fetchId)
                intent.putExtra(Constants.LOCATION, fetchId)
                if (!isInDetail) {
                    intent.putExtra(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
                }
                intent.putExtra(NewsConstants.DH_SECTION, section)
                intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
            } else {
                Logger.e(TAG, "Error opening other perspective story", it.exceptionOrNull())
            }
        })
    }

    private fun onClick(view: View, item: Any,
                        videoPlayerProvider: VideoPlayerProvider? = null,
                        args: Bundle? = null, contentAdDelegate: ContentAdDelegate? = null) {
        NhAnalyticsAppState.getInstance().setAction(NhAnalyticsUserAction.CLICK)
        Logger.d("cardsViewModel", "view clicked $item" + "onViewClick3 - " + videoPlayerProvider)
        if ((item as? CommonAsset)?.i_format() == Format.LOCAL) {
            when {
                view.id == R.id.post_discard -> {
                    discardLocalCard(view, item)
                }
            }
            return
        }
        if (item is BaseError) {
            val retry = CommonUtils.getString(com.newshunt.dhutil.R.string.dialog_button_retry)
            val home = CommonUtils.getString(com.newshunt.dhutil.R.string.btn_home)
            val settings = CommonUtils.getString(com.newshunt.dhutil.R.string.action_settings)
            val unblock = CommonUtils.getString(com.newshunt.dhutil.R.string.action_unblock)

            when {
                view.id == R.id.error_action -> {
                    val actionText = view as NHTextView
                    val actionOriginalText = actionText.originalText
                    when (actionOriginalText) {
                        retry -> fpUsecase.execute(Bundle())
                        home -> {
                            val foryouId = PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, Constants.EMPTY_STRING)
                            if (foryouId == entityId) {
                                fpUsecase.execute(Bundle())
                                return
                            }
                            val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.fromName(section))
                            val navigationIntent = NavigationEvent(CommonNavigator.getNewsHomeIntent(view.context,
                                    false, prevNewsAppSection?.id, null,
                                    null,
                                    false))
                            NavigationHelper.navigationLiveData.postValue(navigationIntent)
                        }
                        settings -> {
                            val nwSettingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            view.context.startActivity(nwSettingIntent)
                        }
                        unblock -> {
                            val intent = Intent()
                            intent.action = DHConstants.OPEN_FOLLOW_ENTITIES_SCREEN
                            intent.setPackage(AppConfig.getInstance().packageName)
                            intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER,
                                    PageReferrer(NhGenericReferrer.BLOCKED_SOURCES_NO_FEED_ITEMS_ERROR))
                            val followNavModel = FollowNavModel(null, null, null, null,
                                    FollowModel.BLOCKED)
                            intent.putExtra(NewsConstants.BUNDLE_OPEN_FOLLOWED_ENTITY, followNavModel)
                            intent.putExtra(Constants.BUNDLE_FOLLOW_MODEL, FollowModel.BLOCKED.name)
                            view.context.startActivity(intent)
                        }
                    }
                }
            }
        }
        if (item is ReviewActionBody) {
            when (view.id) {
                R.id.positive_approve_btn -> handleApproveAction(item)
                R.id.negative_approve_btn -> handleDeclineAction(item)
            }
            return
        } else if(item is Member) {
            when {
                view.id == R.id.member_info_card -> {
                    CommonNavigator.launchProfileActivity(view.context,
                            UserBaseProfile().apply { userId = item.userId },
                            referrerProviderListener?.latestPageReferrer)
                }
            }
            return
        }
        else if (item is GroupInfo) {
            when (view.id) {
                R.id.invite_card_btn -> {
                    NavigationHelper.navigationLiveData.postValue(NavigationEvent(CommonNavigator
                            .getGroupInvitationIntent(item,
                                    PageReferrer(NhGenericReferrer.GROUP_HOME)), callback = null))
                }
                R.id.group_info_card -> {
                    CommonNavigator.launchGroupDetailActivity(view.context, GroupBaseInfo().apply {
                        this.id = item.id
                    }, referrerProviderListener?.latestPageReferrer, null)
                }
            }
            return
        } else if (item !is CommonAsset) {
            Logger.e(LOG_TAG, "data is not a common asset item : $item")
            return
        }
        /*
        - full card
        - 3dots (class delegate?)
        - follow-button
        - like
        - comment
        - collection-item
        - collection view-more
        - language-card
        - next-retry-button
         */

        /*
        - full card
        - 3dots (class delegate?)
        - follow-button
        - like
        - comment
        - collection-item
        - collection view-more
        - language-card
        - next-retry-button
         */

        when {
            view.id == R.id.language_card -> {
                val usecaseArguments = args ?: Bundle()
                usecaseArguments.putSerializable(Constants.BUNDLE_POST_ID, Constants.LANGUAGE_SELECT_FEED_ITEM_ID)
                clearLanguageSelectionCard.execute(usecaseArguments)
                val intent = Intent()
                intent.action = Constants.ONBOARDING_ACTIVITY_OPEN_ACTION
                intent.`package` = AppConfig.getInstance().packageName
                intent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, true)
                intent.putExtra(NewsConstants.BUNDLE_LAUNCHED_FROM_LANGUAGE_CARD, true)
                val event = NavigationEvent(intent)
                NavigationHelper.navigationLiveData.postValue(event)
            }
            view.id == R.id.save_language_selection -> {
                val usecaseArguments = args ?: Bundle()
                usecaseArguments.putSerializable(Constants.BUNDLE_POST_ID, Constants.LANGUAGE_SELECT_FEED_ITEM_ID)
                clearLanguageSelectionCard.execute(usecaseArguments)
            }
            view.id == R.id.carousel_view_more -> {
                Logger.i(LOG_TAG, "Carousel view more clicked")
                onCollectionCardClick(view, null, item, videoPlayerProvider, true)
            }
            view.id == R.id.follow_button -> {
                val fragmentCommunicationVM = (view.context as? FragmentActivity)?.let {
                    ViewModelProviders.of(it).get(FragmentCommunicationsViewModel::class.java)
                }
                if (item is UserFollowView) {
                    val actionableEntity = item.actionableEntity
                    postExplicitFollowCarouselEvent(fragmentCommunicationVM,item)
                    toggleFollowUseCase.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to actionableEntity))
                    AnalyticsHelper2.logFollowButtonClickEvent(item.actionableEntity, referrer, item.isFollowing.not(), section)
                    if (fragmentBundle?.getBoolean(Constants.BUNDLE_IS_IMPORT_CONTACTS_FRAGMENT) == true) {
                        ImportFollowFragment.onFollowChange(item.i_id(), item.isFollowing.not())
                    }
                    return
                }

                val source = item.i_source() ?: run { Logger.e(TAG, "source missing"); return }
                val id = source.id ?: run { Logger.e(TAG, "source id  missing"); return }
                val type = source.entityType
                        ?: run { Logger.e(TAG, "source type   missing"); return }
                val entity = ActionableEntity(entityId = id,
                        entityType = type,
                        entitySubType = source.type,
                        displayName = source.displayName ?: Constants.EMPTY_STRING,
                        entityImageUrl = source.entityImageUrl ?: Constants.EMPTY_STRING,
                        iconUrl = source.icon ?: Constants.EMPTY_STRING,
                        deeplinkUrl = source.deeplinkUrl ?: Constants.EMPTY_STRING,
                        nameEnglish = source.nameEnglish)

                postExplicitFollowCarouselEvent(fragmentCommunicationVM,item)
                AnalyticsHelper2.logFollowButtonClickEvent(entity, referrer, item.i_isFollowin()?.not()
                        ?: true, section)
                toggleFollowUseCase.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to entity))
            }

            view.id == R.id.share_count_tv -> {
                shareButtonClicked(view, item)
            }
            view.id == R.id.repost_icon_tv || view.id == R.id.see_all_repost || view.id == R.id.see_all_repost_empty -> {
                repostButtonClicked(view, item)
            }
            view.id == R.id.comment_count_tv -> {
                commentButtonClicked(view, item,true) // This is called from detail page only.
            }
            view.id == R.id.icon_1 || view.id == R.id.icon_2 -> {
                listIconClicked(view, item)
            }
            view.id == R.id.block_frame -> {
                if (item is UserFollowView) {
                    val entity = item.actionableEntity
                    toggleFollowUseCase.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to entity,
                            ToggleFollowUseCase.B_ACTION to FollowActionType.BLOCK.name))
                }
            }

            view.id == R.id.nsfw_filter_text_button -> {
                AnalyticsHelper2.logVHNSFWAcceptanceEvent((item as? CommonAsset)?.i_id(), section);
                PreferenceManager.savePreference(GenericAppStatePreference.SHOW_NSFW_FILTER,
                        false)
            }
            view.id == R.id.approval_card_rootview -> {
                val pageReferrer = (view.context as? ReferrerProviderlistener)?.latestPageReferrer
                CommonNavigator.launchApprovalsActivity(view.context, getPreferredApprovalTab
                (item), PageReferrer(referrer))
                val counts = item.i_counts()?.TOTAL_PENDING_APPROVALS?.value
                AnalyticsHelper2.logApprovalCardClickEvent(pageReferrer,
                        NewsExploreButtonType.APPROVAL_CARD, item.i_id(), counts = counts)
            }
            view.id == R.id.poll_option_1 -> answerPoll(view.context, item, 0)
            view.id == R.id.poll_option_2 -> answerPoll(view.context, item, 1)
            view.id == R.id.poll_option_3 -> answerPoll(view.context, item, 2)
            view.id == R.id.poll_option_4 -> answerPoll(view.context, item, 3)
            view.id == R.id.og_post_body -> handleOgClick(view.context, item)

            view.id == R.id.topic_card_image -> {
                CommonNavigator.launchDeeplink(view.context, item.i_deeplinkUrl(), PageReferrer
                (NewsReferrer.NEWS_HOME))
            }


            view.id == R.id.interaction_rootview ||
                    view.id == R.id.user_interaction_delete -> {
                handleUserInteractionClicks(view, item, view.id)
            }
            view.id == R.id.saved_stories_small_carousel_rootview ||
                    view.id == R.id.saved_story_small_view ||
                    view.id == R.id.video_normal_rootview ||
                    view.id == R.id.video_player_holder -> {
                CommonNavigator.launchInternalDeeplink(view.context, item.i_deeplinkUrl(),
                        PageReferrer(NewsReferrer.SAVED_ARTICLES), true, DefaultNavigatorCallback(),
                        bundleOf(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE to true))
            }
            view.id == R.id.story_delete || view.id == R.id.video_delete -> {
                deleteBookmarkUsecase.execute(makeBookmarkApiPostBody(item, BookMarkAction.DELETE))
            }
            view.id == R.id.follow_entity_item_rootview -> {
                if (item is UserFollowView) {
                    val deeplink = item.i_deeplinkUrl()
                    deeplink?.let {
                        val position = args?.getInt("position") ?: 0
                        val isFPV = fragmentBundle?.getBoolean(Constants.BUNDLE_IS_FPV) ?: true
                        val model = fragmentBundle?.getString(Constants.BUNDLE_FOLLOW_MODEL)
                                ?: FollowModel.FOLLOWING.name
                        AnalyticsHelper2.logEntityCardClick(item.actionableEntity, position, isFPV, model, section, referrer)
                        CommonNavigator.launchInternalDeeplink(view.context, it,
                                PageReferrer(NewsReferrer.CS_FOLLOWING), true, DefaultNavigatorCallback())
                    }
                }
            }

            view.id == R.id.isfavorite_container -> {
                if (item is UserFollowView) {
                    val state = item.isFavorite
                    /*TODO : execute toggle page selection usecase*/
                    Logger.d(LOG_TAG, "User favourite toggle here $state")
                }
            }

            view.id == R.id.profile_picture -> {
                val id = item.i_source()?.id
                id?.let {
                    CommonNavigator.launchProfileActivity(view.context,
                            UserBaseProfile().apply { userId = it },
                            referrerProviderListener?.latestPageReferrer)
                }
            }

            else -> {
                contentAdDelegate?.onCardClick()

                (item as? CommonAsset)?.let { asset ->

                    MarkStoryCardClickUsecase().toMediator2().execute(
                            bundleOf(MarkStoryCardClickUsecase.UIEVENT_EVENTID to asset.i_id(),
                                    MarkStoryCardClickUsecase.UIEVENT_UID to uniqueId.toString(),
                                    MarkStoryCardClickUsecase.UIEVENT_EVENT to NhAnalyticsAppEvent.STORY_CARD_VIEW.name,
                                    NewsConstants.DH_SECTION to section))

                    if (!CommonUtils.isEmpty(asset.i_deeplinkUrl()) &&
                            (asset.i_onClickUseDeeplink() == true ||
                            asset.i_landingType() == CardLandingType.DEEPLINK)) {
                        CommonNavigator.launchInternalDeeplink(view.context,
                                item.i_deeplinkUrl(),
                                referrer,
                                true,
                                DefaultNavigatorCallback(), section)
                        return
                    } else if (asset.i_format() == Format.NATIVE_CARD && asset.i_subFormat() == SubFormat.ASTRO) {
                        Logger.e(LOG_TAG, "ignored astro card click. no deeplink")
                        return // BE did not send deeplik for astro card; should not open detail.
                    }
                }
                view.disableMomentarily()
                openNewsDetail(item, videoPlayerProvider, args, view)
            }
        }
    }
    
    private fun postExplicitFollowCarouselEvent(fragmentCommunicationVM:FragmentCommunicationsViewModel?,item:CommonAsset) {
        if(item.i_isFollowin()!=true) {
            val bundle = Bundle();
            bundle.putSerializable(Constants.SOURCE_ENTITY, item)
            bundle.putLong(Constants.EVENT_CREATED_AT,System.currentTimeMillis())
            fragmentCommunicationVM?.fragmentCommunicationLiveData?.postValue(
                FragmentCommunicationEvent(
                    -1,
                    useCase = Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL,
                    anyEnum = FollowActionType.FOLLOW.name,
                    arguments = bundle
                )
            )
        }
    }

    private fun listIconClicked(view: View, item: CommonAsset) {

        when (view.getTag(view.id)) {
            IconsList.WA_SHARE -> {
                shareButtonClicked(view, item)
            }
            IconsList.COMMENT -> {
                commentButtonClicked(view, item)
            }
            IconsList.REACTION -> {
                showLikePopup(view, item, null, this, false, Constants.EMPTY_STRING)
            }
            IconsList.REPOST -> {
                repostButtonClicked(view, item)
            }
            else -> {

            }
        }
    }

    private fun discardLocalCard(view: View, item: CommonAsset) {
        val activity = view.context as? Activity
        if (activity == null || activity !is AppCompatActivity) {
            Logger.e(LOG_TAG, "Activity or asset can not be null")
            return
        }

        if (activity.isFinishing) {
            Logger.e(LOG_TAG, "Activity is finishing")
            return
        }
        val args = Bundle()
        args.putString(Constants.BUNDLE_LOCAL_CARD_ID, item.i_localInfo()?.cpId?.toString())
        val commonMessageDialogOptions = CommonMessageDialogOptions(
            uniqueId,
            CommonUtils.getString(R.string.discard_post),
            CommonUtils.getString(R.string.discard_desc),
            CommonUtils.getString(R.string.discard),
            CommonUtils.getString(R.string.dialog_button_retry),
            null,
            Constants.DELETE_LOCAL_CARD_USECASE, args
        )
        PostDeleteDialog.newInstance(commonMessageDialogOptions).show(activity
                .supportFragmentManager, "CommonMessageDialog")

    }

    private fun logStoryRepostEvent(item: CommonAsset) {
        AnalyticsHelper2.logStoryRepostClick(NhAnalyticsEventSection.NEWS, item.i_id(),
                referrer, null, item, groupInfo)
    }

    private fun shareButtonClicked(view: View, item: Any) {
        val intent = Intent()
        intent.action = Constants.SHARE_POST_ACTION
        intent.putExtra(Constants.BUNDLE_STORY, (item as? CommonAsset)?.toMinimizedCommonAsset() as? Serializable)
        (view.getTag(R.id.share_click_argument_tag) as? Bundle)?.let { clickArgs ->
            intent.putExtras(clickArgs)
        }
        val event = NavigationEvent(intent = intent)
        NavigationHelper.navigationLiveData.postValue(event)
        onShareClick(view, item, intent.extras)
        terminateNudgeUsecase.execute(CardNudgeTerminateType.share)
    }

    private fun repostButtonClicked(view: View, item: CommonAsset) {
        if (item.i_postPrivacy() == PostPrivacy.PRIVATE) {
            return
        }
        if (isInDetail) {
            val nextItemID = if(isForyouPage) item.i_id() else null
            val local = LocalInfo(
                    pageId = entityId,
                    location = location,
                    section = section,
                    nextCardId = nextItemID,
                    isCreatedFromMyPosts = isMyPostsPage,
                    creationDate = System.currentTimeMillis()
            )
            val repostIntent = CommonNavigator.getPostCreationIntent(
                    item.i_id(), CreatePostUiMode.REPOST, null, referrer,
                    local, item.i_source()?.id, item.i_source()?.type, item.i_parentPostId())
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(repostIntent))
        } else {
            /*we are posting this event, instead of navigation event, because some
            information is needed from CardsFragment; so it will observe this event
            and post NavigationEvent*/
            CardClickEventHelper.reposts.value = RepostLiveEvent(item)
            terminateNudgeUsecase.execute(CardNudgeTerminateType.repost)
        }
        logStoryRepostEvent(item)
        AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_USER_ENGAGEMENT_REPOST, null)
    }

    private fun logCommentButton(item: CommonAsset,isDetailPage: Boolean) {
        if(isDetailPage) {
            AnalyticsHelper2.logCommentClick(NhAnalyticsEventSection.NEWS, item.i_id(),
                    referrer, null, item, DetailsViewModel.COMMENT_CLICK_DETAIL)
        } else {
            AnalyticsHelper2.logCommentClick(NhAnalyticsEventSection.NEWS, item.i_id(),
                    referrer, null, item, COMMENT_CLICK_LIST)
        }

    }

    private fun commentButtonClicked(view: View, item: Any,isDetailPage:Boolean = false) {
        val card = item as? CommonAsset ?: run {
            return
        }
        try {
            if (!item.i_allowComments()) {
                return
            }
            logCommentButton(card,isDetailPage)
            AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_USER_ENGAGEMENT_COMMENT, null)
            val intent = Intent(Constants.ALL_COMMENTS_ACTION)
            intent.putExtra(Constants.BUNDLE_POST_ID, card.i_id())
            intent.putExtra(Constants.BUNDLE_IS_COMMENT_ONLY, true)
            intent.putExtra(Constants.LOCATION, location)
            intent.putExtra(Constants.BUNDLE_PARENT_ID, card.i_parentPostId())
            intent.putExtra(Constants.BUNDLE_ACTIVITY_TITLE, card.i_title())
            intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
            intent.putExtra(Constants.BUNDLE_SOURCE_ID, card.i_source()?.id)
            intent.putExtra(Constants.BUNDLE_SOURCE_TYPE, card.i_source()?.type)
            intent.putExtra(NewsConstants.DH_SECTION, section)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
            terminateNudgeUsecase.execute(CardNudgeTerminateType.comment)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
    }

    override fun onShareClick(view: View, item: Any, args: Bundle?) {
        super.onShareClick(view, item, args)
        if (item !is CommonAsset) {
            Logger.e(LOG_TAG, "data is not a common asset item : $item")
            return
        }
        shareUsecase.execute(ShareUsecase.args(item.i_id(), "POST", parentId = item.i_parentPostId(),
        postSourceAsset = item.i_source(), sourceLang = item.i_langCode()))
        val shareUi = args?.getSerializable(Constants.SHARE_UI_TYPE) as? ShareUi
        val packageName = args?.getString(Constants.BUNDLE_SHARE_PACKAGE_NAME)
        logStorySharedEvent(item, packageName, shareUi)
        AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_USER_ENGAGEMENT_SHARE, null)
    }

    override fun getShareUsecase(): MediatorUsecase<Bundle, Boolean>? {
        return shareUsecase
    }

    private fun logStorySharedEvent(item: CommonAsset,
                                    packageName: String?,
                                    shareUi: ShareUi?) {
        AnalyticsHelper2.logStorySharedEvent(
                packageName = packageName,
                shareUiParam = shareUi ?: ShareUi.COMMENT_BAR_SHARE_ICON,
                post = item,
                referrer = referrer,
                eventSection = NhAnalyticsEventSection.NEWS,
                groupInfo = groupInfo
        )

    }

    override fun onThreeDotMenuClick(view: View, item: Any?) {
        super.onThreeDotMenuClick(view, item)
        if (item is CommonAsset && item is Serializable) {
            AnalyticsHelper2.logVHItemMenuClickEvent(item, section)
            val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
            intent.putStringArrayListExtra(Constants.BUNDLE_POST_IDS,
                    listOf(item.i_id()).toArrayList())
            intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, menuListLocation)
            intent.putExtra(NewsConstants.DH_SECTION, section)
            intent.putExtra(Constants.BUNDLE_STORY, item.toMinimizedCommonAsset())
            intent.putExtra(Constants.BUNDLE_LOCATION_ID, location)
            intent.putExtra(Constants.REFERRER, getCurrentPageReferrerForFeed())
            intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW, referrerFlow)
            intent.putExtra(Constants.BUNDLE_ENTITY_ID, entityId)
            intent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
        }
    }

    override fun onThreeDotMenuClick(view: View, item: Any?, menulocation: MenuLocation?) {
        if (item is CommonAsset && item is Serializable) {
            AnalyticsHelper2.logVHItemMenuClickEvent(item, section)
            val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
            intent.putStringArrayListExtra(Constants.BUNDLE_POST_IDS,
                    listOf(item.i_id()).toArrayList())
            val menuLocation = menulocation?:menuListLocation
            intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, menuLocation)
            intent.putExtra(NewsConstants.DH_SECTION, section)
            intent.putExtra(Constants.BUNDLE_STORY, item.toMinimizedCommonAsset())
            intent.putExtra(Constants.BUNDLE_LOCATION_ID, location)
            intent.putExtra(Constants.REFERRER, getCurrentPageReferrerForFeed())
            intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW, referrerFlow)
            intent.putExtra(Constants.BUNDLE_ENTITY_ID, entityId)
            intent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
        }
    }

    private fun getCurrentPageReferrerForFeed(): PageReferrer? {
        return when {
            section == PageSection.FOLLOW.section -> PageReferrer(NewsReferrer.FOLLOW_STAR_SECTION)
            section == PageSection.PROFILE.section -> PageReferrer(ProfileReferrer.PROFILE)
            section == PageSection.GROUP.section -> PageReferrer(NhGenericReferrer.GROUP_FEED, groupInfo?.id)
            section == PageSection.SEARCH.section -> PageReferrer(NewsReferrer.SEARCH)
            else -> if(isDetail()){
                PageReferrer(NewsReferrer.STORY_DETAIL)
            }else{
                PageReferrer(NewsReferrer.HASHTAG, entityId)
            }
        }
    }

    private fun openNewsDetail(item: CommonAsset, videoPlayerProvider: VideoPlayerProvider?, args: Bundle? = null, view: View) {
        var uniqueId: String

        if (isInDetail) {
            uniqueId = postId
        } else {
            uniqueId = entityId
        }

        incrementViewcountUsecase.execute(bundleOf(
                Constants.BUNDLE_POST_ID to item.i_id(),
                Constants.BUNDLE_PARENT_ID to item.i_parentPostId()
        ))

        val singlePage = args?.getBoolean(Constants.SINGLE_PAGE, false) ?: false
        val search = CommonUtils.equals(listType, Format.PHOTO.name)

        if (item.i_level() == PostEntityLevel.RELATED_STORIES) {
            uniqueId += "_related"
        } else if (item.i_level() == PostEntityLevel.DISCUSSION) {
            uniqueId +="_discussion"
        } else if (item.i_level() == PostEntityLevel.ASSOCIATION) {
            uniqueId +="_association"
            args?.putBoolean(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE, true)
        }

        if (singlePage) {
            val intent = Intent()
            intent.action = Constants.NEWS_DETAIL_ACTION
            intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD, searchPayloadContext)
            intent.putExtra(Constants.STORY_ID, item.i_id())
            intent.putExtra(NewsConstants.SOURCE_ID, sourceId)
            intent.putExtra(NewsConstants.SOURCE_TYPE, sourceType)
            intent.setPackage(AppConfig.getInstance()!!.packageName)
            intent.putExtra(NewsConstants.DH_SECTION, section)
            intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, referrer)
            intent.putExtra(NewsConstants.POST_ENTITY_LEVEL, item.i_level().name)
            intent.putExtra(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE, true)
            intent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = WeakReference<Any>(videoPlayerProvider)))
        } else {
            val ci = cloneFetchForNewsDetailUsecase.get().toMediator2()
            ci.execute(bundleOf(CloneFetchForNewsDetailUsecase
                    .B_ENTITY_ID to uniqueId, NewsConstants.POST_ENTITY_LEVEL to item.i_level().name,
                    Constants.BUNDLE_KEEP_POST_IDS to arrayListOf(item.i_id())))
            ci.data().take1().observe(lifecycleOwner, Observer {
                if (it.getOrNull()?.isNotEmpty() == true) {
                    val clonedLocation = it.getOrNull()
                    val intent = Intent()
                    intent.action = Constants.NEWS_DETAIL_ACTION
                    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD, searchPayloadContext)
                    intent.putExtra(Constants.STORY_ID, item.i_id())
                    intent.putExtra(NewsConstants.SOURCE_ID, sourceId)
                    intent.putExtra(NewsConstants.SOURCE_TYPE, sourceType)
                    intent.putExtra(Constants.PAGE_ID, uniqueId)
                    intent.putExtra(NewsConstants.SEARCH, search)
                    if (!isInDetail) {
                        intent.putExtra(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
                    }
                    intent.putExtra(Constants.LOCATION, location)
                    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, referrer)
                    intent.setPackage(AppConfig.getInstance()!!.packageName)
                    intent.putExtra(NewsConstants.DH_SECTION, section)
                    intent.putExtra(NewsConstants.POST_ENTITY_LEVEL, item.i_level().name)
                    intent.putExtra(NewsConstants.BUNDLE_LOC_FROM_LIST, clonedLocation)
                    intent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo)
                    intent.putExtra(Constants.IS_LIVE, item.i_videoAsset()?.liveStream)
                    if (args?.containsKey(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE) == true) {
                        intent.putExtra(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE,
                                args.getBoolean(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE))
                    }
                    NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = WeakReference<Any>(videoPlayerProvider)))
                } else {
                    Logger.e(TAG, "DB error. Unable to open newsdetail")
                }
            })
        }

    }

    override fun onFollowEntityClick(view: View, parent: CommonAsset?, item: EntityItem?, position: Int) {
        when (view.id) {
            R.id.explore_text -> {
                if (!CommonUtils.isEmpty(parent?.i_viewAllDeeplink())) {
                    CommonNavigator.launchInternalDeeplink(view.context,
                            parent?.i_viewAllDeeplink(),
                            referrer,
                            true,
                            DefaultNavigatorCallback(), section)
                }
            }
        }
        Logger.d(LOG_TAG, "Follow entity clicked")
        val item1 = item ?: return
        when (view.id) {
            R.id.create_group_card -> {
                launchCreateGroupActivity(view.context)
                val entity = ActionableEntity(entityId = item1.i_entityId()!!,
                        entityType = item1.i_entityType() ?: Constants.EMPTY_STRING,
                        entitySubType = item1.i_entitySubType(),
                        displayName = item1.i_displayName() ?: Constants.EMPTY_STRING,
                        entityImageUrl = item1.i_entityImageUrl() ?: Constants.EMPTY_STRING,
                        experiment = item1.i_experiment(),
                        iconUrl = item1.i_iconUrl() ?: Constants.EMPTY_STRING)

                AnalyticsHelper2.logEntityCardClick(entity, position, section, referrer, parent)
            }

            R.id.import_contact_card -> {
                val url = item.i_deeplinkUrl() ?: Constants.CONTACTS_RECOMENDATION_DEEPLINK
                CommonNavigator.launchInternalDeeplink(view.context, url, referrer, true,
                        DefaultNavigatorCallback())
            }

            R.id.entity_action -> {
                parent ?: run {
                    Logger.e(LOG_TAG, "Parent item1 can not be null")
                    return
                }
                item1.i_entityId() ?: kotlin.run {
                    Logger.e(LOG_TAG, "Entity id can not be null")
                    return
                }
                item1.i_entityType() ?: kotlin.run {
                    Logger.e(LOG_TAG, "Entity type can not be null")
                    return
                }

                val entity = ActionableEntity(entityId = item1.i_entityId()!!,
                        entityType = item1.i_entityType()!!,
                        entitySubType = item1.i_entitySubType(),
                        displayName = item1.i_displayName() ?: Constants.EMPTY_STRING,
                        entityImageUrl = item1.i_entityImageUrl() ?: Constants.EMPTY_STRING,
                        iconUrl = item1.i_iconUrl() ?: Constants.EMPTY_STRING,
                        deeplinkUrl = item.i_deeplinkUrl(),
                        experiment = item.i_experiment(),
                        nameEnglish = item.i_nameEnglish())
                if (item1.i_entityType() == ColdStartEntityType.COMMUNITY_GROUP.name) {
                    joinGroup(item1)
                } else {
                    Logger.d(LOG_TAG, "Following Entity $item1")
                    val pageReferrer =
                        if (section == PageSection.FOLLOW.section)
                            PageReferrer(NewsReferrer.FOLLOW_STAR_SECTION) else referrer
                    if ((FollowActionType.BLOCK.name == parent.i_coldStartAsset()?.actionType)
                    ) {
                        toggleFollowUseCase.execute(
                            bundleOf(
                                ToggleFollowUseCase.B_FOLLOW_ENTITY to entity,
                                ToggleFollowUseCase.B_ACTION to FollowActionType.BLOCK.name
                            )
                        )
                        AnalyticsHelper2.logFollowBlockCorosalClickEvent(
                            entity,
                            pageReferrer,
                            item1.i_selected().not(),
                            section
                        )
                    } else {
                        toggleFollowUseCase.execute(
                            bundleOf(
                                ToggleFollowUseCase.B_FOLLOW_ENTITY to entity
                            )
                        )
                        AnalyticsHelper2.logFollowButtonClickEvent(
                            entity,
                            pageReferrer,
                            item1.i_selected().not(),
                            section
                        )
                    }
                    }
                if (Constants.FOLLOW_EXPLICIT == item.i_experiment()?.get(Constants.CAROUSEL_TYPE)
                ) {
                    FollowBlockPrefUtil.setExplicitFollowlastShownTimestamp(0L)

                } else if (Constants.BLOCK_EXPLICIT == item.i_experiment()
                        ?.get(Constants.CAROUSEL_TYPE)
                ) {
                    FollowBlockPrefUtil.setExplicitBlocklastShownTimestamp(0L)
                }
            }

            R.id.entity_item -> {
                parent ?: run {
                    Logger.e(LOG_TAG, "Parent item1 can not be null")
                    return
                }
                if (item1.i_entityId() == ColdStartEntityItem.ENTITY_ID_FOLLOW_MORE) {
                    Logger.i(LOG_TAG, "Follow more clicked")
                    CommonNavigator.launchFollowHome(view.context, true, null, null,
                            PageReferrer(NhGenericReferrer.FEED_FOLLOWED_CAROUSEL))
                    return
                }
                if (item1.i_entityType() == ColdStartEntityType.COMMUNITY_GROUP.name) {
                    Logger.d(LOG_TAG, "Entity item1 clicked launching groupdetail")
                    item1.i_entityId()?.let {
                        CommonNavigator.launchGroupDetailActivity(view.context, GroupBaseInfo().apply {
                            this.id = it
                        }, referrer, referrerFlow)
                    }
                } else {
                    Logger.d(LOG_TAG, "Entity item1 clicked launching deeplink")
                    CommonNavigator.launchInternalDeeplink(view.context, item1.i_deeplinkUrl(), referrer, true, DefaultNavigatorCallback())
                }
                val entity = ActionableEntity(entityId = item1.i_entityId()!!,
                        entityType = item1.i_entityType() ?: Constants.EMPTY_STRING,
                        entitySubType = item1.i_entitySubType(),
                        displayName = item1.i_displayName() ?: Constants.EMPTY_STRING,
                        entityImageUrl = item1.i_entityImageUrl() ?: Constants.EMPTY_STRING,
                        iconUrl = item1.i_iconUrl() ?: Constants.EMPTY_STRING,
                        experiment = item1.i_experiment(),
                        nameEnglish = item1.i_nameEnglish())

                AnalyticsHelper2.logEntityCardClick(entity, position, section, referrer, parent)
            }
        }
    }


    override fun onFollowEntities(view: View, entityList: List<EntityItem>?, args: Bundle, asset:
    CommonAsset) {
        when (view.id) {
            R.id.save_location_selection -> {

                val adapterList = ArrayList<ActionableEntity>()

                entityList?.forEach { item ->
                    val entity = ActionableEntity(entityId = item.i_entityId()!!,
                            entityType = item.i_entityType()!!,
                            entitySubType = item.i_entitySubType(),
                            displayName = item.i_displayName() ?: Constants.EMPTY_STRING,
                            entityImageUrl = item.i_entityImageUrl() ?: Constants.EMPTY_STRING,
                            iconUrl = item.i_iconUrl() ?: Constants.EMPTY_STRING,
                            deeplinkUrl = item.i_deeplinkUrl(),
                            experiment = item.i_experiment(),
                            nameEnglish = item.i_nameEnglish())
                    adapterList.add(entity)
                }

                Logger.d(LOG_TAG, "Following Entities $ adapterList")
                toggleFollowUseCase.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITIES to
                        adapterList))

                val usecaseArguments = args ?: Bundle()
                usecaseArguments.putSerializable(Constants.BUNDLE_POST_ID, asset.i_id())
                clearLanguageSelectionCard.execute(usecaseArguments)
            }

        }
    }

    //This function is invoked from local zone bottom sheet locations,
    override fun onUnFollowLocations(view: View, actionableEntities: List<ActionableEntity>) {
        when (view.id) {
            R.id.local_zone_location_menu -> {
                toggleFollowUseCase.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITIES to
                        actionableEntities))
                for (item in actionableEntities) {
                    Logger.d(LOG_TAG, "Unfollowed Locations ${item.displayName}")
                    AnalyticsHelper2.logFollowButtonClickEvent(item, referrer, false, section)
                }
            }
        }
    }

    private fun onCollectionCardClick(view: View, item: CommonAsset?, parent: CommonAsset?,
                                      videoPlayerProvider: VideoPlayerProvider? = null,
                                      loadViewMore: Boolean = false, isAutoClick: Boolean = false,collectionId: String?=null) {
        item?:return
        if (item.isViralCard) {
            incrementViewcountUsecase.execute(bundleOf(
                    Constants.BUNDLE_POST_ID to item.i_id(),
                    Constants.BUNDLE_PARENT_ID to parent?.i_id()
            ))
        }

        openCollectionUsecase.execute(bundleOf(Constants.BUNDLE_POST_ID to parent?.i_id(),
                Constants.BUNDLE_AD_ID to parent?.i_adId(),
                Constants.BUNDLE_LOCATION_ID to section, Constants.BUNDLE_USE_COLLECTION to true,
                Constants.COLLECTION_ID to (collectionId ?: Constants.EMPTY_STRING)))
        openCollectionUsecase.data().observe(lifecycleOwner, Observer {
            val fetchId = it.getOrNull()
            if (it.isSuccess && fetchId != null && fetchId != lastKnownOPFetchId) {
                lastKnownOPFetchId = fetchId
                openCollectionUsecase.data().removeObservers(lifecycleOwner)
                val intent = Intent()
                intent.action = Constants.CAROUSEL_DETAIL_ACTION
                intent.putExtra(Constants.PARENT_STORY_ID, parent?.i_id())
                intent.putExtra(Constants.STORY_ID, item.i_id())
                intent.putExtra(Constants.CAROUSEL_LOAD_VIEW_MORE, loadViewMore)
                intent.putExtra(Constants.PAGE_ID, fetchId)
                intent.putExtra(Constants.LOCATION, fetchId)
                intent.putExtra(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
                intent.putExtra(NewsConstants.DH_SECTION, section)
                intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, referrer)
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = WeakReference<Any>(videoPlayerProvider)))
            } else {
                Logger.e(TAG, "Error opening other perspective story", it.exceptionOrNull())
            }
        })
    }


    override fun onCollectionItemClick(view: View, item: CommonAsset?, parent: CommonAsset?, parentCardPosition: Int) {
        super.onCollectionItemClick(view, parent, item, parentCardPosition)
        item?.let {
            AnalyticsHelper2.logStoryCardClickEvent(
                    item,
                    /*enum value can be derived from 'location' but its string*/
                    referrer,
                    parentCardPosition,
                    null,
                    referrerProviderListener)
        }
        onCollectionCardClick(view, item, parent, null)
    }

    override fun onCollectionItemClick(view: View, item: CommonAsset?, parent: CommonAsset?, parentCardPosition: Int,collectionId:String?) {
        super.onCollectionItemClick(view, parent, item, parentCardPosition)
        item?.let {
            AnalyticsHelper2.logStoryCardClickEvent(
                item,
                /*enum value can be derived from 'location' but its string*/
                referrer,
                parentCardPosition,
                null,
                referrerProviderListener)
        }
        onCollectionCardClick(view, item, parent, null, collectionId = collectionId)
    }

    private fun makeGroupBaseInfo(groupId: String, userId: String): GroupBaseInfo {
        return GroupBaseInfo().apply {
            this.id = groupId
            this.userId = userId
        }
    }

    private fun launchCreateGroupActivity(context: Context) {
        CommonNavigator.launchEditGroupActivity(context, null, SSO.getInstance().isLoggedIn
        (false), PageReferrer(NewsReferrer.FOLLOW_STAR_SECTION))
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?, contentAdDelegate: ContentAdDelegate?) {
        when (view.id) {
            R.id.header -> {
                if (item is CommonAsset) {
                    handleSourceClick(item, view.context, args, view)
                }
            }
            else -> {
                NhAnalyticsAppState.getInstance().setAction(NhAnalyticsUserAction.CLICK)
                onClick(view, item, null, args, contentAdDelegate = contentAdDelegate)
            }
        }
    }

    override fun onDialogDiscardLocal(arguments: Bundle?) {
        if (arguments != null) {
            deleteLocalCardUseCase.execute(arguments)
        }
    }

    private fun handleApproveAction(item: ReviewActionBody) {
        item.action = ApprovalAction.APPROVED
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NhGenericReferrer.APPROVALS), NewsExploreButtonType.APPROVE, section)
        approvalActionMediatorUC.execute(item)
    }

    private fun handleDeclineAction(item: ReviewActionBody) {
        item.action = ApprovalAction.DECLINED
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NhGenericReferrer.APPROVALS), NewsExploreButtonType.DECLINE, section)
        approvalActionMediatorUC.execute(item)
    }

    private fun handleOgClick(context: Context, item: CommonAsset) {
        AnalyticsHelper2.logOgClick(NhAnalyticsEventSection.NEWS, item.i_id(), referrer, null, item, groupInfo)
        NHBrowserUtil.openWithNHBrowser(context, item.i_linkAsset()?.url, true)
    }

    private fun handleSourceClick(item: CommonAsset, context: Context, args: Bundle?, view: View) {

        if ((CardsBindUtils.isSameUserId(item) && section == PageSection.PROFILE.section) ||
                fragmentBundle?.getString(NewsConstants.SOURCE_ID) == item.i_source()?.id || item.i_format() == Format.LOCAL) {
            if (!isInDetail) {
                return
            }
        }
        args?.let {
            openNPLanding(item, context)
        }
    }


    override fun canClickOnSource(item: Any?): Boolean {
        if (item is CommonAsset) {
            if ((CardsBindUtils.isSameUserId(item) && section == PageSection.PROFILE.section) ||
                    fragmentBundle?.getString(NewsConstants.SOURCE_ID) == item.i_source()?.id || item.i_format() == Format.LOCAL) {
                if (!isInDetail) {
                    return false
                }
            }
        }
        return true
    }


    private fun answerPoll(context: Context, item: CommonAsset, index: Int) {
        auth.runWhenLoggedin("${item.i_id()}poll") {
            if (it) {
                val userId = SSO.getInstance().userDetails?.userLoginResponse?.userId
                val pollId = item.i_id()
                val optionId = item.i_poll()?.options?.getOrNull(index)?.id?.toString()
                val submitUrl = item.i_poll()?.interactionUrl
                AnalyticsHelper2.logPollClick(AnalyticsHelper2.getSection(section), item.i_id(),
                        referrer, null, item, groupInfo)
                if (userId == null || optionId == null) {
                    Logger.e(TAG, "illegal state in poll_option_1 click")
                } else {
                    answerPollUsecase.data().take1().observe(lifecycleOwner, Observer {
                        if (it.isSuccess && it.getOrDefault("").isNotEmpty()) {
                            FontHelper.showCustomFontToast(context,
                                    it.getOrDefault(""), Toast.LENGTH_SHORT)
                        }
                    })
                    answerPollUsecase.execute(bundleOf(AnswerPollUsecase.B_VOTE to
                            Vote(userId, pollId, optionId),
                            AnswerPollUsecase.B_INTERACTIONURL to submitUrl))
                }
            }
        }
    }

    private fun handleUserInteractionClicks(view: View, item: CommonAsset, viewId: Int) {
        item.i_userInteractionAsset()?.let { interaction ->
            when (viewId) {
                R.id.interaction_rootview -> {
                    val deeplink = if (interaction.activityDeeplink.isNullOrEmpty()) {
                        item.i_deeplinkUrl()
                    } else {
                        interaction.activityDeeplink
                    }
                    CommonNavigator.launchInternalDeeplink(view.context,
                            deeplink,
                            referrer,
                            true,
                            DefaultNavigatorCallback(),
                            bundleOf(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE to true))
                }
                R.id.user_interaction_delete -> {
                    markInteractionDeletedUC.execute(item.i_id())
                }
                else -> {
                }
            }
            return
        }
        if (listType == Constants.LIST_TYPE_BOOKMARKS) {
            CommonNavigator.launchInternalDeeplink(view.context, item.i_deeplinkUrl(),
                PageReferrer(NewsReferrer.SAVED_ARTICLES), true, DefaultNavigatorCallback(),
                bundleOf(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE to true))
        }
    }

    override fun onEmojiClick(view: View, item: Any, parentItem: Any?, likeType: LikeType, isComment: Boolean?, commentType: String?) {
        Logger.d(TAG, "Like type $likeType clicked")
        val postAsset = item as? CommonAsset
        val parentAsset = parentItem as? CommonAsset
        if (postAsset != null) {
            AnalyticsHelper2.logLikeStoryEvent(AnalyticsHelper2.getSection(section), postAsset.i_id(),
                    referrer, null, postAsset, parentAsset, referrer, likeType, groupInfo, isComment, commentType)
        }
        AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_USER_ENGAGEMENT_LIKE, null)
        if (item is CommonAsset) {
            toggleLikeUsecase.execute(ToggleLikeUsecase.args((item as? CommonAsset)?.i_id() ?: "",
                    "POST", likeType.name,
                    bundleOf(Constants.BUNDLE_PARENT_ID to item.i_parentPostId())))
        } else {
            toggleLikeUsecase.execute(ToggleLikeUsecase.args((item as? CreatePostEntity)?.cpId.toString() ?: "", "POST", likeType.name))
        }
    }

    fun openNPLanding(item: CommonAsset, context: Context, useDeeplinkIfAvailable: Boolean = true) {
        val feedTypeProfile = item.i_source()?.feedType == "PROFILE"
        val srcDeeplink = if (feedTypeProfile) item.i_source()?.deeplinkUrl else item.i_source()?.getSourceDeeplink(true)
        if (useDeeplinkIfAvailable && srcDeeplink != null) {
            Logger.d(LOG_TAG, "openNPLanding: launch deeplink $srcDeeplink")
            CommonNavigator.launchInternalDeeplink(context, srcDeeplink,
                    referrer, true, DefaultNavigatorCallback(), section)
            return
        }
        Logger.d(LOG_TAG, "openNPLanding: launch intent")
        val sourceAsset = item.i_source() ?: return
        if ("SOURCE" == sourceAsset.feedType) {
            sourceAsset.let {
                val intent = Intent(Constants.ENTITY_OPEN_ACTION)
                intent.setPackage(CommonUtils.getApplication().packageName)
                intent.putExtra(NewsConstants.ENTITY_KEY, sourceAsset.id)
                intent.putExtra(NewsConstants.ENTITY_TYPE, sourceAsset.entityType)
                intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, referrer)
                intent.putExtra(NewsConstants.DH_SECTION, section)
                NavigationHelper.navigationLiveData.value = NavigationEvent(intent)
            }
        } else {
            val loggedInUserId = SSO.getInstance().userDetails?.userID

            val pageReferrer = referrer
            val tabType = if (loggedInUserId == sourceAsset.id) ProfileTabType.FPV_POSTS else ProfileTabType.TPV_POSTS
            val profile = UserBaseProfile()
            profile.userId = sourceAsset.id ?: Constants.EMPTY_STRING
            val intent = CommonNavigator.getProfileHomeIntent(profile,
                    pageReferrer,
                    tabType)
            val event = NavigationEvent(intent = intent,
                    timeStamp = System.currentTimeMillis())
            NavigationHelper.navigationLiveData.postValue(event)

        }
    }

    private fun joinGroup(entity: EntityItem) {
        entity.i_entityId()?.let { entityId ->
            //Join group is allowed only with social login.
            auth.runWhenLoggedin("${entityId}join") { loggedIn ->
                if (loggedIn) {
                    SSO.getLoginResponse()?.userId?.let { userId ->
                        joinGroupUsecase.execute(makeGroupBaseInfo(entityId, userId))
                    }
                }
            }
        }
    }

    override fun onInternalUrlClick(view: View, url: String) {
        super.onInternalUrlClick(view, url)
        Logger.d(LOG_TAG, "launching deeplink $url")
        if (DeeplinkUtils.isDHDeeplink(url)) {
            CommonNavigator.launchDeeplink(view.context, url, referrer)
        } else if (DeeplinkUtils.isValidHost(url)) {
            AndroidUtils.launchExternalLink(view.context, url)
        }
    }

    companion object {
        const val TAG = "CardClickDelegate"

    }

    class Factory @Inject constructor(@Named("entityId") private val entityId: String,
                                      @Named("postId") private val postId: String,
                                      @Named("pageEntity") private val pageEntity: PageEntity?,
                                      @Named("sourceId") private val sourceId: String?,
                                      @Named("sourceType") private val sourceType: String?,
                                      @Named("timeSpentEventId") private val timeSpentEventId: Long,
                                      private val toggleFollowUseCase: ToggleFollowUseCase,
                                      private val toggleLikeUsecase: ToggleLikeUsecase,
                                      private val shareUsecase: ShareUsecase,
                                      private val answerPollUsecase: AnswerPollUsecase,
                                      @Named("clickDelegate") private val clickDelegate: ClickDelegate?,
                                      @Named("approvalActionMediatorUC")
                                      private val approvalActionMediatorUC: MediatorUsecase<ReviewActionBody, Boolean>,
                                      private val cloneFetchForNewsDetailUsecase: Provider<CloneFetchForNewsDetailUsecase>,
                                      @Named("joinGroupMediatorUC")
                                      private val joinGroupMediatorUC: MediatorUsecase<GroupBaseInfo, GroupInfo>,
                                      private val deleteBookmarkUsecase: PostBookmarksUsecase,
                                      private val openCollectionUsecase: OpenOtherPerspectiveUsecase,
                                      private val lifecycleOwner: LifecycleOwner,
                                      @Named("section") private val section: String,
                                      @Named("fpUsecase") private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>,
                                      @Named("location") private val location: String,
                                      private val markInteractionDeletedUsecase:
                                      MarkInteractionDeletedUsecase,
                                      @Named("listType")
                                      private val listType: String?,
                                      @Named("searchQuery")
                                      private val searchQuery: SearchQuery?,
                                      private val deleteLocalCardUseCase: DeleteLocalCardUseCase,
                                      private val auth: AuthOrchestrator,
                                      private val menuListLocation: MenuLocation,
                                      private val groupInfo: GroupInfo?,
                                      private val clearLanguageSelectionCard: ClearLanguageSelectionCard,
                                      @Named("isForyouPage") private val isForyouPage: Boolean,
                                      @Named("isMyPostsPage") private val isMyPostsPage: Boolean,
                                      @Named("incrementViewcountUsecase")
                                      private val incrementViewcountUsecase: MediatorUsecase<Bundle, Boolean>,
                                      @Named("terminateNudgeUc")
                                      private val terminateNudgeUsecase: MediatorUsecase<CardNudgeTerminateType, Boolean>


    ) {
        fun get(): ClickDelegate {
            return clickDelegate ?: CardClickDelegate(entityId, postId, pageEntity, sourceId,
                    sourceType,
                    timeSpentEventId,
                    toggleFollowUseCase.toMediator2(),
                    toggleLikeUsecase.toMediator2(),
                    shareUsecase.toMediator2(),
                    answerPollUsecase.toMediator2(),
                    approvalActionMediatorUC,
                    cloneFetchForNewsDetailUsecase,
                    joinGroupMediatorUC,
                    deleteBookmarkUsecase.toMediator2(),
                    openCollectionUsecase.toMediator2(),
                    lifecycleOwner,
                    section,
                    fpUsecase,
                    location,
                    markInteractionDeletedUsecase.toMediator2(),
                    listType,
                    searchQuery,
                    auth,
                    menuListLocation,
                    groupInfo,
                    clearLanguageSelectionCard.toMediator2(),
                    isForyouPage,
                    deleteLocalCardUseCase.toMediator2(),
                    isMyPostsPage,
                    incrementViewcountUsecase,
                    terminateNudgeUsecase)
        }
    }
}

private const val LOG_TAG = "CardClickDelegate"
private const val COMMENT_CLICK_LIST = "comment_click_list"