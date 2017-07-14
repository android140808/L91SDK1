package cn.appscomm.pedometer.model;

/**
 * Created by Administrator on 2016/6/16.
 */
public class HearRateDataCache {
    public String deviceId = null;
    public int heartRate = 0;
    public String countDate = null;
    public int min = 0;
    public int max = 0;
    public int avg = 0;
    public String stimestamp = null;
    public String etimestamp = null;

    public HearRateDataCache(String deviceId, int heartRate, String countDate) {
        this.deviceId = deviceId;
        this.heartRate = heartRate;
        this.countDate = countDate;
        toString();
    }

    public HearRateDataCache(int min, int max, int avg, String stimestamp, String etimestamp) {
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.stimestamp = stimestamp;
        this.etimestamp = etimestamp;
    }

    public HearRateDataCache(String deviceId, int heartRate, String countDate, int min, int max, int avg, String stimestamp, String etimestamp) {
        this.deviceId = deviceId;
        this.heartRate = heartRate;
        this.countDate = countDate;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.stimestamp = stimestamp;
        this.etimestamp = etimestamp;
    }

    @Override
    public String toString() {
        return "HearRateDataCache{" +
                "设备ID='" + deviceId + '\'' +
                ", 心率=" + heartRate +
                ", 日期=" + countDate +
                ", 最小值=" + min +
                ", 最大值=" + max +
                ", 平均值=" + avg +
                ", 开始时间戳=" + stimestamp +
                ", 结束时间戳=" + etimestamp +
                '}';
    }
}
