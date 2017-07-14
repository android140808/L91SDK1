//package cn.appscomm.pedometer.activity;
//
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Application;
//import android.app.ProgressDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothManager;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.PopupWindow;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.RadioGroup.OnCheckedChangeListener;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ToggleButton;
//
//import com.bugtags.library.Bugtags;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Properties;
//import java.util.Random;
//
//import apps.utils.ConfigHelper;
//import apps.utils.HttpUtil;
//import apps.utils.Logger;
//import apps.utils.NumberUtils;
//import apps.utils.PropertiesUtil;
//import apps.utils.PublicData;
//import cn.appscomm.pedometer.UI.PickerWheelView;
//import cn.appscomm.pedometer.UI.TimeWheelPopupWindow;
//import cn.appscomm.pedometer.model.RemindNotesData;
//import cn.appscomm.pedometer.protocol.AboutState.RemindSetting;
//import cn.appscomm.pedometer.protocol.BluetoothUtil;
//import cn.appscomm.pedometer.protocol.Commands;
//import cn.appscomm.pedometer.protocol.IResultCallback;
//import cn.appscomm.pedometer.protocol.Leaf;
//import cn.appscomm.pedometer.service.BluetoothLeService;
//import cn.appscomm.pedometer.service.DBService;
//import cn.appscomm.pedometer.service.HttpResDataService;
//import cn.l11.appscomm.pedometer.activity.R;
//import kankan.wheel.widget.OnWheelScrollListener;
//import kankan.wheel.widget.WheelView;
//
///**
// * Created with Eclipse.
// * Author: Tim Liu  email:9925124@qq.com
// * Date: 14-3-17
// * Time: 22:57
// */
//public class RemindActivity extends Activity implements IResultCallback {
//    private final static String TAG = "RemindActivity";
//
//    //自定义的弹出框类
//    private TimeWheelPopupWindow wheelWindow;
//    private Integer int_hour_item;
//    private Integer int_minute_item;
//    private TextView textview_time;
//
//    private RadioGroup radiogroup, rbngp_unit; // radiobutton按钮组
//    private RadioButton rbn_am, rbn_pm;
//    private RadioButton radiobutton1; // 运动
//    private RadioButton radiobutton2; // 睡觉
//    private RadioButton radiobutton3; // 吃饭
//    private RadioButton radiobutton4; // 吃药
//    private RadioButton radiobutton5; // 喝水
//    private RadioButton radiobutton6; // 自定义
//    private ToggleButton togglebutton_time1; // 星期一
//    private ToggleButton togglebutton_time2; // 星期二
//    private ToggleButton togglebutton_time3;
//    private ToggleButton togglebutton_time4;
//    private ToggleButton togglebutton_time5;
//    private ToggleButton togglebutton_time6;
//    private ToggleButton togglebutton_time7; // 星期日
//    private String amOrPm = "1";    //1-am; 2-pm
//    private int remind_note_type = -1; // 提醒便签的类型， 	1：运动；2：睡觉；3：吃饭；4：吃药；5:喝水；6:自定义；
//    //	private StringBuffer strRemindWeek; // 从周日、六、五、四、三、二到周一，1是提醒，0是不提醒
//    private EditText edittext;
//    private DBService dbService;
//    private ProgressDialog mProgressDialog; // 进度条
//    private RemindNotesData mData;
//    private boolean isModify = false;
//    private HttpUtil mhttpUtil;
//    private TextView btn_right;
//    private Button btn_save;
//    //顶部时间、电量
//    private TextView top_title_time, top_title_battery;
//    private final String REMIND_EXTRA = "REMIND_EXTRA";
//    private final int REMIND_ACTIVITY_FINISH = 6666;
//    private final int REMIND_ACTIVITY_NOTHING = 6667;
//    private final int REMIND_ACTIVITY_ADDNEW = 6668;
//    private final int REMIND_ACTIVITY_MODIFIY = 6669;
//
//    private final int REMIND_ADD_SUCCESS = 6670;
//    private final int REMIND_ADD_FAIL = 6671;
//    private final int REMIND_ADD_TIMEOUT = 6672;
//    private final int REMIND_MOD_TIMEOUT = 6673;
//    private final int REMIND_MOD_SUCCESS = 6674;
//    private final int REMIND_MOD_FAIL = 6675;
//    private final int REMIND_DEL_TIMEOUT = 6676;
//    private int RandCode = 0;
//    private boolean Needresponse = false;
//    private boolean mIsBind = false; // service与广播是否已经绑定
//    private final int MAXTIMEOUT = 8000;
//    private boolean IsAlreadyReturn = false;
//    private boolean isOnClick = false;
//
//    private RemindNotesData cur_RemindData, del_RemindData = null;  //保存当前的操作的ReMindData
//
//    private Random rand = new Random(25);
//
//    private int orderType = 0;
//    private int retValue = 0;
//    private boolean isNeedConnect = false;
//    private String mDeviceAddress = "";
//    private BluetoothLeService mBluetoothLeService = null;
//    private String deviceType;
//
//    private List expansionList = new ArrayList();
//    private LinearLayout activityExpansionLinearLayout;
//    private ToggleButton activityExpansionToggleButton;
//    private LinearLayout activityLinearLayout;
//    private TextView activityTimeTextView;
//    private TextView activityAmPmTextView;
//    private ToggleButton activityToggleButtonItem1;
//    private ToggleButton activityToggleButtonItem2;
//    private ToggleButton activityToggleButtonItem3;
//    private ToggleButton activityToggleButtonItem4;
//    private ToggleButton activityToggleButtonItem5;
//    private ToggleButton activityToggleButtonItem6;
//    private ToggleButton activityToggleButtonItem7;
//    private LinearLayout sleepExpansionLinearLayout;
//    private ToggleButton sleepExpansionToggleButton;
//    private LinearLayout sleepLinearLayout;
//    private TextView sleepTimeTextView;
//    private TextView sleepAmPmTextView;
//    private ToggleButton sleepToggleButtonItem1;
//    private ToggleButton sleepToggleButtonItem2;
//    private ToggleButton sleepToggleButtonItem3;
//    private ToggleButton sleepToggleButtonItem4;
//    private ToggleButton sleepToggleButtonItem5;
//    private ToggleButton sleepToggleButtonItem6;
//    private ToggleButton sleepToggleButtonItem7;
//    private LinearLayout eatExpansionLinearLayout;
//    private ToggleButton eatExpansionToggleButton;
//    private LinearLayout eatLinearLayout;
//    private TextView eatTimeTextView;
//    private TextView eatAmPmTextView;
//    private ToggleButton eatToggleButtonItem1;
//    private ToggleButton eatToggleButtonItem2;
//    private ToggleButton eatToggleButtonItem3;
//    private ToggleButton eatToggleButtonItem4;
//    private ToggleButton eatToggleButtonItem5;
//    private ToggleButton eatToggleButtonItem6;
//    private ToggleButton eatToggleButtonItem7;
//    private LinearLayout medicineExpansionLinearLayout;
//    private ToggleButton medicineExpansionToggleButton;
//    private LinearLayout medicineLinearLayout;
//    private TextView medicineTimeTextView;
//    private TextView medicineAmPmTextView;
//    private ToggleButton medicineToggleButtonItem1;
//    private ToggleButton medicineToggleButtonItem2;
//    private ToggleButton medicineToggleButtonItem3;
//    private ToggleButton medicineToggleButtonItem4;
//    private ToggleButton medicineToggleButtonItem5;
//    private ToggleButton medicineToggleButtonItem6;
//    private ToggleButton medicineToggleButtonItem7;
//    private LinearLayout customExpansionLinearLayout;
//    private ToggleButton customExpansionToggleButton;
//    private LinearLayout customLinearLayout;
//    private TextView customTimeTextView;
//    private TextView customAmPmTextView;
//    private ToggleButton customToggleButtonItem1;
//    private ToggleButton customToggleButtonItem2;
//    private ToggleButton customToggleButtonItem3;
//    private ToggleButton customToggleButtonItem4;
//    private ToggleButton customToggleButtonItem5;
//    private ToggleButton customToggleButtonItem6;
//    private ToggleButton customToggleButtonItem7;
//    private LinearLayout wakeUpExpansionLinearLayout;
//    private ToggleButton wakeUpExpansionToggleButton;
//    private LinearLayout wakeUpLinearLayout;
//    private TextView wakeUpTimeTextView;
//    private TextView wakeUpAmPmTextView;
//    private ToggleButton wakeUpToggleButtonItem1;
//    private ToggleButton wakeUpToggleButtonItem2;
//    private ToggleButton wakeUpToggleButtonItem3;
//    private ToggleButton wakeUpToggleButtonItem4;
//    private ToggleButton wakeUpToggleButtonItem5;
//    private ToggleButton wakeUpToggleButtonItem6;
//    private ToggleButton wakeUpToggleButtonItem7;
//    String activityHour, wakeUpMinutes, activityAmPm;
//    private String[] remindWeek = new String[7];
//    private String remindText;
//    private TextView doneTextView;
//    private EditText customEditText;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
////		//去掉标题栏
////		requestWindowFeature(Window.FEATURE_NO_TITLE);
////		//全屏显示
////		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
////				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setContentView(R.layout.remind_view);
//        deviceType = (String) ConfigHelper.getSharePref(this,
//                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
//        initView();
//
//        initAM_PM();
//        initTime();
//
//        mhttpUtil = new HttpUtil(this);
//        setListeners();
//
//    }
//
//
//    private TimeClickListener clickls = new TimeClickListener();
//
//    @Override
//    public void onSuccess(Leaf leaf) {
//        BluetoothUtil.getInstance().continueSend();
//        byte commandCode = leaf.getCommandCode();
//        if (commandCode == Commands.COMMANDCODE_REMIND_SETTING) {    // 提醒数据
//            if (leaf.getAction() == Commands.ACTION_SET) {             // 设置查询
////                Message msg = Message.obtain();
////                msg.arg1 = REMIND_MSG_DEL;
////                msg.arg2 = mCurListIndex;
////                msg.what = SYNREMIN_OK;
//                Logger.e(TAG, "==>>SYNREMIN_OK3zzzzz");
////                mHandler.sendMessage(msg);
//                // 提醒设置
//                if (nowType == 0) {                                             // 提醒设置 删除
//                    addNewRemindNoteOrder(cur_RemindData);
//                    dbService.deleteRemindNoteByID(del_RemindData.remind_id);
////                    Message msg = Message.obtain();
////                    msg.what = REMIND_MOD_SUCCESS;
////                    mHandler.sendMessage(msg);
////                    Toast.makeText(this, "删除提醒成功!!!", 0).show();
//                } else if (nowType == 1) {
//
//                    Logger.e(TAG, "==>>SYNREMIN_OK3aaaa");
////                    Toast.makeText(this, "修改提醒成功!!!", 0).show();
//                } else if (nowType == 2) {
//                    cur_RemindData.remind_set_ok = 1;
//                    new Thread(ModRemindRunnable).start();
//                    Logger.e(TAG, "==>>SYNREMIN_OK3ssssss");
////                    Toast.makeText(this, "添加成功!!!", 0).show();
//                } else if (nowType == 3) {
////
////                    Toast.makeText(this, "按钮成功!!!", 0).show();
//                } else if (nowType == 4) {
//
////                    Toast.makeText(this, "按钮成功2!!!", 0).show();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onFaild(Leaf leaf) {
//        CloseProgressDiag();
//        Toast.makeText(this, "faild", 0).show();
//    }
//
//    class TimeClickListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View v) {
//            final int id1 = v.getId();
//
//            switch (id1) {
//                case R.id.timeWheelSave:
//                    int_hour_item = mCurrentHourItem;
//                    Logger.i("", "int_minute_item" + int_hour_item);
//                    int_minute_item = mCurrentMinItem;
//                    setTextViewTime(int_hour_item, int_minute_item);
//                    wheelWindow.dismiss();
//                    break;
//                case R.id.timeWheelCancel:
//                    mCurrentHourItem = int_hour_item;
//                    mCurrentMinItem = int_minute_item;
//                    wheelWindow.setWheelHourCurrentItem(int_hour_item);
//                    wheelWindow.setWheelMinuteCurrentItem(int_minute_item);
//                    wheelWindow.dismiss();
//                    break;
//                default:
//                    break;
//            }
//
//        }
//    }
//
//
//    private void ShowProgressDiag(String S1) {
//
//        //getString(R.string.syndata)
//        if (mProgressDialog == null)
//            mProgressDialog = ProgressDialog.show(RemindActivity.this, null,
//                    S1, true, true);
//
//        else
//            mProgressDialog.show();
//
//        mProgressDialog.setCanceledOnTouchOutside(false);
//
//
//    }
//
//
//    private void CloseProgressDiag() {
//        if (mProgressDialog != null)
//            mProgressDialog.dismiss();
//
//    }
//
//
//    Handler mHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//
//            switch (msg.what) {
//
//                case REMIND_ADD_SUCCESS:
//                    IsAlreadyReturn = true;
//                    ConfigHelper.setSharePref(RemindActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.REMINDS_AMORPM_ITEM_KEY, amOrPm);
//                    CloseProgressDiag();
//                    cur_RemindData.remind_id = msg.arg1;
//                    dbService.saveRemindNotesData(cur_RemindData);
//                    Intent it = new Intent();
//                    it.putExtra(REMIND_EXTRA, REMIND_ACTIVITY_ADDNEW);
//                    setResult(REMIND_ACTIVITY_FINISH, it);  //设置结果码
//                    finish();
//
//
//                    break;
//
//
//                case REMIND_ADD_TIMEOUT:
//                case REMIND_MOD_TIMEOUT:
//                    if (IsAlreadyReturn) return;
//                    CloseProgressDiag();
//                    /**
//                     DialogUtil.commonDialog(RemindActivity.this,
//                     getString(R.string.app_name),
//                     getString(R.string.setting_failed));
//                     */
//
//                    break;
//
//
//                case REMIND_DEL_TIMEOUT:
//
//                    if (IsAlreadyReturn) return;
//                    CloseProgressDiag();
//                    /**DialogUtil.commonDialog(RemindActivity.this,
//                     getString(R.string.app_name),
//                     getString(R.string.setting_failed));*/
//                    Toast.makeText(RemindActivity.this, R.string.make_sure_zefit, Toast.LENGTH_SHORT).show();
//
//                    break;
//
//                case REMIND_MOD_SUCCESS:
//                    IsAlreadyReturn = true;
//                    cur_RemindData.remind_set_ok = 1;
//                    dbService.saveRemindNotesData(cur_RemindData);
//                    Intent it1 = new Intent();
//                    it1.putExtra(REMIND_EXTRA, REMIND_ACTIVITY_MODIFIY);
//                    it1.putExtra("OLDDATA", mData);
//                    setResult(REMIND_ACTIVITY_FINISH, it1);  //设置结果码
//                    finish();
//
//                    break;
//
//                default:
//
//                    CloseProgressDiag();
//                    /**
//                     DialogUtil.commonDialog(RemindActivity.this,
//                     getString(R.string.app_name),
//                     getString(R.string.setting_failed));*/
//
//                    break;
//            }
//
//        }
//
//    };
//
//    private void initView() {
//        mData = (RemindNotesData) getIntent().getSerializableExtra("remindNotesData");
//
//        activityLinearLayout = (LinearLayout) findViewById(R.id.activityLinearLayout);
//        activityToggleButtonItem1 = (ToggleButton) findViewById(R.id.activityToggleButtonItem1);
//        activityToggleButtonItem2 = (ToggleButton) findViewById(R.id.activityToggleButtonItem2);
//        activityToggleButtonItem3 = (ToggleButton) findViewById(R.id.activityToggleButtonItem3);
//        activityToggleButtonItem4 = (ToggleButton) findViewById(R.id.activityToggleButtonItem4);
//        activityToggleButtonItem5 = (ToggleButton) findViewById(R.id.activityToggleButtonItem5);
//        activityToggleButtonItem6 = (ToggleButton) findViewById(R.id.activityToggleButtonItem6);
//        activityToggleButtonItem7 = (ToggleButton) findViewById(R.id.activityToggleButtonItem7);
//        activityExpansionLinearLayout = (LinearLayout) findViewById(R.id.activityExpansionLinearLayout);
//        activityExpansionToggleButton = (ToggleButton) findViewById(R.id.activityExpansionToggleButton);
//        activityExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    activityLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    activityExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(activityExpansionToggleButton, expansionList);
//                    Logger.i("", "breaktime");
//                    initRemindData();
//                    remindText = getString(R.string.activity);
//                    remind_note_type = 1;
//                } else if (!isChecked) {
//                    activityExpansionLinearLayout.setVisibility(View.GONE);
//                    activityLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        activityTimeTextView = (TextView) findViewById(R.id.activityTimeTextView);
//        activityAmPmTextView = (TextView) findViewById(R.id.activityAmPmTextView);
//        activityTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(activityTimeTextView, activityAmPmTextView);
//            }
//        });
//        expansionList.add(activityExpansionToggleButton);
//
//        sleepLinearLayout = (LinearLayout) findViewById(R.id.sleepLinearLayout);
//        sleepToggleButtonItem1 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem1);
//        sleepToggleButtonItem2 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem2);
//        sleepToggleButtonItem3 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem3);
//        sleepToggleButtonItem4 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem4);
//        sleepToggleButtonItem5 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem5);
//        sleepToggleButtonItem6 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem6);
//        sleepToggleButtonItem7 = (ToggleButton) findViewById(R.id.sleepToggleButtonItem7);
//        sleepExpansionLinearLayout = (LinearLayout) findViewById(R.id.sleepExpansionLinearLayout);
//        sleepExpansionToggleButton = (ToggleButton) findViewById(R.id.sleepExpansionToggleButton);
//        sleepTimeTextView = (TextView) findViewById(R.id.sleepTimeTextView);
//        sleepAmPmTextView = (TextView) findViewById(R.id.sleepAmPmTextView);
//        sleepExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    sleepLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    sleepExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(sleepExpansionToggleButton, expansionList);
//                    Logger.i("", "sleep");
//                    initRemindData();
//                    remindText = getString(R.string.sleep);
//                    remind_note_type = 2;
//                } else if (!isChecked) {
//                    sleepExpansionLinearLayout.setVisibility(View.GONE);
//                    sleepLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        sleepTimeTextView = (TextView) findViewById(R.id.sleepTimeTextView);
//        sleepTimeTextView = (TextView) findViewById(R.id.sleepTimeTextView);
//        sleepTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(sleepTimeTextView, sleepAmPmTextView);
//            }
//        });
//        expansionList.add(sleepExpansionToggleButton);
//
//        eatLinearLayout = (LinearLayout) findViewById(R.id.eatLinearLayout);
//        eatToggleButtonItem1 = (ToggleButton) findViewById(R.id.eatToggleButtonItem1);
//        eatToggleButtonItem2 = (ToggleButton) findViewById(R.id.eatToggleButtonItem2);
//        eatToggleButtonItem3 = (ToggleButton) findViewById(R.id.eatToggleButtonItem3);
//        eatToggleButtonItem4 = (ToggleButton) findViewById(R.id.eatToggleButtonItem4);
//        eatToggleButtonItem5 = (ToggleButton) findViewById(R.id.eatToggleButtonItem5);
//        eatToggleButtonItem6 = (ToggleButton) findViewById(R.id.eatToggleButtonItem6);
//        eatToggleButtonItem7 = (ToggleButton) findViewById(R.id.eatToggleButtonItem7);
//        eatExpansionLinearLayout = (LinearLayout) findViewById(R.id.eatExpansionLinearLayout);
//        eatExpansionToggleButton = (ToggleButton) findViewById(R.id.eatExpansionToggleButton);
//        eatTimeTextView = (TextView) findViewById(R.id.eatTimeTextView);
//        eatAmPmTextView = (TextView) findViewById(R.id.eatAmPmTextView);
//        eatExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    eatLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    eatExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(eatExpansionToggleButton, expansionList);
//                    Logger.i("", "mealtime");
//
//                    initRemindData();
//                    remindText = getString(R.string.eat1);
//                    remind_note_type = 3;
//                } else if (!isChecked) {
//                    eatExpansionLinearLayout.setVisibility(View.GONE);
//                    eatLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        eatTimeTextView = (TextView) findViewById(R.id.eatTimeTextView);
//        eatTimeTextView = (TextView) findViewById(R.id.eatTimeTextView);
//        eatTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(eatTimeTextView, eatAmPmTextView);
//            }
//        });
//        expansionList.add(eatExpansionToggleButton);
//
//        medicineLinearLayout = (LinearLayout) findViewById(R.id.medicineLinearLayout);
//        medicineToggleButtonItem1 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem1);
//        medicineToggleButtonItem2 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem2);
//        medicineToggleButtonItem3 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem3);
//        medicineToggleButtonItem4 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem4);
//        medicineToggleButtonItem5 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem5);
//        medicineToggleButtonItem6 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem6);
//        medicineToggleButtonItem7 = (ToggleButton) findViewById(R.id.medicineToggleButtonItem7);
//        medicineExpansionLinearLayout = (LinearLayout) findViewById(R.id.medicineExpansionLinearLayout);
//        medicineExpansionToggleButton = (ToggleButton) findViewById(R.id.medicineExpansionToggleButton);
//        medicineTimeTextView = (TextView) findViewById(R.id.medicineTimeTextView);
//        medicineAmPmTextView = (TextView) findViewById(R.id.medicineAmPmTextView);
//        medicineExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    medicineLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    medicineExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(medicineExpansionToggleButton, expansionList);
//                    Logger.i("", "medicine");
//
//                    initRemindData();
//                    remindText = getString(R.string.medicine);
//                    remind_note_type = 4;
//                } else if (!isChecked) {
//                    medicineExpansionLinearLayout.setVisibility(View.GONE);
//                    medicineLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        medicineTimeTextView = (TextView) findViewById(R.id.medicineTimeTextView);
//        medicineTimeTextView = (TextView) findViewById(R.id.medicineTimeTextView);
//        medicineTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(medicineTimeTextView, medicineAmPmTextView);
//            }
//        });
//        expansionList.add(medicineExpansionToggleButton);
//
//        wakeUpLinearLayout = (LinearLayout) findViewById(R.id.wakeUpLinearLayout);
//        wakeUpToggleButtonItem1 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem1);
//        wakeUpToggleButtonItem2 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem2);
//        wakeUpToggleButtonItem3 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem3);
//        wakeUpToggleButtonItem4 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem4);
//        wakeUpToggleButtonItem5 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem5);
//        wakeUpToggleButtonItem6 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem6);
//        wakeUpToggleButtonItem7 = (ToggleButton) findViewById(R.id.wakeUpToggleButtonItem7);
//        wakeUpExpansionLinearLayout = (LinearLayout) findViewById(R.id.wakeUpExpansionLinearLayout);
//        wakeUpExpansionToggleButton = (ToggleButton) findViewById(R.id.wakeUpExpansionToggleButton);
//        wakeUpTimeTextView = (TextView) findViewById(R.id.wakeUpTimeTextView);
//        wakeUpAmPmTextView = (TextView) findViewById(R.id.wakeUpAmPmTextView);
//        wakeUpExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    wakeUpLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    wakeUpExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(wakeUpExpansionToggleButton, expansionList);
//                    Logger.i("", "wakeup");
//
//                    initRemindData();
//                    remindText = getString(R.string.wake_up);
//                    remind_note_type = 5;
//                } else if (!isChecked) {
//                    wakeUpExpansionLinearLayout.setVisibility(View.GONE);
//                    wakeUpLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        wakeUpTimeTextView = (TextView) findViewById(R.id.wakeUpTimeTextView);
//        wakeUpTimeTextView = (TextView) findViewById(R.id.wakeUpTimeTextView);
//        wakeUpTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(wakeUpTimeTextView, wakeUpAmPmTextView);
//            }
//        });
//        expansionList.add(wakeUpExpansionToggleButton);
//
//        customLinearLayout = (LinearLayout) findViewById(R.id.customLinearLayout);
//        customToggleButtonItem1 = (ToggleButton) findViewById(R.id.customToggleButtonItem1);
//        customToggleButtonItem2 = (ToggleButton) findViewById(R.id.customToggleButtonItem2);
//        customToggleButtonItem3 = (ToggleButton) findViewById(R.id.customToggleButtonItem3);
//        customToggleButtonItem4 = (ToggleButton) findViewById(R.id.customToggleButtonItem4);
//        customToggleButtonItem5 = (ToggleButton) findViewById(R.id.customToggleButtonItem5);
//        customToggleButtonItem6 = (ToggleButton) findViewById(R.id.customToggleButtonItem6);
//        customToggleButtonItem7 = (ToggleButton) findViewById(R.id.customToggleButtonItem7);
//        customExpansionLinearLayout = (LinearLayout) findViewById(R.id.customExpansionLinearLayout);
//        customExpansionToggleButton = (ToggleButton) findViewById(R.id.customExpansionToggleButton);
//        customTimeTextView = (TextView) findViewById(R.id.customTimeTextView);
//        customAmPmTextView = (TextView) findViewById(R.id.customAmPmTextView);
//        customEditText = (EditText) findViewById(R.id.customEditText);
//        customEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                remindText = s.toString();
//            }
//        });
//        customExpansionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    customLinearLayout.setBackgroundColor(Color.argb(106, 0, 0, 0));
//                    customExpansionLinearLayout.setVisibility(View.VISIBLE);
//                    expansionOff(customExpansionToggleButton, expansionList);
//                    Logger.i("", "custom");
//
//                    initRemindData();
//                    remindText = getString(R.string.custom);
//                    remind_note_type = 6;
//                } else if (!isChecked) {
//                    customExpansionLinearLayout.setVisibility(View.GONE);
//                    customLinearLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        });
//        customTimeTextView = (TextView) findViewById(R.id.customTimeTextView);
//        customTimeTextView = (TextView) findViewById(R.id.customTimeTextView);
//        customTimeTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopupWindow(customTimeTextView, customAmPmTextView);
//            }
//        });
//        expansionList.add(customExpansionToggleButton);
//
//        TextView title = (TextView) findViewById(R.id.title);
//        title.setText(R.string.reminds_title);
//
//        top_title_time = (TextView) findViewById(R.id.top_title_time);
//        top_title_battery = (TextView) findViewById(R.id.top_title_battery);
//
//        btn_right = (TextView) findViewById(R.id.btn_right);
//        btn_save = (Button) findViewById(R.id.btn_save);
//
//        textview_time = (TextView) findViewById(R.id.textview_time);
////		int_hour_item =  (Integer)ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, "HOUR_ITEM", ConfigHelper.DATA_INT);
////		int_minute_item =  (Integer)ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, "MINUTE_ITEM", ConfigHelper.DATA_INT);
//
//
//        // radiobutton按钮组
//        radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
//        rbngp_unit = (RadioGroup) findViewById(R.id.rbngp_unit);
//        rbn_am = (RadioButton) findViewById(R.id.rbn_am);
//        rbn_pm = (RadioButton) findViewById(R.id.rbn_pm);
//        radiobutton1 = (RadioButton) findViewById(R.id.radiobutton1);
//        radiobutton2 = (RadioButton) findViewById(R.id.radiobutton2);
//        radiobutton3 = (RadioButton) findViewById(R.id.radiobutton3);
//        radiobutton4 = (RadioButton) findViewById(R.id.radiobutton4);
//        radiobutton5 = (RadioButton) findViewById(R.id.radiobutton5);
//        radiobutton6 = (RadioButton) findViewById(R.id.radiobutton6);
//
//        edittext = (EditText) findViewById(R.id.edittext);
//
//        togglebutton_time1 = (ToggleButton) findViewById(R.id.togglebutton_time1);
//        togglebutton_time2 = (ToggleButton) findViewById(R.id.togglebutton_time2);
//        togglebutton_time3 = (ToggleButton) findViewById(R.id.togglebutton_time3);
//        togglebutton_time4 = (ToggleButton) findViewById(R.id.togglebutton_time4);
//        togglebutton_time5 = (ToggleButton) findViewById(R.id.togglebutton_time5);
//        togglebutton_time6 = (ToggleButton) findViewById(R.id.togglebutton_time6);
//        togglebutton_time7 = (ToggleButton) findViewById(R.id.togglebutton_time7);
//
//
//        //当点击不同按钮时触发的事件
//        rbngp_unit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                switch (i) {
//                    case R.id.rbn_am:
//                        Logger.e(TAG, "--am");
//                        amOrPm = "1";
//
//                        try {
//                            Class<?> clazz = Class.forName("android.app.ActivityThread");
//                            Field field = clazz.getDeclaredField("sCurrentActivityThread");
//                            field.setAccessible(true);
//                            //得到ActivityThread的对象，虽然是隐藏的，但已经指向了内存的堆地址
//                            Object object = field.get(null);
//                            Logger.d("[app]", "object=" + object);
//                            Method method = clazz.getDeclaredMethod("getApplication");
//                            method.setAccessible(true);
//                            Application application = (Application) method.invoke(object);
//                            Logger.d("[app]", "application=" + application);
//                            Logger.d("[app]", "程序的application对象=" + getApplication());
//                        } catch (ClassNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (NoSuchFieldException e) {
//                            e.printStackTrace();
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        } catch (NoSuchMethodException e) {
//                            e.printStackTrace();
//                        } catch (InvocationTargetException e) {
//                            e.printStackTrace();
//                        }
//
//                        break;
//
//                    case R.id.rbn_pm:
//                        Logger.e(TAG, "--pm");
//                        amOrPm = "2";
//
//                        break;
//                }
//            }
//        });
//        radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//                switch (checkedId) {
//
//                    case R.id.radiobutton1:
//                        remind_note_type = 1;
//                        break;
//
//                    case R.id.radiobutton2:
//                        remind_note_type = 2;
//                        break;
//
//                    case R.id.radiobutton3:
//                        remind_note_type = 3;
//                        break;
//
//                    case R.id.radiobutton4:
//                        remind_note_type = 4;
//                        break;
//
//                    case R.id.radiobutton5:
//                        remind_note_type = 5;
//                        if (isNoteTypeText())
//                            break;
//
//                    case R.id.radiobutton6:
//                        isOnClick = true;
//                        remind_note_type = 6;
//                        break;
//
//                    default:
//                        break;
//                }
//                Logger.i("", " choiceNoteType(remind_note_type);");
//                choiceNoteType(remind_note_type);
//            }
//
//
//        });
//
//        // 如果是修改的，就用修改数据初始化view
//        if (mData != null) {
//
//
//            isModify = true;
//            Logger.i("", "int_minute_item" + int_hour_item);
//
//            int_hour_item = mData.remind_time_hours;
//            int_minute_item = mData.remind_time_minutes;
//            setTextViewTime(int_hour_item, int_minute_item);
//
//            choiceNoteType(mData.remind_type);
//            setTextViewTime(mData.remind_time_hours, mData.remind_time_minutes);
//
//            // 注意提醒是从周一开始、周二、周三 、周四、周五、周六、周日
//            if (mData.remind_week.trim().substring(6, 7).equals("1")) {
//                togglebutton_time1.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(5, 6).equals("1")) {
//                togglebutton_time2.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(4, 5).equals("1")) {
//                togglebutton_time3.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(3, 4).equals("1")) {
//                togglebutton_time4.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(2, 3).equals("1")) {
//                togglebutton_time5.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(1, 2).equals("1")) {
//                togglebutton_time6.setChecked(true);
//            }
//            if (mData.remind_week.trim().substring(0, 1).equals("1")) {
//                togglebutton_time7.setChecked(true);
//            }
//
//        } else {
//
//            // 默认值是系统时间推后1小时
////    		textview_time_hour.setText(arr[int_item]);
//            Calendar calendar = Calendar.getInstance();
//            //不推后1小时，取当前时间
//            //calendar.add(Calendar.HOUR_OF_DAY, +1);
//            int_hour_item = calendar.get(Calendar.HOUR);
//            int_minute_item = calendar.get(Calendar.MINUTE);
//            Logger.e(TAG, "int_hour_item=" + int_hour_item);
//            setTextViewTime(int_hour_item, int_minute_item);
//        }
//
//        dbService = new DBService(this);
//    }
//
//    DecimalFormat df = new DecimalFormat("00");
//    String AM_PM;
//    int hoursIndex, minutesIndex, am_pmIndex;
//
//    /**
//     * 弹出时间选择器
//     *
//     * @param timeTextView
//     * @param AmPmTextView
//     */
//    private void showPopupWindow(final TextView timeTextView, final TextView AmPmTextView) {
//        Logger.i("", "showPopupWindow");
//
//        Calendar calendar = Calendar.getInstance();
//        View popupView = getLayoutInflater().inflate(R.layout.time_popupwindow, null);
//        List<String> hoursList = new ArrayList();
//        for (int i = 1; i <= 12; i++) {
//            hoursList.add(String.valueOf(i));
//        }
//        List<String> minutesList = new ArrayList<String>();
//        for (int i = 0; i <= 59; i++) {
//            DecimalFormat df = new DecimalFormat("00");
//            minutesList.add(df.format(i));
//        }
//
//        String time = timeTextView.getText().toString();
//        if (time != null && !"".equals(time)) {
//            String[] timeArray = time.split(":");
//            String hour = timeArray[0];
//            String minutes = timeArray[1];
//            hoursIndex = hoursList.indexOf(hour);
//            minutesIndex = minutesList.indexOf(minutes);
//            Logger.e(TAG, "int_hour_item=" + int_hour_item);
//
//            int_hour_item = Integer.parseInt(hour);
//            int_minute_item = Integer.parseInt(minutes);
//        }
//
//        AM_PM = AmPmTextView.getText().toString();
//        if (AM_PM.equals(getString(R.string.reminder_am)))
//            am_pmIndex = 0;
//        else if (AM_PM.equals(getString(R.string.reminder_pm)))
//            am_pmIndex = 1;
//
//        PickerWheelView hoursTimePickerWheelView = (PickerWheelView) popupView.findViewById(R.id.hoursPickerWheelView);
//        hoursTimePickerWheelView.setOffset(1);
//        hoursTimePickerWheelView.setItems(hoursList);
//        hoursTimePickerWheelView.setSeletion(hoursIndex);
//        hoursTimePickerWheelView.setOnWheelViewListener(new PickerWheelView.OnWheelViewListener() {
//            @Override
//            public void onSelected(int selectedIndex, String item) {
//                Logger.d(TAG, "selectedIndex: " + selectedIndex + ", item: " + item);
//                Logger.e(TAG, "int_hour_item=" + int_hour_item);
//
//                int_hour_item = Integer.parseInt(item);
//            }
//        });
//
//        PickerWheelView minutesTimePickerWheelView = (PickerWheelView) popupView.findViewById(R.id.minutesPickerWheelView);
//        minutesTimePickerWheelView.setItems(minutesList);
//        minutesTimePickerWheelView.setSeletion(minutesIndex);
//        minutesTimePickerWheelView.setOnWheelViewListener(new PickerWheelView.OnWheelViewListener() {
//            @Override
//            public void onSelected(int selectedIndex, String item) {
//                Logger.d(TAG, "selectedIndex: " + selectedIndex + ", item: " + item);
//                int_minute_item = selectedIndex - 1;
//            }
//        });
//        String[] am_pm = new String[]{getString(R.string.reminder_am), getString(R.string.reminder_pm)};
//        PickerWheelView AM_PMTimePickerWheelView = (PickerWheelView) popupView.findViewById(R.id.AM_PMPickerWheelView);
//        AM_PMTimePickerWheelView.setItems(Arrays.asList(am_pm));
//        AM_PMTimePickerWheelView.setSeletion(am_pmIndex);
//        AM_PMTimePickerWheelView.setOnWheelViewListener(new PickerWheelView.OnWheelViewListener() {
//            @Override
//            public void onSelected(int selectedIndex, String item) {
//                Logger.d(TAG, "selectedIndex: " + selectedIndex + ", item: " + item);
//                amOrPm = String.valueOf(selectedIndex);
//                AM_PM = item;
//            }
//        });
//
//        final PopupWindow mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams
//                .WRAP_CONTENT, true);
//        mPopupWindow.setTouchable(true);
//        mPopupWindow.setOutsideTouchable(true);
//        mPopupWindow.showAtLocation(RemindActivity.this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//        Logger.i("", "cancelTextView");
//
//        TextView doneTextView = (TextView) popupView.findViewById(R.id.doneTextView);
//        TextView cancelTextView = (TextView) popupView.findViewById(R.id.cancelTextView);
//
//        cancelTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Logger.i("", "cancelTextView");
//                mPopupWindow.dismiss();
//            }
//        });
//
//        doneTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Logger.i("", "doneTextView");
//
//                timeTextView.setText(String.valueOf(int_hour_item) + ":" + df.format(int_minute_item));
//                AmPmTextView.setText(AM_PM);
//                mPopupWindow.dismiss();
//            }
//        });
//
//    }
//
//    /**
//     * 关闭其他展开的选项
//     *
//     * @param expansionToggleButton 展开的项
//     * @param expansionList         所有项集合
//     */
//    private void expansionOff(ToggleButton expansionToggleButton, List expansionList) {
//        for (int i = 0; i < expansionList.size(); i++) {
//            ToggleButton toggleButton = (ToggleButton) expansionList.get(i);
//            if (toggleButton != expansionToggleButton)
//                toggleButton.setChecked(false);
//        }
//    }
//
//    public void setListeners() {
//        btn_right.setOnClickListener(new ClickListener());
//        btn_save.setOnClickListener(new ClickListener());
//        textview_time.setOnClickListener(new ClickListener());
//    }
//
//
//    //
//    private void choiceNoteType(int note_type) {
//        DecimalFormat df = new DecimalFormat("00");
//        String minutes = df.format(mData.remind_time_minutes);
//        int hours = mData.remind_time_hours > 12 ? mData.remind_time_hours - 12 : mData.remind_time_hours;
//        Logger.i("", "choiceNoteType" + note_type);
//        if (isNoteTypeText() && note_type == 1) {
//            edittext.setText(getString(R.string.sport).replace(" ", ""));
//            edittext.setEnabled(false);
//            radiobutton1.setChecked(true);
//
//            activityExpansionToggleButton.setChecked(true);
//            activityTimeTextView.setText(hours + ":" + minutes);
//            activityAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            activityToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            activityToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            activityToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            activityToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            activityToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            activityToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            activityToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        } else if (isNoteTypeText() && note_type == 2) {
//            edittext.setText(getString(R.string.sleep));
//            edittext.setEnabled(false);
//            radiobutton2.setChecked(true);
//
//            sleepExpansionToggleButton.setChecked(true);
//            sleepTimeTextView.setText(hours + ":" + minutes);
//            sleepAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            sleepToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            sleepToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            sleepToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            sleepToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            sleepToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            sleepToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            sleepToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        } else if (isNoteTypeText() && note_type == 3) {
//            edittext.setText(getString(R.string.eat).replace(" ", ""));
//            edittext.setEnabled(false);
//            radiobutton3.setChecked(true);
//
//            eatExpansionToggleButton.setChecked(true);
//            eatTimeTextView.setText(hours + ":" + minutes);
//            eatAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            eatToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            eatToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            eatToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            eatToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            eatToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            eatToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            eatToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        } else if (isNoteTypeText() && note_type == 4) {
//            edittext.setText(getString(R.string.medicine));
//            edittext.setEnabled(false);
//            radiobutton4.setChecked(true);
//
//            medicineExpansionToggleButton.setChecked(true);
//            medicineTimeTextView.setText(hours + ":" + minutes);
//            medicineAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            medicineToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            medicineToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            medicineToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            medicineToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            medicineToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            medicineToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            medicineToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        } else if (isNoteTypeText() && note_type == 5) {
//            edittext.setText(getString(R.string.wake_up).replace(" ", ""));
//            edittext.setEnabled(false);
//            radiobutton5.setChecked(true);
//
//            wakeUpExpansionToggleButton.setChecked(true);
//            wakeUpTimeTextView.setText(hours + ":" + minutes);
//            wakeUpAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            wakeUpToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            wakeUpToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            wakeUpToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            wakeUpToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            wakeUpToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            wakeUpToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            wakeUpToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        } else if (isNoteTypeText() && note_type == 6) {
//            edittext.setText("");
//            if (mData != null && isOnClick) {
//                String str = mData.remind_text;
//                if (str != null && str.length() > 10) {
//                    str = str.substring(0, 10);
//                }
//                edittext.setText(str);
//            }
//            isOnClick = false;
//            deviceType = (String) ConfigHelper.getSharePref(this,
//                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                    PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
//            if (deviceType.equals(PublicData.L28T)) {
//                edittext.setEnabled(false);
//            } else {
//                edittext.setEnabled(true);
//            }
//            radiobutton6.setChecked(true);
//
//            customExpansionToggleButton.setChecked(true);
//            customTimeTextView.setText(hours + ":" + minutes);
//            customAmPmTextView.setText(mData.remind_time_hours > 12 ? R.string.reminder_pm : R.string.reminder_am);
//            customToggleButtonItem1.setChecked(mData.remind_week.substring(0, 1).equals("0") ? false : true);
//            customToggleButtonItem2.setChecked(mData.remind_week.substring(6, 7).equals("0") ? false : true);
//            customToggleButtonItem3.setChecked(mData.remind_week.substring(5, 6).equals("0") ? false : true);
//            customToggleButtonItem4.setChecked(mData.remind_week.substring(4, 5).equals("0") ? false : true);
//            customToggleButtonItem5.setChecked(mData.remind_week.substring(3, 4).equals("0") ? false : true);
//            customToggleButtonItem6.setChecked(mData.remind_week.substring(2, 3).equals("0") ? false : true);
//            customToggleButtonItem7.setChecked(mData.remind_week.substring(1, 2).equals("0") ? false : true);
//        }
//    }
//
//    // 文字是否是定义好的范围内
//    private boolean isNoteTypeText() {
//        String str = edittext.getText().toString().trim().toLowerCase();
//        if (str.equals("sports") || str.equals("sleep") || str.equals("eat") || str.equals("medicine") || str.equals("wake up") || str.equals(""))
//            return true;
//        else
//            //return false;
//            return true;
//    }
//
//
//    /**
//     * feigle
//     * 2016-4-22
//     * 初始化上午下午选择器
//     */
//    private void initAM_PM() {
//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rbngp_unit);
//        Calendar calendar = Calendar.getInstance();
//        int AM_PM = calendar.get(Calendar.AM_PM);
//        if (AM_PM == Calendar.PM) {
////            rbn_am.setChecked(false);
////            rbn_pm.setChecked(true);
//            amOrPm = "2";
//            activityAmPmTextView.setText(getString(R.string.reminder_pm));
//            sleepAmPmTextView.setText(getString(R.string.reminder_pm));
//            eatAmPmTextView.setText(getString(R.string.reminder_pm));
//            medicineAmPmTextView.setText(getString(R.string.reminder_pm));
//            wakeUpAmPmTextView.setText(getString(R.string.reminder_pm));
//            customAmPmTextView.setText(getString(R.string.reminder_pm));
//        } else {
////            rbn_pm.setChecked(false);
////            rbn_am.setChecked(true);
//            amOrPm = "1";
//            activityAmPmTextView.setText(getString(R.string.reminder_am));
//            sleepAmPmTextView.setText(getString(R.string.reminder_am));
//            eatAmPmTextView.setText(getString(R.string.reminder_am));
//            medicineAmPmTextView.setText(getString(R.string.reminder_am));
//            wakeUpAmPmTextView.setText(getString(R.string.reminder_am));
//            customAmPmTextView.setText(getString(R.string.reminder_am));
//        }
//        if (mData != null) {
//            Logger.i("", " choiceNoteType(remind_note_type);");
//            choiceNoteType(mData.remind_type);
//        }
//    }
//
//    private void initTime() {
//        DecimalFormat df = new DecimalFormat("00");
//        Calendar calendar = Calendar.getInstance();
//
//        Logger.i("", "int_hour_item=" + int_hour_item);
//        if (mData != null) {
//            int_hour_item = mData.remind_time_hours;
//            int_minute_item = mData.remind_time_minutes;
//
//        } else {
//            int_hour_item = calendar.get(Calendar.HOUR);
//            int_minute_item = calendar.get(Calendar.MINUTE);
//        }
//        Logger.i("", "int_hour_item=" + int_hour_item);
//
//        activityTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//        sleepTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//        eatTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//        medicineTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//        wakeUpTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//        customTimeTextView.setText(calendar.get(Calendar.HOUR) + ":" + df.format(calendar.get(Calendar.MINUTE)));
//
//        if (mData != null) {
//            Logger.i("", " choiceNoteType(remind_note_type);");
//            choiceNoteType(mData.remind_type);
//        }
//    }
//
//    private void initRemindData() {
//        remind_note_type = -1;
//        remindWeek = new String[7];
//        remindText = "";
//        Calendar calendar = Calendar.getInstance();
//        Logger.e(TAG, "int_hour_item=" + int_hour_item);
//        int_hour_item = calendar.get(Calendar.HOUR);
//        int_minute_item = calendar.get(Calendar.MINUTE);
//    }
//
//    private void setTextViewTime(int hour, int mins) {
//        //设置时间
//        Date mDate = new Date();
//        mDate.setHours(hour);
//        mDate.setMinutes(mins);
//
//        //格式化时间
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
//        String time = sdf.format(mDate);
//
//        Logger.e(TAG, "hour=" + hour + "/mins=" + mins);
//        String strTime = "";
//
//        if (rbn_pm.isChecked()) {
////            hour = hour - 12;
////            rbn_am.setChecked(false);
////            rbn_pm.setChecked(true);
//            amOrPm = "2";
//        } else if (rbn_am.isChecked()) {
////            rbn_am.setChecked(true);
////            rbn_pm.setChecked(false);
//            amOrPm = "1";
//        }
//
//        if (!isModify) {
////            String amOrPm = (String) ConfigHelper.getSharePref(RemindActivity.this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData
////                    .REMINDS_AMORPM_ITEM_KEY, ConfigHelper.DATA_STRING);
//            Logger.e(TAG, "amOrPm=" + amOrPm);
//            setTimeType(amOrPm);
//        }
//
//
//        if (hour >= 0 && hour < 9) {
//            strTime = "0" + (hour + 1);
//        } else {
//            if (hour >= 12) {
//                strTime = "" + (hour - 12 + 1);
//                amOrPm = "2";
//            } else {
//                strTime = "" + (hour + 1);
//                amOrPm = "1";
//            }
//        }
//        setTimeType(amOrPm);
//
//        if (mins >= 0 && mins < 10) {
//            strTime = strTime + ":0" + mins;
//        } else {
//            strTime = strTime + ":" + mins;
//        }
//        textview_time.setText(strTime);
//
//        mCurrentHourItem = hour;
//        mCurrentMinItem = mins;
////		ConfigHelper.setSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, "HOUR_ITEM", int_hour_item);
////		ConfigHelper.setSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, "MINUTE_ITEM", int_minute_item);
//    }
//
//    private void setTimeType(String amOrPm) {
//        Logger.i(TAG, "--set TimeType-- ... amOrPm=" + amOrPm);
//        if ("1".equals(amOrPm)) {//1-am	;2-pm
//            Logger.e(TAG, "33333333333333");
//            rbn_pm.setChecked(false);
//            rbn_am.setChecked(true);
//        }
//        if ("2".equals(amOrPm)) {
//            Logger.e(TAG, "aaaaaaaaaa");
//            rbn_am.setChecked(false);
//            rbn_pm.setChecked(true);
//        }
//    }
//
//    // Tim 返回// 从周日、六、五、四、三、二到周一，1是提醒，0是不提醒
//    private String remindWeek() {
//        StringBuffer strRemindWeek = new StringBuffer();
////        if (togglebutton_time7.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time6.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time5.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time4.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time3.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time2.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
////        if (togglebutton_time1.isChecked()) strRemindWeek.append("1");
////        else strRemindWeek.append("0");
//        Logger.i("", "remindWeek");
//        switch (remind_note_type) {
//            case 1:
//                if (activityToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (activityToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//            case 2:
//                if (sleepToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (sleepToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//            case 3:
//                if (eatToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (eatToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//            case 4:
//                if (medicineToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (medicineToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//            case 5:
//                if (wakeUpToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (wakeUpToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//            case 6:
//                if (customToggleButtonItem1.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem7.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem6.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem5.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem4.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem3.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                if (customToggleButtonItem2.isChecked()) strRemindWeek.append("1");
//                else strRemindWeek.append("0");
//                break;
//        }
//
//        Logger.i(TAG, "周日、六、五、四、三、二、一：" + strRemindWeek);
//
//        return strRemindWeek.toString().trim();
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Logger.d(TAG, "Pause..");
//        Needresponse = false;
//
//        if (mIsBind == true) {
//            if (deviceType.equals(PublicData.L38I)) {
////                BluetoothUtil.getInstance().endBroadcast();
//            } else {
//                unbindService(mServiceConnection);
//                unregisterReceiver(mGattUpdateReceiver);
//                if (mBluetoothLeService != null) mBluetoothLeService.close();
//                mBluetoothLeService = null;
//            }
//            mIsBind = false;
//        }
//
//        Bugtags.onPause(this);
//        //unregisterReceiver(mBatteryInfoReceiver);
//    }
//
//    @Override
//    protected void onResume() {
//        Logger.d(TAG, "resume..");
//        super.onResume();
//        bindLeService();
//        Bugtags.onResume(this);
//        //registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Bugtags.onDispatchTouchEvent(this, ev);
//        return super.dispatchTouchEvent(ev);
//    }
//
//    private void bindLeService() {
//        mIsBind = true;
//        if (deviceType.equals(PublicData.L38I)) {
//            BluetoothUtil.getInstance().startBroadcast();
//        } else {
//            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//            registerReceiver(mGattUpdateReceiver, BluetoothLeService.makeGattUpdateIntentFilter());
//        }
//    }
//
//
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };
//
//
//    private void sendOrderToDevice(int orderType) {
//        //	if(mGattPedometerCharacteristic_1 != null && mGattPedometerCharacteristic_2 != null)
//
//        if (mBluetoothLeService != null) {
//
//
//            byte[] bytes = null;
//            switch (orderType) {
//                case 1:  //del
//                    bytes = clearARemindNoteOrder();
//                    break;
//
//                default:
//                    break;
//            }
//
//            if (mBluetoothLeService != null && !deviceType.equals(PublicData.L28T) && !deviceType.equals(PublicData.W01)
//                    && !deviceType.equals(PublicData.L39) && !deviceType.equals(PublicData.L38I))
//                mBluetoothLeService.sendPackets(bytes);
//            else
//                mBluetoothLeService.sendDataToPedometer(bytes);
//            Logger.d(TAG, "send orderdata is " + NumberUtils.binaryToHexString(bytes));
//            Logger.d("test-remind", "send orderdata is " + NumberUtils.binaryToHexString(bytes));
//        }
//    }
//
//
//    private byte[] clearARemindNoteOrder() {
//        byte[] bytes = null;
//        deviceType = (String) ConfigHelper.getSharePref(this,
//                PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                PublicData.CURRENT_BIND_DEVICE_ITEM, ConfigHelper.DATA_STRING);
//        Logger.i(TAG, "==>>clearARemindNoteOrder: deviceType " + deviceType);
//        if (deviceType.equals(PublicData.L28T) || deviceType.equals(PublicData.W01) || deviceType.equals(PublicData.L39)) {
//
//            bytes = new byte[6];
//            bytes[0] = 0x6E;
//            bytes[1] = 0x01;
//            bytes[2] = 0x09;
//
//            bytes[3] = NumberUtils.intToByteArray(del_RemindData.remind_time_hours)[3];
//
//            bytes[4] = NumberUtils.intToByteArray(del_RemindData.remind_time_minutes)[3];
//
//            bytes[5] = (byte) 0x8F;
//        } else {
//            bytes = new byte[29];
//            bytes[0] = 0x6E;
//            bytes[1] = 0x01;
//            bytes[2] = (byte) 0x05;
//            bytes[3] = (byte) 0x02;
//            bytes[4] = NumberUtils.intToByteArray(del_RemindData.remind_type)[3];
//            bytes[5] = NumberUtils.intToByteArray(del_RemindData.remind_time_hours)[3];
//            bytes[6] = NumberUtils.intToByteArray(del_RemindData.remind_time_minutes)[3];
//            bytes[7] = NumberUtils.binaryStr2Bytes(del_RemindData.remind_week)[0];
//            for (int i = 0; i < 21; i++)
//                bytes[8 + i] = 0;
//            bytes[28] = (byte) 0x8F;
//        }
//        return bytes;
//    }
//
//
//    void DelDeviceRemind()
//
//    {
//
//        Needresponse = true;
//        Logger.d(TAG, "now delete REminds.. connect:" + mDeviceAddress + "****" + del_RemindData.remind_set_ok + "----" + mData.remind_set_ok);
//        Logger.d(TAG, "now delete REminds.. connect:" + isModify);
//
//        orderType = 1;
//        retValue = 0;
//        if (deviceType.equals(PublicData.L38I) && isModify) {
//            if (mData.remind_set_ok == 1) {
//                delNewRemindNoteOrder(mData);
//            } else {
////                addNewRemindNoteOrder(cur_RemindData);
//                dbService.deleteRemindNoteByID(del_RemindData.remind_id);
//                new Thread(ModRemindRunnable).start();
//            }
//
//            ShowProgressDiag(getString(R.string.syndata));
//        } else {
//            connectBluetooth();
//        }
//    }
//
//    private void delNewRemindNoteOrder(RemindNotesData mData1) {
//        if (mData1 != null) {
//            nowType = 0;
//            byte remindType = 0;
//            byte remindHour = 0;
//            byte remindMin = 0;
//            byte remindCycle = 0;
//            if (mData1 != null) {
//
//                // 提醒类型	1：运动；2：睡觉；3：吃饭；4：吃药；5:喝水；6:自定义；
//                if (mData1.remind_type == 1)
//                    remindType = 0x05;//02
//                else if (mData1.remind_type == 2)
//                    remindType = 0x03;//03
//
//                else if (mData1.remind_type == 3)
//                    remindType = 0x00;//00
//
//                else if (mData1.remind_type == 4)
//                    remindType = 0x01;//01
//
//                else if (mData1.remind_type == 5) {
//                    remindType = 0x04;//02
//
//                } else if (mData1.remind_type == 6) {
//                    remindType = 0x07;//07
//                } else
//                    remindType = (byte) 0xFF; // 0B11111111
//                Logger.i("test-remin", " remind_time_hours" + mData1.remind_time_hours);
//                Logger.i("test-remin", " remind_time_minutes" + mData1.remind_time_minutes);
//                Logger.i("test-remin", " remind_week" + mData1.remind_week);
//                // 小时
//                remindHour = NumberUtils.intToByteArray(mData1.remind_time_hours)[3];
//
//                // 分钟
//                remindMin = NumberUtils.intToByteArray(mData1.remind_time_minutes)[3];
//                // 提醒星期几:01111111
//                remindCycle = NumberUtils.binaryStr2Bytes(mData1.remind_week)[0];
//
//            }
//            byte remindSwitchStatus = (byte) 0x01;
//            Logger.i("test-remin", " remindType" + remindType);
//            Logger.i("test-remin", " remindHour" + remindHour);
//            Logger.i("test-remin", " remindMin" + mData1.remind_week);
//            BluetoothUtil.getInstance().send(new RemindSetting(this, 6, (byte) 0x02, remindType, remindHour, remindMin, remindCycle, (byte) 0x00, mData1.remind_text.getBytes(),
//                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00));
//        }
//    }
//
//    private int nowType = 0;
//
//    private void addNewRemindNoteOrder(RemindNotesData mData) {
//        Logger.i("test-remin", " addNewRemindNoteOrder1111111");
//        nowType = 2;
//        byte[] bytes = new byte[0];
//        byte type = 0;
//        byte byte4 = 0;
//        byte byte5 = 0;
//        byte byte6 = 0;
//        if (mData != null) {
//
//            // 提醒类型	1：运动；2：睡觉；3：吃饭；4：吃药；5:喝水；6:自定义；
//            if (mData.remind_type == 1)
//                type = 0x05;//02
//            else if (mData.remind_type == 2)
//                type = 0x03;//03
//
//            else if (mData.remind_type == 3)
//                type = 0x00;//00
//
//            else if (mData.remind_type == 4)
//                type = 0x01;//01
//
//            else if (mData.remind_type == 5) {
//                type = 0x04;//02
//
//            } else if (mData.remind_type == 6) {
//                type = 0x07;//07
//            } else
//                type = (byte) 0xFF; // 0B11111111
//            Logger.i("test-remin", " remind_time_hours" + mData.remind_time_hours);
//            Logger.i("test-remin", " remind_time_minutes" + mData.remind_time_minutes);
//            Logger.i("test-remin", " remind_week" + mData.remind_week);
//            // 小时
//            byte4 = NumberUtils.intToByteArray(mData.remind_time_hours)[3];
//
//            // 分钟
//            byte5 = NumberUtils.intToByteArray(mData.remind_time_minutes)[3];
//            // 提醒星期几:01111111
//            byte6 = NumberUtils.binaryStr2Bytes(mData.remind_week)[0];
//
//        }
//        int len = 6;
//        if (type == 0x07) {
//            len = 6 + mData.remind_text.getBytes().length;
//        } else {
//            len = 6;
//        }
//        BluetoothUtil.getInstance().send(new RemindSetting(this, len, (byte) 0x00, type, byte4, byte5, byte6, (byte) 0x01, mData.remind_text.getBytes(), (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01));
////    	Logger.d(TAG, "设置的current_steps1=" + current_steps1 + " current_steps2=" + current_steps2 + " current_steps3=" + current_steps3 + " current_steps4=" + current_steps4);
//    }
//
//    private void connectBluetooth() {
//        if (mBluetoothLeService != null) {
//            //	IsSynsuccessed = false;
//            //AlreadyReturn = false;
//            isNeedConnect = true;
//            mDeviceAddress = (String) ConfigHelper.getSharePref(this, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.MAC_KEY, ConfigHelper
//                    .DATA_STRING);
//            ;
//
//
//            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
//            if (mBluetoothAdapter == null) {
//                Toast.makeText(
//                        this,
//                        R.string.error_bluetooth_not_supported,
//                        Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (!mBluetoothAdapter.isEnabled()) {
//
//
//                Intent enableBtIntent = new Intent(
//                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, 0);
//
//            } else {
//
//
//                final boolean result = mBluetoothLeService.connect(mDeviceAddress, RemindActivity.this, true);
//                Logger.d(TAG, "mBluetoothLeService.connect(mDeviceAddress)--》clicked--》" + result);
//
//                RandCode = rand.nextInt(10000);
//                ShowProgressDiag(getString(R.string.syndata));
//                Message msg = Message.obtain();
//                msg.arg2 = RandCode;
//                msg.what = REMIND_DEL_TIMEOUT;
//                // IsTimeOut = false;
//                mHandler.sendMessageDelayed(msg, MAXTIMEOUT + 5000);
//
//
//            }
//
//
//        }
//
//
//    }
//
//
//    // Tim 广播,接收service发过来的广播
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//
////            Logger.e(TAG, "Needresponse=" + Needresponse + "/isInAutoSynMode=" );
////            if (!Needresponse || SynBlueToothDataService.isInAutoSynMode) {
////                Logger.d(TAG, "not allow respone..");
////                return;
////            }
//
//            final String action = intent.getAction();
//            Logger.i(TAG, "BroadcastReceiver.action=" + action);
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//
//
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                if (isNeedConnect) {
//                    isNeedConnect = false;
//
//                    sendOrderToDevice(orderType);
//
//                }
//            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//
//                byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                Logger.d(TAG, "获取到的数据：" + NumberUtils.bytes2HexString(bytes));
//
//                parseBytesArray(bytes);
//
//            }
//        }
//
//
//    };
//
//
//    // Tim 解析获取到的byte[]
//    private void parseBytesArray(byte[] byteArray) {
//        // 清除提醒指令：// 0x21;  {0x6E, 0x01, 0x40, 0x01, (byte) 0x8F}; ，执行成功：返回:6E0101-21-00-8F,
//
//        Logger.i("test-remind", "----" + NumberUtils.binaryToHexString(byteArray));
//
//        if (byteArray.length == 6 && byteArray[0] == 0x6e && (byteArray[3] == 0x15) && byteArray[2] == 0x01) // 返回响应消息
//        {
//
//            if (retValue == 0) {
//                retValue = 1;
//
//                sendOrderToDevice(orderType);
//
//            }
//        } else if (byteArray.length == 6 && byteArray[0] == 0x6e && byteArray[1] == 0x01 && (byteArray[3] == 0x09 || byteArray[3] == 0x05) &&
//                byteArray[5] == (byte) 0x8f)    // 09 to 05
//        {
//
//            //删除单条
//
//            orderType = 2;
//            new Thread(ModRemindRunnable).start();
//
//        }
//    }
//
//
//    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
//                int level = intent.getIntExtra("level", 0); //获得当前电量
//                int scale = intent.getIntExtra("scale", 100); //获得总电量
//
//                int percent = level * 100 / scale;
//                Logger.d(TAG, "===电量：" + percent + "%");
//                top_title_battery.setText(percent + "%");
//
//                SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
//                String date = sDateFormat.format(new Date());
//                top_title_time.setText(date);
//            }
//        }
//
//    };
//
//    // 返回按钮响应事件：
//    public void btn_return_clicked(View view) {
//        Intent it = new Intent();
//        it.putExtra(REMIND_EXTRA, REMIND_ACTIVITY_NOTHING);
//        setResult(REMIND_ACTIVITY_FINISH, it);  //设置结果码
//        finish();
//    }
//
//    // 保存到数据库按钮响应事件：
//    public void btn_save_db_clicked(View view) {
//
//        Logger.d(TAG, "save_db_clicked..");
//        // 必须有运动类型
//        if (remind_note_type == -1) {
//            Toast.makeText(this, R.string.note_type_no, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // 必须有周几提醒
//        if (remindWeek().equals("0000000")) {
//            Toast.makeText(this, R.string.select_day_no, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 提醒最多只能20条
//        if (dbService.getRemindNotesCount() >= 10) {
//            Toast.makeText(this, R.string.notes_max_20, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        int id = -1;
//
//
//        // 时间方面，必须没有相同的时间提醒，但是修改的提醒可以与自己的时间相同
//        if (mData == null) {
//            id = dbService.findSameTimesID(int_hour_item, int_minute_item, -1);
//            if (id != -1) {
//                Toast.makeText(this, R.string.note_time_same, Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // 如果没有数据，就在重新插入数据：
//            RemindNotesData mRemindNotesData = new RemindNotesData(remind_note_type, remindText, int_hour_item, int_minute_item,
//                    remindWeek(), 0);
//
//
//            dbService.saveRemindNotesData(mRemindNotesData);
//        } else {
//            id = dbService.findSameTimesID(int_hour_item, int_minute_item, mData.remind_id);
//            if (id != -1) {
//                Toast.makeText(this, R.string.note_time_same, Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // 如果是有数据，就在原来基础id的记录上修改
//            RemindNotesData mRemindNotesData = new RemindNotesData(mData.remind_id, remind_note_type, remindText, int_hour_item,
//                    int_minute_item, remindWeek(), 0);
//            dbService.saveRemindNotesData(mRemindNotesData);
//        }
//
//
//        finish();
//    }
//
//
//    // 编辑时间按钮响应事件：
//    public void btn_edit_time_clicked(View view) {
//        if (wheelWindow == null)
//            wheelWindow = new TimeWheelPopupWindow(this, 6, int_hour_item, int_minute_item, scrollListener, clickls);
//
//        //显示窗口 //设置layout在PopupWindow中显示的位置
//        wheelWindow.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//
//    }
//
//
//    int mCurrentHourItem = 0, mCurrentMinItem = 0;
//    OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
//        public void onScrollingStarted(WheelView wheel) {
//
//        }
//
//        public void onScrollingFinished(WheelView wheel) {
//            if (wheel.getId() == 1) { // 小时
////				int_hour_item = wheel.getCurrentItem();
//                mCurrentHourItem = wheel.getCurrentItem();
//            } else if (wheel.getId() == 2) { // 分钟
////				int_minute_item = wheel.getCurrentItem();
//                mCurrentMinItem = wheel.getCurrentItem();
//
//            }
//            //setTextViewTime(int_hour_item, int_minute_item);
//
//        }
//    };
//
//
//    private int CoverOldRemindToCloudRemind(int oldRemindType) {
//
//        //提醒类型	1：运动；2：睡觉；3：吃饭；4：吃药；5:醒来；6:自定义
//        int ret = 0;
//        switch (oldRemindType) {
//            case 1:
//                ret = 2;
//                break;
//
//            case 2:
//                ret = 3;
//                break;
//
//            case 3:
//
//                ret = 0;
//                break;
//
//            case 4:
//                ret = 1;
//                break;
//            case 5:
//                ret = 5;
//                break;
//
//            case 6:
//                ret = 6;
//                break;
//
//            default:
//                ret = 0;
//                break;
//        }
//
//        return (ret);
//
//    }
//
//
//    public String padStr(String val, String pad, int len) {
//        String str = val;
//        while (str.length() < len)
//            str = pad + str;
//        return str;
//    }
//
//
//    Runnable AddRemindRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            Logger.d(TAG, "--GetAddRemindRunable---");
//
//            PropertiesUtil pu = new PropertiesUtil();
//            pu.initResRawPropFile(RemindActivity.this, R.raw.server);
//
//            Properties props = pu.getPropsObj();
//            String url = props.getProperty("server.remind.add", "http://lecomm.appscomm.cn/sport/api/add_remind_info");
//
//            Logger.d(TAG, "请求地址：" + url);
//
//            ///ring method = "get";
//            //  String url2 ="";
//            String uid = (String) ConfigHelper.getCommonSharePref(RemindActivity.this,
//                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                    PublicData.CURRENT_USERID_ITEM_KEY, 1);
//
//
//            String watchId = ConfigHelper.GetBind_DN(RemindActivity.this);
//
//
//            if (cur_RemindData == null) return;
//
//            //	RemindNotesData md1 = null ;//mRemindNotesDataList.get(mCurListIndex);
//
//            String repeat = new StringBuffer(cur_RemindData.remind_week).reverse().toString();
//            String time = cur_RemindData.remind_time_hours + ":" + String.format("%02d", cur_RemindData.remind_time_minutes);
//            String type = padStr(Integer.toBinaryString(CoverOldRemindToCloudRemind(cur_RemindData.remind_type)), "0", 8);
//            String re_text = cur_RemindData.remind_text;
//            String detail = re_text;
//            String status = "0";// (md1.remind_set_ok ==1)?"1":"0" ;
//            String doType = "1";
//            String re_id = "0";
//
//            String params = "userId=" + uid + "&watchId=" + watchId + "&repeat=" + repeat + "&time=" + time
//                    + "&type=" + type + "&text=" + re_text + "&detail=" + detail + "&status=" + status
//                    + "&doType=" + doType + "&id=" + re_id;
//
//            Logger.d(TAG, "请求地址：" + url + "params" + params);
//            //url = url+'/';
//
//            int respondStatus = mhttpUtil.httpReq("post", url, params);
//            String respondBody = mhttpUtil.httpResponseResult;
//
//
//            HttpResDataService httpResDataService = new HttpResDataService(
//                    RemindActivity.this);
//
//            int i = httpResDataService.commonParse(respondStatus, respondBody,
//                    "9"); // 9是 add reminds.
//
//            Logger.i(TAG, "------------->>>:" + i);
//
//            if (i >= 90000) { //ID = i -90000
//
//                Message msg = Message.obtain();
//                msg.arg1 = i - 90000;
//
//                msg.what = REMIND_ADD_SUCCESS;
//
//                mHandler.sendMessage(msg);
//
//
//            } else {
//
//                Message msg = Message.obtain();
//
//                msg.what = REMIND_ADD_FAIL;
//
//                mHandler.sendMessage(msg);
//
//            }
//
//        }
//    };
//
//
//    Runnable ModRemindRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            Logger.d(TAG, "--GetAddRemindRunable---");
//
//            PropertiesUtil pu = new PropertiesUtil();
//            pu.initResRawPropFile(RemindActivity.this, R.raw.server);
//
//            Properties props = pu.getPropsObj();
//            String url = props.getProperty("server.remind.add", "http://lecomm.appscomm.cn/sport/api/add_remind_info");
//
//            Logger.d(TAG, "请求地址：" + url);
//
//            ///ring method = "get";
//            //  String url2 ="";
//            String uid = (String) ConfigHelper.getCommonSharePref(RemindActivity.this,
//                    PublicData.SHARED_PRE_SAVE_FILE_NAME,
//                    PublicData.CURRENT_USERID_ITEM_KEY, 1);
//
//
//            String watchId = ConfigHelper.GetBind_DN(RemindActivity.this);
//
//
//            if (cur_RemindData == null) return;
//
//            //	RemindNotesData md1 = null ;//mRemindNotesDataList.get(mCurListIndex);
//
//            String repeat = new StringBuffer(cur_RemindData.remind_week).reverse().toString();
//            String time = cur_RemindData.remind_time_hours + ":" + String.format("%02d", cur_RemindData.remind_time_minutes);
//            String type = padStr(Integer.toBinaryString(CoverOldRemindToCloudRemind(cur_RemindData.remind_type)), "0", 8);
//            String re_text = cur_RemindData.remind_text;
//            String detail = re_text;
//            String status = "0";// (md1.remind_set_ok ==1)?"1":"0" ;
//            String doType = "2"; //modify
//            String re_id = "" + cur_RemindData.remind_id;
//
//            String params = "userId=" + uid + "&watchId=" + watchId + "&repeat=" + repeat + "&time=" + time
//                    + "&type=" + type + "&text=" + re_text + "&detail=" + detail + "&status=" + status
//                    + "&doType=" + doType + "&id=" + re_id;
//
//            Logger.d(TAG, "请求地址：" + url + "params" + params);
//            //url = url+'/';
//
//            int respondStatus = mhttpUtil.httpReq("post", url, params);
//            String respondBody = mhttpUtil.httpResponseResult;
//
//
//            HttpResDataService httpResDataService = new HttpResDataService(
//                    RemindActivity.this);
//
//            int i = httpResDataService.commonParse(respondStatus, respondBody,
//                    "19"); // 19是 mod reminds.
//
//            Logger.i(TAG, "------------->>>:" + i);
//
//            if (i == 0) { //ID = i -90000
//
//                Message msg = Message.obtain();
//
//
//                msg.what = REMIND_MOD_SUCCESS;
//
//                mHandler.sendMessage(msg);
//
//
//            } else {
//
//                Message msg = Message.obtain();
//
//                msg.what = REMIND_MOD_FAIL;
//
//                mHandler.sendMessage(msg);
//
//            }
//
//        }
//    };
//
//    class ClickListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View v) {
//            Logger.d(TAG, "View_onClick_clicked../amOrPm=" + amOrPm);
//            switch (v.getId()) {
////                case R.id.btn_save:
//                case R.id.btn_right:
//                    //关闭自定义的软键盘
//                    View view = getWindow().peekDecorView();
//                    if (view != null) {
//                        InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                    }
//                    if ("2".equals(amOrPm) && mData == null) {
////                        if (int_hour_item < 12) {
////                            int_hour_item = int_hour_item + 12;
////                        }
//                        int_hour_item = int_hour_item + 12 == 24 ? 0 : int_hour_item + 12;
//                    } else {
////                        if (int_hour_item > 12) {
////                            int_hour_item = int_hour_item - 12;
////                        }
////                        int_hour_item = int_hour_item - 12;
//                    }
//                    Logger.e(TAG, "int_hour_item=" + int_hour_item);
//
//                    // 必须有运动类型
//                    if (remind_note_type == -1) {
//                        Toast.makeText(RemindActivity.this, R.string.note_type_no, Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    // 必须有周几提醒
//                    if (remindWeek().equals("0000000")) {
//                        Toast.makeText(RemindActivity.this, R.string.select_day_no, Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    // 提醒最多只能10条
//                    if (dbService.getRemindNotesCount() >= 10 && mData == null) {
//                        Toast.makeText(RemindActivity.this, R.string.notes_max_20, Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    int id = -1;
//                    // 时间方面，必须没有相同的时间提醒，但是修改的提醒可以与自己的时间相同
//                    if (mData == null) {
//                        Logger.e("test-remind", "+++++++++++++000");
//                        id = dbService.findSameTimesID(int_hour_item, int_minute_item, -1);
//                        if (id != -1) {
//                            Toast.makeText(RemindActivity.this, R.string.note_time_same, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        // 如果没有数据，就在重新插入数据：
//
//
//                        if (!mhttpUtil.isNetworkConnected()) {
//
//                            AlertDialog.Builder builder = new AlertDialog.Builder(RemindActivity.this);
//                            builder.setTitle(R.string.os_hint_title);
//                            builder.setMessage(R.string.NetWorkError);
//                            // builder.setMessage(getText(R.string.dialog_message).toString());
//                            builder.setPositiveButton(getString(android.R.string.ok), null);
//
//                            builder.show();
//                            return;
//                        }
//
//
////                        cur_RemindData = new RemindNotesData(remind_note_type, edittext.getText().toString(), int_hour_item, int_minute_item,
////                                remindWeek(), 0);
//                        if (remind_note_type == 6) {
//                            byte[] res = remindText.getBytes();
//                            if (res.length > 23) {
//                                Toast.makeText(RemindActivity.this, "Prompt too much content, please try again.", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                        cur_RemindData = new RemindNotesData(remind_note_type, remindText, int_hour_item, int_minute_item,
//                                remindWeek(), 0);
//
//                        //Save to cloud First, then save local.db
//
//                        ShowProgressDiag(getString(R.string.loading));
//                        IsAlreadyReturn = false;
//                        Message msg = Message.obtain();
//                        msg.what = REMIND_ADD_TIMEOUT;
//                        mHandler.sendMessageDelayed(msg, MAXTIMEOUT);
//                        Logger.e("test-remind", "+++++++++++++111");
//                        new Thread(AddRemindRunnable).start();
//
//
//                    } else {
//                        Logger.e("test-remind", "+++++++++++++222");
//                        Logger.e("test-remind", "+++++++++++++222" + int_minute_item);
//                        //修改
//                        id = dbService.findSameTimesID(int_hour_item, int_minute_item, mData.remind_id);
//                        if (id != -1) {
//                            Toast.makeText(RemindActivity.this, R.string.note_time_same, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        // 如果是有数据，就在原来基础id的记录上修改
//                        Logger.i("test-remind", "---旧的分钟:" + mData.remind_time_minutes + " 新的分钟:" + int_minute_item);
//
//
//                        cur_RemindData = new RemindNotesData(mData.remind_id, remind_note_type, remindText, int_hour_item,
//                                int_minute_item, remindWeek(), 0);
//                        del_RemindData = new RemindNotesData(mData.remind_id, mData.remind_type, remindText, mData
//                                .remind_time_hours, mData.remind_time_minutes, remindWeek(), mData.remind_set_ok);
//                        if (deviceType.equals(PublicData.L38I)) {
//                            del_RemindData = new RemindNotesData(mData.remind_id, mData.remind_type, remindText, mData
//                                    .remind_time_hours, mData.remind_time_minutes, mData.remind_week, mData.remind_set_ok);
//                        }
//                        //保存原来要删除的数据
//
//
//                        DelDeviceRemind();
//
//				/*	ShowProgressDiag();
//                     IsAlreadyReturn = false;
//					 Message msg = Message.obtain();
//					 msg.what = REMIND_MOD_TIMEOUT;
//					 mHandler.sendMessageDelayed(msg, MAXTIMEOUT);
//
//					 new Thread(ModRemindRunnable).start();*/
//
//
//			/*		RemindNotesData mRemindNotesData = new RemindNotesData(mData.remind_id, remind_note_type, edittext.getText().toString(),
//            int_hour_item, int_minute_item, remindWeek(), 0);
//					dbService.saveRemindNotesData(mRemindNotesData);
//					Intent it = new Intent();
//					it.putExtra(REMIND_EXTRA,REMIND_ACTIVITY_MODIFIY);
//					it.putExtra("OLDDATA", mData);
//					setResult(REMIND_ACTIVITY_FINISH,it);  //设置结果码
//					finish();*/
//                    }
//
//                    break;
//
//                case R.id.textview_time:
//                    Logger.e(TAG, "int_hour_item=" + int_hour_item);
//                    if (int_hour_item > 12) {
//                        int_hour_item = int_hour_item - 12;
//                    }
//                    Logger.e(TAG, "int_hour_item=" + int_hour_item);
//
//                    if (wheelWindow == null)
//                        wheelWindow = new TimeWheelPopupWindow(RemindActivity.this, 6, int_hour_item, int_minute_item, scrollListener, clickls);
//
//                    //显示窗口 //设置layout在PopupWindow中显示的位置
//                    wheelWindow.showAtLocation(RemindActivity.this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//
//                    break;
//
//                default:
//                    break;
//            }
//
//        }
//
//    }
//
//
//}
//
