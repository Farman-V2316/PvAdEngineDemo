package com.newshunt.appview.common.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.model.usecase.AddPageableTopicUsecase
import com.newshunt.appview.common.model.usecase.GetPageableTopicUsecase
import com.newshunt.appview.common.model.usecase.MediatorPageableTopicUsecase
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2

class PageableTopicViewModel(val section: String) : ViewModel() {

    private val observableTrendingTopicUsecase = MediatorPageableTopicUsecase()
    private val getPageableTopicUsecase = GetPageableTopicUsecase().toMediator2()
    val pageableTopicLiveData: LiveData<Result0<List<PageableTopicsEntity>>>
    val topicResponseLiveData: LiveData<Result0<List<PageEntity>>>

    init {
        observableTrendingTopicUsecase.execute(section)
        pageableTopicLiveData = observableTrendingTopicUsecase.data()
        topicResponseLiveData = getPageableTopicUsecase.data()
    }

    fun viewStarted() {
        getPageableTopicUsecase.execute(section)
    }

    fun onTabStateChanged(view: View, isAdded: Boolean, pageableTopicsEntity: PageableTopicsEntity) {
        AddPageableTopicUsecase().toMediator2().execute(
                bundleOf(AddPageableTopicUsecase.BUNDLE_PAGEABLE_TOPIC to pageableTopicsEntity,
                        AddPageableTopicUsecase.BUNDLE_IS_ADDED to isAdded,
                        AddPageableTopicUsecase.BUNDLE_SECTION to section))
        if (isAdded && !pageableTopicsEntity.isFollowed) {
            ToggleFollowUseCase(FollowRepo(SocialDB.instance().followEntityDao())).toMediator2().execute(
                    bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to pageableTopicsEntity.pageEntity.toFollowActionableEntity().actionableEntity)
            )
        } else if (!isAdded && pageableTopicsEntity.isFollowed) {
            ToggleFollowUseCase(FollowRepo(SocialDB.instance().followEntityDao())).toMediator2().execute(
                    bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to pageableTopicsEntity.pageEntity.toFollowActionableEntity().actionableEntity)
            )
        }
    }

    fun onLocationFollowChanged(view: View, isAdded: Boolean, location: Location) {

        ToggleFollowUseCase(FollowRepo(SocialDB.instance().followEntityDao())).toMediator2().execute(
                bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to location.toActionableEntity()))


    }
}


class PageableTopicViewModelFactory(val section: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageableTopicViewModel(section) as T
    }

}