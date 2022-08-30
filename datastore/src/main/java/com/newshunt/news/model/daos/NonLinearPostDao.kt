package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.asset.NLFCItem
import io.reactivex.Observable

@Dao
abstract class NonLinearPostDao : BaseDao<NLFCItem> {

	@Query("SELECT * FROM nlfc WHERE isInserted=0 LIMIT 1")
	abstract fun getNLFCList() : LiveData<NLFCItem?>

	@Query("SELECT * FROM nlfc WHERE isConsumed=0")
	abstract fun getNonLinearForList() : LiveData<List<NLFCItem>>

	@Query("UPDATE nlfc SET isConsumed=1 WHERE postId=:postId")
	abstract fun markPostAsConsumed(postId: String)

	@Query("DELETE FROM nlfc")
	abstract fun cleanUpNlfc()

}