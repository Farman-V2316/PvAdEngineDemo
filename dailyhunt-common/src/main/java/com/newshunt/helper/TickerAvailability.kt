/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.helper

/**
 * Defines different states of ticker availability.
 */
enum class TickerAvailability {
    /**
     * case where we want to show ticker, but the state is unknown
     */
    UNKNOWN,
    /**
     * case where we want to show ticker and ticker is available to show
     */
    AVAILABLE,
    /**
     * case where we want to show ticker, but ticker is not available
     */
    UNAVAILABLE,
    /**
     * case where do not want to show ticker, eg, PageType.GALLERY does not require to show ticker
     */
    NONE
}