package com.newshunt.common.helper;

/**
 * Created by anshul on 06/03/18.
 */

/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

import com.newshunt.common.helper.common.AppUtilsService;

/**
 * Created by anshul on 05/02/18.
 * A class for providing app level functionality to lower level modules
 */

public class AppUtilsProvider {

  private static AppUtilsService appUtilsService;

  public static AppUtilsService getAppUtilsService() {
    return appUtilsService;
  }

  public static void setAppUtilsService(AppUtilsService appUtilsService) {
    AppUtilsProvider.appUtilsService = appUtilsService;
  }
}
