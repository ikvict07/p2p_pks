package sk.stuba.pks.model;

import org.junit.jupiter.api.Test;
import sk.stuba.pks.service.mapping.JsonService;

import static org.junit.jupiter.api.Assertions.*;

class MessageWrapperTest {

    @Test
    void test() {
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
    }
}