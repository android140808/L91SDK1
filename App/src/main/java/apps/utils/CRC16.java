package apps.utils;

/**
 * Created by Administrator on 2015/12/14
 */
public class CRC16 {
    /**
     * @param buf
     * @return
     */
    public static int getCRC(String buf) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (int j = 0; j < buf.length(); j++) {
            char b = buf.charAt(j);
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return crc;
    }

    /**
     * @param buf
     * @return
     */
    public static String getCRCString(String buf) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (int j = 0; j < buf.length(); j++) {
            char b = buf.charAt(j);
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        String str = "" + (char) (crc / 256) + (char) (crc % 256);
        return str;
    }

    public static int getCRC(byte[] buffers) {
        char crc = 0xFFFF;          // initial value
        char polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)
        for (int j = 0; j < buffers.length; ++j) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((buffers[j] >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;
    }
}
