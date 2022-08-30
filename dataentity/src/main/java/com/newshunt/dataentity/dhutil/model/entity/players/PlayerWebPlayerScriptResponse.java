package com.newshunt.dataentity.dhutil.model.entity.players;

import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

/**
 * Created by santoshkulkarni on 25/05/17.
 */

public class PlayerWebPlayerScriptResponse {
  private int requestId;
  private ApiResponse<PlayerUnifiedWebPlayer> playerScript;
  private BaseError baseError;

  public int getRequestId() {
    return requestId;
  }

  public void setRequestId(int requestId) {
    this.requestId = requestId;
  }

  public ApiResponse<PlayerUnifiedWebPlayer> getPlayerScript() {
    return playerScript;
  }

  public void setPlayerScript(
      ApiResponse<PlayerUnifiedWebPlayer> playerScript) {
    this.playerScript = playerScript;
  }

  public BaseError getBaseError() {
    return baseError;
  }

  public void setBaseError(BaseError baseError) {
    this.baseError = baseError;
  }


}
