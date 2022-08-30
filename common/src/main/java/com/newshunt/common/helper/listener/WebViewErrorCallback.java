/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.listener;

import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.NhWebViewErrorType;

/**
 * Created by anshul on 09/05/17.
 * A callback for getting errors on the WebView. These functions can be called from the API
 * response or from the Js callbacks.
 */

public interface WebViewErrorCallback {

  void onErrorReceived(BaseError baseError);

  void onErrorReceived(NhWebViewErrorType nhWebViewErrorType);

}

