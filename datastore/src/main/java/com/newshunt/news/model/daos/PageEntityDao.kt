package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.PageSyncEntity
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.common.pages.TopicsEntity
import com.newshunt.dataentity.social.entity.TABLE_ADD_PAGE

import io.reactivex.Observable

@Dao
abstract class PageEntityDao : BaseDao<S_PageEntity> {

  /**
   * Using Observable as return-type instead of LiveData, to fix pages-refresh-issue in on-boarding flow.
   */
  @Query("SELECT * FROM pages WHERE section=:section ORDER BY viewOrder")
  abstract fun getPageEntities(section: String): Observable<List<S_PageEntity>>

  @Query("SELECT id FROM pages WHERE section=:section ORDER BY viewOrder Limit 1")
  abstract fun getFirstPageId(section: String): String?

  @Query("SELECT * FROM pages WHERE id IN (SELECT id FROM follow)")
  abstract fun getFollowedHomePages(): List<S_PageEntity>

  @Query("SELECT * FROM pages WHERE section=:section")
  abstract fun getAllPages(section: String): List<S_PageEntity>?

  @Query("SELECT * FROM pages WHERE section=:section ORDER BY viewOrder Limit 1")
  abstract fun getHomePage(section: String): S_PageEntity?

  @Query("SELECT * FROM pages WHERE section=:section AND id=:entityId")
  abstract fun getPageForId(section: String, entityId: String): S_PageEntity?

  @Transaction
  open fun getPageOrHome(section: String, entityId: String): S_PageEntity? {
    return getPageForId(section, entityId) ?: getHomePage(section)
  }

  //todo prevent ondelete cascade fetchinfo
  @Query("DELETE FROM pages WHERE section=:section")
  abstract fun deleteAllEntites(section: String)

  @Query("DELETE FROM pages WHERE id=:id")
  abstract fun deletePage(id: String)

  @Transaction
  open fun updateAfterPageSync(list: List<S_PageEntity>, section: String) {
    deleteAllEntites(section)
    insReplace(list)
  }
}

@Dao
abstract class AddPageDao : BaseDao<AddPageEntity> {

  @Query("SELECT * FROM $TABLE_ADD_PAGE ORDER BY time")
  abstract fun getAddPageList() : LiveData<List<AddPageEntity>>

  @Query("SELECT * FROM $TABLE_ADD_PAGE WHERE id=:id")
  abstract fun getAddPageEntity(id: String) : AddPageEntity?

  @Query("DELETE FROM $TABLE_ADD_PAGE WHERE id=:id")
  abstract fun deleteAddPageEntity(id: String)

  @Query("SELECT * FROM $TABLE_ADD_PAGE WHERE mode='added' LIMIT 1")
  abstract fun getFirstAddedPage() : LiveData<AddPageEntity>

  @Query("DELETE FROM $TABLE_ADD_PAGE")
  abstract fun clearAddPages()

  @Transaction
  open fun toggleAddPage(addPage: AddPageEntity) {
    val entity = getAddPageEntity(addPage.id)
    if (entity == null) {
      insReplace(addPage)
    } else {
      deleteAddPageEntity(addPage.id)
    }
  }
}

@Dao
abstract class PageSyncEntityDao : BaseDao<PageSyncEntity> {

  @Query("SELECT * FROM pagesync WHERE mode =:mode AND section=:section")
  abstract fun getPageSyncEntities(mode: String, section: String) : List<PageSyncEntity>

  @Query("SELECT * FROM pagesync WHERE section=:section")
  abstract fun getAllPageSyncEntity(section: String) : List<PageSyncEntity>

  @Query("DELETE FROM pagesync WHERE section=:section")
  abstract fun deleteAllPageSyncEntity(section: String)

}

@Dao
abstract class PageableTopicsDao : BaseDao<TopicsEntity> {

    @Query("""
    SELECT t.* , 
    CASE WHEN p.id IS NULL THEN 0 ELSE 1 END isFavorite,
    CASE WHEN fl.entityId IS NULL THEN 0 ELSE 1 END isFollowed
    FROM pageableTopics t 
    LEFT JOIN pages p ON t.id=p.id AND t.section = p.section
    LEFT JOIN follow fl ON fl.entityId = p.id AND fl.`action`='FOLLOW'
    WHERE t.section=:section ORDER BY viewOrder
  """)
  abstract fun getPageableTopics(section: String) : LiveData<List<PageableTopicsEntity>>

  @Query("DELETE FROM pageabletopics WHERE section=:section")
  abstract fun deletePageableTopics(section: String)

  @Query("DELETE FROM pageabletopics")
  abstract fun cleanUpPageableTopics()

  @Transaction
  open fun replaceTopics(list: List<TopicsEntity>, section: String) {
    deletePageableTopics(section)
    insReplace(list)
  }
}