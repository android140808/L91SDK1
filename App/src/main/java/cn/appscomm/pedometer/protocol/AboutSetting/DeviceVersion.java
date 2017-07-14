package cn.appscomm.pedometer.protocol.AboutSetting;

import java.util.Arrays;

import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 设备版本
 * Created by Administrator on 2016/1/26.
 */
public class DeviceVersion extends Leaf {

    /**
     * 设备版本
     * 构造函数(0x70)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public DeviceVersion(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_DEVICE_VERSION, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * contents字节数组解析：
     * 长度不固定
     * 例子:
     * 6F 03 80   14 00   00 56 30 2E 31   8F(软件版本v0.1)
     * 1、类型(0x00:软件版本	0x01:硬件版本	0x02:蓝牙协议版本	0x03:功能版本)
     * 2~N、字符串类型，左靠齐
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        int ret = Commands.RESULTCODE_ERROR;
        if (len > 0) {
            try {
                String version = new String(Arrays.copyOfRange(contents, 1, len), "US-ASCII");
                switch (contents[0]) {

                    case 0x00: // 设备类型
                        GlobalVar.deviceType = version;
                        break;

                    case 0x01: // 软件版本
                        //N0.5R0.1H0.1 heartVersion

                        GlobalVar.heartVersion=version.substring(9,12);
                        GlobalVar.fontVersion=version.substring(5,8);
                        GlobalVar.softVersion=version.substring(1,4);
                        GlobalVar.allVersion=version;

                        Logger.i(TAG, "version ："+version+"··softVersion : " + GlobalVar.softVersion+"--fontVersion : " + GlobalVar.fontVersion+"--heartVersion : " + GlobalVar.heartVersion);

                        break;

                    case 0x02: // 硬件版本
                        GlobalVar.hardwareVersion = version;
                        break;

                    case 0x03: // 通信协议
                        GlobalVar.commPprotocol = version;
                        break;

                    case 0x04: // 功能版本
                        GlobalVar.functionVersion = version;
                        break;

                    default: // 其他
                        GlobalVar.otherVersion = version;
                        break;
                }
                ret = Commands.RESULTCODE_SUCCESS;
                Logger.i(TAG, "version : " + version);

            } catch (Exception e) {
            }
        } else {
            ret = Commands.RESULTCODE_FAILD;
        }
        return ret;
    }
}
