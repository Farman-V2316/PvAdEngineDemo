package com.newshunt.common.common

import com.newshunt.dataentity.common.model.entity.BaseDataResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse

/**
 * Collection Response for caraousal contents
 *
 * @author karthik.r
 */
class CollectionResponse : BaseDataResponse() {

    private var data: CollectionData<Any>? = null

    fun getData(): CollectionData<Any>? {
        return data
    }

    fun setData(data: CollectionData<Any>) {
        this.data = data
    }

}

typealias CollectionData<T> =  MultiValueResponse<T>