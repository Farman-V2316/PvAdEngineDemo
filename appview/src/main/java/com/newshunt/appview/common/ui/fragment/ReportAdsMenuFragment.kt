/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.newshunt.adengine.ReportAdsMenuUsecase
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ReportAdsMenuEntity
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ReportAdsMenuOptionEntity
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image

/**
 * A bottom sheet dialog fragment to show the ads report option.
 *
 * @author shashikiran.nr
 */
class ReportAdsMenuFragment(private val parentFragment: Fragment,
                            private val reportAdsMenuEntity: ReportAdsMenuEntity?,
                            reportAdsEntity: BaseAdEntity?,
                            private val reportedAdParentUniqueAdIdIfCarousal: String,
                            private val onAdReportedListener: OnAdReportedListener? = null)
    : BottomSheetDialog(parentFragment.requireContext()), AdapterView.OnItemClickListener {

    private var reportAdsMenuOptionConfigListAdapter: ReportAdsMenuOptionListAdapter? = null
    private var reportAdsEntity: BaseDisplayAdEntity? = null
    private var isReportAdsMenuCalled: Boolean = false

    init {
        this.reportAdsEntity = if (reportAdsEntity is MultipleAdEntity) {
            reportAdsEntity.baseDisplayAdEntities.firstOrNull()
        } else {
            reportAdsEntity as? BaseDisplayAdEntity
        }
        initDialog()
        setOnDismissListener {
           onAdReportedListener?.onAdReportDialogDismissed(reportAdsEntity, reportedAdParentUniqueAdIdIfCarousal)
        }
    }

    private fun initDialog() {
        val adsMenuOtionBottomSheetView = layoutInflater.inflate(R.layout
                .report_ads_menu_option_view, null)
        setContentView(adsMenuOtionBottomSheetView)

        val adsMenuOptionBottomSheetBehavior =
                BottomSheetBehavior.from(adsMenuOtionBottomSheetView.parent as View)
        adsMenuOptionBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(p0: View, p1: Int) {
            }

        })

        val adsMenuOptionListView = adsMenuOtionBottomSheetView.run {
            findViewById<View>(R.id.report_ads_menu_option_lv)
        } as? ListView

        reportAdsMenuOptionConfigListAdapter = ReportAdsMenuOptionListAdapter(context,
                reportAdsMenuEntity!!.options)
        adsMenuOptionListView?.adapter = reportAdsMenuOptionConfigListAdapter
        adsMenuOptionListView?.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        reportAdsMenuEntity ?: dismiss()
        reportAdsEntity ?: dismiss()
        if(reportAdsMenuEntity?.options != null){
            if(reportAdsMenuEntity.options[position].showWebForm){
                val browserIntent = NhBrowserNavigator.getTargetIntent()
                browserIntent.putExtra(DailyhuntConstants.URL_STR, reportAdsMenuEntity.url)
                // Setting the view port of Feedback wedview to true alsways.
                browserIntent.putExtra(DailyhuntConstants.USE_WIDE_VIEW_PORT, true)
                browserIntent.putExtra(DailyhuntConstants.CLEAR_HISTORY_ON_PAGE_LOAD,
                        reportAdsEntity?.clearHistoryOnPageLoad)
                browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, true)
                browserIntent.putExtra(Constants.REPORT_ADS_MENU_SELETED_L1_OPTION_ID,
                        reportAdsMenuEntity.options[position].id)
                browserIntent.putExtra(Constants.REPORTED_ADS_ENTITY, reportAdsEntity)
                browserIntent.putExtra(Constants.PARENT_UNIQUE_ADID_REPORTED_ADS_ENTITY,
                        reportedAdParentUniqueAdIdIfCarousal)
                parentFragment?.startActivityForResult(browserIntent, Constants.REPORTED_ADS_RESULT_CODE)
                dismiss()
            } else{
                isReportAdsMenuCalled = true
                ReportAdsMenuUsecase().invoke(bundleOf(
                        ReportAdsMenuUsecase.REQUEST_URL to
                                reportAdsEntity?.reportAdsMenuFeedBackEntity?.feedbackUrl,
                        ReportAdsMenuUsecase.DATA_TO_SEND to
                                reportAdsMenuEntity.options[position].dataToSend)).subscribe()
                reportAdsMenuEntity.options[position].thankYouMessage.forEach {
                    if(it.key == AppUserPreferenceUtils.getUserPrimaryLanguage()){
                        parentFragment?.view?.let {view ->
                            FontHelper.showCustomSnackBar(view, it.value, Snackbar.LENGTH_SHORT,
                                null, null)
                        }
                    }
                }
                reportAdsEntity?.let { ad ->
                    if (reportAdsMenuEntity.options[position].collapseOnSubmit) {
                        onAdReportedListener?.onAdReported(ad, reportedAdParentUniqueAdIdIfCarousal)
                    }
                }
                dismiss()
            }
        }
    }
}

class ReportAdsMenuOptionListAdapter(context: Context?,
                                     private val reportAdsMenuOptionEntityList:
                                     List<ReportAdsMenuOptionEntity>?) : BaseAdapter(){

    private var viewHolder: ViewHolder? = null
    private val inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val reportAdsMenuOptionConfig = reportAdsMenuOptionEntityList?.get(position)
        var view: View? = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.report_ads_menu_option_item_view, parent, false)
            viewHolder = ViewHolder()
            viewHolder?.adsMenuOptionItemTextView = view?.findViewById(R.id.report_ads_menu_option_text)
            viewHolder?.adsMenuOptionIconImageView = view?.findViewById(R.id._report_ads_menu_option_icon)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as? ViewHolder
        }

        reportAdsMenuOptionConfig?.labels?.forEach {
            if (it.key == AppUserPreferenceUtils.getUserPrimaryLanguage()) {
                viewHolder?.adsMenuOptionItemTextView?.text = it.value
            }
        }

        val dimension = CardsBindUtils.getMenuOptionL1IconSize()
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(reportAdsMenuOptionConfig?.iconUrl,
                        dimension.first, dimension.second)
        Image.load(qualifiedUrl).apply(RequestOptions().dontTransform()).into(viewHolder?.adsMenuOptionIconImageView)
        return view!!
    }

    override fun getItem(position: Int): Any {
        return reportAdsMenuOptionEntityList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return reportAdsMenuOptionEntityList!!.size
    }

    internal  inner class ViewHolder{
        var adsMenuOptionItemTextView : NHTextView? = null
        var adsMenuOptionIconImageView : ImageView? = null
    }
}