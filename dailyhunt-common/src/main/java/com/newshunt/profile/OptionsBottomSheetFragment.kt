/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.profile

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.model.entity.*
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.Priority
import com.newshunt.sdk.network.image.Image

/**
 * A Bottom sheet fragment to show various options in profile screens
 * <p>
 * Created by srikanth.ramaswamy on 07/01/2019.
 */

val bottomOptionIconDimension = Pair(CommonUtils.getDimensionInDp(R.dimen
        .filter_option_icon_width), CommonUtils.getDimensionInDp(R.dimen.filter_option_icon_width))

class OptionsBottomSheetFragment : BottomSheetDialogFragment(), OptionItemClickListener {
    private lateinit var optionsList: RecyclerView
    private var hostId: Int = 0//Host activity/fragment unique ID
    private var showImages = true
    private var currentOptionItem:SimpleOptionItem? = null
    private var selectedPosition = -1
    private var optionsAdapter:OptionsAdapter? = null
    private var profileFilterOption: ProfileFilterOption? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.options_bottomsheet, container, false)
        val rootView = view.findViewById<ConstraintLayout>(R.id.rootview_options)
        if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(), Constants.URDU_LANGUAGE_CODE)){
            ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_RTL)
        }
        else{
            ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_LTR)
        }
        val options = arguments?.getSerializable(KEY_DIALOG_OPTIONS) as? SimpleOptions?
        showImages = arguments?.getBoolean(SHOW_DIALOG_IMAGES) ?: true
        profileFilterOption = arguments?.getSerializable(FILTER_OPTION_DIALOG) as? ProfileFilterOption
        val headingView = view.findViewById<NHTextView>(R.id.heading)
        optionsList = (view as ViewGroup).findViewById(R.id.optionsList)
        options?.let {
            hostId = it.hostId
            optionsAdapter = OptionsAdapter(activity as Context, it.optionsList,showImages, this)
            optionsList.adapter = optionsAdapter
            if (!CommonUtils.isEmpty(it.heading)) {
                headingView.text = it.heading
                headingView.visibility = View.VISIBLE
            } else {
                headingView.visibility = View.GONE
            }
        }
        optionsList.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onResume() {
        super.onResume()
        updateSelectedItem()
    }

    private fun updateSelectedItem() {
        val index = optionsAdapter?.getItems()?.indexOfFirst { ((it.anyEnumerationAsEnum as? RunTimeProfileFilter?)?.filterOption as? ProfileFilterOption?) == profileFilterOption } ?: -1
        val optionItem = optionsAdapter?.getItemAtIndex(index)
        optionItem?.isSelected = true
        optionsAdapter?.updateItem(index)
        currentOptionItem = optionItem
        selectedPosition = index
    }

    override fun onOptionItemClicked(optionItem: SimpleOptionItem,position: Int) {
        if(!showImages) {
            updateItem(position,optionItem)
            return
        }
        dismiss()
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.value = FragmentCommunicationEvent(hostId, optionItem.anyEnumerationAsEnum)
        }
    }

    private fun updateItem(curPos:Int,optionItem: SimpleOptionItem) {
        optionsAdapter?.getItemAtIndex(selectedPosition)?.isSelected = false
        optionsAdapter?.updateItem(selectedPosition)
        currentOptionItem = optionItem
        optionItem.isSelected = true
        selectedPosition = curPos
        optionsAdapter?.updateItem(selectedPosition)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            if(!showImages) {
                currentOptionItem?.anyEnumerationAsEnum?.let {
                    viewModel.fragmentCommunicationLiveData.value = FragmentCommunicationEvent(hostId, it)
                    return
                }
            }
            viewModel.fragmentCommunicationLiveData.value = FragmentCommunicationEvent(hostId,
                    CommonMessageEvents.DISMISS)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(options: SimpleOptions,showImages:Boolean=true,filterOption: ProfileFilterOption?=null): OptionsBottomSheetFragment {
            val fragment = OptionsBottomSheetFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            bundle.putSerializable(KEY_DIALOG_OPTIONS, options)
            bundle.putSerializable(SHOW_DIALOG_IMAGES,showImages)
            bundle.putSerializable(FILTER_OPTION_DIALOG,filterOption)
            return fragment
        }
    }
}

class OptionsAdapter(private val context: Context,
                     private val items: List<SimpleOptionItem>,
                     private val showIcon:Boolean,
                     private val optionClickedListener: OptionItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val optionViewType = SimpleOptionItemType.getEnumFromIndex(viewType)
        if (optionViewType == SimpleOptionItemType.DIVIDER) {
            return DividerViewHolder(LayoutInflater.from(context).inflate(R.layout
                    .options_item_divder, parent, false))
        }
        return OptionsViewHolder(LayoutInflater.from(context).inflate(R.layout.options_item, parent, false), optionClickedListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItemAtIndex(index:Int):SimpleOptionItem? {
        return items.getOrNull(index)
    }

    fun getItems():List<SimpleOptionItem> {
        return items
    }

    fun updateItem(index: Int) {
        notifyItemChanged(index)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? OptionsViewHolder)?.updateView(items[position],position,showIcon)
    }

    override fun getItemViewType(position: Int): Int {
        val uiOptions = items[position].uiProperties ?: return SimpleOptionItemType.NORMAL.index
        return uiOptions.type.index
    }
}

class OptionsViewHolder(view: View,
                        private val optionClickedListener: OptionItemClickListener?) : RecyclerView.ViewHolder(view) {
    private val textView = view.findViewById<NHTextView>(R.id.options_textView)
    private val imgView = view.findViewById<ImageView>(R.id.options_icon)
    private val rootView = view.findViewById<ConstraintLayout>(R.id.options_rootview)
    private var optionItem: SimpleOptionItem? = null
    private var currentPos = -1

    init {
        itemView.setOnClickListener {
            optionItem ?: return@setOnClickListener
            optionClickedListener?.onOptionItemClicked(optionItem!!,currentPos)
        }
    }

    fun updateView(simpleOption: SimpleOptionItem,position: Int,showIcon: Boolean) {
        currentPos = position
        optionItem = simpleOption
        imgView.isSelected = simpleOption.isSelected == true
        textView.text = simpleOption.displayText
        if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(), Constants.URDU_LANGUAGE_CODE)) {
            ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_RTL)
        } else {
            ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_LTR)
        }
        val imageUrl = ImageUrlReplacer.getQualifiedUrl(simpleOption.iconUrl ?: Constants.EMPTY_STRING,
                bottomOptionIconDimension)
        if(showIcon) {
            if (!CommonUtils.isEmpty(imageUrl)) {
                imgView.visibility = View.VISIBLE
                Image.load(imageUrl).priority(Priority.PRIORITY_NORMAL)
                    .placeHolder(com.newshunt.common.util.R.color.empty_image_color)
                    .into(imgView)
            } else {
                simpleOption.drawableId?.let {
                    if (it != View.NO_ID) {
                        imgView.visibility = View.VISIBLE
                        imgView.setImageDrawable(CommonUtils.getDrawable(it))
                    }
                }
            }
        }
        simpleOption.uiProperties?.let { uiOption ->
            imgView.isSelected = uiOption.selected
            uiOption.textColor?.let { textColor ->
                textView.setTextColor(textColor)
            }
            uiOption.imageIconSize?.let { iconSize ->
                imgView.layoutParams.width = iconSize
                imgView.layoutParams.height = iconSize
            }
        }

    }
}

class DividerViewHolder(view: View) : RecyclerView.ViewHolder(view)
