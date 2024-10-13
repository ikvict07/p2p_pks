package sk.stuba.pks.model;

import org.junit.jupiter.api.Test;
import sk.stuba.pks.old.model.Message;
import sk.stuba.pks.old.service.mapping.JsonService;
import sk.stuba.pks.old.util.IpUtil;

import java.net.UnknownHostException;

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