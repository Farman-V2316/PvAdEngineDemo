/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.relatedvideo

import com.newshunt.appview.common.group.model.usecase.InsertIntoGroupDaoUsecase
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.di.scopes.PerFragment
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import dagger.Module
import dagger.Provides

@Module
class RelatedVideoModule(private val feedPage: FeedPage,
                         private val socialDB: SocialDB = SocialDB.instance()) {
    @Provides
    @PerFragment
    fun feedPage()  = feedPage

    @Provides
    @PerFragment
    fun insUc(uc: InsertIntoGroupDaoUsecase): MediatorUsecase<List<GeneralFeed>, List<String>> = uc.toMediator2()
}