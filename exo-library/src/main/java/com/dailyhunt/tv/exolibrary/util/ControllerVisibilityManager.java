package com.dailyhunt.tv.exolibrary.util;

import com.newshunt.common.helper.common.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ketkigarg on 09/01/18.
 */

public class ControllerVisibilityManager {
  private static ControllerVisibilityManager instance;

  private ControllerVisibilityManager() {
  }

  public static ControllerVisibilityManager getInstance() {
    if (instance == null) {
      synchronized (ControllerVisibilityManager.class) {
        if (instance == null) {
          instance = new ControllerVisibilityManager();
        }
      }
    }
    return instance;
  }

  private PublishSubject<Boolean> observable;
  private Disposable disposable;

  public void subscribe(Consumer<Boolean> observer) {
    if (observable == null) {
      observable = PublishSubject.create();
    }
    disposable = observable
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer, Logger::caughtException);
  }

  public void publishControllerVisibilityState(boolean isVisible) {
    if (observable != null) {
      observable.onNext(isVisible);
    }
  }

  public void unSubscribe() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
      observable = null;
    }
  }
}
