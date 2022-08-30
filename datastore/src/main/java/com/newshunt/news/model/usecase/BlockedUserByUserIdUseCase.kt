package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.news.model.repo.FollowRepo
import javax.inject.Inject

/**
 * created by atul.yadav
 *
 * date: 2 march 2022
 */

class BlockedUserByUserIdUseCase @Inject constructor(private val followRepo: FollowRepo) : MediatorUsecase<String, String?>{

    private val _data = MediatorLiveData<Result0<String?>>()
    override fun execute(t: String): Boolean {
        _data.addSource(followRepo.fetchBlockId(t)) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<String?>> {
       return _data
    }
}