package com.newshunt.news.util

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dhutil.logD
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginResult
import com.newshunt.sso.model.entity.SSOResult
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Handles authentication and retrying an operation. Maintains list of executables function and
 * calls them upon receiving LoginResult bus event.
 *
 * @see CommentsPresenter
 * @see CountsPresenter
 *
 * @author satosh.dhanyamraju
 *
 */
class AuthOrchestrator(private val performLogin: (Boolean, Int) -> Unit,
                       private val userLoggedIn: () -> Boolean = { SSO.getInstance().isLoggedIn(false) },
                       private val bus: Bus = BusProvider.getUIBusInstance()) {
	private val LOG_TAG = "AuthOrchestrator"
	/**
	 * Some operations need auth. For those, Presenter invokes view.perFormLogin(), stores the
	 * function in this map, and listens for LoginResult on the bus. On successful login, all the
	 * stores functions will be invoked and map is cleared.
	 */
	private val funcsPendingAuth = hashMapOf<Any, (Boolean) -> Any>()

	/**
	 * @param doOnSuccess should not call trigger login again
	 */
	fun runWhenLoggedin(key: Any, requireLogin: Boolean = true, toastMsgId: Int = 0, doOnSuccess: (Boolean) -> Any) {
		if (requireLogin && !userLoggedIn()) {
			handle401(key, true, toastMsgId, doOnSuccess)
		} else {
			logD(LOG_TAG, "Already logged in. Running $key, map= ${funcsPendingAuth.keys}")
			doOnSuccess(true)
		}
	}

	fun handle401(key: Any, showToast: Boolean = false, toastMsgId: Int = 0, f: (Boolean) -> Any) {
		funcsPendingAuth[key] = f
		performLogin(showToast, toastMsgId)
		logD(LOG_TAG, "Trying login for $key, map= ${funcsPendingAuth.keys}")
	}

	@Subscribe
	fun onLoginResponse(event: LoginResult) {
		val success = event.result == SSOResult.SUCCESS && SSO.getInstance().isLoggedIn(false)
		funcsPendingAuth.forEach {
			val (k, f) = it
			logD(LOG_TAG, "Invoking fun of $k, success= $success")
			f(success)
		}
		if (success) { // clear if success. If failure, may be retry?
			funcsPendingAuth.clear()
		}
	}

	fun start() = bus.register(this)

	fun stop() {
		runCatching { bus.unregister(this) }
	}
}