/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.interceptor

import com.newshunt.dhutil.helper.launch.AppSectionLauncherHelper
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchRule
import com.newshunt.dataentity.dhutil.model.entity.launch.TIME_00_00_HOURS_MS
import com.newshunt.dataentity.dhutil.model.entity.launch.TIME_23_59_HOURS_MS
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Test cases to test the class AppSectionLauncherHelper
 * <p>
 * Created by srikanth.ramaswamy on 02/06/2019.
 */
private val random = Random()

class AppSectionLauncherHelperTest {
    @Test
    fun testRuleWithoutTimeWindow() {
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(AppLaunchRule()))
    }

    @Test
    fun testRuleWithEmptyTimeWindow() {
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(makeRuleWithTimeWindow()))
    }

    @Test
    fun testRuleWithUninitializedTimeWindow() {
        val rule = makeRuleWithTimeWindow()
        rule.timeWindows.add(TimeWindow())
        Assert.assertFalse(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testNoMatchingWindows() {
        val rule = makeRuleWithTimeWindow()
        //Random past time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(-makeRandomTimestamp())))
        //Random future time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(makeRandomTimestamp()), makeTimeinMillis(makeRandomTimestamp())))
        Assert.assertFalse(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun test1MatchingWindow() {
        val rule = makeRuleWithTimeWindow()
        //Random past time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(-makeRandomTimestamp())))
        //Random past start time and Random future end time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(makeRandomTimestamp())))
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testMultipleMatchingWindows() {
        val rule = makeRuleWithTimeWindow()
        //Random past time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(-makeRandomTimestamp())))
        //Random past start time and Random future end time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(makeRandomTimestamp())))
        //Random past start time and Random future end time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(makeRandomTimestamp())))
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testMatchingStartNowWindow() {
        val rule = makeRuleWithTimeWindow()
        //Starts now and ends at a random future time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(0), makeTimeinMillis(makeRandomTimestamp())))
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testNoMatchingEndNowWindow() {
        val rule = makeRuleWithTimeWindow()
        //Random past start time and end time NOW
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(-makeRandomTimestamp()), makeTimeinMillis(0)))
        Assert.assertFalse(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testMatchingEntireDayWindow() {
        val rule = makeRuleWithTimeWindow()
        rule.timeWindows.add(TimeWindow(TIME_00_00_HOURS_MS, TIME_23_59_HOURS_MS))
        Assert.assertTrue(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    @Test
    fun testNoMatchingStartTimeGreaterThanEndTime() {
        val rule = makeRuleWithTimeWindow()
        //Random future start time and past end time
        rule.timeWindows.add(TimeWindow(makeTimeinMillis(makeRandomTimestamp()), makeTimeinMillis(-makeRandomTimestamp())))
        Assert.assertFalse(AppSectionLauncherHelper.ruleFitsInTimeWindow(rule))
    }

    private fun makeRuleWithTimeWindow(): AppLaunchRule {
        return AppLaunchRule().apply {
            this.timeWindows = ArrayList<TimeWindow>()
        }
    }

    /**
     * Make a new timestamp in milliseconds by adding (or subtracting) a delta to current time.
     */
    private fun makeTimeinMillis(deltaMS: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentTimeInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY).toLong()) +
                TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE).toLong()) +
                TimeUnit.SECONDS.toMillis(calendar.get(Calendar.SECOND).toLong()) +
                calendar.get(Calendar.MILLISECOND).toLong()
        return currentTimeInMillis + deltaMS
    }

    private fun makeRandomTimestamp(): Long {
        //43200000L is 12 hours in milliseconds
        return Math.abs(random.nextLong() % 43200000L)
    }
}