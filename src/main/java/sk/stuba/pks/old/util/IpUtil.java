package sk.stuba.pks.old.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class IpUtil {

    public static String getIp() throws Exception {
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        throw new RuntimeException("No suitable private IP address found in local network interfaces.");
    }

    public static void main(String[] args) {
        try {
            String ipAddress = getIp();
            System.out.println("Found IP address: " + ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}