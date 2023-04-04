package com.ccdt.easyble.gatt.callback;


import com.ccdt.easyble.BleDevice;


public interface BleMtuCallback extends BleCallback {
    void onMtuChanged(int mtu, BleDevice device);
}
