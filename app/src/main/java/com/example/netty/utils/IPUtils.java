package com.example.netty.utils;

import android.util.Log;


import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtils {

	/**
	 * 获得当前设备的IP
	 * @return
	 */
	public static String getDeviceIp() {
		String result = null;
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				NetworkInterface ne = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> e2 = ne.getInetAddresses();

				while (e2.hasMoreElements()) {
					InetAddress ia = (InetAddress) e2.nextElement();

					if (!ia.isAnyLocalAddress() && !ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
							&& !ia.isMulticastAddress())
						if (result == null || !ia.isSiteLocalAddress()) {
							result = ia.getHostAddress();
						}
				}
			}
		} catch (Exception ex) {
			//Constants.ahessianLogger.warn("", ex);
		}
		return result;

	}

	public static String getHostIP() {
		String hostIp = null;
		try {
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			InetAddress ia = null;
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					ia = ias.nextElement();
					if (ia instanceof Inet6Address) {
						continue;// skip ipv6
					}
					String ip = ia.getHostAddress();
					if (!"127.0.0.1".equals(ip)) {
						hostIp = ia.getHostAddress();
						break;
					}
				}
			}
		} catch (SocketException e) {
			Log.i("yao", "SocketException");
			e.printStackTrace();
		}
		return hostIp;

	}
	
}
