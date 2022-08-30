package com.newshunt.dataentity.common.model.entity.server

data class AstroSubscriptionRequest(val gender: String,
                                    val dob: String,
                                    val entityId: String,
                                    val entityType: String = "HASHTAG")
