package com.example.netty.java;

/**
 * 描述:
 * 创建者：pjh
 * 创建日期：2020/10/10
 * UpdateUser:更新者
 * 更新时间：2020/10/10
 */
public class DiscoveryClient extends MulticastSender {

    public final static String  MULTICAST_NAME = "device_discovery";
    private String deviceType = "mobile";
    private String deviceUser = "_undefined";

    public DiscoveryClient() throws Exception {
        super();
        super.init();
    }

    @Override
    public String getSendData() {
        return MULTICAST_NAME+"&"+deviceType+"&"+deviceUser;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceUser() {
        return deviceUser;
    }

    public void setDeviceUser(String deviceUser) {
        this.deviceUser = deviceUser;
    }
}
