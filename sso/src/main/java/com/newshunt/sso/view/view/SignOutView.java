/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.sso.view.view;

import com.newshunt.common.view.view.BaseMVPView;

/**
 * View interface for Sign out view.
 *
 * @author anshul.jain on 3/14/2016.
 */

public interface SignOutView extends BaseMVPView {

  void showUnexpectedError();

  void showToast(String message);

  void onLogoutSuccess();

  void onLogoutFailed();

}
