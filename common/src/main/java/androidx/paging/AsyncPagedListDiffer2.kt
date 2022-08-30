/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.paging

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Helper object for mapping a [PagedList] into a
 * [RecyclerView.Adapter][androidx.recyclerview.widget.RecyclerView.Adapter].
 *
 *
 * For simplicity, the [PagedListAdapter] wrapper class can often be used instead of the
 * differ directly. This diff class is exposed for complex cases, and where overriding an adapter
 * base class to support paging isn't convenient.
 *
 *
 * When consuming a [LiveData] of PagedList, you can observe updates and dispatch them
 * directly to [.submitList]. The AsyncPagedListDiffer then can present this
 * updating data set simply for an adapter. It listens to PagedList loading callbacks, and uses
 * DiffUtil on a background thread to compute updates as new PagedLists are received.
 *
 *
 * It provides a simple list-like API with [.getItem] and [.getItemCount] for an
 * adapter to acquire and present data objects.
 *
 *
 * A complete usage pattern with Room would look like this:
 * <pre>
 * @Dao
 * interface UserDao {
 * @Query("SELECT * FROM user ORDER BY lastName ASC")
 * public abstract DataSource.Factory&lt;Integer, User> usersByLastName();
 * }
 *
 * class MyViewModel extends ViewModel {
 * public final LiveData&lt;PagedList&lt;User>> usersList;
 * public MyViewModel(UserDao userDao) {
 * usersList = new LivePagedListBuilder&lt;>(
 * userDao.usersByLastName(), /* page size */ 20).build();
 * }
 * }
 *
 * class MyActivity extends AppCompatActivity {
 * @Override
 * public void onCreate(Bundle savedState) {
 * super.onCreate(savedState);
 * MyViewModel viewModel = ViewModelProviders.of(this).get(MyViewModel.class);
 * RecyclerView recyclerView = findViewById(R.id.user_list);
 * final UserAdapter adapter = new UserAdapter();
 * viewModel.usersList.observe(this, pagedList -> adapter.submitList(pagedList));
 * recyclerView.setAdapter(adapter);
 * }
 * }
 *
 * class UserAdapter extends RecyclerView.Adapter&lt;UserViewHolder> {
 * private final AsyncPagedListDiffer&lt;User> mDiffer
 * = new AsyncPagedListDiffer(this, DIFF_CALLBACK);
 * @Override
 * public int getItemCount() {
 * return mDiffer.getItemCount();
 * }
 * public void submitList(PagedList&lt;User> pagedList) {
 * mDiffer.submitList(pagedList);
 * }
 * @Override
 * public void onBindViewHolder(UserViewHolder holder, int position) {
 * User user = mDiffer.getItem(position);
 * if (user != null) {
 * holder.bindTo(user);
 * } else {
 * // Null defines a placeholder item - AsyncPagedListDiffer will automatically
 * // invalidate this row when the actual object is loaded from the database
 * holder.clear();
 * }
 * }
 * public static final DiffUtil.ItemCallback&lt;User> DIFF_CALLBACK =
 * new DiffUtil.ItemCallback&lt;User>() {
 * @Override
 * public boolean areItemsTheSame(
 * @NonNull User oldUser, @NonNull User newUser) {
 * // User properties may have changed if reloaded from the DB, but ID is fixed
 * return oldUser.getId() == newUser.getId();
 * }
 * @Override
 * public boolean areContentsTheSame(
 * @NonNull User oldUser, @NonNull User newUser) {
 * // NOTE: if you use equals, your object must properly override Object#equals()
 * // Incorrectly returning false here will result in too many animations.
 * return oldUser.equals(newUser);
 * }
 * }
 * }</pre>
 *
 * @param <T> Type of the PagedLists this differ will receive.
</T> */
class AsyncPagedListDiffer2<T>(adapter: RecyclerView.Adapter<*>,
                               diffCallback: DiffUtil.ItemCallback<T>) {
    // updateCallback notifications must only be notified *after* new data and item count are stored
    // this ensures Adapter#notifyItemRangeInserted etc are accessing the new data

    /**
     * Convenience for `AsyncPagedListDiffer(new AdapterListUpdateCallback(adapter),
     * new AsyncDifferConfig.Builder<T>(diffCallback).build();`
     *
     * @param adapter      Adapter that will receive update signals.
     * @param diffCallback The [DiffUtil.ItemCallback] instance to
     * compare items in the list.
     */
    private /* synthetic access */ val mUpdateCallback: ListUpdateCallback = AdapterListUpdateCallback(adapter)
    private /* synthetic access */ val mConfig: AsyncDifferConfig<T> = AsyncDifferConfig.Builder(diffCallback).build()

    internal var mMainThreadExecutor =  ExecutorImpl()

    private val mListeners = CopyOnWriteArrayList<PagedListListener<T>>()

    private var mIsContiguous: Boolean = false

    private var mPagedList: PagedListWrapper<T>? = null
    private var mSnapshot: PagedListWrapper<T>? = null

    // Max generation of currently scheduled runnable
    internal /* synthetic access */ var mMaxScheduledGeneration: Int = 0

    private val mPagedListCallback = object : PagedList.Callback() {
        override fun onInserted(position: Int, count: Int) {
            mUpdateCallback.onInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            mUpdateCallback.onRemoved(position, count)
        }

        override fun onChanged(position: Int, count: Int) {
            // NOTE: pass a null payload to convey null -> item
            mUpdateCallback.onChanged(position, count, null)
        }
    }

    /**
     * Get the number of items currently presented by this Differ. This value can be directly
     * returned to [RecyclerView.Adapter.getItemCount].
     *
     * @return Number of items being presented.
     */
    val itemCount: Int
        get() {
            if (mPagedList != null) {
                return mPagedList!!.size
            }

            return if (mSnapshot == null) 0 else mSnapshot!!.size
        }

    /**
     * Returns the PagedList currently being displayed by the differ.
     *
     *
     * This is not necessarily the most recent list passed to [.submitList],
     * because a diff is computed asynchronously between the new list and the current list before
     * updating the currentList value. May be null if no PagedList is being presented.
     *
     * @return The list currently being displayed, may be null.
     */
    val currentList: PagedListWrapper<T>?
        get() = if (mSnapshot != null) {
            mSnapshot
        } else mPagedList

    /**
     * Listener for when the current PagedList is updated.
     *
     * @param <T> Type of items in PagedList
    </T> */
    interface PagedListListener<T> {
        /**
         * Called after the current PagedList has been updated.
         *
         * @param previousList The previous list, may be null.
         * @param currentList  The new current list, may be null.
         */
        fun onCurrentListChanged(
                previousList: PagedListWrapper<T>?, currentList: PagedListWrapper<T>?)
    }

    /**
     * Get the item from the current PagedList at the specified index.
     *
     *
     * Note that this operates on both loaded items and null padding within the PagedList.
     *
     * @param index Index of item to get, must be >= 0, and &lt; [.getItemCount].
     * @return The item, or null, if a null placeholder is at the specified position.
     */
    fun getItem(index: Int): T? {
        if (mPagedList == null) {
            return if (mSnapshot == null) {
                throw IndexOutOfBoundsException(
                        "Item count is zero, getItem() call is invalid")
            } else {
                mSnapshot!![index]
            }
        }

        mPagedList!!.loadAround(index)
        return mPagedList!![index]
    }

    /**
     * Pass a new PagedList to the differ.
     *
     *
     * If a PagedList is already present, a diff will be computed asynchronously on a background
     * thread. When the diff is computed, it will be applied (dispatched to the
     * [ListUpdateCallback]), and the new PagedList will be swapped in as the
     * [current list][.getCurrentList].
     *
     * @param pagedList The new PagedList.
     */
    fun submitList(pagedList: PagedListWrapper<T>?) {
        mMainThreadExecutor.execute {
            submitListInternal(pagedList, null)
        }
    }

    fun submitList(pagedList: PagedListWrapper<T>?, commitCallback: Runnable?) {
        mMainThreadExecutor.execute {
            submitListInternal(pagedList, commitCallback)
        }
    }

    /**
     * Pass a new PagedList to the differ.
     *
     *
     * If a PagedList is already present, a diff will be computed asynchronously on a background
     * thread. When the diff is computed, it will be applied (dispatched to the
     * [ListUpdateCallback]), and the new PagedList will be swapped in as the
     * [current list][.getCurrentList].
     *
     *
     * The commit callback can be used to know when the PagedList is committed, but note that it
     * may not be executed. If PagedList B is submitted immediately after PagedList A, and is
     * committed directly, the callback associated with PagedList A will not be run.
     *
     * @param pagedList      The new PagedList.
     * @param commitCallback Optional runnable that is executed when the PagedList is committed, if
     * it is committed.
     */
    private fun submitListInternal(pagedList: PagedListWrapper<T>?,
                                   commitCallback: Runnable?) {
        if (pagedList != null) {
            if (mPagedList == null && mSnapshot == null) {
                mIsContiguous = pagedList.isContiguous
            } else {
                require(pagedList.isContiguous == mIsContiguous) { "AsyncPagedListDiffer cannot handle both" + " contiguous and non-contiguous lists." }
            }
        }

        // incrementing generation means any currently-running diffs are discarded when they finish
        val runGeneration = ++mMaxScheduledGeneration

        if (pagedList === mPagedList) {
            // nothing to do (Note - still had to inc generation, since may have ongoing work)
            commitCallback?.run()
            return
        }

        val previous = if (mSnapshot != null) mSnapshot else mPagedList

        if (pagedList == null) {
            val removedCount = itemCount
            if (mPagedList != null) {
                mPagedList!!.removeWeakCallback(mPagedListCallback)
                mPagedList = null
            } else if (mSnapshot != null) {
                mSnapshot = null
            }
            // dispatch update callback after updating mPagedList/mSnapshot
            mUpdateCallback.onRemoved(0, removedCount)
            onCurrentListChanged(previous, null, commitCallback)
            return
        }

        if (mPagedList == null && mSnapshot == null) {
            // fast simple first insert
            mPagedList = pagedList
            pagedList.addWeakCallback(null, mPagedListCallback)

            // dispatch update callback after updating mPagedList/mSnapshot
            mUpdateCallback.onInserted(0, pagedList.size)

            onCurrentListChanged(null, pagedList, commitCallback)
            return
        }

        if (mPagedList != null) {
            // first update scheduled on this list, so capture mPages as a snapshot, removing
            // callbacks so we don't have resolve updates against a moving target
            mPagedList!!.removeWeakCallback(mPagedListCallback)
            mSnapshot = mPagedList!!.snapshot() as PagedListWrapper<T>
            mPagedList = null
        }

        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to diff" }

        val oldSnapshot = mSnapshot
        val newSnapshot = pagedList.snapshot() as PagedListWrapper<T>
        mConfig.backgroundThreadExecutor.execute {
            val result: DiffUtil.DiffResult
            result = PagedStorageDiffHelper2.computeDiff(
                    oldSnapshot!!.wrappedStorage,
                    newSnapshot.wrappedStorage,
                    mConfig.diffCallback)

            mMainThreadExecutor.execute {
                if (mMaxScheduledGeneration == runGeneration) {
                    latchPagedList(pagedList, newSnapshot, result,
                            oldSnapshot.mLastLoad2, commitCallback)
                }
            }
        }
    }

    internal /* synthetic access */ fun latchPagedList(
            newList: PagedListWrapper<T>,
            diffSnapshot: PagedListWrapper<T>,
            diffResult: DiffUtil.DiffResult,
            lastAccessIndex: Int,
            commitCallback: Runnable?) {
        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to apply diff" }

        val previousSnapshot = mSnapshot
        mPagedList = newList
        mSnapshot = null

        // dispatch update callback after updating mPagedList/mSnapshot
        PagedStorageDiffHelper2.dispatchDiff(mUpdateCallback,
                previousSnapshot!!.wrappedStorage, newList.wrappedStorage,
                diffResult)

        newList.addWeakCallback(diffSnapshot, mPagedListCallback)
        val pagedList = mPagedList
        if (!pagedList.isNullOrEmpty()) {
            // Transform the last loadAround() index from the old list to the new list by passing it
            // through the DiffResult. This ensures the lastKey of a positional PagedList is carried
            // to new list even if no in-viewport item changes (AsyncPagedListDiffer#get not called)
            // Note: we don't take into account loads between new list snapshot and new list, but
            // this is only a problem in rare cases when placeholders are disabled, and a load
            // starts (for some reason) and finishes before diff completes.
            val newPosition = PagedStorageDiffHelper2.transformAnchorIndex(
                    diffResult, previousSnapshot.wrappedStorage, diffSnapshot.wrappedStorage,
                    lastAccessIndex)

            // Trigger load in new list at this position, clamped to list bounds.
            // This is a load, not just an update of last load position, since the new list may be
            // incomplete. If new list is subset of old list, but doesn't fill the viewport, this
            // will likely trigger a load of new data.
            pagedList.loadAround(Math.max(0, Math.min(pagedList.size - 1, newPosition)))
        }

        onCurrentListChanged(previousSnapshot, pagedList, commitCallback)
    }

    private fun onCurrentListChanged(
            previousList: PagedListWrapper<T>?,
            currentList: PagedListWrapper<T>?,
            commitCallback: Runnable?) {
        for (listener in mListeners) {
            listener.onCurrentListChanged(previousList, currentList)
        }
        commitCallback?.run()
    }

    /**
     * Add a PagedListListener to receive updates when the current PagedList changes.
     *
     * @param listener Listener to receive updates.
     * @see .getCurrentList
     * @see .removePagedListListener
     */
    fun addPagedListListener(listener: PagedListListener<T>) {
        mListeners.add(listener)
    }

    /**
     * Remove a previously registered PagedListListener.
     *
     * @param listener Previously registered listener.
     * @see .getCurrentList
     * @see .addPagedListListener
     */
    fun removePagedListListener(listener: PagedListListener<T>) {
        mListeners.remove(listener)
    }
}
