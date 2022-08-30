/*
 * Created by Rahul Ravindran at 25/9/19 11:56 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model;

/**
 * Status for tab sync
 *
 * @author shrikant.agrawal on 6/8/2016.
 */

public enum NewsPageMode {

    ADDED("added"),
    MODIFIED("modified"),
    DELETED("deleted");

    private String mode;

    NewsPageMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
