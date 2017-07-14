package cn.appscomm.pedometer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bugtags.library.Bugtags;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import apps.utils.ConfigHelper;
import apps.utils.DialogUtil;
import apps.utils.HttpUtil;
import apps.utils.Logger;
import apps.utils.NumberUtils;
import apps.utils.PublicData;
import apps.utils.TimesrUtils;
import cn.appscomm.pedometer.model.SleepData;
import cn.appscomm.pedometer.model.SportDataCache;
import cn.appscomm.pedometer.model.SportsData;
import cn.appscomm.pedometer.model.SportsEveryDate;
import cn.appscomm.pedometer.service.BluetoothLeService;
import cn.appscomm.pedometer.service.DBService;
import cn.appscomm.pedometer.service.DBService2;
import cn.l11.appscomm.pedometer.activity.R;

/**
 * Created with Eclipse. Author: Tim Liu email:9925124@qq.com Date: 14-4-10
 * Time: 13:52
 */
public class SynDataActivity extends Activity {
    private final static String TAG = "SynDataActivity";

    private TextView textview_battery;
    private TextView textview_today_steps;
    private TextView tv_syndata;
    private ProgressBar progress_horizontal;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mServiceDiscovered = false;
    private boolean isReaded = false;
    private boolean isSynSportDataFinish = false; // 运动数据是否全部传完。
    private int sportdata_count = 0, sleepdata_count = 0; //运动条数汇总
    private int cursport_count = 0, cursleep_count = 0;
    private int count, otherCount;
    private int total = 0;

    private int currentSteps; // 当天运动总步数
    private int currentSleeps; // 当天睡眠总条数
    private String lastDate, lastTime;
    int battery = 0;

    // private BluetoothGattCharacteristic mNotifyCharacteristic;
    // private BluetoothGattCharacteristic mGattWriteCharacteristic;

    private BluetoothGattService mGattPedometerService;
    private BluetoothGattCharacteristic mGattPedometerCharacteristic_1;
    private BluetoothGattCharacteristic mGattPedometerCharacteristic_2;

    private int orderType = 0; // 1:获取电池电量；2：获取最新的（当天汇总）运动数据；
    // 3.请求睡眠数据0x02格式总数；4：请求睡眠详细数据； 5.获取运动详细数据
    private int retValue = 0;
    private int finishValue = -1;// -1默认到时自动关闭/接收成功后关闭/未收到时半闭 ；0不关闭
    private int isfinishTotal = -1; // 是否同步了汇总数据

    private boolean mIsBind = false; // service与广播是否已经绑定

    private static final int RESULT_DATA_LOADED = 333; // 计步器的数据是否上传，结果码：成功
    private boolean isSaveData = false; // 获取睡眠数据是否已经完成
    private boolean isBack = false;

    private TextView tv_stepTotal, tv_stepCount;

    private List<SportsData> mGetPedometerSportsDataList; // 从手环上传过来的运动数据

    private List<SportsData> mSportsDataListFromDb; //
    private boolean isGetSportsDataFinished = true; // 获取运动数据是否已经完成

    private List<SportDataCache> mSportDataCacheList;// 缓存列表

    private List<SleepData> mGetPedometerSleepDataList; // 从手环上传过来的睡眠数据
    private boolean isGetSleepDataFinished = true; // 获取睡眠数据是否已经完成

    private List<SleepData> mLocalSleepDataList; // 本地保存的睡眠列表

    private DBService dbService;
    private DBService2 dbService2;

    private static final long TIME_PERIOD = 25000;
    private static final int UNFINISH_CLOSE = 1111;
    private static final int FINISH_CLOSE = 1112;
    private static final int SLEEP_DATA_LOADED = 11222;
    private static final int SPORTS_DATA_LOADED = 11333;

    // add 2014-05-23
    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 3000;
    private static final int CANCEL = 9999;

    private int TotalStepCount = 0; // 累计目标值
    private int totalStep = 0; // 汇总步数总数
    private double totaldis = 0.0; // 汇总距离
    private int totalCal = 0; // 汇总卡路里值
    private float stepLength = 0; // 步长
    private long lastTimeStamp = 0; // 最后一次同步的时间
    private SleepData lastRecvSleepData;
    private HttpUtil httpUtil = new HttpUtil();

    private ImageView iv_left_battery01, iv_left_battery02, iv_left_battery03,
            iv_left_battery04;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case CANCEL:
                    Logger.d(TAG, "<<===handleMessage-->CANCEL");

                    Intent intent0 = new Intent();
                    if (isSynSportDataFinish) {
                        intent0.putExtra("IS_FINISH_DATA", 1);

                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TEMP_DAYSTEP, totalStep);

                        Logger.d(TAG, "<<===handleMessage-->CANCEL="+ (float) totaldis);

                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TEMP_DAYDIS, (float) totaldis);

                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TEMP_DAYCAL, totalCal);

                        String lastDate = new SimpleDateFormat("yyyy-MM-dd")
                                .format(new Date());

                        // 储存最后一次同步的汇总的时间
                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.LASTSYNCED_TOTALDATA_DATE_KEY, lastDate);

                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TOP_HINT_LASTSYNCED_DATE_KEY, lastDate);
                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TOP_HINT_LASTSYNCED_TIME_KEY, lastTime);
                        ConfigHelper.setSharePref(SynDataActivity.this,
                                PublicData.SHARED_PRE_SAVE_FILE_NAME,
                                PublicData.TOP_HINT_BATTERY_KEY, battery);

                    } else {
                        // 返回时是否保存汇总数据
                        if (isfinishTotal > 0) {
                            intent0.putExtra("IS_FINISH_DATA", 0);
                        /*
						 * ConfigHelper.setSharePref(SynDataActivity.this,
						 * PublicData.SHARED_PRE_SAVE_FILE_NAME,
						 * PublicData.TEMP_DAYSTEP,totalStep );
						 * 
						 * ConfigHelper.setSharePref(SynDataActivity.this,
						 * PublicData.SHARED_PRE_SAVE_FILE_NAME,
						 * PublicData.TEMP_DAYDIS, (float)totaldis);
						 * 
						 * ConfigHelper.setSharePref(SynDataActivity.this,
						 * PublicData.SHARED_PRE_SAVE_FILE_NAME,
						 * PublicData.TEMP_DAYCAL,totalCal );
						 */

                        } else
                            intent0.putExtra("IS_FINISH_DATA", -1);
                    }

                    setResult(RESULT_DATA_LOADED, intent0);
                    finish();

                    break;

                case STOPSPLASH:
                    Logger.d(TAG, "<<===>>STOPSPLASH");
                    // 如果已经保存过数据到数据库：
                    // if(isSaveData){
                    Logger.i(TAG, "isSaveData=" + isSaveData);
                    Intent intent1 = new Intent();
                    setResult(RESULT_DATA_LOADED, intent1);
                    // }

                    finish();

                    // sendEmptyMessageDelayed(STOPSPLASH, SPLASHTIME);

                    break;

                case UNFINISH_CLOSE:
                    Logger.d(TAG, "<<==UNFINISH_CLOSE-- finishValue:" + finishValue);
                    Logger.d(TAG,
                            ">>>>end:"
                                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new Date())
                                    + "||---->orderType:" + orderType);

                    if (orderType == 1) {
                        Toast.makeText(SynDataActivity.this,
                                R.string.make_sure_zefit, Toast.LENGTH_SHORT)
                                .show();
                    }
                    if (orderType < 2 || finishValue == -1) // 说明没有获取数据,一般是没有连接上蓝牙设备
                    {
                        Logger.d(TAG, ">>>>>>>>>>>>>>>2");
                        // Toast.makeText(SynDataActivity.this,
                        // R.string.make_sure_zefit, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();

                        if (isSynSportDataFinish)
                            intent.putExtra("IS_FINISH_DATA", 1);
                        else
                            intent.putExtra("IS_FINISH_DATA", 0);
                        setResult(RESULT_DATA_LOADED, intent);
                        finish();
                    }

                    break;

                case FINISH_CLOSE:
                    Logger.d(TAG, "<<===>>>>>>>FINISH_CLOSE");
                    // 如果已经保存过数据到数据库：
                    Logger.i(TAG, "<<===>>isSaveData=" + isSaveData);
                    Logger.d(TAG, "<<===>>finish接收到的运动条数："
                            + mGetPedometerSportsDataList.size());
                    /**
                     * 20140726 if(isSaveData){ Intent intent = new Intent();
                     * setResult(RESULT_DATA_LOADED, intent); } Logger.i(TAG,
                     * "<<===>>isReaded=" + isReaded); if (!isReaded) { new
                     * DBSaveSportsDataTask().execute(mGetPedometerSportsDataList);
                     * } else { finish(); }
                     */
                    Intent intent = new Intent();

                    if (isSynSportDataFinish)
                        intent.putExtra("IS_FINISH_DATA", 1);
                    else
                        intent.putExtra("IS_FINISH_DATA", 0);
                    setResult(RESULT_DATA_LOADED, intent);
                    finish();

                    break;

                case SLEEP_DATA_LOADED:
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            SynDataActivity.this);
                    builder.setTitle("");
                    // builder.setMessage("获取睡眠详细数据意外中断，要继续获取吗？");
                    builder.setMessage(getString(R.string.get_sports_data_break_off));
                    builder.setPositiveButton(getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    // mBluetoothLeService.connect(mDeviceAddress);
                                    Logger.d(TAG, "<<===>>睡眠数据中断");
                                    Intent intent = new Intent();
                                    setResult(RESULT_DATA_LOADED, intent);
                                    finish();
                                }
                            });
                    /**
                     * builder.setNegativeButton("NO", new
                     * DialogInterface.OnClickListener() {
                     *
                     * @Override public void onClick(DialogInterface dialog, int
                     *           which) { finish(); // context. } });
                     */
                    builder.show();
                    break;

                case SPORTS_DATA_LOADED:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(
                            SynDataActivity.this);
                    builder2.setTitle("");
                    // builder2.setMessage("获取运动详细数据意外中断，要继续获取吗？");
                    builder2.setMessage(getString(R.string.get_sports_data_break_off));
                    builder2.setPositiveButton(getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // mBluetoothLeService.connect(mDeviceAddress);
                                    Logger.d(TAG, "<<===>>运动数据中断");
                                    Intent intent = new Intent();
                                    setResult(RESULT_DATA_LOADED, intent);
                                    finish();
                                }
                            });
                    /**
                     * builder2.setNegativeButton("NO", new
                     * DialogInterface.OnClickListener() {
                     *
                     * @Override public void onClick(DialogInterface dialog, int
                     *           which) { finish(); // context. } });
                     */
                    builder2.show();
                    break;

            }
        }
    };

    /**
     * 发送延时信息
     */
    private void sendDelayedMsg() {
        Message msg = new Message();
        msg.what = STOPSPLASH;
        mHandler.sendMessageDelayed(msg, SPLASHTIME);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1、设置全屏可以使用如下代码：
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 2、 设置无title bar可以使用如下代码：

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.syn_data_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep
        // Screen
        mServiceDiscovered = false;                                                                    // ON
        httpUtil = new HttpUtil(this);
        this.setFinishOnTouchOutside(false);
        sportdata_count = 0;
        sleepdata_count = 0; //运动条数汇总
        cursport_count = 0;
        cursleep_count = 0;

        initView();

    }

    // 初始化页面
    private void initView() {
        textview_battery = (TextView) findViewById(R.id.textview_battery);
        textview_today_steps = (TextView) findViewById(R.id.textview_today_steps);
        tv_syndata = (TextView) findViewById(R.id.tv_syndata);
        tv_stepTotal = (TextView) findViewById(R.id.tvToStep);
        tv_stepCount = (TextView) findViewById(R.id.tvStepIndex);
        progress_horizontal = (ProgressBar) findViewById(R.id.progress_horizontal);
        //	progress_horizontal.setProgressDrawable(R.drawable.color.progress_color);
        //android:indeterminateDrawable="@color/progress_color"
        progress_horizontal.setIndeterminateDrawable(getResources().getDrawable(R.drawable.probar_color));
        progress_horizontal.setIndeterminate(true);


        //	progress_horizontal.seti
        //	progress_horizontal.setMax(4);
        // progress_horizontal.

        // mDeviceAddress = getIntent().getStringExtra("mac");
        mDeviceAddress = (String) ConfigHelper.getSharePref(this,
                PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY,
                ConfigHelper.DATA_STRING);
        Logger.i(TAG, "mac=" + mDeviceAddress);

        iv_left_battery01 = (ImageView) findViewById(R.id.iv_left_battery01);// 显示电量图标
        iv_left_battery02 = (ImageView) findViewById(R.id.iv_left_battery02);
        iv_left_battery03 = (ImageView) findViewById(R.id.iv_left_battery03);
        iv_left_battery04 = (ImageView) findViewById(R.id.iv_left_battery04);

        dbService = new DBService(this);
        dbService2 = new DBService2(this);


        if (PublicData.isSynningSportData == false) {
            bindLeService();
//           PublicData.isSynningSportData = true;
        }
        orderType = 8;    // 先设置手动删除数据
        mGetPedometerSleepDataList = new ArrayList<SleepData>();
        mGetPedometerSportsDataList = new ArrayList<SportsData>();
        mSportsDataListFromDb = new ArrayList<SportsData>();
        mSportDataCacheList = new ArrayList<SportDataCache>();
        mSportDataCacheList.clear();

        mLocalSleepDataList = new ArrayList<SleepData>();

        lastRecvSleepData = new SleepData(0, 0);

        mSportsDataListFromDb = dbService.getAllSportsDataList();

        // 如果过了x0秒，还没有获取到数据，说明可能蓝牙连接有问题，就关闭此Activity
        Logger.d(TAG,
                ">>>>start:"
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date()));
        mHandler.sendEmptyMessageDelayed(UNFINISH_CLOSE, TIME_PERIOD);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    public void onDestroy() {

        PublicData.isSynningSportData = false;
        Logger.w(TAG, "onDestroy()-->");
        if (mIsBind == true) {
            unbindService(mServiceConnection);
            unregisterReceiver(mGattUpdateReceiver);
            if (mBluetoothLeService != null)
                mBluetoothLeService.close();
            mBluetoothLeService = null;
            mIsBind = false;
        }
        super.onDestroy();
    }

    // Tim 绑定service
    public void bindLeService() {
        mIsBind = true;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver,
                BluetoothLeService.makeGattUpdateIntentFilter());

    }

    // Tim 服务的绑定
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();

            mBluetoothLeService.NeedSynTime = true;
            mBluetoothLeService.connect(mDeviceAddress, SynDataActivity.this, true);
            Logger.i(TAG, "onServiceConnected()-->mBluetoothLeService="
                    + mBluetoothLeService);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.d(TAG, "<<===>>onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    // Tim 广播,接收service发过来的广播
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Logger.i(TAG, "BroadcastReceiver.action=" + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_TIMEOUT
                    .equals(action)) {
                total++;
                Logger.d(TAG, "<<====蓝牙超时次数total:" + total);

                if (total > 20) {
                    mHandler.sendEmptyMessage(CANCEL);
                }

                sendOrderToDevice(orderType);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                Logger.d(TAG, "<<===>>222222222222222222222222");
                Logger.d(TAG, "<<===>>蓝牙断开");
                mConnected = false;
                mServiceDiscovered = false;
                total++;
                Logger.d(TAG, "<<====蓝牙断开次数total:" + total);

                Logger.w(TAG, "<<===>>isGetSportsDataFinished="
                        + isGetSportsDataFinished + " | SportsDataList="
                        + mGetPedometerSportsDataList);
                Logger.w(TAG, "<<===>>isGetSleepDataFinished="
                        + isGetSleepDataFinished + " | SleepDataList="
                        + mGetPedometerSleepDataList);
				
				
			/*	if (isGetSleepDataFinished == false
						&& mGetPedometerSleepDataList != null
						&& mGetPedometerSleepDataList.size() > 0) {
					// 获取睡眠数据中间断开，将进行保存数据到sqlite的动作，然后再去获取一次
					Logger.w(TAG, "<<===>>先保存数据，获取剩余的睡眠详细数据");
					// Toast.makeText(SynDataActivity.this,
					// R.string.get_sports_data_break_off,
					// Toast.LENGTH_SHORT).show();
					new DBSaveSleepDataTask()
							.execute(mGetPedometerSleepDataList);

					// mHandler.sendEmptyMessage(SLEEP_DATA_LOADED);
				} else if (isGetSportsDataFinished == false
						&& mGetPedometerSportsDataList != null
						&& mGetPedometerSportsDataList.size() > 0) {
					// 获取运动数据中间断开，将进行保存数据到sqlite的动作，然后再去获取一次
					Logger.w(TAG, "<<===>>先保存数据，获取剩余的运动详细数据");
					// Toast.makeText(SynDataActivity.this,
					// R.string.get_sports_data_break_off,
					// Toast.LENGTH_SHORT).show();
					dbService.saveSportsCacheData(mSportDataCacheList);
					mSportDataCacheList.clear();
					new DBSaveSportsDataTask()
							.execute(mGetPedometerSportsDataList);

					// mHandler.sendEmptyMessage(SPORTS_DATA_LOADED);
				} */


                //else

                {
                    Logger.d(TAG, "<<===>>没有待保存的运动和睡眠数据，进入重连！");
                    Logger.d(TAG, "<<====total22:" + total);

                    if (total > 20) {
                        mHandler.sendEmptyMessage(CANCEL);
                    }

                    if (mBluetoothLeService != null) {
                        Logger.d(TAG, "<<===>>mDeviceAddress:" + mDeviceAddress);
                        mBluetoothLeService.NeedSynTime = false;
                        final boolean result1 = mBluetoothLeService
                                .connect(mDeviceAddress, SynDataActivity.this, true);
                        Logger.d(TAG, "<<===>>result1:" + result1 + " orderType="
                                + orderType);
                    }

                }

                // mHandler.sendEmptyMessage(UNFINISH_CLOSE);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {

                BluetoothLeService.SendTimeOut = true; // 设置传输同步数据时，发送超时消息代替断开重连
                mConnected = true;
                mServiceDiscovered = true;
                // if (当前orderType =
                Logger.d(TAG, "<<===>>333");
                Logger.d(TAG,
                        "====>>BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED");

                getGattServiceAndSendData();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mConnected = true;

                finishValue = -1;
                byte[] bytes = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Logger.e(TAG, "获取到的数据：" + NumberUtils.bytes2HexString(bytes));
                // DialogUtil.commonDialog(SynDataActivity.this, "", "获取到的数据：" +
                // NumberUtils.bytes2HexString(bytes));
                Logger.d(TAG, "<<==data length is " + bytes.length);
                parseBytesArray(bytes);

            } else {
                finishValue = -1;
                byte[] bytes = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Logger.e(TAG, "获取到的数据111：" + NumberUtils.bytes2HexString(bytes));
                parseBytesArray(bytes);
            }

        }

    };


    /**
     * 是否有效的唯一数据
     *
     * @param newData
     * @return
     */
    private boolean isUniqueSportData(SportsData newData) {
        if ((mSportsDataListFromDb == null) || (mSportsDataListFromDb.size() == 0)) {
            return true;
        }

        for (SportsData mData : mSportsDataListFromDb) {

            if (newData.sport_time_stamp == mData.sport_time_stamp) {
                return false;

            }


        }

        return true;

    }

    // Tim 解析获取到的byte[]
    private void parseBytesArray(byte[] bytes) {
        // DialogUtil.commonDialog(SynDataActivity.this, "", "获取到的数据22：" +
        // NumberUtils.bytes2HexString(bytes)
        // + "|length:" + bytes.length);
        // 获取手环电池电量的数据解析：对应的命令是：bytes = new byte[]{0x6E, 0x01, 0x0F, 0x01,
        // (byte) 0x8F};
        // 返回的数据格式是:6E-01-00-14-8F

        if (bytes.length == 5 && bytes[0] == 0x6e && bytes[1] == 0x01 && bytes[2] == 0x00
                && bytes[4] == (byte) 0x8f) {
            finishValue = 0;
            battery = bytes[3] * 5;
            if (battery > 100)
                battery = 100;
            //	textview_battery.setText(battery + "%");

            SetProgressPos(1);
            lastDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            lastTime = new SimpleDateFormat("HH:mm").format(new Date());
            // 储存同步时间及电池电量

            for (int i = 0; i < bytes.length; i++) {
                Logger.i(TAG, ">>>RS-电量bytes[" + i + "]:" + bytes[i]);
            }

		/*	if (battery <= 20) {
				iv_left_battery01.setVisibility(View.VISIBLE);
				iv_left_battery02.setVisibility(View.GONE);
				iv_left_battery03.setVisibility(View.GONE);
				iv_left_battery04.setVisibility(View.GONE);
			} else if (battery > 20 && battery <= 50) {
				iv_left_battery01.setVisibility(View.GONE);
				iv_left_battery02.setVisibility(View.VISIBLE);
				iv_left_battery03.setVisibility(View.GONE);
				iv_left_battery04.setVisibility(View.GONE);
			} else if (battery > 50 && battery <= 75) {
				iv_left_battery01.setVisibility(View.GONE);
				iv_left_battery02.setVisibility(View.GONE);
				iv_left_battery03.setVisibility(View.VISIBLE);
				iv_left_battery04.setVisibility(View.GONE);
			} else if (battery > 75) {
				iv_left_battery01.setVisibility(View.GONE);
				iv_left_battery02.setVisibility(View.GONE);
				iv_left_battery03.setVisibility(View.GONE);
				iv_left_battery04.setVisibility(View.VISIBLE);
			}
*/
            retValue = 1;
            orderType = 7; // 1:获取电池电量，2：获取最新的（当天汇总）运动数据 3.请求睡眠数据0x02格式总数 5，运动详细
            // orderType = 5;
            sendOrderToDevice(orderType);

        }
        //
        if (bytes.length == 6 && bytes[0] == 0x6e && bytes[2] == 0x01) // 返回响应消息
        {
            finishValue = 0;
            if (retValue == 0) {

                //	orderType = 1; // 1:获取电池电量，2：获取最新的（当天汇总）运动数据 3.请求睡眠数据0x02格式总数
                if (mServiceDiscovered) sendOrderToDevice(orderType);
            }

            retValue = 1;

        }


        // 获取个人信息 6E010B00C60707168288138F
        if (bytes.length == 12 && bytes[0] == 0x6e && bytes[1] == 0x01
                && bytes[2] == 0x0B && bytes[11] == (byte) 0x8f) {
            SetProgressPos(2);
            int a = NumberUtils.byteToInt(new byte[]{bytes[3]});
            boolean issex = false;
            if (a == 0) {
                issex = true;
            }
            int height = NumberUtils.byteToInt(new byte[]{bytes[8]});
            Logger.d(TAG, "<<issex:" + a + "|bytes[3]:" + bytes[3] + "|bytes[8]:"
                    + bytes[8] + "|height:" + height);

            // 保存性别
            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.SEX_OLD_ITEM_KEY, issex);
            // 保存身高
            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.HEIGHT_OLD_ITEM_KEY, height);

            orderType = 0x0b;  //  获取运动记录总数    5;  		// 1:获取电池电量，2：获取最新的（当天汇总）运动数据 3.请求睡眠数据0x02格式总数 5，运动详细
            sendOrderToDevice(orderType);
        } else if (bytes.length == 6 && bytes[0] == 0x6e && bytes[1] == 0x01 && bytes[2] == 0x01
                && bytes[3] == 0x32 && bytes[5] == (byte) 0x8f) {


            if (bytes[4] == 0) {


                switch (orderType) {
                    case 0x8:  //设置手动删除数据
                        Logger.d(TAG, "set manual delete data success!");
                        orderType = 1;  //获取电量
                        sendOrderToDevice(orderType);
                        break;

                    case 0x9:  //手动删除运动数据
                        Logger.d(TAG, "manual delete sportdata success!");
                        orderType = 2;  //获取汇总
                        sendOrderToDevice(orderType);
                        break;

                    case 0x0A:  //手动删除睡眠数据
                        Logger.d(TAG, "manual delete sleepdata success!");
                        mHandler.sendEmptyMessage(CANCEL);
                        orderType++;
                        break;


                    default:
                        orderType = 1;  //获取电量
                        sendOrderToDevice(orderType);
                        break;


                }


            } else {

                Logger.d(TAG, "set manual delete data Fail, cursend Order :" + orderType);
                mHandler.sendEmptyMessage(CANCEL);

            }


        } else if (bytes.length == 8 && bytes[0] == 0x6e && bytes[1] == 0x01 && bytes[2] == 0x12
                && bytes[7] == (byte) 0x8f) {

            sportdata_count = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[3], bytes[4], bytes[5], bytes[6]});

            Logger.d(TAG, "sport data count:" + sportdata_count);

            orderType = 5;   //读明细数据

            sendOrderToDevice(orderType);

        } else if (bytes.length == 8 && bytes[0] == 0x6e && bytes[1] == 0x01 && bytes[2] == 0x15
                && bytes[7] == (byte) 0x8f) {

            sleepdata_count = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[3], bytes[4], bytes[5], bytes[6]});

            Logger.d(TAG, "sleep data count:" + sleepdata_count);

            orderType = 4;   //读明细数据睡眠

            sendOrderToDevice(orderType);

        }


        // 发送当天的汇总数据解析： 对应的命令是： byte[] bytes = new byte[]{0x6E, 0x01, 0x1b,
        // 0x01, (byte) 0x8F}; // 获取最新当天汇总的运动数据
        // 返回的数据格式是：6E-01-0F-00000000-00000000-1B000000-00000000-8F（能力环值-卡路里-步数-当天的目标能量值）
        if (bytes.length == 20 && bytes[0] == 0x6e && bytes[2] == 0x0F
                && bytes[19] == (byte) 0x8f) {
            finishValue = 0;

			/*
			 * for (int i = 0; i<bytes.length;i++) { Logger.i(TAG,
			 * ">>>RS-汇总数量bytes[" + i + "]:" + bytes[i]); }
			 */
            // int i1 = bytes[3] * bytes[4]* bytes[5]* bytes[6];

            boolean sex = (Boolean) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.SEX_OLD_ITEM_KEY, ConfigHelper.DATA_BOOLEAN);
            int height = (Integer) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.HEIGHT_ITEM_KEY, ConfigHelper.DATA_INT);

            String sheight = (String) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.INFO_HEIGHT,
                    ConfigHelper.DATA_STRING);
            String hUnit = (String) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME, "heightunit",
                    ConfigHelper.DATA_STRING);

            if ("0".equals(hUnit)) { // ft in
                // height =
                height = (int) (Float.valueOf(sheight) * 2.54); // ft in 转 cm

            } else { // cm
                try {
                    height = (int) (float) (Float.valueOf(sheight));
                } catch (Exception e) {
                    height = 170;
                }
            }

            // -直接取绑定时的身高
            int height_old = (Integer) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.HEIGHT_OLD_ITEM_KEY, ConfigHelper.DATA_INT);
            Logger.d(TAG, "<<===height-old:" + height_old);
            height = height_old;
            // -

            /**
             * 距离是根据系数算出来的，举例：男人系数：0.415，女人系数：0.413 步长=身高*系数 距离=步长*步数
             */

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

            currentSteps = NumberUtils.byteReverseToInt(new byte[]{bytes[11],
                    bytes[12], bytes[13], bytes[14]});

            Logger.d(TAG, "汇总数据 steps :" + currentSteps);
            Logger.d("test-data", "汇总数据 steps :" + currentSteps);
            // add 2014-05-23 begin
            int targetSteps = 7000; // 默认是7000步
            int shpStep = (Integer) ConfigHelper.getSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.TOTAL_TARGET_STEPS_KEY, ConfigHelper.DATA_INT);
            if (shpStep > 0) {
                targetSteps = shpStep;
            }
            // add 2014-05-23 end
            textview_today_steps.setText(getString(R.string.today_steps1) + " "
                    + (currentSteps * 100) / targetSteps + "% "
                    + getString(R.string.today_steps2));
            SportsEveryDate mData = new SportsEveryDate();
            Calendar calendar = Calendar.getInstance();
            mData.date_time_stamp = TimesrUtils.getTimesMorning(calendar); // 保存的时间点是：当天00:00:00
            // 的时间
            mData.date_steps = currentSteps;
            mData.date_cal = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[7], bytes[8], bytes[9], bytes[10]});
            mData.date_energy = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[3], bytes[4], bytes[5], bytes[6]});
            mData.date_goal_energy = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[15], bytes[16], bytes[17], bytes[18]});
            Logger.i(TAG, "SportsEveryDate=" + mData);

            float dists2 = (currentSteps * stepLength / 100);

            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CUR_STEPS_TOTAL, currentSteps);
            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CUR_CALORIES_TOTAL, mData.date_cal);
            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CUR_DIS_TOTAL, dists2);
            ConfigHelper.setSharePref(this,
                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
                    PublicData.CUR_SPORTTIME_TOTAL, mData.date_energy);

            // 先保存汇总值变量
            totalStep = currentSteps;
            totaldis = dists2;
            totalCal = mData.date_cal;

            isfinishTotal = 1;

			/*
			 * ConfigHelper.setSharePref(this,
			 * PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CUR_STEPS_TOTAL,
			 * currentSteps); ConfigHelper.setSharePref(this,
			 * PublicData.SHARED_PRE_SAVE_FILE_NAME,
			 * PublicData.CUR_CALORIES_TOTAL, mData.date_cal);
			 */
            /**
             * 20140710 //更新保存本地汇总数据 dbService2.saveSportsEveryDate(mData);
             * isSaveData = true;
             */

            // modify begin 2014-05-23
            // mHandler.sendEmptyMessage(FINISH_CLOSE);
            textview_today_steps.setVisibility(View.GONE);


            //	orderType =   0x0c ; //获取睡眠个数   //4;


            if (DialogUtil.checkFw204(this, false, false)) {
                Logger.d(TAG, "firmware < 2.04 ,Skip send get Sleep status");
                orderType = 0x0C;                 //固件少于2.04 不发送睡眠状态命令
                sendOrderToDevice(orderType);
            } else {
                Logger.d(TAG, "firmware >= 2.04 , send get Sleep status");
                orderType = 0x0d; //先发送判断睡眠状态的命令
                SetProgressPos(3);

                sendOrderToDevice(orderType);
            }
        }
		/*
		 * try { Thread.sleep(500); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		/*
		 * // 睡眠记录总个数解析： 对应的命令是： byte[] bytes = new byte[]{0x6E, 0x01, 0x30,
		 * 0x02, (byte) 0x8F}; // 请求运动记录0x01、睡眠数据0x02格式总数 //
		 * 返回的数据格式是：6E-01-15-00-00-00-00-8F if (bytes.length == 8 && bytes[0] ==
		 * 0x6e && bytes[2] == 0x15 && bytes[7] == (byte)0x8f ) { // int i1 =
		 * bytes[3] * bytes[4]* bytes[5]* bytes[6]; currentSleeps =
		 * NumberUtils.byteReverseToInt(new byte[]{bytes[3], bytes[4],
		 * bytes[5],bytes[6]});
		 * 
		 * textview_today_steps.setText("今天总运动条数：" + currentSteps + " 今天总睡眠数：" +
		 * currentSleeps); orderType = 4; // 4：请求睡眠详细数据； 5.获取运动详细数据
		 * sendOrderToDevice(orderType);
		 * 
		 * }
		 */

        // ===============================获取睡眠详细数据返回的数据======================================================

        // 获取收到睡眠详细数据 失败6E-01-01-31-03-8F；命令响应成功：6E-01-01-31-00-8F
        if (bytes.length == 6 && bytes[0] == 0x6e && bytes[2] == 0x01
                && bytes[3] == 0x31 && bytes[5] == (byte) 0x8f) {
            finishValue = 0;
            for (int i = 0; i < bytes.length; i++) {
                Logger.i(TAG, "<<===>>|>>>RS-猎取睡眠详细数据bytes[" + i + "]:" + bytes[i]);
            }
            isGetSleepDataFinished = true; // 获取运动数据完成，将进行保存数据到sqlite的动作
            if (bytes[4] == 0x00) {
                // 命令响应成功
                Logger.d(TAG, "<<===>>后台线程启动-->要保存的睡眠数据条数="
                        + mGetPedometerSleepDataList.size() + "<--");

                if (cursleep_count == sleepdata_count) {
                    Logger.d(TAG, "cursleep_count:" + cursleep_count + "  sleepcount:" + sleepdata_count + "  is equal");
                    dbService.saveSleepCacheDataList(mLocalSleepDataList);
                    mLocalSleepDataList.clear();
                    new DBSaveSleepDataTask().execute(mGetPedometerSleepDataList);

                } else {
                    Logger.d(TAG, "cursleep_count:" + cursleep_count + "  sleepcount:" + sleepdata_count + "  Not equal !!");

                    mHandler.sendEmptyMessage(CANCEL);
                }

            } else {
                // Toast.makeText(this, R.string.get_sports_data_finish,
                // Toast.LENGTH_SHORT).show();

                mHandler.sendEmptyMessage(CANCEL);

                /**
                 * 20140724 finishValue = -1; isSaveData = true; Logger.d(TAG,
                 * "<<===>>准备发送关闭窗口"); sendDelayedMsg();
                 */
            }

        }

        // 请求发送睡眠详细数据解析： 对应的命令是： byte[] bytes = new byte[]{0x6E, 0x01, 0x31,
        // 0x01, (byte) 0x8F};
        // 返回的数据格式是：6E-01-13-10-B0BC4C53-8F
        // (20140710>>>获取到的数据：6E011310332FBE538F)
        if (bytes.length == 9 && bytes[0] == 0x6e && bytes[2] == 0x13
                && bytes[8] == (byte) 0x8f) {
            finishValue = 0;
            Logger.i(TAG, ">>RS收到睡眠详细数据-->1");
            for (int i = 0; i < bytes.length; i++) {
                Logger.i(TAG, ">>>RS-收到睡眠详细数据bytes[" + i + "]:" + bytes[i]);
            }
            cursleep_count++;
            Logger.d(TAG, "cursleep count :" + cursleep_count);


            if (bytes[3] == 18) {

                Logger.d(TAG, "prep.. SleepTime Set");
                return;

            }
            isGetSleepDataFinished = false; // 获取运动数据没有完成

            SleepData mData = new SleepData();
            mData.sleep_type = NumberUtils.byteToInt(new byte[]{bytes[3]});

            long d = (long) NumberUtils.byteReverseToInt(new byte[]{bytes[4],
                    bytes[5], bytes[6], bytes[7]});
            long sleep_time_stamp = d + 8 * 3600;// 手表是东8区时间//ZeFit需要+8小时，L28S不用，中国时区
            // 20140724

            mData.sleep_time_stamp = sleep_time_stamp;

            if ((mData.sleep_type != lastRecvSleepData.sleep_type)
                    || (mData.sleep_time_stamp != lastRecvSleepData.sleep_time_stamp)) {
                mLocalSleepDataList.add(new SleepData(mData.sleep_type,
                        mData.sleep_time_stamp));
                lastRecvSleepData.sleep_type = mData.sleep_type;
                lastRecvSleepData.sleep_time_stamp = mData.sleep_time_stamp;

            }

            mGetPedometerSleepDataList.add(mData);

        }

        // ===============================获取运动详细数据返回的数据======================================================
        // 获取收到运动详细数据失败6E-01-01-06-04-8F；命令响应成功：6E-01-01-06-00-8F
        if (bytes.length == 6 && bytes[0] == 0x6e && bytes[2] == 0x01
                && bytes[3] == 0x06 && bytes[5] == (byte) 0x8f) {
            finishValue = 0;

            isSynSportDataFinish = true;

            Logger.d(TAG, "==>>运动详细数据情况：" + mGetPedometerSportsDataList.size());

            if (bytes[4] == 0x00) {

                dbService.saveSportsCacheData(mSportDataCacheList);
                mSportDataCacheList.clear();

                Logger.d(TAG, "后台线程启动-->要保存运动的数据条数="
                        + mGetPedometerSportsDataList.size() + "<--");
                if (!isGetSportsDataFinished) // 如果是true，说明在解析完运动数据的时候已经保存过一次了
                {


                    if (cursport_count == sportdata_count)

                    {

                        Logger.d(TAG, "cursport:" + cursport_count + "  sportdata_count：" + sportdata_count + "  equal.. ");
                        new DBSaveSportsDataTask()
                                .execute(mGetPedometerSportsDataList);


                        isGetSportsDataFinished = true;
                    } else {

                        Logger.d(TAG, "cursport:" + cursport_count + "  sportdata_count：" + sportdata_count + " not  equal !!! ");
                        mHandler.sendEmptyMessage(CANCEL);
                    }

                } else {
                    orderType = 2; // 获取完运动详细数据，再获取睡眠详细数据
                    sendOrderToDevice(orderType);
                    SetProgressPos(4);
					/*
					 * try { Thread.sleep(500); } catch (InterruptedException e)
					 * { // TODO Auto-generated catch block e.printStackTrace();
					 * }
					 */
                }

                // 命令响应成功，再次去读取一次数据
                // mBluetoothLeService.readCharacteristic(mGattPedometerCharacteristic_1);
            } else if (bytes[4] == 0x04) {
                // Toast.makeText(this, R.string.get_sports_data_finish,
                // Toast.LENGTH_SHORT).show();
                // mHandler.sendEmptyMessage(PROGRESSBAR_HIDE);
                Logger.d(TAG, "======<<没有运动详细数据，马上获取睡眠详细数据111>>========");
                orderType = 2; // 运动详细数据，马上获取睡眠详细数据
                SetProgressPos(4);
                sendOrderToDevice(orderType);
				/*
				 * try { Thread.sleep(500); } catch (InterruptedException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); }
				 */
            } else {
                // Toast.makeText(this, R.string.get_sports_data_false,
                // Toast.LENGTH_SHORT).show();
                // mHandler.sendEmptyMessage(PROGRESSBAR_HIDE);
                Logger.d(TAG, "======<<没有运动详细数据，马上获取睡眠详细数据222>>========");
                orderType = 2; // 运动详细数据，马上获取睡眠详细数据
                SetProgressPos(4);
                sendOrderToDevice(orderType);
				/*
				 * try { Thread.sleep(500); } catch (InterruptedException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); }
				 */
            }


        }
        // 收到运动详细数据6E-01-05-81-39683B53-1E000000-00000000-100A0000-8F
        // 6E-01-05-81-423B3E53-12000000-00000000-82020000-8F
        if (bytes.length == 19 && bytes[0] == 0x6e && bytes[2] == 0x05
                && bytes[18] == (byte) 0x8f) {
            finishValue = 0;
            Logger.i(TAG, ">>RS收到运动详细数据-->1");
            for (int i = 0; i < bytes.length; i++) {
                Logger.i(TAG, ">>>RS-收到运动详细数据bytes[" + i + "]:" + bytes[i]);
            }
            isGetSportsDataFinished = false; // 获取运动数据没有完成
            // 0x81说明后面还有数据,0x01是最后一条数据了
            // if(bytes[3] == (byte)0x80 || bytes[3] == (byte)0x81 || bytes[3]
            // == (byte)0x82 || bytes[3] == (byte)0x83 || bytes[3] ==
            // (byte)0x01)
            // if((bytes[3]&0x80)==(byte)0x80)
            // {
            Logger.d(TAG, "<<==>>当前读第" + (count + 1) + " 条运动数据");
            tv_stepCount.setText("StepDataCount:" + (count + 1));
            count++;
            SportsData mSportsData = new SportsData();
            mSportsData.sport_type = 1;
            mSportsData.sport_time_stamp = (long) NumberUtils
                    .byteReverseToInt(new byte[]{bytes[4], bytes[5],
                            bytes[6], bytes[7]});
            mSportsData.sport_steps = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[8], bytes[9], bytes[10], bytes[11]});
			/*mSportsData.sport_energy = NumberUtils.byteReverseToInt(new byte[] {
					bytes[12], bytes[13], bytes[14], bytes[15] });*/
            mSportsData.sport_cal = NumberUtils.byteReverseToInt(new byte[]{
                    bytes[12], bytes[13], bytes[14], bytes[15]});

            if ((Math.abs(mSportsData.sport_time_stamp - lastTimeStamp)) > 1 && mSportsData.sport_steps > 0) // 时间间隔大于1S为合法数据  且步数大于0的,过滤设备生成的导常负值数据

            {


                lastTimeStamp = mSportsData.sport_time_stamp;
                mGetPedometerSportsDataList.add(mSportsData);

                cursport_count++;
                Logger.d(TAG, "cursportData Count :" + cursport_count);

                if (isUniqueSportData(mSportsData)) {
                    saveSportDataToCache(mSportsData, stepLength / 100);
                }


                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Logger.i(TAG, ">>RS运动时间mSportsData.sport_time_stamp:"
                        + mSportsData.sport_time_stamp);

                long value;
                value = (long) ((bytes[4] & 0xFF) | ((bytes[5] & 0xFF) << 8)
                        | ((bytes[6] & 0xFF) << 16) | ((bytes[7] & 0xFF) << 24));
                Logger.d(TAG,
                        ">>RS运动时间value:" + value + " | format:"
                                + sdf.format(new Date(value * 1000)));
                Logger.i(
                        TAG,
                        ">>RS收到运动详细数据-->2 time="
                                + sdf.format(new Date(
                                mSportsData.sport_time_stamp * 1000))
                                + " steps=" + mSportsData.sport_steps
                                + " energy=" + mSportsData.sport_energy
                                + " cal=" + mSportsData.sport_cal);
                TotalStepCount = TotalStepCount + mSportsData.sport_steps;
                tv_stepTotal.setText("TotalStep:" + TotalStepCount);

            } else {
                Logger.d("new-test", "two record time less than 2s"
                        + lastTimeStamp + "," + mSportsData.sport_time_stamp);
                lastTimeStamp = mSportsData.sport_time_stamp;

            }

            if ((bytes[3] & 0x80) == (byte) 0x00) {
				/*
				 * isReaded = true; // Logger.d(TAG,
				 * "=====================<<接收完从手表发来的所有运动详细数据！>>====================="
				 * ); new
				 * DBSaveSportsDataTask().execute(mGetPedometerSportsDataList);
				 * Logger.d(TAG, "收到运动详细数据-->3完成，条数=" +
				 * mGetPedometerSportsDataList.size()); isGetSportsDataFinished
				 * = true;
				 */

                /**
                 * 20140724 orderType = 4; // 获取完运动详细数据，再获取睡眠详细数据
                 * sendOrderToDevice(orderType);
                 */
            }

            // } else {
            // otherCount ++;
            // Logger.d(TAG, "<<==other>>其它情况条数" + otherCount + " |获取到的数据" +
            // NumberUtils.bytes2HexString(bytes));
            // }

        }


        if (bytes.length == 8 && bytes[0] == 0x6e && bytes[1] == 0x01
                && bytes[2] == 0x1e && bytes[7] == (byte) 0x8f) {


            if (bytes[3] == 1) {
                Logger.d(TAG, "device in sleep Mode....");

                mHandler.sendEmptyMessage(CANCEL);
                orderType++;
            } else {

                Logger.d(TAG, "device Not in sleep Mode....");

                orderType = 0x0C;
                sendOrderToDevice(orderType);
            }


        }


        Logger.d(TAG, "<<==finishValue:" + finishValue);
    }

    private void saveSportDataToCache(SportsData mSportsData, float mStepLength) {

        long mDate = 0;
        int mHour = 0;
        boolean find = false;
        long utcTime = mSportsData.sport_time_stamp + 3600 * 8;
        mDate = TimesrUtils.getUnixDate(utcTime);
        mHour = TimesrUtils.getUnixHours(utcTime);

        Logger.d("data-cache", "timesstamp,date,hour :"
                + mSportsData.sport_time_stamp + "," + mDate + "," + mHour);

        for (SportDataCache mSportCache : mSportDataCacheList) {
            if ((mSportCache.sportDataDate == mDate)
                    && (mSportCache.sportDataHour == mHour)) {
                find = true;
                mSportCache.sportDataSteps = mSportCache.sportDataSteps
                        + mSportsData.sport_steps;
                mSportCache.sportDataDis = mSportCache.sportDataDis
                        + mSportsData.sport_steps * mStepLength;
                mSportCache.sportDataCal = mSportCache.sportDataCal
                        + mSportsData.sport_cal;

                Logger.d("data-cache", "update record");
                break;

            }

        }

        if (!find) {
            SportDataCache spCache = new SportDataCache(mDate, mHour,
                    mSportsData.sport_steps, mSportsData.sport_cal,
                    mSportsData.sport_steps * mStepLength);

            mSportDataCacheList.add(spCache);
            Logger.d("data-cache", "add a record");
        }

        // TODO Auto-generated method stub

    }

    private void SetProgressPos(final int i) {
        progress_horizontal.post(new Runnable() {

            @Override
            public void run() {
                //	progress_horizontal.setProgress(i);
                // TODO Auto-generated method stub

            }
        });// TODO Auto-generated method stub

    }

    // Tim 获取要通信的service UUID：6006，通信的特征1:8001，通信的特征2:8002
    // Tim 以及发送数据
    private void getGattServiceAndSendData() {
        mGattPedometerService = mBluetoothLeService.getPedometerGattService();
        /**
         * 2014-06-11 mGattPedometerCharacteristic_1 =
         * mBluetoothLeService.getPedometerGattCharacteristic1();
         * mGattPedometerCharacteristic_2 =
         * mBluetoothLeService.getPedometerGattCharacteristic2();
         */
        Logger.i(TAG, "mGattPedometerService=" + mGattPedometerService
                + " Ch_1=" + mGattPedometerCharacteristic_1 + " Ch_2="
                + mGattPedometerCharacteristic_2);
        // Tim 读取运动手环的运动数据：0x06
		/*
		 * if(mGattPedometerCharacteristic_1 != null &&
		 * mGattPedometerCharacteristic_2 != null) { byte[] bytes = new
		 * byte[]{0x6E, 0x01, 0x06, 0x01, (byte) 0x8F}; // 上传运动数据 // byte[]
		 * bytes = new byte[]{0x6E, 0x01, 0x1b, 0x01, (byte) 0x8F}; //
		 * 获取最新当天汇总的运动数据 // byte[] bytes = new byte[]{0x6E, 0x01, 0x04, 0x01,
		 * (byte) 0x8F}; // 获取watchID // byte[] bytes = new byte[]{0x6E, 0x01,
		 * 0x30, 0x02, (byte) 0x8F}; // 请求运动记录、睡眠数据格式总数
		 * 
		 * // byte[] bytes = new byte[]{0x6E, 0x01, (byte)0xA1, 0x01, (byte)
		 * 0x8F}; // 返回当前运动数据命令（0x17）
		 * mBluetoothLeService.sendDataToPedometer(mGattPedometerCharacteristic_1
		 * , mGattPedometerCharacteristic_2, bytes ); Logger.d(TAG, "要发送的命令是=" +
		 * NumberUtils.bytes2HexString(bytes) ); }
		 */
        sendOrderToDevice(orderType);
    }

    // 发送的命令类型选择
    private void sendOrderToDevice(int orderType) {
        finishValue = -1;// 先设置发送后未收到情况标志-1，收到后设成0
        // DialogUtil.commonDialog(SynDataActivity.this, "  ", "orderType1=" +
        // orderType);
        if (mBluetoothLeService != null)// && mGattPedometerCharacteristic_1 !=
        // null &&
        // mGattPedometerCharacteristic_2 !=
        // null)
        {
            byte[] bytes = null;
            switch (orderType) {

                case 0x01: // 获取手环的电量
                    bytes = new byte[]{0x6E, 0x01, 0x0F, 0x01, (byte) 0x8F};
                    break;

                case 0x02: // 获取最新当天汇总的运动数据
                    bytes = new byte[]{0x6E, 0x01, 0x1B, 0x01, (byte) 0x8F};
                    break;

                case 0x03: // 请求运动记录0x01、睡眠数据0x02格式总数
                    bytes = new byte[]{0x6E, 0x01, 0x30, 0x02, (byte) 0x8F};
                    break;

                case 0x04: // 请求睡眠详细数据
                    bytes = new byte[]{0x6E, 0x01, 0x31, 0x01, (byte) 0x8F};
                    break;

                case 0x05: // 获取运动详细数据
                    isSynSportDataFinish = false;
                    bytes = new byte[]{0x6E, 0x01, 0x06, 0x01, (byte) 0x8F};
                    break;

                case 0x06: // 获取watchID
                    bytes = new byte[]{0x6E, 0x01, 0x04, 0x01, (byte) 0x8F};
                    break;

                case 0x07:  // 获取个人信息
                    bytes = new byte[]{0x6E, 0x01, 0x14, 0x01, (byte) 0x8F};
                    break;


                case 0x08: //设置手动删除设置运动，睡眠数据模式

                    bytes = new byte[]{0x6E, 0x01, 0x32, 0x04, (byte) 0x8F};
                    break;

                case 0x09: //手动删除运动数据

                    bytes = new byte[]{0x6E, 0x01, 0x32, 0x01, (byte) 0x8F};
                    break;


                case 0x0A: //手动删除睡眠数据

                    bytes = new byte[]{0x6E, 0x01, 0x32, 0x02, (byte) 0x8F};
                    break;


                case 0x0B: //获取运动总记录数

                    bytes = new byte[]{0x6E, 0x01, 0x30, 0x01, (byte) 0x8F};
                    break;

                case 0x0C: //获取睡眠总记录数

                    bytes = new byte[]{0x6E, 0x01, 0x30, 0x02, (byte) 0x8F};
                    break;

                case 0x0D: // 获取睡眠状态
                    bytes = new byte[]{0x6E, 0x01, 0x36, (byte) 0x80, 0, 0, 0, (byte) 0x8F};

                    break;

                default:
                    break;
            }
            // byte[] bytes = new byte[]{0x6E, 0x01, 0x06, 0x01, (byte) 0x8F};
            // // 上传运动数据
            // byte[] bytes = new byte[]{0x6E, 0x01, 0x04, 0x01, (byte) 0x8F};
            // // 获取watchID
            // byte[] bytes = new byte[]{0x6E, 0x01, 0x30, 0x02, (byte) 0x8F};
            // // 请求运动记录、睡眠数据格式总数

            // byte[] bytes = new byte[]{0x6E, 0x01, (byte)0xA1, 0x01, (byte)
            // 0x8F}; // 返回当前运动数据命令（0x17）
            // 2014-06-11
            // mBluetoothLeService.sendDataToPedometer(mGattPedometerCharacteristic_1,
            // mGattPedometerCharacteristic_2, bytes );
            mBluetoothLeService.sendDataToPedometer(bytes);

            Logger.i(TAG, "要发送的命令是=" + NumberUtils.bytes2HexString(bytes));
            // DialogUtil.commonDialog(SynDataActivity.this, "  ", "要发送的命令是1=" +
            // NumberUtils.bytes2HexString(bytes));
        }
    }

    // 运动数据保存到数据库(第一个为doInBackground接受的参数，第二个为显示进度的参数，第三个为doInBackground返回和onPostExecute传入的参数)
    private class DBSaveSportsDataTask extends
            AsyncTask<List<SportsData>, Integer, List<SportsData>> {

        @Override
        protected synchronized List<SportsData> doInBackground(
                List<SportsData>... params) {
            Logger.d(TAG, "<<===>>进入保存运动数据线程1");
            Logger.d(TAG, "<<===>>Sport->[doInBackground]");
            isSaveData = true; // 已经有数据保存
            isReaded = true;
            // 保存最新获取到的运动数据到DB
            if (params[0] != null && params[0].size() > 0) {
                Logger.w(TAG, "<<===>>保存的运动数据条数=" + params[0].size());
                // Logger.i(TAG, "保存的运动数据条数params[0]=" + params[0] + " <--");
                dbService.saveSportsDataList(params[0]);
                mGetPedometerSportsDataList.clear();

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Logger.d(TAG, "<<===>>Sport->[onProgressUpdate]");
            // 更新进度
            Logger.i(TAG, ">>>>323" + values[0]);

        }

        @Override
        protected void onCancelled() {
            Logger.d(TAG, "<<===>>Sport->[onCancelled]");

            super.onCancelled();
        }

        @Override
        protected void onPostExecute(List<SportsData> result) {
            Logger.d(TAG, "<<===>>Sport->[onPostExecute]");

            // 根据当前是否断开标志发送重连
            // 。。？？
            Logger.d(TAG, "<<===>>mConnected:" + mConnected);
            if (!mConnected) {// 断开，重连
                if (mBluetoothLeService != null) {
                    Logger.d(TAG, "<<===>>mDeviceAddress:" + mDeviceAddress);
                    mBluetoothLeService.NeedSynTime = false;
                    Logger.d(TAG, "save data success! ,now del sport data, Bluetooth Disconnected...,reconnect.");
                    orderType = 0x09;  //手动删除数据
                    final boolean result1 = mBluetoothLeService
                            .connect(mDeviceAddress, SynDataActivity.this, true);
                    Logger.d(TAG, "<<===>>result1:" + result1 + " orderType="
                            + orderType);
                }

            } else {
                //	Logger.d(TAG, "<<===>>获取完运动详细数据，马上获取睡眠详细数据000>>========");
                orderType = 0x09;  //手动删除数据   // 2; // 获取完运动详细数据，再获取睡眠详细数据

                Logger.d(TAG, "save data success! ,now del sport data");

                setProgress(4);
                sendOrderToDevice(orderType);
				/*
				 * try { Thread.sleep(500); } catch (InterruptedException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); }
				 */
            }

            /**
             * 20140724 Logger.d(TAG, "<<===>>isBack：" + isBack); if (isBack) {
             * Logger.d(TAG, "<<===>>发送关闭");
             * mHandler.sendEmptyMessage(FINISH_CLOSE); }
             *
             * Logger.i(TAG, "33运动数据保存后isSportsed=" + isGetSportsDataFinished +
             * "　orderType=" + orderType);
             *
             * // 说明在获取详细数据的时候断连接了，保存详细运动数据后，再重新去获取一次运动详细数据：
             * if(isGetSportsDataFinished == false && orderType == 5 ) { if
             * (mBluetoothLeService != null) { final boolean result1 =
             * mBluetoothLeService.connect(mDeviceAddress); Logger.d(TAG,
             * "mBluetoothLeService.connect(mDeviceAddress)--》" + result1 +
             * " orderType=" + orderType); }
             *
             * }else{ //mHandler.sendEmptyMessage(FINISH_CLOSE); }
             */

            super.onPostExecute(result);
        }
    }

    // 睡眠数据保存到数据库(第一个为doInBackground接受的参数，第二个为显示进度的参数，第三个为doInBackground返回和onPostExecute传入的参数)
    private class DBSaveSleepDataTask extends
            AsyncTask<List<SleepData>, Integer, List<SleepData>> {

        @Override
        protected synchronized List<SleepData> doInBackground(
                List<SleepData>... params) {
            Logger.d(TAG, "<<===>>Sleep->[doInBackground]");
            isSaveData = true; // 已经有数据保存
            // 保存最新获取到的运动数据到DB
            if (params[0] != null && params[0].size() > 0) {
                Logger.w(TAG, "保存的睡眠数据条数=" + params[0].size());
                // Logger.i(TAG, "保存的运动数据条数params[0]=" + params[0] + " <--");
                dbService.saveSleepDataList(params[0]);
                dbService.saveSleepCacheDataList(mLocalSleepDataList);
                mLocalSleepDataList.clear();
                //	dbService.saveSleepCacheDataList(params);
                mGetPedometerSleepDataList.clear();

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Logger.d(TAG, "<<===>>Sleep->[onProgressUpdate]");
            // 更新进度
            Logger.i(TAG, "" + values[0]);

        }

        @Override
        protected void onCancelled() {
            Logger.d(TAG, "<<===>>Sleep->[onCancelled]");
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(List<SleepData> result) {
            Logger.d(TAG, "<<===>>Sleep->[onPostExecute]");
            // 根据当前是否断开标志发送重连
            // 。。？？
            Logger.d(TAG, "<<===>>mConnected:" + mConnected);
            if (!mConnected) {// 断开，重连
                if (mBluetoothLeService != null) {
                    Logger.d(TAG, "<<===>>mDeviceAddress:" + mDeviceAddress);
                    orderType = 0x0A;
                    mBluetoothLeService.NeedSynTime = false;
                    final boolean result1 = mBluetoothLeService
                            .connect(mDeviceAddress, SynDataActivity.this, true);
                    Logger.d(TAG, "<<===>>result1:" + result1 + " orderType="
                            + orderType);
                }

            } else {
                //Logger.d(TAG, "<<===>>准备发送关闭窗口");

                //
                Logger.d(TAG, "save sleep data ok ,now delete sleepdata!");
                orderType = 0x0A;  //手动删除睡眠数据
                sendOrderToDevice(orderType);
                //mHandler.sendEmptyMessage(CANCEL);
            }

            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Logger.d(TAG, "<<=>>KEYCODE_BACK");
            isBack = true;

            mHandler.sendEmptyMessage(FINISH_CLOSE);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
