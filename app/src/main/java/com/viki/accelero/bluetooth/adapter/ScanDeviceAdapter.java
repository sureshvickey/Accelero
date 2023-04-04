package com.viki.accelero.bluetooth.adapter;

import android.content.Context;

import android.util.SparseArray;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.ccdt.easyble.BleDevice;
import com.viki.accelero.R;

import java.util.List;


public class ScanDeviceAdapter extends CommonRecyclerViewAdapter<BleDevice> {

    public ScanDeviceAdapter(@NonNull Context context, @NonNull List<BleDevice> dataList, @NonNull SparseArray<int[]> resLayoutAndViewIds) {
        super(context, dataList, resLayoutAndViewIds);
    }

    @Override
    public int getItemResLayoutType(int position) {
        return R.layout.item_rv_scan_devices;
    }

    @Override
    public void bindDataToItem(CommonRecyclerViewAdapter.MyViewHolder holder, BleDevice data, int position) {
        TextView tvName = (TextView) holder.mViews.get(R.id.tv_name);
        TextView tvAddress = (TextView) holder.mViews.get(R.id.tv_address);
        tvName.setText(data.name);
        tvAddress.setText(data.address);
    }
}
