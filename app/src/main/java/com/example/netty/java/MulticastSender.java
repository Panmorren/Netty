package com.example.netty.java;

import android.util.Log;

import com.example.netty.utils.IPUtils;
import com.example.netty.utils.ThreadPoolUtils;

import java.util.concurrent.ExecutorService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * 描述:
 * 创建者：pjh
 * 创建日期：2020/10/10
 * UpdateUser:更新者
 * 更新时间：2020/10/10
 */
public abstract class MulticastSender extends MulticastEndpoint{
    private static final String TAG = "MulticastSender";
    private boolean stop = false;
    private String deviceIp;
    private long period = 3000;

    private ExecutorService executor = ThreadPoolUtils.newSingleThreadExecutor("MulticastSender ThreadPool");

    public void init() throws Exception {
        deviceIp = IPUtils.getDeviceIp();
        Log.e(TAG, "init: "+deviceIp);
        super.setBindAddress(deviceIp);
        ChannelPipelineFactory<Channel> factory = new ChannelPipelineFactory<Channel>() {
            public HandlerList getPipeline() throws Exception {
                HandlerList pipeline = new HandlerList();
                pipeline.addLast("simpleHandler", new SimpleChannelInboundHandler<DatagramPacket>(){
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
//                        Log.i("UPD sender",msg.toString());
                    }
                });
                return pipeline;
            }
        };
        super.init(factory);
    }

    /**
     * 获得需要发送的组播数据
     * @return
     */
    public abstract String getSendData();

    /**
     * 开始发送组播数据
     * @throws Exception
     */
    public void start() throws Exception {
        stop = false;
        executor.execute(new Runnable() {
            public void run() {
                while (!stop) {
                    try {
                        String sendData = getSendData();
                        if(sendData != null) {
                            send(sendData);
                        }
                    } catch (Exception e) {
                        //Constants.ahessianLogger.warn("", e);
                    }
                    try {
                        Thread.sleep(period);
                    } catch (InterruptedException e) {
                        //Constants.ahessianLogger.warn("", e);
                    }
                }
            }
        });
    }

    /**
     * 停止发送组播数据
     */
    public void stop() {
        stop = true;
        executor.shutdown();
        super.close();
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }
}

