package com.viki.accelero.bluetooth.utils;


import com.viki.accelero.OperateActivity;

import java.util.HashMap;
public class ByteUtils {
    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            b.append(String.format("%02x", bytes[i] & 0xFF));
        }
        return b.toString();
    }

    public static byte[] hexStr2Bytes(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String getUuidName(String uuid){
        HashMap<String,String> uuidArr=new HashMap<String,String>();
        uuidArr.put(OperateActivity.SERVICE_LEVEL_UUID.toString(),"SERVICE UUID");
        uuidArr.put(OperateActivity.ACCESS_LEVEL_UUID.toString(),"ACCESS LEVEL UUID");
        uuidArr.put(OperateActivity.ACCESS_KEY_UUID.toString(),"ACCESS KEY UUID");
        uuidArr.put(OperateActivity.SPEED_UUID.toString(),"SPEED UUID");
        uuidArr.put(OperateActivity.CAR_SPEED_UUID.toString(),"CAR MAX SPEED UUID");
        uuidArr.put(OperateActivity.SOC_UUID.toString(),"SOC UUID");
        uuidArr.put(OperateActivity.OP_MODE_UUID.toString(),"OP MODE UUID");
        uuidArr.put(OperateActivity.FNR_SWITCH_UUID.toString(),"FNR SWITCH UUID");
        uuidArr.put(OperateActivity.ODOMETER_UUID.toString(),"ODOMETER UUID");
        uuidArr.put(OperateActivity.FAULT_STATUS_UUID.toString(),"FAULT STATUS UUID");

        uuid = uuidArr.get(uuid);

        return uuid;
    }
}
