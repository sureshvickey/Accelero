package com.ccdt.easyble.gatt.callback;


import com.ccdt.easyble.BleDevice;

public interface BleWriteCallback extends BleCallback {
    void onWriteSuccess(byte[] data, BleDevice device);
}
