package com.newshunt.profile

import android.view.View
import java.io.Serializable

/**
 * Created by karthik.r on 2020-02-25.
 */
data class SimpleOptionItem
@JvmOverloads constructor(val drawableId: Int? = View.NO_ID,
                          val displayText: String,
                          val anyEnumerationAsEnum: Serializable,
                          val uiProperties: UiProperties? = null,
                          val iconUrl: String? = null,
                          var isSelected:Boolean? = false) : Serializable

data class SimpleOptions(val optionsList: List<SimpleOptionItem>,
                         val hostId: Int,
                         val heading: String? = null) : Serializable

class UiProperties : Serializable{
    var selected: Boolean = false
    var type = SimpleOptionItemType.NORMAL
    var textColor: Int? = null
    var imageIconSize: Int? = null
}

enum class SimpleOptionItemType(val index: Int) {
    NORMAL(0), DIVIDER(1);

    companion object {
        @JvmStatic
        fun getEnumFromIndex(index: Int): SimpleOptionItemType {
            return SimpleOptionItemType.values().find { it.index == index } ?: NORMAL
        }
    }
}