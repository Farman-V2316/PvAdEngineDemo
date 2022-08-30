package androidx.paging

internal class PagedStorageWrapper<T> constructor(private val source: PagedStorage<T>) : AbstractList<T?>() {
    internal val extraItems: HashMap<Int, T> = HashMap()

    constructor(extraItems: HashMap<Int, T>, source: PagedStorage<T>) : this(source) {
        this.extraItems.putAll(extraItems)
    }

    override val size: Int
        get() {
            return source.size + extraItems.size
        }

    override fun get(index: Int): T? {
        val extra = extraItems[index]
        if (extra != null) {
            return extra
        }
        return source.getOrNull(index - lowerCount(index))
    }

    private fun lowerCount(index: Int): Int {
        return extraItems.count { it.key <= index }
    }

    fun addExtraItem(index: Int, data: T) {
        extraItems.put(index, data)
    }

    fun removeExtraItem(data: T) {
        val entry = extraItems.entries.find { it.value == data }
        entry?.let {
            extraItems.remove(it.key)
        }
    }

    fun removeExtraItem(index: Int) {
        extraItems.remove(index)
    }

    fun addExtraItem(data: T) {
        val index = size
        addExtraItem(index, data)
    }

    fun getSnapshot(): PagedStorageWrapper<T> {
        val temp = HashMap<Int, T>()
        temp.putAll(extraItems)
        return PagedStorageWrapper<T>(temp, source.snapshot())
    }

    fun sourceIndex(index: Int): Int {
        return index - lowerCount(index)
    }

    fun isExtraIndex(index: Int): Boolean {
        return extraItems.containsKey(index)
    }

    fun clone(): PagedStorageWrapper<T> {
        return PagedStorageWrapper(extraItems, source)
    }

    fun getSource(): PagedStorage<T> {
        return source
    }

    fun getStorageCount(): Int {
        return source.storageCount + extraItems.size
    }

    fun removeAllExtraItem() {
        extraItems.clear()
    }
}