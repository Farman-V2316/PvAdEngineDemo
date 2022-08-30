/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.nhcommand;

import android.app.Activity;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;


/**
 * Interface for any dhcommand handler
 * Created by santosh.kumar on 9/23/2015.
 */
public interface DHCommandHandler {
    boolean handle(String json, Activity parentActivity, PageReferrer pageReferrer);
}
