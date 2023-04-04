package com.ccdt.easyble.gatt.callback;


import com.ccdt.easyble.BleDevice;

public interface BleReadCallback extends BleCallback {
    void onReadSuccess(byte[] data, BleDevice device);
}
