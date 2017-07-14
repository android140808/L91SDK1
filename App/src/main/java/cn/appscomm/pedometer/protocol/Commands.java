package cn.appscomm.pedometer.protocol;

/**
 * Created by Administrator on 2016/1/26.
 */
public class Commands {

    /*-----------------------------------------------开始结束标志-------------------------------------------------*/
    public static final byte FLAG_START = (byte) 0x6F;                                    // 开始标志
    public static final byte FLAG_END = (byte) 0x8F;                                      // 结束标志
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------动作-----------------------------------------------------*/
    public static final byte ACTION_CHECK = (byte) 0x70;                                  // 查询
    public static final byte ACTION_SET = (byte) 0x71;                                    // 设置
    public static final byte ACTION_CHECK_RESPONSE = (byte) 0x80;                         // 查询响应
    public static final byte ACTION_SET_RESPONSE = (byte) 0x81;                           // 设置响应
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------命令码---------------------------------------------------*/
    public static final byte COMMANDCODE_RESPONSE = (byte) 0x01;                          // 响应
    public static final byte COMMANDCODE_WATCHID = (byte) 0x02;                           // watchID
    public static final byte COMMANDCODE_UID = (byte) 0x02;                               // userID
    public static final byte COMMANDCODE_DEVICE_VERSION = (byte) 0x03;                    // 设备版本
    public static final byte COMMANDCODE_DATETIME = (byte) 0x04;                          // 日期时间
    public static final byte COMMANDCODE_TIME_SURFACE_SETTING = (byte) 0x05;              // 时间界面设置
    public static final byte COMMANDCODE_PRIMARY_SURFACE_DISPLAY_SETTING = (byte) 0x06;   // 一级界面显示设置
    public static final byte COMMANDCODE_SCREEN_BRIGHTNESS_SETTING = (byte) 0x07;         // 屏幕亮度设置
    public static final byte COMMANDCODE_BATTERY_POWER = (byte) 0x08;                     // 电池电量
    public static final byte COMMANDCODE_VOLUME = (byte) 0x09;                            // 音量
    public static final byte COMMANDCODE_SHOCK = (byte) 0x0A;                             // 震动
    public static final byte COMMANDCODE_LANGUAGE = (byte) 0x0B;                          // 语言
    public static final byte COMMANDCODE_UNIT = (byte) 0x0C;                              // 单位
    public static final byte COMMANDCODE_RESTORE_FACTORY = (byte) 0x0D;                   // 恢复出厂
    public static final byte COMMANDCODE_TIME_ORDER = (byte) 0x05;                   // 时间格式
    public static final byte COMMANDCODE_UPGRADE_MODE = (byte) 0x0E;                      // 升级模式

    public static final byte COMMANDCODE_VIBRATION = (byte) 0x10;                         // 震动强度

    public static final byte COMMANDCODE_USERINFO = (byte) 0x30;                          // 个人信息
    public static final byte COMMANDCODE_USAGE_HABITS = (byte) 0x31;                      // 使用习惯

    public static final byte COMMANDCODE_AUTO_HEART = (byte) 0x5C;                          // 心率启动
    public static final byte COMMANDCODE_TARGET_SETTING = (byte) 0x50;                    // 目标设置
    public static final byte COMMANDCODE_SPORT_SLEEP_MODE = (byte) 0x51;                  // 运动/睡眠模式
    public static final byte COMMANDCODE_TOTAL_SPORT_SLEEP_COUNT = (byte) 0x52;           // 运动/睡眠数量总数
    public static final byte COMMANDCODE_TOTAL_HEART_COUNT = (byte) 0x59;                 // 心率数量总数
    public static final byte COMMANDCODE_DELETE_SPORTDATA = (byte) 0x53;                  // 删除运动数据
    public static final byte COMMANDCODE_GET_SPORTDATA = (byte) 0x54;                     // 获取运动数据
    public static final byte COMMANDCODE_DELETE_SLEEPDATA = (byte) 0x55;                  // 删除睡眠数据
    public static final byte COMMANDCODE_DELETE_HEARTDATA = (byte) 0x5A;                  // 删除心率数据
    public static final byte COMMANDCODE_GET_SLEEPDATA = (byte) 0x56;                     // 获取睡眠数据
    public static final byte COMMANDCODE_GET_HEARTASTRICT = (byte) 0x5D;                     // 获取心率报警
    public static final byte COMMANDCODE_GET_HEARTDATA = (byte) 0x5B;                     // 获取心率数据
    public static final byte COMMANDCODE_SET_SITDATA = (byte) 0x5E;                      // 设置静坐提醒
    public static final byte COMMANDCODE_DEVICE_DISPLAY_SPORT_SLEEP = (byte) 0x57;        // 设备端显示的运动和睡眠数据
    public static final byte COMMANDCODE_AUTO_SLEEP = (byte) 0x58;                        // 设置睡眠状态(自动睡眠、马上睡眠)
    /*------------------------------------------------L91新增蓝牙命令,create by Avater 2017317-------------------------------------------------*/
    public static final byte COMMANDCODE_TOTAL_BLOOD_COUNT = (byte) 0x61;
    public static final byte COMMANDCODE_DELETE_BLOODDATA = (byte) 0x62;
    public static final byte COMMANDCODE_GET_BLOODDATA = (byte) 0x63;
    public static final byte COMMANDCODE_AUTO_SETUP_BLOODDATA = (byte) 0x64;
    /*-----------------------------------------------------------------------------------------------------------------------------------------*/

    public static final byte COMMANDCODE_PHONE_NAME_PUSH = (byte) 0x70;                   // 电话姓名推送
    public static final byte COMMANDCODE_SMS_PUSH = (byte) 0x71;                          // 短信推送
    public static final byte COMMANDCODE_MSG_COUNT_PUSH = (byte) 0x72;                    // 消息条数推送
    public static final byte COMMANDCODE_SOCIAL_COUNT_PUSH = (byte) 0x73;                 //社交推送
    public static final byte COMMANDCODE_MAIL_PUSH = (byte) 0x74;                                            // 邮件推送
    public static final byte COMMANDCODE_CALENDAR_PUSH = (byte) 0x75;                                            // 日历推送

    public static final byte COMMANDCODE_SWITCH_SETTING = (byte) 0x90;                    // 开关设置
    public static final byte COMMANDCODE_REMIND_COUNT = (byte) 0x91;                      // 提醒条数
    public static final byte COMMANDCODE_REMIND_SETTING = (byte) 0x92;                    // 提醒设置
    public static final byte COMMANDCODE_BIND_START = (byte) 0x93;                        // 绑定开始
    public static final byte COMMANDCODE_BIND_END = (byte) 0x94;                          // 绑定结束

    public static final byte COMMANDCODE_PAY_CARDNUMBER = (byte) 0xB0;                    // 支付:当前使用的芯片应用卡号
    public static final byte COMMANDCODE_PAY_MONEY = (byte) 0xB1;                         // 支付:余额
    public static final byte COMMANDCODE_PAY_RECORD = (byte) 0xB2;                        // 支付:交易记录
    public static final byte COMMANDCODE_PAY_PASSTHROUGH = (byte) 0xB3;                   // 支付:透传

    public static final byte COMMANDCODE_MUSIC = (byte) 0xD0;                             // 音乐
    public static final byte COMMANDCODE_TAKE_PHOTO = (byte) 0xD1;                        // 拍照
    public static final byte COMMANDCODE_FIND_PHONE = (byte) 0xD2;                        // 寻找手机
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------返回值---------------------------------------------------*/
    public static final int RESULTCODE_SUCCESS = 0;                                 // 成功
    public static final int RESULTCODE_FAILD = 1;                                   // 失败
    public static final int RESULTCODE_PROTOCOL_ERROR = 2;                          // 协议解析错误
    public static final int RESULTCODE_ERROR = -1;                                  // 错误
    public static final int RESULTCODE_LARGEBYTES_ERROR = -2;                       // 大字节接收错误
    public static final int RESULTCODE_CONTINUE_RECEIVING = 3;                      // 继续接收
    public static final int RESULTCODE_INDEXS_COMMAND = 4;                          // 索引号命令
    public static final int RESULTCODE_RE_SEND = 5;                                 // 重新发送命令
    public static final int RESULTCODE_CONNECT_FAILD = 6;                                   // 蓝牙连接失败

    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------提醒类型-------------------------------------------------*/
    public static byte REMINDTYPE_EAT = (byte) 0x00;                                // 吃饭
    public static byte REMINDTYPE_MEDICINE = (byte) 0x01;                           // 吃药
    public static byte REMINDTYPE_DRINK = (byte) 0x02;                              // 喝水
    public static byte REMINDTYPE_SLEEP = (byte) 0x03;                              // 睡觉
    public static byte REMINDTYPE_AWAKE = (byte) 0x04;                              // 清醒
    public static byte REMINDTYPE_SPORT = (byte) 0x05;                              // 运动
    public static byte REMINDTYPE_METTING = (byte) 0x06;                            // 会议
    public static byte REMINDTYPE_CUSTOM = (byte) 0x07;                             // 自定义
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------消息类型-------------------------------------------------*/
    public static byte MSGPUSHTYPE_MISSCALL = (byte) 0x00;                          // 未接来电
    public static byte MSGPUSHTYPE_SMS = (byte) 0x01;                               // 短信
    public static byte MSGPUSHTYPE_SOCIAL = (byte) 0x02;                            // 社交
    public static byte MSGPUSHTYPE_EMAIL = (byte) 0x03;                             // 邮件
    public static byte MSGPUSHTYPE_CALENDAR = (byte) 0x04;                          // 农历
    public static byte MSGPUSHTYPE_INCOMECALL = (byte) 0x05;                        // 来电
    public static byte MSGPUSHTYPE_OFFCALL = (byte) 0x06;                           // 来电挂断

    public static byte MSGPUSHTYPE_WECHAT = (byte) 0x07;                            // Wechat
    public static byte MSGPUSHTYPE_VIBER = (byte) 0x08;                             // Viber
    public static byte MSGPUSHTYPE_SNAPCHAT = (byte) 0x09;                          // Snapchat
    public static byte MSGPUSHTYPE_WHATSAPP = (byte) 0x0A;                          // WhatsApp
    public static byte MSGPUSHTYPE_QQ = (byte) 0x0B;                                // QQ
    public static byte MSGPUSHTYPE_FACEBOOK = (byte) 0x0C;                          // Facebook
    public static byte MSGPUSHTYPE_HANGOUTS = (byte) 0x0D;                          // Hangouts
    public static byte MSGPUSHTYPE_GMAIL = (byte) 0x0E;                             // Gmail
    public static byte MSGPUSHTYPE_MESSENGER = (byte) 0x0F;                         // Facebook Messenger
    public static byte MSGPUSHTYPE_INSTAGRAM = (byte) 0x10;                         // Instagram
    public static byte MSGPUSHTYPE_TWITTER = (byte) 0x11;                           // Twitter
    public static byte MSGPUSHTYPE_LINKEDIN = (byte) 0x12;                          // Linkedin
    public static byte MSGPUSHTYPE_UBER = (byte) 0x13;                              // Uber
    public static byte MSGPUSHTYPE_LINE = (byte) 0x14;                              // Line
    public static byte MSGPUSHTYPE_SKYPE = (byte) 0x15;                             // Skype
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------界面显示-------------------------------------------------*/
    public static int INTERFACE_DISPLAY_TIME = 1;                                   // 时间
    public static int INTERFACE_DISPLAY_STEP = 2;                                   // 步数
    public static int INTERFACE_DISPLAY_DISTANCE = 3;                               // 距离
    public static int INTERFACE_DISPLAY_CALORIE = 4;                                // 卡路里
    public static int INTERFACE_DISPLAY_SLEEP = 5;                                  // 睡眠
    public static int INTERFACE_DISPLAY_CITYCARD = 6;                               // 城市一卡通
    public static int INTERFACE_DISPLAY_BANKCARD = 7;                               // 银行卡
    /*-----------------------------------------------------------------------------------------------------------*/


    /*---------------------------------------------------震动动作-------------------------------------------------*/
    public static int SHOCKACTION_ANTI = 0;                                         // 防丢失
    public static int SHOCKACTION_CLOCK = 1;                                        // 闹钟
    public static int SHOCKACTION_INCOMECALL = 2;                                   // 来电
    public static int SHOCKACTION_MISSCALL = 3;                                     // 未接来电
    public static int SHOCKACTION_SMS = 4;                                          // 短信
    public static int SHOCKACTION_SOCIAL = 5;                                       // 社交
    public static int SHOCKACTION_EMAIL = 6;                                        // 邮件
    public static int SHOCKACTION_CALENDAR = 7;                                     // 日历
    public static int SHOCKACTION_SEDENTARY = 8;                                    // 久坐
    public static int SHOCKACTION_LOWPOWER = 9;                                     // 低电
    /*-----------------------------------------------------------------------------------------------------------*/


    public static int ACTION_SET_RESPONSE_LEN = 8;                                  // 设置响应长度 固定为8
    public static int MSGPUSHTYPE_MAXNAMELEN = 90;                                  // 消息推送姓名的最大长度
    //    public static int MSGPUSHTYPE_MAXCONTENTLEN = 120;                              // 消息推送内容的最大长度
    public static int MSGPUSHTYPE_MAXCONTENTLEN = 120;                              // 消息推送内容的最大长度

}
