/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeState
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.EventsActivity
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.news.model.sqlite.SocialDB
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class NudgeDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var nudgeDao: NudgeDao


    private val nudgeState = CardNudgeState(0, true, false)
    private val shareNudge = CardNudge(50, PostEntityLevel.LOCAL, Format.HTML, null, null, null, "nudge_card", "share", "share with your friends and family", 10, 2, 1, nudgeState)
    private val commentNudge = CardNudge(52, null, null, null, null, true, "nudge_card", "comment", "Join the conversation", 10, 1, 1, nudgeState)
    private val reposttNudge = CardNudge(51, null, Format.POST_COLLECTION, SubFormat.HTML, null, null, "nudge_card", "repost",  "Repost this story to start the conversation", 10, 1, 1, nudgeState)
    private val cardInfo_share = CardInfo("id1", PostEntityLevel.LOCAL, Format.HTML, SubFormat.STORY, UiType2.NORMAL, false)
    private val cardInfo_comment = CardInfo("id3", PostEntityLevel.TOP_LEVEL, Format.HTML, SubFormat.STORY, UiType2.GRID_2, true)
    private val cardInfo_repost = CardInfo("id4", PostEntityLevel.TOP_LEVEL, Format.POST_COLLECTION, SubFormat.HTML, UiType2.NORMAL, false)
    private val eventsInfo_share = EventsInfo("", shareNudge.id, null, EventsActivity().apply {
        type = "nudge_card"
        attributes = hashMapOf(
                "tooltipDurationSec" to "10",
                "text" to "share with your friends and family"
        )
    }, precondition = hashMapOf(
            "level" to "LOCAL",
            "maxAttempts" to "5",
            "sessionGroup" to "1",
            "terminationType" to "share",
            "format" to "HTML"
    ))

    private val eventsInfo_shareJson = JsonUtils.fromJson("{\n" +
            "        \"id\": 50,\n" +
            "        \"event\": \"nudge\",\n" +
            "        \"precondition\": {\n" +
            "          \"level\": \"LOCAL\",\n" +
            "          \"maxAttempts\": 2,\n" +
            "          \"sessionGroup\": 1,\n" +
            "          \"terminationType\": \"share\",\n" +
            "          \"format\": \"HTML\"\n" +
            "                 },\n" +
            "        \"resource\": \"news\",\n" +
            "        \"activity\": {\n" +
            "          \"type\": \"nudge_card\",\n" +
            "          \"attributes\": {\n" +
            "            \"tooltipDurationSec\": 10,\n" +
            "            \"text\": \"share with your friends and family\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n", EventsInfo::class.java)

    private val eventsInfo_repost = JsonUtils.fromJson("{\n" +
            "        \"id\": 51,\n" +
            "        \"event\": \"nudge\",\n" +
            "        \"precondition\": {\n" +
            "          \"format\": \"POST_COLLECTION\",\n" +
            "          \"subFormat\": \"HTML\",\n" +
            "          \"maxAttempts\": 1,\n" +
            "          \"sessionGroup\": 1,\n" +
            "          \"terminationType\": \"repost\"\n" +
            "        },\n" +
            "        \"resource\": \"news\",\n" +
            "        \"activity\": {\n" +
            "          \"type\": \"nudge_card\",\n" +
            "          \"attributes\": {\n" +
            "            \"tooltipDurationSec\": 10,\n" +
            "            \"text\": \"Repost this story to start the conversation\",\n" +
            "            \"ctaText\": \"Repost\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n", EventsInfo::class.java)

    private val eventsInfo_comment = JsonUtils.fromJson("{\n" +
            "        \"id\": 52,\n" +
            "        \"event\": \"nudge\",\n" +
            "        \"precondition\": {\n" +
            "          \"hasCommentsOrReposts\": \"true\",\n" +
            "          \"maxAttempts\": 1,\n" +
            "           \"sessionGroup\": 1,\n" +
            "          \"terminationType\": \"comment\"\n" +
            "        },\n" +
            "        \"resource\": \"news\",\n" +
            "        \"activity\": {\n" +
            "          \"type\": \"nudge_card\",\n" +
            "          \"attributes\": {\n" +
            "            \"tooltipDurationSec\": 10,\n" +
            "            \"text\": \"Join the conversation\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n", EventsInfo::class.java)

    private val eventsInfo_like = EventsInfo("", 42, null, EventsActivity().apply {
        type = "nudge_card"
        attributes = hashMapOf(
                "tooltipDurationSec" to "10",
                "text" to "share with your friends and family"
        )
    }, precondition = hashMapOf(
            "level" to "LOCAL",
            "maxAttempts" to "5",
            "sessionGroup" to "1",
            "terminationType" to "share",
            "format" to "HTML"
    ))
    private val eventsInfo_share_invalid = EventsInfo("", shareNudge.id, null, EventsActivity().apply { type = "nudge_card1" })

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        nudgeDao = instance.nudgeDao()
        nudgeDao.insertIgnore(shareNudge)
        nudgeDao.insertIgnore(commentNudge)
        nudgeDao.insertIgnore(reposttNudge)
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testReadCardNudges_returnsMatchingNudgeOrNull() {
        assertThat(nudgeDao.all().size).isEqualTo(3)
        val c1 = CardInfo("id2", PostEntityLevel.TOP_LEVEL, Format.HTML, SubFormat.STORY, UiType2.NORMAL, false)
        assertThat(nudgeDao.readCardNudges(listOf(
                cardInfo_share,
                c1,
                cardInfo_comment,
                cardInfo_repost
        ))).containsExactly(
                cardInfo_share to shareNudge,
                c1 to null,
                cardInfo_comment to commentNudge,
                cardInfo_repost to reposttNudge
        )
    }

    @Test
    fun testReadCardNudges_doesNotReturnInactiveAndTerminatedCards() {

    }

    @Test
    fun testCardShown_DeActivatesAllNudgesInTheSameGroup() {
        nudgeDao.insertIgnore(shareNudge.copy(id = 56, sessionGroup = shareNudge.sessionGroup + 10))
        assertThat(nudgeDao.all().map { it.state?.active }).containsExactly(true, true, true, true)
        nudgeDao.markShown(commentNudge.id)
        assertThat(nudgeDao.all().map { it.state?.active }).containsExactly(false, false, false, true)
        nudgeDao.markAllActive()
        assertThat(nudgeDao.all().map { it.state?.active }).containsExactly(true, true, false /* terminated ones are not modified*/, true)
    }

    @Test
    fun testCardShown_TerminatesNudgeIfCountMatches() {
        fun storedCommentNudge() = nudgeDao.all().find { it.id == commentNudge.id }
        assertThat(storedCommentNudge()?.state?.curAttempts).isEqualTo(0)
        nudgeDao.markShown(commentNudge.id)
        assertThat(storedCommentNudge()?.state?.curAttempts).isEqualTo(1)
    }

    @Test
    fun testCardShown_IncViewCount() {
        fun storedCommentNudge() = nudgeDao.all().find { it.id == commentNudge.id }
        assertThat(storedCommentNudge()?.state?.terminated).isFalse()
        nudgeDao.markShown(commentNudge.id)
        assertThat(storedCommentNudge()?.state?.terminated).isTrue()
    }

    @Test
    fun testCardShared_terminates() {
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_share))).containsExactly(cardInfo_share to shareNudge)
        nudgeDao.terminate(CardNudgeTerminateType.share.name)
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_share))).containsExactly(cardInfo_share to null)
    }

    @Test
    fun testReposted_terminates() {
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_repost))).containsExactly(cardInfo_repost to reposttNudge)
        nudgeDao.terminate(CardNudgeTerminateType.repost.name)
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_repost))).containsExactly(cardInfo_repost to null)
    }

    @Test
    fun testCommented_terminates() {
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_comment))).containsExactly(cardInfo_comment to commentNudge)
        nudgeDao.terminate(CardNudgeTerminateType.comment.name)
        assertThat(nudgeDao.readCardNudges(listOf(cardInfo_comment))).containsExactly(cardInfo_comment to null)
    }


    @Test
    fun testUpdateFrom_FiltersOnlyCardNudges() {
        assertThat(nudgeDao.all()).containsExactly(shareNudge, commentNudge, reposttNudge)
        nudgeDao.updateFrom(listOf(eventsInfo_share_invalid))
        assertThat(nudgeDao.all()).isEmpty()
    }


    @Test
    fun testUpdateFrom_DeletesRowsNotPresentInResponse() {
        assertThat(nudgeDao.all().map { it.id }).containsExactly(shareNudge.id, commentNudge.id, reposttNudge.id)
        nudgeDao.updateFrom(listOf(eventsInfo_share))
        assertThat(nudgeDao.all().map { it.id }).containsExactly(shareNudge.id)
    }

    @Test
    fun testUpdateFrom_UpdateRowsPresentInResp() {
        assertThat(nudgeDao.lookup(shareNudge.id)).isEqualTo(shareNudge)
        nudgeDao.updateFrom(listOf(eventsInfo_share))
        assertThat(nudgeDao.lookup(shareNudge.id)).isEqualTo(shareNudge.copy(maxAttempts = 5))
    }

    @Test
    fun testUpdateFrom_InsertsRowsPresentInRespButNotInDB() {
        assertThat(nudgeDao.all().map { it.id }).containsExactly(shareNudge.id, commentNudge.id, reposttNudge.id)
        nudgeDao.updateFrom(listOf(eventsInfo_share, eventsInfo_like))
        assertThat(nudgeDao.all().map { it.id }).containsExactly(shareNudge.id, eventsInfo_like.id)
    }

    @Test
    fun test_jsonParsing() {
        nudgeDao.updateFrom(listOf(eventsInfo_shareJson, eventsInfo_comment, eventsInfo_repost))
        assertThat(nudgeDao.all()).containsExactly(shareNudge, reposttNudge, commentNudge)
    }
}