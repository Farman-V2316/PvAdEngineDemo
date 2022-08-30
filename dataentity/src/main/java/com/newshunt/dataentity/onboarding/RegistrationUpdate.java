package com.newshunt.dataentity.onboarding;

import com.newshunt.dataentity.common.model.entity.BaseError;


/**
 * Registration Update after Device Registration is Stated
 * <p/>
 * a) Registration State
 * b) Error Message {@link String}
 *
 * @author ranjith.suda
 */
public class RegistrationUpdate {

  private final RegistrationState registrationState;
  private final BaseError baseError;

  public RegistrationUpdate(RegistrationState registrationState, BaseError
      baseError) {
    this.baseError = baseError;
    this.registrationState = registrationState;
  }

  public RegistrationState getRegistrationState() {
    return registrationState;
  }

  public BaseError getRegistrationErrorMessage() {
    return baseError;
  }
}


