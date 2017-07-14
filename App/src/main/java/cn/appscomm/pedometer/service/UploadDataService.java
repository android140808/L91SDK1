package cn.appscomm.pedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.HttpUtil;
import apps.utils.LeaderBoardUrl;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PropertiesUtil;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.HeartRateData;
import cn.appscomm.pedometer.model.SleepData;
import cn.appscomm.pedometer.model.SleepTime;
import cn.appscomm.pedometer.model.SportsData;
import cn.l11.appscomm.pedometer.activity.R;

public class UploadDataService extends Service {

    public static final String TAG = "UploadDataService";
    public String gLastCallNo = "";
    private ContentObserver mObserver;
    private ContentResolver resolver;
    public static final String UPLOAD_DATA_OK = "cn.appscomm.pedometer.service.UPLOAD_DATA_OK";
    public static final String UPLOAD_DATA_FAIL = "cn.appscomm.pedometer.service.UPLOAD_DATA_FAIL";
    public static final String UPLOAD_DATA_NOW = "cn.appscomm.pedometer.service.UPLOAD_DATA_NOW";
    public static final String UPLOAD_DATA_NODATA = "cn.appscomm.pedometer.service.UPLOAD_DATA_NODATA";
    public static final String UPLOAD_SERVICE_DESTROY = "cn.appscomm.pedometer.service.UPLOAD_SERVICE_DESTROY";        // summer: add

    private static final int UnLeaderBoardSuccess = 20160501;
    private static final int UnLeaderBoardFail = 20160502;
    private static final int UnbindingSuccess = 20160503;
    private static final int UnbindingFail = 20160504;

    private Timer timer1 = null;  //定时器
    private final static int CHECK_INTER = 3 * 60;   //每次检查的间隔
    private DBService dbService;
    private HttpUtil mHttpUtil = null;
    private int timecount = 0;
    private CloudDataService cloudDataService = null;
    private List pkList = new ArrayList<Integer>();
    private boolean isSportSave = false;
    private int ret = 0;

    private boolean isSleepSave = false;

    private String reqUrl = "";
    private String reqParams = "";

    private String deviceType = "";

    private boolean isNetConnected() {
        Logger.d(TAG, "isNetConnected");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo ni : infos) {
                    if (ni.isConnected()) {
                        Logger.d(TAG, "NetWork is OK....");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        Logger.d(TAG, "makeGattUpdateIntentFilter");

        IntentFilter intentfilter = new IntentFilter();
        intentfilter
                .addAction(UPLOAD_DATA_FAIL);
        intentfilter
                .addAction(UPLOAD_DATA_OK);

        intentfilter.addAction(UPLOAD_DATA_NODATA);

        intentfilter.addAction(UPLOAD_SERVICE_DESTROY);

        return intentfilter;
    }

    private void broadcastUpdate(String s) {
        Logger.d(TAG, "broadcastUpdate");

        Intent intent = new Intent(s);

        sendBroadcast(intent);
    }

    private final BroadcastReceiver mRecData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Logger.i(TAG, "BroadcastReceiver.action=" + action);
            if (UploadDataService.UPLOAD_DATA_NOW.equals(action)) {
                //	ret = 1;
            }
        }


    };

    private Handler mHandler = new Handler() {
        @Override


        public void handleMessage(Message msg) {

            Logger.d(TAG, " Msg : " + msg.what);
            switch (msg.what) {
                case UnbindingSuccess:
                    ConfigHelper.setSharePref(getApplication(),
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOCAL_DEL_SERVER_FLAG, "1");//解绑成功之后设为已清
                    uploadLocalData();
                    break;

                case 1001: //上传云端运动数据成功
                    Logger.d(TAG, (String) msg.obj);
                    if (isSportSave) {
                        /**
                         * 上传完一批后删除掉已经上传的那批数据，然后再判断是否还有没上传的本地数据，之后再操作睡眠数据
                         *
                         */
                        Logger.d(TAG, "==>>1001上传云端运动数据成功");
                        Logger.d(TAG, "==>>1001进入删除已上传的本地运动数据");
                        for (int i = 0; i < pkList.size(); i++) {
                            int pkid = (Integer) pkList.get(i);
                            try {
                                SportsData sportsData = dbService.getSportsData(pkid);
                                NumberUtils.appendContent("删除操作:上传步数:" + sportsData.sport_steps + " 卡路里:" + sportsData.sport_cal + " 时长:" + sportsData.sport_timeTotal + " 成功，本地数据库删除该条记录...!!!" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                            } catch (Exception e) {
                            }
                            dbService.deleteSportsData(pkid);
                        }

                        List<SportsData> sportlist = dbService.getSportsDataList();
                        if (null != sportlist && sportlist.size() > 0) {
                            PublicData.dataSendedInfo.curCount = sportlist.size();
                            PublicData.dataSendedInfo.curTodayCount = 0;
                            pkList.clear();
                            for (SportsData mSportsData : sportlist) {
                                int sid = mSportsData.sid;
                                pkList.add(sid);

                            }

                            if (cloudDataService == null) {
                                cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
                                cloudDataService.setData();
                            }
                            isSportSave = true;

                            cloudDataService.saveCloudSportData(sportlist);
                        } else {
                            //	Toast.makeText(getApplicationContext(), "data uploaded automatically sucessful! ", Toast.LENGTH_LONG).show();

                            List<SleepData> sleepList = dbService.getSleepDataList();
                            //还有睡眠数据
                            if (null != sleepList && !sleepList.isEmpty() && sleepList.size() > 0) {
                                //	isSleepSave = true;
                                //	cloudDataService.setData();
//			        				cloudDataService.saveCloudSleepData(list);

                                if (cloudDataService == null) {
                                    cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
                                    cloudDataService.setData();
                                }
                                saveCloudSleepData(sleepList);
                            } else {

                                Logger.i(TAG, "mHandler():1001 --> broadcastUpdate(UPLOAD_DATA_OK)");
                                broadcastUpdate(UPLOAD_DATA_OK);
                                stopSelf();

                            }

                        }

                    }

                case 2001:
                    dbService.deleteSleepData();
                    if (!deviceType.equals(PublicData.L39) || deviceType.equals(PublicData.L38I)) {
                        stopSelf();
                        Logger.i(TAG, "mHandler():2001 --> broadcastUpdate(UPLOAD_DATA_OK)");
                        broadcastUpdate(UPLOAD_DATA_OK);
                    }
                    break;

                case 4001:
                    dbService.deleteHeartRateData();
                    Logger.i(TAG, "mHandler():4001 --> broadcastUpdate(UPLOAD_DATA_OK)");
                    broadcastUpdate(UPLOAD_DATA_OK);
                    stopSelf();
                    break;

                default:
                    broadcastUpdate(UPLOAD_DATA_FAIL);

                    stopSelf();
                    break;

            }
        }
    };

    @Override
    public void onCreate() {
        Logger.i(TAG, "UploadService onCreate().");
        super.onCreate();
        mHttpUtil = new HttpUtil(this);
         /*if (null==timer1)
        {
			
	    timer1 = new Timer();
	    timecount = 0;
		timer1.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                timecount++;
            }
        },0,1000);*/
        //	if (((timecount % CHECK_INTER) ==2) || ret==1)   //自定间隔检查

					
			/*	if (ret==1) //强制上传
                    {
						Logger.d(TAG, "fore upload....,timecount : "+ timecount);
						ret = 0;
						uploadLocalData();
						
						
						return;
					}*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "UploadService onStartCommand().");
        dbService = new DBService(getApplicationContext());
        deviceType = (String) ConfigHelper.getSharePref(this,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);

        String local_del_server_flag = (String) ConfigHelper.getSharePref(this,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.LOCAL_DEL_SERVER_FLAG, ConfigHelper.DATA_STRING);

        if (cloudDataService == null) {
            cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
            cloudDataService.setData();
        }

        if (timecount > Integer.MAX_VALUE - 2) timecount = 3;

//        Logger.d(TAG, "data count:" + dbService.getSportsDataCount() + "," + dbService.getSleepDataCount());
//
//        if ((!isNetConnected()) || ((dbService.getSportsDataCount() == 0) && (dbService.getSleepDataCount() == 0)
//                && (dbService.getHeartRateDataCount() == 0)))
//
//        {
//            Logger.i(TAG,"broadcastUpdate");
//            broadcastUpdate(UPLOAD_DATA_NODATA);
//            stopSelf();
//            return super.onStartCommand(intent, flags, startId);
//        }

        //已清服务器数据,直接上传
        if ("1".equals(local_del_server_flag)) {
            uploadLocalData();
        } else {//先清服务器数据,成功之后再上传
            new Thread(delLeaderBoardRunnable).start();
        }
        //uploadLocalData();

        return super.onStartCommand(intent, flags, startId);
    }

    Runnable UnBindRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "---UnBindRunnable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(getApplicationContext(), R.raw.server);
            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.bind.unbind", "http://app.appscomm.cn/sport/api/device_unbind");
            Logger.d(TAG, "请求地址：" + url);

            String method = "post";

            String uid = (String) ConfigHelper.getCommonSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);

            String DeviceID = (String) ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, 1);
            String params = "userId=" + uid + "&watchId=" + DeviceID;

            int respondStatus = mHttpUtil.httpReq(method, url, params);
            String respondBody = mHttpUtil.httpResponseResult;

            HttpResDataService httpResDataService = new HttpResDataService(getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody, "5"); //5是解绑

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: //解绑成功
                    mHandler.obtainMessage(UnbindingSuccess, "UnBingOK!").sendToTarget();

                    break;
                case 1:

                case 7784:  //Fail
                    mHandler.obtainMessage(UnbindingFail, "UnBindFail!").sendToTarget();
                    break;

                default:
                    mHandler.obtainMessage(UnbindingFail, "UnBindFail!").sendToTarget();
                    break;
            }

        }

    };

    Runnable delLeaderBoardRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "---delLeaderBoardRunnable---");
            String url = LeaderBoardUrl.url_deleteLeaderBoard;
            String seq = LeaderBoardUrl.createRandomSeq();
            Logger.d(TAG, "请求地址：" + url);

            String accountId = (String) ConfigHelper.getCommonSharePref(getApplication(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_EMAIL_ITEM_KEY, 1);
            String DeviceID = (String) ConfigHelper.getSharePref(getApplication(), PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, 1);

            int versionCode = 0;
            try {
                versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String versionNo = "" + versionCode;

            JSONObject detailObject = new JSONObject();
            try {
                detailObject.put("seq", seq);
                detailObject.put("versionNo", versionNo);
                detailObject.put("clientType", "andriod");
                detailObject.put("customerCode", "appscomm");
//                detailObject.put("customerCode", "3PLUS");
                detailObject.put("accountId", accountId);
                detailObject.put("deviceId", DeviceID);
                detailObject.put("dateTime", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                int respondStatus = mHttpUtil.httpPostWithJSON(url, detailObject.toString());
                String respondBody = mHttpUtil.httpResponseResult;
                if (respondBody.indexOf("\"code\"") != -1 && respondBody.indexOf("\"msg\"") != -1
                        && respondBody.indexOf("\"resMap\"") != -1) {

                    JSONObject jsonObj = new JSONObject(respondBody);
                    String result = jsonObj.getString("code");
                    Logger.i(TAG, ">>>>>resultCode:" + result);
                    if ("0".equals(result)) {//成功的结果码
                        new Thread(UnBindRunnable).start();
                        //TODO 丢包问题？不太确定...
//                        mHandler.obtainMessage(UnbindingSuccess, "UnBingOK!").sendToTarget();
                    } else {
                        mHandler.obtainMessage(UnLeaderBoardFail, "UnLeaderBoardFail!").sendToTarget();
                    }
                } else {
                    mHandler.obtainMessage(UnLeaderBoardFail, "UnLeaderBoardFail!").sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.obtainMessage(UnLeaderBoardFail, "UnLeaderBoardFail!").sendToTarget();
            }
        }

    };

    private void uploadLocalData() {
        Logger.d(TAG, "upLoadLocalData....");

        List<SportsData> sportlist = dbService.getSportsDataList();
        List<SleepData> sleepList = dbService.getSleepDataList();
        List<HeartRateData> heartRateList = dbService.getHeartRateDataList();
        Logger.d(TAG, "sportlist...." + sportlist.toString());
        Logger.d(TAG, "SleepData...." + sleepList.toString());
        Logger.d(TAG, "HeartRateData...." + heartRateList.toString());

        if (null != sportlist && sportlist.size() > 0) {
            PublicData.dataSendedInfo.curCount = sportlist.size();
            PublicData.dataSendedInfo.curTodayCount = 0;
            pkList.clear();
            for (SportsData mSportsData : sportlist) {
                int sid = mSportsData.sid;

                pkList.add(sid);
            }

            if (cloudDataService == null) {
                cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
                cloudDataService.setData();
            }

            isSportSave = true;

            cloudDataService.saveCloudSportData(sportlist);
        } else if (null != sleepList && !sleepList.isEmpty() && sleepList.size() > 0) {
            //	isSleepSave = true;
//    		cloudDataService.setData();
//			cloudDataService.saveCloudSleepData(list);
            if (cloudDataService == null) {
                cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
                cloudDataService.setData();
            }
            saveCloudSleepData(sleepList);
        } else if (null != heartRateList && !heartRateList.isEmpty() && heartRateList.size() > 0) {
            if (cloudDataService == null) {
                cloudDataService = new CloudDataService(getApplicationContext(), mHandler);
                cloudDataService.setData();
            }
            cloudDataService.saveCloudHeartRateData(heartRateList);
        } else {
//            broadcastUpdate(UPLOAD_DATA_OK);
//            broadcastUpdate(UPLOAD_SERVICE_DESTROY);
            broadcastUpdate(UPLOAD_DATA_NODATA);

            stopSelf();
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
            //	mHandler.obtainMessage(HIDDEN_DIALOG, "HIDDEN_DIALOG").sendToTarget();
            return;
        }

        //存放多段睡眠数据
        List<List<SleepData>> list = new ArrayList<List<SleepData>>();
        //存放单段睡眠数据
        List<SleepData> list1 = null;
        //所有未上传的睡眠原始数据
        for (SleepData sleepData : sleepList) {
            int id = sleepData.sleep_id;
            int sleep_type = sleepData.sleep_type;
            long sleep_time_stamp = sleepData.sleep_time_stamp;

            Logger.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>id:" + id + "|type:" + sleep_type + "|time:"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(sleep_time_stamp)));

            //0x00：睡着， 0x01：浅睡， 0x02：醒着，0x03：准备入睡，0x10（16）：进入睡眠模式；0x11（17）：退出睡眠模式

            if (sleep_type == 16) {
                list1 = new ArrayList<SleepData>();
                SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                list1.add(mData); //每段睡眠的开始

            } else if (sleep_type == 17) {
                if (list1 != null) {
                    SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                    list1.add(mData); //每段睡眠的结束

                    list.add(list1);

                    list1 = null;
                }

            } else {
                if (list1 != null) {
                    SleepData mData = new SleepData(id, sleep_type, sleep_time_stamp);
                    list1.add(mData);

                    //list.add(list1);		// summer: add
                }
            }
        }

        Logger.d(TAG, "======================list.size():" + list.size());
        HttpUtil httpUtil = new HttpUtil(getApplicationContext());
        PropertiesUtil pu = new PropertiesUtil();
        pu.initResRawPropFile(getApplicationContext(), R.raw.server);
        Properties props = pu.getPropsObj();

        for (int i = 0; i < list.size(); i++) {
            List<SleepData> oneList = list.get(i);

            // 将日历当天的24小时的睡眠原始数据，转换成睡眠图表要的数据结构：key值有：	"AWAKE","LIGHT","DEEP" 3组。
            Map<String, List<SleepTime>> mMapOneDaySleepTime = DataService.getOneDaySleepTimeListlocal(oneList);

            // 浅睡时间，深睡时间：单位秒
            //   Map<String, List<SleepTime>> mMapOneDaySleepTime = DataService.getOneDaySleepTimeListlocal(oneList);

            // 浅睡时间，深睡时间：单位秒
            long intLightSleep = DataService.totalLightSleep(mMapOneDaySleepTime) / 1000L;
            long intDeepSleep = DataService.totalDeepSleep(mMapOneDaySleepTime) / 1000L;

            // 总睡眠小时数
            int slept_hours = (int) (intLightSleep + intDeepSleep) / (60 * 60);
            // 总睡眠分钟数
            int slept_mins = (int) (intLightSleep + intDeepSleep) / 60 % 60;

            // 2014-06-16


            // 20140806
            long intSleepDayTime = DataService.totalSleepTime(mMapOneDaySleepTime) / 1000L;
            long intAwakeDayTime = DataService.totalAwakeTime(mMapOneDaySleepTime) / 1000L; //SWS+  intSleepDayTime; //清醒时长等于：入睡+清醒
            // 醒来次数
            int awake_times = DataService.awakeTimes2(mMapOneDaySleepTime);

			/*int awake_times = DataService.awakeTimes(mMapOneDaySleepTime);
            int awake_time2 = DataService.awakeTimes2(mMapOneDaySleepTime);
			if (awake_time2 > 0) {
				awake_times = awake_times - awake_time2 - 1;
				if (awake_times < 0) {
					awake_times = 0;
				}
			}*/

            // 浅睡小时数、分钟数
            int light_sleep_hours = (int) intLightSleep / (60 * 60);
            int light_sleep_mins = (int) intLightSleep / 60 % 60;
            // 深睡小时数、深睡分钟数
            int deep_sleep_hours = (int) intDeepSleep / (60 * 60);
            int deep_sleep_mins = (int) intDeepSleep / 60 % 60;
            // 醒来小时数、分钟数
            int awake_hours = (int) intAwakeDayTime / (60 * 60);
            int awake_mins = (int) intAwakeDayTime / 60 % 60;

            // 入睡小时数、分钟数
            int sleep_hours = (int) intSleepDayTime / (60 * 60);
            int sleep_mins = (int) intSleepDayTime / 60 % 60;

            // 在床上总小时数、在床上总分钟数
            int total_bed_hours = (int) (intLightSleep + intDeepSleep + intAwakeDayTime + intSleepDayTime) / (60 * 60);
            int total_bed_mins = (int) (intLightSleep + intDeepSleep + intAwakeDayTime + intSleepDayTime) / 60 % 60;

            int awakeDuration = awake_hours * 60 + awake_mins;
            int lightDuration = light_sleep_hours * 60 + light_sleep_mins;
            int deepDuration = deep_sleep_hours * 60 + deep_sleep_mins;
            // int sleepDuration = lightDuration + deepDuration;
            int sleepDuration = sleep_hours * 60 + sleep_mins;
            int totalDuration = total_bed_hours * 60 + total_bed_mins;

            Logger.d(TAG, "======================list.size()-----subSize:" + oneList.size());
            String tmp = "";
            long startTime = 0;
            long endTime = 0;
            for (int j = 0; j < oneList.size(); j++) {
                SleepData sleepData = oneList.get(j);
                if (sleepData.sleep_type == 3) {
                    startTime = sleepData.sleep_time_stamp;
                }
                if (sleepData.sleep_type == 17) {
                    endTime = sleepData.sleep_time_stamp;
                }
                if (!"".equals(tmp)) {
                    tmp = tmp + ",";
                }
                Logger.i("test-test", "时间戳:" + sleepData.sleep_time_stamp + " 时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(sleepData.sleep_time_stamp)));
                tmp += "{\"startTime\":" + sleepData.sleep_time_stamp + ",\"status\":" + sleepData.sleep_type + "}";
            }

            String userId = (String) ConfigHelper.getCommonSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);
            String watchId = (String) ConfigHelper.getSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_ID_ITEM,
                    ConfigHelper.DATA_STRING);
            String type = PublicData.getDeviceTypeByWatchId(watchId);


            String sleepDate = " ";
            if (CommonUtil.isPreSetTimeCrossDay(PublicData.appContext2)) {

                boolean isCrossAday = (TimesrUtils.getUnixDate(endTime / 1000L) - TimesrUtils.getUnixDate(startTime / 1000L)) > 0;
                if (isCrossAday) {

                    sleepDate = "\"sleepDate\":" + endTime + ",";
                } else {

//                        if (TimesrUtils.getUnixHours(endTime / 1000L) < 12) {
                    if (!CommonUtil.isSleepTimeInPreSet(PublicData.appContext2, TimesrUtils.getUnixMins(startTime / 1000L), TimesrUtils.getUnixMins(endTime / 1000L))) {

                        // 算成当天
                        sleepDate = "\"sleepDate\":" + endTime + ",";
                    } else {

                        //往后推一天
                        sleepDate = "\"sleepDate\":" + (endTime + 3600 * 24 * 1000) + ",";
                    }
                }
            }

            reqUrl = props.getProperty("server.upload.sleep.data", "http://app.appscomm.cn/appscomm/api/sleep/record/upload");
            reqParams = "{\"personId\":\"" + userId + "\",\"deviceType\":" + "\"" + type + "\",\"startTime\":" + startTime + ",\"endTime\":" + endTime
                    + ",\"quality\":99," + "\"sleepDuration\":" + sleepDuration + "," + "\"awakeDuration\":" + awakeDuration + "," + "\"lightDuration\":"
                    + lightDuration + "," + sleepDate + "\"deepDuration\":" + deepDuration + "," + "\"totalDuration\":" + totalDuration + "," + "\"awakeCount\":"
                    + awake_times + "," + "\"details\":[" + tmp + "]}";
//            DBService dbService = new DBService(GlobalApp.globalApp.getApplicationContext());
//            ArrayList<SleepDataCache> sleepCacheList=new ArrayList<>();
//            sleepCacheList.add(new SleepDataCache(1,startTime,endTime,sleepDuration,awakeDuration,lightDuration,deepDuration,totalDuration,"",1,1,1,1,1));
//            dbService.saveSleepCacheData(sleepCacheList);

            isSleepSave = true;
            Thread mThread = new Thread(mRunnable);
            mThread.start();


        }
    }

    /*
     * 保存云端睡眠数据线程
     */
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---mRunnable---");

            Logger.d(TAG, ">>云端请求地址：" + reqUrl);
            Logger.d(TAG, ">>云端请求参数：" + reqParams);

            HttpUtil httpUtil = new HttpUtil(getApplicationContext());
            int respondStatus = httpUtil.httpPostWithJSON(reqUrl, reqParams);
            String respondBody = httpUtil.httpResponseResult;

            HttpResDataService httpResDataService = new HttpResDataService(getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody, "");

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: //成功
                    //保存云端睡眠数据成功后发送消息
                    mHandler.obtainMessage(2001, "operation success!").sendToTarget();

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


    @Override
    public void onDestroy() {
        Logger.i(TAG, "onDestroy().");
        super.onDestroy();


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


}

	
	





