package com.newshunt.dataentity.common.model

import java.io.Serializable

/**
 * Handles about us response formate.
 *
 * @author Mukesh Yadav
 */
data class AboutUsResponse(val version: String, val content: String, val contentBaseUrl: String) : Serializable