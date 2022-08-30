package com.dailyhunt.tv.players.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.dailyhunt.tv.players.model.entities.CallState;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Logger;

/**
 * @Author Rahul Ravindra
 */

public class TelephonyUtil {
  private TelephonyManager telephonyMgr;
  private TeleListener callListener;


  public TelephonyUtil(final Context context, int screenId) {
    this.telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    callListener = new TeleListener(screenId);
  }


  public static class TeleListener extends PhoneStateListener {
    private int screenId;

    public TeleListener(int screenId) {
      this.screenId = screenId;
    }
    static Handler handler = new Handler(Looper.getMainLooper());

    public void onCallStateChanged(final int state, final String incomingNumber) {
      super.onCallStateChanged(state, incomingNumber);
      try {
        handler.post(new Runnable() {
          @Override
          public void run() {
            CallState tvCallState = new CallState();
            tvCallState.setState(state);
            tvCallState.setScreenId(screenId);
            BusProvider.getUIBusInstance().post(tvCallState);
          }
        });
      } catch (final Exception ex) {
        Logger.caughtException(ex);
      }
    }
  }

  public void unregisterCallListener() {
    if (null != telephonyMgr && null != callListener) {
      telephonyMgr.listen(callListener,
          PhoneStateListener.LISTEN_NONE);
    }
  }

  public void registerCallListener() {
    telephonyMgr.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
  }
}
