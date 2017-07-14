package apps.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cn.appscomm.pedometer.application.GlobalApp;

public class NumberUtils {

    // 把时间戳（秒），转换为日期格式：2014-0-04-16 13:25
    public static String timeStamp2format(long time_stamp) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String reTime = df.format(time_stamp * 1000L);

        return reTime;
    }


    public static String utcTimeStamp2format(long time_stamp) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String reTime = df.format(time_stamp * 1000L);

        return reTime;
    }

    // byte[] 高低位转换
    public static byte[] byteArrayReverse(byte[] bs) {
        for (int i = 0; i < bs.length / 2; i++) {
            byte temp = bs[i];
            bs[i] = bs[bs.length - 1 - i];
            bs[bs.length - 1 - i] = temp;
        }
        return bs;
    }

    // 2进制的byte[]数组转int类型
    public static int byteToInt(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < b.length; i++) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

    // 2进制的byte[]高低位置换数组转int类型
    public static int byteReverseToInt(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = b.length - 1; i > -1; i--) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

    // int 类型转byte[] 2进制数组
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    // int 类型转byte[] 2进制数组
    public static String bytes2HexString(byte[] b) {


        String ret = "";
        if (b == null) return ret;
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase() + " ";
        }
        return ret;
    }


    // 2进制字符串传化为16进制字符串： "01111111" -->  "7F"
    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp).toUpperCase());
        }
        return tmp.toString();
    }

    // 16进制字符串传化为2进制字符串  : "FF" -->  "01111111"
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     * "1F"-->0b01111111 或 0x1F
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    // 二进制字符串转换成2进制数组 byte[]: "0111111" -->byte[]再换int类型是：127
    public static byte[] binaryStr2Bytes(String binaryByteString) {
        //假设binaryByte 是01，10，011，00以，分隔的格式的字符串 
        String[] binaryStr = binaryByteString.split(",");
        byte[] byteArray = new byte[binaryStr.length];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte) parse(binaryStr[i]);
        }
        return byteArray;
    }

    public static int parse(String str) {
        //32位 为负数 
        if (32 == str.length()) {
            str = "-" + str.substring(1);
            return -(Integer.parseInt(str, 2) + Integer.MAX_VALUE + 1);
        }
        return Integer.parseInt(str, 2);
    }


    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray =
            {"0000", "0001", "0010", "0011",
                    "0100", "0101", "0110", "0111",
                    "1000", "1001", "1010", "1011",
                    "1100", "1101", "1110", "1111"};

    /**
     * @param
     * @return 2进制数组byte[]转换为二进制字符串
     * new byte[]{0b01111111}-->"01111111" ;  new byte[]{0x1F}-->"00011111"
     */
    public static String bytes2BinaryStr(byte[] bArray) {

        String outStr = "";
        int pos = 0;
        for (byte b : bArray) {
            //高四位  
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位  
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;

    }

    /**
     * @param bytes
     * @return 将二进制转换为十六进制字符输出
     * new byte[]{0b01111111}-->"7F" ;  new byte[]{0x2F}-->"2F"
     */
    public static String binaryToHexString(byte[] bytes) {

        String result = "";
        if (bytes == null) {
            return result;
        }
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            //字节高4位  
            hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
            //字节低4位  
            hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     * "1F"-->0b01111111 或 0x1F
     */
    public static byte[] hexStringToBinary(String hexString) {
        //hexString的长度对2取整，作为bytes的长度  
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位  
        byte low = 0;//字节低四位  

        for (int i = 0; i < len; i++) {
            //右移四位得到高位
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算
        }
        return bytes;
    }

    /**
     * Summer:
     * 将字符串转换为UTF-16格式的字节数组（例："ABCD" -> 41 00 42 00 43 00 44 00）
     *
     * @param ori String 源字符串
     * @return byte[] 转换后的字节数组（小端编码，截去开头的FEFF）
     */
    public static byte[] covertStrToUTF16Bytes(String ori) {
        byte[] res = null, temp = null;
        try {
            temp = ori.getBytes("utf-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (temp == null || temp.length <= 2)
            return null;
        res = new byte[temp.length - 2];
        for (int i = 0; i < res.length; i = i + 2) {
            System.out.print(byteToInt(new byte[]{temp[2 + i + 1]}));
            System.out.println(", " + byteToInt(new byte[]{temp[2 + i]}));
//            res[i] = temp[2+i+1];     // Eclipse中需手动转换为小端，此处不用（原因未知）
//            res[i+1] = temp[2+i];
            res[i + 1] = temp[2 + i + 1];
            res[i] = temp[2 + i];
        }
        return res;
    }

    //截取浮点型数据，小数点后两位，不需要四舍五入
    public static String getFormatData(String data) {
        String str = "";
        if (data.length() == 3) {
            data = data + "0";
        }
        if (data.contains(".")) {
            int start = data.indexOf(".");
            try {
                str = data.substring(0, start + 3);
            } catch (Exception e) {

                str = "0";
            }
        }
        return str;
    }

    /**
     * 追加写
     *
     * @param content
     */
    public static void appendContent(String content) {
        try {
            String path = GlobalApp.globalApp.getFilesDir() + "/log.txt";
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(path, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压zip
     *
     * @param context
     * @param path    路径
     */
    public static void unZip(Context context, String path) {
        try {
            ZipInputStream Zin = new ZipInputStream(new FileInputStream(path));// 输入源zip路径
            BufferedInputStream Bin = new BufferedInputStream(Zin);
            String Parent = context.getFilesDir().getAbsolutePath(); // 输出路径（文件夹目录）
            Logger.i("", "path" + Parent);

            File Fout = null;
            ZipEntry entry = null;
            Logger.i("", "path" + path);
//            Logger.i("","path-"+Zin.getNextEntry());
            while ((entry = Zin.getNextEntry()) != null && !entry.isDirectory()) {
                Logger.i("", "path---" + entry);

                Fout = new File(Parent, entry.getName());
                if (!Fout.exists()) {
                    (new File(Fout.getParent())).mkdirs();
                }
                FileOutputStream out = new FileOutputStream(Fout);
                BufferedOutputStream Bout = new BufferedOutputStream(out);
                int b = 0;
                while ((b = Bin.read()) != -1) {
                    Bout.write(b);
                }
                Bout.close();
                out.close();
            }
            Bin.close();
            Zin.close();
        } catch (FileNotFoundException e) {
            Logger.i("", "path" + e);

            e.printStackTrace();
        } catch (IOException e) {
            Logger.i("", "path" + e);

            e.printStackTrace();
        } catch (Exception e) {
            Logger.i("", "path" + e);

            e.printStackTrace();
        }
    }

    /**
     * crc16算法2
     *
     * @param bytes
     * @return
     */
    public static byte[] crc16(byte[] bytes) {
        byte[] crcBytes = new byte[2];
        int len = bytes.length;
        int crc = 0xFFFF;
        for (int i = 0; i < len; i++) {
            crc = (int) (((short) ((crc >> 8) & 0xFF) | (crc << 8)) & 0xFFFF);
            crc ^= (short) (bytes[i] & 0xFF);
            crc ^= (int) (((short) (crc & 0xFF) >> 4) & 0xFFFF);
            crc ^= (int) (((crc << 8) << 4) & 0xFFFF);
            crc ^= (int) (((crc & 0xff) << 4) << 1 & 0xFFFF);
        }
        crcBytes[0] = (byte) (crc & 0xFF);
        crcBytes[1] = (byte) ((crc >> 8) & 0xFF);
        return crcBytes;
    }

}
