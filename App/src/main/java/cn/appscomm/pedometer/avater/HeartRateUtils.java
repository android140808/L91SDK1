package cn.appscomm.pedometer.avater;

/**
 * Created by Administrator on 2017/4/6.
 */

public class HeartRateUtils {
    private HeartRateUtils instance;
    /**
     * post 的地址
     */
    private String mUrl_POST = "http://3plus.fashioncomm.com/appscomm/api/heartrate/uploadHeartRecord";
    /**
     * get 统计数据的地址
     */
    private String mUrl_GET = "http://3plus.fashioncomm.com/appscomm/api/heartrate/getHeartRecord?";

    private HeartRateUtils() {
    }

    public HeartRateUtils getInstance() {
        if (instance == null) {
            instance = new HeartRateUtils();
        }
        return instance;
    }

    /**
     * 上传数据到服务器
     *
     * @param url
     * @param json
     * @return
     */
    public int httpPostWithJSON(String url, String json) {
        int state = -1;

        return state;
    }
}
