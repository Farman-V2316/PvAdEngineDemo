/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.statusupdate;


import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.model.entity.BaseDataResponse;

/**
 * Contains response from server on any update done that results in boolean response.
 *
 * @author shreyas.desai
 */
public class StatusUpdateResponse extends BaseDataResponse {
  private static final long serialVersionUID = 3437087038543682330L;

  private Boolean data;
  private int status = Constants.ERROR_UNEXPECTED_INT;
  private StatusUpdateType statusUpdateType;

  public StatusUpdateResponse() {
  }

  public StatusUpdateResponse(Boolean data, StatusUpdateType statusUpdateType) {
    this.data = data;
    this.statusUpdateType = statusUpdateType;
  }

  public StatusUpdateResponse(int uniqueRequestId, Boolean data,
                              StatusUpdateType statusUpdateType) {
    super(uniqueRequestId);
    this.data = data;
    this.statusUpdateType = statusUpdateType;
  }

  public StatusUpdateResponse(int uniqueRequestId, Boolean data, StatusUpdateType statusUpdateType,
                              int status) {
    super(uniqueRequestId);
    this.data = data;
    this.statusUpdateType = statusUpdateType;
    this.status = status;
  }

  public Boolean getData() {
    return data;
  }

  public void setData(Boolean data) {
    this.data = data;
  }

  public StatusUpdateType getStatusUpdateType() {
    return statusUpdateType;
  }

  public void setStatusUpdateType(
      StatusUpdateType statusUpdateType) {
    this.statusUpdateType = statusUpdateType;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
