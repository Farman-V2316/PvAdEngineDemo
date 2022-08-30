/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.shared.presenters.tests;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse;
import com.newshunt.sso.SSO;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.model.entity.SocialLoginPayload;
import com.newshunt.sso.presenter.SignOnPresenter;
import com.newshunt.sso.view.view.SignOnView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author anshul.jain on 5/13/2016.
 * Class for unit testing SignOnPresenter
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class, SSO.class, BusProvider.class})

public class SignOnPresenterTest {
  private boolean retryLogin = false;
  private int uniqueId = 10;
  private SignOnPresenter signOnPresenter;
  private String noInternetConnMsg = "No internet connection";
  private Status status = new Status();
  @Mock
  private Context mockContext;
  @Mock
  private SocialLoginPayload mockSocialLoginPayload;
  @Mock
  private UserLoginResponse mockUserLoginResponse;
  @Mock
  private SignOnView mockSignOnView;
  @Mock
  private Application mockApplication;

  @Before
  public void setUp() {
   //Init of static classes which have static methods;
    PowerMockito.mockStatic(CommonUtils.class);
    PowerMockito.mockStatic(SSO.class);
    PowerMockito.mockStatic(BusProvider.class);

    //Set expectations
    when(CommonUtils.getApplication()).thenReturn(mockApplication);
    when(mockApplication.getString(anyInt())).thenReturn(noInternetConnMsg);
  }

  private void initForLoginErrorTests() {
    Resources mockResources = mock(Resources.class);
    when(mockSignOnView.getViewContext()).thenReturn(mockContext);
    when(mockContext.getResources()).thenReturn(mockResources);
    when(mockResources.getString(anyInt())).thenReturn(anyString());
  }

  private void disablePostLoginMethod() {
    doNothing().when(signOnPresenter).postLoginResponse(Matchers.any(SSOResult.class), Matchers.any
            (LoginType.class),
        anyString(), anyString(), anyString());
  }

  @Test
  public void testLogin() {
    //Init
    when(CommonUtils.isNetworkAvailable(CommonUtils.getApplication())).thenReturn(false);
    //Action
    verify(mockSignOnView).showToast(noInternetConnMsg);
  }

  @Test
  public void testOnHelperLoginError_Network_Error() {
    initForLoginErrorTests();
    for (SSOResult ssoResult : SSOResult.values()) {
      if (ssoResult == SSOResult.NETWORK_ERROR) {
        signOnPresenter.onClientLoginError(ssoResult);
        verify(mockSignOnView).showToast(anyString());
        verify(mockSignOnView).showLoadingProgress(false, null);
        verify(mockSignOnView).showSignOnView(true);
      }
    }
  }

  @Test
  public void testOnClientLoginError_Unexpected_Error() {
    initForLoginErrorTests();
    for (SSOResult ssoResult : SSOResult.values()) {
      if (ssoResult == SSOResult.UNEXPECTED_ERROR) {
        signOnPresenter.onClientLoginError(ssoResult);
        verify(mockSignOnView).showToast(anyString());
        verify(mockSignOnView).showLoadingProgress(false, null);
        verify(mockSignOnView).showSignOnView(true);
      }
    }
  }
}