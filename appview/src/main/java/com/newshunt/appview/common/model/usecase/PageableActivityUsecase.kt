package com.newshunt.appview.common.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.model.repo.PageableTopicRepo
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable

class MediatorPageableTopicUsecase : MediatorUsecase<String, List<PageableTopicsEntity>> {

  private val _data = MediatorLiveData<Result0<List<PageableTopicsEntity>>>()

  override fun execute(t: String): Boolean {
    _data.addSource(PageableTopicRepo(t).getPageableTopics()) {
      _data.value = Result0.success(it)
    }
    return true
  }

  override fun data(): LiveData<Result0<List<PageableTopicsEntity>>> {
    return _data
  }
}

class GetPageableTopicUsecase : Usecase<String, List<PageEntity>> {
  override fun invoke(p1: String): Observable<List<PageEntity>> {
    return PageableTopicRepo(p1).getPageableTopicsFromServer().map {
      it.rows
    }
  }
}

class MediatorAddPageUsecase : MediatorUsecase<Any, List<AddPageEntity>> {

  private val _data = MediatorLiveData<Result0<List<AddPageEntity>>>()

  override fun execute(t: Any): Boolean {
    _data.addSource(SocialDB.instance().addPageDao().getAddPageList()) {
      _data.value = Result0.success(it)
    }
    return true
  }

  override fun data(): LiveData<Result0<List<AddPageEntity>>> {
    return _data
  }
}

class MediatorFirstAddPageUsecase : MediatorUsecase<Any, AddPageEntity> {

  private val _data = MediatorLiveData<Result0<AddPageEntity>>()

  override fun execute(t: Any): Boolean {
    _data.addSource(SocialDB.instance().addPageDao().getFirstAddedPage()) {
      _data.value = Result0.success(it)
    }
    return true
  }

  override fun data(): LiveData<Result0<AddPageEntity>> {
    return _data
  }
}

class DeleteAllAddPageUsecase : Usecase<Any, Any> {
  override fun invoke(p1: Any): Observable<Any> {
    return Observable.fromCallable {
       SocialDB.instance().addPageDao().clearAddPages()
    }
  }
}

class AddPageableTopicUsecase : BundleUsecase<Any> {

  override fun invoke(bundle: Bundle): Observable<Any> {
    val pageableTopicsEntity = bundle.getSerializable(BUNDLE_PAGEABLE_TOPIC) as PageableTopicsEntity
    val isAdded = bundle.getBoolean(BUNDLE_IS_ADDED, false)
    val section = bundle.getString(BUNDLE_SECTION, PageSection.NEWS.section)
    return PageableTopicRepo(section).addOrRemovePages(pageableTopicsEntity = pageableTopicsEntity,
        isAdded = isAdded, section = section)
  }

  companion object {
    @JvmStatic val BUNDLE_PAGEABLE_TOPIC = "BUNDLE_PAGEABLE_TOPIC"
    @JvmStatic val BUNDLE_IS_ADDED = "BUNDLE_IS_ADDED"
    @JvmStatic val BUNDLE_SECTION = "BUNDLE_SECTION"
  }
}