/*
 * Created by Rahul Ravindran at 25/9/19 11:56 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model;

/**
 * Sync status for tabs
 *
 * @author shrikant.agrawal on 6/8/2016.
 */

public enum SyncStatus {
    SYNCED(0),
    NOT_SYNCED(1),
    IN_PROGRESS(2);

    private int status;

    SyncStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
