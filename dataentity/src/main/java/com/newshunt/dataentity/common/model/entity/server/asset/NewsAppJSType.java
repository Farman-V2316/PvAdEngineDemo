/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.server.asset;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

/**
 * String Definitions for Java scripts used for various places in our app, for eg News Detail
 *
 * @author santhosh.kc
 */
public class NewsAppJSType implements Serializable {

    public static final String NEWS_DETAIL_CHUNK_1 = "NEWS_DETAIL_CHUNK_1";
    public static final String NEWS_DETAIL_CHUNK_2 = "NEWS_DETAIL_CHUNK_2";
    private static final long serialVersionUID = -4093711832465614296L;
    private String type;

    @JSType
    public String getType() {
        return type;
    }

    public void setType(@JSType String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NewsAppJSType && CommonUtils.equals(((NewsAppJSType) o).type, type);
    }

    @Retention(RetentionPolicy.SOURCE)

    @StringDef({
            NEWS_DETAIL_CHUNK_1, NEWS_DETAIL_CHUNK_2
    })
    @interface JSType {
    }

}
