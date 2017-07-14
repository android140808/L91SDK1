package cn.appscomm.pedometer.protocol.AboutSportSleep;


import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 设备端显示的运动/睡眠值
 * Created by Administrator on 2016/1/27.
 */
public class DeviceDisplaySportSleep extends Leaf {

    /**
     * 设备端显示的运动/睡眠值
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public DeviceDisplaySportSleep(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_DEVICE_DISPLAY_SPORT_SLEEP, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * contents字节数组解析：
     * 长度不固定，但为4的倍数
     * 例子:
     * 6F 58 80   0E 00   64 00 00 00 32 00 00 00 32 00 00 00 58 02   8F(100步   50卡   50米   600分钟)
     * 1~4:	    步数(步)
     * 5~8:	    卡路里(卡)
     * 9~12:	距离(米)
     * 13~14:	睡眠时间(分)
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        Logger.i(TAG, "查询 : 共"  + "条设备端现在数据,准备获取len..."+len);
        int ret = Commands.RESULTCODE_ERROR;
        if (len == 20) {
            int deviceDisplayStep = (int) ParseUtil.bytesToLong(contents, 0, 3);        //不睡
            int deviceDisplayCalorie = (int)ParseUtil.bytesToLong(contents, 4, 7);         // 卡路里
            int deviceDisplayDistance = (int) ParseUtil.bytesToLong(contents, 8, 11);         // 距离
            int deviceDisplaySleep = (int) ParseUtil.bytesToLong(contents, 12, 15);    // 睡眠时间
            int deviceDisplayStepTime = (int) ParseUtil.bytesToLong(contents, 16, 19);   // 运动时长
            GlobalVar.deviceDisplayStep=deviceDisplayStep;
            GlobalVar.deviceDisplayCalorie=deviceDisplayCalorie;
            GlobalVar.deviceDisplayDistance=deviceDisplayDistance;
            GlobalVar.deviceDisplaySleep=deviceDisplaySleep;
            GlobalVar.deviceDisplayStepTime=deviceDisplayStepTime;
            ret = Commands.RESULTCODE_SUCCESS;
        }
        return ret;
    }
}
