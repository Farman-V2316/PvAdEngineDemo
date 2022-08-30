/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.shared.presenters.tests;

import android.app.Activity;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.sso.SSO;
import com.newshunt.sso.helper.SSOLoginSourceTracker;
import com.newshunt.sso.model.entity.LoginMode;
import com.newshunt.model.entity.LoginType;
import com.newshunt.sso.model.entity.SSOLoginSourceType;
import com.newshunt.sso.presenter.SSOGuestPresenter;
import com.newshunt.sso.presenter.SSOPresenter;
import com.newshunt.sso.view.activity.SignOnActivity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author anshul.jain on 5/18/2016.
 *         Class for Unit Testing SSO
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({SSO.class, Logger.class, SignOnActivity.class})

public class SSOTest {
  private static SSOPresenter mockSSOPresenter = PowerMockito.mock(SSOPresenter.class);
  private static SSOLoginSourceTracker mockSSOLoginSourceTracker = PowerMockito.mock
      (SSOLoginSourceTracker.class);
  private static SSOGuestPresenter mockSSOGuestPresenter =
      PowerMockito.mock(SSOGuestPresenter.class);
  private static SSO.UserDetails mockUserDetails = PowerMockito.mock(SSO.UserDetails.class);
  private SSO sso;
  private LoginMode loginMode = LoginMode.USER_EXPLICIT;
  private SSOLoginSourceType loginSourceType = SSOLoginSourceType.UNKNOWN;
  @Mock
  private Activity mockActivity;

  @BeforeClass
  public static void onSetUpClass() {
    try {
      whenNew(SSO.UserDetails.class).withAnyArguments().thenReturn(mockUserDetails);
      whenNew(SSOPresenter.class).withAnyArguments().thenReturn(mockSSOPresenter);
      whenNew(SSOLoginSourceTracker.class).withAnyArguments().thenReturn
          (mockSSOLoginSourceTracker);
      whenNew(SSOGuestPresenter.class).withAnyArguments().thenReturn(mockSSOGuestPresenter);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Before
  public void onSetUp() {
    //init of static method
    PowerMockito.mockStatic(Logger.class);
    PowerMockito.mockStatic(SignOnActivity.class);
    //Set Expectations.
    PowerMockito.doNothing().when(Logger.class);
    Logger.d(anyString(), anyString());
    //init
    sso = spy(SSO.getInstance());
  }


  @Test
  public void testLogin_AlreadyLoggedIn() {
    //Init
    loginMode = LoginMode.USER_EXPLICIT;
    doNothing().when(mockSSOGuestPresenter).loginAsGuest();
    Mockito.when(mockUserDetails.getLoginType()).thenReturn(LoginType.NONE);
    doReturn(true).when(sso).isLoggedIn(false);
    //Action
    sso.login(mockActivity, loginMode, loginSourceType);
    //Tests
    verify(mockSSOGuestPresenter).loginAsGuest();
  }

  @Test
  public void testLogin_Facebook() {
    //Init
    loginMode = LoginMode.USER_EXPLICIT;
    doReturn(true).when(sso).isLoggedIn(false);
    when(mockUserDetails.getLoginType()).thenReturn(LoginType.FACEBOOK);
    //Action
    sso.login(mockActivity, loginMode, loginSourceType);
    //Tests
    try {
      verifyPrivate(sso).invoke("loginForType", LoginType.FACEBOOK, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testLogin_Facebook_Background() {
    //Init
    loginMode = LoginMode.BACKGROUND_ONLY;
    when(mockUserDetails.getLoginType()).thenReturn(LoginType.FACEBOOK);
    //Action
    sso.login(mockActivity, loginMode, loginSourceType);
    //Test
    try {
      verifyPrivate(sso).invoke("loginForType", LoginType.FACEBOOK, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}