package com.ccdt.easyble.gatt.callback;


import com.ccdt.easyble.BleDevice;

public interface BleWriteByBatchCallback extends BleCallback {
    void writeByBatchSuccess(byte[] data, BleDevice device);
}
