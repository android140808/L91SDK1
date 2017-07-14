package cn.appscomm.pedometer.protocol;

import java.util.LinkedList;

import cn.appscomm.pedometer.model.HeartData;
import cn.appscomm.pedometer.model.HeartRateData;
import cn.appscomm.pedometer.model.RemindNotesData;
import cn.appscomm.pedometer.model.SleepData;
import cn.appscomm.pedometer.model.SportsData;

/**
 * Created by Administrator on 2016/1/28.
 */
public class GlobalVar {
    public static String watchID;           // watchID


    public static String deviceType;        // 设备类型
    public static String softVersion;       // 软件版本
    public static String hardwareVersion;   // 硬件版本
    public static String commPprotocol;     // 通信协议
    public static String functionVersion;   // 功能版本
    public static String otherVersion;      // 其他
    public static String allVersion;      // 其他

    public static String binUid;      // 绑定的UID

    public static String heartVersion;           // heartVersion
    public static String fontVersion;           // fontVersion

    public static int sportCount;           // 运动条数
    public static int heartCount;           // 心率条数
    public static int sleepCount;           // 睡眠条数
    public static int batteryPower;         // 电量
    public static int sportSleepMode;       // 运动/睡眠模式

    public static LinkedList<SportsData> sportsDatas;          // 存储运动数据
    public static LinkedList<SleepData> sleepDatas;            // 存储睡眠数据
    public static LinkedList<HeartData> heartDatas;            // 存储心率数据
    public static LinkedList<HeartRateData> heartRateDatas;     // 存储心率数据
    public static LinkedList<Integer> indexsResendCommand;     // 需要单独获取的索引号集合

    public static int stepGoalsValue;      // 步数目标值 单位是步
    public static int stepGoalsFlag;       // 步数目标标志
    public static int calorieGoalsValue;   // 卡路里目标 单位是千卡
    public static int calorieGoalsFlag;    // 卡路里目标标志
    public static int distanceGoalsValue;  // 距离目标 单位是其千米
    public static int distanceGoalsFlag;   // 距离目标标志
    public static int sleepGoalsValue;     // 睡眠时间目标 单位是小时
    public static int sleepGoalsFlag;      // 睡眠时间目标标志
    public static int stepTimeGoalsValue;     // 运动时长目标 单位是小时
    public static int stepTimeGoalsFlag;      // 运动时长目标标志
    public static int distanceChance;

    public static int dateFormat;          // 日期格式
    public static int timeFormat;          // 时间格式
    public static int lunarFormat;         // 农历格式
    public static int batteryFormat;       // 电池格式
    public static int screenFormat;        // 屏幕格式
    public static int heartRateFormat;        // 心率格式
    public static int userNameFormat;        // 用户名格式格式

    public static int screenBrightness;    // 屏幕亮度

    public static int volume;              // 音量
    public static int vibration;              // 震动强度
    public static int autoHeart = 0;              // 自动检测心率范围开关
    public static int autoTrack;              // 自动检测心率时间开关
    public static int topHeart;              // 上线心率时间
    public static int insertingHeart;              // 下线心率时间


    public static boolean onInactivity;
    public static int intervalInactivity;
    public static int startInactivity;
    public static int endInactivity;
    public static String weekInactivity;
    public static long stepsInactivity;
    public static int language;            // 语言

    public static int unit;                // 单位

    public static int sex;                 // 性别
    public static int age;                 // 年龄
    public static int height;              // 身高
    public static float weight;            // 体重

    public static int usageHabits;         // 用户习惯

    public static int deviceDisplayStep;        // 设备端显示的步数
    public static int deviceDisplayCalorie;     // 设备端显示的卡路里
    public static int deviceDisplayDistance;    // 设备端显示的距离
    public static int deviceDisplaySleep;       // 设备端显示的睡眠
    public static int deviceDisplayStepTime;       // 设备端显示的运动时长

    public static boolean antiLostSwitch = false;     // 防丢开关
    public static boolean autoSyncSwitch = false;     // 自动同步开关
    public static boolean sleepSwitch = false;        // 睡眠开关
    public static boolean sleepStateSwitch = false;   // 自动睡眠监测开关
    public static boolean incomePhoneSwitch = false;  // 来电提醒开关
    public static boolean missPhoneSwitch = false;    // 未接来电提醒开关
    public static boolean smsSwitch = false;          // 短信提醒开关
    public static boolean socialSwitch = false;       // 社交提醒开关
    public static boolean mailSwitch = false;         // 邮件提醒开关
    public static boolean calendarSwitch = false;     // 日历开关
    public static boolean sedentarySwitch = false;    // 久坐提醒开关
    public static boolean lowPowerSwitch = false;     // 超低功耗功能开关
    public static boolean secondRemindSwitch = false; // 二次提醒开关

    public static int enterSleepHour;       // 进入睡眠时
    public static int enterSleepMin;        // 进入睡眠分
    public static int quitSleepHour;        // 退出睡眠时
    public static int quitSleepMin;         // 退出睡眠分
    public static int remindSleepCycle;     // 提醒睡眠周期

    public static String cardNumber;       // 卡号
    public static String money;            // 余额
    public static StringBuffer record;     // 记录
    public static int recordCount;         // 记录条数
    public static boolean haveRecord;      // 是否有记录

    public static int remindCount;                                 // 提醒条数
    public static LinkedList<RemindNotesData> remindNotesDatas;    // 所有提醒数据

    public static int[] arrItems;          // 界面显示

    public static int[] shockItems;        // 震动

    public static byte playStatus;         // 播放状态
    public static String songName;         // 歌曲名字

}
