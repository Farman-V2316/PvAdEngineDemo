/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagKey
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.DbgCode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.APIException
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.helper.FullPageErrorMessage
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.actionSettings
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.btnHome
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.dialogButtonRetry
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorConnectivity
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorGeneric
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorIcon
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoConnection
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoContentMsg
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorServerIssue
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.noConnectionError
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.noContentFound
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.offlineSavingFailed
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.savedArticleEmptyList
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowNetworkInfo
import org.robolectric.shadows.ShadowToast
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.IMG as IMG1

/**
 * Tests for [ErrorMessageBuilder].
 * Run locally, with androidx.test
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
class ErrorMessageBuilderTest {
    lateinit var context: Context
    @get:Rule
    val rule = InstantTaskExecutorRule()
    init {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    }

    private val connectionEventNoCon = ConnectionSpeedEvent(ConnectionSpeed.NO_CONNECTION)
    private val connectionEventAvg = ConnectionSpeedEvent(ConnectionSpeed.AVERAGE)

    @Before
    fun setUp() {
        CommonUtils.IS_IN_TEST_MODE = true
        AppConfig.createInstance(AppConfigBuilder())
        ApplicationProvider.getApplicationContext<Application>().setTheme(R.style.AppThemeDay)
        context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style
                .AppThemeDay)

    }

    // basic UI ---------------------------------------

    @Test
    fun testDisplay_nosectionerror() {
        val msg = errorNoContentMsg
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.bad_error)
        val action = dialogButtonRetry
        val baseError = BaseError(msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_nosavedarticleerror() {
        val msg = savedArticleEmptyList
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.no_saved_artcles)
        val baseError = BaseError(msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action))).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testDisplay_genericerror() {
        val msg = errorGeneric
        val action = dialogButtonRetry
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.bad_error)
        val baseError = BaseError(msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_genericerrorfordhtv() {
        val msg = errorGeneric
        val action = dialogButtonRetry
        val icon = R.drawable.error_generic_dhtv_night
        val baseError = BaseError(msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError, "isDhtv" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_nocontenterror() {
        val msg = noContentFound
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.content_error)
        val action = btnHome
        val baseError = BaseError(DbgCode.DbgHttpCode(204),msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_nocontenterrorfordhtv() {
        val msg = noContentFound
        val icon = R.drawable.no_content_found_dhtv_night
        val action = btnHome
        val baseError = BaseError(DbgCode.DbgHttpCode(204),msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError, "isDhtv" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_servererror() {
        val msg = errorServerIssue
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connection_error)
        val action = dialogButtonRetry
        val apiException = APIException(BaseError(Throwable(), "", "500"))
        val baseError = BaseError(apiException, msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_servererrorfordhtv() {
        val msg = errorServerIssue
        val icon = R.drawable.error_server_issue_dhtv_night
        val action = dialogButtonRetry
        val apiException = APIException(BaseError(Throwable(), "", "500"))
        val baseError = BaseError(apiException, msg)

        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError, "isDhtv" to true))
        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon)/*, withTagKey(icon, equalTo(IMG1))*/))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_nointerneterror() {
        val msg = errorNoConnection
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))


        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_nointerneterrorfordhtv() {
        val msg = errorNoConnection
        val icon = R.drawable.error_no_connection
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError,"isDhtv" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_nointernet_SettingsIsShownWhenNoConnection() {
        val msg = errorNoConnection
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = actionSettings
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        val ctx = ApplicationProvider.getApplicationContext<Application>()
        val shadowCM = Shadows.shadowOf(ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        shadowCM.setActiveNetworkInfo(ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.DISCONNECTED, 1,
                1, false, NetworkInfo.State.UNKNOWN))

        f.onFragment {
            it.showErr()
        }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_nointernet_SettingsIsShownWhenConnectionIsLost() {
        val msg = errorNoConnection
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = actionSettings
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment {
            it.showErr()
            it.liveData.postValue(connectionEventNoCon)
        }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_nointernet_WhenShowingNoContent_networkChanges_doesNotChangeAction() {
        val msg = noContentFound
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.content_error)
        val action = btnHome
        val baseError = BaseError(DbgCode.DbgHttpCode(204),msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment {
            it.showErr()
            it.liveData.postValue(connectionEventNoCon)
            it.liveData.postValue(connectionEventAvg)
            it.liveData.postValue(connectionEventNoCon)
        }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(actionSettings))).check(doesNotExist())
    }

    @Test
    fun testDisplay_nointernet_WhenErrorNotShown_networkChanges_noImpact() {
        val msg = errorNoConnection
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment {
            it.showErr()
            it.hideErr()
            it.liveData.postValue(connectionEventAvg)
        }


        onView(withId(R.id.error_msg)).check(doesNotExist())
        onView(withId(R.id.connection_error_msg_icon)).check(doesNotExist())
        onView(withId(R.id.error_action)).check(doesNotExist())
    }

    @Test
    fun testDisplay_connectivityerror() {
        val msg = errorConnectivity
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testDisplay_connectivityerrorfordhtv() {
        val msg = errorConnectivity
        val icon = R.drawable.error_no_connection
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError,"isDhtv" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1)))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testDisplay_emptyerror() {
        val msg = errorNoContentMsg
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.bad_error)
        val action = dialogButtonRetry
        val baseError = BaseError(msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }


    @Test
    fun testHideerror_shows_nothing() {
        val baseError = BaseErrorBuilder.getBaseError(null)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        f.onFragment {
            it.showErr()
            it.hideErr()
        }

        onView(withId(R.id.error_msg)).check(doesNotExist())
        onView(withId(R.id.connection_error_msg_icon)).check(doesNotExist())
        onView(withId(R.id.error_action)).check(doesNotExist())
    }

    @Test
    fun testparam_ShowRetryOnNoContent() {
        val msg = noContentFound
        val action = dialogButtonRetry

        val baseError = BaseError(DbgCode.DbgHttpCode(204),msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError,
                "showRetryOnNoContent" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed
        ()))
    }


    @Test
    fun testparam_Override204ForSearch() {
        val searchNoContent = CommonUtils.getString(R.string.error_search_no_results,
                SearchAnalyticsHelper.query)

        val msg = noContentFound
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.content_error)
        val action = btnHome
        val baseError = BaseError(DbgCode.DbgHttpCode(204), msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError,
                "override204MsgForSearch" to searchNoContent))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(doesNotExist())
        onView(allOf(withId(R.id.error_msg), withText(searchNoContent))).check(matches(isDisplayed()))
    }

    @Test
    fun testparam_HideButtons() {
        val msg = noContentFound
        val actionHome = btnHome
        val actionRetry = dialogButtonRetry
        val baseError = BaseError(DbgCode.DbgHttpCode(204),msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError,
                "hideButtons" to true))

        f.onFragment { it.showErr() }

        onView(allOf(withId(R.id.error_action), withText(actionHome), withEffectiveVisibility
        (Visibility.VISIBLE))).check(doesNotExist())
        onView(allOf(withId(R.id.error_action), withText(actionRetry), withEffectiveVisibility
        (Visibility.VISIBLE))).check(doesNotExist())
    }

    @Test
    fun testDisplay_showToast() {
        val f = launchFragmentInContainer<F>(bundleOf("err" to ""), themeResId = R.style.AppThemeDay)
        val message = "sample-toast"

        f.onFragment {
            it.showToastError(message, context)
        }

        assertTrue(ShadowToast.showedToast(message))
    }

    // listeners ---------------------------------------

    @Test
    fun testListenerNotified_onRetryClick() {
        val msg = errorNoConnection
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        val mockListener = Mockito.mock(ErrorMessageBuilder.ErrorMessageClickedListener::class.java)
        f.onFragment {
            it.listener = mockListener
            it.showErr()
        }
        onView(withText(action)).perform(click())

        Mockito.verify(mockListener).onRetryClicked(ArgumentMatchers.any(View::class.java))
        Mockito.verifyNoMoreInteractions(mockListener)
    }

    @Test
    fun testListenerNotified_onNoContentClick() {
        val msg = noContentFound
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.content_error)
        val action = btnHome
        val baseError = BaseError(DbgCode.DbgHttpCode(204), msg)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        val mockListener = Mockito.mock(ErrorMessageBuilder.ErrorMessageClickedListener::class.java)
        f.onFragment {
            it.listener = mockListener
            it.showErr()
        }
        onView(withText(action)).perform(click())

        Mockito.verify(mockListener).onNoContentClicked(ArgumentMatchers.any(View::class.java))
        Mockito.verifyNoMoreInteractions(mockListener)
    }


    @Test
    fun testWhenConnected_CTA_becomesRetry() {
        val msg = errorNoConnection
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = dialogButtonRetry
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment {
            it.showErr()
            it.liveData.postValue(connectionEventAvg)
        }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testWhenDisconnected_CTA_becomesSettingsAgain() {
        val msg = errorNoConnection
        val icon = resForAttr(CommonUtils.getApplication(), R.attr.connectivity_error)
        val action = actionSettings
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))

        f.onFragment {
            it.showErr()
            it.liveData.postValue(connectionEventAvg)
            it.liveData.postValue(connectionEventNoCon)
        }

        onView(allOf(withId(R.id.error_msg), withText(msg))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.connection_error_msg_icon), withTagKey(icon, equalTo(IMG1))))
                .check(matches(isDisplayed()))
        onView(allOf(withId(R.id.error_action), withText(action))).check(matches(isDisplayed()))
    }

    @Test
    fun testOnSettingsClicked_IntentFired_noListenerInteraction() {
        val msg = errorNoConnection
        val action = actionSettings
        val baseError = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_INTERNET)
        val f = launchFragmentInContainer<F>(bundleOf("err" to baseError))
        val mockListener = Mockito.mock(ErrorMessageBuilder.ErrorMessageClickedListener::class.java)
        val ctk = ApplicationProvider.getApplicationContext<Application>()
        val shadowCM = Shadows.shadowOf(ctk.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        shadowCM.setActiveNetworkInfo(ShadowNetworkInfo.newInstance
        (NetworkInfo.DetailedState.DISCONNECTED, 1,
                1, false, NetworkInfo.State.UNKNOWN))
        var shadowAct: ShadowActivity? = null
        f.onFragment {
            it.listener = mockListener
            it.showErr()
            shadowAct = Shadows.shadowOf(it.activity)
        }
        onView(withText(action)).perform(click())

        val nextStartedActivity = shadowAct?.nextStartedActivity ?: Intent()
        assertThat(nextStartedActivity.action, equalTo(android.provider.Settings
                .ACTION_WIRELESS_SETTINGS))
        Mockito.verifyNoMoreInteractions(mockListener)
    }

    // state  ---------------------------------------
    @Test
    fun testState_isErrorShown_shouldeBeFalse_initially() {
        val f = launchFragmentInContainer<F>(bundleOf("err" to ""))
        var errorShown = false

        f.onFragment { frag -> frag.emb?.isErrorShown?.let { errorShown = it } }

        assertFalse(errorShown)
    }

    @Test
    fun testState_isErrorShwon_shouldeBeTrue_afterShowError() {
        val f = launchFragmentInContainer<F>(bundleOf("err" to BaseError("")))
        var errorShown = false

        f.onFragment { it ->
            it.showErr()
            it.emb?.isErrorShown?.let { errorShown = it }
        }

        assertTrue(errorShown)
    }

    @Test
    fun testState_isErrorShwon_shouldeBeFalse_afterHideError() {
        val f = launchFragmentInContainer<F>(bundleOf("err" to BaseError("")))
        var errorShown = false

        f.onFragment { it ->
            it.showErr()
            it.hideErr()
            it.emb?.isErrorShown?.let { errorShown = it }
        }

        assertFalse(errorShown)
    }

    @Test
    fun testCTAFullScreen() {
        assertTrue(ErrorMessageBuilder.getCTA(errorConnectivity) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTA(errorNoConnection) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTA(errorServerIssue) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTA(errorGeneric) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTA(noContentFound) == btnHome)
        assertTrue(ErrorMessageBuilder.getCTA(errorNoContentMsg) == btnHome)
        assertNull(ErrorMessageBuilder.getCTA(savedArticleEmptyList))
    }


    @Test
    fun testCTASnackbar() {

        assertTrue(ErrorMessageBuilder.getCTASnackbar(errorConnectivity) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTASnackbar(errorNoConnection) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTASnackbar(noConnectionError) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTASnackbar(offlineSavingFailed) == dialogButtonRetry)
        assertTrue(ErrorMessageBuilder.getCTASnackbar(noContentFound)
                == btnHome)
        assertTrue(ErrorMessageBuilder.getCTASnackbar(errorNoContentMsg) == btnHome)
        assertNull(ErrorMessageBuilder.getCTASnackbar(errorServerIssue))
        assertNull(ErrorMessageBuilder.getCTASnackbar(errorGeneric))


    }


    @Test
    fun testErrorIcon() {
        errorIcon(context, errorConnectivity) shouldBe resForAttr(context, R.attr.connectivity_error)
        errorIcon(context, errorNoConnection) shouldBe resForAttr(context, R.attr.connectivity_error)
        errorIcon(context, errorServerIssue) shouldBe resForAttr(context, R.attr.connection_error)
        errorIcon(context, savedArticleEmptyList) shouldBe resForAttr(context, R.attr
                .no_saved_artcles)
        errorIcon(context, noContentFound) shouldBe resForAttr(context, R.attr.content_error)
        errorIcon(context, errorGeneric) shouldBe resForAttr(context, R.attr.bad_error)
    }

    fun resForAttr(context: Context, res: Int ) = CommonUtils.getResourceIdFromAttribute(context, res)
}

/**
 * Added for UI tests on [ErrorMessageBuilder]. The only UI it shows is from [ErrorMessageBuilder].
 * We launch the fragment and make assertions on view.
 *
 * The purpose is to demonstrate the feasibility of testing view (on-device/local) through a
 * fragment in test-code. DO NOT follow this pattern for testing views. This is more suitable for
 * testing actual fragments (in production code). Refer to [ReadMoreLayoutViewHolderTest] about
 * testing views without using fragments.
 *
 *
 * @author satosh.dhanyamraju
 */
class F : Fragment() {
    val liveData: MutableLiveData<ConnectionSpeedEvent> = MutableLiveData()
    var emb: ErrorMessageBuilder? = null
    var listener: ErrorMessageBuilder.ErrorMessageClickedListener? = null
    private lateinit var linearLayout: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        linearLayout = LinearLayout(inflater.context)
        return linearLayout
    }

    fun showErr() {
        initemb()

        arguments?.run {
            val msg = getString("override204MsgForSearch")
            val erromsg204 = if(msg!= null)
                FullPageErrorMessage(msg)
            else null
            emb?.showError(error = get("err") as BaseError?,
                    showRetryOnNoContent = getBoolean("showRetryOnNoContent", false),
                    error204Message = erromsg204,
                    hideButtons = getBoolean("hideButtons", false),
                    isDhTv = getBoolean("isDhtv",false))
        }
    }

    fun hideErr() {
        emb?.hideError()
    }

    fun showToastError(message: String, context: Context) {
        ErrorMessageBuilder.showErrorSnackbar(linearLayout, message, forceToast = true)
    }

    private fun initemb(context: Context? = null) {
        if (emb == null) {
            emb = ErrorMessageBuilder(linearLayout, context ?: activity!!, listener
                    ?: (object : ErrorMessageBuilder.ErrorMessageClickedListener {
                        override fun onRetryClicked(view: View?) {}
                        override fun onNoContentClicked(view: View?) {}
                    }), connectivityData = liveData
            )
        }
    }
}