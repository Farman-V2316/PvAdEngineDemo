/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.server.asset;

import java.io.Serializable;

/**
 * @author santhosh.kc
 */
public class NewsAppJS implements Serializable {

    private static final long serialVersionUID = -7502193021297588013L;
    private String jsScript;

    private String css;

    private NewsAppJSType newsAppJSType;


    public String getJsScript() {
        return jsScript;
    }

    public void setJsScript(String jsScript) {
        this.jsScript = jsScript;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public NewsAppJSType getNewsAppJSType() {
        return newsAppJSType;
    }

    public void setNewsAppJSType(NewsAppJSType newsAppJSType) {
        this.newsAppJSType = newsAppJSType;
    }
}
