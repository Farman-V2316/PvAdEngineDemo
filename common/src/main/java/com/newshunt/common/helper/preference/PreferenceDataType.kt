/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference

/**
 * @author anshul.jain
 * All the data types which are supported by Android Preferences.
 */

enum class PreferenceDataType {

    STRING,
    INTEGER,
    LONG,
    BOOLEAN,
    SET,
    FLOAT;

    companion object {
        @JvmStatic
        fun getDataType(obj: Any): PreferenceDataType? {
            if (obj is String) {
                return STRING
            } else if (obj is Int) {
                return INTEGER
            } else if (obj is Long) {
                return LONG
            } else if (obj is Boolean) {
                return BOOLEAN
            } else if (obj is Float) {
                return FLOAT
            } else if (obj is Set<*>) {
                return SET
            }
            return null
        }
    }

}
