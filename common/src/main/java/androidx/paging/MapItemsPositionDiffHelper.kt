package androidx.paging

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format

class MapItemsPositionDiffHelper<T>() {
    fun getNewMapItems(oldList: PagedList<T>?, newList: PagedList<T>?, mapItems: HashMap<Int, T>): HashMap<Int, T> {
        val result = HashMap<Int, T>()
        if (newList == null || oldList == null || mapItems.isEmpty()) {
            return result
        }

        result.putAll(mapItems)
        val storage = newList.mStorage

        for (i in storage.leadingNullCount until (storage.loadedCount + storage.leadingNullCount)) {
            val item = newList[i]
            if (item is CommonAsset && item.i_format() == Format.AD) {
                for (entry in mapItems.entries) {
                    if ((entry.value as? CommonAsset)?.i_id() == item.i_id()) {
                        Logger.i(LOG_TAG, "removing ${item.i_id()}")
                        result.remove(entry.key)
                    }
                }
            }
        }
        return result
    }
}

private const val LOG_TAG = "MapExtraItems"