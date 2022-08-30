package com.newshunt.dataentity.dhutil.model.entity.adupgrade

import java.io.Serializable

data class ReportAdsMenuEntity(val url: String,
                               val options: List<ReportAdsMenuOptionEntity>): Serializable


data class ReportAdsMenuOptionEntity(val id: String,
                                     val labels: HashMap<String, String>,
                                     val showWebForm : Boolean = false,
                                     val iconUrl: String,
                                     val dataToSend: String,
                                     val thankYouMessage: HashMap<String, String>,
                                     val collapseOnSubmit: Boolean = false): Serializable


data class ReportAdsMenuFeedBackEntity(val feedbackUrl : String): Serializable

data class ReportAdsMenuPostBodyEntity(val clientId: String, val data: String): Serializable