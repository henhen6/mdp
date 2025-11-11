package com.gitee.sop.support.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author 六如
 */
public class NetUtil {

    /**
     * 获取本机的内网ip地址
     *
     * @return 返回内网ip
     * @throws SocketException 异常
     */
    public static String getIntranetIp() throws SocketException {
        // 本地IP，如果没有配置外网IP则返回它
        String localip = null;
        // 外网IP
        String netip = null;
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        // 是否找到外网IP
        boolean finded = false;
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress()
                    && !ip.isLoopbackAddress()
                    && !ip.getHostAddress().contains(":")) {
                    // 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress()
                           && !ip.isLoopbackAddress()
                           && !ip.getHostAddress().contains(":")) {
                    // 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

}
