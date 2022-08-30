/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity

/**
 * Do not use indices from {@link AdDisplayType}
 */
enum class DetailCardType(val index: Int, val adGroup: Int = index) {

    SOURCE(1),
    TITLE(2),
    TIME(3),
    IMAGE(4),
    GALLERY_2(5),
    GALLERY_3(6),
    GALLERY_4(7),
    GALLERY_5(8),
    CHUNK1(9),
    CHUNK2(10),
    LOCATION(11),
    HASHTAGS(12),
    SUGGESTED_FOLLOW(13),
    LIKES_LIST(14),
    DISCUSSION(15),
    OTHER_PERSPECTIVES(16),
    SUPPLEMENTARY_RELATED(17),
    POLL(18),
    POLL_RESULT(19),
    VIRAL(20),
    OGCARD(21),
    DISCUSSION_HEADER(22),
    REPOST_NORMAL(23),
    REPOST_POLL(24),
    REPOST_BIG_IMAGE(25),
    REPOST(26),
    DISCLAIMER(27),

    RICH_GALLERY(29),
    SEE_IN_VIDEO(30),
    //Ad Stubs
    MASTHEAD(31),
    STORYPAGE(32),
    SUPPLEMENT(33),
    READMORE(34),
    DISCUSSION_LOADER(35),
    SHIMMER(36),
    SEEPOST(37),
    MAIN_COMMENT(38),
    REPOST_VIRAL(39),
    REPOST_OG(40),
    AD_SUPPLEMENT_HEADER(41),
    DETAIL_LIKES(42),
    GUEST_DETAIL_FOOTER(43),
    IMAGE_DYNAMIC(44, IMAGE.index),
    SECOND_CHUNK_LOADING(45),
    DISCUSSION_NS(46), // Similar to Discussion, but doesn't auto scroll.
    DISCUSSION_SHOW_ALL(47),
    SPACER(48),
    SOURCE_TIME(49)
}