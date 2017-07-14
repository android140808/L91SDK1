package cn.appscomm.pedometer.protocol.AboutSportSleep;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

import apps.utils.Logger;
import cn.appscomm.pedometer.model.HeartRateData;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.protocol.ParseUtil;

/**
 * 上传心率数据
 * Created by Administrator on 2016/1/27.
 */
public class GetHeartData extends Leaf {
    private int heartCount;

    /**
     * 上传心率数据
     * 构造函数(0x70)
     *
     * @param iResultCallback
     * @param len             内容长度
     * @param content70       内容
     * @param sleepCount      需要获取的心率条数
     */
    public GetHeartData(IResultCallback iResultCallback, int len, int content70, int sleepCount) {
        super(iResultCallback, Commands.COMMANDCODE_GET_HEARTDATA, Commands.ACTION_CHECK);
        byte[] contentLen = ParseUtil.intToByteArray(len, 2);
        byte[] content = ParseUtil.intToByteArray(content70, len);
        this.heartCount = sleepCount;
        super.setContentLen(contentLen);
        super.setContent(content);
        Logger.i(TAG, "查询 : 共" + sleepCount + "条心率数据,准备获取...");
    }

    /**
     * contents字节数组解析：
     * 长度固定为7
     * 设备端持续发送直到最后一条
     * 6F 57 80   07 00   01 00 79 44 3C 56 10   8F
     * 6F 57 80   07 00   02 00 79 45 3C 56 01   8F
     * 1~2：数据索引值(1~65535)
     * 3~6：时间戳
     * 7  ：心率类型(0x00：睡着   0x01：浅睡   0x02：醒着   0x03：准备入睡   0x04：退出心率   0x10：进入心率模式   0x11：退出心率模式(本次心率非预设心率)   0x12：退出心率模式(本次心率为预设心率))
     */
    @Override
    public int parse80BytesArray(int len, byte[] contents) {
        Logger.i(TAG, "byte[] contents == " + contents);
        int ret = Commands.RESULTCODE_ERROR;
        if (len == 7) {
            int index = (int) ParseUtil.bytesToLong(contents, 0, 1); // 索引值
            long timeStamp = ParseUtil.bytesToLong(contents, 2, 5);  // 时间戳
            int sleepType = contents[6];//心率值
            Logger.i(TAG, "设备传递的心率值 == " + sleepType);
            timeStamp = timeStamp + 8 * 3600;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TimeZone tz = TimeZone.getDefault();
            int offset1 = tz.getRawOffset() / 3600000;
            Logger.e(TAG, "______________offset:" + offset1 + " /ID:" + tz.getID());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            Logger.i(TAG, "查询返回 : 心率数据(索引值:" + " 时间(" + sdf.format(new Date(timeStamp * 1000)));
            Logger.i(TAG, "查询返回 : 心率数据(索引号:" + index + " 时间(" + timeStamp + "):" + " 心率值:" + sleepType + ")");
            Logger.i(TAG, "查询返回 : 心率数据(索引值:" + index + " 时间(" + timeStamp + "):" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(timeStamp * 1000)) + " 心率:" + sleepType);

            if (GlobalVar.heartRateDatas == null || GlobalVar.heartRateDatas.size() == 0 || index == 1) {
                GlobalVar.heartRateDatas = new LinkedList<HeartRateData>();
            }
            HeartRateData heartRateData = new HeartRateData();

            heartRateData.heartRate_mode = 0x00;
            heartRateData.heartRate_time_stamp = timeStamp;
            heartRateData.heartRate_value = sleepType;
            heartRateData.heartRate_index = index;

            heartRateData.heartRate_min = sleepType;
            heartRateData.heartRate_max = sleepType;
            heartRateData.heartRate_avg = sleepType;
            heartRateData.heartRate_start_time_stamp = timeStamp;
            heartRateData.heartRate_end_time_stamp = timeStamp;

            Logger.e(TAG, "心率值的对象数据 == " + heartRateData);

            GlobalVar.heartRateDatas.add(heartRateData);
            ret = Commands.RESULTCODE_CONTINUE_RECEIVING;

            if (GlobalVar.heartRateDatas.size() == heartCount) {
                Log.i(TAG, "获取完所有心率数据!!!");
                ret = Commands.RESULTCODE_SUCCESS;
            } else {
                if (index == heartCount) { // 获取到最后一条，但没有收到完整的数据，需要重新获取
                    if (GlobalVar.heartRateDatas != null) {
                        GlobalVar.heartRateDatas.clear();
                    }
                    Log.i(TAG, "有心率数据丢失，需要重新获取!!!");
                    ret = Commands.RESULTCODE_RE_SEND;
                }
            }


        }
        return ret;
    }

}
