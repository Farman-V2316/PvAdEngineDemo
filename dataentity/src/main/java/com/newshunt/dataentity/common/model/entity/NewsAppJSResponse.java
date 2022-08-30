/*
 * Created by Rahul Ravindran at 25/9/19 11:32 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;


import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJS;

import java.io.Serializable;
import java.util.List;

public class NewsAppJSResponse implements Serializable {

    private static final long serialVersionUID = -2772558837746506026L;
    private String version;

    private int count;

    private List<NewsAppJS> scripts;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<NewsAppJS> getScripts() {
        return scripts;
    }

    public void setScripts(List<NewsAppJS> scripts) {
        this.scripts = scripts;
    }
}
