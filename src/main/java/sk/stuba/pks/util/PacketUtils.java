package sk.stuba.pks.util;

import sk.stuba.pks.dto.Packet;

import java.util.zip.CRC32;

public class PacketUtils {
    private PacketUtils() {
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] generateChecksum(Packet packet) {
        CRC32 crc = new CRC32();
        crc.update(packet.getBytesWithoutChecksum());
        long checksumValue = crc.getValue();
        return new byte[] {
                (byte) (checksumValue >> 8),
                (byte) checksumValue
        };
    }
}
