package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.R
import com.newshunt.appview.databinding.PostCreateLocationViewBinding
import com.newshunt.dataentity.common.asset.PostCurrentPlace

class PostCreationLocationView : ConstraintLayout {

    private var locationData: PostCurrentPlace? = null
    private lateinit var viewBinding: PostCreateLocationViewBinding

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        initView()
        updateView()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.post_create_location_view,
            this,
            true
        )
    }

    fun setLocationResponse(data: PostCurrentPlace) {
        this.locationData = data
        updateView()
    }

    fun getLocationResponse(): PostCurrentPlace? = locationData

    private fun updateView() {
        if (locationData != null) {
            viewBinding.locationData = locationData
            viewBinding.executePendingBindings()
        }
    }

}