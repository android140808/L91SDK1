package apps.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.Locale;

import cn.appscomm.pedometer.activity.TurnOnBluetoothActivity;
import cn.appscomm.pedometer.application.GlobalApp;
import cn.appscomm.pedometer.model.SportDataSendInfo;
import cn.l11.appscomm.pedometer.activity.R;

public class PublicData {

    public static final String L42 = "VIBE";
    private static final String TAG = "PublicData";
    public static float TEXT_SIZE = 20;
    public static int Remin_size = 20;
    public static float TEXT_SIZE_M = 30;
    public static float TEXT_SIZE_Y = 10;
    public final static String DEVICE_DISPLAY_STEP = "DEVICE_DISPLAY_STEP";

    public final static String IS_UPDATE = "IS_UPDATE";       //是否处于升级状态
    public static String bindDeviceName = "";
    public static String selectDeviceName = "";
    public static String mDeviceName = "";
    //	public static final String L28H = "L28H";
//	public static final String L30D = "C01M";
//	public static final String W01 = "W01";
//	public static final String L28T = "L28T";
    public static final String L28H = "SWIPE";
    public static final String L30D = "TOUCH";
    public static final String W01 = "TIME";
    //	public static final String L28T = "BASIC";
    public static final String L28T = "LITE";
    //	public static final String L39 = "ZeFit2 Pulse";
    public static final String L39 = "LITE WWW";
    public static final String L38I = "B5Ameter";


    public static final String MSGHEAD = "cn.threeplus.appscomm.pedometer.service.";//新协议数据接收
    public static final String IS_SCREEN_ON = "IS_SCREEN_ON";    // summer: add
    public static boolean isScreenOn = true;        // 是否灭屏
    public static boolean isScrollable = true;        // ViewPager是否可滑动
    public static Calendar curShowCal = null;        // 当前详细睡眠界面显示的日期

    public final static String CURRENT_BIND_DEVICE_ITEM = "CURRENT_BIND_DEVICE_ITEM";
    public final static String SLEEP_DAY_AWAKECOUNT = "SLEEP_DAY_AWAKECOUNT";

//    public static boolean isSend=false ;


    // sharePrefe 保存的配置文件名：
    public final static String SHARED_PRE_SAVE_FILE_NAME = "setting";

    public final static String VIBRATION_ITEM = "VIBRATION_ITEM";
    public final static String VIBRATION_SWITCH_ITEM = "VIBRATION_SWITCH_ITEM";

    public final static String NORDIC_VER_MAJOR_ITEM_KEY = "NORDIC_VER_MAJOR_ITEM";         // nordic 主版本
    public final static String NORDIC_VER_MINOR_ITEM_KEY = "NORDIC_VER_MINOR_ITEM";         // nordic 从版本
    public final static String FREESCALE_VER_MAJOR_ITEM_KEY = "FREESCALE_VER_MAJOR_ITEM";   // Freescale 主版本
    public final static String FREESCALE_VER_MINOR_ITEM_KEY = "FREESCALE_VER_MINOR_ITEM";   // Freescale 从版本
    public final static String ADDSLEEP_BEDTIME_DAY = "ADDSLEEP_BEDTIME_DAY";
    public final static String ADDSLEEP_BEDTIME_HOUR = "ADDSLEEP_BEDTIME_HOUR";
    public final static String ADDSLEEP_BEDTIME_MIN = "ADDSLEEP_BEDTIME_MIN";
    public final static String ADDSLEEP_AWAKETIME_DAY = "ADDSLEEP_AWAKETIME_DAY";

    public final static String ADDSLEEP_AWAKETIME_HOUR = "ADDSLEEP_AWAKETIME_HOUR";
    public final static String ADDSLEEP_AWAKETIME_MIN = "ADDSLEEP_AWAKETIME_MIN";

    public final static String SLEEP_ITEM_KEY = "SLEEP_ITEM"; // sharePrefe 保存的睡眠目标时间item：
    public final static String SLEEP_GOAL_KEY = "SLEEP_GOAL"; // sharePrefe 保存的睡眠目标时间：小时
    public static boolean isNewNetworkAPI = true; // 是否网络新接口
    public final static String YEAR_ITEM_KEY = "YEAR_ITEM"; // sharePrefe 保存的人的出生年
    public final static String MONTH_ITEM_KEY = "MONTH_ITEM"; // sharePrefe 保存的人的出生月
    public final static String DAY_ITEM_KEY = "DAY_ITEM"; // sharePrefe 保存的人的出生日
    public static final String FREESCALE_VERSION = "FREESCALE_VERSION"; // freescale版本
    public final static String SEX_ITEM_KEY = "SEX_ITEM"; // sharePrefe 保存的人的性别：0是男，1是女
    public final static String SEX_OLD_ITEM_KEY = "SEX_OLD_ITEM"; // sharePrefe 保存的人的性别：0是男，1是女
    public final static String HEIGHT_ITEM_KEY = "HEIGHT_ITEM"; // sharePrefe 保存的人的身高
    public final static String HEIGHT_OLD_ITEM_KEY = "HEIGHT_OLD_ITEM"; // sharePrefe 保存的人的身高
    public final static String HEIGHT_DEC_ITEM_KEY = "HEIGHT_DEC_ITEM"; // sharePrefe 保存的人的dec身高
    public final static String HEIGHT_UNIT_ITEM_KEY = "HEIGHT_UNIT_ITEM"; // 0 = ft  1 =cm
    public static final String NORDIC_VERSION = "NORDIC_VERSION"; // nordic版本
    public final static String WEIGHT_ITEM_KEY = "WEIGHT_ITEM"; // sharePrefe 保存的人的体重
    public final static String WEIGHT_OLD_ITEM_KEY = "WEIGHT_OLD_ITEM"; // sharePrefe 保存的人的体重
    public final static String OTHER_BINDEMAIL_KEY = "OTHER_BINDEMAIL_ITEM"; // sharePrefe 保存的人的体重

    public final static String LOCAL_SAVE_HEIGHT_CM_KEY = "LOCAL_SAVE_HEIGHT_CM_KEY";//
    public final static String LOCAL_SAVE_WEIGHT_KG_KEY = "LOCAL_SAVE_WEIGHT_KG_KEY";

    public final static String WEIGHT_DEC_ITEM_KEY = "WEIGHT_DEC_ITEM"; // sharePrefe 保存的人的体重
    public final static String WEIGHT_UNIT_ITEM_KEY = "WEIGHT_UNIT_ITEM";

    //因无网络绑定,故需在每次绑定后设置本标志为未清除服务器数据,清除成功后再设本地标志为已清同时再上传相关数据到服务器!!!
    public final static String LOCAL_DEL_SERVER_FLAG = "LOCAL_DEL_SERVER_FLAG";//保存本地是否已删除服务器数据标志  1-已清,0或空-未清

    public final static String REMINDS_AMORPM_ITEM_KEY = "REMINDS_AMORPM_ITEM_KEY"; //1-am; 2-pm
    public final static String CURRENT_NAME_ITEM_KEY = "CURRENT_NAME_ITEM"; // sharePrefe 保存的姓名
    public final static String CURRENT_PASSWORD_ITEM_KEY = "current_password_item_key"; // sharePrefe 保存的密码
    public final static String CURRENT_USERID_ITEM_KEY_OLD = "CURRENT_USERID_ITEM_OLD"; // sharePrefe 保存的userId


    public final static String CURRENT_REGID_ITEM_KEY = "CURRENT_REGID_ITEM"; // sharePrefe 保存的注册返回时的唯一Id
    public final static String CURRENT_USERID_ITEM_KEY = "CURRENT_USERID_ITEM"; // sharePrefe 保存的userId
    public final static String CURRENT_EMAIL_ITEM_KEY = "CURRENT_EMAIL_ITEM"; // sharePrefe 保存的邮箱
    public final static String CURRENT_DDID_ITEM_KEY = "CURRENT_DDID_ITEM_KEY"; // sharePrefe 保存的ddId
    public final static String CURRENT_ICON_PATH_ITEM_KEY = "CURRENT_ICON_PATH_ITEM_KEY"; // sharePrefe 保存的头像地址

    public final static String CURRENT_IMGURL_ITEM_KEY = "CURRENT_IMGURL_ITEM_KEY"; // sharePrefe 保存的邮箱
    public static boolean isMenuFlag = false; // 是否处于菜单界面
    public final static String NOTI_ALLSW_ITEM_KEY = "NOTI_ALLSW_ITEM_KEY"; //通知--所有开关
    public final static String NOTI_CALLSW_ITEM_KEY = "NOTI_CALLSW_ITEM_KEY"; //通知--电话开关
    public final static String NOTI_MISCALLSW_ITEM_KEY = "NOTI_MISCALLSW_ITEM_KEY"; //通知--未接电话开关
    public final static String NOTI_SMSSW_ITEM_KEY = "NOTI_SMSSW_ITEM_KEY";//通知--短信开关
    public final static String NOTI_EMAILSW_ITEM_KEY = "NOTI_EMAILSW_ITEM_KEY";//通知--email开关
    public final static String NOTI_SOCSW_ITEM_KEY = "NOTI_SOCSW_ITEM_KEY";//社交提醒开关
    public final static String NOTI_CALSW_ITEM_KEY = "NOTI_CALSW_ITEM_KEY";//日历开关
    public final static String NOTI_ANTILOST_SW_ITEM_KEY = "NOTI_ANTILOST_SW_ITEM_KEY";

    public final static String DATE_TYPE_ITEM_KEY = "DATE_TYPE_ITEM_KEY";  //保存的3种不同的日期类型
    public final static String CURRENT_VER_MAJOR_ITEM_KEY = "CURRENT_VER_MAJOR_ITEM";       //设备软件主版本
    public final static String CURRENT_VER_MINOR_ITEM_KEY = "CURRENT_VER_MINOR_ITEM";     //从版本
    public final static String CURRENT_VER_EXTRA_ITEM_KEY = "CURRENT_VER_EXTRA_ITEM";     //附版本
    public final static String SOFT_VERSION = "SOFT_VERSION";       //软件版本
    public final static String ALL_VERSION = "ALL_VERSION";       //软件版本
    public final static String FONT_VERSION = "FONT_VERSION";       //软件版本
    public final static String HEART_VERSION = "HEART_VERSION";       //软件版本heartVersion
    public final static String SERVER_VERSION = "SERVER_VERSION";     //服务器版本
    public final static String SERVER_UPDATEURL = "SERVER_UPDATEURL";     //服务器固件url


    public final static String AUTOSLEEP_SW_ITEM_KEY = "AUTOSLEEP_SW_ITEM_KEY";     //自动睡眠开关
    public final static String AUTOSLEEP_BED_TIME_KEY = "AUTOSLEEP_BED_TIME_KEY";     //自动睡眠开始时间
    public final static String AUTOSLEEP_AWAKE_TIME_KEY = "AUTOSLEEP_AWAKE_TIME_KEY";     //自动睡眠结束时间
    public final static String AUTOSLEEP_KEY = "AUTOSLEEP_KEY";                // summer: add

    public final static String HEART_RATE_AUTO_TRACKSW_ITEM_KEY = "HEART_RATE_AUTO_TRACKSW_ITEM_KEY"; //自动检测心率开关
    public final static String HEART_RATE_RANGE_ALERTSW_ITEM_KEY = "HEART_RATE_RANGE_ALERTSW_ITEM_KEY"; //心率报警开关
    public final static String HEART_RATE_FREQUENCY = "HEART_RATE_FREQUENCY"; //自动心率频率
    public final static String HEART_RATE_LOW_LIMIT = "HEART_RATE_LOW_LIMIT"; //最低心率值
    public final static String HEART_RATE_HIGH_LIMIT = "HEART_RATE_HIGH_LIMIT"; //最高心率值

    public final static String BEGIN_SLEEP_TIME_STAMP = "BEGIN_SLEEP_TIME_STAMP";        // summer: add 计时开始时间


    public final static String LAST_SLEEP_TIME_KEY = "LAST_SLEEP_TIME_KEY";     //上次睡眠进入睡眠保存的时间戳
    public static boolean isNeedNetwork = true; // 是否需要同步到服务器

    public final static int STEP_GOALS_DEFAULT = 7000;          // 单位是步
    public final static int DISTANCE_GOALS_DEFAULT = 5;         // 单位是千米
    public final static int CALORIE_GOALS_DEFAULT = 350;        // 单位是千卡
    public final static int SLEEP_GOALS_DEFAULT = 8;            // 单位是小时
    public  static int ACTIVE_MINUTES_GOALS_DEFAULT = 60;  // 单位是小时

    public final static String SERVER_FW_MAJOR_VER = "SERVER_FW_MAJOR_VER";       //设备软件主版本
    public final static String SERVER_FW_MINOR_VER = "SERVER_FW_MINOR_VER";     //从版本

    public final static String ISSHOW_SERVER_FW_UPDATE = "ISSHOW_SERVER_FW_UPDATE";

    public static String CURRENT_BIND_ID_ITEM = "CURRENT_BIND_ID_ITEM"; // sharePrefe 保存的bind ID		// summer: delete final
    public final static String CURRENT_BIND_ID_ITEM_1 = "CURRENT_BIND_ID_ITEM_1"; // sharePrefe 保存的 temp bind ID


    public final static String BEGIN_SYNSPORTDATE = "BEGIN_SYNSPORTDATE"; //服务器同步有效数据的起点区间 ，保存时间是以天为单位= GMT 秒数/3600/24

    public final static String END_SYNSPORTDATE = "END_SYNSPORTDATE"; //服务器同步有效数据的终点区间 ，保存时间是以天为单位= GMT 秒数/3600/24


    public final static String BEGIN_SYNSLEEPDATE = "BEGIN_SYNSLEEPDATE"; //服务器同步有效数据的起点区间 ，保存时间是以天为单位= GMT 秒数/3600/24

    public final static String END_SYNSLEEPDATE = "END_SYNSLEEPDATE"; //服务器同步有效数据的终点区间 ，保存时间是以天为单位= GMT 秒数/3600/24

    public final static String LAST_SYNDATE = "LAST_SYNDATE";

    public final static String TEMP_DAYSTEP = "TEMP_DAYSTEP";
    public final static String TEMP_DAYDIS = "TEMP_DAYDIS";
    public final static String TEMP_DAYCAL = "TEMP_DAYCAL";
    public final static String TEMP_DAYMINS = "TEMP_DAYMINS";
    public final static String TEMP_DAYSLEEP = "TEMP_DAYSLEEP";

    public static int updateStep = 0;                                                       // 0:不升级 1:升级nordic 2:升级freescale 3:都需要升级
    public static String nordicZipName = "";                                                // 升级nordic的名称(zip)
    public static String freescaleBinName = "";                                             // 升级freescale的名称(bin)

    public final static int YEAR_START = 1900; // 出生年的起点是
    public final static int HEIGHT_INT_START = 90; // 人身高的起点是90cm
    public final static int HEIGHT_INT_MAX = 240; //

    public final static int HEIGHT_FT_INT_START = 3; //
    public final static int HEIGHT_FT_INT_MAX = 7; //

    public final static boolean GetData4Local = false;   //是否指定取本地数据
    public final static boolean NotNetCanSyn = false;
    public static boolean IsReSet = false;


    public final static int WEIGHT_START = 30; // 人体重的起点是

    public final static int WEIGHT_MAX = 400; //

    public final static int WEIGHT_lb_START = 70; // 人体重的起点是

    public final static int WEIGHT_lb_MAX = 999; //


    public final static String TB_ITEM1_KEY = "current_togglebutton_time1"; // sharePrefe 保存的9:00 是否设置了运动目标
    public final static String TB_ITEM2_KEY = "current_togglebutton_time2"; // sharePrefe 保存的12:00 是否设置了运动目标
    public final static String TB_ITEM3_KEY = "current_togglebutton_time3"; // sharePrefe 保存的16:00 是否设置了运动目标
    public final static String TB_ITEM4_KEY = "current_togglebutton_time4"; // sharePrefe 保存的20:00 是否设置了运动目标
    public final static int STEPS_START = 1000; // 运动目标设置的起步是1000步
    public final static String STEPS_ITEM1_KEY = "steps_item1"; // sharePrefe 保存的9:00 设置的运动目标是多少步的item：起步1000步--10万步
    public final static String STEPS_ITEM2_KEY = "steps_item2"; // sharePrefe 保存的12:00 设置的运动目标是多少步的item：起步1000步--10万步
    public final static String STEPS_ITEM3_KEY = "steps_item3"; // sharePrefe 保存的16:00 设置的运动目标是多少步的item：起步1000步--10万步
    public final static String STEPS_ITEM4_KEY = "steps_item4"; // sharePrefe 保存的20:00 设置的运动目标是多少步的item：起步1000步--10万步

    //
    public final static String TOTAL_TARGET_STEPS_KEY = "total_target_steps_key"; // 运动目标值
    public final static String TOTAL_TARGET_DISTANCE_KEY = "total_target_distance_key"; // 距离目标值
    public final static String TOTAL_TARGET_ACTIVE_MINUTES_KEY = "total_target_active_minutes_key"; // 运动分钟目标值
    public final static String TOTAL_TARGET_CALORIES_KEY = "total_target_calories_key"; // 卡路里目标值
    public final static String TOTAL_TARGET_SLEEP_KEY = "total_target_sleep_key"; // 睡眠目标值
//	public final static String TOTAL_SLEEP_AWAKETIMES_KEY = "total_sleep_awaketimes_key"; // 睡眠清醒次数

    public final static String COUNTRY_CODE_KEY = "country_code_key"; // 国家position序号
    public final static String IMG_FACE_KEY = "img_face_key";  //头像
    public final static String INFO_HEIGHT = "info_height"; // 身高
    public final static String INFO_WEIGHT = "info_weight"; // 体重
    public final static String LENGTH_UNIT_KEY = "length_unit_key"; // 长度单位
    public final static String WEIGHT_UNIT_KEY = "weight_unit_key"; // 体重单位
    public final static String TIME_FORMAT_KEY = "time_format_key"; // 时间格式

    public final static String INACTIVITY_ON_NEW_KEY = "inactivity_on_new_key"; // 静坐提醒的开关状态
    public final static String INACTIVITY_TIME_NEW_KEY = "inactivity_time_new_key"; // 静坐提醒的时间
    public final static String INACTIVITY_START_NEW_KEY = "inactivity_start_new_key"; // 静坐提醒的开始时间
    public final static String INACTIVITY_END_NEW_KEY = "inactivity_end_new_key"; // 静坐提醒的关闭时间
    public final static String INACTIVITY_STEPS_NEW_KEY = "inactivity_steps_new_key"; // 静坐提醒的步数
    public final static String INACTIVITY_WEEK_KEY = "inactivity_week_key"; // 静坐提醒的周期


    public final static String UNIT_KEY = "UNIT_KEY";                // 单位设置 0：公制，1：英制
    public final static String DATE_FORMAT = "DATE_FORMAT";            // 日期格式
    public final static String TIME_FORMAT = "TIME_FORMAT";            // 时间格式
    public final static String BRIGHTNESS_KEY = "BRIGHTNESS_KEY";    // 亮度设置
    public final static String RINGMODE_KEY = "RINGMODE_KEY";    // 响铃设置
    public final static String VOLUE_KEY = "VOLUE_KEY";    // 音量设置
    public final static String TIME_FORMAT_L39 = "TIME_FORMAT_L39";            // L39时间格式
    public final static String TIME_FORMAT_L38I = "TIME_FORMAT_L38I";            // L38i时间格式

    public final static String IS_CENT_KILOMETERS_KEY = "is_cent_kilometers_key"; // 是否公里
    public final static String IS_FEETMILES_KEY = "is_feetmiles_key"; // 是否英里

    public final static String IS_KILOGRAMS_KEY = "is_kilograms_key"; // 是否千克
    public final static String IS_POUNDS_KEY = "is_pounds_key"; // 是否英磅

    public final static String COMMON_SHOW_STEPS_ITEM1_KEY = "1101"; // 天
    public final static String COMMON_SHOW_STEPS_ITEM2_KEY = "1102"; // 周
    public final static String COMMON_SHOW_STEPS_ITEM3_KEY = "1103"; // 月

    public final static String COMMON_SHOW_DISTANCE_ITEM1_KEY = "2201";
    public final static String COMMON_SHOW_DISTANCE_ITEM2_KEY = "2202";
    public final static String COMMON_SHOW_DISTANCE_ITEM3_KEY = "2203";

    public final static String COMMON_SHOW_CALORIES_ITEM1_KEY = "3301";
    public final static String COMMON_SHOW_CALORIES_ITEM2_KEY = "3302";
    public final static String COMMON_SHOW_CALORIES_ITEM3_KEY = "3303";

    public final static String COMMON_SHOW_SLEEP_ITEM1_KEY = "4401";
    public final static String COMMON_SHOW_SLEEP_ITEM1_01_KEY = "440101";
    public final static String COMMON_SHOW_SLEEP_ITEM2_KEY = "4402";
    public final static String COMMON_SHOW_SLEEP_ITEM3_KEY = "4403";
    public final static String COMMON_ADD_SLEEP_ITEM_KEY = "4404";

    public final static String SETTINGS_ITEM_KEY = "settings_item_key";

    public final static String SETTINGS_MY_PROFILE_ITEM_KEY = "settings_my_profile_item_key";
    public final static String SETTINGS_ADVANCED_SETTINGS_ITEM_KEY = "settings_advanced_settings_item_key";
    public final static String SETTINGS_NOTIFI_ITEM_KEY = "settings_notifi_item_key";

    public final static String SETTINGS_HELP_ITEM_KEY = "settings_help_item_key";
    public final static String SETTINGS_ABOUT_US_ITEM_KEY = "settings_about_us_item_key";

    public final static String TOP_HINT_LASTSYNCED_DATE_KEY = "top_hint_lastsynced_date_key";
    public final static String TOP_HINT_LASTSYNCED_TIME_KEY = "top_hint_lastsynced_time_key";
    public final static String TOP_HINT_BATTERY_KEY = "top_hint_battery_key";
    public final static String LASTSYNCED_TOTALDATA_DATE_KEY = "lastsynced_total_date_key";
    public final static String LOGIN_USERNAME_KEY = "login_username_key";

    public final static String LOGOUT_KEY = "logout_key";
    public final static String LOGIN_PASSWORD_KEY = "login_password_key";

    public final static String ACCESS_TOKEN_KEY = "access_token_key";

    public final static String CUR_STEPS_TOTAL = "cur_steps_total";         //汇总步数
    public final static String CUR_CALORIES_TOTAL = "cur_calories_total";   //汇总卡路里
    public final static String CUR_DIS_TOTAL = "cur_dis_total";             //汇总距离
    public final static String CUR_SPORTTIME_TOTAL = "cur_sporttime_total"; //汇总运动时长

    public final static boolean isUserCurData = false; // 是否使用本地汇总
    public static boolean isWrite = false;


    public final static String BRIGHTNESS_ITEM_KEY = "BRIGHTNESS_ITEM_KEY";  //亮度设置
    public final static String SLEEP_BEDTIME_H_KEY = "SLEEP_BEDTIMEH_KEY";  //睡觉时间 hous
    public final static String SLEEP_BEDTIME_M_KEY = "SLEEP_BEDTIMEM_KEY";  //睡觉时间 min
    public final static String SLEEP_AWAKETIME_H_KEY = "SLEEP_AWAKETIMEH_KEY";  //起床时间 housr
    public final static String SLEEP_AWAKETIME_M_KEY = "SLEEP_AWAKETIMEM_KEY";  //起床时间 housr


    public final static String INACTIVITY_SW_KEY = "INACTIVITY_SW_KEY";
    public final static String INACTIVITY_INTERVAL_KEY = "INACTIVITY_INTERVAL_KEY";
    public final static String INACTIVITY_START_KEY = "INACTIVITY_START_KEY";
    public final static String INACTIVITY_END_KEY = "INACTIVITY_END_KEY";
    public final static String INACTIVITY_WEEKS_KEY = "INACTIVITY_WEEKS_KEY";

    public final static String ALWAYS_ON_KEY = "ALWAYS_ON_KEY";

    public final static String IS_CALL_PUSH = "is_call_push";
    public final static String IS_SMS_PUSH = "is_sms_push";


    public static SportDataSendInfo dataSendedInfo = new SportDataSendInfo();  //数据上传的信息统计

    public static boolean isSynningSportData = false; //是否在同步数据，防止消息通知时中断同步
    public final static boolean isUserCacheData = true;


    public static Context appContext2 = null;

    // 当前语言
    public static String currentLang;
    public static String[] english_monthL = new String[]{
            "January", "February", "March",
            "April", "May", "June", "July",
            "August", "September",
            "October", "November", "December"
    };
    // 简、英
    public static final String GB = "gb";
    public static final String ENG = "eng";

    public static final String MAC_KEY = "MAC_KEY";
    public static final String DEVICE_NAME = "DEVICE_NAME";        // summer: add deviceName

    public static boolean BindingPedometer = false;

    //public static ArrayList<Integer> CloudRemindListID;


    public static int heightVal_int = 0;    //身高整数
    public static int heightVal_dec = 0;   //小数

    //FIXME
    public static int heightVal_unit = 0;   //单位
    public static int weightVal_unit = 0;   //单位
    public static int isUnit = 1;   //单位用于单位设置界面


    public static long synSport_date_begin = 0;
    public static long synSport_date_end = 0;


    public static long synSleep_date_begin = 0;
    public static long synSleep_date_end = 0;

    public static int weightVal_dec = 0;   //小数
    public static int weightVal_int = 0;    //weight整数


    public static int time_H = 0;
    public static int time_M = 0;


    public static String[] arrHeight_Int;   //高度整数值 cm 下拉列表
    public static String[] arrHeightFt_Int;  //高度整数值  ft 下拉列表
    public static String[] arrWeight_Int;   //体重整数值 cm  下拉列表
    public static String[] arrWeightlbs_Int;  //体重整数值  lbs 下拉列表


    //height decimal data, /
    public static String[] arrHeight_Dec = new String[]{".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};  //10进制的小数
    public static String[] arrHeightFt_Dec = new String[]{"0\"", "1\"", "2\"", "3\"", "4\"", "5\"", "6\"", "7\"", "8\"", "9\"", "10\"", "11\""}; //英寸单位 12进制

    public static String[] arrWeight_Dec = new String[]{".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};  //10进制的小数


    public static String[] arrgender = new String[]{"Male", "Female"};         //gender 0: male 1: female
    public static String[] arrHeightUnit = new String[]{"cm", "ft in"};
    public static String[] arrWeightUnit = new String[]{"kg", "lbs"};

    public static String[] arrFrequency = new String[]{"5", "15", "30", "45", "60"};         // frequency
    public static String[] arrLowLimit = new String[]{"30", "40", "50", "60", "70", "80"};         // low limit
    public static String[] arrHightLimit = new String[]{"90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220"};         // high limit
    public static String[] arrAlertInterval = new String[]{"15min", "30min", "45min", "1h", "1h15min", "1h30min", "1h45min", "2h", "2h15min"};         // frequency

    public static int trackAlertIntervalIndex = 1;
    public static int trackFrequencyIndex = 1;
    public static int cloud_goal_step = 0;
    public static int cloud_goal_dis = 0;
    public static int cloud_goal_sleep = 0;
    public static int cloud_goal_cal = 0;
    public static int cloud_goal_activity = 0;


    public static int upload_data_now = 0;//serverNordicVersion

    public static String serverNordicVersion = "0.0";
    public static String serverResMapVersion = "0.0";
    public static String serverHeartrateVersion = "0.0";

    public static String softVersion = "";
    public static String resMapVersion = "";
    public static String heartrateVersion = "";

    public static boolean nordicUpVersion = false;
    public static boolean resMapUpVersion = false;
    public static boolean heartrateUpVersion = false;

    public static String SAVE_IMG_PATH = "";  //图片文件保存的目录

    public static boolean showDebugToast = false;


    public static boolean gUseAnimation = true;  //是否使用动画
    public static boolean mIsUpdate = false;//是否正在升级
    public static boolean mIsL38Update = false;//是否正在升级

    public static int minHeartRate = 0;

    public static int getMinHeartRate() {
        return minHeartRate;
    }

    public static void setMinHeartRate(int minHeartRate) {
        PublicData.minHeartRate = minHeartRate;
    }

    public static int getMaxHeartRate() {
        return maxHeartRate;
    }

    public static void setMaxHeartRate(int maxHeartRate) {
        PublicData.maxHeartRate = maxHeartRate;
    }

    public static int maxHeartRate = 0;

//初始化长度和重量单位，提供下拉列表选择


    public static void InitDragListData(Context context) {
        Resources res = context.getResources();
        SAVE_IMG_PATH = context.getFilesDir().toString();
        arrgender = new String[2];
        arrgender[0] = res.getString(R.string.reg_male);
        arrgender[1] = res.getString(R.string.reg_female);

        arrHeight_Int = new String[PublicData.HEIGHT_INT_MAX - PublicData.HEIGHT_INT_START + 1];
        for (int i = 0; i < arrHeight_Int.length; i++) {
            arrHeight_Int[i] = PublicData.HEIGHT_INT_START + i + "";// + " cm";

        }


        arrHeightFt_Int = new String[PublicData.HEIGHT_FT_INT_MAX - PublicData.HEIGHT_FT_INT_START + 1];
        for (int i = 0; i < arrHeightFt_Int.length; i++) {
            arrHeightFt_Int[i] = PublicData.HEIGHT_FT_INT_START + i + "'";// + " cm";

        }


        arrWeightlbs_Int = new String[PublicData.WEIGHT_lb_MAX - PublicData.WEIGHT_lb_START + 1];// 14--313
        for (int i = 0; i < arrWeightlbs_Int.length; i++) {
            arrWeightlbs_Int[i] = PublicData.WEIGHT_lb_START + i + "";// + " lbs";

        }


        arrWeight_Int = new String[PublicData.WEIGHT_MAX - PublicData.WEIGHT_START + 1];// 14--313
        for (int i = 0; i < arrWeight_Int.length; i++) {
            arrWeight_Int[i] = PublicData.WEIGHT_START + i + "";// + " kg";

        }

        //  context.getResources().getString(id)

    }

    //xxl获取当前系统语言
    public static void getCurSystemLang(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        currentLang = "";
        if (language.endsWith("zh") || language.endsWith("tw") || language.endsWith("ja"))
            currentLang = "zh";
        else if (language.endsWith("en"))
            currentLang = "en";
    }

    // 获取客户的语言设置状态,及設置对应的语言和URL
    public static void setCurrentLang(Context context) {
        Resources res;
        Configuration conf;

        DisplayMetrics dm;
        res = context.getResources();
        conf = res.getConfiguration();
        dm = res.getDisplayMetrics();

        return;
         /*SharedPreferences preferences = context.getSharedPreferences("setting", Context.MODE_WORLD_WRITEABLE);
         currentLang = preferences.getString("lang", PublicData.GB); // 默认是繁体版本
		Logger.i(TAG,"currentLang=" + currentLang);
		if(currentLang.equals(PublicData.GB)) // 简体中文
		{
			conf.locale = Locale.SIMPLIFIED_CHINESE;
		}else // 默认是：英文
		{
			conf.locale = Locale.ENGLISH;
		}*/


		/*String language = conf.locale.getLanguage();

		currentLang = language;
		String country = conf.locale.getCountry().toLowerCase();
		Logger.d(TAG, "--------language:" + language);
		Logger.d(TAG, "--------country:" + country);
		if ("zh".equals(language)) {
			if ("cn".equals(country)) {
				conf.locale = Locale.SIMPLIFIED_CHINESE;
			} else if ("tw".equals(country)) {
				conf.locale = Locale.SIMPLIFIED_CHINESE;
			}
		} else if ("fr".equals(language)) {
			conf.locale = Locale.FRANCE;
		}

		 else if ("ru".equals(language)) {
			//	conf.locale = Locale.
			}

		else {
			conf.locale = Locale.ENGLISH;
		}  */

        //	conf.setLocale(loc);
        //	conf.locale = Locale.FRANCE;
//		conf.locale = Locale.FRANCE;
        //	res.updateConfiguration(conf,dm);

    }

    public static boolean isBindingPedometer(Context mContext) {

        return isBindingPedometer(mContext, true); // 默认要跳转
    }

    // toActivity:是否要跳转到设置页面
    public static boolean isBindingPedometer(Context mContext, boolean toActivity) {
        // 绑定的计步器的mac地址
        String pedometerMac = (String) ConfigHelper.getSharePref(mContext, SHARED_PRE_SAVE_FILE_NAME, CURRENT_BIND_ID_ITEM, ConfigHelper.DATA_STRING);
        // 是否绑定了计步器
        if (null == pedometerMac || "".equals(pedometerMac)) {
            if (toActivity) {
//				PublicData.selectDeviceName = PublicData.L28T;		// summer: test
                ConfigHelper.setSharePref(mContext, PublicData.SHARED_PRE_SAVE_FILE_NAME, PublicData.CURRENT_BIND_DEVICE_ITEM, "");
                Logger.e(TAG, "PublicData.CURRENT_BIND_DEVICE_ITEM set null");
                Intent mIntent = new Intent(mContext, TurnOnBluetoothActivity.class);
                mContext.startActivity(mIntent);
                return false;
            } else {
                return false;
            }
        } else {
            return true;
        }

    }

    /**
     * summer: 设备类型转换为服务器所需类型
     *
     * @param deviceType
     * @return
     */
    public static String getCloudDeviceType(String deviceType) {
        String tagType = "L11";
        switch (deviceType) {
            case L39:
                tagType = "L38B";
                break;
            case L28H:
                tagType = "L28";
                break;
            case L30D:
                tagType = "L30";
                break;
            case W01:
                tagType = "W01";
                break;
            case L28T:
                tagType = "L28";
                break;
            case L38I:
                tagType = "L38I";
                break;
        }
        return tagType;
    }

    /**
     * 根据watchID转换deviceType
     *
     * @param watchId
     * @return
     */
    public static String getDeviceTypeByWatchId(String watchId) {
        String deviceType = "L38B";
        if ("".equals(watchId) || watchId == null) {
            Logger.e(TAG, "watchId is null, set default val is " + deviceType);
            return deviceType;
        }
        if (watchId.indexOf("L28") != -1) {
            deviceType = "L28T";
        } else if (watchId.indexOf("L38I") != -1) {
            deviceType = "L38I";
        } else if (watchId.indexOf("L38") != -1) {
            deviceType = "L38B";
        } else if (watchId.indexOf("L30") != -1) {
            deviceType = "L30D";
        } else if (watchId.indexOf("W01") != -1) {
            deviceType = "W01";
        }
        //TODO 适配 L91  FCL91A17040701000045
        else if (watchId.indexOf("L91") != -1) {
            deviceType = "L91";
        }
        Logger.e(TAG, "watchId is null, set default val is " + deviceType);
        return deviceType;
    }

    public static boolean checkBluetoothStatus() {
        BluetoothManager bluetoothManager = (BluetoothManager) GlobalApp.globalApp.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) { // 手机蓝牙没打开
            return false;
        }
        return true;
    }

}
