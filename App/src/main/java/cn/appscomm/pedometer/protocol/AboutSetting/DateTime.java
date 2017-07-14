package cn.appscomm.pedometer.protocol.AboutSetting;

import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 日期时间
 * Created by Administrator on 2016/1/26.
 */
public class DateTime extends Leaf {

    /**
     * 日期时间
     * 构造函数(0x70)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public DateTime(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_DATETIME, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        Logger.i(TAG, "查询 : 准备获取设备时间...");
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * 日期时间
     * 构造函数(0x71)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param year            年
     * @param month           月
     * @param day             日
     * @param hour            时
     * @param min             分
     * @param sec             秒
     */
    public DateTime(IResultCallback iResultCallback, int len, int year, int month, int day, int hour, int min, int sec) {
        super(iResultCallback, Commands.COMMANDCODE_DATETIME, Commands.ACTION_SET);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = new byte[len];
        byte[] bYear = ParseUtil.intToByteArray(year, 2);
        byte[] bMonth = ParseUtil.intToByteArray(month, 1);
        byte[] bDay = ParseUtil.intToByteArray(day, 1);
        byte[] bHour = ParseUtil.intToByteArray(hour, 1);
        byte[] bMin = ParseUtil.intToByteArray(min, 1);
        byte[] bSec = ParseUtil.intToByteArray(sec, 1);

        System.arraycopy(bYear, 0, content, 0, 2);
        System.arraycopy(bMonth, 0, content, 2, 1);
        System.arraycopy(bDay, 0, content, 3, 1);
        System.arraycopy(bHour, 0, content, 4, 1);
        System.arraycopy(bMin, 0, content, 5, 1);
        System.arraycopy(bSec, 0, content, 6, 1);

        Logger.i(TAG, "设置 : 准备同步时间到设备...");
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * contents字节数组解析：
     * 长度固定为7
     * 例子:
     * 6F 04 80   07 00   DD 07 03 01 0A 1D 05   8F(2013-03-01 10:29:05)
     * 1~2、 年
     * 3、   月
     * 4、   日
     * 5、   时
     * 6、   分
     * 7、   秒
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        int ret = Commands.RESULTCODE_ERROR;
        if (len == 7) {
            String dateTime = ParseUtil.getDateTime(contents, 0);
            Logger.i(TAG, "查询返回 : 设备的时间是:" + dateTime);
            ret = Commands.RESULTCODE_SUCCESS;
        }
        return ret;
    }
}
