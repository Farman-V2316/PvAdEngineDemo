/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * @author Amitkumar
 */
class TemporaryChannelManager {
    val channels: MutableSet<TemporaryEntity> = mutableSetOf()
    val groups: MutableSet<TemporaryEntity> = mutableSetOf()

    fun load() {
        val channelSet = PreferenceManager
                .getPreference<Set<String>>(AppStatePreference
                        .TEMPORARY_NOTIFICATION_CHANNELS, emptySet())
        channels.addAll(channelSet.map { TemporaryEntity.createInstance(it) })

        val groupSet = PreferenceManager
                .getPreference<Set<String>>(AppStatePreference
                        .TEMPORARY_NOTIFICATION_GROUPS, emptySet())
        groups.addAll(groupSet.map { TemporaryEntity.createInstance(it) })
    }

    fun isTemporaryChannel(id: String): Boolean {
        val c = channels.find { it.id == id } ?: return false
        if (c.isExpired()) {
            channels.remove(c)
            return false
        }
        return true
    }

    fun isTemporaryGroup(id: String): Boolean {
        val g = groups.find { it.id == id } ?: return false
        if (g.isExpired()) {
            groups.remove(g)
            return false
        }
        return true
    }

    fun save() {
        val channelSet = channels.filter { !it.isExpired() }.map { it.toString() }.toSet()
        val groupSet = groups.filter { !it.isExpired() }.map { it.toString() }.toSet()

        PreferenceManager
                .savePreference(AppStatePreference
                        .TEMPORARY_NOTIFICATION_CHANNELS, channelSet)

        PreferenceManager
                .savePreference(AppStatePreference
                        .TEMPORARY_NOTIFICATION_GROUPS, groupSet)
    }

    companion object {
        @JvmStatic
        fun addChannel(id: String) {
            val channelSet = PreferenceManager
                    .getPreference<MutableSet<String>>(AppStatePreference
                            .TEMPORARY_NOTIFICATION_CHANNELS, mutableSetOf())
            channelSet.add(TemporaryEntity.createInstanceFromId(id).toString())
            PreferenceManager
                    .savePreference(AppStatePreference
                            .TEMPORARY_NOTIFICATION_CHANNELS, channelSet)
        }

        @JvmStatic
        fun addGroup(id: String) {
            val groupSet = PreferenceManager
                    .getPreference<MutableSet<String>>(AppStatePreference
                            .TEMPORARY_NOTIFICATION_GROUPS, mutableSetOf())
            groupSet.add(TemporaryEntity.createInstanceFromId(id).toString())
            PreferenceManager
                    .savePreference(AppStatePreference
                            .TEMPORARY_NOTIFICATION_GROUPS, groupSet)
        }
    }
}

class TemporaryEntity private constructor(val id: String, private val expireOn: Long) {
    override fun toString(): String {
        return "$id~$expireOn"
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expireOn
    }

    companion object {
        private const val TTL = 3600000
        @JvmStatic
        fun createInstance(value: String): TemporaryEntity {
            val contents = value.split('~')
            return TemporaryEntity(contents[0], contents[1].toLong())
        }

        @JvmStatic
        fun createInstanceFromId(id: String): TemporaryEntity {
            val expireTime = System.currentTimeMillis() + TTL
            return TemporaryEntity(id, expireTime)
        }
    }
}