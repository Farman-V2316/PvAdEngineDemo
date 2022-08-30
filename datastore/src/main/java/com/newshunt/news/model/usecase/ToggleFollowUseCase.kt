/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.news.model.repo.FollowRepo
import io.reactivex.Observable
import javax.inject.Inject

class ToggleFollowUseCase @Inject constructor(val followRepo: FollowRepo) : BundleUsecase<Boolean> {

    override fun invoke(p1: Bundle): Observable<Boolean> {
        val entities : List<ActionableEntity>?= (p1.getSerializable(B_FOLLOW_ENTITIES) as? List<ActionableEntity>)
        val action = p1.getString(B_ACTION)
        entities?.let {
            return  followRepo.toggleFollows(entities, action).map { true }
        }


        val entity = (p1.getSerializable(B_FOLLOW_ENTITY) as? ActionableEntity)
                ?: return Observable.just(false)

        return followRepo.toggleFollow(entity, action).map { true }
    }

    companion object {
        const val B_FOLLOW_ENTITY = "follow_entity"
        const val B_FOLLOW_ENTITIES = "follow_entities"
        const val B_ACTION = "action"
    }
}