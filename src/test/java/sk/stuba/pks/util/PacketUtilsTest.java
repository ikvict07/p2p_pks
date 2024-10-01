package sk.stuba.pks.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}