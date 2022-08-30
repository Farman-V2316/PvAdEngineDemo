package com.newshunt.dataentity.notification

data class ContactsRecoNavModel(val deepLinkUrl: String? = null) : BaseModel() {

	override fun getBaseModelType(): BaseModelType {
		return BaseModelType.CONTACTS_RECO_MODEL
	}
}