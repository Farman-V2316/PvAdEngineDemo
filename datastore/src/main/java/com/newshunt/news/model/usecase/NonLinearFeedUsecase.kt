package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.NonLinearPostDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * @author shrikant.agrawal
 */
class NonLinearFeedUsecase @Inject constructor(private val nonLinearPostDao: NonLinearPostDao) : MediatorUsecase<Any,NLFCItem?> {

    private val _data = MediatorLiveData<Result0<NLFCItem?>>()

    override fun execute(t: Any): Boolean {
        _data.addSource(nonLinearPostDao.getNLFCList()) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<NLFCItem?>> {
        return _data
    }
}

class NonLinearListFeedUsecase @Inject constructor(private val nonLinearPostDao: NonLinearPostDao) : MediatorUsecase<Any, List<NLFCItem>> {

    private val _data = MediatorLiveData<Result0<List<NLFCItem>>>()

    override fun execute(t: Any): Boolean {
        _data.addSource(nonLinearPostDao.getNonLinearForList()) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<List<NLFCItem>>> {
        return _data
    }
}

class NonLinearConsumedUsecase @Inject constructor(private val nonLinearPostDao: NonLinearPostDao) : Usecase<String, Any> {
    override fun invoke(p1: String): Observable<Any> {
        return Observable.fromCallable {
            nonLinearPostDao.markPostAsConsumed("nlfc_$p1")
        }
    }
}

class CleanUpNonlinearUsecase @Inject constructor(private val nonLinearPostDao: NonLinearPostDao) : Usecase<Any, Any> {
    override fun invoke(p1: Any): Observable<Any> {
        return Observable.fromCallable {
            nonLinearPostDao.cleanUpNlfc()
        }
    }

}

class InsertNonLinearFeedUsecase @Inject constructor(@Named("entityId") private val entityId: String,
                                                     @Named("location") private val location: String,
                                                     @Named("section") private val section: String,
                                                     val fetchDao: FetchDao)  : BundleUsecase<Any> {

    override fun invoke(p1: Bundle): Observable<Any> {
      return Observable.fromCallable {
        val prevPostId = p1.getString(BUNDLE_PREV_POST_ID) ?: return@fromCallable
        val nlfcItem = p1.getSerializable(BUNDLE_NLFC_ITEM) as? NLFCItem ?: return@fromCallable
          val forcePosition = p1.getInt(BUNDLE_FORCE_POSITION, -100)
          val url = p1.getString(BUNDLE_URL, Constants.EMPTY_STRING)
          if (forcePosition >= -1) {
              fetchDao.insertNonLinearPostAt(nlfcItem, prevPostId, forcePosition, entityId,
                      location, section, url)
          } else {
              fetchDao.insertNonLinearPost(nlfcItem, prevPostId, entityId, location, section)
          }

      }
    }

    companion object {
        const val BUNDLE_NLFC_ITEM = "BUNDLE_NLFC_ITEM"
        const val BUNDLE_PREV_POST_ID = "BUNDLE_PREV_POST_ID"
        const val BUNDLE_FORCE_POSITION = "BUNDLE_FORCE_POSITION"
        const val BUNDLE_URL = "BUNDLE_URL"
    }
}