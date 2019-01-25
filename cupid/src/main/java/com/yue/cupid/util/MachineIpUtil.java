package com.yue.cupid.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class MachineIpUtil {

    private static final String LOG_PREFIX = "[MachineIpUtil]";
    /**
     * 本机ip.
     */
    private volatile static String host;

    /**
     * 不可被实例化
     */
    private MachineIpUtil() {
    }

    /**
     * 获取本机IP
     *
     * @return 本机IP
     */
    public static String getHost() {
        if (host == null) {
            synchronized (MachineIpUtil.class) {
                if (host == null) {
                    List<String> hostAddresses = new LinkedList<>();
                    try {
                        for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements(); ) {
                            for (Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                                InetAddress inetAddress = inetAddresses.nextElement();
                                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                                    hostAddresses.add(inetAddress.getHostAddress());
                                }
                            }
                        }
                        host = String.join(";", hostAddresses);
                    } catch (SocketException ex) {
                        log.error("{} Get host error", LOG_PREFIX, ex);
                        host = "Unknown IP";
                    }
                }
            }
        }
        return host;
    }
}
