/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.shared.presenters.tests;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.sso.SSO;
import com.newshunt.sso.model.entity.LoginResponse;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.model.internal.service.VerifySessionServiceImpl;
import com.newshunt.sso.presenter.SSOPresenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author anshul.jain on 5/13/2016.
 * Class for unit testing SSOPresenter
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    SSO.class, Looper.class, CommonUtils.class, SSOPresenter.class, VerifySessionServiceImpl.class
})

public class SSOPresenterTest {
  private int uniqueId = 10;
  private SSOPresenter ssoPresenter;
  private Status status;
  @Mock
  private SSO mockSSO;
  @Mock
  private SSOView mockSSOView;
  @Mock
  private SSO.Publisher mockPublisher;
  @Mock
  private Looper mockLooper;
  @Mock
  private Handler mockHandler;
  @Mock
  private Application mockApplication;
  @Mock
  private LoginResponse mockLoginResponse;

  @Before
  public void setUp() {
    //init mock of static methods
    PowerMockito.mockStatic(SSO.class);
    PowerMockito.mockStatic(Looper.class);
    PowerMockito.mockStatic(CommonUtils.class);
    //Set Expectations
    when(Looper.getMainLooper()).thenReturn(mockLooper);
    when(SSO.getInstance()).thenReturn(mockSSO);
    PowerMockito.when(CommonUtils.getApplication()).thenReturn(mockApplication);
    Handler handler = PowerMockito.mock(Handler.class);
    try {
      whenNew(Handler.class).withAnyArguments().thenReturn(handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
    doReturn(true).when(handler).post(Matchers.any(Runnable.class));
    //init
    ssoPresenter = new SSOPresenter(mockPublisher, mockSSOView);
    status = new Status();
  }

  @Test
  public void testVerifySession_User_isNull() {
    //init
    when(mockSSO.getUserDetails()).thenReturn(null);
    //action
    ssoPresenter.verifySession();
    //Test
    verify(mockPublisher).postLogoutResult(SSOResult.SESSION_INVALID);
  }

  public void testVerifySession_User_NotNull() {
    //Init
    SSO.UserDetails userDetails = mock(SSO.UserDetails.class);
    when(mockSSO.getUserDetails()).thenReturn(userDetails);
    VerifySessionServiceImpl sessionService = PowerMockito.mock(VerifySessionServiceImpl.class);
    try {
      whenNew(VerifySessionServiceImpl.class).withAnyArguments().thenReturn(sessionService);
    } catch (Exception e) {
      e.printStackTrace();
    }
    PowerMockito.doNothing().when(sessionService).verifySession(Constants.COMMON_ID);
    //Action
    ssoPresenter.verifySession();
    //Test
    verify(sessionService).verifySession(Constants.COMMON_ID);
  }


  @Test
  public void testOnLogin() {
    //Init
    SSOResult ssoResult = mockLoginResponse.getSsoResult();
    //Action
    ssoPresenter.onLogin(mockLoginResponse);
    //Test
    verify(mockPublisher).postLoginResult(ssoResult);
  }

  @Test
  public void testOnLogin_LoginType_None() {
    //Init
    PowerMockito.when(mockLoginResponse.getLoginType()).thenReturn(LoginType.NONE);
    SSOResult ssoResult = mockLoginResponse.getSsoResult();
    //Action
    ssoPresenter.onLogin(mockLoginResponse);
    //Test
    verify(mockPublisher).postLoginResult(ssoResult, LoginType.NONE, Constants.EMPTY_STRING,
        Constants.EMPTY_STRING, Constants.EMPTY_STRING);
  }

  @Test
  public void testOnLogin_SSOResult_Success() {
    //Init
    PowerMockito.when(mockLoginResponse.getSsoResult()).thenReturn(SSOResult.SUCCESS);
    //Action
    ssoPresenter.onLogin(mockLoginResponse);
    //Test
    verify(mockPublisher).postLoginResult(eq(SSOResult.SUCCESS), Matchers.any(LoginType.class),
        anyString(), anyString(), anyString());
  }

}