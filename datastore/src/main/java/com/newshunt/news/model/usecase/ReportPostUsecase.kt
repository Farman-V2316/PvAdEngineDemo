/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.news.model.apis.PostReportService
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author amit.chaudhary
 */
class ReportPostUsecase @Inject constructor(private val postReportApi: PostReportService) : BundleUsecase<Boolean> {

    override fun invoke(args: Bundle): Observable<Boolean> {
        val post = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset ?: kotlin.run {
            Logger.e(LOG_TAG, "post can not be null")
            return Observable.error(Throwable("Post can not be null"))
        }

        val format = post.i_format()?.name ?: kotlin.run {
            Logger.e(LOG_TAG, "Format can not be null")
            return Observable.error(Throwable("Format can not be null"))
        }

        val header = args.getString(Constants.BUNDLE_DELETE_HEADER, Constants.EMPTY_STRING)

        return postReportApi.reportComments(post.i_id(), format, header).map {
            true
        }
    }
}

private const val LOG_TAG = "DeletePostUsecase"