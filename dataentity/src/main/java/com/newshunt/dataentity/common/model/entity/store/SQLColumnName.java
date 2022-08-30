/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to be used when column name in DB is not following "_". For
 * example, instead of user_name if column name is userName use this
 * annotation.
 *
 * @author shreyas
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLColumnName {
  String value() default "";
}
