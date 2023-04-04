package com.ccdt.easyble.gatt.callback;


import com.ccdt.easyble.BleDevice;

public interface BleRssiCallback extends BleCallback {

    void onRssi(int rssi, BleDevice bleDevice);
}
