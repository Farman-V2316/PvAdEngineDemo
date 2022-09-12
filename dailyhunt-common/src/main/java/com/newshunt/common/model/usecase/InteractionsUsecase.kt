package com.newshunt.common.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.model.repo.InteractionsRepo
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FollowBlockUpdateUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class SyncLikeUsecase @Inject constructor() : Usecase<Any, Any> {
  override fun invoke(p1: Any): Observable<Any> {
    return InteractionsRepo().syncLikes()
  }
}

class ToggleLikeUsecase @Inject constructor(private val dao: InteractionDao,
                                            private val cpDao: CreatePostDao,
                                            private val syncLikeUsecase: SyncLikeUsecase):
        BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val id = p1.getString(B_ID) ?: throw Throwable("need $B_ID in bundle")
        val type = p1.getString(B_TYPE) ?: throw Throwable("need $B_TYPE in bundle")
        val lType = p1.getString(B_LTYPE) ?: throw Throwable("need $B_LTYPE in bundle")
        val parentId: String? = p1.getString(Constants.BUNDLE_PARENT_ID)

        return Observable.fromCallable {
            dao.toggleLike(id, type, lType, parentId)
            cpDao.toggleLike(id, lType)
        }.flatMap {
          syncLikeUsecase.invoke(Any())
        }.map {
            true
        }
    }

    companion object {
        private val B_ID = "tgl_e_id"
        private val B_TYPE = "tgl_e_type"
        private val B_LTYPE = "tgl_l_type"

        fun args(entityId: String, entityType: String, likeType: String, bundle: Bundle = Bundle()) =
                bundle.also {
                    it.putString(B_ID, entityId)
                    it.putString(B_TYPE, entityType)
                    it.putString(B_LTYPE, likeType)
                }
    }
}

