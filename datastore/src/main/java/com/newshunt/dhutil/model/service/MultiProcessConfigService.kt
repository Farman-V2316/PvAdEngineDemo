/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.service

/**
 * Created by karthik.r on 11/12/18.
 */
interface MultiProcessConfigService {
    fun isMultiProcessModeEnabled(): Boolean
    fun getKillProcessBGDuration(): Int
    fun getKillProcessFGDuration(): Int
}