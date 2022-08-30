/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.sso.model.helper.interceptor

import com.google.gson.JsonObject
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants.HTTP_401_ERROR_AUTH_03
import com.newshunt.common.helper.common.Constants.HTTP_401_ERROR_STATUS
import com.newshunt.common.helper.common.Constants.HTTP_401_SPECIFIC_ERROR_CODE
import com.newshunt.dataentity.common.model.entity.APIException
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.sso.SSO
import com.newshunt.sso.analytics.SSOAnalyticsUtility
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.LoginResponse
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.sso.model.entity.SSOResult
import com.squareup.otto.Subscribe
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * @author santhosh.kc
 */

private const val LOG_TAG = "HTTP_401_Interceptor"
private const val DEFAULT_LOGIN_TIME_OUT_IN_SECONDS = 10L
private const val DEFAULT_RETRY_COUNT = 1

class HTTP401Interceptor(private val retriesCount: Int = DEFAULT_RETRY_COUNT,
                         private val loginFunction: (socialLoginMandatory : Boolean) -> LoginResponse? =
                                 getDefaultLoginFunction()) : Interceptor {

    private var attemptCount = 0
    private var firedSignOutEvent: Boolean = false
    override fun intercept(chain: Interceptor.Chain): Response? {

        var response: Response?
        do {
            val request = chain.request()
            response = chain.proceed(request) //1st request

            if (response.code() != Constants.HTTP_UNAUTHORISED) {
                //not a 401 response, then dont intercept and propagate the response to upper layers
                return response
            }

            val socialLoginMandatory = try {
                isSocialLoginMandatory(response)
            } catch (ex: APIException) {
                return response
            }

            val loginResponse = loginFunction(socialLoginMandatory)
            if (loginResponse == null || loginResponse.ssoResult != SSOResult.SUCCESS) {
                // on login failed, propagate the error
                Logger.d(LOG_TAG, "Login failed, so propagating the error to upper presentation " +
                        "layer")
                return response
            }
            if(!firedSignOutEvent){
                firedSignOutEvent = true
                SSOAnalyticsUtility.logMenuSignOut(SSO.getLoginType().name,null, Constants.SYSTEM)
            }

        } while (++attemptCount < retriesCount)

        return response
    }
}

private fun isSocialLoginMandatory(response: Response): Boolean {
    val string = ApiResponseUtils.extractRawResponse(response)
    val json = JsonUtils.fromJson(string, JsonObject::class.java)

    val status = json.get(HTTP_401_ERROR_STATUS) as? JsonObject
            ?: throw APIException(BaseError(Constants.HTTP_401_INVALID_RESPONSE_BODY))
    val errorCode = status.getAsJsonPrimitive(HTTP_401_SPECIFIC_ERROR_CODE)
            ?: throw APIException(BaseError(Constants.HTTP_401_INVALID_RESPONSE_BODY))

    return CommonUtils.equals(errorCode, HTTP_401_ERROR_AUTH_03) &&
            (SSO.getInstance().userDetails.loginType == LoginType.GUEST ||
                    SSO.getInstance().userDetails.loginType == LoginType.NONE)
}

fun getDefaultLoginFunction(): (socialLoginMandatory : Boolean) -> LoginResponse? {
    return {
        Logger.d(LOG_TAG, "Default function - login function - Entry")

        try {
            LoginOn401.login(it)
        } catch (e : Exception) {
            Logger.caughtException(e)
            null
        }
    }
}

object LoginOn401 {
    private val loginSubject = PublishSubject.create<LoginResponse>()

    init {
        Logger.d(LOG_TAG, "Registering to bus..")
        AndroidUtils.getMainThreadHandler().post {
            BusProvider.getUIBusInstance().register(this)
        }
        Logger.d(LOG_TAG, "Registering to bus success")
    }

    @Subscribe
    fun onLoginResponseReceived(loginResponse: LoginResponse) {
        Logger.d(LOG_TAG, "Received LoginResponse and going to post on LoginSubject")
        loginSubject.onNext(loginResponse)
    }

    fun login(socialLoginMandatory: Boolean): LoginResponse? {
        Logger.d(LOG_TAG, "Going to attempt login.")
        return try {
            if (socialLoginMandatory) {
                Logger.d(LOG_TAG, "Social Login is mandatory, so will call SSO to start Sign in " +
                        "with sign in options")
                SSO.getInstance().loginSocialMandatory()
            } else {
                Logger.d(LOG_TAG, "Social login is not mandatory, so login into last logged in " +
                        "option")
                SSO.getInstance().login(null, LoginMode.BACKGROUND_ONLY, SSOLoginSourceType
                        .API_401_RESPONSE)
            }
            Logger.d(LOG_TAG, "Going to call blocking Last of loginSubject with timeout ..")
            loginSubject.timeout(DEFAULT_LOGIN_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS).blockingLast()
        } catch (e: Throwable) {
            Logger.e(LOG_TAG, "Login in timed out, so returning null..")
            Logger.caughtException(e)
            null
        }
    }
}
