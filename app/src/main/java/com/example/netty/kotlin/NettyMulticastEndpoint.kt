package com.example.netty.kotlin

import android.util.Log
import com.example.netty.java.ChannelPipelineFactory
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.oio.OioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.oio.OioDatagramChannel
import io.netty.util.ReferenceCountUtil
import io.netty.util.internal.logging.InternalLogger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.util.*

/**
 *描述:组播父类
 *创建者：pjh
 *创建日期：2020/10/10
 *UpdateUser:更新者
 *更新时间：2020/10/10
 */
open class NettyMulticastEndpoint {
    private var mcastGroupIp = "224.0.0.3" //组播地址 路由地址 所有成员共享
    private var mcastGroupPort = 12343 //组播端口
    private var bindAddress: String? = null //本机IP地址
    private var datagramChannel: DatagramChannel? = null
    private var connectionlessBootstrap: Bootstrap? = null
    private var multicastAddress: InetSocketAddress? = null
    var id: ByteArray? = null
    var init = false
    var group: EventLoopGroup? = null
    var debugs = false
    private var logger: InternalLogger? = null
    private var multicast = true // 是否开启组播
    @Throws(Exception::class)
    fun init(factory: ChannelPipelineFactory<Channel?>) {
        // 生成通道ID
        id = String.format("%1$020d", Math.abs(Random(System.currentTimeMillis()).nextLong()))
            .toByteArray()
        group = OioEventLoopGroup()
        connectionlessBootstrap = Bootstrap()
        connectionlessBootstrap!!.group(group)
        connectionlessBootstrap!!.option(ChannelOption.SO_BROADCAST, true)
        connectionlessBootstrap!!.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true)
        connectionlessBootstrap!!.option(ChannelOption.IP_MULTICAST_TTL, 8)
        connectionlessBootstrap!!.option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 2)
        connectionlessBootstrap!!.handler(factory)
        connectionlessBootstrap!!.channel(OioDatagramChannel::class.java)
        multicastAddress = InetSocketAddress(mcastGroupIp, mcastGroupPort)

        //创建管道
        datagramChannel = connectionlessBootstrap!!.bind(InetSocketAddress(mcastGroupPort)).sync()
            .channel() as DatagramChannel
        val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddress))
        //将自己加入组播
        datagramChannel!!.joinGroup(multicastAddress, networkInterface)
        init = true
        if (debugs) factory.debug()
    }

    fun isInit(): Boolean {
        return init
    }

    fun setDebug(debug: Boolean) {
        this.debugs = debug
    }

    fun setLogger(logger: InternalLogger?) {
        this.logger = logger
    }

    @Throws(Exception::class)
    fun send(msg: String) {
        val msgBuf = Unpooled.wrappedBuffer(msg.toByteArray())
        send(msgBuf)
    }

    /**
     * 发送组播消息
     *
     * @param msg
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun send(msg: ByteBuf) {
        val arr = msg.array()
        val buf = ByteArray(arr.size + id!!.size)
        System.arraycopy(id, 0, buf, 0, id!!.size)
        System.arraycopy(arr, 0, buf, id!!.size, arr.size)
        val bbuf = Unpooled.wrappedBuffer(buf)
        Log.i("UDP", "udp send: " + String(bbuf.array()))
        if (!group!!.isShutdown && datagramChannel!!.isOpen) {
            datagramChannel!!.writeAndFlush(DatagramPacket(bbuf, multicastAddress))
        }
        ReferenceCountUtil.release(msg)
        // datagramChannel.writeAndFlush(buf, multicastAddress);
    }

    fun getMcastGroupIp(): String {
        return mcastGroupIp
    }

    fun getMcastGroupPort(): Int {
        return mcastGroupPort
    }

    fun getBindAddress(): String? {
        return bindAddress
    }

    fun setMcastGroupIp(mcastGroupIp: String) {
        this.mcastGroupIp = mcastGroupIp
    }

    fun setMcastGroupPort(mcastGroupPort: Int) {
        this.mcastGroupPort = mcastGroupPort
    }

    fun setBindAddress(bindAddress: String?) {
        this.bindAddress = bindAddress
    }

    /**
     * 关闭通道
     */
    fun close() {
        datagramChannel!!.close()
        try {
            group!!.shutdownGracefully().sync()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 获取消息体
     *
     * @param e
     * @return
     */
    fun getMessage(e: ByteBuf): ByteBuf? {
        return if (checkMessage(e)) {
            e.slice(id!!.size, e.readableBytes() - id!!.size)
        } else null
    }

    /**
     * 获取消息体文本
     *
     * @param e
     * @return
     */
    fun getStringMessage(e: ByteBuf): String? {
        val m = getMessage(e) ?: return null
        //		ReferenceCountUtil.release(m);
        return m.toString(Charset.defaultCharset())
    }

    /**
     * 检测消息体中的ID是否与当前通道的ID匹配
     *
     * @param e
     * @return
     */
    fun checkMessage(e: ByteBuf): Boolean {
        val eId = ByteArray(id!!.size)
        e.getBytes(0, eId, 0, eId.size)
        return !Arrays.equals(id, eId)
    }

    fun isMulticast(): Boolean {
        return multicast
    }

    fun setMulticast(multicast: Boolean) {
        this.multicast = multicast
    }
}
