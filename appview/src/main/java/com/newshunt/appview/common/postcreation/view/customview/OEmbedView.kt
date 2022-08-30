package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.R
import com.newshunt.appview.databinding.PostCreateOembedViewBinding
import com.newshunt.dataentity.common.asset.OEmbedResponse

class OEmbedView : RelativeLayout, View.OnClickListener {
    private var oEmbedData: OEmbedResponse? = null
    private var showOEmbedLoader = false
    private lateinit var oEmbedViewBinging : PostCreateOembedViewBinding
    private var ogRemoveCallback: OgRemoveCallback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
            attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initView()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        oEmbedViewBinging = DataBindingUtil.inflate<PostCreateOembedViewBinding>(inflater,
                R.layout.post_create_oembed_view,
                this,
                true)
        oEmbedViewBinging.oembedRemoveIv.setOnClickListener(this)
    }

    fun setShowLoader(value: Boolean){
        showOEmbedLoader = value
        oEmbedViewBinging.showLoader = showOEmbedLoader
        oEmbedViewBinging.executePendingBindings()
    }

    fun setOEmbedResponse(data: OEmbedResponse) {
        this.oEmbedData = data
        oEmbedViewBinging.data = oEmbedData
        oEmbedViewBinging.executePendingBindings()
    }


    fun hideView(){
        removeAllViews()
    }

    fun setOgRemoveCallback(callback: OgRemoveCallback) {
        this.ogRemoveCallback = callback
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.oembed_remove_iv) {
            ogRemoveCallback?.onOgViewRemove(this)
        }
    }

    interface OgRemoveCallback {
        fun onOgViewRemove(view: View)
    }
}
