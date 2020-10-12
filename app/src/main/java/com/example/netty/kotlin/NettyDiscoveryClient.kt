package com.example.netty.kotlin

import com.example.netty.java.MulticastSender

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
class NettyDiscoveryClient : NettyMulticastSender() {
    private var deviceType = "netty"
    private var deviceUser = "_undefined"
    override fun getSendData(): String {
        return MULTICAST_NAME + "&" + deviceType + "&" + deviceUser
    }

    fun getDeviceType(): String {
        return deviceType
    }

    fun setDeviceType(deviceType: String) {
        this.deviceType = deviceType
    }

    fun getDeviceUser(): String {
        return deviceUser
    }

    fun setDeviceUser(deviceUser: String) {
        this.deviceUser = deviceUser
    }

    companion object {
        const val MULTICAST_NAME = "device_discovery"
    }

    init {
        super.init()
    }
}
