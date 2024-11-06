package sk.stuba.pks.old.util;

import lombok.val;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        try (HttpClient client = HttpClient.newBuilder().build();) {
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://checkip.amazonaws.com"))
                    .build();

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().trim();
        } catch (Exception e) {
            throw e;
        }
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