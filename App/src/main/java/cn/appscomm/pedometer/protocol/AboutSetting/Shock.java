package cn.appscomm.pedometer.protocol.AboutSetting;


import apps.utils.Logger;
import apps.utils.NumberUtils;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 震动
 * Created by Administrator on 2016/1/27.
 */
public class Shock extends Leaf {

    /**
     * 震动
     * 构造函数(0x70)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public Shock(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_SHOCK, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);

        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * 震动
     * 构造函数(0x71)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param remindType      动作类型
     * @param shockType       动作对应的震动类型
     */
    public Shock(IResultCallback iResultCallback, int len, byte remindType, byte shockType) {
        super(iResultCallback, Commands.COMMANDCODE_SHOCK, Commands.ACTION_SET);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = new byte[len];
        content[0] = remindType;
        content[1] = shockType;
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * contents字节数组解析：
     * 长度不固定
     * 例子:
     * 6F 0A 80   0A 00   06 02 04 05 06 02 04 05 04 05   8F
     * 1、动作类型0x00对应的震动模式
     * 2、动作类型0x01对应的震动模式
     * ……
     * <p/>
     * 动作类型(0x00:防丢提醒   0x01:闹钟提醒   0x02:来电提醒   0x03:未接来电提醒   0x04:短信提醒   0x05:社交提醒   0x06:邮件提醒   0x07:日历提醒   0x08:久坐提醒   0x09:低电提醒   ……)
     * 震动模式(0x00:关闭,不震动   0x01:模式一(单次长震动)   0x02:模式二(单次短震动)   0x03:模式三(间歇2次长震动)   0x04:模式四(间歇2次短震动)   0x05:模式五(长震动-短震动交替)   0x06:一直长震   0x07:一直短震)
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        int ret = Commands.RESULTCODE_ERROR;
        if (len > 0) {
            GlobalVar.shockItems = new int[len];
            for (int i = 0; i < contents.length; i++) {
                GlobalVar.shockItems[i] = contents[i];
            }
            Logger.i(TAG, "提醒震动 : " + NumberUtils.binaryToHexString(contents));
            ret = Commands.RESULTCODE_SUCCESS;
        }
        return ret;
    }
}
