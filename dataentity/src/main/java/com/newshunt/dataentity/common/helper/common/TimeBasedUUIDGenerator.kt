package com.newshunt.dataentity.common.helper.common

import java.util.UUID

object TimeBasedUUIDGenerator {

	private var lastTime = 0L

	private val clockSeqAndNode: Long
		get() {
			var clockSeqAndNode = UUID.randomUUID().leastSignificantBits
			clockSeqAndNode = clockSeqAndNode or ((Math.random() * 16383.0).toLong() shl 48)
			return clockSeqAndNode
		}

	@Synchronized
	private fun newTime(): Long {
		return newTime(System.currentTimeMillis())
	}

	@Synchronized
	private fun newTime(currentTimeInMillis: Long): Long {
		var timeMillis = currentTimeInMillis * 10000L + 122192928000000000L
		if (timeMillis > lastTime) {
			lastTime = timeMillis
		} else {
			timeMillis = ++lastTime
		}

		var time = timeMillis shl 32
		time = time or (timeMillis and 281470681743360L shr 16)
		time = time or (4096L or (timeMillis shr 48 and 4095L))
		return time
	}

	fun createUUID(currentTimeInMillis: Long): UUID {
		return UUID(newTime(currentTimeInMillis), clockSeqAndNode)
	}
}