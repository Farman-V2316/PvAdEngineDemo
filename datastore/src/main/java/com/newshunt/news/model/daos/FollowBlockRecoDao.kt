package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.common.pages.TABLE_SOURCE_FOLLOW_BLOCK


@Dao
abstract class FollowBlockRecoDao : BaseDao<SourceFollowBlockEntity> {

    @Query("SELECT * FROM $TABLE_SOURCE_FOLLOW_BLOCK WHERE `sourceId`=:id")
    abstract fun getSourceFollowBlockEntity(id:String):SourceFollowBlockEntity?

    @Query("SELECT * FROM $TABLE_SOURCE_FOLLOW_BLOCK ORDER BY `updateTimeStamp` DESC LIMIT 1 ")
    abstract fun getSourceFollowBlockEntityDesc():SourceFollowBlockEntity?

    @Query("SELECT * FROM $TABLE_SOURCE_FOLLOW_BLOCK ORDER BY `updateTimeStamp` DESC LIMIT 1 ")
    abstract fun getSourceFollowBlockEntityDescLiveData():LiveData<SourceFollowBlockEntity?>

    @Query("UPDATE $TABLE_SOURCE_FOLLOW_BLOCK SET `showImplicitFollowDialogCount` = `showImplicitFollowDialogCount`+1 WHERE `sourceId`=:sourceId")
    abstract fun incrementShowImplicitFollowDialogCount(sourceId:String)

    @Query("UPDATE $TABLE_SOURCE_FOLLOW_BLOCK SET `showImplicitBlockDialogCount` = `showImplicitBlockDialogCount`+1 WHERE `sourceId`=:sourceId")
    abstract fun incrementShowImplicitBlockDialogCount(sourceId:String)
}