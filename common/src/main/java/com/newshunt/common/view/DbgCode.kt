/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view

import com.google.firebase.inject.Provider
import com.google.gson.JsonSyntaxException
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.APIException
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Represents an dev-error-code appended to error-messages shown to user.
 *
 * @author satosh.dhanyamraju
 */
sealed class DbgCode(message: String) : Throwable(message), Provider<String> {
    val id = uid.incrementAndGet() // for debugging and logging purpose
    override fun get(): String = message ?: "ZU"

    open class DbgHttpCode(code: Int) : DbgCode(code.toString()) {
        val map = mapOf('2' to 'B', '3' to 'C', '4' to 'D', '5' to 'E')
        override fun get(): String {
            val code = message?.let {
                val c = map[it.first()]
                c?.run {
                    "B$c${it.substring(1)}"
                } ?: "BU00"
            }
            return code ?: "BU00"
        }
    }
    class DbgApiInvalidStatusCode() : DbgCode("BU01")

    class DbgNotFoundInCache : DbgCode("CH${nwBit()}")

    class DbgResponseErrorNull : DbgCode("CE${nwBit()}")

    class DbgVersionedApiCorrupt : DbgCode("DV${nwBit()}")

    class DbgOnBoardingRequest : DbgCode("DO${nwBit()}")

    class DbgErrorConnectivity : DbgCode("AC${nwBit()}")

    class DbgNoItemsInList : DbgCode("DL${nwBit()}")

    class DbgSocketTimeoutCode : DbgCode("AS${nwBit()}")

    class DbgUnknownHostCode : DbgCode("AD${nwBit()}")

    class DbgNoConnectivityCode : DbgCode("AN${nwBit()}")

    class DbgJsonSyntaxCode : DbgCode("CJ${nwBit()}")

    class DbgUnexpectedCode(message: String) : DbgCode(message) {
        // beth success and error are null.
        // see ERROR_UNEXPECTED_INT usages

        override fun get(): String {
            return "ZU"
        }
    }

    //response errors used in nhbrowser
    class DbgBroswerServer : DbgCode("RS${nwBit()}")

    class DbgBroswerGeneric : DbgCode("RG${nwBit()}")



    companion object {
        /**
         * network bit.
         * @return 1- if connected, 0 otherwise.
         */
        private fun nwBit(): Int =
                if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) 1 else 0

        private val uid = AtomicInteger(0)
    }

}


fun BaseError?.dbgCode(): DbgCode {
    return when (this?.originalError) {
        is DbgCode -> this.originalError as DbgCode
        is SocketTimeoutException -> DbgCode.DbgSocketTimeoutCode()
        is UnknownHostException -> DbgCode.DbgUnknownHostCode()
        is NoConnectivityException -> DbgCode.DbgNoConnectivityCode()
        is JsonSyntaxException -> DbgCode.DbgJsonSyntaxCode()
        is HttpException -> DbgCode.DbgHttpCode((this.originalError as
                HttpException).code())
        is APIException -> DbgCode.DbgHttpCode((this.originalError as APIException).error
                .statusAsInt)
        else -> DbgCode.DbgUnexpectedCode("BaseError.dbgCode else-> ${this?.originalError?.toString()}")
    }
}

fun BaseError?.isNoContentError() = this?.dbgCode()?.get() == "BB04"
fun BaseError?.isNotFoundInCacheError() = this?.dbgCode()?.get()?.startsWith("CH") == true
