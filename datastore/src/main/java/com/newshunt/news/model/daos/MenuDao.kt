/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.model.entity.COL_ACTION
import com.newshunt.dataentity.model.entity.COL_ID
import com.newshunt.dataentity.model.entity.TABLE_BOOKMARKS
import com.newshunt.dataentity.news.model.entity.MenuL1Filter
import com.newshunt.dataentity.social.entity.MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.MenuL1
import com.newshunt.dataentity.social.entity.MenuL2
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuOptionData
import com.newshunt.dataentity.social.entity.MenuOptionDataView
import com.newshunt.dataentity.social.entity.MenuOptionKey
import com.newshunt.dataentity.social.entity.TABLE_MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.TABLE_MenuL1
import com.newshunt.dataentity.social.entity.TABLE_MenuL2
import com.newshunt.dataentity.social.entity.VIEW_MenuOptionDataView

/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class MenuDao {

    /*
    * Visible for testing
    * */
    fun optionsMatching1(
            format: Format,
            subFormat: SubFormat,
            uiType: UiType2,
            location: String,
            entityId: String,
            entityType: String,
            entitySubType: String?,
            itemId: String,
            filterNotAllowed: List<String>
    ): LiveData<List<MenuOptionDataView>> {
        val notAllowedFilterLiveData = Transformations.map(getEntityFollowAction(entityId = entityId,
                entitySubType = entitySubType,
                entityType = entityType)) {
            MenuDao.notAllowedFilters(it)
        }
        return Transformations.switchMap(notAllowedFilterLiveData) { filterNotAllowed2 ->
            optionsMatching1(format, subFormat,
                    uiType,
                    location,
                    itemId,
                    filterNotAllowed.plus(filterNotAllowed2))
        }
    }

    @Query("""
        SELECT * FROM $VIEW_MenuOptionDataView m
        WHERE format = :format 
            AND subFormat = :subFormat 
            AND uiType = :uiType 
            AND location = :location
            AND ((m.filter NOT IN ( SELECT CASE WHEN c==1 THEN 'CAN_SAVE'
                ELSE 'CAN_UNSAVE'
                END
                FROM (SELECT count(*) as c from $TABLE_BOOKMARKS where  $COL_ID = :itemId AND 
                `$COL_ACTION` = 'ADD')))
                AND
                (m.filter NOT IN (:filterNotAllowed))
              OR m.filter is NULL)
                 """)
    abstract fun optionsMatching1(
            format: Format,
            subFormat: SubFormat,
            uiType: UiType2,
            location: String,
            itemId: String,
            filterNotAllowed: List<String>
    ): LiveData<List<MenuOptionDataView>>
    @Query("""
        SELECT * FROM $VIEW_MenuOptionDataView m
        WHERE format = :format 
            AND subFormat = :subFormat 
            AND uiType = :uiType 
            AND location = :location
            AND m.l1id  IN (:filterAllowed)""")
    abstract fun optionsMatching1(
            format: Format,
            subFormat: SubFormat,
            uiType: UiType2,
            location: String,
            filterAllowed: List<String>
    ): LiveData<List<MenuOptionDataView>>

    @Insert
    internal abstract fun ins(menuDictionaryEntity: MenuDictionaryEntity1): Long

    fun optionMatchingKeys(keys: List<String>, entityId: String?, entityType: String?,
                           entitySubType: String?): LiveData<List<MenuL1>> {
        val notAllowedFilterLiveData = Transformations.map(getEntityFollowAction(entityId = entityId,
                entitySubType = entitySubType,
                entityType = entityType)) {
            MenuDao.notAllowedFilters(it)
        }
        return Transformations.switchMap(notAllowedFilterLiveData) { filterNotAllowed2 ->
            optionMatchingKeys(keys, filterNotAllowed2)
        }
    }

    @Query("""
        SELECT `action` FROM follow WHERE entityId = :entityId AND entityType = :entityType AND 
        (entitySubType IS NULL OR entitySubType = :entitySubType) """)
    abstract fun getEntityFollowAction(entityId: String?,
                                       entityType: String?, entitySubType: String?): LiveData<List<FollowActionType>>

    @Query("""
        SELECT * FROM $TABLE_MenuL1 m
        WHERE m.l1id IN (:keys)
        AND (m.filter IS NULL OR m.filter NOT IN (:notAllowedFilteres))
           """)
    abstract fun optionMatchingKeys(keys: List<String>, notAllowedFilteres: List<String>): LiveData<List<MenuL1>>

    @Query("""
        SELECT * FROM $TABLE_MenuL2 m
        WHERE m.l2id IN (:keys)
           """)
    abstract fun l2OptionMatchingKeys(keys: List<String>): LiveData<List<MenuL2>>

    @Query(""" DELETE FROM $TABLE_MenuDictionaryEntity1 """)
    internal abstract fun deleteDictionary1()

    @Insert
    internal abstract fun insL2(menuL2: List<MenuL2>)

    @Insert
    internal abstract fun insL1(menuL1: List<MenuL1>)

    @Insert
    internal abstract fun insOptionKey(menuOptionKey: MenuOptionKey): Long

    @Insert
    internal abstract fun insOptionsValues(menuOptionData: List<MenuOptionData>)


    /**
     * Returns auto-gen id of dictionary entry
     */
    @Transaction
    open fun clearAndinsert1(menuDictionaryEntity: MenuDictionaryEntity1): Long {
        deleteDictionary1() // deletes will cascade
        val dictionaryId = ins(menuDictionaryEntity)
        insL1(menuDictionaryEntity.masterOptionsL1.map { it.copy(dictionaryIdL1 = dictionaryId) })
        insL2(menuDictionaryEntity.masterOptionsL2.map { it.copy(dictionaryIdL2 = dictionaryId) })
        val l1PresentMetaKeys = mutableSetOf<String>()
        l1PresentMetaKeys.addAll(menuDictionaryEntity.masterOptionsL1.map { it.id })

        val l2PresentMetaKeys = mutableSetOf<String>()
        l2PresentMetaKeys.addAll(menuDictionaryEntity.masterOptionsL2.map { it.id })

        menuDictionaryEntity.listL1Options.forEach { mapEntry ->
            val validItems = mapEntry.value.filter { mapItemEntry ->
                /*
                * 1.) l1 key should not be null
                * 2.) l1 key should be present in l1MetaList
                * 3.) l2 key either should be null or should be present in l2Metalist
                * */
                mapItemEntry.l1Key != null &&
                        l1PresentMetaKeys.contains(mapItemEntry.l1Key as String) &&
                        (mapItemEntry.l2Key == null || l2PresentMetaKeys.contains(mapItemEntry.l2Key as String))
            }
            if (!validItems.isNullOrEmpty()) {
                val (f, sf, uitype) = mapEntry.key.split("/")
                val loc = MenuLocation.LIST
                val key = insOptionKey(MenuOptionKey(format = Format.valueOf(f),
                        subFormat = SubFormat.valueOf(sf), uiType = UiType2.valueOf(uitype), location = loc.name, dictionaryIdMp = dictionaryId))
                insOptionsValues(validItems.mapIndexed { index, mp ->
                    mp.toOptionData(key, index)
                })
            }
        }
        menuDictionaryEntity.detailL1Options.map { mapEntry ->
            val validItems = mapEntry.value.filter { mapItemEntry ->
                /*
                * 1.) l1 key should not be null
                * 2.) l1 key should be present in l1MetaList
                * 3.) l2 key either should be null or should be present in l2Metalist
                * */
                mapItemEntry.l1Key != null &&
                        l1PresentMetaKeys.contains(mapItemEntry.l1Key as String) &&
                        (mapItemEntry.l2Key == null || l2PresentMetaKeys.contains(mapItemEntry.l2Key as String))
            }
            if (!validItems.isNullOrEmpty()) {
                val (f, sf, uitype) = mapEntry.key.split("/")
                val loc = MenuLocation.DETAIL
                val key = insOptionKey(MenuOptionKey(format = Format.valueOf(f),
                        subFormat = SubFormat.valueOf(sf), uiType = UiType2.valueOf(uitype), location = loc.name, dictionaryIdMp = dictionaryId))
                insOptionsValues(validItems.mapIndexed { index, mp ->
                    mp.toOptionData(key, index)
                })
            }
        }
        return dictionaryId
    }

    @Query("""
        SELECT * FROM $TABLE_MenuDictionaryEntity1
    """)
    internal abstract fun fetchMenuMeta(): List<MenuDictionaryEntity1>

    @Query("""
        SELECT * FROM $TABLE_MenuDictionaryEntity1
    """)
    internal abstract fun fetchMenuMetaLive(): LiveData<List<MenuDictionaryEntity1>>


    @Query("""
        SELECT postUrl FROM $TABLE_MenuDictionaryEntity1 LIMIT 1
    """)
    internal abstract fun postUrl(): String?

    @Query("UPDATE $TABLE_MenuDictionaryEntity1 SET version=0")
    abstract fun resetVersion()


    companion object {
        fun notAllowedFilters(list: List<FollowActionType>): List<String> {
            val default = listOf(MenuL1Filter.CAN_UNFOLLOW.name, MenuL1Filter.CAN_UNBLOCK.name)
            if (list.isEmpty()) {
                return default
            }
            return when (list[0]) {
                FollowActionType.BLOCK -> listOf(MenuL1Filter.CAN_BLOCK.name, MenuL1Filter.CAN_UNFOLLOW.name)
                FollowActionType.FOLLOW -> listOf(MenuL1Filter.CAN_FOLLOW.name, MenuL1Filter.CAN_UNBLOCK.name)
                else -> default
            }
        }
    }

}