package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.adapter.PollViewPollDurationListAdapter
import com.newshunt.dataentity.common.asset.PollDuration
import com.newshunt.dataentity.common.helper.common.CommonUtils


class PollViewPollDurationBSDialog : BottomSheetDialog, AdapterView.OnItemClickListener {

    private var defaultPollDurationPosition: Int? = -1
    private var pollDurationList: MutableList<PollDuration>? = mutableListOf()
    private var pvPollDurationListAdapter: PollViewPollDurationListAdapter? = null
    private var pvCallBack: PollViewCallback? = null

    constructor(context: Context, pollDurationCallback: PollViewCallback,
                pollDurationList: MutableList<PollDuration>?,
                defaultPollDurationPosition: Int?) : super(context) {

        this.pvCallBack = pollDurationCallback
        this.pollDurationList = pollDurationList
        this.defaultPollDurationPosition = defaultPollDurationPosition
        initDailog()
    }

    private fun initDailog() {
        val pollDurationBottomSheetView = layoutInflater.inflate(R.layout
                .post_create_poll_length_view, null)
        setContentView(pollDurationBottomSheetView)

        var pollDurationBottomSheetBehavior =
                BottomSheetBehavior.from(pollDurationBottomSheetView.parent as View)
        pollDurationBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val pollDurationListView = pollDurationBottomSheetView.run {
            findViewById<View>(R.id.poll_length_option_lv)
        } as? ListView

        pvPollDurationListAdapter = PollViewPollDurationListAdapter(context, pollDurationList)
        pollDurationListView?.adapter = pvPollDurationListAdapter
        pollDurationListView?.onItemClickListener = this
        if(defaultPollDurationPosition != -1){
            pollDurationListView?.setItemChecked(defaultPollDurationPosition!!, true)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val pollLenght = parent?.adapter?.getItem(position) as PollDuration
        if(!CommonUtils.isEmpty(pollLenght.displayString) && pollLenght.duration != null){
            pvCallBack?.handlePVPollDuration(pollLenght.displayString!!, pollLenght
                    .duration!!, position)
        }
        dismiss()
    }
}
