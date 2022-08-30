/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.UserFollowEntity
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.repo.FollowRepo
import io.reactivex.Observable
import java.io.Serializable
import javax.inject.Inject

/**
 * Handles follow, unfollow, block from menu.
 * Uses insert/replace on the table.
 *
 * @author satosh.dhanyamraju
 */
class FollowFromMenuUsecase @Inject constructor(private val followEntityDao: FollowEntityDao) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val pojo = (p1.getSerializable(B_ARG) as? Pojo)
                ?: return Observable.error(Throwable("FollowFromMenuUsecase: missing arg"))

        return FollowRepo(followEntityDao).insertActionableEntity(ActionableEntity(
            pojo.entityId, pojo.entityType, pojo.entitySubType), pojo.action).
            map {
                true
            }
    }

    companion object {
        private val B_ARG = "b_arg"
        fun createBundle(pojo: Pojo, bundle: Bundle = Bundle()) =
                bundle.also {
                    it.putSerializable(B_ARG, pojo)
                }
    }

    data class Pojo(
            val entityId: String,
            val entityType: String,
            val entitySubType: String? = null,
            val action: FollowActionType) : Serializable
}