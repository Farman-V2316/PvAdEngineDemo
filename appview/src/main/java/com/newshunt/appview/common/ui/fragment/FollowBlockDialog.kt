package com.newshunt.appview.common.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.appview.R
import com.newshunt.appview.databinding.FragmentFollowBlockDialogBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.news.model.entity.server.asset.AssetType
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.news.model.daos.FollowBlockRecoDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import com.newshunt.news.viewmodel.FollowUpdateViewModel
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.news.view.activity.NewsBaseActivity

/**
 * Created by Rekha.Rani on 2021-12-20.
 */

//TODO @aman.roy Animate the Fragment from bottom to top.
private const val TAG = "FollowBlockDialog"
class FollowBlockDialogFragment : BottomSheetDialogFragment() {

    private lateinit var source: PostSourceAsset
    private lateinit var fragmntFollowBlockDialogBinding: FragmentFollowBlockDialogBinding;
    private var state: Boolean = false
    lateinit var action: String

    lateinit var followUpdateViewModelF: FollowUpdateViewModel.Factory
    private lateinit var vmFollowUpdate: FollowUpdateViewModel
    private lateinit var fragmentCommunicationsViewModel: FragmentCommunicationsViewModel
    private lateinit var toggleFollowUseCase: MediatorUsecase<Bundle, Boolean>
    private lateinit var followRecoDao: FollowBlockRecoDao
    private lateinit var followEntityDao: FollowEntityDao
    private var isClicked:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        fragmntFollowBlockDialogBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_follow_block_dialog,
            container,
            false
        )
        val entity = arguments?.getSerializable(KEY_SOURCE_ITEM) as SourceFollowBlockEntity?
        val currentPageReferrer = arguments?.getSerializable(KEY_PAGE_REFERER) as PageReferrer?
        action = arguments?.getString(KEY_ACTION).toString()

        fragmntFollowBlockDialogBinding.text.text = action
        state = CommonUtils.equalsIgnoreCase(Constants.FOLLOW, action)
        fragmntFollowBlockDialogBinding.state = state
        fragmntFollowBlockDialogBinding.sourceItem = entity?.postSourceEntity;
        followRecoDao = SocialDB.instance().followBlockRecoDao()
        followEntityDao = SocialDB.instance().followEntityDao()
        toggleFollowUseCase = ToggleFollowUseCase(FollowRepo(followEntityDao)).toMediator2()
        followUpdateViewModelF = FollowUpdateViewModel.Factory(
            CommonUtils.getApplication(),
            followBlockUpdateUsecase = FollowBlockUpdateUsecase(followRecoDao),
            ImplicitFollowTriggerUsecase(followRecoDao, followEntityDao),
            ImplicitBlockTriggerUsecase(followRecoDao, followEntityDao),
            ExplicitFollowBlockTriggerUsecase(followRecoDao, followEntityDao),
            GetFollowBlockUpdateUsecase(followRecoDao),
            ColdSignalUseCase(followRecoDao,followEntityDao),
            MinCardPositionUseCase(followRecoDao),
            UpdateFollowBlockImplictDialogCountUsecase(followRecoDao),
            CardPositionUseCase(followRecoDao),
            BottomBarDurationUseCase(followRecoDao)
        );
        if(!state) {
            FollowBlockPrefUtil.incrementSoftFollowSessionCount()
        } else {
            FollowBlockPrefUtil.incrementSoftBlockSessionCount()
        }
        vmFollowUpdate = ViewModelProviders.of(this, followUpdateViewModelF).get(
            FollowUpdateViewModel::class.java
        )
        fragmentCommunicationsViewModel = ViewModelProviders.of(requireActivity())
            .get(FragmentCommunicationsViewModel::class.java)

        entity?.postSourceEntity?.let {
            source = it
        }

        fragmntFollowBlockDialogBinding.followContainer.setOnClickListener {
            state = !state
            fragmntFollowBlockDialogBinding.state = state
            updateState()
            if (!state) {
                entity?.postSourceEntity?.let {
                    triggerFollowBlockCase(
                        FollowActionType.BLOCK.name, entity
                    )
                }

                DialogAnalyticsHelper.logDialogBoxActionEvent(
                    DialogBoxType.IMPLICIT_BLOCK_PROMPT, currentPageReferrer, Constants.BLOCK,
                    NhAnalyticsEventSection.NEWS, null
                )

            } else {
                entity?.postSourceEntity?.let {
                    triggerFollowBlockCase(
                        FollowActionType.FOLLOW.name, entity
                    )
                }

                DialogAnalyticsHelper.logDialogBoxActionEvent(
                    DialogBoxType.IMPLICIT_FOLLOW_PROMPT, currentPageReferrer, Constants.FOLLOW,
                    NhAnalyticsEventSection.NEWS, null
                )

            }
            isClicked = true
            this.dismiss()
        }


        return fragmntFollowBlockDialogBinding.root;
    }
/***
 * This flow is called from clicking back and clicking outside view
 ***/
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        state = !state
        updateImplicitFollowBlockPrefs()
    }
/***
 * This flow is called from clicking on popup or after timeout
 ***/
    override fun dismiss() {
        super.dismiss()
        if(!isClicked) {
            state = !state
            updateImplicitFollowBlockPrefs() // when user does not act and timeout happens
        } else {
            updateImplicitFollowBlockPrefs(0) // no need to check clicked because this path will be taken when user acts on popup hence it will always be non clicked state.
        }
    }
/***
 * By the time this function is called state is reflected to be true when follow and false when block.
 ***/
    private fun updateImplicitFollowBlockPrefs(time:Long = System.currentTimeMillis()) {
        if(state) {
            FollowBlockPrefUtil.updateImplicitFollowShow(time)
        } else {
            FollowBlockPrefUtil.updateImplicitBlockShow(time)
        }
    }

    private fun triggerFollowBlockCase(
        action: String, sourceFollowBlockEntity: SourceFollowBlockEntity
    ) {
        var postSourceAsset = sourceFollowBlockEntity?.postSourceEntity
        val id = postSourceAsset?.id ?: run {
            Logger.e(
                TAG,
                "source id  missing"
            ); return
        }

        val type = postSourceAsset.entityType ?: AssetType.SOURCE.name

        val entity = ActionableEntity(
            entityId = id,
            entityType = type,
            entitySubType = postSourceAsset.type,
            displayName = postSourceAsset.displayName ?: Constants.EMPTY_STRING,
            entityImageUrl = postSourceAsset.entityImageUrl ?: Constants.EMPTY_STRING,
            iconUrl = postSourceAsset.icon ?: Constants.EMPTY_STRING,
            deeplinkUrl = postSourceAsset.deeplinkUrl ?: Constants.EMPTY_STRING,
            nameEnglish = postSourceAsset.nameEnglish
        )

        toggleFollowUseCase.execute(
            bundleOf(
                ToggleFollowUseCase.B_FOLLOW_ENTITY to entity,
                ToggleFollowUseCase.B_ACTION to action
            )
        )
        val bundle = Bundle()
        bundle.putSerializable(
            Constants.SOURCE_ENTITY, PostEntity(
                id = sourceFollowBlockEntity.sourceId,
                langCode = sourceFollowBlockEntity.sourceLang,
                source = sourceFollowBlockEntity.postSourceEntity
            )
        )
        bundle.putLong(Constants.EVENT_CREATED_AT,System.currentTimeMillis())
        fragmentCommunicationsViewModel.fragmentCommunicationLiveData.postValue(
            FragmentCommunicationEvent(
               hostId =  (requireActivity() as NewsBaseActivity).activityId,
               useCase= Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL,
                anyEnum = action,
                arguments = bundle
            )
        )

    }

    fun setState(newState: Boolean, ignoreIfSame: Boolean = false) {
        if (ignoreIfSame && newState == this.state) return
        this.state = newState
        updateState()
    }

    fun updateState() {
        fragmntFollowBlockDialogBinding.followContainer.isSelected = state
        showFollowButton()
    }


    private fun showFollowButton() {
        if (state) {
            fragmntFollowBlockDialogBinding.icon.setImageDrawable(CommonUtils.getDrawable(R.drawable.ic_block))
            fragmntFollowBlockDialogBinding.text.setTextColor(CommonUtils.getColor(R.color.block_button_text_color))
            fragmntFollowBlockDialogBinding.text.text = Constants.BLOCK
            fragmntFollowBlockDialogBinding.followContainer.background =
                CommonUtils.getDrawable(R.drawable.block_grey_bg_outlined)

        } else {
            fragmntFollowBlockDialogBinding.icon.setImageDrawable(CommonUtils.getDrawable(R.drawable.ic_follow_star))
            fragmntFollowBlockDialogBinding.text.setTextColor(CommonUtils.getColor(R.color.follow_color));
            fragmntFollowBlockDialogBinding.text.text = Constants.FOLLOW
            fragmntFollowBlockDialogBinding.followContainer.background =
                CommonUtils.getDrawable(R.drawable.entity_follow_container_bg)

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    companion object {
        private const val KEY_SOURCE_ITEM: String = "Source_item"
        private const val KEY_PAGE_REFERER: String = "page_referrer"
        private const val KEY_ACTION: String = "action"

        @JvmStatic
        fun newInstance(item: SourceFollowBlockEntity, pageReferrer: PageReferrer?, action: String): FollowBlockDialogFragment {
            val fragment = FollowBlockDialogFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            bundle.putSerializable(KEY_SOURCE_ITEM, item)
            bundle.putSerializable(KEY_PAGE_REFERER, pageReferrer)
            bundle.putString(KEY_ACTION, action)
            return fragment
        }
    }

}