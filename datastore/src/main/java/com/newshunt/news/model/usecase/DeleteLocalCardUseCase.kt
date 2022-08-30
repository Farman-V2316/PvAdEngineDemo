package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.news.model.daos.CreatePostDao
import io.reactivex.Observable
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named

class DeleteLocalCardUseCase @Inject constructor(private val createPostDao:
                                                 CreatePostDao)
    : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val postId = p1.getString(Constants.BUNDLE_LOCAL_CARD_ID)
        return Observable.fromCallable {
            postId ?: return@fromCallable false
            try {
                createPostDao.delete(postId.toInt())
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
            true
        }
    }

}