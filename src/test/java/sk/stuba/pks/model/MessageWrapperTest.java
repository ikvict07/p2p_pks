package sk.stuba.pks.model;

import org.junit.jupiter.api.Test;
import sk.stuba.pks.library.model.Message;
import sk.stuba.pks.library.service.mapping.JsonService;
import sk.stuba.pks.library.util.IpUtil;

import java.net.UnknownHostException;

class MessageWrapperTest {

    @Test
    void test() throws Exception {
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