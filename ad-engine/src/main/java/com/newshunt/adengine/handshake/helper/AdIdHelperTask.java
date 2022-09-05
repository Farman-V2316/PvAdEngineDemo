/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.handshake.helper;

import android.os.AsyncTask;

/**
 * Helps retrieve ad id. It's google ad id used for re-targeting users.
 * We pass this information in every ad request and in DEVICE_GOOGLE_IDS event.
 * </p>
 * USAGE:
 * </p>
 * <p/>
 * <pre>
 *   {@code
 *   AdIdHelper.init(context);
 *   AdIdHelper.getGoogleAdId();
 *   }
 * </pre>
 *
 * @author shreyas.desai
 */
public class AdIdHelperTask {

  /**
   * This method must be called before calling get on ad id. Fetches google ad id in
   * separate thread.
   */
  public static void init() {
    AsyncTask<Void, Void, Boolean> asyncTask =
        new AsyncTask<Void, Void, Boolean>() {
          @Override
          public Boolean doInBackground(Void... params) {
            return AdIdHelper.getAdIdInfo() != null;
          }

          @Override
          protected void onPostExecute(Boolean success) {
            //TODO (raunak.yadav) :A possible case for race condition and hence multiple api calls.
            // Will handle all corner cases properly in next release. For now, when gaid
            // changes, at max two calls will go through, but since its a rare case, letting it be.
            //TODO: PANDA removed
            //OnAppRegistrationController.getInstance().setIsGoogleAdIdStatus(success);
          }
        };

    asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
  }
}
