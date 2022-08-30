/**
 *  Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common

/**
 * This is a helper class to store served but not played videos for local zone and normal videos
 * @author ajay.gu
 */

object ServedButNotPlayedHelper {
    private val TAG = "ServedButNotPlayedHelper"
    private var localZoneServedList = HashSet<String>()
    private var videosServedList = HashSet<String>()
    private var localZonePlayedList = HashSet<String>()
    private var videosPlayedList = HashSet<String>()

    fun addTOlocalZoneServedList(value: String) {
        localZoneServedList.add(value)
    }

    fun removeFromlocalZoneServedList(value: String) {
        localZonePlayedList.add(value)
    }

    fun addTORelatedServedList(value: String) {
        videosServedList.add(value)
    }

    fun removeFromRelatedServedList(value: String) {
        videosPlayedList.add(value)
    }

    // Returns served but not played videos from Local zone
    fun getlocalZoneServedList(): HashSet<String> {
        val servedButNotPlayedVideos = HashSet<String>(localZoneServedList)
        servedButNotPlayedVideos.removeAll(localZonePlayedList)
        return servedButNotPlayedVideos
    }

    // Returns served but not played videos
    fun getRelatedVideoSBNP(): HashSet<String> {
        val servedButNotPlayed = HashSet<String>(videosServedList)
        servedButNotPlayed.removeAll(videosPlayedList)
        return  servedButNotPlayed
    }
}