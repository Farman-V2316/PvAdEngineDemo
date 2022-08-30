/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.shared.presenters.tests;

import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse;
import com.newshunt.sso.SSO;
import com.newshunt.sso.model.entity.Credential;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.model.entity.UserLoginPayload;
import com.newshunt.sso.presenter.SSOGuestPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author anshul.jain on 5/16/2016.
 * Class for unit Testing SSOGuestPresenter
 */

public class SSOGuestPresenterTest {
  private SSOGuestPresenter ssoGuestPresenter;
  @Mock
  private SSO.Publisher mockPublisher;
  @Mock
  private UserLoginResponse mockUserLoginResponse;
  @Mock
  private UserLoginPayload mockUserLoginPayload;
  @Mock
  private Status mockStatus;

  @Before
  public void setUp() {
    ssoGuestPresenter = new SSOGuestPresenter(mockPublisher);
  }

  @Test
  public void testOnUserLoginResponse() {
    //Action
    ssoGuestPresenter.onUserLoginResponse(mockUserLoginResponse, mockUserLoginPayload, anyString(),
        anyInt());
    //Test
    verify(mockPublisher).postGuestLoginResult(Matchers.any(Credential.class), eq(SSOResult
        .SUCCESS));
  }

  @Test
  public void testOnUserLoginError() {
    //Action
    ssoGuestPresenter.onUserLoginError(Matchers.any(Status.class), mockUserLoginPayload
        , anyInt());
    //Test
    verify(mockPublisher).postGuestLoginResult(Matchers.any(Credential.class),
        eq(SSOResult.UNEXPECTED_ERROR));
  }
}