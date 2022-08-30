/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.common.model.entity.model;

/**
 * Listener to set url in news list services.
 * This url will be appended to analytics event in case of errors after
 * success in network layer.
 *
 * @author raunak.yadav
 */
public interface BaseErrorReportingResponse {

  void setUrl(String url);
}