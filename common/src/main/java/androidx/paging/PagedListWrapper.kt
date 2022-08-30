package androidx.paging

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset

open class PagedListWrapper<T> :
        PagedList<T> {
    internal val wrappedStorage: PagedStorageWrapper<T>
    internal var source: PagedList<T>
        private set

    internal val mLastLoad2: Int
        get() {
            return source.mLastLoad + wrappedStorage.extraItems.size
        }

    internal val mLastItem2: T?
        get() {
            return source.mLastItem
        }

    internal constructor(source: PagedList<T>, storage: PagedStorageWrapper<T>) : super(source.mStorage,
            source
            .mMainThreadExecutor, source.mBackgroundThreadExecutor, source.mBoundaryCallback,
            source.config) {
        wrappedStorage = storage
        this.source = source
    }

    constructor(source: PagedList<T>) : super(source.mStorage, source
            .mMainThreadExecutor, source.mBackgroundThreadExecutor, source.mBoundaryCallback,
            source.config) {
        wrappedStorage = PagedStorageWrapper<T>(source.mStorage)
        this.source = source
    }

    override fun loadAroundInternal(index: Int) {
        source.loadAround(index)
    }

    override fun getDataSource(): DataSource<*, T> {
        return source.dataSource
    }

    override fun dispatchUpdatesSinceSnapshot(snapshot: PagedList<T>, callback: Callback) {
        source.dispatchUpdatesSinceSnapshot(snapshot, callback)
    }

    internal override fun isContiguous(): Boolean {
        return source.isContiguous
    }

    override fun getLastKey(): Any? {
        return source.lastKey
    }

    override fun isDetached(): Boolean {
        return source.isDetached
    }

    override fun loadAround(index: Int) {
        val superIndex = wrappedStorage.sourceIndex(index)
        if (superIndex < 0) {
            source.loadAround(0)
        } else if (superIndex >= source.size) {
            source.loadAround(source.size - 1)
        } else {
            source.loadAround(superIndex)
        }
    }

    override fun notifyChanged(position: Int, count: Int) {
        source.notifyChanged(position, count)
    }

    override fun notifyInserted(position: Int, count: Int) {
        source.notifyInserted(position, count)
    }

    override fun detach() {
        source.detach()
    }

    override fun getLoadedCount(): Int {
        val result = source.loadedCount + wrappedStorage.extraItems.size
        return result
    }

    override fun isImmutable(): Boolean {
        return source.isImmutable
    }

    override fun getConfig(): Config {
        return source.config
    }

    override fun notifyRemoved(position: Int, count: Int) {
        source.notifyRemoved(position, count)
    }

    override fun deferBoundaryCallbacks(deferEmpty: Boolean, deferBegin: Boolean, deferEnd: Boolean) {
        source.deferBoundaryCallbacks(deferEmpty, deferBegin, deferEnd)
    }

    override fun removeWeakCallback(callback: Callback) {
        source.removeWeakCallback(callback)
    }

    override fun dispatchBoundaryCallbacks(begin: Boolean, end: Boolean) {
        source.dispatchBoundaryCallbacks(begin, end)
    }

    override fun get(index: Int): T? {
        if (!wrappedStorage.isExtraIndex(index)) {
            return source.getOrNull(wrappedStorage.sourceIndex(index))
        }
        return wrappedStorage.get(index)
    }

    override fun snapshot(): MutableList<T> {
        return PagedListWrapper<T>(source.snapshot() as PagedList<T>, wrappedStorage.getSnapshot())
    }

    fun clone(): PagedListWrapper<T> {
        return PagedListWrapper(source, wrappedStorage.clone())
    }

    override fun addWeakCallback(previousSnapshot: MutableList<T>?, callback: Callback) {
        source.addWeakCallback(previousSnapshot, callback)
    }

    override fun getPositionOffset(): Int {
        return source.positionOffset
    }

    override fun tryDispatchBoundaryCallbacks(post: Boolean) {
        source.tryDispatchBoundaryCallbacks(post)
    }

    override val size: Int
        get() = wrappedStorage.size


    fun addExtraItem(index: Int, data: T) {
        Logger.i("MapExtraItems", "Inserting extra items ${(data as? CommonAsset)?.i_id()}")
        return wrappedStorage.addExtraItem(index, data)
    }

    fun removeExtraItem(data: T) {
        return wrappedStorage.removeExtraItem(data)
    }

    fun removeExtraItem(index: Int) {
        return wrappedStorage.removeExtraItem(index)
    }

    fun addExtraItem(data: T) {
        return wrappedStorage.addExtraItem(data)
    }

    fun updateSource(source: PagedList<T>, mapItemsPositionDiffHelper: MapItemsPositionDiffHelper<T>): PagedListWrapper<T> {
        val newExtraItems = mapItemsPositionDiffHelper.getNewMapItems(this.source.snapshot() as
                PagedList<T>?, source.snapshot() as PagedList<T>?, wrappedStorage.extraItems)
        return PagedListWrapper(source, storage = PagedStorageWrapper(newExtraItems, source.mStorage))
    }

    fun removeAllExtraItem() {
        return wrappedStorage.removeAllExtraItem()
    }
}