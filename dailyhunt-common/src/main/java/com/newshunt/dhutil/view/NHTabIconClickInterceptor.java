/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view;

import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dhutil.view.customview.NhTabVIewItem;

/**
 * An implementation to intercept the click event of
 * {@link com.newshunt.dhutil.view.customview.NHTabView} icon and do some other operation. Usual
 * operation on click of NHTabView icon is launch the corresponding {@link AppSection}
 *
 * @author santhosh.kc
 */
public interface NHTabIconClickInterceptor {

  /**
   * Function to intercept the click event
   *
   * @param nhTabVIewItem - {@link NhTabVIewItem} clicked
   * @param appSectionInfo - {@link AppSectionInfo} of the icon represented
   * @return - true to intercept click event else false
   */
  boolean onInterceptUpdateIconClick(NhTabVIewItem nhTabVIewItem, AppSectionInfo appSectionInfo);
}
