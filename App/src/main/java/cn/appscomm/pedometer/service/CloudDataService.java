package cn.appscomm.pedometer.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.LeaderBoardUrl;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PropertiesUtil;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.HeartRateData;
import cn.appscomm.pedometer.model.SleepData;
import cn.appscomm.pedometer.model.SportsData;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.l11.appscomm.pedometer.activity.R;

//import apps.utils.DateTool;

public class CloudDataService {

    public final String TAG = "CloudDataService";

    private Handler mHandler;
    private Context context;
    private Intent mIntent;
    private ProgressDialog mProgressDialog = null;
    private Thread mThread;
    private PropertiesUtil pu;
    private Properties props;


    private DBService dbService;
    private HttpUtil httpUtil;

    private JSONArray jsonArray = null;
    private String reqUrl; //云端请求地址
    private String reqParams; //云端请求参数
    private String reqMethod = "POST"; //云端请求方式

    public String respondBody = "";
    public String sucOpType = "";
    public boolean isSportSave = false;
    public boolean isSleepSave = false;
    public boolean isHeartRateSave = false;
    public boolean isCheckBind = false;
    public int sendedTodayData = 0;
    public boolean isShowMsgTip = false;


    private static final int HIDDEN_DIALOG = 999;
    private static final int RESULT_DATA_LOADED = 8888; // 云端数据下载完成后同步，结果码：成功

    public CloudDataService() {

    }

    public CloudDataService(Context context, Handler handler) {
        //this.context = context;
        this.context = PublicData.appContext2;
        this.mHandler = handler;
    }

    /**
     * 设置数据
     */
    public void setData() {
        dbService = new DBService(context);
        httpUtil = new HttpUtil(context);
        pu = new PropertiesUtil();

        pu.initResRawPropFile(context, R.raw.server);
        props = pu.getPropsObj();
    }

    /**
     * 显示处理对话框
     */
    public void showProgressDialog() {
        String title = context.getString(R.string.app_name);
        String message = context.getString(R.string.login_loading);
        mProgressDialog = DialogUtil.comProDialog(context, title, message);
        mProgressDialog.show();
    }

    /**
     * 保存云端运动详细数据
     *
     * @param sportList
     */


    public void saveCloudSportData(List<SportsData> sportList) {

        Logger.d(TAG, ">>进入saveCloudSportData");
        if (sportList == null || sportList.isEmpty() || sportList.size() == 0) {
            Logger.d(TAG, ">>运动数据为空  sportList is null || size:" + sportList.size());
            mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
            return;
        }

        if (httpUtil.isNetworkConnected()) {
//			Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            boolean sex = (Boolean) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.SEX_OLD_ITEM_KEY, ConfigHelper.DATA_BOOLEAN);
            int height = (Integer) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.HEIGHT_ITEM_KEY, ConfigHelper.DATA_INT);

            String sheight = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_HEIGHT, ConfigHelper
                    .DATA_STRING);
            String hUnit = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.UNIT_KEY, ConfigHelper.DATA_STRING);


            //-直接取绑定时的身高
            String sHei = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.INFO_HEIGHT, ConfigHelper.DATA_STRING);
            Logger.i(TAG, "<<===sHei===:" + sHei);

            double height_old = new Double(sHei);
            Logger.i(TAG, "<<===height-old:" + height_old);
            height = (int) height_old;
            if ("0".equals(hUnit)) { //ft in
                //height =
                height = (int) (float) Float.valueOf(sheight); //ft in 转 cm

            } else { // cm
                height = (int) (float) (Float.valueOf(sheight) * 2.54);
            }
            //-

            /**
             * 距离是根据系数算出来的，举例：男人系数：0.415，女人系数：0.413
             * 步长=身高*系数
             * 距离=步长*步数
             */
            float stepLength = 0;
            if (sex) {
                if ("".equals(sheight)) {
                    stepLength = (float) (170 * 0.415);
                } else {
                    stepLength = (float) (height * 0.415);
                }
            } else {
                if ("".equals(sheight)) {
                    stepLength = (float) (160 * 0.413);
                } else {
                    stepLength = (float) (height * 0.413);
                }
            }
            TimeZone tz = TimeZone.getDefault();
            int offset = tz.getRawOffset() / 3600000;
            Logger.i(TAG, "================时区偏移：" + offset);

            String tmp = "";
            String deviceId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM, 1);
            int totalRecord = sportList.size();
            for (SportsData mSportsData : sportList) {
                if (!"".equals(tmp)) {
                    tmp += ",";
                }

                int steps = mSportsData.sport_steps;
                Logger.i("", "stepLength=" + stepLength);
                Logger.i("", "steps=" + steps);
                float dists = stepLength * steps / 100;

                int sportDuration = mSportsData.sport_timeTotal;
//				DateTool dt = new DateTool();
//				String src = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(mSportsData.sport_time_stamp*1000));
//				Log.d(TAG, "==========UCT:" + dt.string2TimezoneDefault(src, "UCT"));

                //	Log.i(TAG, ">>>转换前时间：" + "stamp:"+mSportsData.sport_time_stamp+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date
                // (mSportsData.sport_time_stamp*1000)));
                //Calendar calendar = Calendar.getInstance();
                //	calendar.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
                //calendar.setTimeZone(tz);
                //calendar.setTimeInMillis(mSportsData.sport_time_stamp*1000);
                //calendar.add(Calendar.HOUR_OF_DAY, offset);
                //		Log.i(TAG, ">>>转换后时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(calendar.getTimeInMillis())));

		/*		long startTime = calendar.getTimeInMillis()/1000;//mSportsData.sport_time_stamp;// - offset;
                long endTime = calendar.getTimeInMillis()/1000;//mSportsData.sport_time_stamp;// - offset;
*/


                long tempSec = mSportsData.sport_time_stamp + 8 * 3600;

                /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time8 = sdf.format(new Date(tempSec*1000));
                Log.i(TAG,"8时区的时间是:" + time8);
                String time0 =sdf.format(new Date((tempSec - 8 * 3600) * 1000));
                Log.i(TAG,"0时区的时间是:" + time0);*/
                long endTime = tempSec;
                long startTime = endTime - 600;


			/*	long startTime = mSportsData.sport_time_stamp - 60 + 8*3600;
                long endTime = mSportsData.sport_time_stamp + 8*3600;
*/

                if (tempSec % (3600 * 24) < 60) //结束时间在0--59秒内
                {
                    Logger.d(TAG, "endTime in first min");
                    endTime = tempSec - 60;
                    startTime = endTime - 10 * 60;


                } else if (tempSec % (3600 * 24) <= 600) //结束时间在1--10分内
                {
                    Logger.d(TAG, "endTime in 10 min");
                    endTime = tempSec;
                    startTime = tempSec - (tempSec % (3600 * 24));

                } else {   //正常范围内
                    Logger.d(TAG, "endTime in normal region");
                    endTime = tempSec;
                    startTime = endTime - 10 * 60;

                }

//				long startTime2 = mSportsData.sport_time_stamp;
//				long endTime2 = mSportsData.sport_time_stamp;
//				
//				startTime2 = startTime2 - TimeZone.getDefault().getRawOffset();
//				endTime2 = endTime2 - TimeZone.getDefault().getRawOffset();
                //这里固定传上来的是北京时间，..
                /*tmp += "{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"" +
                        ",\"steps\":\"" + steps + "\",\"dist\":\"" + dists + "\",\"speed\":\"0\",\"cal\":\"" + mSportsData.sport_cal + "\"" +
						",\"avgRate\":\"0\",\"minRate\":\"0\",\"maxRate\":\"0\"}";*/

                float cal1 = mSportsData.sport_cal / 1000F;

                // summer: change sportDuration 2 totaltime
//                tmp += "{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"" +
//                        ",\"steps\":\"" + steps + "\",\"dist\":\"" + dists + "\",\"sportDuration\":\"" + sportDuration * 60 + "\",\"speed\":\"0\"," +
//                        "\"cal\":\"" + cal1 + "\"" +
//                        ",\"avgRate\":\"0\",\"minRate\":\"0\",\"maxRate\":\"0\"}";
                Logger.e("", "Avater 进行距离的调试 上传的距离的计算值 = " + dists);
//                if (GlobalVar.distanceChance != 0) {
//                    dists = GlobalVar.distanceChance;
//                    Logger.e("", "Avater 进行距离的调试 上传时重新赋值，避免出现误差 = " + dists);
//                }
                dists = mSportsData.sport_energy;
                Logger.e("", "Avater 进行距离的调试 上传时重新赋值，避免出现误差 = " + dists);
                tmp += "{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"" +
                        ",\"steps\":\"" + steps + "\",\"dist\":\"" + dists + "\",\"sportDuration\":\"" + sportDuration + "\",\"speed\":\"0\"," +
                        "\"cal\":\"" + cal1 + "\"" +
                        ",\"avgRate\":\"0\",\"minRate\":\"0\",\"maxRate\":\"0\"}";


                //Sinker:add 组合待上传的排行榜数据
                jsonArray = new JSONArray();
                try {
                    JSONObject detailObject = new JSONObject();
                    detailObject.put("deviceId", deviceId);
                    if (PublicData.selectDeviceName.equals(PublicData.L38I)) {
                        detailObject.put("deviceType", PublicData.getCloudDeviceType(PublicData.L38I));
                    } else {
                        detailObject.put("deviceType", PublicData.getCloudDeviceType(PublicData.L39));
                    }
                    detailObject.put("dataDate", NumberUtils.utcTimeStamp2format(endTime));
                    detailObject.put("sportsStep", steps);
                    detailObject.put("sportsDistance", dists);
                    detailObject.put("sportsCalorie", cal1);
                    detailObject.put("activeTime", sportDuration);
                    jsonArray.put(detailObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            Logger.e(TAG, "jsonArray=" + jsonArray.toString());

            //Test
//			tmp += "{\"startTime\":\"" + new Date().getTime() + "\",\"endTime\":\"" + new Date().getTime() + "\"" +
//					",\"steps\":\"" + 50 + "\",\"dist\":\"" + 10 + "\",\"speed\":\"0\",\"cal\":\"" + 5 + "\"" +
//					",\"avgRate\":\"0\",\"minRate\":\"0\",\"maxRate\":\"0\"}";

            if ("".equals(tmp)) {
                mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
                return;
            }


            int cur_steps_total = (Integer) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CUR_STEPS_TOTAL, ConfigHelper.DATA_INT);


            //
            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            String uid = (String) ConfigHelper.getCommonSharePref(context,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);
            String type = PublicData.getDeviceTypeByWatchId(watchId);

            //\"total\":\"" + cur_steps_total + "\",
            //\"total\":\"" + cur_steps_total + "\",
            //"total":{ "startTime":"89565615","endTime":"851212121","steps":"1234","dist":"700","speed":"8.7","cal":"3560"},

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //long time = TimesrUtils.getTimesMorning(calendar)*1000L;


            long curStartTime = TimesrUtils.getTimesMorning(calendar) * 1000L;
            long curEndTime = TimesrUtils.getTimesNight(calendar) * 1000L;

            long curDis = (long) (stepLength * cur_steps_total / 100);
            long curCal = 0;
            String scurStep = " ";

            //如果是最后一个数据包，则需要上传汇总值
            if (PublicData.dataSendedInfo.totalDataCount == PublicData.dataSendedInfo.totalSendedCount + PublicData.dataSendedInfo.curCount) {
                Logger.d(TAG, "已是最后一个数据包，检查是否要上传汇总");

                //如果汇总值和详细数据不一样，需要上传汇总值
                //最后一个包必须上传		if (PublicData.dataSendedInfo.totalTodaySendedCount + PublicData.dataSendedInfo.curTodayCount != cur_steps_total )
                {

                    //   int stepsDiff= cur_steps_total - PublicData.dataSendedInfo.totalTodaySendedCount;
                    scurStep = "\"total\":{\"startTime\":\"" + curStartTime + "\", \"endTime\":\"" + curEndTime + "\", \"steps\":\"" +
                            cur_steps_total + "\",  \"dist\":\"" + curDis + "\",  \"speed\":\"" + "0" + "\", \"cal\":\"" + curCal + "\"}, ";
                    Logger.d(TAG, "需要要上传汇总 ，汇总值:" + cur_steps_total);
                }
            }
            Logger.d(TAG, "---------------" + scurStep);
            reqUrl = props.getProperty("server.upload.sport.data", "http://app.appscomm.cn:8000/comm/api/sportdataupload");
            reqParams = "sportDatas={\"watchId\":\"" + watchId + "\"," + scurStep + "\"version\":\"1.0.0\",\"type\":" +
                    "\"" + type + "\",\"personId\":" + new Integer(uid) + ",\"customer\":\"appscomm\",\"totalRecord\":" + totalRecord + ",\"values\":[" + tmp + "]}";
//            reqParams = "sportDatas={\"watchId\":\"" + watchId + "\"," + scurStep + "\"version\":\"1.0.0\",\"type\":" +
//                    "\"" + type + "\",\"personId\":" + new Integer(uid) + ",\"customer\":\"3plus\",\"totalRecord\":" + totalRecord + ",\"values\":[" + tmp + "]}";

            isSportSave = true; //运动数据上传
            Logger.i("test-sync", "上传运动数据 url:" + reqUrl + "  参数:" + reqParams + "  条数:" + totalRecord);
            NumberUtils.appendContent("上传运动数据 url:" + reqUrl + "  参数:" + reqParams + "  条数:" + totalRecord + System.getProperty("line.separator"));
            NumberUtils.appendContent("上传时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + System.getProperty("line.separator"));
            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.upload_sportdata_fail), Toast.LENGTH_LONG).show();

        }
    }

    public void saveLeaderBoardData(JSONArray jsonArray) {
        Logger.e(TAG, "--saveLeaderBoardData--jsonArray:" + jsonArray);
        if (jsonArray == null) {
            return;
        }

        String url = LeaderBoardUrl.url_uploadLeaderBoard;
        String seq = LeaderBoardUrl.createRandomSeq();
        int ddId = (int) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_DDID_ITEM_KEY, ConfigHelper.DATA_INT);
        String deviceId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject jsonObject = new JSONObject();

        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            jsonObject.put("seq", seq);
            jsonObject.put("versionNo", "" + versionCode);
            jsonObject.put("clientType", "android");
            jsonObject.put("ddId", ddId);
            jsonObject.put("detail", jsonArray);

            reqUrl = LeaderBoardUrl.url_uploadLeaderBoard;
            reqParams = jsonObject.toString();
            mThread = new Thread(mLeaderBoardRunnable);
            mThread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存云端睡眠详细数据
     *
     * @param sleepList
     */
    public void saveCloudSleepData(List<SleepData> sleepList) {
        Logger.d(TAG, ">>进入saveCloudSleepData");


        if (sleepList == null || sleepList.isEmpty() || sleepList.size() == 0) {
            Logger.d(TAG, ">>睡眠数据为空  sleepList is null || size:" + sleepList.size());
            mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
            return;
        }

        if (httpUtil.isNetworkConnected()) {
//			Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String tmp = "";
            //存放多段睡眠数据
            List<List<SleepData>> list = new ArrayList<List<SleepData>>();
            //存放单段睡眠数据
            List<SleepData> list1 = null;
            //所有未上传的睡眠原始数据
            for (SleepData sleepData : sleepList) {

                int id = sleepData.sleep_id;
                int sleep_type = sleepData.sleep_type;
                long sleep_time_stamp = sleepData.sleep_time_stamp;

                //0x00：睡着， 0x01：浅睡， 0x02：醒着，0x03：准备入睡，0x10（16）：进入睡眠模式；0x11（17）：退出睡眠模式

                if (sleep_type == 16) {
                    list1 = new ArrayList<SleepData>();
                    SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                    list1.add(mData); //每段睡眠的开始

                } else if (sleep_type == 17) {
                    SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                    list1.add(mData); //每段睡眠的结束

                    list.add(list1);

                    list1 = null;

                } else {
                    if (list1 != null) {
                        SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                        list1.add(mData);

                    }
                }
            }

            for (int i = 0; i < list.size(); i++) {
                List<SleepData> oneList = list.get(i);
            }


            String sleepDate = " ";
          /*  if (CommonUtil.isInAutoSleepTime(PublicData.appContext2))
            {
                sleepDate =   "\"sleepDate\":" + endTime + ",";
            }*/

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);
            reqUrl = props.getProperty("server.upload.sleep.data", "http://app.appscomm.cn/appscomm/api/sleep/record/upload");
            reqParams = "{\"personId\":\"" + 2130 + "\",\"deviceType\":" +
                    "\"" + type + "\",\"startTime\":1404662400,\"endTime\":1404662400,\"quality\":99," +
                    "\"sleepDuration\":100," +
                    "\"awakeDuration\":10," +
                    "\"lightDuration\":10," + sleepDate +
                    "\"deepDuration\":90," +
                    "\"totalDuration\":105," +
                    "\"awakeCount\":2," +
                    "\"details\":[" +
                    "{\"startTime\":1404662400,\"status\":2}]}";

            isSleepSave = true;
//			mThread = new Thread(mRunnable);
//			mThread.start();


        } else {
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.upload_Sleepdata_fail), Toast.LENGTH_LONG).show();
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }
    }

    // summer: add
    public void saveCloudHeartRateData(List<HeartRateData> heartRateDataList) {

        Logger.d(TAG, ">>进入saveCloudHeartRateData");
        if (heartRateDataList == null || heartRateDataList.isEmpty() || heartRateDataList.size() == 0) {
            Logger.d(TAG, ">>心率数据为空  heartRateDataList is null || size:" + heartRateDataList.size());
            mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
            return;
        }

        if (httpUtil.isNetworkConnected()) {
//			Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            TimeZone tz = TimeZone.getDefault();
            int offset = tz.getRawOffset() / 3600000;
            Logger.d(TAG, "================时区偏移：" + offset);

            String tmp = "";
            for (HeartRateData heartRateData : heartRateDataList) {
                if (!"".equals(tmp)) {
                    tmp += ",";
                }

                int heartMin = heartRateData.heartRate_min == 0 ? heartRateData.heartRate_value : heartRateData.heartRate_min;
                int heartMax = heartRateData.heartRate_max == 0 ? heartRateData.heartRate_value : heartRateData.heartRate_max;
                int heartAvg = heartRateData.heartRate_avg == 0 ? heartRateData.heartRate_value : heartRateData.heartRate_avg;

                long tempSec = heartRateData.heartRate_time_stamp;

                long endTime = tempSec;
                long startTime = endTime - 5;


			/*	long startTime = mSportsData.sport_time_stamp - 60 + 8*3600;
                long endTime = mSportsData.sport_time_stamp + 8*3600;
*/

                startTime = heartRateData.heartRate_start_time_stamp;
                endTime = heartRateData.heartRate_end_time_stamp;
                TimeZone tz1 = TimeZone.getDefault();
                int offset1 = tz1.getRawOffset() / 3600000;
                Logger.e(TAG, "______________offset:" + offset1 + " /ID:" + tz.getID());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                String startTimeStr = sdf.format(new Date(startTime * 1000L)),
                        endTimeStr = sdf.format(new Date(endTime * 1000L));

                tmp += "{\"heartMin\":\"" + heartMin + "\",\"heartMax\":\"" + heartMax + "\"" +
                        ",\"heartAvg\":\"" + heartAvg + "\",\"startTime\":\"" + startTimeStr + "\",\"endTime\":\"" + endTimeStr + "\"}";

            }

            if ("".equals(tmp)) {
                mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
                return;
            }

            //
            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);

            String userId = (String) ConfigHelper.getCommonSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //long time = TimesrUtils.getTimesMorning(calendar)*1000L;

            reqUrl = props.getProperty("server.upload.heartrate.data", "http://28h.fashioncomm.com/comm/api/heartrate/uploadHeartRecord");

            reqParams = "{\"personId\":\"" + userId + "\",\"deviceId\":" + "\"" + watchId + "\",\"deviceType\":" + "\"" + type + "\""
                    + "," + "\"details\":[" + tmp + "]}";
            isHeartRateSave = true; //心率数据上传
            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.upload_sportdata_fail), Toast.LENGTH_LONG).show();

        }
    }

    /**
     * 获取云端运动数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param queryType 查询类型  1按天 2按小时 3按周  4按月
     */
    public void getCloudSportData(String startTime, String endTime, String queryType) {
        Logger.d(TAG, ">>进入getCloudSportData");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.sport.data", "http://app.appscomm.cn/sport/api/get_sport_total");
            reqParams = "watchId=" + watchId + "&watchType=" + type + "&startTime=" + startTime
                    + "&endTime=" + endTime + "&queryType=" + queryType + "&userId=" + userId;

            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sportdata_fail), Toast.LENGTH_LONG).show();
            if (mHandler != null) {
                // 没有网络的情况下，使用本地缓存数据
                mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
            }
        }
    }

    /**
     * @param startTime
     * @param endTime   固定查询按小时统计的数据列表
     */
    public void getCloudSportDataByHours(String startTime, String endTime) {
        Logger.d(TAG, ">>进入getCloudSportDataByHours");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            //String watchId = "FCL28C14032821023327";
            String type = PublicData.getDeviceTypeByWatchId(watchId);
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.sport.data", "http://app.appscomm.cn/sport/api/get_sport_total");
            reqParams = "watchId=" + watchId + "&watchType=" + type + "&startTime=" + startTime
                    + "&endTime=" + endTime + "&queryType=2" + "&userId=" + userId;

            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sportdata_fail), Toast.LENGTH_LONG).show();
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }


    }

    /**
     * 获取云端睡眠汇总数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param queryType 查询类型  1按天 2按周  3按月
     */
    public void getCloudSleepTotalData(long startTime, long endTime, String queryType) {
        Logger.d(TAG, ">>进入getCloudSleepTotalData");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.sleep.total.data", "http://app.appscomm.cn/appscomm/api/sleep/total");
            reqParams = "personId=" + userId + "&deviceType=" + type + "&startDate=" + startTime
                    + "&endDate=" + endTime + "&qryType=" + queryType;

            reqUrl = reqUrl + "?" + reqParams;
            Logger.i(TAG, "reqParams=" + reqParams);
            mThread = new Thread(mRunnable2);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sleepdata_fail), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 获取云端睡眠记录数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    public void getCloudSleepRecordData(long startTime, long endTime) {
        Logger.d(TAG, ">>进入getCloudSleepRecordData");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.sleep.record.data", "http://app.appscomm.cn/appscomm/api/sleep/record");
            reqParams = "personId=" + userId + "&deviceType=" + type + "&startDate=" + startTime
                    + "&endDate=" + endTime;

            reqUrl = reqUrl + "?" + reqParams;
            Logger.d("CloundDataService", "睡眠数据请求链接" + reqUrl);

            mThread = new Thread(mRunnable2);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sleepdata_fail), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * summer: add
     * 获取云端心率数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param queryType 查询类型  1按天 2按小时 3按周  4按月
     */
    public void getCloudHeartRateData(String startTime, String endTime, String queryType) {
        Logger.d(TAG, ">>getCloudHeartRateData");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            //String watchId = "FCL28C14032821023327";
//			String type = "L11";
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.heartrate.data", "http://28h.fashioncomm.com/appscomm/api/heartrate/getHeartRecord");
            reqParams = "personId=" + userId + "&deviceId=" + watchId + "&startDate=" + startTime
                    + "&endDate=" + endTime;
            reqUrl = reqUrl + "?" + reqParams;
            reqMethod = "GET";
            mThread = new Thread(mRunnable2);
            mThread.start();
        } else {
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sportdata_fail), Toast.LENGTH_LONG).show();
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }
    }

    /**
     * 获取心率统计数据
     *
     * @param startTime
     * @param endTime
     */
    public void getCloudHeartRateCount(String startTime, String endTime) {
        Logger.d(TAG, ">>getCloudHeartRateData");

        if (httpUtil.isNetworkConnected()) {
            Logger.d(TAG, ">>有网络");
            //showProgressDialog();

            String watchId = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
            //String watchId = "FCL28C14032821023327";
//			String type = "L11";
            String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

            reqUrl = props.getProperty("server.get.heartratecount.data", "http://28h.fashioncomm.com/appscomm/api/heartrate/getHeartRateCount");
            reqParams = "personId=" + userId + "&deviceId=" + watchId + "&startDate=" + startTime
                    + "&endDate=" + endTime;
            Logger.i(TAG, "startTime=" + startTime + "---" + "endTime=" + endTime);
            reqUrl = reqUrl + "?" + reqParams;
            reqMethod = "GET";
            mThread = new Thread(mRunnable2);
            mThread.start();
        } else {
            if (isShowMsgTip)
                Toast.makeText(context, context.getString(R.string.download_sportdata_fail), Toast.LENGTH_LONG).show();
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }
    }

    public void getClondAccessToken() {

        String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);
        String password = (String) ConfigHelper.getSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_PASSWORD_ITEM_KEY, ConfigHelper.DATA_STRING);

        reqUrl = props.getProperty("server.access.token", "http://app.appscomm.cn/appscomm/api/token");

        reqUrl = reqUrl + "/" + userId;
        reqParams = "appId=Apps_android&applyCode=" + password;

        if (httpUtil.isNetworkConnected()) {

            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }

    }

    /**
     * 获取云端绑定状态
     */
    public void getClondBindStatus() {

        String userId = (String) ConfigHelper.getCommonSharePref(context, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);

        reqUrl = props.getProperty("server.get.device", "http://app.appscomm.cn/sport/api/get_devices");

        reqParams = "userId=" + userId;

        if (httpUtil.isNetworkConnected()) {
            isCheckBind = true;
            mThread = new Thread(mRunnable);
            mThread.start();

        } else {
            mHandler.obtainMessage(4, context.getString(R.string.NetWorkError)).sendToTarget();
        }

    }

    Runnable mRunnable2 = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---mRunnable2---");

            Logger.d(TAG, ">>云端请求地址：" + reqUrl);
            Logger.d(TAG, ">>云端请求参数：" + reqParams);

            int respondStatus = httpUtil.httpGetReq(reqUrl);
            respondBody = httpUtil.httpResponseResult;

            Logger.d(TAG, ">>>>>>>>>>respondStatus:" + respondStatus);
            HttpResDataService httpResDataService = new HttpResDataService(context);

            int i = httpResDataService.commonParse(respondStatus, respondBody, "");
            Logger.i(TAG, "------------->>>respondBody:" + respondBody);

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: //成功
                    mHandler.obtainMessage(0, "operation success!").sendToTarget();

                    break;

                case 1: //服务器返回的错误归类
                    String resultCode = httpResDataService.getResultCode();
                    //String msg = httpResDataService.getMessage();
                    String msg = "";

                    if ("".equals(msg)) {
                        msg = "[" + resultCode + "]ERROR!";
                    }
                    Logger.e(TAG, "msg=>>" + msg);

                    mHandler.obtainMessage(1, msg).sendToTarget();
                    break;

                case 2: //错误的响应信息码
                    mHandler.obtainMessage(2, "ERROR RESPOND INFO!").sendToTarget();
                    break;

                case 3: //JSONException
                    mHandler.obtainMessage(3, "JSONException!").sendToTarget();
                    break;

                case -1: //服务器未响应
                    mHandler.obtainMessage(-1, "SERVER IS NOT RESPOND!").sendToTarget();
                    break;

                case -2: //ClientProtocolException
                    mHandler.obtainMessage(-2, "ClientProtocolException!").sendToTarget();
                    break;

                case -3: //ParseException
                    mHandler.obtainMessage(-3, "ParseException!").sendToTarget();
                    break;

                case -4: //IOException
                    mHandler.obtainMessage(-4, "IOException!").sendToTarget();
                    break;

            }


        }
    };

    Runnable mLeaderBoardRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "---mLeaderBoardRunnable---");
            Logger.d(TAG, ">>云端请求地址：" + reqUrl);
            Logger.d(TAG, ">>云端请求参数：" + reqParams);

            int respondStatus = httpUtil.httpPostWithJSON(reqUrl, reqParams);

            String respondBody = httpUtil.httpResponseResult;
            Logger.e(TAG, "==>>respondStatus:" + respondStatus);
            switch (respondStatus) {
                case 0:
                    try {
                        Logger.i(TAG, "==>>respondBody:" + respondBody);
                        if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                && respondBody.contains("\"msg\"")) {
                            JSONObject jsonObj = new JSONObject(respondBody);
                            String seq1 = jsonObj.getString("seq");
                            String code = jsonObj.getString("code");
                            String msg = jsonObj.getString("msg");
                            Logger.e(TAG, "==>>code:" + code);
                            if (code.equals("0")) {
                                mHandler.obtainMessage(1001, "operation success!").sendToTarget();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                default:

                    break;
            }
        }
    };

    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---mRunnable---");

            Logger.d(TAG, ">>云端请求地址：" + reqUrl);
            Logger.d(TAG, ">>云端请求参数：" + reqParams);

            int respondStatus = 0;
//            if (isSleepSave || isHeartRateSave) {
//                isSleepSave = false;
//                Logger.d(TAG, ">>云端请求地址httpPostWithJSON：" + reqUrl);
//
//                respondStatus = httpUtil.httpPostWithJSON(reqUrl, reqParams);
//            }
            if (isSleepSave) {
                isSleepSave = false;
                Logger.d(TAG, ">>云端请求地址httpPostWithJSON：" + reqUrl);

                respondStatus = httpUtil.httpPostWithJSON(reqUrl, reqParams);
            } else if (isHeartRateSave) {
                //TODO 心率数据不再上传，交由HeartRateActivity负责
            } else {
                Logger.d(TAG, ">>---httpReq：" + reqUrl);

                respondStatus = httpUtil.httpReq(reqMethod, reqUrl, reqParams);
            }
            respondBody = httpUtil.httpResponseResult;

            HttpResDataService httpResDataService = new HttpResDataService(context);

            int i = httpResDataService.commonParse(respondStatus, respondBody, "");

            if (isSportSave) {
                Logger.i("test-sync", "响应内容 : " + respondBody);
                Logger.i("test-sync", "结果码:" + i);
                NumberUtils.appendContent("响应内容 : " + respondBody + System.getProperty("line.separator"));
                NumberUtils.appendContent("结果码:" + i + System.getProperty("line.separator"));
            }
            Logger.i(TAG, "------上传iiiii" + i);
            switch (i) {

                case 0: //成功

                    if (isSportSave) {//运动数据上传成功后发送消息
                        isSportSave = false;
                        sucOpType = "1";
                        Logger.i("test-sync", "------上传成功");
                        NumberUtils.appendContent("------上传成功" + System.getProperty("line.separator"));
                        mHandler.obtainMessage(1001, "operation success!").sendToTarget();
                        GlobalVar.distanceChance = 0;
                        Logger.e("", "Avater 进行距离的调试 距离的变化值上传成功后清零  = " + GlobalVar.distanceChance);
                        //排行榜数据客户端不再上传,改由服务端拷贝...2014/04/29
                        //saveLeaderBoardData(jsonArray);
                    } else if (isHeartRateSave) {        // summer: add
                        isHeartRateSave = false;
                        mHandler.obtainMessage(4001, "operation success!").sendToTarget();
                    } else {
                        if (isCheckBind) {
                            isCheckBind = false;
                            mHandler.obtainMessage(3001, "get bind status success!").sendToTarget();
                        } else {
                            mHandler.obtainMessage(0, "operation success!").sendToTarget();
                        }

                    }

                    break;

                case 1: //服务器返回的错误归类

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });

                    String resultCode = httpResDataService.getResultCode();
                    //String msg = httpResDataService.getMessage();
                    String msg = "";

                    if ("4000".equals(resultCode)) {
                        msg = context.getString(R.string.send_email_failed);

                    } else if ("4001".equals(resultCode)) {
                        msg = context.getString(R.string.login_username_exist);

                    }

                    if ("".equals(msg)) {
                        msg = "[" + resultCode + "]ERROR!";
                    }
                    Logger.e(TAG, "msg=>>" + msg);

                    mHandler.obtainMessage(1, msg).sendToTarget();
                    break;

                case 2: //错误的响应信息码


                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });

                    mHandler.obtainMessage(2, "ERROR RESPOND INFO!").sendToTarget();
                    break;

                case 3: //JSONException

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });
                    mHandler.obtainMessage(3, "JSONException!").sendToTarget();
                    break;

                case -1: //服务器未响应

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });
                    mHandler.obtainMessage(-1, "SERVER IS NOT RESPOND!").sendToTarget();
                    break;

                case -2: //ClientProtocolException

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });
                    mHandler.obtainMessage(-2, "ClientProtocolException!").sendToTarget();
                    break;

                case -3: //ParseException

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });
                    mHandler.obtainMessage(-3, "ParseException!").sendToTarget();
                    break;

                case -4: //IOException

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (isShowMsgTip)
                                Toast.makeText(context, context.getString(R.string.NetWorkError), Toast.LENGTH_LONG).show();
                        }
                    });

                    mHandler.obtainMessage(-4, "IOException!").sendToTarget();
                    break;
                case -999:    // summer: 上传心率数据错误，删除本地数据
                    DBService dbService = new DBService(context);
                    dbService.deleteHeartRateData();
                    mHandler.obtainMessage(-4, "IOException!").sendToTarget();

                    break;
            }

        }

    };


    public Handler mHandler2 = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0: // 成功
                    Logger.d(TAG, (String) msg.obj);
                    respondBody = httpUtil.httpResponseResult;
                    if ("1".equals(sucOpType)) {
                        dbService.deleteSportsData();
                        sucOpType = "";
                    }
                    //Toast.makeText(context, context.getString(R.string.setting_success), Toast.LENGTH_LONG).show();
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();


                    break;

                case 1: //服务器返回错误码归类说明
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context,
//						context.getString(R.string.app_name),
//						(String) msg.obj);
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case 2: // 错误的响应信息码
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "ERROR RESPOND INFO", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"ERROR RESPOND INFO");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case 3: // JSONException
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "JSONException", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"JSONException");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case 4: // 没有网络连接
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "Not Network Connection", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"Not Network Connection");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case -1: // 服务器无响应
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "Server not respond", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"Server not respond");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case -2: // ClientProtocolException
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "ClientProtocolException", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"ClientProtocolException");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case -3: // ParseException
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "ParseException", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"ParseException");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;

                case -4: // IOException
                    Logger.d(TAG, (String) msg.obj);
                    //Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
//				DialogUtil.commonDialog(context, 
//						context.getString(R.string.app_name),
//						"IOException");
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();

                    break;


            }
            super.handleMessage(msg);
        }

    };


}
