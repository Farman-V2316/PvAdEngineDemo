/*
 * Created by Rahul Ravindran at 26/9/19 12:07 AM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

/**
 * @author shashikiran.nr
 */
public enum NewsArticleState {
    NONE("NONE"),
    DOWNLOADING("DOWNLOADING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");
    private final String name;

    NewsArticleState(String name) {
        this.name = name;
    }

    public static NewsArticleState fromName(String name) {
        for (NewsArticleState downloadState : NewsArticleState.values()) {
            if (downloadState.name.equalsIgnoreCase(name)) {
                return downloadState;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

}
