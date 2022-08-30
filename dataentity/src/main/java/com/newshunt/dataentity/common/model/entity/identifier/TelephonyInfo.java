package com.newshunt.dataentity.common.model.entity.identifier;

import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by amardeep.kumar on 6/30/2016.
 */
public final class TelephonyInfo {

    private List<SimInfo> mSimInfoList;
    private final HashSet<String> mImeiDictionary;
    private final HashSet<String> mSimSubscriberDictionary;
    private final HashSet<String> mSimSerialDictionary;

    public TelephonyInfo() {
        mSimInfoList = new ArrayList<>(3);
        mImeiDictionary = new HashSet<>();
        mSimSubscriberDictionary = new HashSet<>();
        mSimSerialDictionary = new HashSet<>();
    }

    private static String getSubscriberIdBySlot(TelephonyManager telephony, String predictedMethodName, int slotID)
            throws GeminiMethodNotFoundException {
        String simSubscriberId = null;
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                simSubscriberId = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return simSubscriberId;
    }

    private static String getSimSerialBySlot(TelephonyManager telephony, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        String simSerialNumber = null;
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                simSerialNumber = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return simSerialNumber;
    }

    private static String getDeviceIdBySlot(TelephonyManager telephony, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        String imsi = null;
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                imsi = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imsi;
    }

    public List<SimInfo> getSimInfoList() {
        return mSimInfoList;
    }

    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }
}