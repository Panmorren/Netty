package com.example.netty.kotlin

import android.util.Log
import com.example.netty.java.ChannelPipelineFactory
import com.example.netty.utils.IPUtils
import com.example.netty.java.MulticastEndpoint
import com.example.netty.utils.ThreadPoolUtils
import com.safframework.log.L
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
abstract class NettyMulticastSender : NettyMulticastEndpoint() {
    private var stop = false
    private var deviceIp: String? = null
    private var period: Long = 3000
    private val executor = ThreadPoolUtils.newSingleThreadExecutor("MulticastSender ThreadPool")
    @Throws(Exception::class)
    fun init() {
        deviceIp = IPUtils.getDeviceIp()
        L.e("init: $deviceIp")
        super.setBindAddress(deviceIp)
        val factory: ChannelPipelineFactory<Channel?> = object : ChannelPipelineFactory<Channel?>() {
            @Throws(Exception::class)
            override fun getPipeline(): HandlerList {
                val pipeline = HandlerList()
                pipeline.addLast(
                    "simpleHandler",
                    object : SimpleChannelInboundHandler<DatagramPacket?>() {
                        @Throws(Exception::class)
                        override fun messageReceived(
                            ctx: ChannelHandlerContext,
                            msg: DatagramPacket?
                        ) {
                        L.e(msg.toString())
                        }
                    })
                return pipeline
            }
        }
        super.init(factory)
    }

    /**
     * 获得需要发送的组播数据
     * @return
     */
    abstract fun getSendData(): String?

    /**
     * 开始发送组播数据
     * @throws Exception
     */
    @Throws(Exception::class)
    fun start() {
        stop = false
        executor.execute {
            while (!stop) {
                try {
                    val sendData = getSendData()
                    sendData?.let { send(it) }
                } catch (e: Exception) {
                    L.e(e.toString())
                }
                try {
                    Thread.sleep(period)
                } catch (e: InterruptedException) {
                    L.e(e.toString())
                }
            }
        }
    }

    /**
     * 停止发送组播数据
     */
    fun stop() {
        stop = true
        executor.shutdown()
        super.close()
    }

    fun getPeriod(): Long {
        return period
    }

    fun setPeriod(period: Long) {
        this.period = period
    }
}

