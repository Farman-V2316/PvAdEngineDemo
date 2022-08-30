/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model

/**
 * Enumeration to tell why an ad got closed
 *
 * @author raunak.yadav
 */
enum class AdInteraction {
    USER_CLOSE,
    AUTO_TIMER,
    USER_CLICK,
    USER_BACK_NAVIGATION,
    USER_SCROLL,
    USER_NAV,
    USER_FEEDBACK_CLICK
}