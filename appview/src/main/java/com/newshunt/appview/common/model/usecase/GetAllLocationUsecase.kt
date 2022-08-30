/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.model.usecase

/*
 * @author priya.gupta
*/

import com.newshunt.appview.common.model.repo.LocationEntityRepo
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable


class GetAllLocationUsecase : Usecase<String, List<Locations>> {
    override fun invoke(p1: String): Observable<List<Locations>> {
        return LocationEntityRepo(p1).getLocations()
    }
}