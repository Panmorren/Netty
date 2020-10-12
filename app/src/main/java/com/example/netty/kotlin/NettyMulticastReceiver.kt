package com.example.netty.kotlin

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.netty.java.ChannelPipelineFactory
import com.example.netty.utils.IPUtils
import com.example.netty.java.MulticastEndpoint
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.ipfilter.IpFilterRule
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
abstract class NettyMulticastReceiver : NettyMulticastEndpoint() {
    private var deviceIp: String? = null
    private var firewall: Set<IpFilterRule>? = null
    abstract fun onReceive(remoteIp: String?, receivedData: String?)
    @Throws(Exception::class)
    fun init() {
        if (deviceIp == null) {
            deviceIp = IPUtils.getDeviceIp()
            super.setBindAddress(deviceIp)
        }
        val factory: ChannelPipelineFactory<Channel?> = object : ChannelPipelineFactory<Channel?>() {
            @Throws(Exception::class)
            override fun getPipeline(): HandlerList {
                val pipeline = HandlerList()
                pipeline.addLast("custom", object : SimpleChannelInboundHandler<DatagramPacket>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Throws(Exception::class)
                    override fun messageReceived(ctx: ChannelHandlerContext, msg: DatagramPacket) {
                        val receivedData = getStringMessage(msg.content())
                        val remoteAddress = msg.sender()
                        if (receivedData == null || remoteAddress == null) {
                            return
                        }
                        if (!validate(msg.content(), remoteAddress)) return
                        onReceive(remoteAddress.hostString, receivedData)
                    }
                })
                return pipeline
            }
        }
        super.init(factory)
    }

    fun setIpSet(ipSet: Set<IpFilterRule>?) {
        firewall = ipSet
    }

    private fun validate(e: ByteBuf, socketAddress: SocketAddress): Boolean {
        return if (firewall == null) true else {
            val iterator = firewall!!.iterator()
            var ipFilterRule: IpFilterRule? = null
            while (iterator.hasNext()) {
                ipFilterRule = iterator.next()
                if (!ipFilterRule.matches(socketAddress as InetSocketAddress)) {
                    return false
                }
            }
            true
        }
    }
}

