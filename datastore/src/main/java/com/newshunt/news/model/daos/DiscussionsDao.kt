package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.asset.Associations
import com.newshunt.dataentity.common.asset.Discussions
import com.newshunt.dataentity.social.entity.TABLE_CARD

/**
 * Created by karthik.r on 2019-11-16.
 */
@Dao
abstract class DiscussionsDao : BaseDao<Discussions> {

    @Query("""
        DELETE FROM discussions WHERE parentId IN (
            SELECT uniqueId FROM $TABLE_CARD c LEFT JOIN fetch_data f ON c.uniqueId = f.storyId 
            WHERE c.id=:postId AND f.fetchId IN (SELECT col_fetchInfoId FROM fetch_info WHERE
                col_entity_id = :entityId AND col_disp_loc = :location AND section = :section))
    """)
    abstract fun deleteForParentId(postId: String, entityId: String, location: String, section:String)

    @Query("DELETE FROM discussions WHERE parentId = :postId AND child_id = :discussionId")
    abstract fun deleteDiscussion(postId: String, discussionId: String)

    @Query("DELETE FROM discussions")
    abstract fun deleteAllDiscussion()

}



@Dao
abstract class AssociationsDao : BaseDao<Associations>
