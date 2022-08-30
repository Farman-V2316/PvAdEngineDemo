/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.presenter;

import com.newshunt.common.presenter.BasePresenter;
import com.newshunt.news.view.view.BaseNewsMVPView;

/**
 * @author satosh.dhanyamraju
 */
public abstract class BaseNewsPresenter extends BasePresenter {

  public BaseNewsPresenter(BaseNewsMVPView baseNewsMVPView){
    if (baseNewsMVPView == null) {
      throw new IllegalArgumentException("all parameters should be non-null");
    }
  }
}
