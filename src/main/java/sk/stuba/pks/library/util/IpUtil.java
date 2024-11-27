package sk.stuba.pks.library.util;

import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IpUtil {

    public static String getIp() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return getIpWindows();
        }
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.isMCGlobal()) {
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

    public static String getIpWindows() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "ipconfig");
            Process process = processBuilder.start();
            List<String> ips = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("IPv4 Address")) {
                        ips.add(line.trim().split(":")[1].trim());
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Command execution failed with exit code: " + exitCode);
            }
            return ips.getLast();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("no ip was found");
    }
}

