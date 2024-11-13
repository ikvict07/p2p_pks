package sk.stuba.pks.library.util;

import sk.stuba.pks.library.dto.Packet;

import java.util.Arrays;
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

    public static int bytesToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = result << 8 | (b & 0xFF);
        }
        return result;
    }

    public static int byteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Byte array must be of length 4");
        }
        return (bytes[0] & 0xFF) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8  |
                (bytes[3] & 0xFF);
    }

    public static byte[] incrementSequenceNumber(byte[] sequenceNumber) {
        byte[] incrementedSequenceNumber = Arrays.copyOf(sequenceNumber, sequenceNumber.length);
        for (int i = incrementedSequenceNumber.length - 1; i >= 0; i--) {
            if (incrementedSequenceNumber[i] == (byte) 0xFF) {
                incrementedSequenceNumber[i] = 0;
            } else {
                incrementedSequenceNumber[i]++;
                break;
            }
        }
        return incrementedSequenceNumber;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {(byte) (value >> 8), (byte) value};
    }
}
