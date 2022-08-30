package com.newshunt.common.model.interceptor

import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.info.DebugHeaderProvider
import com.newshunt.common.helper.info.MigrationStatusProvider
import com.newshunt.common.helper.info.RegistrationHeaderProvider
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response


/**
 * @author satosh.dhanyamraju
 */
class HeaderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader(DEBUG_HEADER, DebugHeaderProvider.getDebugHeader())
        val clientId: String? = ClientInfoHelper.getClientId()
        clientId?.let {
            builder.addHeader(UNIQUE_ID_HEADER, generateRequestId(clientId))
        }
        //Add Migration header if applicable
        MigrationStatusProvider.getMigrationStateHeaderIfApplicable(chain.request().url())?.apply {
            builder.addHeader(MIGRATION_STATUS_HEADER, this)
        }
        val connectionType = NetworkSDKUtils.getLastKnownConnectionType()
        if (connectionType?.connectionType?.isNotEmpty() == true) {
            builder.addHeader(USER_CONNECTION, connectionType.connectionType)
        }
        builder.addHeader(USER_CONNECTION_QUALITY, UserConnectionHolder.userConnectionQuality)

        //Add registration header if applicable
        RegistrationHeaderProvider.getRegistrationHeader()?.apply {
            builder.addHeader(REG_INFO_HEADER, this)
        }
        builder.addHeader(APP_ID, Constants.APP_ID)
        val request = builder.build()
        val response  = chain.proceed(request)

        //Attempt to save the migration header if this API gave one.
        MigrationStatusProvider.readMigrationStatusHeaderFromResponse(response)
        return response
    }

    companion object {

        private const val DEBUG_HEADER = "dh-debug-info"
        private const val REG_INFO_HEADER = "reg-info"
        private const val APP_ID = "App-Id"
        const val MIGRATION_STATUS_HEADER = "user_migration_state"
        private const val USER_CONNECTION = "uc"
        private const val USER_CONNECTION_QUALITY = "ucq"

        @JvmField
        val UNIQUE_ID_HEADER = "X-Request-Id"

        @JvmStatic
        @JvmOverloads
        fun generateRequestId(clientId: String = ClientInfoHelper.getClientId()) =
                clientId + Constants.UNDERSCORE_CHARACTER + System.currentTimeMillis()

    }
}
