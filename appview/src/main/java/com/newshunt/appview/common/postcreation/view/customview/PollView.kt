package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.adapter.PollViewOptionsListAdapter
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.asset.PollDuration
import com.newshunt.dataentity.common.asset.PostPollOption
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.util.concurrent.TimeUnit

class PollView : RelativeLayout, View.OnClickListener, PollViewCallback {

    private lateinit var pvRemoveImageView: NHImageView
    private lateinit var pvAddOptionImageView: NHImageView
    private lateinit var pvPollLenghtLayout: LinearLayout
    private lateinit var pvPollLenghtTextView: NHTextView
    private lateinit var pvOptionsListRecyclerView: RecyclerView
    private var pvOptionsListAdapter: PollViewOptionsListAdapter? = null
    private var pvDefaultPollDurationPosition: Int = -1 // default poll lenght position
    private var pollDurationList: MutableList<PollDuration>? = mutableListOf()
    private var callback: PollViewRemoveCallback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
            context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initView()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.post_create_poll_view, this, true)
        pvRemoveImageView = view!!.findViewById(R.id.remove_polls_view_iv)
        pvOptionsListRecyclerView = view.findViewById(R.id.recycler_list_polls_view)
        pvAddOptionImageView = view.findViewById(R.id.add_poll_option_iv)
        pvPollLenghtLayout = view.findViewById(R.id.poll_lenght_layout)
        pvPollLenghtTextView = view.findViewById(R.id.poll_lenght_tv)
        pvPollLenghtLayout.setOnClickListener(this)
        pvOptionsListRecyclerView.also {
            it.layoutManager = LinearLayoutManager(context)
            pvOptionsListAdapter = PollViewOptionsListAdapter(this)
            it.adapter = pvOptionsListAdapter
        }

        pvRemoveImageView.setOnClickListener(this)
        pvAddOptionImageView.setOnClickListener(this)

        val json = PreferenceManager.getPreference(AppStatePreference.POST_CREATE_POLL_DURATION, Constants.EMPTY_STRING)
        val type = object : TypeToken<List<PollDuration>>(){}.type
        pollDurationList = JsonUtils.fromJson(json, type)

        if(pollDurationList.isNullOrEmpty()){
            pollDurationList = mutableListOf()
            pollDurationList?.add(0, PollDuration("6 hours", TimeUnit.HOURS.toMillis(6)))
            pollDurationList?.add(1, PollDuration("12 hours", TimeUnit.HOURS.toMillis(12)))
            pollDurationList?.add(2, PollDuration("1 day", TimeUnit.DAYS.toMillis(1)))
            pollDurationList?.add(3, PollDuration("2 day", TimeUnit.DAYS.toMillis(2)))
            pollDurationList?.add(4, PollDuration("4 day", TimeUnit.DAYS.toMillis(4)))
        }

        if (pvDefaultPollDurationPosition == -1) {
            // setting default option to 0th position
            pvDefaultPollDurationPosition = 0
            pvPollLenghtTextView.text =
                    pollDurationList?.get(pvDefaultPollDurationPosition)?.displayString ?:
                    Constants.EMPTY_STRING
        } else {
            pvPollLenghtTextView.text =
                    pollDurationList?.get(pvDefaultPollDurationPosition)?.displayString ?:
                    Constants.EMPTY_STRING
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.remove_polls_view_iv -> {
                callback?.onPollViewRemoved(PollView@this)
            }

            R.id.add_poll_option_iv -> {
                pvOptionsListAdapter?.addNewPollOption()
                callback?.onPollOptionAdded()
            }

            R.id.poll_lenght_layout -> initPollLengthBottomSheet()
        }
    }

    override fun handlePVAddNewOption(value: Boolean) {
        pvAddOptionImageView.visibility = if (value) View.VISIBLE else View.GONE
        callback?.onPollTextChanged(false)
    }

    override fun handlePVPollDuration(
            pollDurationString: String, pollDurationValue: Long, pollDurationPostion: Int) {
        pvDefaultPollDurationPosition = pollDurationPostion
        pvPollLenghtTextView.text = pollDurationString
    }


    fun getPVPollOptionList(): MutableList<PostPollOption>? {
        return pvOptionsListAdapter?.getPostPollOptionData()
    }

    fun getPVPollDuration(): Long? {
        return pollDurationList?.get(pvDefaultPollDurationPosition)?.duration
    }

    private fun initPollLengthBottomSheet() {
        PollViewPollDurationBSDialog(
                context,
                this,
                pollDurationList,
                pvDefaultPollDurationPosition
        ).show()
    }

    fun setPollViewRemoveCallback(callback: PollViewRemoveCallback) {
        this.callback = callback
    }

    override fun onTextChanged(isValidLength: Boolean) {
        callback?.onPollTextChanged(isValidLength)
    }

    fun isPollViewValid(): Boolean {
        // 1. check for poll option characters
        if(pvOptionsListAdapter == null ){
            return false
        }

        for(x in 0 until pvOptionsListAdapter?.itemCount!!){
            val pollOption = pvOptionsListAdapter?.getItemAtPosition(x)
            return if(pollOption != null && !CommonUtils.isEmpty(pollOption.title)){
                if(pollOption.title!!.length <= 25){
                    if(x != pvOptionsListAdapter?.itemCount!! - 1){
                        continue
                    } else {
                        true
                    }
                } else {
                    false
                }
            } else {
                false
            }
        }
        return false
    }
}

interface PollViewCallback {
    fun handlePVAddNewOption(value: Boolean)
    fun handlePVPollDuration(
            pollDurationString: String, pollDurationValue: Long,
            pollDurationPostion: Int)
    fun onTextChanged(isValidLength: Boolean)
}

interface PollViewRemoveCallback {
    fun onPollViewRemoved(view: View)
    fun onPollTextChanged(isValidLength: Boolean)
    fun onPollOptionAdded()
}

