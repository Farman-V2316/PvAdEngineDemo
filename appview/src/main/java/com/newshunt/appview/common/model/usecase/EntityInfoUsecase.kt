package com.newshunt.appview.common.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.model.repo.EntityInfoRepo
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.pages.EntityInfoList
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable

class ObservableEntityInfoUsecase : MediatorUsecase<Bundle, EntityInfoList?> {

    private val _data = MediatorLiveData<Result0<EntityInfoList?>>()

    override fun execute(t: Bundle): Boolean {
        val id = t.getString(EntityConstants.BUNDLE_ID)?:Constants.EMPTY_STRING
        val section = t.getString(EntityConstants.BUNDLE_SECTION)?:PageSection.NEWS.section
        val legacyKey = t.getString(EntityConstants.BUNDLE_LEGACY_KEY)
        _data.addSource(SocialDB.instance().entityInfoDao().getEntityList(id, section, legacyKey)) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<EntityInfoList?>> {
        return _data
    }
}

class GetEntityInfoUsecase : BundleUsecase<Any> {

  override fun invoke(p1: Bundle): Observable<Any> {
    val id: String = p1[EntityConstants.BUNDLE_ID] as String? ?: Constants.EMPTY_STRING
    val entityType: String = p1[EntityConstants.BUNDLE_ENTITY_TYPE] as String? ?: Constants.EMPTY_STRING
    val section = p1.getString(EntityConstants.BUNDLE_SECTION)?:PageSection.NEWS.section
    val langCode = p1.getString(EntityConstants.BUNDLE_LANG_CODE) ?: Constants.ENGLISH_LANGUAGE_CODE
    return EntityInfoRepo(section).getInfoFromServer(id, entityType, langCode)
  }
}

class ClearEntityInfoUsecase : Usecase<String,Any> {

    override fun invoke(p1: String): Observable<Any> {
        return Observable.fromCallable {
            SocialDB.instance().entityInfoDao().clearEntity(p1)
        }
    }
}


object EntityConstants {
    const val BUNDLE_ID = "_id"
    const val BUNDLE_ENTITY_TYPE = "entityType"
    const val BUNDLE_SECTION = "bundle_section"
    const val BUNDLE_LEGACY_KEY = "legacyKey"
    const val BUNDLE_LANG_CODE = "langCode"
}