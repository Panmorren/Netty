package com.example.netty.java;

import android.util.Log;

import com.example.netty.java.ChannelPipelineFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;

/**
 * 描述:
 * 创建者：pjh
 * 创建日期：2020/10/10
 * UpdateUser:更新者
 * 更新时间：2020/10/10
 */
public class MulticastEndpoint {
    private String mcastGroupIp = "224.0.0.3";//组播地址 路由地址 所有成员共享
    private int mcastGroupPort = 12343;//组播端口
    private String bindAddress;//本机IP地址

    private DatagramChannel datagramChannel;
    private Bootstrap connectionlessBootstrap;
    private InetSocketAddress multicastAddress;
    byte[] id;//组播通道消息ID
    boolean init = false;
    EventLoopGroup group;
    public boolean debug = false;
    public InternalLogger logger;
    private boolean multicast = true; // 是否开启组播

    public void init(ChannelPipelineFactory<Channel> factory) throws Exception {
        // 生成通道ID
        id = String.format("%1$020d", Math.abs(new Random(System.currentTimeMillis()).nextLong())).getBytes();

        group = new OioEventLoopGroup();
        connectionlessBootstrap = new Bootstrap();
        connectionlessBootstrap.group(group);
        connectionlessBootstrap.option(ChannelOption.SO_BROADCAST, true);
        connectionlessBootstrap.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
        connectionlessBootstrap.option(ChannelOption.IP_MULTICAST_TTL, 8);
        connectionlessBootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 2);
        connectionlessBootstrap.handler(factory);
        connectionlessBootstrap.channel(OioDatagramChannel.class);
        multicastAddress = new InetSocketAddress(mcastGroupIp, mcastGroupPort);

        //创建管道
        datagramChannel = (DatagramChannel) connectionlessBootstrap.bind(new InetSocketAddress(mcastGroupPort)).sync()
                .channel();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddress));
        //将自己加入组播
        datagramChannel.joinGroup(multicastAddress, networkInterface);

        init = true;
        if (debug)
            factory.debug();
    }

    public boolean isInit() {
        return init;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setLogger(InternalLogger logger) {
        this.logger = logger;
    }

    public void send(String msg) throws Exception {
        ByteBuf msgBuf = Unpooled.wrappedBuffer(msg.getBytes());
        send(msgBuf);
    }

    /**
     * 发送组播消息
     *
     * @param msg
     * @throws Exception
     */
    private void send(ByteBuf msg) throws Exception {

        byte[] arr = msg.array();
        byte[] buf = new byte[arr.length + id.length];
        System.arraycopy(id, 0, buf, 0, id.length);
        System.arraycopy(arr, 0, buf, id.length, arr.length);

        ByteBuf bbuf = Unpooled.wrappedBuffer(buf);

        Log.i("UDP", "udp send: " + new String(bbuf.array()));

        if (!group.isShutdown() && datagramChannel.isOpen()) {
            datagramChannel.writeAndFlush(new DatagramPacket(bbuf, multicastAddress));
        }

        ReferenceCountUtil.release(msg);
        // datagramChannel.writeAndFlush(buf, multicastAddress);
    }

    public String getMcastGroupIp() {
        return mcastGroupIp;
    }

    public int getMcastGroupPort() {
        return mcastGroupPort;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setMcastGroupIp(String mcastGroupIp) {
        this.mcastGroupIp = mcastGroupIp;
    }

    public void setMcastGroupPort(int mcastGroupPort) {
        this.mcastGroupPort = mcastGroupPort;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * 关闭通道
     */
    public void close() {
        datagramChannel.close();
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取消息体
     *
     * @param e
     * @return
     */
    public ByteBuf getMessage(ByteBuf e) {
        if (checkMessage(e)) {
            return e.slice(id.length, e.readableBytes() - id.length);
        }
        return null;
    }

    /**
     * 获取消息体文本
     *
     * @param e
     * @return
     */
    public String getStringMessage(ByteBuf e) {
        ByteBuf m = getMessage(e);
        if (m == null)
            return null;
        String ret = m.toString(Charset.defaultCharset());
//		ReferenceCountUtil.release(m);
        return ret;
    }

    /**
     * 检测消息体中的ID是否与当前通道的ID匹配
     *
     * @param e
     * @return
     */
    public boolean checkMessage(ByteBuf e) {
        byte[] eId = new byte[id.length];
        e.getBytes(0, eId, 0, eId.length);
        return (!Arrays.equals(id, eId));
    }

    public boolean isMulticast() {
        return multicast;
    }

    public void setMulticast(boolean multicast) {
        this.multicast = multicast;
    }
}
