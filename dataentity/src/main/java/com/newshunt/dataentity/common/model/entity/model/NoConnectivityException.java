/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.model;

import java.io.IOException;

/*
 * Exception for when there is not internet connectivity
 *
 * @author maruti.borker
 */
public class NoConnectivityException extends IOException {
  public NoConnectivityException(String s) {
    super(s);
  }
}
