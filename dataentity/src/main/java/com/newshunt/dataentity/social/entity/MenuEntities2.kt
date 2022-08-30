/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.pages.PageEntity
import java.io.Serializable

@Entity(tableName = TABLE_MenuDictionaryEntity1)
data class MenuDictionaryEntity1(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var postUrl: String? = null,
        var version: String? = null,
        var title: String? = null,
        var subTitle: String? = null,
        @Ignore
        var masterOptionsL1: List<MenuL1> = emptyList(),
        @Ignore
        var masterOptionsL2: List<MenuL2> = emptyList(),
        @Ignore
        var listL1Options: Map<String, List<Mp>> = emptyMap(),
        @Ignore
        var detailL1Options: Map<String, List<Mp>> = emptyMap()) : MenuMeta {

    override fun i_title(): String? = title
    override fun i_subTitle(): String? = title
    override fun i_postUrl(): String? = postUrl
    companion object {
        const val TABLE = "MenuDictionaryEntity1"
    }
}

data class Mp(
        val l1Key: String?,
        val l2Key: String?
) {
    fun toOptionData(mapId: Long, index: Int) =
            MenuOptionData(l1Key ?: Constants.EMPTY_STRING, l2Key, mapId, index)
}

/**
 *
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_MenuL1,
        foreignKeys = [ForeignKey(entity = MenuDictionaryEntity1::class, parentColumns = ["id"], childColumns = ["dictionaryIdL1"],
        onDelete = CASCADE)])
data class MenuL1(@PrimaryKey @ColumnInfo(name = "l1id")
                  val id: String = Constants.EMPTY_STRING,
                  val icon: String? = null,
                  val nIcon: String? = null,
                  val clickAction: String? = null, //MenuClickAction? = null,
                  val hideCard: Boolean? = null,
                  val isDislike: Boolean? = null,
                  val filter: String? = null, // MenuFilter? = null,
                  val postAction: String? = null, // MenuPostAction? = null,
                  val browserUrl: String? = null,
                  val title: String? = null,
                  val hideForCreator: Boolean = false,
                  val isDislikeL2: Boolean? = null,
                  val eventName: String? = null,
                  val dictionaryIdL1: Long = 0) : Serializable {
    fun toMenuOption(): MenuOption {
        return MenuOption(menuL1 = this, menuL2 = null)
    }
    companion object {
        const val TABLE = "MenuL1"
    }
}

/**
 *
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_MenuL2,
        foreignKeys = [ForeignKey(entity = MenuDictionaryEntity1::class, parentColumns = ["id"], childColumns = ["dictionaryIdL2"],
        onDelete = CASCADE)])
data class MenuL2(@PrimaryKey @ColumnInfo(name = "l2id")
                  val id: String = Constants.EMPTY_STRING,
                  val content: String? = null,
                  val dictionaryIdL2: Long = 0)

/**
 *
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_MenuOptionKey,
        foreignKeys = [ForeignKey(entity = MenuDictionaryEntity1::class, parentColumns = ["id"], childColumns = ["dictionaryIdMp"],
        onDelete = CASCADE)])
data class MenuOptionKey(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val format: Format,
        val subFormat: SubFormat,
        val uiType: UiType2,
        val location: String,
        val dictionaryIdMp: Long
)

/**
 *
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_MenuOptionData,
        primaryKeys = ["l1", "mappingId"], foreignKeys = [ForeignKey(entity = MenuOptionKey::class,
        parentColumns = ["id"], childColumns = ["mappingId"], onDelete = CASCADE)])
data class MenuOptionData(
        val l1: String,
        val l2: String?,
        val mappingId: Long,
        val index: Int
)

const val Q_MenuOpDataView = "SELECT * from $TABLE_MenuOptionData m LEFT JOIN $TABLE_MenuL1 l1 ON m.l1 = l1.l1id LEFT JOIN $TABLE_MenuL2 l2 ON m.l2 = l2.l2id LEFT JOIN $TABLE_MenuOptionKey k ON m.mappingId = k.id WHERE m.l1 = l1.l1id ORDER BY `index`"
@DatabaseView(value = Q_MenuOpDataView, viewName = VIEW_MenuOptionDataView)
data class MenuOptionDataView(
        val l1: String,
        val l2: String?,
        val mappingId: Long,
        val index: Int,
        @Embedded
        val menuL1: MenuL1,
        @Embedded
        val menuL2: MenuL2?,
        @Embedded
        val menuOptionKey: MenuOptionKey
) {
    fun toMenuOption() = MenuOption(menuL1, menuL2)
}


/**
 *
 * @author satosh.dhanyamraju
 */
enum class MenuLocation(val primaryType: MenuLocation?) {
    LIST(null),
    DETAIL(null),
    NP_LANDING(null),
    HASHTAG(null),
    NONE(null),
    GROUP_LIST(LIST),
    PROFILE_POST_LIST(LIST),
    DETAIL_UNIFIED_BAR(DETAIL),
    COMMENTS(LIST)
}


/**
 *
 * @author satosh.dhanyamraju
 */
data class MenuOption(
        @Embedded
        val menuL1: MenuL1,
        @Embedded
        val menuL2: MenuL2?
)

data class MenuOptionListData(
        val menuList: List<MenuOption>,
        val card: CommonAsset? = null,
        val pageEntity: PageEntity? = null
)


/**
 *
 * @author amit.chaudhary
 */
interface MenuMeta {
    fun i_postUrl(): String? = null
    fun i_title(): String? = null
    fun i_subTitle(): String? = null
}


enum class MenuL1Id(val l2: MenuL2Id?) {
    L1_BLOCK_SOURCE(null),
    L1_UNBLOCK_SOURCE(null),
    L1_SHARE(null),
    L1_BROWSE_MY_POST(null),
    L1_DELETE_POST(null),
    L1_REPORT(MenuL2Id.HTML_3),
    L1_SAVE_VIDEO(null)
}

enum class MenuL2Id {
    HTML_3
}