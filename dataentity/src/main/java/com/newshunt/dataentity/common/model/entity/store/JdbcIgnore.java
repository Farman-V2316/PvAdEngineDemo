/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Any getter / setter method annotated with @JdbcIgnore will not be
 * used for querying
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcIgnore {

}
