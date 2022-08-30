package com.newshunt.dataentity.notification

import android.net.Uri
import java.io.Serializable

/**
 * Created by kajal.kumari on 21/10/21.
 */
class AdjunctLangModel(private val deeplink: String) : BaseModel(), Serializable {
    val uri = kotlin.runCatching { Uri.parse(deeplink) }.getOrNull()
    val addLang = uri?.getQueryParameter("addLang")
    val removeLang = uri?.getQueryParameter("removeLang")
    val langFlow = uri?.getQueryParameter("langFlow")

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.ADJUNCT_LANG_MODEL;
    }

    companion object {
        @JvmStatic private val serialVersionUID = 1L
    }
}