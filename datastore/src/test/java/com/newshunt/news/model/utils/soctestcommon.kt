/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.asset.ColdStartEntity
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.CollectionEntity
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.news.model.entity.server.asset.ShareParam
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.PostUploadStatus

/**
 * contains functions and properties common for all tests
 *
 */

 val post1 = PostEntity(id = "p1i", type = "news", title = "title1", source = PostSourceAsset
("times", type = "src", entityType = "src"))
val post2 = PostEntity(id = "p2i", type = "news", title = "title2", source = PostSourceAsset
("times2", type = "src", entityType = "src"))
val post3 = PostEntity(id = "p3i", type = "photo", title = "title3")
val post4 = PostEntity(id = "p4i", "news",title = "title4", source = PostSourceAsset
("times", type = "src", entityType = "src"), ignoreSourceBlock = true)
val post12 = post1.copy(moreStories = listOf(post2))
val post23 = post2.copy(moreStories = listOf(post3))
val post1c2 = post1.copy(collectionAsset = CollectionEntity(listOf(post2)))
val adpost = PostEntity("adid", format = Format.AD, subFormat = SubFormat.AD)

val cp1 = CreatePostEntity(language = "en", postId = "42", cpId = 42 , uiMode = CreatePostUiMode.ALL, isLocalcardShown = true, state = PostUploadStatus.SUCCESS)
val foryouid = "foryouid"
val ad1 = PostEntity(id="ad1", format = Format.AD)
val ad2 = PostEntity(id="ad2", format = Format.AD)

val postCold1= post1.copy(coldStartAsset = ColdStartEntity("cs-p", coldStartCollectionItems = listOf(ColdStartEntityItem("cs-e1"))))
val postCold2= post2.copy(coldStartAsset = ColdStartEntity("cs-p", coldStartCollectionItems = listOf(ColdStartEntityItem("cs-e1"))))

fun <T> LiveData<T>.latestValue() = LiveDataTestUtil.getValue(this)


val storyIdAndGroupType =
        Correspondence.from({ p1: Any?, p2: Any? ->
            val f = when (p1) {
                is CommonAsset? -> p1?.i_id() to p1?.i_type()
                else -> null to null
            }
            val s = when (p2) {
                is CommonAsset? -> p2?.i_id() to p2?.i_type()
                else -> null to null
            }
            f.first == s.first && f.second == s.second

        }, "similar")

val samplePageEntity = S_PageEntity(PageEntity(
        id = "foryou",
        name = "",
        displayName = "",
        entityType = "",
        subType = "",
        entityLayout = "",
        contentUrl = "asoethuos",
        entityInfoUrl = "",
        handle = "",
        deeplinkUrl = "",
        moreContentLoadUrl = "",
        entityImageUrl = "",
        shareParams = ShareParam(),
        shareUrl = "",
        nameEnglish = "",
        contentRequestMethod = "GET",
        enableWebHistory = true,
        viewOrder = 1,
        isFollowable = true,
        appIndexDescription = ""),
        section = PageSection.NEWS.section)


val sampleSaveCarousel = """
          {
        "id": "SAVED_STORY_1576590672131",
        "publishTime": 0,
        "counts": {
          "STORY": {
            "value": "6",
            "ts": 1576590672164
          }
        },
        "format": "POST_COLLECTION",
        "subFormat": "HTML",
        "uiType": "CAROUSEL_6",
        "showPublishDate": false,
        "title": "Multimedia collection card",
        "viewOrder": 0,
        "recommendationTs": 0,
        "includeCollectionInSwipe": false,
        "sticky": false,
        "createdTime": 0,
        "isApprovalPending": false,
        "collectionAsset": {
          "collectionItem": [
            {
              "id": "dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6",
              "content": " <p> By PTI </p> <p style=\"\" >LONDON: A UK court has ordered an Indian-origin drug dealer to pay back 50,000 pounds accrued from a plot to flood the streets with illegal drugs.</p> <p>Ravindra Modha had a confiscation order imposed on him under the UK&apos;s Proceeds of Crime Act (POCA) by Leicester Crown Court last Friday.</p> <p>The order allows police to take assets and cash from criminals who have benefitted financially from their criminality.</p> <p>Modha, 57, was sentenced to 16 years&apos; imprisonment after being found guilty of conspiracy to import cocaine in March last year.</p> <p>Paul Wenlock, from Leicestershire Police&apos;s Economic Crime Unit (ECU), said: &quot;Modha was involved in a plot to import drugs on a very large scale&quot; including cocaine with a street value of 9.6 million pounds and a kilogram of heroin with a street value of 316,000 pounds. </p> <p>&quot;He as able to make a vast sum of money from his crimes and this is something we will always look to rectify following the conviction of offenders.&quot; </p> <p style = \"margin-bottom: 0px;\">Four other men involved in the plot were also sentenced for their crimes following an investigation by the East Midlands Special Operations Unit last year.<nh-chunk-delimit /></p>",
              "publishTime": 1576517520000,
              "format": "HTML",
              "subFormat": "STORY",
              "langCode": "en",
              "type": "POST",
              "uiType": "HERO",
              "source": {
                "id": "dhb94a0041718e45b5b1eface00cdef265",
                "sourceName": "The New Indian Express",
                "displayName": "The New Indian Express",
                "type": "OGC",
                "entityType": "SOURCE",
                "counts": {
                  "STORY": {
                    "value": "257",
                    "ts": 1576590672164
                  }
                },
                "feedType": "SOURCE",
                "langCode": "en",
                "legacyKey": "newexpress"
              },
              "showPublishDate": false,
              "title": "UK court orders Indian-origin drug dealer to pay back 50,000 pounds",
              "titleEnglish": "UK court orders Indian-origin drug dealer to pay back 50,000 pounds",
              "shareUrl": "http://stage.dhunt.in/G3bl",
              "viewOrder": 0,
              "recommendationTs": 0,
              "moreContentLoadUrl": "http://qa-news.newshunt.com/api/v2/posts/article/content/dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6?useWidgetPosition=false&feedConfigKey=null",
              "deeplinkUrl": "https://m.dailyhunt.in/news/india/english/the+new+indian+express-epaper-dhb94a0041718e45b5b1eface00cdef265/uk+court+orders+indianorigin+drug+dealer+to+pay+back+50000+pounds-newsid-dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6",
              "publisherStoryUrl": "https://www.newindianexpress.com/world/2019/dec/16/uk-court-orders-indian-origin-drug-dealer-to-pay-back-50000-pounds-2077034.html",
              "includeCollectionInSwipe": false,
              "sticky": false,
              "thumbnailUrls": [
                "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_aeaf102c466012033308e60763c29d3e568bfad69b92baaabaafe61ea2a612ed.webp"
              ],
              "hashtags": [
                {
                  "name": "#dhtopic3",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic3-topics-376d57f5f7aec4ccd75b18625fbbae64/all-subtopics-all"
                },
                {
                  "name": "#dhtopic4",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic4-topics-e278aa8fdf564140fd76faafb030efe0/all-subtopics-all"
                },
                {
                  "name": "#dhtopic5",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic5-topics-77351711a8d45e30bfc2dbebe6c8f045/all-subtopics-all"
                }
              ],
              "createdTime": 1576564617000,
              "contentImage": "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_aeaf102c466012033308e60763c29d3e568bfad69b92baaabaafe61ea2a612ed.webp",
              "isApprovalPending": false,
              "state": "PUBLISHED",
              "categoryKey": "International",
              "genres": [
                "G1100"
              ],
              "sourceProvidedContentUrl": "https://www.newindianexpress.com/world/2019/dec/16/uk-court-orders-indian-origin-drug-dealer-to-pay-back-50000-pounds-2077034.html",
              "allowComments": true,
              "counts": {
                "VIEWS": {
                  "value": "6",
                  "ts": 1576590672164
                }
              },
              "languageKey": "en",
              "clickOutEnabled": false
            },
            {
              "id": "dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b",
              "content": " <p> <strong>New Delhi</strong>: The Supreme Court on Wednesday granted bail to former finance minister and Congress leader P Chidambaram in INX Media money laundering case, registered by the Enforcement Directorate (ED).</p> <p>Granting bail to former finance minister, the top court said P Chidambaram should not tamper with the evidence and not influence the witnesses. The court ordered that he should not give press interviews or make public statements in connection with this case.</p> <p style=\"border:0px;margin-top:0px;margin-bottom:0px;padding:0px;\" ></p> <p style = \"margin-bottom: 0px;\">The court directed P Chidambaram to furnish a bail bond of Rs 2 lakhs along with 2 sureties of the same amount and said he could not travel abroad without the Court's permission.<nh-chunk-delimit /></p>",
              "publishTime": 1575438300000,
              "format": "HTML",
              "subFormat": "STORY",
              "langCode": "en",
              "type": "POST",
              "uiType": "HERO",
              "source": {
                "id": "dh6dc1c103eefa45689fe7e3e137065edb",
                "sourceName": "Deccan Chronicle",
                "displayName": "Deccan Chronicle",
                "type": "OGC",
                "entityType": "SOURCE",
                "counts": {
                  "FOLLOW": {
                    "value": "123",
                    "ts": 1576590672173
                  },
                  "STORY": {
                    "value": "4.1k",
                    "ts": 1576590672173
                  }
                },
                "feedType": "SOURCE",
                "langCode": "en",
                "legacyKey": "deccanch"
              },
              "showPublishDate": false,
              "title": "After 105 days in custody, SC grants bail to Chidambaram in INX case",
              "titleEnglish": "After 105 days in custody, SC grants bail to Chidambaram in INX case",
              "shareUrl": "http://stage.dhunt.in/FxVi",
              "viewOrder": 0,
              "recommendationTs": 0,
              "moreContentLoadUrl": "http://qa-news.newshunt.com/api/v2/posts/article/content/dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b?useWidgetPosition=false&feedConfigKey=null",
              "deeplinkUrl": "https://m.dailyhunt.in/news/india/english/deccan+chronicle-epaper-dh6dc1c103eefa45689fe7e3e137065edb/after+105+days+in+custody+sc+grants+bail+to+chidambaram+in+inx+case-newsid-dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b",
              "publisherStoryUrl": "http://www.deccanchronicle.com/nation/current-affairs/041219/sc-grants-bail-to-chidambaram-in-inx-media-case-registered-by-ed.html",
              "includeCollectionInSwipe": false,
              "sticky": false,
              "thumbnailUrls": [
                "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_c42fcc3f3f012a3dc5c57c14ac2c4bed687799bd745e4953a59ee6028ba7d737.webp"
              ],
              "hashtags": [
                {
                  "name": "#checksc1",
                  "url": "http://web.dailyhunt.in/news/india/english/checksc1-topics-6ddc283ef8bc74b587775796204f1d74/all-subtopics-all"
                },
                {
                  "name": "#Page navigation",
                  "url": "http://web.dailyhunt.in/news/india/english/page+navigation-topics-27334adfbf73c1e367151f215ef05643/all-subtopics-all"
                }
              ],
              "createdTime": 1575440886000,
              "contentImage": "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_c42fcc3f3f012a3dc5c57c14ac2c4bed687799bd745e4953a59ee6028ba7d737.webp",
              "isApprovalPending": false,
              "state": "PUBLISHED",
              "categoryKey": "topstories",
              "genres": [
                "G1100"
              ],
              "sourceProvidedContentUrl": "http://www.deccanchronicle.com/nation/current-affairs/041219/sc-grants-bail-to-chidambaram-in-inx-media-case-registered-by-ed.html",
              "allowComments": true,
              "counts": {
                "SHARE": {
                  "value": "56",
                  "ts": 1576590672173
                },
                "COMMENTS": {
                  "value": "21",
                  "ts": 1576590672173
                },
                "LOVE": {
                  "value": "1",
                  "ts": 1576590672173
                },
                "VIEWS": {
                  "value": "4.2k",
                  "ts": 1576590672173
                },
                "TOTAL_LIKE": {
                  "value": "7",
                  "ts": 1576590672173
                },
                "WOW": {
                  "value": "1",
                  "ts": 1576590672173
                },
                "REPOST": {
                  "value": "12",
                  "ts": 1576590672173
                },
                "LIKE": {
                  "value": "5",
                  "ts": 1576590672173
                }
              },
              "languageKey": "en",
              "clickOutEnabled": false
            },
            {
              "id": "dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6",
              "content": " <p> By PTI </p> <p style=\"\" >LONDON: A UK court has ordered an Indian-origin drug dealer to pay back 50,000 pounds accrued from a plot to flood the streets with illegal drugs.</p> <p>Ravindra Modha had a confiscation order imposed on him under the UK&apos;s Proceeds of Crime Act (POCA) by Leicester Crown Court last Friday.</p> <p>The order allows police to take assets and cash from criminals who have benefitted financially from their criminality.</p> <p>Modha, 57, was sentenced to 16 years&apos; imprisonment after being found guilty of conspiracy to import cocaine in March last year.</p> <p>Paul Wenlock, from Leicestershire Police&apos;s Economic Crime Unit (ECU), said: &quot;Modha was involved in a plot to import drugs on a very large scale&quot; including cocaine with a street value of 9.6 million pounds and a kilogram of heroin with a street value of 316,000 pounds. </p> <p>&quot;He as able to make a vast sum of money from his crimes and this is something we will always look to rectify following the conviction of offenders.&quot; </p> <p style = \"margin-bottom: 0px;\">Four other men involved in the plot were also sentenced for their crimes following an investigation by the East Midlands Special Operations Unit last year.<nh-chunk-delimit /></p>",
              "publishTime": 1576517520000,
              "format": "HTML",
              "subFormat": "STORY",
              "langCode": "en",
              "type": "POST",
              "uiType": "HERO",
              "source": {
                "id": "dhb94a0041718e45b5b1eface00cdef265",
                "sourceName": "The New Indian Express",
                "displayName": "The New Indian Express",
                "type": "OGC",
                "entityType": "SOURCE",
                "counts": {
                  "STORY": {
                    "value": "257",
                    "ts": 1576590672164
                  }
                },
                "feedType": "SOURCE",
                "langCode": "en",
                "legacyKey": "newexpress"
              },
              "showPublishDate": false,
              "title": "UK court orders Indian-origin drug dealer to pay back 50,000 pounds",
              "titleEnglish": "UK court orders Indian-origin drug dealer to pay back 50,000 pounds",
              "shareUrl": "http://stage.dhunt.in/G3bl",
              "viewOrder": 0,
              "recommendationTs": 0,
              "moreContentLoadUrl": "http://qa-news.newshunt.com/api/v2/posts/article/content/dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6?useWidgetPosition=false&feedConfigKey=null",
              "deeplinkUrl": "https://m.dailyhunt.in/news/india/english/the+new+indian+express-epaper-dhb94a0041718e45b5b1eface00cdef265/uk+court+orders+indianorigin+drug+dealer+to+pay+back+50000+pounds-newsid-dhb94a0041718e45b5b1eface00cdef265_f7d8d1c95e8130cc94728cd33eeb74e6",
              "publisherStoryUrl": "https://www.newindianexpress.com/world/2019/dec/16/uk-court-orders-indian-origin-drug-dealer-to-pay-back-50000-pounds-2077034.html",
              "includeCollectionInSwipe": false,
              "sticky": false,
              "thumbnailUrls": [
                "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_aeaf102c466012033308e60763c29d3e568bfad69b92baaabaafe61ea2a612ed.webp"
              ],
              "hashtags": [
                {
                  "name": "#dhtopic3",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic3-topics-376d57f5f7aec4ccd75b18625fbbae64/all-subtopics-all"
                },
                {
                  "name": "#dhtopic4",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic4-topics-e278aa8fdf564140fd76faafb030efe0/all-subtopics-all"
                },
                {
                  "name": "#dhtopic5",
                  "url": "http://web.dailyhunt.in/news/india/english/dhtopic5-topics-77351711a8d45e30bfc2dbebe6c8f045/all-subtopics-all"
                }
              ],
              "createdTime": 1576564617000,
              "contentImage": "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_aeaf102c466012033308e60763c29d3e568bfad69b92baaabaafe61ea2a612ed.webp",
              "isApprovalPending": false,
              "state": "PUBLISHED",
              "categoryKey": "International",
              "genres": [
                "G1100"
              ],
              "sourceProvidedContentUrl": "https://www.newindianexpress.com/world/2019/dec/16/uk-court-orders-indian-origin-drug-dealer-to-pay-back-50000-pounds-2077034.html",
              "allowComments": true,
              "counts": {
                "VIEWS": {
                  "value": "6",
                  "ts": 1576590672164
                }
              },
              "languageKey": "en",
              "clickOutEnabled": false
            },
            {
              "id": "dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b",
              "content": " <p> <strong>New Delhi</strong>: The Supreme Court on Wednesday granted bail to former finance minister and Congress leader P Chidambaram in INX Media money laundering case, registered by the Enforcement Directorate (ED).</p> <p>Granting bail to former finance minister, the top court said P Chidambaram should not tamper with the evidence and not influence the witnesses. The court ordered that he should not give press interviews or make public statements in connection with this case.</p> <p style=\"border:0px;margin-top:0px;margin-bottom:0px;padding:0px;\" ></p> <p style = \"margin-bottom: 0px;\">The court directed P Chidambaram to furnish a bail bond of Rs 2 lakhs along with 2 sureties of the same amount and said he could not travel abroad without the Court's permission.<nh-chunk-delimit /></p>",
              "publishTime": 1575438300000,
              "format": "HTML",
              "subFormat": "STORY",
              "langCode": "en",
              "type": "POST",
              "uiType": "HERO",
              "source": {
                "id": "dh6dc1c103eefa45689fe7e3e137065edb",
                "sourceName": "Deccan Chronicle",
                "displayName": "Deccan Chronicle",
                "type": "OGC",
                "entityType": "SOURCE",
                "counts": {
                  "FOLLOW": {
                    "value": "123",
                    "ts": 1576590672173
                  },
                  "STORY": {
                    "value": "4.1k",
                    "ts": 1576590672173
                  }
                },
                "feedType": "SOURCE",
                "langCode": "en",
                "legacyKey": "deccanch"
              },
              "showPublishDate": false,
              "title": "After 105 days in custody, SC grants bail to Chidambaram in INX case",
              "titleEnglish": "After 105 days in custody, SC grants bail to Chidambaram in INX case",
              "shareUrl": "http://stage.dhunt.in/FxVi",
              "viewOrder": 0,
              "recommendationTs": 0,
              "moreContentLoadUrl": "http://qa-news.newshunt.com/api/v2/posts/article/content/dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b?useWidgetPosition=false&feedConfigKey=null",
              "deeplinkUrl": "https://m.dailyhunt.in/news/india/english/deccan+chronicle-epaper-dh6dc1c103eefa45689fe7e3e137065edb/after+105+days+in+custody+sc+grants+bail+to+chidambaram+in+inx+case-newsid-dh6dc1c103eefa45689fe7e3e137065edb_b96f8312343439fc97540a47fe63d71b",
              "publisherStoryUrl": "http://www.deccanchronicle.com/nation/current-affairs/041219/sc-grants-bail-to-chidambaram-in-inx-media-case-registered-by-ed.html",
              "includeCollectionInSwipe": false,
              "sticky": false,
              "thumbnailUrls": [
                "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_c42fcc3f3f012a3dc5c57c14ac2c4bed687799bd745e4953a59ee6028ba7d737.webp"
              ],
              "hashtags": [
                {
                  "name": "#checksc1",
                  "url": "http://web.dailyhunt.in/news/india/english/checksc1-topics-6ddc283ef8bc74b587775796204f1d74/all-subtopics-all"
                },
                {
                  "name": "#Page navigation",
                  "url": "http://web.dailyhunt.in/news/india/english/page+navigation-topics-27334adfbf73c1e367151f215ef05643/all-subtopics-all"
                }
              ],
              "createdTime": 1575440886000,
              "contentImage": "http://qa-img-cluster.dailyhunt.in/cmd/resize/#DH_EMB_IMG_REP#__DHQ_/fetchdata15_c42fcc3f3f012a3dc5c57c14ac2c4bed687799bd745e4953a59ee6028ba7d737.webp",
              "isApprovalPending": false,
              "state": "PUBLISHED",
              "categoryKey": "topstories",
              "genres": [
                "G1100"
              ],
              "sourceProvidedContentUrl": "http://www.deccanchronicle.com/nation/current-affairs/041219/sc-grants-bail-to-chidambaram-in-inx-media-case-registered-by-ed.html",
              "allowComments": true,
              "counts": {
                "SHARE": {
                  "value": "56",
                  "ts": 1576590672173
                },
                "COMMENTS": {
                  "value": "21",
                  "ts": 1576590672173
                },
                "LOVE": {
                  "value": "1",
                  "ts": 1576590672173
                },
                "VIEWS": {
                  "value": "4.2k",
                  "ts": 1576590672173
                },
                "TOTAL_LIKE": {
                  "value": "7",
                  "ts": 1576590672173
                },
                "WOW": {
                  "value": "1",
                  "ts": 1576590672173
                },
                "REPOST": {
                  "value": "12",
                  "ts": 1576590672173
                },
                "LIKE": {
                  "value": "5",
                  "ts": 1576590672173
                }
              },
              "languageKey": "en",
              "clickOutEnabled": false
            }
          ],
          "carouselProperties": {
            "animationType": "NONE",
            "circular": "true",
            "aspectRatio": "1",
            "nextPageUrl": "http://www.google.com",
            "actionButtonText": "Play all",
            "viewMoreText": "View more",
            "autoSwipeListIntervalInSeconds": "5",
            "autoSwipeDetailIntervalInSeconds": "5",
            "viewAllUrl": "https://qa-news.newshunt.com/api/v2/user/saved/posts/items?itemType=HTML"
          }
        },
        "clickOutEnabled": false
      }

    
""".trimIndent()

fun PostEntity.moreNews(vararg ids: String): PostEntity {
    return copy(
            moreStories = (moreStories?: listOf()) +
                    (ids.map { PostEntity(it) })
    )
}

fun PostEntity.associations(vararg ids: String): PostEntity {
    return copy(
            associations = (associations?: listOf()) +
                    (ids.map { PostEntity(it) })
    )
}

fun PostEntity.collectionitems(vararg ids: String): PostEntity {
    return copy(
            collectionAsset = (collectionAsset?: CollectionEntity()).copy(collectionItem = (collectionAsset?.collectionItem?: emptyList()) + (
                    ids.map { PostEntity(it) }
                    ))
    )
}

data class Quadraple<A, B, C, D>(
        val a: A,
        val b: B,
        val c: C,
        val d: D
)

fun <T> LiveData<T>.test() : IRxTestObserverFacade<T> {
    val obs = TestLiveDataObserver<T>()
    observeForever(obs)
    return obs
}

class TestLiveDataObserver<T>(private val rxobs : RxTestObserverFacade<T> =  RxTestObserverFacade<T>()) : Observer<T>, IRxTestObserverFacade<T> by rxobs {
    override fun onChanged(t: T) {
        rxobs.add(t)
    }
}

/**
 * This is for live data assertions
 * Keeping the same API as Rx TestObserver
 *
 */
interface IRxTestObserverFacade<T> {
    fun assertValueCount(size: Int):IRxTestObserverFacade<T>
    fun values(): List<T>
    fun assertValues(vararg t: T)
    fun assertValueAt(index: Int, f: (T) -> Boolean)
    fun assertValue(t: T)
    fun assertEmpty()
    fun assertResult(vararg emptyList: T)
}

class RxTestObserverFacade<T>() : IRxTestObserverFacade<T> {
    override fun assertResult(vararg emptyList: T) {
        assertValues(*emptyList)
    }

    val list = arrayListOf<T>()

    override fun assertValueAt(index: Int, f: (T) -> Boolean) {
        assertThat(f(list[index])).isTrue()
    }

    override fun assertValues(vararg t: T) {
        assertThat(list).hasSize(t.size)
        for (i in t.indices) {
            assertValueAt(i) {
                t[i] == it
            }
        }
    }

    override fun assertValueCount(size: Int): RxTestObserverFacade<T> {
        assertThat(list).hasSize(size)
        return this
    }

    override fun values()  = list

    override fun assertValue(t: T) {
        assertThat(list).containsExactly(t)
    }

    override fun assertEmpty() {
        assertThat(list).isEmpty()
    }

    fun add(t: T) = list.add(t)
}

val dummy = S_PageEntity(PageEntity(
        id = "",
        name = "",
        displayName = "",
        entityType = "",
        subType = "",
        entityLayout = "",
        contentUrl = TEST_CONTENT_URL,
        entityInfoUrl = "",
        handle = "",
        deeplinkUrl = "",
        moreContentLoadUrl = "",
        entityImageUrl = "",
        shareParams = ShareParam(),
        shareUrl = "",
        nameEnglish = "",
        contentRequestMethod = "GET",
        enableWebHistory = true,
        viewOrder = 1,
        isFollowable = true,
        appIndexDescription = ""), section = PageSection.NEWS.section)

val dummySourceEntity = S_PageEntity(PageEntity(
        id = "c2",
        name = "",
        displayName = "",
        entityType = "SOURCECAT",
        subType = "",
        entityLayout = "",
        contentUrl = TEST_CONTENT_URL,
        entityInfoUrl = "",
        handle = "",
        deeplinkUrl = "",
        moreContentLoadUrl = "",
        entityImageUrl = "",
        shareParams = ShareParam(),
        shareUrl = "",
        nameEnglish = "",
        contentRequestMethod = "GET",
        enableWebHistory = true,
        viewOrder = 1,
        isFollowable = true,
        appIndexDescription = ""), section = PageSection.NEWS.section)


val correspId : Correspondence<Any?, String> = Correspondence.transforming({
    if(it is CreatePostEntity) it.cpId.toString()
    if(it is CreatePost) it.cpEntity.cpId.toString()
    else if (it is CommonAsset) it.i_id()
    else it.toString()
}, "has matching id")
