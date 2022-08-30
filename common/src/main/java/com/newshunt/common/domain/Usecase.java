/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.domain;

/**
 * Base class for usecases to be used by presenters.
 * Follows command pattern and has a execute function which will
 * run the usecase.
 *
 * @author maruti.borker
 */
public interface Usecase {

  void execute();
}
