/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.info;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import androidx.core.app.ActivityCompat;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo;
import com.newshunt.sdk.network.connection.ConnectionType;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * Provides methods to access device connection info.
 * <p/>
 * <B>Note: This code is taken from existing app code without modification of
 * verification.
 * </B>
 *
 * @author shreyas.desai
 */
public class ConnectionInfoHelper {
  private static final String EMPTY_STRING = "";

  public static ConnectionInfo getConnectionInfo() {
    ConnectionInfo connectionInfo = new ConnectionInfo();

    try {
      Context context = CommonUtils.getApplication();
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = connectivityManager.getActiveNetworkInfo();

      String connectionType = getConnectionType(connectivityManager);
      connectionInfo.setConnection(connectionType);

      String currentAPNName = info.getExtraInfo();
      connectionInfo.setApnName(currentAPNName);


      String cellId = PasswordEncryption.encrypt(getCellId());
      connectionInfo.setCellid(cellId);

    } catch (Exception e) {
      return connectionInfo;
    }

    return connectionInfo;
  }

  public static String getConnectionType() {
    try {
      Context context = CommonUtils.getApplication();
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      return getConnectionType(connectivityManager);
    } catch (Exception ex) {
      Logger.caughtException(ex);
    }

    return null;
  }

  public static ConnectionType getConnectionTypeWithTimeout() {
    try {
      Context context = CommonUtils.getApplication();
      ConnectivityManager connectivityManager =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

      return Observable.fromCallable(
          () -> ConnectionType.fromName(getConnectionType(connectivityManager)))
          .timeout(3_000, TimeUnit.MILLISECONDS)
          .blockingFirst();

    } catch (Exception ex) {
      Logger.caughtException(ex);
    }

    return null;
  }

  private static String getConnectionType(ConnectivityManager mConnectivity) {
    String sConnectionType = "";

    try {
      NetworkInfo info = mConnectivity.getActiveNetworkInfo();

      // default is '0'
      sConnectionType = ConnectionType.NO_CONNECTION.getConnectionType();
      if (info == null || !mConnectivity.getActiveNetworkInfo().isConnected()) {
        return sConnectionType;
      }

      // Only update if WiFi or 3G is connected and not roaming
      if (!info.isAvailable() || !info.isConnected()) {
        return ConnectionType.NO_CONNECTION.getConnectionType();
      }

      int netType = info.getType();
      int netSubtype = info.getSubtype();
      if (netType == ConnectivityManager.TYPE_WIFI) {
        sConnectionType = ConnectionType.WI_FI.getConnectionType();
      } else if ((netType == ConnectivityManager.TYPE_MOBILE ||
          netType == ConnectivityManager.TYPE_WIMAX) &&
          (netSubtype == TelephonyManager.NETWORK_TYPE_LTE ||
              netSubtype == TelephonyManager.NETWORK_TYPE_HSPAP)) {
        sConnectionType = ConnectionType.FOUR_G.getConnectionType();

      } else if (netType == ConnectivityManager.TYPE_MOBILE &&
          (netSubtype == TelephonyManager.NETWORK_TYPE_EHRPD ||
              netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_B ||
              netSubtype == TelephonyManager.NETWORK_TYPE_HSDPA ||
              netSubtype == TelephonyManager.NETWORK_TYPE_HSPA ||
              netSubtype == TelephonyManager.NETWORK_TYPE_HSUPA ||
              netSubtype == TelephonyManager.NETWORK_TYPE_UMTS)) {
        sConnectionType = ConnectionType.THREE_G.getConnectionType();
      } else if (netType == ConnectivityManager.TYPE_MOBILE &&
          (netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
              netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_A)) {
        sConnectionType = ConnectionType.THREE_C.getConnectionType();
      } else if (netType == ConnectivityManager.TYPE_MOBILE &&
          netSubtype == TelephonyManager.NETWORK_TYPE_CDMA) {
        sConnectionType = ConnectionType.TWO_C.getConnectionType();
      } else if (netType == ConnectivityManager.TYPE_MOBILE) {
        sConnectionType = ConnectionType.TWO_G.getConnectionType();
      }
    } catch (Exception e) {
    }

    return sConnectionType;
  }

  private static String getCellId() {
    TelephonyManager telMgr = null;
    Context context = CommonUtils.getApplication();
    String strMCC = EMPTY_STRING;
    String strMNC = EMPTY_STRING;
    try {
      telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (telMgr != null) {
        CellLocation.requestLocationUpdate();
        String strCellId = EMPTY_STRING;
        String strLAC = EMPTY_STRING;
        String strSID = EMPTY_STRING;
        String strBID = EMPTY_STRING;
        String strNID = EMPTY_STRING;
        String strNetworkType = "Gsm";
        if (TelephonyManager.PHONE_TYPE_CDMA == telMgr.getPhoneType()) {

          /**
           * Get telephony relevant data for CDMA Phones
           */

          ServiceState serviceState = new ServiceState();
          strMCC = "" + serviceState.getOperatorNumeric();
          // Returns the MCC + 00. so remove last two chars
          strMCC = strMCC.substring(0, strMCC.length() - 2);

          /**
           * CDMA
           *
           * TelephonyManager.getSimOperator() In non "world phones",
           * this returns the first 6 digits of the IMSI (e.g.
           * 3100004). This is bogus, because CDMA IMSI's have 00 for
           * the MNC which 2 digits not 3 and useless either way. The
           * 4 on the end is actually the beginning of the subscriber
           * ID. This is a bug in Android as far as I can tell. In
           * world phones, it will probably return the MCC +MNC for an
           * operator in Europe.
           *
           * ServiceState.getOperatorNumeric() Returns the MCC + 00.
           * This is mostly useless, because it does not identify the
           * carrier.
           *
           * CdmaCellLocation.getSystemId() Returns the System ID of
           * the current cell cite (this identifies the operator that
           * owns the cell site). It is analogous to MNC.
           *
           * CdmaCellLocation.getNetworkId() Returns the Network ID of
           * the current cell site. It is analogous to LAC.
           *
           * CdmaCellLocation.getBaseStationId() Returns the Base
           * Station ID of the current cell site, It is analogous to
           * CID.
           *
           * What is missing is a CDMA equivalent of
           * TelephonyManager.getSimOperator () that returns the home
           * SID.
           */
          strNetworkType = "Cdma";
          if (ActivityCompat.checkSelfPermission(context,
              Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telMgr.getCellLocation();
            strSID = "" + cdmaCellLocation.getSystemId();
            strNID = "" + cdmaCellLocation.getNetworkId();
            strBID = "" + cdmaCellLocation.getBaseStationId();
          }
        } else {
          /***
           * this is a fallBack for Getting telephony relevant data
           * for GSM, possible chances are with GSM/CDMA phones
           */
          if (ActivityCompat.checkSelfPermission(context,
              Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) telMgr.getCellLocation();
            strCellId = "" + gsmCellLocation.getCid();
            strLAC = "" + gsmCellLocation.getLac();
            String strIMSI = telMgr.getNetworkOperator();
            if (strIMSI != null && strIMSI.length() >= 3) {
              strMCC = strIMSI.substring(0, 3);
              strMNC = strIMSI.substring(3, strIMSI.length());
            }
          }
        }
        if (strCellId.contains("-")) {
          strCellId = EMPTY_STRING;
        }

        if (strMCC.contains("-")) {
          strMCC = EMPTY_STRING;
        }

        if (strMNC.contains("-")) {
          strMNC = EMPTY_STRING;
        }

        if (strLAC.contains("-")) {
          strLAC = EMPTY_STRING;
        }

        if (strBID.contains("-")) {
          strBID = EMPTY_STRING;
        }

        if (strSID.contains("-")) {
          strSID = EMPTY_STRING;
        }

        if (strNID.contains("-")) {
          strNID = EMPTY_STRING;
        }
        return strCellId + "-" + strMCC + "-"
            + strMNC + "-" + strLAC + "-" + strBID + "-" + strSID
            + "-" + strNID + "-" + strNetworkType;
      }
    } catch (Exception e) {
    }

    return null;
  }

  /**
   * Check if the connection is fast.
   *
   * @return returns if you are on slow or fast network. In case of unknown, defaulting to fast
   */
  public static boolean isConnectionFast(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
    // No network connectivity then NetworkInfo becomes null. So below condition is applied.
    if (info == null || info.getType() == ConnectivityManager.TYPE_WIFI) {
      return true;
    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
      switch (info.getSubtype()) {
        case TelephonyManager.NETWORK_TYPE_1xRTT:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_CDMA:
          return false; // ~ 14-64 kbps
        case TelephonyManager.NETWORK_TYPE_EDGE:
          return false; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
          return true; // ~ 400-1000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
          return true; // ~ 600-1400 kbps
        case TelephonyManager.NETWORK_TYPE_GPRS:
          return false; // ~ 100 kbps
        case TelephonyManager.NETWORK_TYPE_HSDPA:
          return true; // ~ 2-14 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPA:
          return true; // ~ 700-1700 kbps
        case TelephonyManager.NETWORK_TYPE_HSUPA:
          return true; // ~ 1-23 Mbps
        case TelephonyManager.NETWORK_TYPE_UMTS:
          return true; // ~ 400-7000 kbps
      /*
       * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
        case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
          return true; // ~ 1-2 Mbps
        case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
          return true; // ~ 5 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
          return true; // ~ 10-20 Mbps
        case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
          return false; // ~25 kbps
        case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
          return true; // ~ 10+ Mbps
        // Unknown or default - making it TRUE
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        default:
          return true;
      }
    } else {
      return true;
    }
  }

  /**
   * Get IP address from first non-localhost interface
   *
   * @return address or empty string
   */
  public static String getIPAddress() {
    Enumeration<NetworkInterface> en = null;
    try {
      en = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
    }

    if (en == null) {
      return Constants.EMPTY_STRING;
    }

    while (en.hasMoreElements()) {
      NetworkInterface intf = en.nextElement();
      Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
      while (enumIpAddr.hasMoreElements()) {
        InetAddress inetAddress = enumIpAddr.nextElement();
        if (!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress() &&
            inetAddress instanceof Inet4Address && isPublicIP(inetAddress)) {
          return inetAddress.getHostAddress();
        }
      }
    }

    return Constants.EMPTY_STRING;
  }

  /**
   * Ref: https://www.arin.net/knowledge/address_filters.html
   */
  private static boolean isPublicIP(InetAddress inetAddress) {
    String ipAddress = inetAddress.getHostAddress();
    return !(ipAddress.startsWith("10.") || ipAddress.startsWith("172.") ||
        ipAddress.startsWith("192.168"));
  }

  public static String getMacAddress(Context context) {
    return Constants.EMPTY_STRING;
  }

  public static String getWifiSsid() {
    try {
      WifiManager wifiManager = (WifiManager) CommonUtils.getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      WifiInfo info = wifiManager.getConnectionInfo ();
      String ssid = info.getSSID();
      if (!CommonUtils.isEmpty(ssid)) {
        return PasswordEncryption.encrypt(ssid);
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return null;
  }
}
