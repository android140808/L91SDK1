package cn.appscomm.pedometer.protocol.AboutSportSleep;


import apps.utils.Logger;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 运动和睡眠数据总数
 * Created by Administrator on 2016/1/27.
 */
public class TotalSportSleepCount extends Leaf {

    /**
     * 运动和睡眠数据总数
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     */
    public TotalSportSleepCount(IResultCallback iResultCallback, int len, int content70) {
        super(iResultCallback, Commands.COMMANDCODE_TOTAL_SPORT_SLEEP_COUNT, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        Logger.i(TAG, "查询 : 准备获取运动和睡眠条数..."+contentLen+content);
        super.setContentLen(contentLen);
        super.setContent(content);
    }

    /**
     * contents字节数组解析：
     * 长度固定为2
     * 例子:
     * 6F 55 80   04 00   64 00 06 00   8F(100条运动、6条睡眠)
     * 1~2、运动总数
     * 3~4、睡眠总数
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        Logger.i(TAG, "查询 : 准备获取运动和睡眠条数...");
        int ret = Commands.RESULTCODE_ERROR;
        if (len == 4) {
            GlobalVar.sportCount = (int) ParseUtil.bytesToLong(contents, 0, 1); // 运动数据总数
            GlobalVar.sleepCount = (int) ParseUtil.bytesToLong(contents, 2, 3); // 睡眠数据总数
            Logger.i(TAG, "查询返回 : 运动数据" + GlobalVar.sportCount + "条 睡眠数据" + GlobalVar.sleepCount + "条!!!");
            ret = Commands.RESULTCODE_SUCCESS;
        }
        return ret;
    }
}
