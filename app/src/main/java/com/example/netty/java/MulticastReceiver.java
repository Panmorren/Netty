package com.example.netty.java;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.netty.utils.IPUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.ipfilter.IpFilterRule;

/**
 * 描述:
 * 创建者：pjh
 * 创建日期：2020/10/10
 * UpdateUser:更新者
 * 更新时间：2020/10/10
 */
public abstract class MulticastReceiver extends MulticastEndpoint{

    private static final String TAG = "MulticastReceiver";

    private String deviceIp;

    private Set<IpFilterRule> firewall;

    public abstract void onReceive(String remoteIp,String receivedData);

    public void init() throws Exception {
        if (deviceIp == null) {
            deviceIp = IPUtils.getDeviceIp();
            super.setBindAddress(deviceIp);
        }
        ChannelPipelineFactory<Channel> factory = new ChannelPipelineFactory<Channel>() {
            public HandlerList getPipeline() throws Exception {
                HandlerList pipeline = new HandlerList();
                pipeline.addLast("custom",new SimpleChannelInboundHandler<DatagramPacket>(){
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        String receivedData = getStringMessage(msg.content());
                        InetSocketAddress remoteAddress = msg.sender();

                        if (receivedData == null || remoteAddress == null) {
                            return;
                        }
                        if (!validate(msg.content(), remoteAddress))
                            return;
                        onReceive(remoteAddress.getHostString(),receivedData);
                    }
                });
                return pipeline;
            }

        };
        super.init(factory);
    }

    public void setIpSet(Set<IpFilterRule> ipSet) {
        this.firewall = ipSet;
    }

    private boolean validate(ByteBuf e, SocketAddress socketAddress) {
        if (firewall == null)
            return true;
        else {
            Iterator<IpFilterRule> iterator = firewall.iterator();
            IpFilterRule ipFilterRule = null;
            while (iterator.hasNext()) {
                ipFilterRule = iterator.next();
                if(!ipFilterRule.matches((InetSocketAddress) socketAddress)) {
                    return false;
                }
            }
            // No limitation founds and no allow either, but as it is like
            // Firewall rules, it is therefore accepted
            if (debug && logger != null)
                logger.info("DiscoverServer no firewall: ");
            return true;
        }
    }
}

