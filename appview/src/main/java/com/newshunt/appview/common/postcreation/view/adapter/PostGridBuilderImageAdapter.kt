/*
 * Created by Rahul Ravindran at 2/9/19 11:45 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.databinding.PostCreateGridItemLayoutBinding
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.HeaderRecyclerViewAdapter
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.social.entity.ImageEntity
import com.newshunt.dataentity.social.entity.MAX_IMAGE_COUNT
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.usecase.CpImageInsertUseCase
import com.newshunt.news.model.usecase.MediatorUsecase
import org.jetbrains.annotations.TestOnly
import java.io.File
import kotlin.math.min

/*
* Adapter for Recyclerview with functionality to remove image items held by a capacity field
* */

class GridBuilderImageAdapter : HeaderRecyclerViewAdapter {
    private var capacity = PreferenceManager.getPreference(
        AppStatePreference.POST_CREATE_MAX_IMAGE_SIZE, MAX_IMAGE_COUNT
    )
    //for now making it as array of string. should be Uri
    private var imageList = mutableListOf<ImageDetail>()
    private val IMAGE_VIEWHOLDER_TYPE = 1;
    private lateinit var headerAction: () -> Unit
    private var gridItemRemoveCallback: GridItemRemoveCallback? = null
    private var imgDao: MediatorUsecase<Bundle, Boolean>? = null
    private var cpId: Long = -1

    fun setGridItemRemoveCallback(gridItemRemoveCallback: GridItemRemoveCallback?) {
        this.gridItemRemoveCallback = gridItemRemoveCallback
    }

    private var callback = object : RemoveItemCallback {
        override fun onRemoveItem(position: Int, holder: RecyclerView.ViewHolder) {
            val pos = position - 1
            if (!useHeader()) {
                imgDao?.execute(bundleOf(CpImageInsertUseCase.IMG_ENTITY to ImageEntity(
                        cpId = cpId,
                        imgHeight = imageList[position].height.toInt(),
                        imgWidth = imageList[position].width.toInt(),
                        imgPath = imageList[position].url,
                        imgOrientation = imageList[position].orientation,
                        imgRes = imageList[position].resolution,
                        imgFormat = ""
                ), CpImageInsertUseCase.IS_REMOVE to true, CpImageInsertUseCase.POST_ID to cpId))
                deleteCopiedImage(imageList[position].url)
                imageList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(1, imageList.size - 1)
                gridItemRemoveCallback?.onGridItemRemoved()
                return
            }

            imgDao?.execute(bundleOf(CpImageInsertUseCase.IMG_ENTITY to ImageEntity(
                    cpId = cpId,
                    imgHeight = imageList[pos].height.toInt(),
                    imgWidth = imageList[pos].width.toInt(),
                    imgPath = imageList[pos].url,
                    imgOrientation = imageList[pos].orientation,
                    imgRes = imageList[pos].resolution,
                    imgFormat = ""
            ), CpImageInsertUseCase.IS_REMOVE to true, CpImageInsertUseCase.POST_ID to cpId))
            deleteCopiedImage(imageList[pos].url)
            imageList.removeAt(pos)
            notifyItemRemoved(position)
            if (imageList.isEmpty()) {
                notifyItemRemoved(0)
            }
            gridItemRemoveCallback?.onGridItemRemoved()
        }
    }
        @TestOnly
        get() = field


    constructor(cpId: Long) : super() {
        this.cpId = cpId;
    }

    /*
    *  @param capacity: Int
    * @param action: () -> Unit
    * */
    constructor (cpId: Long, imgDao: MediatorUsecase<Bundle, Boolean>, action: () -> Unit) {
        this.cpId = cpId
        this.headerAction = action
        this.imgDao = imgDao
    }

    fun updateList(vararg uris: ImageDetail): Boolean {
        if (imageList.size < capacity) {
            val oldSize = imageList.size
            val totalSize = oldSize + uris.size
            if (totalSize >= capacity) {
                notifyItemRemoved(0)
            }
            val oldItemCount = itemCount
            imageList.addAll(uris.slice(IntRange(0, min(capacity - oldSize, uris.size) -1)))
            imageList.forEach {
                imgDao?.execute(bundleOf(CpImageInsertUseCase.IMG_ENTITY to ImageEntity(
                        cpId = cpId,
                        imgHeight = it.height.toInt(),
                        imgWidth = it.width.toInt(),
                        imgPath = it.url,
                        imgOrientation = it.orientation,
                        imgRes = it.resolution,
                        imgFormat = it.format
                )))
            }
            notifyItemRangeInserted(oldItemCount, itemCount)
            return true
        }
        return false
    }

    fun getAttachmentSize(): Int = capacity

    override fun useHeader(): Boolean = imageList.size in 1 until capacity


    override fun onCreateHeaderViewHolder(
            parent: ViewGroup?,
            viewType: Int
    ): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.post_create_plus_grid_item_layout, parent, false)
        return CreatePostPlusImageViewHolder(rootView)
    }

    override fun onBindHeaderView(holder: RecyclerView.ViewHolder?, position: Int) {
        holder?.itemView?.let {
            it.setOnClickListener {
                //trigger for gallery explicit intent
                if (::headerAction.isInitialized) headerAction.invoke()
            }
        }
    }

    override fun useFooter(): Boolean = false

    override fun onCreateFooterViewHolder(
            parent: ViewGroup?,
            viewType: Int
    ): RecyclerView.ViewHolder {
        TODO("no implementation")
    }

    override fun onBindFooterView(holder: RecyclerView.ViewHolder?, position: Int) {}

    override fun onCreateBasicItemViewHolder(
            parent: ViewGroup?,
            viewType: Int
    ): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<PostCreateGridItemLayoutBinding>(
                LayoutInflater.from(parent?.context),
                R.layout.post_create_grid_item_layout,
                parent, false)
        return CreatePostImageViewHolder(binding, callback)

    }

    override fun onBindBasicItemView(holder: RecyclerView.ViewHolder?, position: Int) {
        //update image viewholder with data from image
        if (holder is CreatePostImageViewHolder) {
            holder.updateView(imageList[position])
        }
    }

    override fun getBasicItemCount(): Int = imageList.size

    override fun getBasicItemType(position: Int): Int = IMAGE_VIEWHOLDER_TYPE

    private fun deleteCopiedImage(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists() && file.name.contains(
                    PostConstants.POST_CREATE_IMAGE_FILE_NAME, ignoreCase = true
                )
            ) {
                file.delete()
            }
        } catch (e: Exception) {
        }
    }

    fun destroy() {
        imgDao?.dispose()
    }

}

interface RemoveItemCallback {
    fun onRemoveItem(position: Int, holder: RecyclerView.ViewHolder)
}

interface GridItemRemoveCallback {
    fun onGridItemRemoved()
}

class CreatePostImageViewHolder(
        private val binding: PostCreateGridItemLayoutBinding,
        private val callback: RemoveItemCallback
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(data: ImageDetail) {
        binding.item = data
        binding.executePendingBindings()

        binding.gridImageDeleteButton.setOnClickListener {
            callback.onRemoveItem(position = adapterPosition, holder = CreatePostImageViewHolder@ this)
        }
    }
}

class CreatePostPlusImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var gridPlusImageView: NHRoundedCornerImageView = itemView
            .findViewById(R.id.grid_plus_imageview)
}

