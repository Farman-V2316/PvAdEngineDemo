package com.newshunt.notification.model.entity.server

import com.newshunt.dataentity.notification.BaseInfo
import java.io.Serializable

data class AdjunctLangBaseInfo(val adjText:String,
                               val defaultText:String,
                               val adjunctNotiUrl:String,
                               val adjLang:String,
                               val tickDeeplinkUrl:String,
                               val crossDeeplinkUrl:String,
                               val notificationClickDeeplinkUrl:String) : BaseInfo(),Serializable