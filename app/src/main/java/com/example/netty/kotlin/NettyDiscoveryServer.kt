package com.example.netty.kotlin

import android.util.Log
import com.example.netty.java.MulticastReceiver
import com.safframework.log.L

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
class NettyDiscoveryServer : NettyMulticastReceiver() {
    override fun onReceive(remoteIp: String?, receivedData: String?) {
        L.e(receivedData)
    }

    init {
        super.init()
    }
}
