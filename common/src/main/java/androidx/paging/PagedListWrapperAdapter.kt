package androidx.paging

import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.common.helper.common.Logger

abstract class PagedListWrapperAdapter<T, VH : RecyclerView.ViewHolder>(private val diffCallback: DiffUtil
.ItemCallback<T>) : RecyclerView.Adapter<VH>() {

    private val updatePagedListListener = object : AsyncPagedListDiffer2.PagedListListener<T> {
        override fun onCurrentListChanged(previousList: PagedListWrapper<T>?, currentList: PagedListWrapper<T>?) {
            this@PagedListWrapperAdapter.onCurrentListChanged(previousList, currentList)
        }
    }

    private val updateSimpleListListener = AdapterListUpdateCallback(this)
    private var listType = NOT_DEFINED
        private set(value) {
            if (field != NOT_DEFINED) {
                if (field != value) {
                    Logger.e(LOG_TAG, "can not change list type once set. $field, $value")
                }
            } else {
                field = value
            }
        }


    private val pagedAsyncDiffer = AsyncPagedListDiffer2<T>(this, diffCallback)

    private val simpleAsyncDiffer = AsyncListDiffer<T>(updateSimpleListListener,
            AsyncDifferConfig.Builder<T>(diffCallback).build())

    init {
        pagedAsyncDiffer.addPagedListListener(updatePagedListListener)
    }

    open fun submitList(pagedList: PagedList<T>?, commitCallback: Runnable? = null,
                        clearExtraItems: Boolean = false) {
        if (listType != LIST_TYPE_PAGED && listType!= NOT_DEFINED) {
            Logger.e(LOG_TAG, "submit list type not supported")
            return
        }
        listType = LIST_TYPE_PAGED
        if (pagedList is PagedListWrapper<T>) {
            pagedAsyncDiffer.submitList(pagedList, commitCallback)
        } else {
            val pagedListCurrent = getPagedListWrapper()
            if (pagedListCurrent != null && pagedList != null
                    && !clearExtraItems
                    && canReuseOldPagedList(pagedList, pagedListCurrent.source)) {
                pagedList.lastKey
                pagedAsyncDiffer.submitList(pagedListCurrent.updateSource(pagedList,
                        MapItemsPositionDiffHelper<T>()), commitCallback)
            } else {
                pagedAsyncDiffer.submitList(pagedList?.let { PagedListWrapper(it) }, commitCallback)
            }
        }
    }

    private fun canReuseOldPagedList(newList: PagedList<T>, oldList: PagedList<T>): Boolean {
        return newList.snapshot().size != 0 && newList.mStorage.size == oldList.mStorage.size
    }

    open fun submitList(list: List<T>?, commitCallback: Runnable? = null, clearExtraItems: Boolean = false) {
        if (list is PagedList<T>) {
            submitList(list, commitCallback, clearExtraItems)
            return
        }
        if (listType != LIST_TYPE_SIMPLE && listType!= NOT_DEFINED) {
            Logger.e(LOG_TAG, "submit list type not supported")
            return
        }
        listType = LIST_TYPE_SIMPLE
        simpleAsyncDiffer.submitList(list)
        commitCallback?.run()
    }

    protected open fun getItem(position: Int): T? {
        if (listType == LIST_TYPE_PAGED) {
            return pagedAsyncDiffer.getItem(position)
        } else {
            return simpleAsyncDiffer.currentList.get(position)
        }
    }

    override fun getItemCount(): Int {
        if (listType == LIST_TYPE_PAGED) {
            return pagedAsyncDiffer.itemCount
        } else {
            return simpleAsyncDiffer.currentList.size
        }
    }

    fun getCurrentList(): List<T>? {
        if (listType == LIST_TYPE_PAGED) {
            return pagedAsyncDiffer.currentList
        } else {
            return simpleAsyncDiffer.currentList
        }
    }

    fun getSnapshot(): List<T>? {
        if (listType == LIST_TYPE_PAGED) {
            return pagedAsyncDiffer.currentList?.snapshot()
        } else {
            return simpleAsyncDiffer.currentList
        }
    }


    fun getPagedListWrapper(): PagedListWrapper<T>? {
        if (listType == LIST_TYPE_PAGED) {
            return pagedAsyncDiffer.currentList
        }
        return null
    }

    open fun onCurrentListChanged(
            previousList: PagedList<T>?, currentList: PagedList<T>?) {
    }

    companion object {
        const val LIST_TYPE_PAGED: Int = 1
        const val LIST_TYPE_SIMPLE: Int = 2
        private const val NOT_DEFINED: Int = 3
        const val LOG_TAG = "PagedListWrapper"
    }
}