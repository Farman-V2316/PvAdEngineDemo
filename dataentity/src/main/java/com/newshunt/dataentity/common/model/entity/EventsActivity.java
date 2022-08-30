/*
 * Created by Rahul Ravindran at 26/9/19 12:07 AM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author shrikant.agrawal
 */
public class EventsActivity implements Serializable {

    private String type;

    private Map<String, String> attributes;

    private EventsAction action;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public EventsAction getAction() {
        return action;
    }

    public void setAction(EventsAction action) {
        this.action = action;
    }
}
