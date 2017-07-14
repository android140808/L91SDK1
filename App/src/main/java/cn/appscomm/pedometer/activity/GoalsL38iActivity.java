package cn.appscomm.pedometer.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.Logger;
import apps.utils.PropertiesUtil;
import apps.utils.PublicData;
import cn.appscomm.pedometer.UI.SelectWheelPopupWindow2;
import cn.appscomm.pedometer.protocol.AboutSportSleep.GoalsSetting;
import cn.appscomm.pedometer.protocol.BluetoothUtil;
import cn.appscomm.pedometer.protocol.Commands;
import cn.appscomm.pedometer.protocol.GlobalVar;
import cn.appscomm.pedometer.protocol.IResultCallback;
import cn.appscomm.pedometer.protocol.Leaf;
import cn.appscomm.pedometer.service.BluetoothLeL38IService;
import cn.appscomm.pedometer.service.BluetoothLeService;
import cn.appscomm.pedometer.service.HttpResDataService;
import cn.l11.appscomm.pedometer.activity.R;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

public class GoalsL38iActivity extends Activity implements OnClickListener, IResultCallback {
    private final static String TAG = "GoalsL38iActivity";

    private View rootView;

    private static String deviceType;

    //顶部时间、电量
    private TextView top_title_time, top_title_battery;

    private ImageButton btn_notes_edit;
    private ImageButton btn_notes_edit_ok;


    private ImageButton return_btn = null;
    //
    private RelativeLayout rl_step, rl_distance, rl_minutes, rl_calories, rl_sleep;

    private TextView tv_goal_bar_step_val;
    private TextView tv_goal_bar_distance_val;
    private TextView tv_goal_bar_active_minutes_val;
    private TextView tv_goal_bar_calories_val;
    private TextView tv_goal_bar_sleep_val;

    private EditText et_goal_bar_step_val;
    private EditText et_goal_bar_distance_val;
    private EditText et_goal_bar_active_minutes_val;
    private EditText et_goal_bar_calories_val;
    private EditText et_goal_bar_sleep_val;

    private ImageButton btn_syn = null;

    private int targetSteps; //
    private int targetDistance; //
    private int targetActiveMinutes;
    private int targetCalories; //
    private int targetSleep; //
    //
    private String[] arrStep;
    private String[] arrDistance;
    private String[] arrMinutes;
    private String[] arrCalories;
    private String[] arrSleep;

    private HttpUtil mHttpUtil;
    private Integer current_step_item;  // 保存的当前步数的第几个item参数
    private Integer current_distance_item;
    private Integer current_minutes_item;
    private Integer current_calories_item;
    private Integer current_sleep_item;

    //自定义的步数、距离、运动分钟、卡路里、睡眠 弹出框类
    private SelectWheelPopupWindow2 wheelWindowStep;
    private SelectWheelPopupWindow2 wheelWindowDistance;
    private SelectWheelPopupWindow2 wheelWindowMinutes;
    private SelectWheelPopupWindow2 wheelWindowCalories;
    private SelectWheelPopupWindow2 wheelWindowSleep;

    private boolean CanReLoadData = true;

    private int synGoalMode = 0; //0:all 1:step 2:cal 3:dis 4:mins 5:sleep

    private String mDeviceAddress;
    private Boolean isSynTimeFormatSuccess = false;
    private Boolean IsSyning = false;
    private BluetoothGattService mGattPedometerService;

    private int reCount = 0;
    private int orderType = 0;
    private int retValue = 0;
    private int firmWareSubVersion = 0, firmWareMainVersion = 0, firmWareExtraVersion = 0;
    public static boolean is_showupdatefirm = false;
    private boolean mConnected = false;
    private boolean needConnect = false;


    private boolean mIsBind = false; // service与广播是否已经绑定
    private ProgressDialog mProgressDialog = null; // 进度条
    AlertDialog.Builder builder = null;

    //	private boolean IsShowingProgress = false;
    private boolean AlreadyReturn = false;

    private static final long SCAN_PERIOD = 20000;
    private static final int REQUEST_ENABLE_BT = 5111;
    private final int SYNGOAL_TIMEOUT = 5601;
    private final int SYNGOAL_SUCCESS = 5602;
    private final int SYNGOAL_FAIL = 5603;

    private final int SYNSLEEPGOAL_SUCCESS = 5608;

    private final int CLOUD_SET_SUCCESS = 5606;
    private final int CLOUD_SET_FAIL = 5607;

    private boolean IsSynsuccessed = false;
    private boolean IsTimeOut = false;
    private Random rand = new Random(25);
    private int RandCode = 0;
    private Intent mIntent;
    private boolean IsNeedSynCalDis = false;


    List<Map<String, Object>> list = null;

    private PopupWindow.OnDismissListener lsStep = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            synGoalMode = 1;
            SynClick.onClick(null);
        }
    };


    private PopupWindow.OnDismissListener lsCal = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            synGoalMode = 2;
            SynClick.onClick(null);
        }
    };


    private PopupWindow.OnDismissListener lsDis = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            synGoalMode = 3;
            SynClick.onClick(null);
        }
    };


    private PopupWindow.OnDismissListener lsSleep = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            ShowProgressDiag();
            new Thread(SetSleepGoalRunnable).start();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothUtil.getInstance().startBroadcast();
        PublicData.isMenuFlag = true;
        setContentView(R.layout.goals_view);
        initView();
        mHttpUtil = new HttpUtil(this);
        deviceType = (String) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
        Logger.i(TAG, "查询设备的目标值...");
        BluetoothUtil.getInstance().send(new GoalsSetting(this, 1, 0));
    }

    /**
     * 同步目标到服务器
     */
    Runnable SetGoalRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--SetGoalRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(PublicData.appContext2, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.goal.set", "http://app.appscomm.cn/appscomm/api/sport-info/target/set");

            String method = "post";

            String uid = (String) ConfigHelper.getCommonSharePref(PublicData.appContext2, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_USERID_ITEM_KEY, 1);

            url = url + "/" + uid;
            Logger.d(TAG, "请求地址：" + url);
            String params = "personId=" + uid + "&stepsTarget=" + arrStep[current_step_item] + "&distanceTarget="
                    + arrDistance[current_distance_item] + "&activeTimeTarget=" + arrMinutes[current_minutes_item] + "&caloriesTarget=" + arrCalories[current_calories_item]
                    + "&sleepTarget=" + arrSleep[current_sleep_item];

            Logger.d(TAG, "请求Params：" + params);
            int respondStatus = mHttpUtil.httpReq(method, url, params);
            String respondBody = mHttpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    PublicData.appContext2.getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "7"); // 6是get goal

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: // 成功
                    mHandler.obtainMessage(SYNGOAL_SUCCESS, "BingOK!").sendToTarget();
                    break;

                default:
                    mHandler.obtainMessage(SYNGOAL_FAIL, "BingNO!").sendToTarget();
                    break;
            }

        }

    };


    Runnable SetSleepGoalRunnable = new Runnable() {

        @Override
        public void run() {
            Logger.d(TAG, "--SetSleepGoalRunable---");

            PropertiesUtil pu = new PropertiesUtil();
            pu.initResRawPropFile(PublicData.appContext2, R.raw.server);

            Properties props = pu.getPropsObj();
            String url = props.getProperty("server.upload_sleepgoal", "http://app.appscomm.cn/appscomm/api/sport-info/target/set");

            String method = "post";

            String uid = (String) ConfigHelper.getCommonSharePref(
                    PublicData.appContext2,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CURRENT_USERID_ITEM_KEY, 1);

            url = url + "/" + uid;
            Logger.d(TAG, "请求地址：" + url);
            String params = "personId=" + uid + "&target=" + arrSleep[current_sleep_item];

            Logger.d(TAG, "请求Params：" + params);
            int respondStatus = mHttpUtil.httpReq(method, url, params);
            String respondBody = mHttpUtil.httpResponseResult;


            HttpResDataService httpResDataService = new HttpResDataService(
                    PublicData.appContext2.getApplicationContext());

            int i = httpResDataService.commonParse(respondStatus, respondBody,
                    "200"); // 6是get goal

            Logger.i(TAG, "------------->>>:" + i);
            switch (i) {
                case 0: // 成功
                    mHandler.obtainMessage(SYNSLEEPGOAL_SUCCESS, "BingOK!").sendToTarget();
                    break;

                default:
                    mHandler.obtainMessage(SYNGOAL_FAIL, "BingNO!").sendToTarget();
                    break;

            }

        }

    };

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.goals_title);
        TextView tv_save = (TextView) findViewById(R.id.tv_save);
        tv_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(GoalsL38iActivity.this,getString(R.string.save),Toast.LENGTH_SHORT).show();
//                if (builder==null){
//                    builder = new AlertDialog.Builder(GoalsL38iActivity.this);
//                }
//                builder.setIcon(R.drawable.ic_launcher);
//                builder.setTitle(R.string.app_name);
//                builder.setMessage(R.string.success);
//                builder.setPositiveButton(getString(android.R.string.ok), null);
//                builder.show();
                if (!(BluetoothLeService.isConnected || BluetoothLeL38IService.isConnected)) {
//                message.arg1 = 1;//断链
                    Toast.makeText(GoalsL38iActivity.this, getResources().getString(R.string.blue_connect_fail), Toast.LENGTH_SHORT).show();
                    return;
                }

                SynClick.onClick(null);

            }
        });
        top_title_time = (TextView) findViewById(R.id.top_title_time);
        top_title_battery = (TextView) findViewById(R.id.top_title_battery);

        btn_notes_edit = (ImageButton) findViewById(R.id.btn_notes_edit);
        btn_notes_edit_ok = (ImageButton) findViewById(R.id.btn_notes_edit_ok);

        return_btn = (ImageButton) findViewById(R.id.btn_left);

        rl_step = (RelativeLayout) findViewById(R.id.rl_step);
        rl_distance = (RelativeLayout) findViewById(R.id.rl_distance);
        rl_minutes = (RelativeLayout) findViewById(R.id.rl_minutes);
        String deviceType = (String) ConfigHelper.getSharePref(this,
                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
//        if (deviceType.equals(PublicData.L28H)) {
        rl_minutes.setVisibility(View.VISIBLE);
        findViewById(R.id.line_act).setVisibility(View.VISIBLE);
//        } else if (deviceType.equals(PublicData.L38I)) {
//            rl_minutes.setVisibility(View.GONE);
//            findViewById(R.id.line_act).setVisibility(View.VISIBLE);
//        } else {
//            rl_minutes.setVisibility(View.GONE);
//            ((TextView) findViewById(R.id.line_act)).setVisibility(View.GONE);
//        }
        rl_calories = (RelativeLayout) findViewById(R.id.rl_calories);
        rl_sleep = (RelativeLayout) findViewById(R.id.rl_sleep);

        tv_goal_bar_step_val = (TextView) findViewById(R.id.goal_bar_step_val);
        tv_goal_bar_distance_val = (TextView) findViewById(R.id.goal_bar_distance_val);
        tv_goal_bar_active_minutes_val = (TextView) findViewById(R.id.goal_bar_active_minutes_val);
        tv_goal_bar_calories_val = (TextView) findViewById(R.id.goal_bar_calories_val);
        tv_goal_bar_sleep_val = (TextView) findViewById(R.id.goal_bar_sleep_val);

        et_goal_bar_step_val = (EditText) findViewById(R.id.et_goal_bar_step_val);
        et_goal_bar_distance_val = (EditText) findViewById(R.id.et_goal_bar_distance_val);
        et_goal_bar_active_minutes_val = (EditText) findViewById(R.id.et_goal_bar_active_minutes_val);
        et_goal_bar_calories_val = (EditText) findViewById(R.id.et_goal_bar_calories_val);
        et_goal_bar_sleep_val = (EditText) findViewById(R.id.et_goal_bar_sleep_val);
        btn_syn = (ImageButton) findViewById(R.id.btn_right_bluetooth);
        btn_syn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                synGoalMode = 0; //全部同步
                SynClick.onClick(null);
            }
        });

        btn_notes_edit.setOnClickListener(clickListener);
        btn_notes_edit_ok.setOnClickListener(clickListener);

        rl_step.setOnClickListener(clickListener);
        rl_distance.setOnClickListener(clickListener);
        rl_minutes.setOnClickListener(clickListener);
        rl_calories.setOnClickListener(clickListener);
        rl_sleep.setOnClickListener(clickListener);

        ReadGoalData();

        arrStep = new String[100]; // 步数目标范围 : 1000-100000
        int step = 1000;
        for (int i = 0; i < arrStep.length; i++) {
            arrStep[i] = step + step * i + "";
        }

        arrDistance = new String[500]; // 距离目标范围 : 1-500
        int distance = 1;
        for (int i = 0; i < arrDistance.length; i++) {
            arrDistance[i] = distance + distance * i + "";
        }

        arrMinutes = new String[34]; // 运动时间目标范围 : 15--51
        int minutes = 15;
        for (int i = 0; i < arrMinutes.length; i++) {
            arrMinutes[i] = minutes + minutes * i + "";
        }

        arrCalories = new String[100]; // 卡路里目标范围 : 50--5000
        int calories = 50;
        for (int i = 0; i < arrCalories.length; i++) {
            arrCalories[i] = calories + calories * i + "";
        }

        arrSleep = new String[24]; // 睡眠时间目标范围 : 1--24
        int sleep = 1;
        for (int i = 0; i < arrSleep.length; i++) {
            arrSleep[i] = sleep + sleep * i + "";
        }
    }

    /**
     * 进入目标界面，先设置为本地目标值
     */
    private void ReadGoalData() {
        // 步数
        int total_step = (Integer) getSharePref(PublicData.TOTAL_TARGET_STEPS_KEY, ConfigHelper.DATA_INT);
        targetSteps = total_step > 0 ? total_step : PublicData.STEP_GOALS_DEFAULT; // 默认是7000
        current_step_item = (targetSteps / 1000 - 1) < 0 ? 0 : (targetSteps / 1000 - 1);

        // 距离
        int shpDistance = (Integer) getSharePref(PublicData.TOTAL_TARGET_DISTANCE_KEY, ConfigHelper.DATA_INT);
        targetDistance = shpDistance > 0 ? shpDistance : PublicData.DISTANCE_GOALS_DEFAULT; // 默认是5km
        current_distance_item = (targetDistance - 1) < 0 ? 0 : (targetDistance - 1);

        // 运动时间
        int shpActiveMinutes = (Integer) getSharePref(PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY, ConfigHelper.DATA_INT);
        if (GlobalVar.stepTimeGoalsValue != 0) {
            PublicData.ACTIVE_MINUTES_GOALS_DEFAULT = GlobalVar.stepTimeGoalsValue;
        }
        targetActiveMinutes = shpActiveMinutes > 0 ? shpActiveMinutes : PublicData.ACTIVE_MINUTES_GOALS_DEFAULT; // 默认是60
        current_minutes_item = (targetActiveMinutes / 15 - 1) < 0 ? 0 : (targetActiveMinutes / 15 - 1);
        if (targetActiveMinutes == 60) {
            current_minutes_item = 3;
        }

        // 卡路里
        targetCalories = PublicData.CALORIE_GOALS_DEFAULT; // 默认是350
        int shpCalories = (Integer) getSharePref(PublicData.TOTAL_TARGET_CALORIES_KEY, ConfigHelper.DATA_INT);
        targetCalories = shpCalories > 0 ? shpCalories : PublicData.CALORIE_GOALS_DEFAULT; // 默认是350
        current_calories_item = (targetCalories / 50 - 1) < 0 ? 0 : (targetCalories / 50 - 1);

        // 睡眠
        targetSleep = PublicData.SLEEP_GOALS_DEFAULT; // 默认是8
        int shpSleep = (Integer) getSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, ConfigHelper.DATA_INT);
        targetSleep = shpSleep > 0 ? shpSleep : PublicData.SLEEP_GOALS_DEFAULT; // 默认是8
        current_sleep_item = (targetSleep - 1) < 0 ? 0 : (targetSleep - 1);

        tv_goal_bar_step_val.setText("" + targetSteps);
        tv_goal_bar_distance_val.setText("" + targetDistance);
        tv_goal_bar_active_minutes_val.setText("" + targetActiveMinutes);
        tv_goal_bar_calories_val.setText("" + targetCalories);
        tv_goal_bar_sleep_val.setText("" + targetSleep);

        et_goal_bar_step_val.setText("" + targetSteps);
        et_goal_bar_distance_val.setText("" + targetDistance);
        et_goal_bar_active_minutes_val.setText("" + targetActiveMinutes);
        et_goal_bar_calories_val.setText("" + targetCalories);
        et_goal_bar_sleep_val.setText("" + targetSleep);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.d(TAG, "STOP...................");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Logger.w(TAG, "onDestroy()-->");
//        BluetoothUtil.getInstance().endBroadcast();
        PublicData.isMenuFlag = false;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "===onResume()");
        if (CanReLoadData)
            ReadGoalData();
        CanReLoadData = true;
        CloseProgressDiag();
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(PublicData.appContext2, R.string.open_bluetooth_device, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Logger.d(TAG, "MSG is " + msg.what);
            switch (msg.what) {

                // 睡眠目标同步完成
                case SYNSLEEPGOAL_SUCCESS:
                    setSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, Integer.valueOf(arrSleep[current_sleep_item]));
                    CloseProgressDiag();
                    DialogUtil.commonDialog(GoalsL38iActivity.this, getString(R.string.app_name), getString(R.string.success));
                    break;

                // 超时
                case SYNGOAL_TIMEOUT:
                    if (RandCode != msg.arg2) return;
                    CloseProgressDiag();
                    DialogUtil.commonDialog(GoalsL38iActivity.this, getString(R.string.app_name), getString(R.string.setting_failed));
                    break;

                // 目标同步完成
                case SYNGOAL_SUCCESS:
                    setSharePref(PublicData.TOTAL_TARGET_STEPS_KEY, Integer.valueOf(arrStep[current_step_item]));
                    setSharePref(PublicData.TOTAL_TARGET_DISTANCE_KEY, Integer.valueOf(arrDistance[current_distance_item]));
                    setSharePref(PublicData.TOTAL_TARGET_CALORIES_KEY, Integer.valueOf(arrCalories[current_calories_item]));
                    setSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, Integer.valueOf(arrSleep[current_sleep_item]));
                    setSharePref(PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY, Integer.valueOf(arrMinutes[current_minutes_item]));
                    flag++;
                    if (flag == 5) {
                        DialogUtil.commonDialog(GoalsL38iActivity.this, getString(R.string.app_name), getString(R.string.success));
                        flag = 0;
                    }
                    CloseProgressDiag();
                    break;

                // 失败
                case SYNGOAL_FAIL:
                    CloseProgressDiag();
                    DialogUtil.commonDialog(GoalsL38iActivity.this, getString(R.string.app_name), getString(R.string.NetWorkError_1));
                    break;
            }
        }
    };
    private int flag = 0;


    // 返回按钮响应事件：
    public void btn_return_clicked(View view) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }


    private void ShowProgressDiag() {
        if (mProgressDialog == null)
            mProgressDialog = ProgressDialog.show(GoalsL38iActivity.this, null, getString(R.string.syndata), true, true);
        else
            mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private void CloseProgressDiag() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * 同步到设备的对话框
     */
    private OnClickListener SynClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GoalsL38iActivity.this);
            builder.setPositiveButton(getString(android.R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
                            if (mBluetoothAdapter == null) {
                                Toast.makeText(GoalsL38iActivity.this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!mBluetoothAdapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                CanReLoadData = false;
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            } else {
                                setGoals();
                            }
                        }
                    });
            builder.setNegativeButton(getString(android.R.string.no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.setMessage(getString(R.string.synPedometer)).setTitle(getString(R.string.tips));
            AlertDialog b1 = builder.create();
            b1.show();
        }
    };

    // 监听顶部的按钮与跑步目标4个设置步数按钮的监听
    private OnClickListener clickListener = new OnClickListener() {
        int mCurrentItem = 0;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 顶部的左边按钮：添加; 右边按钮：编辑\提交\确定
                // 进入编辑删除状态，编辑按键隐藏，编辑完成按钮出现
                case R.id.btn_notes_edit:
                    btn_notes_edit.setVisibility(View.GONE);
                    btn_notes_edit_ok.setVisibility(View.VISIBLE);

                    tv_goal_bar_step_val.setVisibility(View.GONE);
                    tv_goal_bar_distance_val.setVisibility(View.GONE);
                    tv_goal_bar_active_minutes_val.setVisibility(View.GONE);
                    tv_goal_bar_calories_val.setVisibility(View.GONE);
                    tv_goal_bar_sleep_val.setVisibility(View.GONE);

                    et_goal_bar_step_val.setVisibility(View.VISIBLE);
                    et_goal_bar_distance_val.setVisibility(View.VISIBLE);
                    et_goal_bar_active_minutes_val.setVisibility(View.VISIBLE);
                    et_goal_bar_calories_val.setVisibility(View.VISIBLE);
                    et_goal_bar_sleep_val.setVisibility(View.VISIBLE);
                    break;

                // 退出编辑删除状态，编辑按键出现，编辑完成按钮隐藏
                case R.id.btn_notes_edit_ok:
                    btn_notes_edit.setVisibility(View.VISIBLE);
                    btn_notes_edit_ok.setVisibility(View.GONE);

                    tv_goal_bar_step_val.setText(et_goal_bar_step_val.getText().toString());
                    tv_goal_bar_distance_val.setText(et_goal_bar_distance_val.getText().toString());
                    tv_goal_bar_active_minutes_val.setText(et_goal_bar_active_minutes_val.getText().toString());
                    tv_goal_bar_calories_val.setText(et_goal_bar_calories_val.getText().toString());
                    tv_goal_bar_sleep_val.setText(et_goal_bar_sleep_val.getText().toString());

                    //设置相关信息
                    setSharePref(PublicData.TOTAL_TARGET_STEPS_KEY, Integer.valueOf(et_goal_bar_step_val.getText().toString()));
                    setSharePref(PublicData.TOTAL_TARGET_DISTANCE_KEY, Integer.valueOf(et_goal_bar_distance_val.getText().toString()));
                    setSharePref(PublicData.TOTAL_TARGET_ACTIVE_MINUTES_KEY, Integer.valueOf(et_goal_bar_active_minutes_val.getText().toString()));
                    setSharePref(PublicData.TOTAL_TARGET_CALORIES_KEY, Integer.valueOf(et_goal_bar_calories_val.getText().toString()));
                    setSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, Integer.valueOf(et_goal_bar_sleep_val.getText().toString()));

                    tv_goal_bar_step_val.setVisibility(View.VISIBLE);
                    tv_goal_bar_distance_val.setVisibility(View.VISIBLE);
                    tv_goal_bar_active_minutes_val.setVisibility(View.VISIBLE);
                    tv_goal_bar_calories_val.setVisibility(View.VISIBLE);
                    tv_goal_bar_sleep_val.setVisibility(View.VISIBLE);

                    et_goal_bar_step_val.setVisibility(View.GONE);
                    et_goal_bar_distance_val.setVisibility(View.GONE);
                    et_goal_bar_active_minutes_val.setVisibility(View.GONE);
                    et_goal_bar_calories_val.setVisibility(View.GONE);
                    et_goal_bar_sleep_val.setVisibility(View.GONE);
                    break;

                // 步数
                case R.id.rl_step:
                    mCurrentItem = current_step_item;
                    if (wheelWindowStep == null)
                        wheelWindowStep = new SelectWheelPopupWindow2(GoalsL38iActivity.this, arrStep, 8, current_step_item, new OnWheelScrollListener() {
                            public void onScrollingStarted(WheelView wheel) {
                            }

                            public void onScrollingFinished(WheelView wheel) {
                                mCurrentItem = wheel.getCurrentItem();
                            }
                        }, null, new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // TODO Auto-generated method stub
                                switch (arg0.getId()) {
                                    case R.id.WheelGender:
                                        current_step_item = mCurrentItem;
                                        tv_goal_bar_step_val.setText(arrStep[current_step_item]);
                                        wheelWindowStep.dismiss();
                                        synGoalMode = 1;
//                                        SynClick.onClick(null);
                                        break;
                                    case R.id.WheelDone:
                                        wheelWindowStep.setWheelCurrentItem(current_step_item);
                                        wheelWindowStep.dismiss();
                                        break;
                                }
                            }
                        });
                    wheelWindowStep.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;

                // 距离
                case R.id.rl_distance:
                    mCurrentItem = current_distance_item;
                    if (wheelWindowDistance == null)
                        wheelWindowDistance = new SelectWheelPopupWindow2(GoalsL38iActivity.this, arrDistance, 8, current_distance_item, new OnWheelScrollListener() {
                            public void onScrollingStarted(WheelView wheel) {
                            }

                            public void onScrollingFinished(WheelView wheel) {
                                mCurrentItem = wheel.getCurrentItem();
                            }
                        }, null, new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {

                                switch (arg0.getId()) {
                                    case R.id.WheelGender:
                                        current_distance_item = mCurrentItem;
                                        tv_goal_bar_distance_val.setText(arrDistance[current_distance_item]);
                                        wheelWindowDistance.dismiss();
                                        synGoalMode = 3;
//                                        SynClick.onClick(null);
                                        break;
                                    case R.id.WheelDone:
                                        wheelWindowDistance.setWheelCurrentItem(current_distance_item);
                                        wheelWindowDistance.dismiss();
                                        break;
                                }
                            }
                        });

                    wheelWindowDistance.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;

                // 运动时间
                case R.id.rl_minutes:
                    mCurrentItem = current_minutes_item;
                    if (wheelWindowMinutes == null)
                        wheelWindowMinutes = new SelectWheelPopupWindow2(GoalsL38iActivity.this, arrMinutes, 8, current_minutes_item, new OnWheelScrollListener() {
                            public void onScrollingStarted(WheelView wheel) {

                            }

                            public void onScrollingFinished(WheelView wheel) {
//							current_calories_item = wheel.getCurrentItem();
                                mCurrentItem = wheel.getCurrentItem();
                            }
                        }, null, new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                switch (arg0.getId()) {
                                    case R.id.WheelGender:
                                        current_minutes_item = mCurrentItem;
                                        tv_goal_bar_active_minutes_val.setText(arrMinutes[current_minutes_item]);
                                        wheelWindowMinutes.dismiss();
                                        synGoalMode = 4;
//                                        SynClick.onClick(null);
                                        break;
                                    case R.id.WheelDone:
                                        wheelWindowMinutes.dismiss();
                                        break;
                                }
                            }
                        });
                    wheelWindowMinutes.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;

                // 卡路里
                case R.id.rl_calories:
                    mCurrentItem = current_calories_item;
                    if (wheelWindowCalories == null)
                        wheelWindowCalories = new SelectWheelPopupWindow2(GoalsL38iActivity.this, arrCalories, 8, current_calories_item, new OnWheelScrollListener() {
                            public void onScrollingStarted(WheelView wheel) {
                            }

                            public void onScrollingFinished(WheelView wheel) {
                                mCurrentItem = wheel.getCurrentItem();
                            }
                        }, null, new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                switch (arg0.getId()) {
                                    case R.id.WheelGender:
                                        current_calories_item = mCurrentItem;
                                        tv_goal_bar_calories_val.setText(arrCalories[current_calories_item]);
                                        wheelWindowCalories.dismiss();
                                        synGoalMode = 2;
//                                        SynClick.onClick(null);
                                        break;
                                    case R.id.WheelDone:
                                        wheelWindowCalories.setWheelCurrentItem(current_calories_item);
                                        wheelWindowCalories.dismiss();
                                        break;
                                }
                            }
                        });
                    wheelWindowCalories.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;

                // 睡眠
                case R.id.rl_sleep:
                    mCurrentItem = current_sleep_item;
                    if (wheelWindowSleep == null)
                        wheelWindowSleep = new SelectWheelPopupWindow2(GoalsL38iActivity.this, arrSleep, 8, current_sleep_item, new OnWheelScrollListener() {
                            public void onScrollingStarted(WheelView wheel) {

                            }

                            public void onScrollingFinished(WheelView wheel) {
                                mCurrentItem = wheel.getCurrentItem();
                            }
                        }, null, new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                switch (arg0.getId()) {
                                    case R.id.WheelGender:
                                        current_sleep_item = mCurrentItem;
                                        tv_goal_bar_sleep_val.setText(arrSleep[current_sleep_item]);

                                        wheelWindowSleep.dismiss();
                                        synGoalMode = 5;
//                                        SynClick.onClick(null);
//                                        ShowProgressDiag();
//                                        new Thread(SetSleepGoalRunnable).start();
                                        break;
                                    case R.id.WheelDone:
                                        wheelWindowSleep.setWheelCurrentItem(current_sleep_item);
                                        wheelWindowSleep.dismiss();
                                        break;
                                }
                            }
                        });
                    wheelWindowSleep.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 点击左上角返回键
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goal_btn_left:
                finish();
                break;
        }
    }

    /**
     * 设置目标
     */
    private void setGoals() {
        GlobalVar.stepGoalsValue = Integer.parseInt(arrStep[current_step_item]);
        GlobalVar.distanceGoalsValue = Integer.parseInt(arrDistance[current_distance_item]);
        GlobalVar.calorieGoalsValue = Integer.parseInt(arrCalories[current_calories_item]);
        GlobalVar.sleepGoalsValue = Integer.parseInt(arrSleep[current_sleep_item]);
        GlobalVar.stepTimeGoalsValue = Integer.parseInt(arrMinutes[current_minutes_item]);

        Logger.i(TAG, "设置目标: 运动时长:" + GlobalVar.stepGoalsValue + "设置目标: 步数:" + GlobalVar.stepGoalsValue + " 距离:" + GlobalVar.distanceGoalsValue + " 卡路里:" + GlobalVar.calorieGoalsValue + " 睡眠:" + GlobalVar.sleepGoalsValue);
        LinkedList<Leaf> ls = new LinkedList<Leaf>();
        ls.addLast(new GoalsSetting(this, 4, (byte) 0x00, GlobalVar.stepGoalsValue / 100, (byte) 0x01));
        ls.addLast(new GoalsSetting(this, 4, (byte) 0x01, GlobalVar.calorieGoalsValue, (byte) 0x01));
        ls.addLast(new GoalsSetting(this, 4, (byte) 0x02, GlobalVar.distanceGoalsValue, (byte) 0x01));
        ls.addLast(new GoalsSetting(this, 4, (byte) 0x04, GlobalVar.stepTimeGoalsValue, (byte) 0x01));
        ls.addLast(new GoalsSetting(this, 4, (byte) 0x03, GlobalVar.sleepGoalsValue, (byte) 0x01));
        BluetoothUtil.getInstance().send(ls);
//        switch (synGoalMode) {
//
//            case 1:
//                BluetoothUtil.getInstance().send(new GoalsSetting(this, 4, (byte) 0x00, GlobalVar.stepGoalsValue / 100, (byte) 0x01));
//                break;
//
//            case 2:
//                BluetoothUtil.getInstance().send(new GoalsSetting(this, 4, (byte) 0x01, GlobalVar.calorieGoalsValue, (byte) 0x01));
//                break;
//
//            case 3:
//                BluetoothUtil.getInstance().send(new GoalsSetting(this, 4, (byte) 0x02, GlobalVar.distanceGoalsValue, (byte) 0x01));
//                break;
//            case 4:
//                BluetoothUtil.getInstance().send(new GoalsSetting(this, 4, (byte) 0x04, GlobalVar.stepTimeGoalsValue, (byte) 0x01));
//                break;
//
//            case 5:
//                BluetoothUtil.getInstance().send(new GoalsSetting(this, 4, (byte) 0x03, GlobalVar.sleepGoalsValue, (byte) 0x01));
//                break;
//        }

    }

    /**
     * 获取sp中key对应的value
     *
     * @param key  键
     * @param type value类型
     * @return
     */
    private Object getSharePref(String key, int type) {
        return ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, key, type);
    }

    /**
     * 设置sp中key对应的value
     *
     * @param key   键
     * @param value 值
     * @return
     */
    private boolean setSharePref(String key, Object value) {
        return ConfigHelper.setSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, key, value);
    }

    private LinkedList<Leaf> leafs = new LinkedList<Leaf>();

    @Override
    public void onSuccess(Leaf leaf) {
        BluetoothUtil.getInstance().continueSend();
        int commandCode = leaf.getCommandCode();
        byte action = leaf.getAction();
        if (commandCode == Commands.COMMANDCODE_TARGET_SETTING) {
            // 查询各目标值
            if (action == Commands.ACTION_CHECK) {
                Logger.i(TAG, "步数:" + GlobalVar.stepGoalsValue + " 距离:" + GlobalVar.distanceGoalsValue + " 卡路里:" + GlobalVar.calorieGoalsValue + " 睡眠:" + GlobalVar.sleepGoalsValue);
                leafs.clear();

                if (GlobalVar.stepGoalsValue == 0) {
                    Logger.i("BluetoothUtil", "手环的步数目标值为0，设置默认值!!!");
                    GlobalVar.stepGoalsValue = PublicData.STEP_GOALS_DEFAULT;
                    leafs.add(new GoalsSetting(this, 4, (byte) 0x00, PublicData.STEP_GOALS_DEFAULT, (byte) 0x01));
                } else {
                    setSharePref(PublicData.TOTAL_TARGET_STEPS_KEY, GlobalVar.stepGoalsValue);
                    tv_goal_bar_step_val.setText(GlobalVar.stepGoalsValue + "");
                }

                if (GlobalVar.calorieGoalsValue == 0) {
                    Logger.i("BluetoothUtil", "手环的卡路里目标值为0，设置默认值!!!");
                    GlobalVar.calorieGoalsValue = PublicData.CALORIE_GOALS_DEFAULT;
                    leafs.add(new GoalsSetting(this, 4, (byte) 0x01, PublicData.CALORIE_GOALS_DEFAULT, (byte) 0x01));
                } else {
                    setSharePref(PublicData.TOTAL_TARGET_CALORIES_KEY, GlobalVar.calorieGoalsValue);
                    tv_goal_bar_calories_val.setText(GlobalVar.calorieGoalsValue + "");
                }

                if (GlobalVar.distanceGoalsValue == 0) {
                    Logger.i("BluetoothUtil", "手环的距离目标值为0，设置默认值!!!");
                    GlobalVar.distanceGoalsValue = PublicData.DISTANCE_GOALS_DEFAULT;
                    leafs.add(new GoalsSetting(this, 4, (byte) 0x02, PublicData.DISTANCE_GOALS_DEFAULT, (byte) 0x01));
                } else {
                    setSharePref(PublicData.TOTAL_TARGET_DISTANCE_KEY, GlobalVar.distanceGoalsValue);
                    tv_goal_bar_distance_val.setText(GlobalVar.distanceGoalsValue + "");
                }

                if (GlobalVar.sleepGoalsValue == 0) {
                    Logger.i("BluetoothUtil", "手环的睡眠目标值为0，设置默认值!!!");
                    GlobalVar.sleepGoalsValue = PublicData.SLEEP_GOALS_DEFAULT;
                    leafs.add(new GoalsSetting(this, 4, (byte) 0x03, PublicData.SLEEP_GOALS_DEFAULT, (byte) 0x01));
                } else {
                    setSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, GlobalVar.sleepGoalsValue);
                    tv_goal_bar_sleep_val.setText(GlobalVar.sleepGoalsValue + "");
                }

//                if (leafs.size() > 0) {
//                    BluetoothUtil.getInstance().send(leafs);
//                }
            }
            // 设置各目标值
            else {
//                BluetoothUtil.getInstance().continueSend();
                if (GlobalVar.stepGoalsValue > 0 && synGoalMode == 1) {
                    Logger.i(TAG, "步数目标设置完成!!!");
                    setSharePref(PublicData.TOTAL_TARGET_STEPS_KEY, GlobalVar.stepGoalsValue);
                    tv_goal_bar_step_val.setText(GlobalVar.stepGoalsValue + "");
                    GlobalVar.stepGoalsValue = 0;
                }
                if (GlobalVar.calorieGoalsValue > 0 && synGoalMode == 2) {
                    Logger.i(TAG, "卡路里目标设置完成!!!");
                    setSharePref(PublicData.TOTAL_TARGET_CALORIES_KEY, GlobalVar.calorieGoalsValue);
                    tv_goal_bar_calories_val.setText(GlobalVar.calorieGoalsValue + "");
                    GlobalVar.calorieGoalsValue = 0;
                }
                if (GlobalVar.distanceGoalsValue > 0 && synGoalMode == 3) {
                    Logger.i(TAG, "距离目标设置完成!!!");
                    setSharePref(PublicData.TOTAL_TARGET_DISTANCE_KEY, GlobalVar.distanceGoalsValue);
                    tv_goal_bar_distance_val.setText(GlobalVar.distanceGoalsValue + "");
                    GlobalVar.distanceGoalsValue = 0;
                }
                if (GlobalVar.sleepGoalsValue > 0 && synGoalMode == 5) {
                    Logger.i(TAG, "睡眠目标设置完成!!!");
                    setSharePref(PublicData.TOTAL_TARGET_SLEEP_KEY, GlobalVar.sleepGoalsValue);
                    tv_goal_bar_sleep_val.setText(GlobalVar.sleepGoalsValue + "");
                    GlobalVar.sleepGoalsValue = 0;
                }
                if (PublicData.isNeedNetwork) {
                    Logger.i(TAG, "同步到服务器...");
                    new Thread(SetGoalRunnable).start();
                } else {
//                    Toast.makeText(this, this.getString(R.string.success), Toast.LENGTH_SHORT).show();
                    if (builder == null) {
                        builder = new AlertDialog.Builder(GoalsL38iActivity.this);
                    }
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(R.string.success);
                    builder.setPositiveButton(getString(android.R.string.ok), null);
                    builder.show();
                }
            }
        }
    }

    @Override
    public void onFaild(Leaf leaf) {
        mHandler.obtainMessage(SYNGOAL_FAIL, "BingNO!").sendToTarget();
    }
}
