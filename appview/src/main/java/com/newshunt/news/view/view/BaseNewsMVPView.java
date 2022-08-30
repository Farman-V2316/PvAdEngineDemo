/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.view;

import com.newshunt.common.view.view.BaseMVPView;
import com.newshunt.dataentity.common.model.entity.BaseError;

/**
 * @author satosh.dhanyamraju
 */
public interface BaseNewsMVPView extends BaseMVPView {

  /**
   * called when we get network error response.
   * @param baseError
   */
  void showNetworkErrorToast(BaseError baseError);

}
