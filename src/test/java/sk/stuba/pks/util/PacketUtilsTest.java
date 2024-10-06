package sk.stuba.pks.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PacketUtilsTest {

    @Test
    void bytesToHex() {
    }

    @Test
    void generateChecksum() {
    }

    @Test
    void bytesToInt() {
        // Given
        byte b = (byte) 255;
        // When
        int i = PacketUtils.bytesToInt(new byte[]{b});
        // Then
        assertEquals(255, i);
    }
    public static byte[] intToTwoByteArray(int value) {
        return new byte[] {
                (byte) (value >> 8),
                (byte) value
        };
    }
    @Test
    void t() {
        System.out.println(Arrays.toString(intToTwoByteArray(256)));
    }


    @Test
    void generateMessage() {
        int len = 1600;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(i);
        }
        System.out.println(sb);
    }
}