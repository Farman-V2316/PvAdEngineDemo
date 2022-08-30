/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.view.entity

import com.newshunt.dataentity.Either
import com.newshunt.dataentity.news.model.entity.MenuL1Meta
import com.newshunt.dataentity.news.model.entity.MenuL2Meta
import com.newshunt.dataentity.news.model.entity.server.asset.L1L2Mapping
import java.io.Serializable

data class MenuOpts(
                    val title: String,
                    val subTitle: String,
                    val list: Either<List<MenuL1Meta>, List<MenuL2Meta>>? = null,
                    val selectedList: Either<List<MenuL1Meta>, List<MenuL2Meta>>? = null,
// --- for presenter, internal use ---
                    val l1Opts: List<L1L2Mapping>? = null,// contains keys for lookup
                    val l1: List<MenuL1Meta>? = null, // looked-up & displayable meta
                    val l1Selected: MenuL1Meta? = null,
                    val l2Opts: List<String>? = null, // contains keys for lookup
                    val l2: List<MenuL2Meta>? = null, // looked-up & displayable meta
                    val l2Selected: List<MenuL2Meta>? = null,
                    val cardIcon: String? = null,
                    val sendButtonText: String? = null,
                    val cardNightIcon: String? = null) : Serializable