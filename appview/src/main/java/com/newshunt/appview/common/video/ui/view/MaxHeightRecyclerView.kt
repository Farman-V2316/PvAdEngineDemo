package com.newshunt.appview.common.video.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.makeMeasureSpec
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.dataentity.common.helper.common.CommonUtils

class MaxHeightRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val heightSpecs = makeMeasureSpec(
            CommonUtils.getDimension(R.dimen.video_tag_view_height), MeasureSpec.AT_MOST
        )
        super.onMeasure(widthSpec, heightSpecs)
    }

}