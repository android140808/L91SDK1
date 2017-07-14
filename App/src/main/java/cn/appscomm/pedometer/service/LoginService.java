package cn.appscomm.pedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import apps.utils.CommonUtil;
import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.LeaderBoardUrl;
import apps.utils.Logger;
import apps.utils.PropertiesUtil;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.activity.BaseSettingActivity;
import cn.appscomm.pedometer.activity.SetUpDeviceActivity1;
import cn.appscomm.pedometer.activity.SetUpDeviceL38iActivity;
import cn.appscomm.pedometer.activity.WelcomeActivity;
import cn.appscomm.pedometer.call.CallReceiver;
import cn.appscomm.pedometer.model.SportDataCache;
import cn.l11.appscomm.pedometer.activity.R;

public class LoginService extends Service {
    public LoginService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final static String TAG = "LoginService";

//    private String[] titles; // 图片标题
//    private TextView tv_title, login_forgot_password;
    //顶部时间、电量
//    private TextView top_title_time, top_title_battery;
//    private EditText login_email, login_password;
//
//    private Button btn_login;

    private Intent mIntent;
    //    private ProgressDialog loginProgressDialog = null;
    private Thread loginThread;

    private HttpUtil httpUtil = new HttpUtil();

    private CloudDataService cloudDataService = null;
    public String respondBody = "";

    private final int CLOUD_GET_FAIL = 5604;
    private final int CLOUD_GET_SUCCESS = 5605;
    private final int GET_NEW_FW = 5607;
    private List<SportDataCache> mSportDataCacheList;
    private CallReceiver callReceiver;
    private DBService dbService;
    private long SynDate_Begin = 0, SynDate_End = 0; //检查服务器的数据同步时间区间。
    private String SynDate_BeginS = "", SynDate_EndS = ""; //检查服务器的数据同步时间区间字符串。
    private boolean IsGetDataSuccss = false;
    private long serverCurDate = 0; //服务器当前的时间
    private long LastStartDate = 0;
    private boolean firstEnter = true;
    private String serverFWVer = "";

    private int offset = 0;


    private boolean canLoginProc = true; //是否响应登陆按钮

    public boolean isShowMsgTip = false; //是否显示下载成功失败的提示


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "LoginService creates");
        PublicData.setCurrentLang(this);  // 设置当前的语言

        CommonUtil.updateGobalcontext(this);
        CommonUtil.refreshCacheRegion(this);

        String email = (String) ConfigHelper.getSharePref(getApplicationContext(), PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_EMAIL_ITEM_KEY,
                ConfigHelper.DATA_STRING);		// summer: test
//        PublicData.CURRENT_BIND_ID_ITEM = email;

        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        dbService = new DBService(this);
        httpUtil = new HttpUtil(this);
        initView();
//        setListeners();

        cloudDataService = new CloudDataService(LoginService.this, getDataHandler);
        mSportDataCacheList = new ArrayList<SportDataCache>();


        TimeZone tz = TimeZone.getDefault();
        offset = tz.getRawOffset();


        Intent intent = new Intent(this,MyPushMsgService.class);
        stopService(intent);
        Intent intentL38i = new Intent(this,MyPushMsgL38iService.class);
        stopService(intentL38i);
    }

    private void broadcastUpdate(String s) {
        Intent intent = new Intent(s);

        sendBroadcast(intent);
    }


    private int readMissCall() {
        int result = 0;
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE
        }, " type=? and new=?", new String[]{
                CallLog.Calls.MISSED_TYPE + "", "1"
        }, "date desc");

        if (cursor != null) {
            result = cursor.getCount();
            Logger.d(TAG, "<<==cursor count is " + result);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex("number"));//
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String date = cursor.getString(cursor.getColumnIndex("date"));

                Date d = new Date(Long.valueOf(date));
                String sdate = sdf.format(date);

                Logger.d(TAG, "<<<==miss call number=" + number + "/type=" + type + "/date=" + sdate);
            }
            cursor.close();
        }
        return result;
    }


    private void initView() {

        String share_login_username = (String) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.LOGIN_USERNAME_KEY, ConfigHelper.DATA_STRING);
        String share_login_password = (String) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.LOGIN_PASSWORD_KEY, ConfigHelper.DATA_STRING);

        int isLoginOut = (Integer) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.LOGOUT_KEY, ConfigHelper.DATA_INT);

        if ((!"".equals(share_login_username) && !"".equals(share_login_password))) {

            if (isLoginOut != 1) procLogin();
            Logger.d(TAG, "Not Need Del Cache Data");
        } else {


        }


    }

   /* public void setListeners() {
        btn_login.setOnClickListener(new ClickListener());
        login_forgot_password.setOnClickListener(new ClickListener());
    }*/

    // 返回按钮响应事件：
    public void btn_return_clicked(View view) {
        Intent mIntent = new Intent(this,
                WelcomeActivity.class);

        mIntent.putExtra("noautologin", 1);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);

        stopSelf();
    }

    /*private boolean check() {

        if ("".equals(login_email.getText().toString())) {
            DialogUtil.commonDialog(LoginService.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_null));

            return false;
        }

        if (!CommonUtil.emailFormat(login_email.getText().toString().trim())) {
            DialogUtil.commonDialog(LoginService.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_email_wrong));

            return false;
        }

        if ("".equals(login_password.getText().toString())) {
            DialogUtil.commonDialog(LoginService.this,
                    getString(R.string.app_name),
                    getString(R.string.reg_password_null));

            return false;
        }

        return true;
    }
*/
    /*class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_login:
//				Intent intent1 = new Intent();
//				intent1.setClass(LoginService.this, MainActivity.class);
//				
//				startActivity(intent1);

                    if (check()) {


                        String share_login_username = (String) ConfigHelper.getSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.LOGIN_USERNAME_KEY, ConfigHelper.DATA_STRING);


                        if (!login_email.getText().toString().trim().equals(share_login_username)) {
                            Logger.d(TAG, "Must Clean  Cache Data");

                            ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYSTEP, 0);

                            ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYDIS, 0.0f);

                            ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYCAL, 0);
                            ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.TEMP_DAYMINS, 0);
                            dbService.delAllSportsCacheData();
                            dbService.delAllSleepCacheData();
                            dbService.deleteSportsData();
                            dbService.deleteSleepData();
                        }

                        procLogin();
                    }

                    break;

                case R.id.login_forgot_password:
                    Intent intent = new Intent();
                    intent.setClass(LoginService.this, ForgotPwdActivity.class);

                    startActivity(intent);
                    break;

                default:
                    break;
            }

        }

    }*/

    public void procLogin() {
        if (httpUtil.isNetworkConnected()) {
//            loginProgressDialog = DialogUtil.logining(LoginService.this);
//            loginProgressDialog.show();

//            loginThread = new Thread(loginRunnable);
//            loginThread.start();
            new Thread(GetTokenRunnable).start();  // 取token

        } else {
            regHandler.obtainMessage(3, getString(R.string.NetWorkError)).sendToTarget();
        }
    }

    /**
     * 登陆线程
     */
   /* Runnable loginRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---loginRunnable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.login.address", "http://app.appscomm.cn/sport/api/login");

            Logger.d(TAG, "请求地址：" + url);
            String pwd = CommonUtil.MD5(login_password.getText().toString());
            String method = "post";
            String params = "account=" + login_email.getText().toString().trim() + "&password=" + pwd+ "&encryptMode=1";;

            int respondStatus = httpUtil.httpReq(method, url, params);
            String respondBody = httpUtil.httpResponseResult;

            HttpResDataService httpResDataService = new HttpResDataService(getApplicationContext());

            Logger.d(TAG, "login get  respondBody:" + respondBody);
            int i = httpResDataService.commonParse(respondStatus, respondBody, "2");

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: //登录成功


                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOGIN_USERNAME_KEY, login_email.getText().toString());
                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOGIN_PASSWORD_KEY, login_password.getText().toString());


                    new Thread(GetTokenRunnable).start();  // 取token


                    break;

                case 1: //服务器返回的错误归类
                    String resultCode = httpResDataService.getResultCode();
                    //String msg = httpUtil.getMessage();
                    String msg = "";


                    if ("1101".equals(resultCode)) {
                        msg = getString(R.string.login_username_wrong);

                    } else if ("1102".equals(resultCode)) {
                        msg = getString(R.string.login_username_exist);

                    } else if ("1105".equals(resultCode)) {
                        msg = getString(R.string.email_exist);

                    }

                    if ("".equals(msg)) {
                        msg = "[" + resultCode + "]ERROR!";
                    }
                    Logger.e(TAG, "msg=>>" + msg);

                    regHandler.obtainMessage(1, msg).sendToTarget();
                    break;

                case 2: //错误的响应信息码
                    regHandler.obtainMessage(2, "ERROR RESPOND INFO!").sendToTarget();
                    break;

                case 3: //JSONException
                    regHandler.obtainMessage(3, "JSONException!").sendToTarget();
                    break;

                case -1: //服务器未响应
                    regHandler.obtainMessage(-1, "SERVER IS NOT RESPOND!").sendToTarget();
                    break;

                case -2: //ClientProtocolException
                    regHandler.obtainMessage(-2, "ClientProtocolException!").sendToTarget();
                    break;

                case -3: //ParseException
                    regHandler.obtainMessage(-3, "ParseException!").sendToTarget();
                    break;

                case -4: //IOException
                    regHandler.obtainMessage(-4, "IOException!").sendToTarget();
                    break;

            }

        }

    };*/


    Runnable GetGoalRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetGoalRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.goal.get", "http://app.appscomm.cn/appscomm/api/sport-info/target/");


            Logger.d(TAG, "请求地址：" + url);

            ///ring method = "get";
            String url2 = "";
            String uid = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);

            String params = "personId=" + uid;


            url2 = url + '/' + uid;
            int respondStatus = httpUtil.httpGetReq(url2);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "6"); // 6是get goal

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: // 成功
                    new Thread(GetRemindRunnable).start();
                    //			regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();


                    break;


                default:
                    new Thread(GetRemindRunnable).start();
                    //regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();
                    break;

            }

        }

    };


    //server.set_notisw

    Runnable GetSleepTimeRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetSleepTimeRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.get_sleeptime", "http://app.appscomm.cn/appscomm/api/sport-info/target/");


            Logger.d(TAG, "请求地址：" + url);

            ///ring method = "get";
            String url2 = "";
            String uid = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);


            String watchId = ConfigHelper.GetBind_DN(LoginService.this);

            String params = "userId=" + uid + "&watchId=" + watchId;
            Logger.d(TAG, "请求Params：" + params);

            String method = "POST";
            int respondStatus = httpUtil.httpReq(method, url, params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    LoginService.this);

            int i = -1;


            try {
                i = httpResDataService.commonParse(respondStatus, respondBody,
                        "200"); // 6是get goal

                Logger.i(TAG, "------------->>>:" + i);

                switch (i) {
                    case 0: // 成功

                        Logger.d(TAG, "sleeptime  " + respondBody);
                        parseSleepTimeData(respondBody);
                        // regHandler.obtainMessage(0,
                        // "LOGIN SUCCESS!").sendToTarget();

                        break;

                    default:

                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.AUTOSLEEP_SW_ITEM_KEY, true);

                        int bedH, bedM, awakeH, awakeM = 0;


                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_BEDTIME_H_KEY, 23);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_BEDTIME_M_KEY, 0);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_AWAKETIME_H_KEY, 7);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_AWAKETIME_M_KEY, 0);


                        break;

                }

            } catch (Exception e) {
                // TODO: handle exception
            }

            new Thread(GetNotiSwRunnable).start();

        }

    };


    Runnable GetInactivityRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetGetInactivityRunnable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.get_stay_remind", "http://app.appscomm.cn/appscomm/api/sport-info/target/");


            Logger.d(TAG, "请求地址：" + url);

            ///ring method = "get";
            String url2 = "";
            String uid = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);


            String watchId = ConfigHelper.GetBind_DN(LoginService.this);

            String params = "userId=" + uid + "&watchId=" + watchId;
            Logger.d(TAG, "请求Params：" + params);

            String method = "POST";
            int respondStatus = httpUtil.httpReq(method, url, params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    LoginService.this);

            int i = -1;


            try {
                i = httpResDataService.commonParse(respondStatus, respondBody,
                        "200"); // 6是get goal

                Logger.i(TAG, "------------->>>:" + i);

                switch (i) {
                    case 0: // 成功

                        Logger.d(TAG, "getInActivity  " + respondBody);
                        parseInactivityData(respondBody);


                        break;

                    default:

                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_SW_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_INTERVAL_KEY, 30);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_START_KEY, 600);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_END_KEY, 1200);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_WEEKS_KEY, "0000000");

                        break;

                }

            } catch (Exception e) {
                // TODO: handle exception
            }
            // new Thread(GetFirmWareVerRunnable).start();
            new Thread(GetAlwaysOnRunnable).start();

        }
    };

    private void parseInactivityData(String respondBody) {


        try {
            // 正确的响应信息
            if (respondBody.indexOf("\"result\"") != -1
                    && respondBody.indexOf("\"message\"") != -1
                    && respondBody.indexOf("\"data\"") != -1) {

                JSONObject jsonObj = new JSONObject(respondBody);
                String result = jsonObj.getString("result");


                if ("0".equals(result)) {// 成功的结果码


                    JSONObject jsonObj1 = jsonObj.getJSONObject("data");


                    if (jsonObj1 != null && !"".equals(jsonObj1)
                            ) {

                        //   JSONObject jsongObj2 = jsonObj1.getJSONObject("StayRemind");


                        int stauts = jsonObj1.getInt("status");
                        int intterval = jsonObj1.getInt("interval");
                        String startTime = jsonObj1.getString("startTime");
                        String endTime = jsonObj1.getString("endTime");
                        String repeat = jsonObj1.getString("repeat");


                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_SW_KEY, (stauts == 1 ? true : false));
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_INTERVAL_KEY, intterval);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_WEEKS_KEY, repeat);

                        String[] timelist = startTime.split(":");

                        if ((timelist != null) && (timelist.length > 1)) {
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_START_KEY, Integer.parseInt(timelist[0]) * 60 + Integer.parseInt(timelist[1]));


                        }


                        timelist = endTime.split(":");

                        if ((timelist != null) && (timelist.length > 1)) {
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_END_KEY, Integer.parseInt(timelist[0]) * 60 + Integer.parseInt(timelist[1]));


                        }


                    }


                } else {

                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_SW_KEY, false);
                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_INTERVAL_KEY, 30);
                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_START_KEY, 600);
                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_END_KEY, 1200);
                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_WEEKS_KEY, "0000000");

                }
            }

        } catch (Exception e) {
            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_SW_KEY, false);
            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_INTERVAL_KEY, 30);
            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_START_KEY, 600);
            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_END_KEY, 1200);
            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INACTIVITY_WEEKS_KEY, "0000000");

        }


    }


    //取通知开关状态
    Runnable GetNotiSwRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--Get NotiSwRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.get_notisw", "http://app.appscomm.cn/appscomm/api/sport-info/target/");


            Logger.d(TAG, "请求地址：" + url);

            ///ring method = "get";
            String url2 = "";
            String uid = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);


            String watchId = ConfigHelper.GetBind_DN(LoginService.this);

            String params = "userId=" + uid + "&watchId=" + watchId;
            Logger.d(TAG, "请求Params：" + params);

            String method = "POST";
            int respondStatus = httpUtil.httpReq(method, url, params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    LoginService.this);

            int i = -1;


            try {
                i = httpResDataService.commonParse(respondStatus, respondBody,
                        "200"); // 6是get goal

                Logger.i(TAG, "------------->>>:" + i);

                switch (i) {
                    case 0: // 成功

                        Logger.d(TAG, "sleeptime  " + respondBody);
                        if(PublicData.selectDeviceName.equals(PublicData.L38I)){

                        }else{
                            parseNotiSwData(respondBody);
                        }
                        // regHandler.obtainMessage(0,
                        // "LOGIN SUCCESS!").sendToTarget();

                        break;

                    default:
                        Logger.d(TAG, "sleeptime  " + respondBody);
                        if(PublicData.selectDeviceName.equals(PublicData.L38I)){

                        }else{
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);
                        }


                        break;

                }

            } catch (Exception e) {
                // TODO: handle exception
            }

            // new Thread(GetFirmWareVerRunnable).start();
            new Thread(GetInactivityRunnable).start();

        }


        private void parseNotiSwData(String respondBody) {
            // TODO Auto-generated method stub
            try {
                // 正确的响应信息
                if (respondBody.indexOf("\"result\"") != -1
                        && respondBody.indexOf("\"message\"") != -1
                        && respondBody.indexOf("\"data\"") != -1) {

                    JSONObject jsonObj = new JSONObject(respondBody);
                    String result = jsonObj.getString("result");


                    if ("0".equals(result)) {// 成功的结果码


                        JSONObject jsonObj1 = jsonObj.getJSONObject("data");


                        if (jsonObj1 != null && !"".equals(jsonObj1)
                                ) {

                            String noTiSw = jsonObj1.getString("status");


                            if ((noTiSw == null) || (noTiSw.length() != 7)) {

                                //
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
                                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);


                            } else

                            {
		                        	
		          /*              	result非0则修改失败
		                        	status : 1111111(类型:string)
		                        	用7个位标识设置的开关
		                        	1: CALLS
		                        	2:MISSED CALLS
		                        	3:SMS
		                        	4:EMAIL
		                        	5:SOCIAL MEDIA
		                        	6:CALENDAR EVENT
		                        	7:ANTI-LOST*/


                                if ('0' == noTiSw.charAt(0))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, true);

                                if ('0' == noTiSw.charAt(1))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, true);


                                if ('0' == noTiSw.charAt(2))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, true);


                                if ('0' == noTiSw.charAt(3))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, true);


                                if ('0' == noTiSw.charAt(4))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, true);


                                if ('0' == noTiSw.charAt(5))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, true);


                                if ('0' == noTiSw.charAt(6))
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);
                                else
                                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, true);
			                           	
		                           	
		                           	
		                          /* 	ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY,false);
		                    	    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
		                    	     ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
		                    	     ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
		                    	      ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
		                    	      ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);
		                    	    */


                            }

                        }


                    } else {

                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
                        ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);

                    }
                }

            } catch (Exception e) {
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALLSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_MISCALLSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SMSSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_EMAILSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_SOCSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_CALSW_ITEM_KEY, false);
                ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.NOTI_ANTILOST_SW_ITEM_KEY, false);

            }


        }

    };

    Runnable GetFirmWareVerRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "-- GetFirmWareVerRunnable ---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.downloadFwVer", "http://app.appscomm.cn/appscomm/api/sport-info/target/");


            String sn = (String) ConfigHelper.getSharePref(LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);


            Logger.d(TAG, "请求地址：" + url);


            String params = "watchId=" + sn;


            int respondStatus = httpUtil.httpReq("post", url, params);
            String respondBody = httpUtil.httpResponseResult;

            HttpResDataService httpResDataService = new HttpResDataService(
                    getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "20"); //

            Logger.i(TAG, "------------->>>:" + i);


            regHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();
                    Intent intent = new Intent();

                    String Sn = (String) ConfigHelper.getSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.CURRENT_BIND_ID_ITEM, 1);

                    if (false && ((Sn == null) || (Sn.length() != 20))) {
                        if (PublicData.selectDeviceName.equals(PublicData.L38I)){
                        intent.setClass(LoginService.this,
                                SetUpDeviceL38iActivity.class);
                        }else {
                            intent.setClass(LoginService.this,
                                    SetUpDeviceActivity1.class);
                        }


                        startActivity(intent);


                        if (PublicData.GetData4Local) {
                            long curMSec = System.currentTimeMillis() + offset;

                            LastStartDate = TimesrUtils.getUnixDate(curMSec / 1000);
                            updateLocalData(); //更新运动信息
                            Intent intent1 = new Intent(LoginService.this, DownloadSleepDataService.class);
                            startService(intent1);//更新睡眠信息

                        } else stopSelf();


                    } else {
                        Logger.d(TAG, "login 11111");
//                        intent.setClass(LoginService.this, MainActivity.class);
//
//                        startActivity(intent);

                        if (PublicData.GetData4Local) {
                            long curMSec = System.currentTimeMillis() + offset;

                            LastStartDate = TimesrUtils.getUnixDate(curMSec / 1000);
                            updateLocalData(); //更新运动信息
                            Intent intent1 = new Intent(LoginService.this, DownloadSleepDataService.class);
                            startService(intent1);//更新睡眠信息

                        } else stopSelf();


                    }

                }
            });


        }
    };


    Runnable GetTokenRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetTokenRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();


            String userId = (String) ConfigHelper.getCommonSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, ConfigHelper.DATA_STRING);
            String password = (String) ConfigHelper.getSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_PASSWORD_ITEM_KEY, ConfigHelper.DATA_STRING);

            String url = props.getProperty("server.access.token", "http://app.appscomm.cn/appscomm/api/token");

            url = url + "/" + userId;
            String Params = "appId=Apps_android&applyCode=" + password;


            Logger.d(TAG, "请求地址：" + url);

            int respondStatus = httpUtil.httpReq("POST", url, Params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    LoginService.this.getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "8"); // 8是get token


            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: // 成功
                    //regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();
                    //regHandler.obtainMessage(CLOUD_GET_SUCCESS, "BingOK!")
                    //	.sendToTarget();
                    new Thread(GetGoalRunnable).start();   //成功了就取 Goals.

                    break;


                default:
                    regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();
                    //regHandler.obtainMessage(CLOUD_GET_FAIL, "IOException!").sendToTarget();
                    break;

            }

        }

    };



    Runnable GetAlwaysOnRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetAlwaysOnRunnable---");


            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.get_syncsw", "http://app.appscomm.cn/appscomm/api/sport-info/target/set");
            //String url = "http://app.appscomm.cn/appscomm/api/sport-info/target/set";


            String method = "post";

            String uid = (String) ConfigHelper.getCommonSharePref(
                    PublicData.appContext2,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);

            String watchId = ConfigHelper.GetBind_DN(PublicData.appContext2);

            //url = url + "/" + uid;
            Logger.d(TAG, "请求地址：" + url);


            String params = "userId=" + uid + "&watchId=" + watchId ;

            Logger.d(TAG, "请求Params：" + params);

            int respondStatus =  httpUtil.httpReq(method, url, params);
            String respondBody =  httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "200"); // 6是get goal

            Logger.i(TAG, "------------->>>:" + i);


            Message msg = Message.obtain();

            switch (i) {
                case 0: // 成功
                    parseSyncONSwData(respondBody);

                    break;


                default:

                    ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.ALWAYS_ON_KEY, false);

                    break;

            }

            new Thread(getImgPathRunnable).start();
        }

    };

    // insert in between GetAlwaysOnRunnable and GetFirmWareVerRunnable
    private String imgPathUrl;
    private int versionCode;
    private static final String ICON_FOLDER_PATH = "http://28h.fashioncomm.com/sportimg/user/";       // 用户头像存放文件夹（服务器）
    // 获取头像路径
    Runnable getImgPathRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "---downLoadImgRunnable---");
            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);
            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.downloadimg", "http://test.3plus.fashioncomm.com/sport/api/get_upload_img");
            String userId = (String) ConfigHelper.getCommonSharePref(
                    PublicData.appContext2,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);
            String reqParams = "userId=" + userId;
            String reqUrl = url + "?" + reqParams;
            Logger.d(TAG, "reqUrl: " + reqUrl);
            int respondStatus = httpUtil.httpGetReq(reqUrl);
            String respondBody = httpUtil.httpResponseResult;
            if (respondBody.contains("\"result\"") && respondBody.contains("\"message\"") && respondBody.contains("\"data\"")) {

                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(respondBody);
                    String result = jsonObj.getString("result");
                    if (result.equals("0")) {
                        JSONObject jsonObj2 = jsonObj.getJSONObject("data");
                        imgPathUrl = jsonObj2.getString("imgUrl");
                        Logger.d(TAG, "get img path successful: " + imgPathUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            new Thread(queryLeaderBoradAcconutRunnable).start();
        }
    };

    // insert in between GetAlwaysOnRunnable and GetFirmWareVerRunnable
    // 查询排行榜的ddId
    Runnable queryLeaderBoradAcconutRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "---queryLeaderBoradAcconutRunnable---");
            String url = LeaderBoardUrl.url_queryJoin;
            String seq = LeaderBoardUrl.createRandomSeq();
            String versionNo = "" + versionCode;
            String clientType = "android";
            String accountId = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_EMAIL_ITEM_KEY, 1);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("accountId", accountId);
                jsonObject.put("type","1");
                String params = jsonObject.toString();
                int respondStatus = httpUtil.httpPostWithJSON(url, params);
                String respondBody = httpUtil.httpResponseResult;
                Logger.e(TAG, "==>>respondStatus:" + respondStatus);
                switch (respondStatus){
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
                                if (code.equals("0")){
                                    JSONArray jsonArray = jsonObj.getJSONArray("accounts");
                                    if (jsonArray != null && !"".equals(jsonArray) && jsonArray.length() > 0) {
                                        JSONObject json = jsonArray.getJSONObject(0);
                                        int ddId = json.getInt("ddId");
                                        String iconUrl = json.getString("iconUrl");
                                        ConfigHelper.setCommonSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                                PublicData.CURRENT_DDID_ITEM_KEY, ddId);
                                        ConfigHelper.setCommonSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                                PublicData.CURRENT_ICON_PATH_ITEM_KEY, iconUrl);
                                        new Thread(GetFirmWareVerRunnable).start();
                                    } else {        // ddId不存在，创建
                                        new Thread(createDDRunnable).start();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // insert in between GetAlwaysOnRunnable and GetFirmWareVerRunnable
    // 创建排行榜的ddId
    Runnable createDDRunnable = new Runnable() {
        @Override
        public void run() {
            String url = LeaderBoardUrl.url_createDD;
            String seq = LeaderBoardUrl.createRandomSeq();
            String versionNo = "" + versionCode;
            String clientType = "android";
            String accountId = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_EMAIL_ITEM_KEY, ConfigHelper.DATA_STRING);
            String userName = (String) ConfigHelper.getCommonSharePref(
                    LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_NAME_ITEM_KEY, ConfigHelper.DATA_STRING);
            String deviceType = PublicData.getCloudDeviceType(PublicData.L39);
            String iconUrl = ICON_FOLDER_PATH + imgPathUrl;
            if (imgPathUrl == null || imgPathUrl.equals(""))
                iconUrl = "";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("seq", seq);
                jsonObject.put("versionNo", versionNo);
                jsonObject.put("clientType", clientType);
                jsonObject.put("accountId", accountId);
                jsonObject.put("customerCode", "appscomm");
//                jsonObject.put("customerCode", "3PLUS");
                jsonObject.put("deviceType", deviceType);
                jsonObject.put("userName", userName);
                jsonObject.put("iconUrl", iconUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String params = jsonObject.toString();
            int respondStatus = httpUtil.httpPostWithJSON(url, params);
            String respondBody = httpUtil.httpResponseResult;
            Logger.e(TAG, "==>>respondStatus:" + respondStatus);
            switch (respondStatus){
                case 0:
                    try {
                        Logger.i(TAG, "==>>respondBody:" + respondBody);
                        if (respondBody.contains("\"seq\"") && respondBody.contains("\"code\"")
                                && respondBody.contains("\"msg\"")) {
                            JSONObject jsonObj = new JSONObject(respondBody);
                            String seq1 = jsonObj.getString("seq");
                            String code = jsonObj.getString("code");
                            String msg = jsonObj.getString("msg");
                            int ddId = jsonObj.getInt("ddId");
                            Logger.e(TAG, "==>>code:" + code);
                            if (code.equals("0")){
                                ConfigHelper.setCommonSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                        PublicData.CURRENT_DDID_ITEM_KEY, ddId);
                                ConfigHelper.setCommonSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                        PublicData.CURRENT_ICON_PATH_ITEM_KEY, iconUrl);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            new Thread(GetFirmWareVerRunnable).start();
        }
    };


    private void parseSyncONSwData(String respondBody) {


        if (respondBody.indexOf("\"result\"") != -1
                && respondBody.indexOf("\"message\"") != -1
                && respondBody.indexOf("\"data\"") != -1) {

            try {
                JSONObject jsonObj = new JSONObject(respondBody);
                String result = jsonObj.getString("result");


                if ("0".equals(result)) {// 成功的结果码


                    JSONObject jsonObj1 = jsonObj.getJSONObject("data");


                    if (jsonObj1 != null && !"".equals(jsonObj1)
                            ) {

                        String syncON = jsonObj1.getString("status");

                        ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.ALWAYS_ON_KEY, syncON.equals("1") ? true : false);


                    }

                }

            } catch (Exception e) {

                ConfigHelper.setSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.ALWAYS_ON_KEY, false);
            }


        }


    }


    private void parseData(String respondBody) {

        try {
            // 正确的响应信息
            if (respondBody.indexOf("\"result\"") != -1
                    && respondBody.indexOf("\"message\"") != -1
                    && respondBody.indexOf("\"data\"") != -1) {

                JSONObject jsonObj = new JSONObject(respondBody);
                String result = jsonObj.getString("result");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                mSportDataCacheList.clear();

                if ("0".equals(result)) {// 成功的结果码


                    JSONArray jsonArray = (JSONArray) jsonObj
                            .getJSONArray("data");
                    Logger.d(TAG, ">>jsonArray Length:" + jsonArray.length());

                    try {
                        serverCurDate = jsonObj.getLong("servertime");
                        serverCurDate = TimesrUtils.getUnixDate(serverCurDate);
                    } catch (Exception e) {
                        serverCurDate = -1;

                    }
                    Logger.d(TAG, "serverTime is " + serverCurDate);

                    if (jsonArray != null && !"".equals(jsonArray)
                            && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);

                            int steps = json.getInt("steps");
                            // int cal = json.getInt("cal")/1000;
                            int cal = json.getInt("cal");

                            float dis = (float) json.getDouble("dist");

                            String mDatetime = json.getString("hours");
                            String mDate = "";
                            String mHours = "";
                            long iDate = 0;
                            int iHours = 0;
                            if (mDatetime.length() == 10) {
                                mHours = mDatetime.substring(8);

                                mDate = mDatetime.substring(0, 8);
                                Logger.d(TAG, "date :" + mDate + "  >>>hours:"
                                        + mHours);

                                try {

                                    iDate = (long) (sdf.parse(mDate).getTime() / 1000);
                                    iDate = TimesrUtils.getUnixDate(iDate);
                                    iHours = Integer.parseInt(mHours);
                                    SportDataCache mspData = new SportDataCache(
                                            iDate, iHours, steps, cal, dis);
                                    Logger.d(TAG, "new cache data : " + mspData);
                                    mSportDataCacheList.add(mspData);
                                } catch (ParseException e) {
                                    Logger.d(TAG, "parse date error");

                                    // return;
                                    // TODO Auto-generated catch block

                                }

                            }
                        }

                    }


                    try {

                        //	mStartDate = (long) (sdf.parse(startDate).getTime() / 1000);

                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

                        String startTime = sdf1.format(new Date(SynDate_Begin * 24 * 3600 * 1000L));       //"2014-10-01 00:00:00";
                        String endTime = sdf1.format(new Date(SynDate_End * 24 * 3600 * 1000L));


                        dbService.upDateSportsCacheData(mSportDataCacheList, startTime, endTime);
                        Logger.d(TAG, "update local date region :" + startTime + "---" + endTime);

                    } catch (Exception e) {
                    }

                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void parseSleepTimeData(String respondBody) {

        try {
            // 正确的响应信息
            if (respondBody.indexOf("\"result\"") != -1
                    && respondBody.indexOf("\"message\"") != -1
                    && respondBody.indexOf("\"data\"") != -1) {

                JSONObject jsonObj = new JSONObject(respondBody);
                String result = jsonObj.getString("result");


                if ("0".equals(result)) {// 成功的结果码


                    JSONObject jsonObj1 = jsonObj.getJSONObject("data");


                    if (jsonObj1 != null && !"".equals(jsonObj1)
                            ) {

                        String bedTime = jsonObj1.getString("startTime");
                        String awakeTime = jsonObj1.getString("endTime");
                        String status = jsonObj1.getString("status");

                        Logger.d(TAG, "startTime:" + bedTime + "   awaketime:" + awakeTime + "  status:" + status);

                        if ((status != null) && status.trim().equals("1"))

                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.AUTOSLEEP_SW_ITEM_KEY, true);
                        else
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.AUTOSLEEP_SW_ITEM_KEY, false);

                        int bedH, bedM, awakeH, awakeM = 0;

                        String[] timelist = bedTime.split(":");

                        if ((timelist != null) && (timelist.length > 1)) {
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_BEDTIME_H_KEY, Integer.parseInt(timelist[0]));
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_BEDTIME_M_KEY, Integer.parseInt(timelist[1]));

                        }


                        timelist = awakeTime.split(":");

                        if ((timelist != null) && (timelist.length > 1)) {
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_AWAKETIME_H_KEY, Integer.parseInt(timelist[0]));
                            ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.SLEEP_AWAKETIME_M_KEY, Integer.parseInt(timelist[1]));


                        }
                    }


                }
            }

        } catch (Exception e) {
        }


    }


    private Handler getDataHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {
                case 0:
                    IsGetDataSuccss = true;
                    Logger.d(TAG, "get data success" + '\n' + "result data :" + cloudDataService.respondBody);
                    respondBody = cloudDataService.respondBody;
                    parseData(respondBody);

                    if ((SynDate_End > 0) && (SynDate_Begin) > 0) {

                        Logger.d(TAG, "appcontext" + PublicData.appContext2 + SynDate_Begin + "," + SynDate_End + "," + serverCurDate);

                        CommonUtil.setSynSportDateToLocal(PublicData.appContext2, SynDate_Begin, SynDate_End, serverCurDate);
                        Logger.d(TAG, "appcontext2" + PublicData.appContext2);

                    }

                    broadcastUpdate(DownloadSportDataService.DOWNLOAD_DATA_OK);
                    //	Toast.makeText(getApplicationContext(), "download 1", Toast.LENGTH_LONG).show();

                    //Toast.makeText(LoginService.this, "Download data successful!", Toast.LENGTH_LONG).show();


                    long startDate_old = 0;
                    try {
                        startDate_old = (Long) ConfigHelper.getSharePref(PublicData.appContext2,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.BEGIN_SYNSPORTDATE, ConfigHelper.DATA_LONG);


                    } catch (Exception e) {
                        startDate_old = 0;
                        // TODO: handle exception
                    }

                    long curMSec = System.currentTimeMillis() + offset;


                    if (TimesrUtils.getUnixDate(curMSec / 1000) - startDate_old < 31 * 3) //未取满最近3个月cache
                    {
                        LastStartDate = startDate_old;
                        getDataHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                cloudDataService.isShowMsgTip = false;
                                updateLocalData();

                            }
                        }, 10000);


                    } else {
                        getDataHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                stopSelf();

                            }
                        }, 2000);


                    }


                    break;
                default:

                    getDataHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (!IsGetDataSuccss) updateLocalData();

                        }
                    }, 5000);


                    break;


            }
		/*	Intent intent = null;
			intent.setClass(LoginService.this, MainActivity.class);
			startActivity(intent);
			finish();*/


        }


    };


    private Handler regHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String Sn = "";
            Intent intent = null;

            switch (msg.what) {

		/*	case CLOUD_GET_FAIL:

				Logger.d(TAG, (String) msg.obj);
				if (loginProgressDialog != null)
					loginProgressDialog.dismiss();

				PublicData.InitDragListData(getApplicationContext());

				 intent = new Intent();

				 Sn = (String) ConfigHelper.getSharePref(
						LoginService.this,
						PublicData.SHARED_PRE_SAVE_FILE_NAME,
						PublicData.CURRENT_BIND_ID_ITEM, 1);

				if ((Sn == null) || (Sn.length() != 20))
					intent.setClass(LoginService.this,
							SetUpDeviceActivity.class);
				else
					intent.setClass(LoginService.this, MainActivity.class);

				startActivity(intent);
				finish();
				break;*/

                case GET_NEW_FW:


                    Logger.i(TAG, "GET NEW FW.....................");


                    int serverFWMainVer = 0, serverFWsubVer = 0, old_FwMain = 0, old_Fwsub = 0;

                    String[] verlist=null;
                    if(serverFWVer.indexOf("N")!=-1){

                    }else{
                        verlist = serverFWVer.split("\\.");
                    }

                    if ((verlist != null) && (verlist.length > 1)) {
                        serverFWMainVer = Integer.parseInt(verlist[0].trim());

                        serverFWsubVer = Integer.parseInt(verlist[1].trim());


                        try {
                            old_FwMain = (Integer) ConfigHelper.getSharePref(LoginService.this,
                                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                    PublicData.SERVER_FW_MAJOR_VER, ConfigHelper.DATA_INT);


                            old_Fwsub = (Integer) ConfigHelper.getSharePref(LoginService.this,
                                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                    PublicData.SERVER_FW_MINOR_VER, ConfigHelper.DATA_INT);
                        } catch (Exception e) {

                            old_FwMain = 0;
                            old_Fwsub = 0;
                            // TODO: handle exception
                        }


                        if ( (old_FwMain+old_Fwsub)>0  &&  ( (old_FwMain < serverFWMainVer) || ((old_Fwsub < serverFWsubVer) && (old_FwMain <= serverFWMainVer)))   ) {
                            ConfigHelper.setSharePref(LoginService.this,
                                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                    PublicData.ISSHOW_SERVER_FW_UPDATE, 1);
                            //需要更新标志位

                        }


                        ConfigHelper.setSharePref(LoginService.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.SERVER_FW_MAJOR_VER, serverFWMainVer);

                        ConfigHelper.setSharePref(LoginService.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.SERVER_FW_MINOR_VER, serverFWsubVer);
					
				/*	
					public final static String  SERVER_FW_MAJOR_VER ="SERVER_FW_MAJOR_VER";       //设备软件主版本                
					public final static String  SERVER_FW_MINOR_VER= "SERVER_FW_MINOR_VER";     //从版本
					
					public final static String  ISSHOW_SERVER_FW_UPDATE = "ISSHOW_SERVER_FW_UPDATE";*/


                    }

                    regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();

                    break;


                case 0: // 注册成功
                    Logger.d(TAG, (String) msg.obj);

                    ConfigHelper.setSharePref(LoginService.this, PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.LOGOUT_KEY, 0);

                    PublicData.InitDragListData(getApplicationContext());

                    ConfigHelper.setSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.TOTAL_TARGET_STEPS_KEY,
                            PublicData.cloud_goal_step);
                    ConfigHelper.setSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.TOTAL_TARGET_DISTANCE_KEY,
                            PublicData.cloud_goal_dis);
                    ConfigHelper.setSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.TOTAL_TARGET_CALORIES_KEY,
                            PublicData.cloud_goal_cal);
                    ConfigHelper.setSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY,
                            PublicData.cloud_goal_activity);
                    ConfigHelper.setSharePref(LoginService.this,
                            PublicData.SHARED_PRE_SAVE_FILE_NAME,
                            PublicData.TOTAL_TARGET_SLEEP_KEY,
                            PublicData.cloud_goal_sleep);


                    File imgfile2 = new File(PublicData.SAVE_IMG_PATH, BaseSettingActivity.SAVED_FACE_IMG);

                    if (imgfile2.exists()) {
                        imgfile2.delete();
                    }


                    // new Thread(GetFirmWareVerRunnable).start();

                    new Thread(GetSleepTimeRunnable).start();
                    break;

                case 1: //服务器返回错误码归类说明
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            (String) msg.obj);
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;

                case -1: // 服务器无响应
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            "Server not respond");
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;

                case -2: // ClientProtocolException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            "ClientProtocolException");
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;

                case -3: // ParseException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            "ParseException");
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;

                case -4: // IOException
                    Logger.d(TAG, (String) msg.obj);
                    DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            LoginService.this.getString(R.string.IOException));
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;

                case 3: // 没有网络连接
                    Logger.d(TAG, (String) msg.obj);
                    btn_return_clicked(null);
                    /*DialogUtil.commonDialog(getApplicationContext(),
                            LoginService.this.getString(R.string.app_name),
                            "Not Network Connection");*/
//                    if (loginProgressDialog != null)
//                        loginProgressDialog.dismiss();

                    break;


            }
            super.handleMessage(msg);
        }
    };


    private void updateLocalData() {

        //	if (IsGetDataSuccss) return;
        if (cloudDataService == null) {
            cloudDataService = new CloudDataService(LoginService.this, getDataHandler);
        }
			
	/*		long curMSec =0;
		 
		    curMSec = System.currentTimeMillis(); 
		    		// new Date(System.get).getTime();
		    
		    SynDate_End = TimesrUtils.getUnixDate(curMSec/1000); //要查询的结束日期
*/


        long oldStartDate = LastStartDate;
        SynDate_End = oldStartDate;

        //		if (firstEnter) 	SynDate_Begin = SynDate_End - 7; //登陆取一个星期
        SynDate_Begin = SynDate_End - 31; //上一个月
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        try {

            //	mStartDate = (long) (sdf.parse(startDate).getTime() / 1000);


            String startTime = sdf.format(new Date(SynDate_Begin * 24 * 3600 * 1000L)) + " 00:00:00";       //"2014-10-01 00:00:00";
            String endTime = sdf.format(new Date(SynDate_End * 24 * 3600 * 1000L)) + " 23:59:59";


            cloudDataService.setData();
            cloudDataService.getCloudSportData(startTime, endTime, "2");

            Logger.d(TAG, "update date regon is " + startTime + "  -- " + endTime);
            //respondBody = cloudDataService.respondBody;
            //Logger.d(TAG, ">>respondBody:" + respondBody);

        } catch (Exception e) {
        }
    }


    Runnable GetRemindRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--GetRemindRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            //server.remind.get=http://lecomm.appscomm.cn/sport/api/get_remind_info
            String url = props.getProperty("server.remind.get", "http://lecomm.appscomm.cn/sport/api/get_remind_info");

            Logger.d(TAG, "请求地址：" + url);

            ///ring method = "get";
            //  String url2 ="";
            String uid = (String) ConfigHelper.getCommonSharePref(LoginService.this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);


            String watchId = ConfigHelper.GetBind_DN(LoginService.this);


            String params = "userId=" + uid + "&watchId=" + watchId;


            Logger.d(TAG, "请求地址：" + url + "params" + params);
            //url = url+'/';

            int respondStatus = httpUtil.httpReq("post", url, params);
            String respondBody = httpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    LoginService.this);

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "10"); // 10是 get reminds.


            Logger.i(TAG, "------------->>>:" + i);

            new Thread(CheckNewFirmWare).start(); //取版本号
            //	regHandler.obtainMessage(0, "LOGIN SUCCESS!").sendToTarget();

        }
    };


    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                /*int level = intent.getIntExtra("level", 0); //获得当前电量
                int scale = intent.getIntExtra("scale", 100); //获得总电量

                int percent = level * 100 / scale;
                Logger.d(TAG, "===电量：" + percent + "%");
                top_title_battery.setText(percent + "%");

                SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
                String date = sDateFormat.format(new Date());
                top_title_time.setText(date);*/
            }
        }

    };


    Runnable CheckNewFirmWare = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "---CheckNewFireWare---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(LoginService.this, R.raw.server);

            Properties props = pu.getPropsObj();
            //	server.fireware.check=http://app.appscomm.cn/common/api/get_firmware_info
            String url = props.getProperty("server.fireware.check", "http://app.appscomm.cn/common/api/get_firmware_info");

            Logger.d(TAG, "请求地址：" + url);

            int respondStatus = httpUtil.httpGetReq(url);

            String respondBody = httpUtil.httpResponseResult;


            serverFWVer = parseServerNewFW(respondBody);


            regHandler.obtainMessage(GET_NEW_FW, "GET_NEW_FW!").sendToTarget();


        }

    };


    private String parseServerNewFW(String rst) {


        String server_newver = "";
        try {
            if (rst.indexOf("\"result\"") != -1 && rst.indexOf("\"message\"") != -1
                    && rst.indexOf("\"data\"") != -1) {

                JSONObject jsonObj = new JSONObject(rst);
                String result = jsonObj.getString("result");

                if ("0".equals(result)) {//成功的结果码

                    JSONObject jsondata = jsonObj.getJSONObject("data");

                    server_newver = jsondata.getString("version");

                    Logger.d("TAG", "Check Server FW ver :" + server_newver);

                    return server_newver;
                    //保存toKen值

                }
            }
        } catch (JSONException e) {
            return server_newver;

        }

        return server_newver;

    }


}
