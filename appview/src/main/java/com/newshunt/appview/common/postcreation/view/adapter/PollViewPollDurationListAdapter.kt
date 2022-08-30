package com.newshunt.appview.common.postcreation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import com.newshunt.appview.R
import com.newshunt.dataentity.common.asset.PollDuration

class PollViewPollDurationListAdapter(private val context: Context?,
                                      private val pollLengthList: MutableList<PollDuration>?) : BaseAdapter() {

    private var viewHolder: ViewHolder? = null
    private val inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val pollLenght = getItem(position) as PollDuration
        var view: View? = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.cp_poll_lenght_item_view, parent, false)
            viewHolder = ViewHolder()
            viewHolder?.pollLenght = view?.findViewById(R.id.poll_lenght_text)
        } else {
            viewHolder = view.tag as? ViewHolder
        }
        viewHolder?.pollLenght?.text = pollLenght.displayString
        return view!!
    }

    override fun getItem(position: Int): Any {
        return pollLengthList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return pollLengthList!!.size
    }

    internal inner class ViewHolder {
        var pollLenght: CheckedTextView? = null
    }
}