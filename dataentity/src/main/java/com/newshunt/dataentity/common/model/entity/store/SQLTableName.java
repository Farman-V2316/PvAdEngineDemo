/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use @SQLTableName if class name and table name varies.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SQLTableName {
  String tableName() default "";
}
