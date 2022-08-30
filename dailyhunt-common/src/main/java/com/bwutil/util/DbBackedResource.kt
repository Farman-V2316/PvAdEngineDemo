/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.util

/**
 * This class is used to hold a Resource that is sourced from database, but can be updated in-memory.
 * Ttl is used to control how frequently it is refreshed from DB.
 * @author satosh.dhanyamraju
 */
class DbBackedResource<T : Any>(
        private val def: T,
        private val ttl: Long,
        private val execHelper: ExecHelper = ExecHelper(),
        private val timeF: () -> Long = {System.currentTimeMillis()},
        private val fetchFromDB: () -> T) {
    private lateinit var t: T
    private var lastUpdatedFromDB: Long = -1
    private val expired: Boolean
        get() = (lastUpdatedFromDB == -1L || (timeF() - lastUpdatedFromDB > ttl))


    init {
        refresh(true)
    }

    fun getVal(): T {
        refresh(false)
        return if (::t.isInitialized) t else def
    }

    fun update(v: T) {
        execHelper.runUI {
            t = v
            refresh(false)
        }
    }

    fun compute(f: (T) -> T) {
        update(f(getVal()))
    }

    fun refresh(force: Boolean = true) {
        if(force || expired) {
            execHelper.runIOThenUi(fetchFromDB) {
                t = it
                lastUpdatedFromDB = timeF()
            }
        }
    }
}