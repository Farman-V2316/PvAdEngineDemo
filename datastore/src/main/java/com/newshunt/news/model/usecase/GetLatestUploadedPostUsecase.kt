/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.news.model.daos.CreatePostDao
import javax.inject.Inject

/**
 *
 * For showing after succesful post upload.
 *
 * @author satosh.dhanyamraju
 */
class GetLatestUploadedPostUsecase @Inject constructor(private val createPostDao: CreatePostDao) :
        MediatorUsecase<Bundle, CreatePostEntity?> {
    val mediator = MediatorLiveData<Result0<CreatePostEntity?>>()
    var lastSource: LiveData<CreatePostEntity?>? = null

    override fun execute(t: Bundle): Boolean {
        val ts = t.getLong(Constants.BUNDLE_CREATION_TIME, System.currentTimeMillis())
        with(createPostDao.latestUploadedPostNotInsertedInForyouNewerThan(ts)) {
            lastSource?.let { mediator.removeSource(it) }
            mediator.addSource(this.distinctUntilChanged()) { mediator.value = Result0.success(it) }
            lastSource = this
        }
        return true
    }

    override fun data(): LiveData<Result0<CreatePostEntity?>> = mediator
}