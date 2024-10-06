package sk.stuba.pks.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.DatagramSocket;

@Configuration
public class SocketConfig {

    @Value("${listenPort}")
    private int listenPort;

    @Bean
    public DatagramSocket datagramSocket() {
        try {
            System.out.println("Creating DatagramSocket on port " + listenPort);
            return new DatagramSocket(listenPort);
        } catch (Exception e) {
            System.out.println("Failed to create DatagramSocket");
            throw new RuntimeException("Failed to create DatagramSocket", e);
        }
    }
}
