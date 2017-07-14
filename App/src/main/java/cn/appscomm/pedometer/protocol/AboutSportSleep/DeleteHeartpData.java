package cn.appscomm.pedometer.protocol.AboutSportSleep;


import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 心率数据删除命令
 * Created by Administrator on 2016/1/27.
 */
public class DeleteHeartpData extends Leaf {

    /**
     * 心率数据删除命令
     * 构造函数(0x71)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content71       内容
     */
    public DeleteHeartpData(IResultCallback iResultCallback, int len, int content71) {
        super(iResultCallback, Commands.COMMANDCODE_DELETE_HEARTDATA, Commands.ACTION_SET);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content71, len);
        super.setContentLen(contentLen);
        super.setContent(content);
        Logger.i(TAG, "修改 : 准备删除设备中的心率数据...");
    }

    /**
     * 心率数据删除没有0x80命令
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        return 0;
    }
}
