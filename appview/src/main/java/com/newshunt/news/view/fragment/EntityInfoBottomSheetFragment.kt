/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newshunt.appview.R
import com.newshunt.appview.common.viewmodel.EntityInfoBottomSheetDescViewModel
import com.newshunt.appview.databinding.LayoutNerDescriptionBsFragmentBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.di.DaggerNERDescriptionComponent
import com.newshunt.news.helper.NHWebViewJSInterface
import com.newshunt.news.util.NewsConstants
import javax.inject.Inject

/**
 * Fragment class for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */
class NERDescriptionBottomSheetFragment:BottomSheetDialogFragment() {
    private lateinit var viewBinding:LayoutNerDescriptionBsFragmentBinding
    private var contentUrl:String? = null
    private lateinit var vm: EntityInfoBottomSheetDescViewModel
    @Inject
    lateinit var vmF: EntityInfoBottomSheetDescViewModel.EntityInfoBottomSheetDescViewModelF

    override fun onCreate(savedInstanceState: Bundle?) {
        contentUrl = arguments?.getString(Constants.NER_DESCRIPTION_URL) ?: Constants.EMPTY_STRING
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.layout_ner_description_bs_fragment,container,false)
        DaggerNERDescriptionComponent
            .builder()
            .build()
            .inject(this)
        vm = ViewModelProviders.of(this,vmF)[EntityInfoBottomSheetDescViewModel::class.java]
        NHWebViewUtils.initializeWebView(viewBinding.descriptionWebview)
        viewBinding.descriptionWebview.addJavascriptInterface(NHWebViewJSInterface(viewBinding.descriptionWebview, activity), NHWebViewJSInterface.INTERFACE_NAME)
        loadData()
        updateShimmerView(true)
        return viewBinding.root
    }

    private fun loadData() {
        vm.webResData.observe(this,  {
            it?.let {
                viewBinding.descriptionWebview.loadDataWithBaseURL(NewsBaseUrlContainer.getApplicationUrl(),
                    HtmlFontHelper.wrapDataWithHTML(it),
                    NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null)
                updateShimmerView(false)
                viewBinding.headerDivider.visibility = View.VISIBLE
                viewBinding.descriptionWebview.visibility = View.VISIBLE
            }
        })
        vm.fetchEntityBottomSheetWebInfo(contentUrl)
    }

    private fun updateShimmerView(visibility:Boolean = false) {
        viewBinding.layoutShimmer.root.visibility = if (visibility) View.VISIBLE else View.GONE
    }

    companion object {
        fun instance(descText:String?):NERDescriptionBottomSheetFragment {
            val fragment = NERDescriptionBottomSheetFragment()
            val bundle = Bundle()
            descText?.let {
                bundle.putString(Constants.NER_DESCRIPTION_URL,it)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}