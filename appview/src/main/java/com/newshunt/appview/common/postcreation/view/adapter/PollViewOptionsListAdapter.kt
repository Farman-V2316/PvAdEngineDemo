package com.newshunt.appview.common.postcreation.view.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.customview.PollViewCallback
import com.newshunt.appview.common.postcreation.view.holders.PollOptionViewHolder
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.PostPollOption
import com.newshunt.dataentity.social.entity.MAX_POLL_OPTION_LENGTH
import com.newshunt.dhutil.helper.preference.AppStatePreference

private const val MAX_POLL_OPTIONS_SIZE = 4
class PollViewOptionsListAdapter(private val pollViewCallback: PollViewCallback) :
        PostCreateBaseRecyclerAdapter<PostPollOption, RecyclerView.ViewHolder>(),
        PollOptionEditTextCallback {

    private val POLL_OPTIONS_TYPE = 1
    private val pollOptionsList: MutableList<PostPollOption> = mutableListOf()
    private var maxPollOptionLength = MAX_POLL_OPTION_LENGTH

    init {
        pollOptionsList.add(0, PostPollOption())
        pollOptionsList.add(1, PostPollOption())
        dataItems.addAll(pollOptionsList)
        maxPollOptionLength = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_POLL_OPTION_LENGTH, MAX_POLL_OPTION_LENGTH
        )
    }


    fun addNewPollOption() {
        if (pollOptionsList.size < MAX_POLL_OPTIONS_SIZE) {
            val poll = PostPollOption()
            pollOptionsList.add(poll)
            dataItems.add(poll)
            notifyItemInserted(itemCount)
        }
        handleAddPollOptionView()
    }

    private fun handleAddPollOptionView() {
        // call createpostPollView Mathod
        pollViewCallback.handlePVAddNewOption(pollOptionsList.size < MAX_POLL_OPTIONS_SIZE)
    }

    override fun createBasicItemViewHolder(
            layoutInflater: LayoutInflater?,
            viewGroup: ViewGroup?,
            viewType: Int): RecyclerView.ViewHolder {
        return PollOptionViewHolder(
                layoutInflater!!.inflate(
                        R.layout.post_create_poll_option_item_vh,
                        viewGroup, false), this, maxPollOptionLength)
    }

    override fun bindBasicItemViewHolder(
            viewHolder: RecyclerView.ViewHolder,
            data: PostPollOption?,
            position: Int) {
        if (viewHolder is PollOptionViewHolder) {
            viewHolder.updateView(position)
        }
    }

    override fun getItemCount(): Int {
        return pollOptionsList.size
    }

    override fun getBasicItemType(position: Int): Int {
        return POLL_OPTIONS_TYPE
    }

    override fun uniqueIdItem(position: Int): Long {
        return position.toLong()
    }

    override fun getPollOptionEditText(position: Int, pollOptionEditable: Editable) {
        val postPollOption = getItemAtPosition(position)
        if (pollOptionEditable.isNotEmpty()) {
            postPollOption?.title = pollOptionEditable.toString()
            postPollOption?.id = position.toString()
        }
    }

    fun getPostPollOptionData(): MutableList<PostPollOption>? {
        return pollOptionsList
    }

    override fun onTextChanged(isValidLength: Boolean) {
        pollViewCallback.onTextChanged(isValidLength)
    }
}

interface PollOptionEditTextCallback {
    fun getPollOptionEditText(position: Int, pollOptionEditable: Editable)
    fun onTextChanged(isValidLength: Boolean)
}