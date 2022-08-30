package com.newshunt.dataentity.notification

class AdjunctLangNavModel:BaseModel() {
}

class AdjunctLangStickyNavModel:BaseModel() {
    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.ADJUNCT_MESSAGE
    }
}