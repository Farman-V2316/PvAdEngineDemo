/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.fragment

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.di.DaggerMenuComponent2
import com.newshunt.appview.common.di.MenuModule2
import com.newshunt.appview.common.video.ui.helper.MenuState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.viewmodel.MenuViewModel
import com.newshunt.appview.databinding.MenuOptionItemNewBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.SSO
import kotlinx.android.synthetic.main.menu_option_item_new.view.*
import javax.inject.Inject

/**
 * @author amit.chaudhary
 */
class MenuFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var menuViewModelFactory: MenuViewModel.Factory
    private lateinit var menuViewModel: MenuViewModel
    private lateinit var adapter: MenuOptionsAdapter
    private lateinit var fragmentCommunicationsViewModel: FragmentCommunicationsViewModel
    private val uniqueScreenId = UniqueIdHelper.getInstance().generateUniqueId()
    private var actionEventLogged: Boolean = false
    private var section: String? = null
    private var pageReferrer: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var groupInfo: GroupInfo? = null
    private var card: CommonAsset? = null
    private lateinit var menuLocation: MenuLocation
    private lateinit var referrer: PageReferrer
    private var isVideoDetailMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentBinding = DataBindingUtil.inflate<com.newshunt.appview.databinding.FragmentMenuBinding>(inflater, R.layout
                .fragment_menu, container, false)
        arguments?.let { args ->
            menuLocation = args.getSerializable(Constants.BUNDLE_MENU_CLICK_LOCATION) as? MenuLocation
                    ?: run {
                        Logger.e(LOG_TAG, "Menu location can not be null")
                        return fragmentBinding.root
                    }
            section = args.getString(NewsConstants.DH_SECTION)
            pageReferrer = args.getSerializable(Constants.REFERRER) as? PageReferrer
            referrerFlow = args.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW) as? PageReferrer
            groupInfo = args.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo
            card = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset
            if((card?.i_format() == Format.VIDEO || card?.i_format() == Format.EMBEDDED_VIDEO) &&
                    menuLocation == MenuLocation.DETAIL){
                isVideoDetailMode = true
                val topPadding = resources.getDimension(R.dimen.vid_detail_bottom_menu_top_padding).toInt()
                fragmentBinding.dislikeFragmentView.setPadding(0, topPadding, 0 , 0)
                fragmentBinding.dislikeFragmentView.background = context?.getDrawable(R.drawable
                        .rounded_top_corner_black_bg)
            }

            initalize(binding = fragmentBinding,
                    args = args,
                    menuLocation = menuLocation)
            card = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset
            referrer = if (menuLocation == MenuLocation.DETAIL) {
                PageReferrer(NhGenericReferrer.STORY_DETAIL)
            }else if (AnalyticsHelper2.getSection(section ?: Constants.EMPTY_STRING) ==
                NhAnalyticsEventSection.GROUP) {
                PageReferrer(NhGenericReferrer.GROUP_FEED)
            }else {
                PageReferrer(NewsReferrer.STORY_CARD, card?.i_id())
            }
            DialogAnalyticsHelper.logDialogBoxViewedEvent(DialogBoxType.REPORT_STORY, referrer,
                    AnalyticsHelper2.getSection(section
                            ?: Constants.EMPTY_STRING), groupInfo?.userRole)
        }
        return fragmentBinding.root
    }

    private fun initalize(binding: ViewDataBinding,
                          args: Bundle = Bundle(),
                          menuLocation: MenuLocation) {

        DaggerMenuComponent2.builder().menuModule2(
                MenuModule2(context = CommonUtils.getApplication(),
                        menuLocation = menuLocation,
                        uniqueScreenId = uniqueScreenId,
                        arguments = args)).build()
                .inject(this)
        menuViewModel = ViewModelProviders.of(this, menuViewModelFactory)[MenuViewModel::class.java]
        fragmentCommunicationsViewModel = ViewModelProviders.of(activity!!).get(FragmentCommunicationsViewModel::class.java)
        fragmentCommunicationsViewModel.fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != uniqueScreenId) {
                return@Observer
            }
            if (it.useCase == Constants.DELETE_POST_DIALOG_USECASE) {
                if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                    menuViewModel.onDialogConformDelete(it.arguments)
                } else {
                    menuViewModel.menuVisibility.postValue(false)
                }
            }
        })
        menuViewModel.menuDeletePostData.observe(this, Observer {
            if (it.isSuccess && it.getOrNull() == true) {
                showPostDeletedSnackbar()
            }
            menuViewModel.menuVisibility.postValue(false)
        })
        adapter = MenuOptionsAdapter(diffCallback = MenuDiffUtil(),
                isNightMode = ThemeUtils.isNightMode(),
                vm = menuViewModel,
                activity = activity,
                isVideoDetailMode =  isVideoDetailMode)
        menuViewModel.menuOption.observe(this, Observer { result ->
            if (result.isSuccess) {
                result.getOrNull()?.let { menuData ->
                    if (menuData.menuList.isNullOrEmpty()) {
                        menuViewModel.menuVisibility.postValue(false)
                    } else {
                        adapter.updateData(menuData.menuList, menuData.card, menuData.pageEntity)
                    }
                }
            }
        })
        menuViewModel.menuMetaDetail.observe(this, Observer { result ->
            if (result.isSuccess) {
                result.getOrNull()?.let {
                    binding.setVariable(BR.menuMeta, it)
                    binding.executePendingBindings()
                }
            }
        })

        menuViewModel.menuVisibility.observe(this, Observer {
            if (!it) {
                dismiss()
            }
        })
        binding.setVariable(BR.adapter, adapter)
        args.putString(Constants.BUNDLE_USER_ID, SSO.getInstance()?.userDetails?.userID)
        menuViewModel.start(args)
    }

    fun showBottomSheetFragment(manager: FragmentManager?, tag: String?) {
        if (manager == null) {
            Logger.e(LOG_TAG, "Fragment manager is null")
            return
        }
        show(manager, tag)
        VideoHelper.menuStateLiveData.postValue(MenuState(true, false))
    }

    companion object {
        fun createInstance(args: Bundle? = null): MenuFragment {
            val menuFragment = MenuFragment()
            menuFragment.arguments = args
            return menuFragment
        }

        const val ARG_JS_ACTION_KEY = "action"
        const val ARG_JS_ACTION_HIDE_STORY = "hidestory"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        logActionEvent()
        VideoHelper.menuStateLiveData.postValue(MenuState(false,
                menuViewModel.l1HideCard ?: false))
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        logActionEvent()
    }


    private fun logActionEvent() {
        if (actionEventLogged) return
        val type = menuViewModel.clickedL1Id ?: DialogBoxType.UNQUALIFIED_FEEDBACK.type
        val action = if (menuViewModel.l1Clicked) DialogAnalyticsHelper.DIALOG_ACTION_OK else
            DialogAnalyticsHelper.DIALOG_ACTION_CANCEL
        DialogAnalyticsHelper.logDialogBoxActionEvent(type, referrer, action,
                AnalyticsHelper2.getSection(section ?: Constants.EMPTY_STRING),
                groupInfo?.userRole)
        actionEventLogged = true
    }

    private fun showPostDeletedSnackbar() {
        val activity = activity ?: return
        GenericCustomSnackBar.showSnackBar(
                view = activity.window.decorView,
                context = activity,
                text = CommonUtils.getString(R.string.post_deleted),
                duration = Snackbar.LENGTH_LONG,
                actionType = null,
                errorMessageClickedListener = null,
                action = null,
                customActionClickListener = null,
                bottomBarVisible = false
        ).show()
    }
}


private const val LOG_TAG = "MenuFragment"

/**
 * @author amit.chaudhary
 */
class MenuOptionsAdapter(diffCallback: DiffUtil.ItemCallback<MenuOption>,
                         private val isNightMode: Boolean = false,
                         private val vm: MenuViewModel,
                         private val activity: Activity? = null,
                         private val isVideoDetailMode:Boolean = false)
    : ListAdapter<MenuOption, RecyclerView.ViewHolder>(diffCallback) {

    private var asset: CommonAsset? = null
    private var pageEntity: PageEntity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflator = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate<MenuOptionItemNewBinding>(layoutInflator, R
                .layout.menu_option_item_new, parent, false)
        binding.setVariable(BR.isNightMode, isNightMode)
        binding.setVariable(BR.activity, activity)
        binding.setVariable(BR.asset, asset)
        binding.setVariable(BR.pageEntity, pageEntity)
        binding.setVariable(BR.vm, vm)
        if(isVideoDetailMode){
            binding.root.dislike_option_title.setTextColor(Color.WHITE)
            binding.root.dislike_options_icon.setColorFilter(ContextCompat.getColor(
                    binding.root.dislike_options_icon.context,
                    R.color.vid_detail_btm_menu_icon_color))

        }
        return MenuOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as? MenuOptionViewHolder)?.bind(item)
    }

    fun updateData(menuList: List<MenuOption>, asset: CommonAsset?, pageEntity: PageEntity?) {
        this.asset = asset
        this.pageEntity = pageEntity
        submitList(menuList)
    }
}

/**
 * @author amit.chaudhary
 */
private class MenuOptionViewHolder(private val viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

    fun bind(item: MenuOption) {
        viewDataBinding.setVariable(BR.item, item)
        viewDataBinding.executePendingBindings()
    }
}


/**
 * @author amit.chaudhary
 */
class MenuDiffUtil : DiffUtil.ItemCallback<MenuOption>() {
    override fun areItemsTheSame(oldItem: MenuOption, newItem: MenuOption): Boolean {
        return oldItem.menuL1.id == newItem.menuL1.id && oldItem.menuL2?.id == newItem.menuL2?.id
    }

    override fun areContentsTheSame(oldItem: MenuOption, newItem: MenuOption): Boolean {
        return oldItem == newItem
    }
}