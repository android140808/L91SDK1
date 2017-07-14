package cn.appscomm.pedometer.protocol.AboutSetting;

import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 升级模式
 * Created by Administrator on 2016/2/1.
 */
public class UpgradeMode extends Leaf {

    /**
     * 升级模式
     * 构造函数
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public UpgradeMode(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_UPGRADE_MODE, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * 升级模式
     * 构造函数
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content71       内容
     */
    public UpgradeMode(IResultCallback iResultCallback, int len, byte content71) {
        super(iResultCallback, Commands.COMMANDCODE_UPGRADE_MODE, Commands.ACTION_SET);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = new byte[]{content71};
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    // 升级模式没有0x80命令
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        Logger.i(TAG,"解析升级模式的80:" + contents[0]);
        int ret;
        if (len > 0) {
            ret = contents[0] == (byte) 0x00 ? Commands.RESULTCODE_SUCCESS : Commands.RESULTCODE_FAILD;
        } else {
            ret = Commands.RESULTCODE_FAILD;
        }
        return ret;
    }
}
