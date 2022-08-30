/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import com.google.common.truth.Truth
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.InvalidCard
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.ListTransformType
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * @author satosh.dhanyamraju
 */
class CardDeserializerTest {

    @Language("JSON") val j_pa_proper = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"VIDEO\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\"\n}"
    @Language("JSON") val j_pa_unknownformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"VIDEO1\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\"\n}"
    @Language("JSON") val j_pa_noformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\"\n}"
    @Language("JSON") val j_pe_unknownsubformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"subFormat\": \"UNK\",\n  \"uiType\":  \"NORMAL\"\n}"
    @Language("JSON") val j_pe_nosubformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"uiType\":  \"NORMAL\"\n}"
    @Language("JSON") val j_pa_morestories_unknownuitype = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\",\n  \"moreStories\" : [\n    {\n      \"id\": \"id\",\n      \"type\": \"atype\",\n      \"format\":  \"HTML\",\n      \"subFormat\": \"STORY\",\n      \"uiType\":  \"UNK\"\n    }\n  ]\n}"
    @Language("JSON") val j_pa_morestories_nouitype = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\",\n  \"moreStories\" : [\n    {\n      \"id\": \"id\",\n      \"type\": \"atype\",\n      \"format\":  \"HTML\",\n      \"subFormat\": \"STORY\"\n    }\n  ]\n}"
    @Language("JSON") val j_pa_repost_noformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\",\n  \"repostAsset\" :\n    {\n      \"id\": \"id\",\n      \"type\": \"atype\",\n      \"subFormat\": \"STORY\",\n      \"uiType\":  \"NORMAL\"\n    }\n}"
    @Language("JSON") val j_pa_repost_unknownformat = "{\n  \"id\": \"id\",\n  \"type\": \"atype\",\n  \"format\":  \"HTML\",\n  \"subFormat\": \"STORY\",\n  \"uiType\":  \"NORMAL\",\n  \"repostAsset\" :\n    {\n      \"id\": \"id\",\n      \"type\": \"atype\",\n      \"format\":  \"UNK\",\n      \"subFormat\": \"STORY\",\n      \"uiType\":  \"NORMAL\"\n    }\n}"

    @Language("JSON") val badCardInQa = """
        
              {
        "id": "COL_1576154641249",
        "publishTime": 0,
        "source": {},
        "showPublishDate": false,
        "viewOrder": 0,
        "experiment": {},
        "recommendationTs": 0,
        "moreContentLoadUrl": "http://qa-news.newshunt.com/api/v2/posts/article/content/COL_1576154641249?useWidgetPosition=false&feedConfigKey=FOR_YOU",
        "includeCollectionInSwipe": false,
        "sticky": false,
        "createdTime": 0,
        "isApprovalPending": false,
        "nonLinearPostUrl": "http://stage-api-news.dailyhunt.in/api/v2/posts/nlf/feed/COL_1576154641249",
        "clickOutEnabled": false
      }
        
    """.trimIndent()
    private fun String.toAnyCard() = gsonForCards.fromJson<AnyCard>(this, AnyCard::class.java)
    private val l =  object: InvalidCardsLogger {
        override fun log(message: String, pojo: Any) {

        }
    }
    private val gsonForCards = CardDeserializer.gson(null,l)

    @Test
    fun testTransf() {
        val f = FilteroutUnknownCards(null, ListTransformType.DEFAULT, l)
        Truth.assertThat(f.transf(listOf(j_pa_proper.toAnyCard()))).hasSize(1)
        Truth.assertThat(f.transf(listOf(j_pa_noformat.toAnyCard()))).isEmpty()
    }


    @Test
    fun testDeserializer() {
        Truth.assertThat(j_pa_proper.toAnyCard()).isInstanceOf(PostEntity::class.java)
        Truth.assertThat(j_pa_noformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pa_unknownformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pe_unknownsubformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pe_nosubformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pa_repost_noformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pa_repost_unknownformat.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pa_morestories_unknownuitype.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(j_pa_morestories_nouitype.toAnyCard()).isInstanceOf(InvalidCard::class.java)
        Truth.assertThat(badCardInQa.toAnyCard()).isInstanceOf(InvalidCard::class.java)
    }

}
