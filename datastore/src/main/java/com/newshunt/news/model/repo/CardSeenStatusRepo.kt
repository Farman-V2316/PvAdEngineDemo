/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.repo

import androidx.annotation.VisibleForTesting
import androidx.viewpager.widget.ViewPager
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.news.model.daos.CSSDao
import com.newshunt.news.model.daos.CSSDao.Companion.CSS_SEEN
import com.newshunt.news.model.daos.CSSEntity
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import okhttp3.Response

/**
 * Track which cards are seen, discarded.
 *
 * @author satosh.dhanyamraju
 */
private typealias RequestId = String

class CardSeenStatusRepo {
    val dao = SocialDB.instance().cssDao()

    fun served(items: List<Any?>?, fetchInfoId: Long) {
        items ?: return
        val ids = filterIds(items)
        ids.map {
            CSSEntity(it, fetchInfoId)
        }.let {
            dao.ins(it)
        }
        Logger.d(LOG_TAG, "served: fetchId=$fetchInfoId,items=#${items.size}, ${_state()}")
    }

    // to be called during SCV
    fun markSeen(cardId: String) {
        val d = Observable.fromCallable {
            val count = dao.markSeen(cardId)
            if(count == 0 ) {
                // row not present in DB. Insert.
                val insId = dao.ins(listOf(CSSEntity(cardId, DEFAULT_FETCH_ID, CSS_SEEN)))
                insId
            } else count
        }
                .subscribeOn(Schedulers.io())
                .subscribeWith(observer(this, "markSeen: id=$cardId, #"))
    }

    // to be called on frag pullToRefresh, onDestroy,
    fun markDiscardedFromFetchId(fetchId: Long): Int {
        val c = dao.markDiscardedFromFetchId(fetchId)
        Logger.d(LOG_TAG, "markDiscardedFromFetchId: fetchId=$fetchId, #$c, ${_state()}")
        return c
    }

    fun tagSeenWith(id: RequestId): List<String> {
        val list = dao.tagSeenWith("_$id")
        Logger.d(LOG_TAG, "tagSeenWith: bid=$id, $list, ${_state()}")
        return list
    }


    fun tagDiscardedWith(id: RequestId): List<String> {
        val list = dao.tagDiscardedWith("_$id")
        Logger.d(LOG_TAG, "tagDiscardedWith: bid=$id, $list, ${_state()}")
        return list
    }

    fun onAPIResponse(response: Response?, request: Request?) {
        if(response == null) {
            onAPIError(request)
        }else {
            val code = response.code()
            val batchId = response.request().header(HEADER_CSS_BATCH_ID)
            if (batchId.isNullOrEmpty()) return
            on200OK(batchId, code == Constants.HTTP_SUCCESS && response.isSuccessful)
        }
    }

    fun onAPIError(request: Request?) {
        if (request == null) {
            Logger.e(LOG_TAG, "onAPIError: request is null")
            return;
        }
        val batchId = request.header(HEADER_CSS_BATCH_ID)
        if (batchId.isNullOrEmpty()) return
        on200OK(batchId, false)
    }

    @VisibleForTesting
    fun on200OK(batchId: String, success: Boolean = true): Int {
        val c = if (success) dao.deleteBatch("_$batchId")
        else dao.rollbackBatch("_$batchId")
        Logger.d(LOG_TAG, "on200OK: bid=$batchId,succ=$success, #$c, ${_state()}")
        return c
    }

    fun markDiscardedForEntites(ids: List<String>) {
        val d =  dao.markDiscardedForEntities(ids)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Logger.d(LOG_TAG,"markDiscardedForEntites: updated $it rows")
                }, { e ->
                    Logger.e(LOG_TAG, "", e)
                })
    }

    private fun _state(): String {
        if(!PR_STATE) return ""
        return "St#(${dao.countAll()},${dao.countByState(CSSDao.CSS_UNKNOWN)},${dao.countByState(CSSDao.CSS_SEEN)},${dao.countByState(CSSDao.CSS_DISCARDED)})"
    }

    companion object {
        const val HEADER_CSS_BATCH_ID = "Cl-CssBatchId"
        private const val LOG_TAG = "CardSeenStatusRepo"
        private const val PR_STATE = false
        private const val DEFAULT_FETCH_ID = 999999999L
        @JvmStatic
        val DEFAULT = CardSeenStatusRepo()

        @JvmStatic
        @JvmOverloads
        fun extractAndUpdateState(pager: ViewPager, newInd: Int,
                                  curInd : Int = pager.getCurrentItem(), repo: CardSeenStatusRepo = DEFAULT) {
            if(newInd == curInd ) return
            val pages = (pager.adapter as? HomeAdap)?.pages()
            if(pages.isNullOrEmpty()) return
            var candidates = listOf<Int>()
            // Assuming offScreenPageLimit=1(default), fill `candidates` with fragments indices whose
            // `onDestroy()` will be called once `newInd` becomes the current index
            if(newInd < curInd) {
                if(newInd < curInd - 1 ) candidates = listOf(curInd -1 , curInd , curInd + 1)
                else if(newInd == curInd -1) candidates = listOf(curInd + 1)
            } else if(newInd > curInd) {
                if(newInd > curInd + 1) candidates = listOf(curInd -1 , curInd, curInd + 1)
                else if(newInd == curInd + 1) candidates = listOf(curInd - 1)
            }
            val ids = candidates.mapNotNull{
                if (pages != null && it in pages.indices) {
                    pages[it].id
                } else null
            }
            if(ids.isEmpty()) {
                Logger.v(LOG_TAG, "extractAndUpdateState: nothing to clear. new=$newInd, old=$curInd")
            }
            else {
                repo.markDiscardedForEntites(ids)
                Logger.d(LOG_TAG, "extractAndUpdateState: at: $newInd, clearing=$ids ," +
                        "getCurItem=$curInd, chidCount=${pager.getChildCount()}")
            }
        }

        private fun filterIds(items: List<Any?>) = items.filterNotNull()
            .filterIsInstance<CommonAsset>()
            .flatMap {
                listOf(it.i_id()) + if (it.i_format() == Format.POST_COLLECTION) {
                    (it.i_collectionItems()?.map { child -> child.i_id() }) ?: emptyList()
                } else emptyList()
            }

        private fun <T> observer(repo: CardSeenStatusRepo, message: String): DisposableObserver<T> {
            return object : DisposableObserver<T>() {
                override fun onNext(t: T) {
                    Logger.d(LOG_TAG, "$message$t, ${repo._state()}")
                }

                override fun onError(e: Throwable) {
                    Logger.e(LOG_TAG, "$message: got $e", e)
                }

                override fun onComplete() {
                }
            }
        }
    }
}