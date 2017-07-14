package cn.appscomm.pedometer.protocol.AboutSetting;


import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 自动心率
 * Created by Administrator on 2016/1/26.
 */
public class HeartSetting extends Leaf {

    /**
     * 查询心率间隔时间
     * 构造函数(0x70)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public HeartSetting(IResultCallback iResultCallback, int len, byte content70) {
        super(iResultCallback, Commands.COMMANDCODE_AUTO_HEART, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        Logger.i(TAG, "查询 : 准备获取自动心率时间...");
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * 查询心率间隔时间
     * 构造函数(0x71)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param minute            时间（分钟）
     */
    public HeartSetting(IResultCallback iResultCallback, int len, int minute) {
        super(iResultCallback, Commands.COMMANDCODE_AUTO_HEART, Commands.ACTION_SET);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = new byte[len];
        byte[] bYear = ParseUtil.intToByteArray(minute, 1);
        System.arraycopy(bYear, 0, content, 0, 1);
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
        Logger.i(TAG, "查询返回 : 自动检测心率时间是:" +len);

        int ret = Commands.RESULTCODE_ERROR;
        if (len>0) {
            Logger.i(TAG, "查询返回 : 自动检测心率时间是:" +len);
//            String dateTime = ParseUtil.getDateTime(contents, 0);
            GlobalVar.autoTrack=contents[0];
            Logger.i(TAG, "查询返回 : 自动检测心率时间是:" + GlobalVar.autoTrack);
            ret = Commands.RESULTCODE_SUCCESS;
        }
        return ret;
    }
}
