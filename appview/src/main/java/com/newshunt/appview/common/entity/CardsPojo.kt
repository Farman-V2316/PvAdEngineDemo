/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.entity

import androidx.paging.EitherList
import com.newshunt.common.view.isNotFoundInCacheError
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.model.entity.BaseError

/**
 * Contains both (not either)  data and error fields.
 * @author satosh.dhanyamraju2
 */
data class CardsPojo(
        val data: List<Any>? = null, // may/may-not contain loader
        val tsData: Long? = null,
        val error: Throwable? = null, // should be BaseError?
        val tsError: Long? = null
) {
    override fun toString(): String {
        return "CardsPojo(data=ls[sz=${data?.size}], tsData=$tsData, error=$error, tsError=$tsError)"
    }
}

data class CardPojo(
        val data: CommonAsset? = null, // not contain loader
        val tsData: Long? = null,
        val error: Throwable? = null, // should be BaseError?
        val tsError: Long? = null
) {
    override fun toString(): String {
        return "CardsPojo(data=$data, tsData=$tsData, error=$error, tsError=$tsError)"
    }
}



data class CardsPojoPagedList(
        val data: EitherList<Any>? = null, // may/may-not contain loader
        val tsData: Long? = null,
        val error: Throwable? = null, // should be BaseError?
        val tsError: Long? = null
) {
    private val isEmptyData
        get() = data?.getList()?.isNullOrEmpty() == true

    private val notSeenError
        get() = error == null && tsError == null

    override fun toString(): String {
        return "CardsPojo(data=ls[sz=${data?.getList()?.size}], tsData=$tsData, error=$error, " + "tsError=$tsError)"
    }

    fun isWaitingForData(): Boolean {
        val isErrorNewer = (tsError ?: 0) > (tsData ?: 0)
        val isCacheError = (error as? BaseError)?.isNotFoundInCacheError() == true
        return when {
            !isEmptyData -> false
            !isErrorNewer -> true
            else -> isCacheError
        }
    }

    fun dataIsEmptyAndNotYetSeenError() = isEmptyData && notSeenError
}