package com.newshunt.appview.common.postcreation.viewmodel

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.appview.common.postcreation.model.usecase.PostAutoLocationUseCase
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject


private const val MSG_DEBOUNCE = 1001
private const val DEBOUNCE_DELAY = 1000L

private const val TAG = "PostAutoCompleteLocationVM"

class PostAutoCompleteLocationVM @Inject constructor(locationUseCase: PostAutoLocationUseCase) :
    ViewModel() {

    private val searchResultsMap = mutableMapOf<String, List<PostCurrentPlace>>()

    private val locationMediatorUseCase = locationUseCase.toMediator2()
    val locationLiveData: MutableLiveData<Result0<List<PostCurrentPlace>>> by lazy {
        locationMediatorUseCase.data() as MutableLiveData
    }

    private val handler = Handler(Looper.getMainLooper(), Handler.Callback {
        if (it.what == MSG_DEBOUNCE) {
            fetchAutoCompleteLocation(it.obj as? String)
        }
        false
    })

    private fun fetchAutoCompleteLocation(query: String?) {
        query ?: return
        if (searchResultsMap.containsKey(query)) {
            Logger.d(TAG, "Found in cache")
            locationLiveData.value = Result0.success(searchResultsMap[query]!!)
        } else {
            getSearchResult(query)
        }
    }

    fun getAutoCompleteLocation(query: String) {
        val msg = Message.obtain(handler, MSG_DEBOUNCE, query)
        handler.removeMessages(MSG_DEBOUNCE)
        handler.sendMessageDelayed(msg, DEBOUNCE_DELAY)
    }

    private fun getSearchResult(query: String) {
        locationMediatorUseCase.execute(bundleOf(Pair(PostConstants.POST_QUERY, query)))
    }

    override fun onCleared() {
        searchResultsMap.clear()
        locationMediatorUseCase.dispose()
        super.onCleared()
    }

}