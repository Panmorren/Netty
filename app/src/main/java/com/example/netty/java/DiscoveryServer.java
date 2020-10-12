package com.example.netty.java;

import android.util.Log;

/**
 * 描述:
 * 创建者：pjh
 * 创建日期：2020/10/10
 * UpdateUser:更新者
 * 更新时间：2020/10/10
 */
public class DiscoveryServer extends MulticastReceiver {
    private static final String TAG = "DiscoveryServer";
    public final static String MULTICAST_NAME = "device_discovery";

    public DiscoveryServer() throws Exception {
        super.init();
    }

    @Override
    public void onReceive(String remoteIp, String receivedData) {
        Log.e(TAG, receivedData);
    }
}
