/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.helper.processor;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.NHJsonTypeAdapter;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.PreferencedAPIResponseWrapper;

import java.lang.reflect.Type;

/**
 * Processes shared preference copy for api responses
 *
 * @author raunak.yadav
 */
public class PreferencedResponseProcessor<T> {

  private static final String TAG = "PreferencedResponseProcessor";

  private APIResponseRequester<T> requester;
  private final Type typeOfT, typeOfApiResponse;
  private final NHJsonTypeAdapter typeAdapter;

  public PreferencedResponseProcessor(APIResponseRequester<T> requester, Type typeOfT,
                                      Type typeOfApiResponse, NHJsonTypeAdapter typeAdapter) {
    this.requester = requester;
    this.typeOfT = typeOfT;
    this.typeOfApiResponse = typeOfApiResponse;
    this.typeAdapter = typeAdapter;
  }

  /**
   * Parse and combine server response with delta stored in shared preference
   *
   * @param response Original server response saved as json.
   * @return
   */
  public T parseSharedPrefResponse(String response) {
    T cachedResponse;
    PreferencedAPIResponseWrapper wrapperResponse = JsonUtils.fromJson(response,
        PreferencedAPIResponseWrapper.class);
    if (wrapperResponse == null || wrapperResponse.getOriginalJson() == null) {
      cachedResponse = JsonUtils.fromJson(response, typeOfT, typeAdapter);
    } else {
      Logger.d(TAG,"Original Json: " + wrapperResponse.getOriginalJson());
      Logger.d(TAG,"Delta Json: " + wrapperResponse.getDeltaJson());
      ApiResponse<T> apiResponse =
          JsonUtils.fromJson(wrapperResponse.getOriginalJson(), typeOfApiResponse, typeAdapter);
      if (apiResponse == null) {
        return null;
      }
      cachedResponse = apiResponse.getData();
      requester.updateResponseWithDelta(cachedResponse, wrapperResponse.getDeltaJson());
    }
    return cachedResponse;
  }

  /**
   * Provides prepared jsonData to persist in prefs.
   */
  public String prepareDataToPersist(String version, String serverJsonResponse, String delta) {
    if (CommonUtils.isEmpty(serverJsonResponse)) {
      return Constants.EMPTY_STRING;
    }
    PreferencedAPIResponseWrapper wrapper = new PreferencedAPIResponseWrapper();
    wrapper.setVersion(version);
    wrapper.setOriginalJson(serverJsonResponse);
    wrapper.setDeltaJson(delta);
    return JsonUtils.toJson(wrapper);
  }

  public PreferencedAPIResponseWrapper getPreferencedWrapper(String response) {
    PreferencedAPIResponseWrapper wrapperResponse = JsonUtils.fromJson(response,
        PreferencedAPIResponseWrapper.class);

    return (wrapperResponse == null || (wrapperResponse.getOriginalJson() == null)) ? null :
        wrapperResponse;
  }

  public T getPreferencedPOJO(String response) {
    return JsonUtils.fromJson(response, typeOfT, typeAdapter);
  }

  public interface APIResponseRequester<T> {
    void updateResponseWithDelta(T response, String delta);
  }

}
