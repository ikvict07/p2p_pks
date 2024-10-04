package sk.stuba.pks.model;

import org.junit.jupiter.api.Test;
import sk.stuba.pks.service.mapping.JsonService;
import sk.stuba.pks.util.IpUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.*;

class MessageWrapperTest {

    @Test
    void test() throws UnknownHostException {
        Message message = JsonService.parseJson(
                //json
                """
                {
                    "type": "syn",
                    "port": 8080,
                    "address": "localhost"
                }
                """);

        System.out.println(message);
        System.out.println(IpUtil.getIp());

    }


}