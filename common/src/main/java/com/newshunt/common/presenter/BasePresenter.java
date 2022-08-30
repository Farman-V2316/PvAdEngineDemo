/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.presenter;

import com.newshunt.common.util.R;
import com.newshunt.common.domain.CancelUsecase;
import com.newshunt.common.domain.CancelUsecaseController;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Interface that represents a Presenter in the model view presenter Pattern
 * defines methods to manage the Activity / Fragment lifecycle
 *
 * @author maruti.borker
 */
public abstract class BasePresenter {

  private static final String LOG_TAG = "BasePresenter";
  private static final CancelUsecase cancelUsecase = new CancelUsecaseController();

  protected final CompositeDisposable disposables = new CompositeDisposable();


  /**
   * Called when the presenter is initialized
   */
  public abstract void start();

  /**
   * Called when the presenter is stop, i.e when an activity
   * or a fragment finishes
   */
  public abstract void stop();

  /**
   * <p>Presenter should pass the Object returned by tag() to usecase, which passes to service,
   * which shall use it for tagging requests sent to NetworkSDK. Then, this class will be able to
   * automatically cancel requests tagged with tag()</p>
   *
   * @return
   * Non-null, unique identifier for current object making requests. Override and return null, if
   * default cancellation behaviour is not desired.
   */
  protected Object tag(){
    return this;
  }

  /**
   * This method adds the disposable to base composite disposable which then gets disposed by
   * either calling destroy() of Presenter or dispose() of presenter.
   *
   * @param disposable
   */
  public void addDisposable(Disposable disposable) {
    disposables.add(disposable);
  }

  /**
   * Disposes the {@link CompositeDisposable} to cancel any on going rx call
   */
  protected void dispose() {
    if (disposables != null) {
      disposables.dispose();
    }
  }

  /**
   * If subclass supports cancellable operations, cancels the requests.
   * @return true if cancel successful, false otherwise.
   */
  public boolean destroy() {
    dispose();
    if (tag() != null) {
      Logger.d(LOG_TAG, "destroy: cancelling [" + tag() + "] on "+this);
      return cancelUsecase.cancel(tag());
    } else {
      Logger.d(LOG_TAG, "destroy: not cancelling " + this + " tag() returned null");
      return false;
    }
  }

  public boolean isPaginationTerminated(String errorMessage) {
    return CommonUtils.equalsIgnoreCase(errorMessage, CommonUtils.getString(R.string.no_content_found));
  }
}

