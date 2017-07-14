package cn.appscomm.pedometer.protocol.AboutSetting;

import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * Created by Administrator on 2017/3/22.
 */

public class OpenBloodPress extends Leaf {

    public OpenBloodPress(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, (byte) 0x64, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        return 0;
    }
}
