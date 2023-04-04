package com.ccdt.easyble.scan;


import com.ccdt.easyble.BleDevice;

public interface BleScanCallback {
    void onLeScan(BleDevice device, int rssi, byte[] scanRecord);

    void onStart(boolean startScanSuccess, String info);

    void onFinish();
}
