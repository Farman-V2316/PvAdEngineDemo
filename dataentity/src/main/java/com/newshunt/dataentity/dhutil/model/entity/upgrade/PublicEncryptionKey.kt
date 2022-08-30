/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.upgrade

/**
 * Data class for Asymmetric Encryption Key and it's version to be passed with handshake.
 *
 * Created by karthik.r on 2020-02-17.
 */
data class PublicEncryptionKey(val key: String, val version: String)