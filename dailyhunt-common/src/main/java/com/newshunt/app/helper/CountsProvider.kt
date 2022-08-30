package com.newshunt.app.helper

import androidx.lifecycle.MutableLiveData
import com.newshunt.dataentity.news.model.entity.Counts

object CountsProvider {

    @JvmStatic
    val countsObserver = MutableLiveData<CountsPostEvent>()

    @JvmStatic
    fun updateCounts(counts: Counts?, itemId: String?) {
        countsObserver.postValue(CountsPostEvent(counts, itemId))
    }

}

data class CountsPostEvent(val counts: Counts?, val itemId: String?)